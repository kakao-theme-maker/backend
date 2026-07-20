#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly SCRIPT_DIR
readonly COMPOSE_FILE="${COMPOSE_FILE:-${SCRIPT_DIR}/docker-compose.yml}"
readonly COMPOSE_ENV_FILE="${COMPOSE_ENV_FILE:-${SCRIPT_DIR}/backend.env}"
readonly HEALTH_TIMEOUT_SECONDS="${HEALTH_TIMEOUT_SECONDS:-120}"
readonly HEALTH_POLL_INTERVAL_SECONDS="${HEALTH_POLL_INTERVAL_SECONDS:-5}"
readonly DEPLOY_SEQUENCE="${DEPLOY_SEQUENCE:-}"
readonly DEPLOY_SEQUENCE_FILE="${DEPLOY_SEQUENCE_FILE:-${SCRIPT_DIR}/.backend-deploy-sequence}"
readonly IMMUTABLE_IMAGE_PATTERN='^ghcr\.io/kakao-theme-maker/backend@sha256:[0-9a-f]{64}$'
export COMPOSE_ENV_FILE

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 ghcr.io/kakao-theme-maker/backend@sha256:<digest>" >&2
  exit 2
fi

readonly TARGET_IMAGE="$1"
if [[ ! "${TARGET_IMAGE}" =~ ${IMMUTABLE_IMAGE_PATTERN} ]]; then
  echo "The backend image must be an immutable ghcr.io/kakao-theme-maker/backend digest." >&2
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

compose() {
  docker compose --env-file "${COMPOSE_ENV_FILE}" -f "${COMPOSE_FILE}" "$@"
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

  container_id="$(docker ps --all --quiet --filter 'name=^/backend$')"
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
