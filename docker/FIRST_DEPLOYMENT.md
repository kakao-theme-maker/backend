# 개발 서버 최초 배포와 MySQL 이전

이 문서는 기존 개발 서버를 private GHCR의 ARM64 backend와 `mysql_data` named volume으로 처음 한 번 전환할 때만 사용한다. 이후 배포는 `deploy-backend.sh`가 backend만 교체한다.

기존 backend는 `ddl-auto=create-drop`일 수 있다. 정상 종료와 재기동 모두 schema를 삭제할 수 있으므로 legacy backend를 복원된 DB 또는 원본 DB에 다시 연결하지 않는다.

배포 스크립트의 자동 rollback은 이전 immutable image를 현재 Compose로 다시 만드는 image rollback이다. Compose 자체의 비호환 변경이 원인이면 자동 복구를 보장하지 않으므로, Compose 변경은 이전 image와의 호환성을 유지하거나 별도 수동 복구 절차를 준비한다.

## 금지 사항

- DB 복원과 승인 전 검증이 끝나기 전에 GitHub `development` 배포를 승인하지 않는다.
- `set -x`, `env`, `printenv`, 일반 `docker compose config`처럼 비밀값 전체가 출력되는 명령을 사용하지 않는다.
- `backend.env`, legacy env, SQL dump 내용을 터미널이나 Actions log에 출력하지 않는다.
- `docker compose down -v`, `docker rm -v`, `docker volume rm/prune`, 기존 DB bind 경로 삭제를 실행하지 않는다.
- migrated DB나 원본 DB에 legacy backend를 다시 연결하지 않는다.
- 성공 검증과 별도 폐기 승인 전에는 legacy Compose/env, 원본 DB storage, dump, Redis data를 삭제하지 않는다.

서버 작업은 비밀값 출력과 느슨한 파일 권한을 막은 상태로 시작한다.

```bash
set -Eeuo pipefail
set +x
umask 077
```

## 1. GitHub 승인 gate 준비

`dev -> master` 병합 전에 저장소 `Settings -> Environments -> development`에서 다음을 설정한다.

1. Required reviewers를 지정하고 deployment branch를 `master`로 제한한다.
2. `DEV_DEPLOY_PATH`를 현재 Compose가 실행되는 절대 경로와 같게 설정한다.
3. `DEV_HOST`, `DEV_SSH_PORT`, `DEV_SSH_USER`, `DEV_DEPLOY_PATH` variables와 SSH secrets를 확인한다.
4. 서버 배포 계정의 GHCR `read:packages` 로그인을 확인한다.

GitHub concurrency는 Actions job끼리만 직렬화한다. Actions 배포가 실행 중일 때 서버에서 `deploy-backend.sh`를 수동 실행하지 않는다.

`master` image가 게시된 뒤 `Deploy development` job은 승인 대기 상태로 둔다. 아래 2~8단계를 끝낸 뒤에만 승인한다. workflow의 파일 전송도 승인 뒤 실행되므로, DB 이전에는 같은 commit의 Compose와 script를 별도로 stage해야 한다.

## 2. Release와 작업 경로 고정

placeholder를 승인 대기 중인 동일 Actions run의 값으로 바꾼다.

```bash
export DEV_DEPLOY_PATH='/absolute/existing/deploy/path'
export RELEASE_SHA='<full-master-commit-sha>'
export RELEASE_DIGEST='sha256:<64-lowercase-hex-digest>'
export IMAGE_REF="ghcr.io/kakao-theme-maker/backend@${RELEASE_DIGEST}"

[[ "$RELEASE_SHA" =~ ^[0-9a-f]{40}$ ]]
[[ "$IMAGE_REF" =~ ^ghcr\.io/kakao-theme-maker/backend@sha256:[0-9a-f]{64}$ ]]

cd "$DEV_DEPLOY_PATH"
test "$(pwd -P)" = "$DEV_DEPLOY_PATH"

MIGRATION_ID="$(date -u +%Y%m%dT%H%M%SZ)"
MIGRATION_DIR="$DEV_DEPLOY_PATH/first-deployment-$MIGRATION_ID"
LEGACY_DIR="$MIGRATION_DIR/legacy"
install -d -m 700 "$LEGACY_DIR"
```

배포 image에는 `:dev` 또는 `:sha-...` tag가 아니라 exact `@sha256:...` digest만 사용한다.

## 3. 기존 배포 inventory와 백업

기존 container와 Compose metadata를 기록한다. 출력 항목에는 environment 값 전체를 포함하지 않는다.

```bash
OLD_BACKEND_ID="$(docker ps -aq --filter 'name=^/backend$')"
OLD_DATABASE_ID="$(docker ps -aq --filter 'name=^/database$')"
test -n "$OLD_BACKEND_ID"
test -n "$OLD_DATABASE_ID"

OLD_PROJECT="$(docker inspect "$OLD_BACKEND_ID" \
  --format '{{index .Config.Labels "com.docker.compose.project"}}')"
OLD_WORKING_DIR="$(docker inspect "$OLD_BACKEND_ID" \
  --format '{{index .Config.Labels "com.docker.compose.project.working_dir"}}')"
OLD_CONFIG_FILES="$(docker inspect "$OLD_BACKEND_ID" \
  --format '{{index .Config.Labels "com.docker.compose.project.config_files"}}')"
OLD_BACKEND_IMAGE="$(docker inspect "$OLD_BACKEND_ID" --format '{{.Config.Image}}')"
OLD_DATABASE_IMAGE="$(docker inspect "$OLD_DATABASE_ID" --format '{{.Config.Image}}')"
OLD_NETWORK="$(docker inspect "$OLD_BACKEND_ID" \
  --format '{{range $name, $_ := .NetworkSettings.Networks}}{{$name}}{{println}}{{end}}' \
  | sed -n '1p')"

mapfile -t OLD_REDIS_IDS < <(docker ps -aq \
  --filter "label=com.docker.compose.project=$OLD_PROJECT" \
  --filter 'label=com.docker.compose.service=redis')
test "${#OLD_REDIS_IDS[@]}" -eq 1
OLD_REDIS_ID="${OLD_REDIS_IDS[0]}"

test -n "$OLD_PROJECT"
test -n "$OLD_NETWORK"
test "$OLD_WORKING_DIR" = "$DEV_DEPLOY_PATH"
test "$(docker inspect "$OLD_REDIS_ID" --format '{{.State.Running}}')" = true
```

backend의 첫 network가 DB와 Redis의 공용 network인지 확인한다. 아니면 세 container가 공유하는 network를 찾아 `OLD_NETWORK`를 고친다.

```bash
docker network inspect "$OLD_NETWORK" \
  --format '{{range $id, $_ := .Containers}}{{$id}}{{println}}{{end}}' \
  | grep -Fq "$(docker inspect "$OLD_DATABASE_ID" --format '{{.Id}}')"
docker network inspect "$OLD_NETWORK" \
  --format '{{range $id, $_ := .Containers}}{{$id}}{{println}}{{end}}' \
  | grep -Fq "$(docker inspect "$OLD_REDIS_ID" --format '{{.Id}}')"
```

실제 `OLD_CONFIG_FILES`를 확인해 사용 중인 모든 Compose/override 파일을 백업한다. 아래 기본 경로는 서버에 맞게 수정한다.

```bash
export OLD_COMPOSE_FILE="$DEV_DEPLOY_PATH/docker-compose.yml"
export OLD_ENV_FILE="$DEV_DEPLOY_PATH/.env"
test -f "$OLD_COMPOSE_FILE"
test -f "$OLD_ENV_FILE"
test "$OLD_ENV_FILE" != "$DEV_DEPLOY_PATH/backend.env"

cp -p "$OLD_COMPOSE_FILE" "$LEGACY_DIR/docker-compose.yml"
cp -p "$OLD_ENV_FILE" "$LEGACY_DIR/.env"
chmod 600 "$LEGACY_DIR/docker-compose.yml" "$LEGACY_DIR/.env"

OLD_DB_MOUNT_TYPE="$(docker inspect "$OLD_DATABASE_ID" \
  --format '{{range .Mounts}}{{if eq .Destination "/var/lib/mysql"}}{{.Type}}{{end}}{{end}}')"
OLD_DB_MOUNT_NAME="$(docker inspect "$OLD_DATABASE_ID" \
  --format '{{range .Mounts}}{{if eq .Destination "/var/lib/mysql"}}{{.Name}}{{end}}{{end}}')"
OLD_DB_MOUNT_SOURCE="$(docker inspect "$OLD_DATABASE_ID" \
  --format '{{range .Mounts}}{{if eq .Destination "/var/lib/mysql"}}{{.Source}}{{end}}{{end}}')"
test -n "$OLD_DB_MOUNT_TYPE"
test -n "$OLD_DB_MOUNT_SOURCE"

{
  printf 'migration_id=%s\n' "$MIGRATION_ID"
  printf 'release_sha=%s\n' "$RELEASE_SHA"
  printf 'image_ref=%s\n' "$IMAGE_REF"
  printf 'old_project=%s\n' "$OLD_PROJECT"
  printf 'old_working_dir=%s\n' "$OLD_WORKING_DIR"
  printf 'old_config_files=%s\n' "$OLD_CONFIG_FILES"
  printf 'old_backend_id=%s\n' "$OLD_BACKEND_ID"
  printf 'old_backend_image=%s\n' "$OLD_BACKEND_IMAGE"
  printf 'old_database_id=%s\n' "$OLD_DATABASE_ID"
  printf 'old_database_image=%s\n' "$OLD_DATABASE_IMAGE"
  printf 'old_redis_id=%s\n' "$OLD_REDIS_ID"
  printf 'old_network=%s\n' "$OLD_NETWORK"
  printf 'old_db_mount_type=%s\n' "$OLD_DB_MOUNT_TYPE"
  printf 'old_db_mount_name=%s\n' "$OLD_DB_MOUNT_NAME"
  printf 'old_db_mount_source=%s\n' "$OLD_DB_MOUNT_SOURCE"
} > "$MIGRATION_DIR/inventory.txt"

sha256sum "$OLD_COMPOSE_FILE" "$OLD_ENV_FILE" \
  > "$MIGRATION_DIR/legacy-files.sha256"
chmod 600 "$MIGRATION_DIR/inventory.txt" \
  "$MIGRATION_DIR/legacy-files.sha256"
```

MySQL 공식 image의 anonymous volume도 위 mount 조회에 잡힌다. `/var/lib/mysql` mount가 없고 데이터가 container writable layer에만 있으면 여기서 중단하고 별도 복구 계획을 세운다.

## 4. Exact ARM64 image preflight

서버 배포 계정에 private GHCR `read:packages` token으로 한 번 로그인한다. token은 명령 인자나 파일에 직접 쓰지 않는다.

```bash
read -r -p 'GHCR username: ' GHCR_USER
read -r -s -p 'GHCR read:packages token: ' GHCR_TOKEN
printf '\n'
printf '%s' "$GHCR_TOKEN" \
  | docker login ghcr.io -u "$GHCR_USER" --password-stdin
unset GHCR_TOKEN
chmod 600 "$HOME/.docker/config.json"
```

기존 image/volume을 prune하지 말고 디스크 여유와 exact digest architecture를 확인한다.

```bash
DOCKER_ROOT="$(docker info --format '{{.DockerRootDir}}')"
df -h "$DEV_DEPLOY_PATH" "$DOCKER_ROOT"
docker system df

docker pull --platform linux/arm64 "$IMAGE_REF"
test "$(docker image inspect "$IMAGE_REF" --format '{{.Os}}')" = linux
test "$(docker image inspect "$IMAGE_REF" --format '{{.Architecture}}')" = arm64
df -h "$DOCKER_ROOT"
```

## 5. 새 배포 파일과 `backend.env` 준비

승인 대기 중인 exact commit의 checkout에서 새 Compose와 script를 stage한다.

```bash
export RELEASE_CHECKOUT='/path/to/exact/release/checkout'
test "$(git -C "$RELEASE_CHECKOUT" rev-parse HEAD)" = "$RELEASE_SHA"

install -m 600 "$RELEASE_CHECKOUT/docker/docker-compose.yml" \
  "$MIGRATION_DIR/docker-compose.next.yml"
install -m 700 "$RELEASE_CHECKOUT/docker/deploy-backend.sh" \
  "$MIGRATION_DIR/deploy-backend.next.sh"
```

`docker/backend.env.example`을 기준으로 서버에서만 `backend.env`를 작성한다. 기존 Compose project를 유지해야 Redis network가 바뀌지 않는다.

```bash
test -f "$DEV_DEPLOY_PATH/backend.env"
chmod 600 "$DEV_DEPLOY_PATH/backend.env"
test "$(stat -c '%a' "$DEV_DEPLOY_PATH/backend.env")" = 600

if grep -qi 'change_me' "$DEV_DEPLOY_PATH/backend.env"; then
  echo 'backend.env still contains placeholders.' >&2
  exit 1
fi
test "$(grep -c '^COMPOSE_PROJECT_NAME=' \
  "$DEV_DEPLOY_PATH/backend.env")" -eq 1
test "$(sed -n 's/^COMPOSE_PROJECT_NAME=//p' \
  "$DEV_DEPLOY_PATH/backend.env")" = "$OLD_PROJECT"
grep -q '^SPRING_JPA_HIBERNATE_DDL_AUTO=update$' \
  "$DEV_DEPLOY_PATH/backend.env"
if grep -Eq '^SPRING_PROFILES_ACTIVE=([^,]+,)*dev(,[^,]+)*$' \
    "$DEV_DEPLOY_PATH/backend.env"; then
  echo 'The dev profile must not be enabled on the persistent DB.' >&2
  exit 1
fi
```

Swagger는 서버에서 선택한 기존 설정을 유지한다. 활성화했다면 Springdoc과 whitelist 값만 확인하고, 이 PR의 파일로 덮어쓰지 않는다.

## 6. Maintenance와 final dump

reverse proxy에서 maintenance를 켜고 진행 중 쓰기가 끝난 뒤, legacy `create-drop` shutdown hook을 피하도록 backend restart를 끄고 SIGKILL한다.

```bash
docker update --restart=no "$OLD_BACKEND_ID"
docker kill --signal KILL "$OLD_BACKEND_ID"
test "$(docker inspect "$OLD_BACKEND_ID" --format '{{.State.Running}}')" = false
```

중요 테이블 row count를 기록하고 실행 중인 기존 MySQL에서 final dump를 만든다. 서버 schema에 맞춰 테이블 목록을 추가할 수 있다.

```bash
COUNT_SQL="SELECT 'post', COUNT(*) FROM post UNION ALL SELECT 'theme_component', COUNT(*) FROM theme_component UNION ALL SELECT 'user', COUNT(*) FROM \`user\` ORDER BY 1"

docker exec "$OLD_DATABASE_ID" sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N "$MYSQL_DATABASE" -e "$1"' \
  sh "$COUNT_SQL" > "$MIGRATION_DIR/row-counts.before.tsv"

FINAL_DUMP="$MIGRATION_DIR/mysql-final.sql"
docker exec "$OLD_DATABASE_ID" sh -c \
  'exec mysqldump -uroot -p"$MYSQL_ROOT_PASSWORD" --single-transaction --quick --routines --triggers --events --hex-blob --default-character-set=utf8mb4 "$MYSQL_DATABASE"' \
  > "$FINAL_DUMP"

test -s "$FINAL_DUMP"
grep -q -- '-- MySQL dump' "$FINAL_DUMP"
wc -c "$FINAL_DUMP"
sha256sum "$FINAL_DUMP" > "$FINAL_DUMP.sha256"
chmod 600 "$FINAL_DUMP" "$FINAL_DUMP.sha256" \
  "$MIGRATION_DIR/row-counts.before.tsv"
```

single quote를 double quote로 바꾸지 않는다. DB 비밀번호는 container 내부에서만 확장된다.

dump는 서버 외부의 암호화된 위치에도 백업하고, checksum과 복호화 가능성을 확인한다. 다음은 공개 age recipient를 사용하는 예시다.

```bash
export BACKUP_AGE_RECIPIENT='age1...'
export BACKUP_TARGET='backup-user@backup-host:/encrypted-backups/komentum/'
age -r "$BACKUP_AGE_RECIPIENT" -o "$FINAL_DUMP.age" "$FINAL_DUMP"
sha256sum "$FINAL_DUMP.age" > "$FINAL_DUMP.age.sha256"
scp "$FINAL_DUMP.age" "$FINAL_DUMP.age.sha256" "$BACKUP_TARGET"
```

## 7. `mysql_data` 생성과 restore

backend가 죽어 있는 상태에서 기존 DB를 정상 종료한다. 원본 volume/bind는 보존한다.

```bash
docker update --restart=no "$OLD_DATABASE_ID"
docker stop "$OLD_DATABASE_ID"
test "$(docker inspect "$OLD_DATABASE_ID" --format '{{.State.Running}}')" = false

install -m 600 "$MIGRATION_DIR/docker-compose.next.yml" \
  "$DEV_DEPLOY_PATH/docker-compose.yml"
install -m 700 "$MIGRATION_DIR/deploy-backend.next.sh" \
  "$DEV_DEPLOY_PATH/deploy-backend.sh"

cmp "$OLD_ENV_FILE" "$LEGACY_DIR/.env"
rm "$OLD_ENV_FILE"

docker rm "$OLD_BACKEND_ID" "$OLD_DATABASE_ID"
case "$OLD_DB_MOUNT_TYPE" in
  volume) docker volume inspect "$OLD_DB_MOUNT_NAME" > /dev/null ;;
  bind) test -d "$OLD_DB_MOUNT_SOURCE" ;;
  *) echo 'Unsupported legacy DB mount type.' >&2; exit 1 ;;
esac
```

비밀값을 출력하지 않는 `config --quiet`로 검증하고 database만 시작한다. Redis와 backend를 이 명령으로 재생성하지 않는다.

```bash
BACKEND_IMAGE="$IMAGE_REF" docker compose \
  --env-file "$DEV_DEPLOY_PATH/backend.env" \
  -f "$DEV_DEPLOY_PATH/docker-compose.yml" \
  config --quiet

BACKEND_IMAGE="$IMAGE_REF" docker compose \
  --env-file "$DEV_DEPLOY_PATH/backend.env" \
  -f "$DEV_DEPLOY_PATH/docker-compose.yml" \
  config --format json \
  | jq --exit-status --arg project "$OLD_PROJECT" \
      '.name == $project' > /dev/null

NEW_DB_VOLUME_EXPECTED="$(BACKEND_IMAGE="$IMAGE_REF" docker compose \
  --env-file "$DEV_DEPLOY_PATH/backend.env" \
  -f "$DEV_DEPLOY_PATH/docker-compose.yml" \
  config --format json \
  | jq --exit-status --raw-output '.volumes.mysql_data.name')"
test -n "$NEW_DB_VOLUME_EXPECTED"
if docker volume inspect "$NEW_DB_VOLUME_EXPECTED" > /dev/null 2>&1; then
  echo 'Target mysql_data volume already exists; do not reuse or delete it.' >&2
  exit 1
fi

BACKEND_IMAGE="$IMAGE_REF" docker compose \
  --env-file "$DEV_DEPLOY_PATH/backend.env" \
  -f "$DEV_DEPLOY_PATH/docker-compose.yml" \
  up -d --no-deps database
```

새 MySQL 준비와 새 named volume을 확인한 뒤 dump를 복원한다.

```bash
NEW_DATABASE_ID="$(docker ps -q --filter 'name=^/database$')"
test -n "$NEW_DATABASE_ID"

for attempt in $(seq 1 60); do
  if docker exec "$NEW_DATABASE_ID" sh -c \
      'exec mysqladmin ping -uroot -p"$MYSQL_ROOT_PASSWORD" --silent'; then
    break
  fi
  if [ "$attempt" -eq 60 ]; then
    echo 'New MySQL did not become ready.' >&2
    exit 1
  fi
  sleep 2
done

NEW_DB_MOUNT_NAME="$(docker inspect "$NEW_DATABASE_ID" \
  --format '{{range .Mounts}}{{if eq .Destination "/var/lib/mysql"}}{{.Name}}{{end}}{{end}}')"
test "$NEW_DB_MOUNT_NAME" = "$NEW_DB_VOLUME_EXPECTED"
test "$NEW_DB_MOUNT_NAME" != "$OLD_DB_MOUNT_NAME"
docker volume inspect "$NEW_DB_MOUNT_NAME" > /dev/null

docker exec -i "$NEW_DATABASE_ID" sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" "$MYSQL_DATABASE"' \
  < "$FINAL_DUMP"

docker exec "$NEW_DATABASE_ID" sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N "$MYSQL_DATABASE" -e "$1"' \
  sh "$COUNT_SQL" > "$MIGRATION_DIR/row-counts.after.tsv"
chmod 600 "$MIGRATION_DIR/row-counts.after.tsv"
diff -u "$MIGRATION_DIR/row-counts.before.tsv" \
  "$MIGRATION_DIR/row-counts.after.tsv"

test "$(docker inspect "$OLD_REDIS_ID" --format '{{.State.Running}}')" = true
test -z "$(docker ps -aq --filter 'name=^/backend$')"
```

row count가 다르거나 Redis가 교체됐으면 배포를 승인하지 않는다. DB 관리 작업은 host port가 아니라 `docker compose exec database` 또는 `docker exec`로 수행한다.

## 8. 승인 전 확인과 배포 승인

다음 조건을 모두 확인한다.

- off-server encrypted backup의 checksum과 복호화 시험 성공
- 원본 DB volume/bind와 final dump가 모두 보존됨
- Actions run의 commit SHA, publish digest, `IMAGE_REF`가 일치함
- image architecture가 `arm64`이고 디스크 여유가 충분함
- `backend.env` mode가 `600`, `deploy-backend.sh` mode가 `700`
- Compose project와 `DEV_DEPLOY_PATH`가 기존 배포와 같음
- 새 DB row count가 이전 값과 같고 기존 Redis container가 실행 중임
- 승인 대상 job이 여전히 같은 master run이며 더 최신 배포가 없음

모두 통과하면 GitHub `development` Environment의 `Deploy development` job을 승인하고 Actions log를 감시한다. 첫 배포에는 자동 rollback 가능한 이전 GHCR digest가 없으므로, 실패한 target은 중지되고 수동 복구 안내와 함께 job이 실패해야 한다.

## 9. 배포 후 검증

health는 status-only인지 확인하고 실행 image digest를 대조한다.

```bash
HEALTH_JSON="$(curl -fsS 'http://127.0.0.1:28080/actuator/health')"
test "$(printf '%s' "$HEALTH_JSON" | jq -r 'keys | sort | join(",")')" = status
test "$(printf '%s' "$HEALTH_JSON" | jq -r '.status')" = UP

BACKEND_ID="$(docker ps -q --filter 'name=^/backend$')"
test -n "$BACKEND_ID"
test "$(docker inspect "$BACKEND_ID" --format '{{.State.Health.Status}}')" = healthy
test "$(docker inspect "$BACKEND_ID" --format '{{.Config.Image}}')" = "$IMAGE_REF"
test "$(docker image inspect "$IMAGE_REF" --format '{{.Architecture}}')" = arm64
```

DB와 Redis가 재생성되지 않았는지 확인하고 row count를 다시 비교한다.

```bash
test "$(docker ps -q --filter 'name=^/database$')" = "$NEW_DATABASE_ID"
test "$(docker inspect "$OLD_REDIS_ID" --format '{{.State.Running}}')" = true

docker exec "$NEW_DATABASE_ID" sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -N "$MYSQL_DATABASE" -e "$1"' \
  sh "$COUNT_SQL" > "$MIGRATION_DIR/row-counts.deployed.tsv"
chmod 600 "$MIGRATION_DIR/row-counts.deployed.tsv"
diff -u "$MIGRATION_DIR/row-counts.after.tsv" \
  "$MIGRATION_DIR/row-counts.deployed.tsv"
```

가능하면 maintenance 상태에서 비교한다. 정상 쓰기로 row가 늘 수는 있지만 감소는 허용하지 않는다.

개발 서버에서 Swagger를 활성화한 기존 운영 설정이면 reverse proxy 경로도 확인한다.

```bash
curl -fsS 'https://<DEV_API_DOMAIN>/v3/api-docs' > /dev/null
curl -fsS 'https://<DEV_API_DOMAIN>/swagger-ui/index.html' > /dev/null
```

로그인, DB 조회, Redis 사용 기능, Kakao redirect, 임시 upload/download까지 성공한 뒤 maintenance를 해제한다. legacy 배포 자산은 별도 보존 기간과 폐기 승인이 끝날 때까지 유지한다.

## 10. 실패 시 수동 복구

새 backend만 실패하고 migrated DB가 정상이라면 maintenance를 유지하고 실패 target을 중지한다. 같은 schema와 호환되며 `ddl-auto=update`, `dev` profile 비활성이 검증된 수정 GHCR digest를 우선 배포한다.

```bash
docker stop backend 2>/dev/null || true
```

DB까지 원복해야 하면 다음 순서를 따른다.

1. 새 backend와 database container만 중지·제거한다. 새 `mysql_data` volume은 제거하지 않는다.
2. `inventory.txt`와 legacy Compose를 참고해 원본 DB volume/bind 또는 검증된 dump로 database만 복구한다.
3. DB env는 legacy env에서 MySQL 3개만 별도 mode-600 파일로 분리해 전달한다.
4. migration 전 row count와 DB 무결성을 확인한다.
5. schema 호환성이 검증된 immutable GHCR backend만 연결한다.

legacy backend는 원본 DB에도 다시 시작하지 않는다. `create-drop`의 startup 동작으로 보존 데이터를 삭제할 수 있다. legacy binary 실행이 불가피하면 원본이 아닌 별도 DB clone과 별도 승인 절차를 먼저 마련한다.
