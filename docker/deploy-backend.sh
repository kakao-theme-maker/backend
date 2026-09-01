#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly SCRIPT_DIR
readonly COMPOSE_FILE="${COMPOSE_FILE:-${SCRIPT_DIR}/docker-compose.deploy.yml}"
readonly COMPOSE_PROD_FILE="${COMPOSE_PROD_FILE:-${SCRIPT_DIR}/docker-compose.deploy.prod.yml}"
readonly COMPOSE_ENV_FILE="${COMPOSE_ENV_FILE:-${SCRIPT_DIR}/backend.env}"
readonly BACKEND_CONFIG_DIR="${BACKEND_CONFIG_DIR_OVERRIDE:-${SCRIPT_DIR}/backend_config}"
readonly HEALTH_TIMEOUT_SECONDS="${HEALTH_TIMEOUT_SECONDS:-120}"
readonly HEALTH_POLL_INTERVAL_SECONDS="${HEALTH_POLL_INTERVAL_SECONDS:-5}"
readonly DEPLOY_SEQUENCE="${DEPLOY_SEQUENCE:-}"
readonly DEPLOY_SEQUENCE_FILE="${DEPLOY_SEQUENCE_FILE:-${SCRIPT_DIR}/.backend-deploy-sequence}"
readonly IMMUTABLE_IMAGE_PATTERN='^ghcr\.io/kakao-theme-maker/backend@sha256:[0-9a-f]{64}$'
readonly BACKEND_CONFIG_SHA_PATTERN='^[0-9a-f]{40}$'

if [[ $# -ne 3 ]]; then
  echo "Usage: $0 ghcr.io/kakao-theme-maker/backend@sha256:<digest> <dev|prod> <backend-config-sha>" >&2
  exit 2
fi

readonly TARGET_IMAGE="$1"
readonly EXPECTED_PROFILE="$2"
readonly EXPECTED_BACKEND_CONFIG_SHA="$3"
if [[ ! "${TARGET_IMAGE}" =~ ${IMMUTABLE_IMAGE_PATTERN} ]]; then
  echo "The backend image must be an immutable ghcr.io/kakao-theme-maker/backend digest." >&2
  exit 2
fi
if [[ "${EXPECTED_PROFILE}" != "dev" && "${EXPECTED_PROFILE}" != "prod" ]]; then
  echo "The expected Spring profile must be dev or prod." >&2
  exit 2
fi
if [[ ! "${EXPECTED_BACKEND_CONFIG_SHA}" =~ ${BACKEND_CONFIG_SHA_PATTERN} ]]; then
  echo "The backend_config SHA must be a 40-character lowercase Git SHA." >&2
  exit 2
fi

if [[ ! "${HEALTH_TIMEOUT_SECONDS}" =~ ^[1-9][0-9]*$ ]] \
    || [[ ! "${HEALTH_POLL_INTERVAL_SECONDS}" =~ ^[1-9][0-9]*$ ]]; then
  echo "Health timeout and polling interval must be positive integers." >&2
  exit 2
fi

if [[ -n "${DEPLOY_SEQUENCE}" && ! "${DEPLOY_SEQUENCE}" =~ ^[1-9][0-9]*$ ]]; then
  echo "DEPLOY_SEQUENCE must be a positive integer when it is set." >&2
  exit 2
fi

if [[ -n "${DEPLOY_SEQUENCE}" && -f "${DEPLOY_SEQUENCE_FILE}" ]]; then
  last_deploy_sequence="$(<"${DEPLOY_SEQUENCE_FILE}")"
  if [[ ! "${last_deploy_sequence}" =~ ^[1-9][0-9]*$ ]]; then
    echo "Invalid deployment sequence file: ${DEPLOY_SEQUENCE_FILE}" >&2
    exit 1
  fi
  if ((DEPLOY_SEQUENCE <= last_deploy_sequence)); then
    echo "Skipping stale deployment sequence ${DEPLOY_SEQUENCE}; last successful sequence is ${last_deploy_sequence}."
    exit 0
  fi
fi

read_env_value() {
  local key="$1"
  local count
  local value

  count="$(grep -Ec "^${key}=" "${COMPOSE_ENV_FILE}" || true)"
  if [[ "${count}" != "1" ]]; then
    echo "${COMPOSE_ENV_FILE} must contain exactly one ${key} entry." >&2
    return 1
  fi
  value="$(sed -n "s/^${key}=//p" "${COMPOSE_ENV_FILE}")"
  if [[ -z "${value}" ]]; then
    echo "${key} must not be empty in ${COMPOSE_ENV_FILE}." >&2
    return 1
  fi
  printf '%s' "${value}"
}

if [[ ! -r "${COMPOSE_FILE}" || ! -r "${COMPOSE_ENV_FILE}" ]]; then
  echo "Deployment Compose and environment files must be readable." >&2
  exit 1
fi

ACTUAL_PROFILE="$(read_env_value SPRING_PROFILES_ACTIVE)"
readonly ACTUAL_PROFILE
if [[ "${ACTUAL_PROFILE}" != "dev" && "${ACTUAL_PROFILE}" != "prod" ]]; then
  echo "SPRING_PROFILES_ACTIVE must contain exactly dev or prod." >&2
  exit 1
fi
if [[ "${ACTUAL_PROFILE}" != "${EXPECTED_PROFILE}" ]]; then
  echo "Expected Spring profile ${EXPECTED_PROFILE}, but backend.env selects ${ACTUAL_PROFILE}." >&2
  exit 1
fi

ACTUAL_COMPOSE_PROJECT_NAME="$(read_env_value COMPOSE_PROJECT_NAME)"
ACTUAL_BACKEND_BIND_ADDRESS="$(read_env_value BACKEND_BIND_ADDRESS)"
ACTUAL_BACKEND_PORT="$(read_env_value BACKEND_PORT)"
readonly ACTUAL_COMPOSE_PROJECT_NAME ACTUAL_BACKEND_BIND_ADDRESS ACTUAL_BACKEND_PORT
if [[ ! "${ACTUAL_COMPOSE_PROJECT_NAME}" =~ ^[a-z0-9][a-z0-9_-]*$ ]]; then
  echo "COMPOSE_PROJECT_NAME must use lowercase letters, digits, hyphens, or underscores." >&2
  exit 1
fi
if [[ ! "${ACTUAL_BACKEND_PORT}" =~ ^[1-9][0-9]{0,4}$ ]] \
    || ((10#${ACTUAL_BACKEND_PORT} > 65535)); then
  echo "BACKEND_PORT must be an integer from 1 to 65535." >&2
  exit 1
fi

if [[ "${BACKEND_CONFIG_DIR}" != /* || ! -d "${BACKEND_CONFIG_DIR}" ]]; then
  echo "${SCRIPT_DIR}/backend_config must be an existing directory." >&2
  exit 1
fi

if ! backend_config_root="$(git -C "${BACKEND_CONFIG_DIR}" rev-parse --show-toplevel 2>/dev/null)"; then
  echo "${SCRIPT_DIR}/backend_config must be a Git checkout." >&2
  exit 1
fi
backend_config_root="$(cd "${backend_config_root}" && pwd -P)"
backend_config_dir_canonical="$(cd "${BACKEND_CONFIG_DIR}" && pwd -P)"
if [[ "${backend_config_root}" != "${backend_config_dir_canonical}" ]]; then
  echo "${SCRIPT_DIR}/backend_config must be the backend_config repository root." >&2
  exit 1
fi

BACKEND_CONFIG_HEAD="$(git -C "${BACKEND_CONFIG_DIR}" rev-parse HEAD)"
readonly BACKEND_CONFIG_HEAD
if [[ "${BACKEND_CONFIG_HEAD}" != "${EXPECTED_BACKEND_CONFIG_SHA}" ]]; then
  echo "backend_config HEAD does not match the expected SHA." >&2
  exit 1
fi
if [[ -n "$(git -C "${BACKEND_CONFIG_DIR}" status --porcelain --untracked-files=all)" ]]; then
  echo "backend_config checkout must be clean before deployment." >&2
  exit 1
fi

readonly COMMON_CONFIG_FILE="${BACKEND_CONFIG_DIR}/dev/monolithic/application.yml"
readonly PROFILE_CONFIG_FILE="${BACKEND_CONFIG_DIR}/dev/monolithic/application-${ACTUAL_PROFILE}.yml"
if [[ ! -f "${COMMON_CONFIG_FILE}" || ! -r "${COMMON_CONFIG_FILE}" \
    || ! -f "${PROFILE_CONFIG_FILE}" || ! -r "${PROFILE_CONFIG_FILE}" ]]; then
  echo "backend_config common and ${ACTUAL_PROFILE} application files must be readable." >&2
  exit 1
fi

COMPOSE_FILES=(-f "${COMPOSE_FILE}")
if [[ "${ACTUAL_PROFILE}" == "prod" ]]; then
  if [[ ! -r "${COMPOSE_PROD_FILE}" ]]; then
    echo "Production Compose override must be readable." >&2
    exit 1
  fi
  COMPOSE_FILES+=(-f "${COMPOSE_PROD_FILE}")
fi
readonly -a COMPOSE_FILES

export COMPOSE_ENV_FILE BACKEND_CONFIG_DIR
export COMPOSE_PROJECT_NAME="${ACTUAL_COMPOSE_PROJECT_NAME}"
export BACKEND_BIND_ADDRESS="${ACTUAL_BACKEND_BIND_ADDRESS}"
export BACKEND_PORT="${ACTUAL_BACKEND_PORT}"
export SPRING_PROFILES_ACTIVE="${ACTUAL_PROFILE}"

compose() {
  docker compose --env-file "${COMPOSE_ENV_FILE}" "${COMPOSE_FILES[@]}" "$@"
}

wait_for_backend_health() {
  local container_id
  local health_status="missing"
  local deadline=$((SECONDS + HEALTH_TIMEOUT_SECONDS))

  while ((SECONDS < deadline)); do
    container_id="$(compose ps --all -q backend)"
    if [[ -n "${container_id}" ]]; then
      health_status="$(docker inspect \
        --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' \
        "${container_id}" 2>/dev/null || true)"
      case "${health_status}" in
        healthy)
          return 0
          ;;
        unhealthy | exited | dead)
          echo "Backend health check failed with status: ${health_status}" >&2
          return 1
          ;;
      esac
    fi
    sleep "${HEALTH_POLL_INTERVAL_SECONDS}"
  done

  echo "Backend did not become healthy within ${HEALTH_TIMEOUT_SECONDS} seconds (last status: ${health_status})." >&2
  return 1
}

stop_backend_image_if_current() {
  local expected_image="$1"
  local container_id
  local current_image
  local running

  container_id="$(compose ps --all -q backend)"
  if [[ -z "${container_id}" ]]; then
    return 0
  fi

  current_image="$(docker inspect --format '{{.Config.Image}}' "${container_id}" 2>/dev/null || true)"
  if [[ "${current_image}" != "${expected_image}" ]]; then
    return 0
  fi

  if ! docker stop "${container_id}" >/dev/null; then
    echo "Failed to stop backend image: ${expected_image}" >&2
    return 1
  fi

  if ! running="$(docker inspect --format '{{.State.Running}}' "${container_id}" 2>/dev/null)"; then
    echo "Could not verify that the backend stopped: ${expected_image}" >&2
    return 1
  fi
  if [[ "${running}" == "true" ]]; then
    echo "Backend is still running after docker stop: ${expected_image}" >&2
    return 1
  fi

  echo "Stopped failed backend image: ${expected_image}" >&2
}

export BACKEND_IMAGE="${TARGET_IMAGE}"
previous_container_id="$(compose ps --all -q backend)"
previous_image=""
rollback_eligible=false
rollback_ineligible_reason="no previous backend container exists"
if [[ -n "${previous_container_id}" ]]; then
  previous_image="$(docker inspect --format '{{.Config.Image}}' "${previous_container_id}")"
  echo "Recorded previous backend image: ${previous_image}"
  previous_has_healthcheck="$(docker inspect \
    --format '{{if .Config.Healthcheck}}true{{else}}false{{end}}' \
    "${previous_container_id}" 2>/dev/null || true)"
  previous_health_status="$(docker inspect \
    --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}none{{end}}' \
    "${previous_container_id}" 2>/dev/null || true)"

  if [[ ! "${previous_image}" =~ ${IMMUTABLE_IMAGE_PATTERN} ]]; then
    rollback_ineligible_reason="the previous image is not an immutable GHCR digest"
  elif [[ "${previous_has_healthcheck}" != "true" ]]; then
    rollback_ineligible_reason="the previous container has no healthcheck"
  elif [[ "${previous_health_status}" != "healthy" ]]; then
    rollback_ineligible_reason="the previous container was not healthy before deployment"
  else
    rollback_eligible=true
    rollback_ineligible_reason=""
  fi
else
  echo "No previous backend container was found."
fi

compose pull backend

if compose up -d --no-deps backend && wait_for_backend_health; then
  if [[ -n "${DEPLOY_SEQUENCE}" ]]; then
    deploy_sequence_tmp="${DEPLOY_SEQUENCE_FILE}.tmp.$$"
    printf '%s\n' "${DEPLOY_SEQUENCE}" >"${deploy_sequence_tmp}"
    mv "${deploy_sequence_tmp}" "${DEPLOY_SEQUENCE_FILE}"
  fi
  echo "Backend deployment completed: ${TARGET_IMAGE}"
  exit 0
fi

echo "Backend deployment failed." >&2
if [[ "${rollback_eligible}" != "true" ]]; then
  if ! stop_backend_image_if_current "${TARGET_IMAGE}"; then
    echo "Manual intervention is required to stop the failed target." >&2
  fi
  echo "Automatic rollback was not attempted because ${rollback_ineligible_reason}." >&2
  echo "Recover manually with the preserved legacy Compose files and original DB storage or verified dump." >&2
  exit 1
fi

echo "Starting automatic rollback." >&2
export BACKEND_IMAGE="${previous_image}"
if ! compose up -d --no-deps backend; then
  echo "Rollback failed while recreating the previous backend image: ${previous_image}" >&2
  rollback_stop_failed=false
  if ! stop_backend_image_if_current "${TARGET_IMAGE}"; then
    rollback_stop_failed=true
  fi
  if ! stop_backend_image_if_current "${previous_image}"; then
    rollback_stop_failed=true
  fi
  if [[ "${rollback_stop_failed}" == "true" ]]; then
    echo "Manual intervention is required to stop the failed backend." >&2
  fi
  echo "Recover manually with the preserved Compose files and verified DB storage or dump." >&2
  exit 1
fi

if ! wait_for_backend_health; then
  echo "Rollback image did not become healthy: ${previous_image}" >&2
  if ! stop_backend_image_if_current "${previous_image}"; then
    echo "Manual intervention is required to stop the failed rollback image." >&2
  fi
  echo "Recover manually with the preserved Compose files and verified DB storage or dump." >&2
  exit 1
fi

echo "Rollback completed with previous backend image: ${previous_image}" >&2
exit 1
