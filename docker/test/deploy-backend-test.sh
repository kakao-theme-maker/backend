#!/usr/bin/env bash

set -Eeuo pipefail

TEST_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly TEST_DIR
readonly DEPLOY_SCRIPT="${TEST_DIR}/../deploy-backend.sh"
readonly ORIGINAL_PATH="${PATH}"
TARGET_IMAGE="ghcr.io/kakao-theme-maker/backend@sha256:$(printf '2%.0s' {1..64})"
PREVIOUS_IMAGE="ghcr.io/kakao-theme-maker/backend@sha256:$(printf '1%.0s' {1..64})"
readonly LEGACY_IMAGE="louie8821/kakao-theme-maker:v1-dev"
readonly MUTABLE_GHCR_IMAGE="ghcr.io/kakao-theme-maker/backend:dev"
BACKEND_CONFIG_SHA_MISMATCH="$(printf '0%.0s' {1..40})"
readonly BACKEND_CONFIG_SHA_MISMATCH
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

  if [[ "$3" == *".Config.Healthcheck"* ]]; then
    echo "${FAKE_PREVIOUS_HAS_HEALTHCHECK:-true}"
    exit 0
  fi

  if [[ "$3" == *"else}}none"* ]]; then
    echo "${FAKE_PREVIOUS_HEALTH_STATUS:-healthy}"
    exit 0
  fi

  if [[ "$3" == *".State.Running"* ]]; then
    if [[ "${FAKE_DOCKER_STOP_FAIL:-false}" == "true" ]]; then
      echo "true"
    else
      echo "false"
    fi
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
    echo "${FAKE_ROLLBACK_HEALTH_STATUS:-healthy}"
  fi
  exit 0
fi

if [[ "$1" == "ps" ]]; then
  if [[ -f "${FAKE_DOCKER_STATE_DIR}/current-image" ]]; then
    echo "backend-container"
  fi
  exit 0
fi

if [[ "$1" == "stop" ]]; then
  if [[ "${FAKE_DOCKER_STOP_FAIL:-false}" == "true" ]]; then
    exit 1
  fi
  rm -f "${FAKE_DOCKER_STATE_DIR}/current-image"
  exit 0
fi

[[ "$1" == "compose" ]] || exit 1
: "${BACKEND_IMAGE:?BACKEND_IMAGE must be set for every Compose invocation}"
shift
[[ "$1" == "--env-file" ]] || exit 1
[[ "$2" == "${FAKE_COMPOSE_ENV_FILE}" ]] || exit 1
shift 2
while [[ "$1" == "-f" ]]; do
  [[ -n "${2:-}" ]] || exit 1
  shift 2
done

case "$1" in
  ps)
    if [[ -f "${FAKE_DOCKER_STATE_DIR}/current-image" ]]; then
      echo "backend-container"
    fi
    ;;
  pull)
    ;;
  up)
    if [[ "${FAKE_ROLLBACK_UP_FAIL:-false}" == "true" \
        && "${BACKEND_IMAGE}" != "${FAKE_TARGET_IMAGE}" ]]; then
      exit 1
    fi
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
  mkdir -p "${case_dir}/state" "${case_dir}/backend_config/dev/monolithic"
  : >"${case_dir}/state/commands.log"
  : >"${case_dir}/docker-compose.yml"
  : >"${case_dir}/docker-compose.deploy.yml"
  : >"${case_dir}/docker-compose.deploy.prod.yml"
  printf 'spring:\n  application:\n    name: test\n' \
    >"${case_dir}/backend_config/dev/monolithic/application.yml"
  printf 'spring:\n  config:\n    activate:\n      on-profile: dev\n' \
    >"${case_dir}/backend_config/dev/monolithic/application-dev.yml"
  printf 'spring:\n  config:\n    activate:\n      on-profile: prod\n' \
    >"${case_dir}/backend_config/dev/monolithic/application-prod.yml"
  git -C "${case_dir}/backend_config" init -q
  git -C "${case_dir}/backend_config" config user.name test
  git -C "${case_dir}/backend_config" config user.email test@example.com
  git -C "${case_dir}/backend_config" config commit.gpgSign false
  git -C "${case_dir}/backend_config" add dev
  git -C "${case_dir}/backend_config" commit -qm 'test config'
  cat >"${case_dir}/backend.env" <<EOF
COMPOSE_PROJECT_NAME=test
BACKEND_BIND_ADDRESS=127.0.0.1
BACKEND_PORT=28080
SPRING_PROFILES_ACTIVE=dev
MYSQL_ROOT_PASSWORD=test
MYSQL_DATABASE=test
MYSQL_ROOT_HOST=%
EOF
  export BACKEND_CONFIG_DIR_OVERRIDE="${case_dir}/backend_config"
  create_fake_docker "${case_dir}/bin"
}

case_config_sha() {
  git -C "$1/backend_config" rev-parse HEAD
}

set_case_profile() {
  local case_dir="$1"
  local profile="$2"
  local env_tmp="${case_dir}/backend.env.tmp"

  awk -v profile="${profile}" '
    /^SPRING_PROFILES_ACTIVE=/ { print "SPRING_PROFILES_ACTIVE=" profile; next }
    { print }
  ' "${case_dir}/backend.env" >"${env_tmp}"
  mv "${env_tmp}" "${case_dir}/backend.env"
}

remove_env_key() {
  local case_dir="$1"
  local key="$2"
  local env_tmp="${case_dir}/backend.env.tmp"

  awk -v key="${key}" 'index($0, key "=") != 1' \
    "${case_dir}/backend.env" >"${env_tmp}"
  mv "${env_tmp}" "${case_dir}/backend.env"
}

test_successful_deployment() {
  local case_dir="${TMP_DIR}/success"
  new_case "${case_dir}"
  printf '%s' "${PREVIOUS_IMAGE}" >"${case_dir}/state/current-image"

  PATH="${case_dir}/bin:${ORIGINAL_PATH}" \
  FAKE_DOCKER_STATE_DIR="${case_dir}/state" \
  FAKE_DOCKER_MODE=success \
  FAKE_TARGET_IMAGE="${TARGET_IMAGE}" \
  FAKE_COMPOSE_ENV_FILE="${case_dir}/backend.env" \
  COMPOSE_FILE="${case_dir}/docker-compose.yml" \
  COMPOSE_ENV_FILE="${case_dir}/backend.env" \
  HEALTH_TIMEOUT_SECONDS=1 \
  HEALTH_POLL_INTERVAL_SECONDS=1 \
  DEPLOY_SEQUENCE=10 \
  DEPLOY_SEQUENCE_FILE="${case_dir}/deploy-sequence" \
    "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev "$(case_config_sha "${case_dir}")"

  [[ "$(<"${case_dir}/state/current-image")" == "${TARGET_IMAGE}" ]] \
    || fail "successful deployment did not keep the target image"
  assert_file_contains "${case_dir}/state/commands.log" "pull backend"
  assert_file_contains "${case_dir}/state/commands.log" "up -d --no-deps backend"
  assert_file_contains "${case_dir}/state/commands.log" "ps --all -q backend"
  assert_file_contains "${case_dir}/state/commands.log" \
    "compose --env-file ${case_dir}/backend.env -f ${case_dir}/docker-compose.yml"
  [[ "$(<"${case_dir}/state/commands.log")" != *"docker-compose.deploy.prod.yml"* ]] \
    || fail "dev deployment must not use the production override"
  if grep -q '^ps ' "${case_dir}/state/commands.log"; then
    fail "backend lookup must remain scoped to the Compose project"
  fi
  [[ "$(<"${case_dir}/deploy-sequence")" == "10" ]] \
    || fail "successful deployment did not record its sequence"
}

test_default_compose_env_file_is_script_relative() {
  local case_dir="${TMP_DIR}/default-compose-env"
  new_case "${case_dir}"
  cp "${DEPLOY_SCRIPT}" "${case_dir}/deploy-backend.sh"
  chmod +x "${case_dir}/deploy-backend.sh"
  printf '%s' "${PREVIOUS_IMAGE}" >"${case_dir}/state/current-image"

  PATH="${case_dir}/bin:${ORIGINAL_PATH}" \
  FAKE_DOCKER_STATE_DIR="${case_dir}/state" \
  FAKE_DOCKER_MODE=success \
  FAKE_TARGET_IMAGE="${TARGET_IMAGE}" \
  FAKE_COMPOSE_ENV_FILE="${case_dir}/backend.env" \
  HEALTH_TIMEOUT_SECONDS=1 \
  HEALTH_POLL_INTERVAL_SECONDS=1 \
    "${case_dir}/deploy-backend.sh" "${TARGET_IMAGE}" dev \
      "$(case_config_sha "${case_dir}")"

  assert_file_contains "${case_dir}/state/commands.log" \
    "compose --env-file ${case_dir}/backend.env -f ${case_dir}/docker-compose.deploy.yml"
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
      FAKE_PREVIOUS_HAS_HEALTHCHECK=true \
      FAKE_PREVIOUS_HEALTH_STATUS=healthy \
      FAKE_COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      COMPOSE_FILE="${case_dir}/docker-compose.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      HEALTH_TIMEOUT_SECONDS=1 \
      HEALTH_POLL_INTERVAL_SECONDS=1 \
      DEPLOY_SEQUENCE=12 \
      DEPLOY_SEQUENCE_FILE="${case_dir}/deploy-sequence" \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
        "$(case_config_sha "${case_dir}")" 2>&1)"; then
    fail "a failed deployment must exit with a non-zero status"
  fi

  [[ "$(<"${case_dir}/state/current-image")" == "${PREVIOUS_IMAGE}" ]] \
    || fail "rollback did not restore the previous image"
  [[ "${output}" == *"Rollback completed with previous backend image"* ]] \
    || fail "rollback completion was not reported"
  [[ "${output}" == *"did not become healthy within 1 seconds"* ]] \
    || fail "health timeout was not reported"
  assert_file_contains "${case_dir}/state/commands.log" "up -d --no-deps backend"
  [[ "$(awk '$0 ~ /up -d --no-deps backend/ { count++ } END { print count + 0 }' \
      "${case_dir}/state/commands.log")" == "2" ]] \
    || fail "eligible rollback must recreate the previous backend once"
  [[ ! -e "${case_dir}/deploy-sequence" ]] \
    || fail "failed deployment must not update its deploy sequence"
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
      FAKE_COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      COMPOSE_FILE="${case_dir}/docker-compose.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      HEALTH_TIMEOUT_SECONDS=1 \
      HEALTH_POLL_INTERVAL_SECONDS=1 \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
        "$(case_config_sha "${case_dir}")"; then
    fail "a failed first deployment must exit with a non-zero status"
  fi

  [[ ! -f "${case_dir}/state/current-image" ]] \
    || fail "failed first-deployment backend must be stopped"
  assert_file_contains "${case_dir}/state/commands.log" "stop backend-container"
}

test_failed_rollback_start_stops_target() {
  local case_dir="${TMP_DIR}/rollback-up-failure"
  local output
  new_case "${case_dir}"
  printf '%s' "${PREVIOUS_IMAGE}" >"${case_dir}/state/current-image"

  if output="$(PATH="${case_dir}/bin:${ORIGINAL_PATH}" \
      FAKE_DOCKER_STATE_DIR="${case_dir}/state" \
      FAKE_DOCKER_MODE=timeout \
      FAKE_TARGET_IMAGE="${TARGET_IMAGE}" \
      FAKE_PREVIOUS_HAS_HEALTHCHECK=true \
      FAKE_PREVIOUS_HEALTH_STATUS=healthy \
      FAKE_ROLLBACK_UP_FAIL=true \
      FAKE_COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      COMPOSE_FILE="${case_dir}/docker-compose.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      HEALTH_TIMEOUT_SECONDS=1 \
      HEALTH_POLL_INTERVAL_SECONDS=1 \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
        "$(case_config_sha "${case_dir}")" 2>&1)"; then
    fail "a rollback start failure must exit with a non-zero status"
  fi

  [[ ! -f "${case_dir}/state/current-image" ]] \
    || fail "target must be stopped when rollback cannot start"
  [[ "${output}" == *"Rollback failed while recreating"* ]] \
    || fail "rollback start failure was not reported"
  [[ "${output}" == *"verified DB storage or dump"* ]] \
    || fail "manual recovery was not reported after rollback start failure"
}

test_unhealthy_rollback_image_is_stopped() {
  local case_dir="${TMP_DIR}/rollback-health-failure"
  local output
  new_case "${case_dir}"
  printf '%s' "${PREVIOUS_IMAGE}" >"${case_dir}/state/current-image"

  if output="$(PATH="${case_dir}/bin:${ORIGINAL_PATH}" \
      FAKE_DOCKER_STATE_DIR="${case_dir}/state" \
      FAKE_DOCKER_MODE=timeout \
      FAKE_TARGET_IMAGE="${TARGET_IMAGE}" \
      FAKE_PREVIOUS_HAS_HEALTHCHECK=true \
      FAKE_PREVIOUS_HEALTH_STATUS=healthy \
      FAKE_ROLLBACK_HEALTH_STATUS=unhealthy \
      FAKE_COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      COMPOSE_FILE="${case_dir}/docker-compose.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      HEALTH_TIMEOUT_SECONDS=1 \
      HEALTH_POLL_INTERVAL_SECONDS=1 \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
        "$(case_config_sha "${case_dir}")" 2>&1)"; then
    fail "an unhealthy rollback image must exit with a non-zero status"
  fi

  [[ ! -f "${case_dir}/state/current-image" ]] \
    || fail "unhealthy rollback image must be stopped"
  [[ "${output}" == *"Rollback image did not become healthy"* ]] \
    || fail "rollback health failure was not reported"
}

test_failed_target_stop_failure_is_reported() {
  local case_dir="${TMP_DIR}/stop-failure"
  local output
  new_case "${case_dir}"

  if output="$(PATH="${case_dir}/bin:${ORIGINAL_PATH}" \
      FAKE_DOCKER_STATE_DIR="${case_dir}/state" \
      FAKE_DOCKER_MODE=first-deployment \
      FAKE_DOCKER_STOP_FAIL=true \
      FAKE_TARGET_IMAGE="${TARGET_IMAGE}" \
      FAKE_COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      COMPOSE_FILE="${case_dir}/docker-compose.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      HEALTH_TIMEOUT_SECONDS=1 \
      HEALTH_POLL_INTERVAL_SECONDS=1 \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
        "$(case_config_sha "${case_dir}")" 2>&1)"; then
    fail "a target stop failure must exit with a non-zero status"
  fi

  [[ "$(<"${case_dir}/state/current-image")" == "${TARGET_IMAGE}" ]] \
    || fail "stop failure test must retain the running target state"
  [[ "${output}" == *"Failed to stop backend image"* ]] \
    || fail "docker stop failure was not reported"
  [[ "${output}" == *"Manual intervention is required to stop the failed target"* ]] \
    || fail "manual stop intervention was not reported"
}

assert_failed_deployment_is_not_rolled_back() {
  local case_name="$1"
  local previous_image="$2"
  local previous_has_healthcheck="$3"
  local previous_health_status="$4"
  local expected_reason="$5"
  local case_dir="${TMP_DIR}/${case_name}"
  local output
  new_case "${case_dir}"
  printf '%s' "${previous_image}" >"${case_dir}/state/current-image"

  if output="$(PATH="${case_dir}/bin:${ORIGINAL_PATH}" \
      FAKE_DOCKER_STATE_DIR="${case_dir}/state" \
      FAKE_DOCKER_MODE=timeout \
      FAKE_TARGET_IMAGE="${TARGET_IMAGE}" \
      FAKE_PREVIOUS_HAS_HEALTHCHECK="${previous_has_healthcheck}" \
      FAKE_PREVIOUS_HEALTH_STATUS="${previous_health_status}" \
      FAKE_COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      COMPOSE_FILE="${case_dir}/docker-compose.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      HEALTH_TIMEOUT_SECONDS=1 \
      HEALTH_POLL_INTERVAL_SECONDS=1 \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
        "$(case_config_sha "${case_dir}")" 2>&1)"; then
    fail "${case_name}: an ineligible rollback must exit with a non-zero status"
  fi

  [[ ! -f "${case_dir}/state/current-image" ]] \
    || fail "${case_name}: failed target must be stopped"
  [[ "${output}" == *"${expected_reason}"* ]] \
    || fail "${case_name}: rollback rejection reason was not reported"
  [[ "${output}" == *"original DB storage or verified dump"* ]] \
    || fail "${case_name}: manual DB recovery was not reported"
  [[ "$(awk '$0 ~ /up -d --no-deps backend/ { count++ } END { print count + 0 }' \
      "${case_dir}/state/commands.log")" == "1" ]] \
    || fail "${case_name}: previous image must not be recreated"
}

test_mutable_tag_is_rejected() {
  if "${DEPLOY_SCRIPT}" "ghcr.io/kakao-theme-maker/backend:dev" dev \
      "${BACKEND_CONFIG_SHA_MISMATCH}" >/dev/null 2>&1; then
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
      FAKE_COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      COMPOSE_FILE="${case_dir}/docker-compose.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      DEPLOY_SEQUENCE=10 \
      DEPLOY_SEQUENCE_FILE="${case_dir}/deploy-sequence" \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
        "$(case_config_sha "${case_dir}")")"

  [[ "${output}" == *"Skipping stale deployment sequence 10"* ]] \
    || fail "stale deployment was not reported"
  [[ ! -s "${case_dir}/state/commands.log" ]] \
    || fail "stale deployment must not invoke Docker"
  [[ "$(<"${case_dir}/state/current-image")" == "${PREVIOUS_IMAGE}" ]] \
    || fail "stale deployment changed the running image"
}

test_prod_uses_compose_override() {
  local case_dir="${TMP_DIR}/prod-override"
  new_case "${case_dir}"
  set_case_profile "${case_dir}" prod
  printf '%s' "${PREVIOUS_IMAGE}" >"${case_dir}/state/current-image"

  PATH="${case_dir}/bin:${ORIGINAL_PATH}" \
  FAKE_DOCKER_STATE_DIR="${case_dir}/state" \
  FAKE_DOCKER_MODE=success \
  FAKE_TARGET_IMAGE="${TARGET_IMAGE}" \
  FAKE_COMPOSE_ENV_FILE="${case_dir}/backend.env" \
  COMPOSE_FILE="${case_dir}/docker-compose.deploy.yml" \
  COMPOSE_PROD_FILE="${case_dir}/docker-compose.deploy.prod.yml" \
  COMPOSE_ENV_FILE="${case_dir}/backend.env" \
  HEALTH_TIMEOUT_SECONDS=1 \
  HEALTH_POLL_INTERVAL_SECONDS=1 \
    "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" prod "$(case_config_sha "${case_dir}")"

  assert_file_contains "${case_dir}/state/commands.log" \
    "compose --env-file ${case_dir}/backend.env -f ${case_dir}/docker-compose.deploy.yml -f ${case_dir}/docker-compose.deploy.prod.yml"
}

test_profile_mismatch_is_rejected() {
  local case_dir="${TMP_DIR}/profile-mismatch"
  local output
  new_case "${case_dir}"

  if output="$(COMPOSE_FILE="${case_dir}/docker-compose.deploy.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" prod \
        "$(case_config_sha "${case_dir}")" 2>&1)"; then
    fail "profile mismatch must be rejected"
  fi
  [[ "${output}" == *"Expected Spring profile prod, but backend.env selects dev"* ]] \
    || fail "profile mismatch reason was not reported"
}

test_unsupported_env_profile_is_rejected() {
  local case_dir="${TMP_DIR}/unsupported-profile"
  local output
  new_case "${case_dir}"
  set_case_profile "${case_dir}" test

  if output="$(COMPOSE_FILE="${case_dir}/docker-compose.deploy.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
        "$(case_config_sha "${case_dir}")" 2>&1)"; then
    fail "unsupported backend.env profile must be rejected"
  fi
  [[ "${output}" == *"SPRING_PROFILES_ACTIVE must contain exactly dev or prod"* ]] \
    || fail "unsupported profile reason was not reported"
}

test_missing_isolation_value_is_rejected() {
  local key
  local case_dir
  local output

  for key in COMPOSE_PROJECT_NAME BACKEND_BIND_ADDRESS BACKEND_PORT; do
    case_dir="${TMP_DIR}/missing-${key}"
    new_case "${case_dir}"
    remove_env_key "${case_dir}" "${key}"

    if output="$(COMPOSE_FILE="${case_dir}/docker-compose.deploy.yml" \
        COMPOSE_ENV_FILE="${case_dir}/backend.env" \
        "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
          "$(case_config_sha "${case_dir}")" 2>&1)"; then
      fail "missing ${key} must be rejected"
    fi
    [[ "${output}" == *"must contain exactly one ${key} entry"* ]] \
      || fail "missing ${key} reason was not reported"
    [[ ! -s "${case_dir}/state/commands.log" ]] \
      || fail "missing ${key} must be rejected before Docker runs"
  done
}

test_invalid_backend_port_is_rejected() {
  local case_dir="${TMP_DIR}/invalid-backend-port"
  local output
  new_case "${case_dir}"
  sed -i.bak 's/^BACKEND_PORT=.*/BACKEND_PORT=70000/' "${case_dir}/backend.env"

  if output="$(COMPOSE_FILE="${case_dir}/docker-compose.deploy.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
        "$(case_config_sha "${case_dir}")" 2>&1)"; then
    fail "invalid backend port must be rejected"
  fi
  [[ "${output}" == *"BACKEND_PORT must be an integer from 1 to 65535"* ]] \
    || fail "invalid backend port reason was not reported"
}

test_backend_config_sha_mismatch_is_rejected() {
  local case_dir="${TMP_DIR}/config-sha-mismatch"
  local output
  new_case "${case_dir}"

  if output="$(COMPOSE_FILE="${case_dir}/docker-compose.deploy.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
        "${BACKEND_CONFIG_SHA_MISMATCH}" 2>&1)"; then
    fail "backend_config SHA mismatch must be rejected"
  fi
  [[ "${output}" == *"backend_config HEAD does not match the expected SHA"* ]] \
    || fail "backend_config SHA mismatch reason was not reported"
}

test_dirty_backend_config_is_rejected() {
  local case_dir="${TMP_DIR}/dirty-config"
  local output
  new_case "${case_dir}"
  printf '\n# dirty\n' >>"${case_dir}/backend_config/dev/monolithic/application.yml"

  if output="$(COMPOSE_FILE="${case_dir}/docker-compose.deploy.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
        "$(case_config_sha "${case_dir}")" 2>&1)"; then
    fail "dirty backend_config checkout must be rejected"
  fi
  [[ "${output}" == *"backend_config checkout must be clean"* ]] \
    || fail "dirty backend_config reason was not reported"
}

test_missing_profile_config_is_rejected() {
  local case_dir="${TMP_DIR}/missing-profile-config"
  local output
  new_case "${case_dir}"
  git -C "${case_dir}/backend_config" rm -q dev/monolithic/application-dev.yml
  git -C "${case_dir}/backend_config" commit -qm 'remove dev config'

  if output="$(COMPOSE_FILE="${case_dir}/docker-compose.deploy.yml" \
      COMPOSE_ENV_FILE="${case_dir}/backend.env" \
      "${DEPLOY_SCRIPT}" "${TARGET_IMAGE}" dev \
        "$(case_config_sha "${case_dir}")" 2>&1)"; then
    fail "missing profile config must be rejected"
  fi
  [[ "${output}" == *"backend_config common and dev application files must be readable"* ]] \
    || fail "missing profile config reason was not reported"
}

TMP_DIR="$(mktemp -d)"
trap 'rm -rf "${TMP_DIR}"' EXIT

test_successful_deployment
test_default_compose_env_file_is_script_relative
test_failed_deployment_rolls_back
test_failed_first_deployment_stops_backend
test_failed_rollback_start_stops_target
test_unhealthy_rollback_image_is_stopped
test_failed_target_stop_failure_is_reported
assert_failed_deployment_is_not_rolled_back \
  legacy "${LEGACY_IMAGE}" true healthy \
  "previous image is not an immutable GHCR digest"
assert_failed_deployment_is_not_rolled_back \
  mutable-ghcr "${MUTABLE_GHCR_IMAGE}" true healthy \
  "previous image is not an immutable GHCR digest"
assert_failed_deployment_is_not_rolled_back \
  no-healthcheck "${PREVIOUS_IMAGE}" false healthy \
  "previous container has no healthcheck"
assert_failed_deployment_is_not_rolled_back \
  unhealthy "${PREVIOUS_IMAGE}" true unhealthy \
  "previous container was not healthy before deployment"
test_mutable_tag_is_rejected
test_stale_deployment_is_skipped
test_prod_uses_compose_override
test_profile_mismatch_is_rejected
test_unsupported_env_profile_is_rejected
test_missing_isolation_value_is_rejected
test_invalid_backend_port_is_rejected
test_backend_config_sha_mismatch_is_rejected
test_dirty_backend_config_is_rejected
test_missing_profile_config_is_rejected

echo "All deploy-backend tests passed."
