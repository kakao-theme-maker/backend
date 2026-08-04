# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

카카오톡 테마를 제작·공유하는 서비스의 백엔드. Spring Boot 3.4.3 / Java 17 기반이며,
과거 MSA(gateway/discovery/theme/user-service) 구조에서 **단일 모놀리식 애플리케이션**으로 통합되었다
(`settings.gradle`의 서브모듈 include는 모두 주석 처리됨). 실행 진입점은 `com.komentum.KomentumApplication` 하나다.

- 배포 산출물: `build/libs/kakao-theme-maker.jar` (`bootJar.archiveFileName` 고정)
- 그룹: `com.komentum`
- 주요 스택: Spring MVC + WebFlux(WebClient) + Data JPA + QueryDSL + Spring Security(JWT + OAuth2) + Redis + MySQL/H2

## 설정(중요): backend_config 서브모듈

`application.yml` / `application-test.yml` / `common.yml`은 **저장소에 커밋되지 않는다** (`.gitignore`).
이 파일들은 별도 private 서브모듈 `backend_config`(`.gitmodules` 참고)에 있고, Gradle 빌드 시 복사된다:

- `copyConfig` → `backend_config/dev/monolithic/application.yml` → `src/main/resources/`
- `copyTestConfig` → `backend_config/dev/monolithic/application-test.yml` → `src/test/resources/`
- 각각 `processResources` / `processTestResources`에 의존하므로 빌드·테스트가 자동으로 복사한다.

따라서 클론 직후에는 반드시 서브모듈을 먼저 받아야 빌드/테스트가 성공한다:

```bash
git submodule update --init --recursive
```

`backend_config/dev/` 아래에는 monolithic 외에 common/gateway-service/discovery-service/theme-service/user-service
프로필이 남아 있으나 현재 실행 대상은 **monolithic** 뿐이다.

## 빌드 · 실행

```bash
./gradlew build                 # 전체 빌드(테스트 포함), 산출물 kakao-theme-maker.jar
./gradlew bootRun               # 로컬 실행 (기본 profile: dev, auth / port 8080)
./gradlew clean                 # 빌드 산출물 + QueryDSL 생성물(src/main/generated) 삭제

# Docker (image_builder.sh)
docker build -t louie8821/kakao-theme-maker:v1-dev -f ./Dockerfile .
# 전체 스택(backend + mysql + redis) 기동
docker compose -f docker/docker-compose.yml up
```

로컬 실행에는 MySQL(3306, `theme_database`)과 Redis(6379)가 필요하다. JPA `ddl-auto=create-drop`이므로
개발 DB 스키마는 기동마다 재생성된다.

## 테스트

```bash
./gradlew test                                              # 전체 테스트
./gradlew test --tests "com.komentum.post.controller.ThemeBoardControllerTest"   # 단일 클래스
./gradlew test --tests "*.ThemeBoardControllerTest.단일_테스트_메서드명"          # 단일 메서드
```

- 프레임워크: JUnit 5 + Mockito(`mockito-inline`) + AssertJ
- 통합 테스트는 `@SpringBootTest` + `@EnableTestProfile` + `@AutoConfigureMockMvc` 조합을 사용한다.
  (`@EnableTestProfile`는 `com.komentum.test.config`의 커스텀 애노테이션)
- DB는 **H2**, Redis는 **embedded-redis**로 대체된다 — 별도 인프라 없이 테스트가 돈다.
- 테스트 데이터는 직접 만들지 말고 `src/test/java/com/komentum/test/` 하위의 헬퍼를 재사용한다:
  `MockMvcUtils`, `data/*DataGenerator`, `data/scenario/*ScenarioSupport`, `fixture/**`.

## 아키텍처 (Architecture Rule)

### 패키지 = 기능 도메인, 도메인 내부 = 계층

`com.komentum` 아래는 기능 도메인별로 나뉜다: `auth`, `user`, `theme`(core/android), `post`,
`designcomponent`, `catalog`, `seed`, 그리고 공용 `global` / `config`.
각 도메인은 아래 계층 구조를 따른다:

```
controller → facade → service → repository → domain(Entity)
                 ↘ mapper(MapStruct) ↗   dto / enums / policy
```

- **Controller**: HTTP 경계. 얇게 유지하고 Facade만 호출한다. 검증/Swagger 애노테이션 담당.
- **Facade** (`facade/*ManagementFacade`, `@Service`로 선언됨): 여러 service·file·transaction을 조합하는
  유스케이스 오케스트레이션 계층. 파일 I/O와 DB 트랜잭션이 함께 필요한 흐름은 여기서 조립한다.
- **Service**: 단일 도메인 비즈니스 로직 + 트랜잭션. DB 커밋 경계가 파일 작업과 얽히는 경우,
  순수 DB 작업을 별도 `service/transaction/*TransactionService`로 분리한다 (예: `ThemeBoardTransactionService`).
- **Repository**: JPA `*Repository` + 동적 쿼리용 `*RepositorySupport`(QueryDSL). 정렬·조건은
  `repository/order/*Order`, `repository/predicate/*Predicate`, `service/condition/*Condition`으로 분리.
- **Mapper**: Entity ↔ DTO 변환은 MapStruct(`*Mapper`). 커스텀 로직이 필요하면 `*MapperSupport`.
- **domain/policy**: 권한·소유권 검증 등 도메인 규칙(`OwnerAdminPolicy`, `PostPolicy` 등).

### 파일 저장 롤백 규칙

파일 업로드와 DB 저장이 함께 일어나는 흐름에서는 **파일을 먼저 저장 → DB 트랜잭션 실행 → 실패 시 저장한
파일을 보상 삭제**하는 패턴을 따른다. `BoardManagementHelper.savePreviewImageIfPresent(...)` /
`deleteFileSilently(...)`를 사용하고, 삭제 실패는 로그만 남기고 흘려보낸다(`Silently`).
저장소 구현은 `FileManager` 인터페이스 뒤에 `LocalFileManager` / `S3FileManager`가 있으며
`file.storage` 설정값(`local` | `s3`)으로 스위칭된다.

### 안드로이드 테마(APK) 빌드

`theme/android/`는 사용자 테마를 실제 `.apk`로 빌드한다. `DockerProcessRunner`가 별도 Docker 이미지
(`louie8821/android-apk-builder`)를 컨테이너로 실행하고, XML/YAML/이미지 리소스를 편집(`editor/`, `utils/`)한 뒤
keystore로 서명한다. 이 때문에 애플리케이션 컨테이너는 `/var/run/docker.sock`을 마운트한다(Docker-in-Docker).
서명 키·keystore 경로는 `theme.android.signing.*` 환경변수로 주입된다.

### 시드 데이터

`seed/`(javafaker 기반 `DevDataGenerator` + `seeder/*Seeder`)는 개발용 더미 데이터 생성용이다.
`designcomponent`·`theme`의 기본 카탈로그 데이터는 `service/seeder/`와 `resources/theme-data/*.json`
(`color_style.json`, `component_type.json`, `theme_spec*.json`, `defaultThemes/`)에서 임포트한다.

## API 규칙

- 모든 REST 엔드포인트는 `@RestController` + `/api/...` 경로. 반환은 `ResponseEntity<T>`.
- **경로 변수·쿼리 파라미터는 snake_case** 문자열을 쓰고 Java 파라미터에 명시적으로 매핑한다
  (`@PathVariable("post_id") Long postId`, `@RequestParam("pinned_post_id")`).
- 페이지 조회: `@PageableDefault(size = 20, sort = "createdAt")` + `@ParameterObject Pageable`.
- 인증 사용자: `@AuthenticationPrincipal CustomUserDetails userDetails` (식별자는 `getUsername()`).
- 파일 업로드가 있는 생성/수정은 `consumes = MULTIPART_FORM_DATA_VALUE` + `@RequestPart`(JSON 파트 + 파일 파트).
- Swagger: 모든 공개 엔드포인트에 `@Operation(summary = ...)`를 붙인다.
- 공용 응답 래퍼 `global.dto.CustomResponse<T>`(`status`/`message`/`data`)가 존재하나 도메인마다
  raw DTO를 그대로 반환하기도 한다 — **주변 컨트롤러의 반환 스타일을 그대로 따를 것**.
- 예외 처리는 컨트롤러에서 하지 말고 `global.exception.GlobalExceptionHandler`(`@ControllerAdvice`)에 위임한다.
  도메인 예외(`ResourceNotFoundException`, `UnauthorizedException`, `CustomEntityNotFoundException` 등)를 던지면
  `{ "message": ... }` 형태 + 적절한 HTTP status로 변환된다.

## 인증 / 보안

- Stateless JWT. `JwtAuthFilter`가 `UsernamePasswordAuthenticationFilter` 앞에서 토큰을 검증한다.
- 소셜 로그인: OAuth2 (Kakao, Google). 성공/실패는 `OAuth2LogInSuccessHandler` /
  `OAuth2LoginFailureHandler`, 사용자 로딩은 `CustomOauth2UserService`.
- 토큰은 쿠키로 관리(`TokenCookieManager`), refresh는 Redis(`user/redis`) 사용.
- 인가: 화이트리스트 경로(`security.white-list` 설정)는 permitAll, 디자인 카탈로그 관리
  엔드포인트(`/api/color-styles/**`, `/api/component-types/**`, `/api/platform-*` )는 `ROLE_ADMIN`,
  나머지는 인증 필요. 새 공개 엔드포인트는 코드가 아니라 `backend_config`의 `security.white-list`에 추가한다.

## Coding Convention

- **들여쓰기 2 스페이스** (기존 파일 스타일 유지). Lombok 적극 사용: 생성자 주입은 `@RequiredArgsConstructor`
  + `private final` 필드.
- DTO는 도메인별 **중첩 클래스 그룹**으로 묶는 관례가 있다 (`ThemeBoardDto.ThemeBoardCreateDto`,
  `ThemeBoardDto.ThemeBoardDetailDto` 등). 새 DTO도 해당 그룹에 넣는다.
- QueryDSL Q클래스는 `src/main/generated`에 생성되며 커밋하지 않는다(gitignore + clean 시 삭제).
- 주석·JavaDoc·Swagger summary는 한국어로 작성한다(기존 스타일).
- 커밋/PR: `.github/PULL_REQUEST_TEMPLATE.md`를 따르고 이슈를 연결한다. 커밋 메시지 관례는
  최근 로그(`Feat/#128 ...`, `Refactor/#117 ...`) 참고 — `타입/#이슈번호 설명` 형식.

## 폴더 구조 (요약)

```
src/main/java/com/komentum/
  KomentumApplication.java     # 진입점 (@SpringBootApplication, @EnableJpaAuditing)
  auth/                        # JWT 발급·검증 유틸
  user/                        # 사용자, 로컬/소셜 인증 (redis/, client/)
  theme/core/                  # 테마 CRUD·조회 (도메인: ThemeComponent/ThemeImage/ThemeStyle)
  theme/android/               # 테마 → APK 빌드 (Docker, editor/, utils/)
  post/                        # 게시판(테마/디자인 보드, 댓글, 태그, 좋아요, 북마크, 카테고리)
  designcomponent/             # 디자인 컴포넌트·색상 스타일·컴포넌트 타입 (관리자)
  catalog/                     # 컴포넌트 카탈로그 조회
  seed/                        # 개발용 더미 데이터 시더 (javafaker)
  global/                      # 공용: security/ exception/ dto/ utils/ properties/ domain/policy
  config/                      # Swagger, QueryDsl, Web, Multipart 설정
src/main/resources/theme-data/ # 기본 테마·색상·컴포넌트 시드 JSON
src/test/java/com/komentum/test/ # 테스트 공용 유틸·데이터 생성기·시나리오·픽스처
backend_config/ (서브모듈)      # application*.yml (프로필별)
```

## Skills (기능별 상세 문서)

기능별 심화 문서는 `docs/` 아래 마크다운으로 두고 필요 시 여기서 링크한다. *(현재 저장소에 별도 기능 문서는
아직 없음 — 새 기능의 비자명한 흐름을 문서화할 때 아래에 추가할 것.)*

- 안드로이드 APK 빌드 파이프라인: `theme/android/` (Docker 실행 + 리소스 편집 + 서명)
- 게시판 상세/검색 유스케이스: `post/facade/ThemeBoardManagementFacade`, `post/repository/**Support`
- 디자인 컴포넌트 시딩: `designcomponent/service/seeder/`, `resources/theme-data/*.json`

## External Knowledge

*(현재 저장소 파일에서 참조되는 Notion/Obsidian/Wiki 등 외부 문서 링크는 발견되지 않았다.
설계 문서·API 스펙 등 외부 지식 베이스가 있다면 아래에 링크를 채워 넣을 것.)*

- Swagger UI (로컬 실행 시): `http://localhost:8080/swagger-ui.html`
- 설정 서브모듈: `backend_config` (private) — `.gitmodules` 참고
