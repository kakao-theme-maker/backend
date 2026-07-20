#!/usr/bin/env bash

set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
readonly SCRIPT_DIR
readonly COMPOSE_FILE="${COMPOSE_FILE:-${SCRIPT_DIR}/docker-compose.yml}"
readonly HEALTH_TIMEOUT_SECONDS="${HEALTH_TIMEOUT_SECONDS:-120}"
readonly HEALTH_POLL_INTERVAL_SECONDS="${HEALTH_POLL_INTERVAL_SECONDS:-5}"
readonly DEPLOY_SEQUENCE="${DEPLOY_SEQUENCE:-}"
readonly DEPLOY_SEQUENCE_FILE="${DEPLOY_SEQUENCE_FILE:-${SCRIPT_DIR}/.backend-deploy-sequence}"

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 ghcr.io/kakao-theme-maker/backend@sha256:<digest>" >&2
  exit 2
fi

readonly TARGET_IMAGE="$1"
if [[ ! "${TARGET_IMAGE}" =~ ^ghcr\.io/kakao-theme-maker/backend@sha256:[0-9a-f]{64}$ ]]; then
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
  docker compose -f "${COMPOSE_FILE}" "$@"
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

export BACKEND_IMAGE="${TARGET_IMAGE}"
previous_container_id="$(compose ps --all -q backend)"
previous_image=""
if [[ -n "${previous_container_id}" ]]; then
  previous_image="$(docker inspect --format '{{.Config.Image}}' "${previous_container_id}")"
  echo "Recorded previous backend image: ${previous_image}"
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

echo "Backend deployment failed; starting rollback." >&2
if [[ -z "${previous_image}" ]]; then
  compose stop backend || true
  echo "No previous image exists. The failed first-deployment backend was stopped." >&2
  exit 1
fi

export BACKEND_IMAGE="${previous_image}"
if ! compose up -d --no-deps backend; then
  echo "Rollback failed while recreating the previous backend image: ${previous_image}" >&2
  exit 1
fi

if ! wait_for_backend_health; then
  echo "Rollback image did not become healthy: ${previous_image}" >&2
  exit 1
fi

echo "Rollback completed with previous backend image: ${previous_image}" >&2
exit 1
