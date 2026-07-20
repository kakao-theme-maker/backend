#!/usr/bin/env bash

set -Eeuo pipefail

TEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly TEST_DIR
readonly DEPLOY_SCRIPT="${TEST_DIR}/../deploy-backend.sh"
readonly ORIGINAL_PATH="${PATH}"
TARGET_IMAGE="ghcr.io/kakao-theme-maker/backend@sha256:$(printf '2%.0s' {1..64})"
PREVIOUS_IMAGE="ghcr.io/kakao-theme-maker/backend@sha256:$(printf '1%.0s' {1..64})"
readonly TARGET_IMAGE PREVIOUS_IMAGE

fail() {
  echo "FAIL: $*" >&2
  exit 1
}

assert_file_contains() {
  local file="$1"
  local expected="$2"
  [[ "$(<"${file}")" == *"${expected}"* ]] \
    || fail "${file} does not contain: ${expected}"
}

create_fake_docker() {
  local bin_dir="$1"

  mkdir -p "${bin_dir}"
  cat >"${bin_dir}/docker" <<'FAKE_DOCKER'
#!/usr/bin/env bash

set -Eeuo pipefail

printf '%s\n' "$*" >>"${FAKE_DOCKER_STATE_DIR}/commands.log"

if [[ "$1" == "inspect" ]]; then
  if [[ "$3" == *".Config.Image"* ]]; then
    [[ -f "${FAKE_DOCKER_STATE_DIR}/current-image" ]] \
      && cat "${FAKE_DOCKER_STATE_DIR}/current-image"
    exit 0
  fi

  current_image="$(<"${FAKE_DOCKER_STATE_DIR}/current-image")"
  if [[ "${current_image}" == "${FAKE_TARGET_IMAGE}" ]]; then
    case "${FAKE_DOCKER_MODE}" in
      success)
        echo "healthy"
        ;;
      timeout)
        echo "starting"
        ;;
      *)
        echo "unhealthy"
        ;;
    esac
  else
    echo "healthy"
  fi
  exit 0
fi

[[ "$1" == "compose" ]] || exit 1
: "${BACKEND_IMAGE:?BACKEND_IMAGE must be set for every Compose invocation}"
shift
if [[ "$1" == "-f" ]]; then
  shift 2
fi

case "$1" in
  ps)
    if [[ -f "${FAKE_DOCKER_STATE_DIR}/current-image" ]]; then
      echo "backend-container"
    fi
    ;;
  pull)
    ;;
  up)
    printf '%s' "${BACKEND_IMAGE}" >"${FAKE_DOCKER_STATE_DIR}/current-image"
    ;;
  stop)
    rm -f "${FAKE_DOCKER_STATE_DIR}/current-image"
    ;;
  *)
    exit 1
    ;;
esac
FAKE_DOCKER
  chmod +x "${bin_dir}/docker"
}

new_case() {
  local case_dir="$1"
  mkdir -p "${case_dir}/state"
  : >"${case_dir}/state/commands.log"
  : >"${case_dir}/docker-compose.yml"
  create_fake_docker "${case_dir}/bin"
}

test_successful_deployment() {
  local case_dir="${TMP_DIR}/success"
  new_case "${case_dir}"
  printf '%s' "${PREVIOUS_IMAGE}" >"${case_dir}/state/current-image"

  PATH="${case_dir}/bin:${ORIGINAL_PATH}" \
  FAKE_DOCKER_STATE_DIR="${case_dir}/state" \
  FAKE_DOCKER_MODE=success \
  FAKE_TARGET_IMAGE="${TARGET_IMAGE}" \
  COMPOSE_FILE="${case_dir}/docker-compose.yml" \
  HEALTH_TIMEOUT_SECONDS=1 \
  HEALTH_POLL_INTERVAL_SECONDS=1 \
  DEPLOY_SEQUENCE=10 \
  DEPLOY_SEQUENCE_FILE="${case_dir}/deploy-sequence" \
    "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}"

  [[ "$(<"${case_dir}/state/current-image")" == "${TARGET_IMAGE}" ]] \
    || fail "successful deployment did not keep the target image"
  assert_file_contains "${case_dir}/state/commands.log" "pull backend"
  assert_file_contains "${case_dir}/state/commands.log" "up -d --no-deps backend"
  assert_file_contains "${case_dir}/state/commands.log" "ps --all -q backend"
  [[ "$(<"${case_dir}/deploy-sequence")" == "10" ]] \
    || fail "successful deployment did not record its sequence"
}

test_failed_deployment_rolls_back() {
  local case_dir="${TMP_DIR}/rollback"
  local output
  new_case "${case_dir}"
  printf '%s' "${PREVIOUS_IMAGE}" >"${case_dir}/state/current-image"

  if output="$(PATH="${case_dir}/bin:${ORIGINAL_PATH}" \
      FAKE_DOCKER_STATE_DIR="${case_dir}/state" \
      FAKE_DOCKER_MODE=timeout \
      FAKE_TARGET_IMAGE="${TARGET_IMAGE}" \
      COMPOSE_FILE="${case_dir}/docker-compose.yml" \
      HEALTH_TIMEOUT_SECONDS=1 \
      HEALTH_POLL_INTERVAL_SECONDS=1 \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" 2>&1)"; then
    fail "a failed deployment must exit with a non-zero status"
  fi

  [[ "$(<"${case_dir}/state/current-image")" == "${PREVIOUS_IMAGE}" ]] \
    || fail "rollback did not restore the previous image"
  [[ "${output}" == *"Rollback completed with previous backend image"* ]] \
    || fail "rollback completion was not reported"
  [[ "${output}" == *"did not become healthy within 1 seconds"* ]] \
    || fail "health timeout was not reported"
  assert_file_contains "${case_dir}/state/commands.log" "up -d --no-deps backend"
  [[ "$(<"${case_dir}/state/commands.log")" != *"up -d --no-deps database"* ]] \
    || fail "database must never be recreated by backend deployment"
  [[ "$(<"${case_dir}/state/commands.log")" != *"up -d --no-deps redis"* ]] \
    || fail "redis must never be recreated by backend deployment"
}

test_failed_first_deployment_stops_backend() {
  local case_dir="${TMP_DIR}/first-deployment"
  new_case "${case_dir}"

  if PATH="${case_dir}/bin:${ORIGINAL_PATH}" \
      FAKE_DOCKER_STATE_DIR="${case_dir}/state" \
      FAKE_DOCKER_MODE=first-deployment \
      FAKE_TARGET_IMAGE="${TARGET_IMAGE}" \
      COMPOSE_FILE="${case_dir}/docker-compose.yml" \
      HEALTH_TIMEOUT_SECONDS=1 \
      HEALTH_POLL_INTERVAL_SECONDS=1 \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}"; then
    fail "a failed first deployment must exit with a non-zero status"
  fi

  [[ ! -f "${case_dir}/state/current-image" ]] \
    || fail "failed first-deployment backend must be stopped"
  assert_file_contains "${case_dir}/state/commands.log" "stop backend"
}

test_mutable_tag_is_rejected() {
  if "${DEPLOY_SCRIPT}" "ghcr.io/kakao-theme-maker/backend:dev" >/dev/null 2>&1; then
    fail "mutable image tags must be rejected"
  fi
}

test_stale_deployment_is_skipped() {
  local case_dir="${TMP_DIR}/stale"
  local output
  new_case "${case_dir}"
  printf '%s' "${PREVIOUS_IMAGE}" >"${case_dir}/state/current-image"
  printf '11\n' >"${case_dir}/deploy-sequence"

  output="$(PATH="${case_dir}/bin:${ORIGINAL_PATH}" \
      FAKE_DOCKER_STATE_DIR="${case_dir}/state" \
      FAKE_DOCKER_MODE=success \
      FAKE_TARGET_IMAGE="${TARGET_IMAGE}" \
      COMPOSE_FILE="${case_dir}/docker-compose.yml" \
      DEPLOY_SEQUENCE=10 \
      DEPLOY_SEQUENCE_FILE="${case_dir}/deploy-sequence" \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}")"

  [[ "${output}" == *"Skipping stale deployment sequence 10"* ]] \
    || fail "stale deployment was not reported"
  [[ ! -s "${case_dir}/state/commands.log" ]] \
    || fail "stale deployment must not invoke Docker"
  [[ "$(<"${case_dir}/state/current-image")" == "${PREVIOUS_IMAGE}" ]] \
    || fail "stale deployment changed the running image"
}

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

test_successful_deployment
test_failed_deployment_rolls_back
test_failed_first_deployment_stops_backend
test_mutable_tag_is_rejected
test_stale_deployment_is_skipped

echo "All deploy-backend tests passed."
