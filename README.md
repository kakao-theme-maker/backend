# Komentum Backend

카카오톡 테마 제작, 커스터마이징, 공유, 커뮤니티 기능을 제공하는 Spring Boot 백엔드입니다. 사용자는 테마와 디자인 컴포넌트를 만들고, 게시판에 공유하며, 좋아요/북마크/댓글/카테고리로 콘텐츠를 관리할 수 있습니다.

## 목차

- [프로젝트 개요](#프로젝트-개요)
- [주요 기능](#주요-기능)
- [기술 스택](#기술-스택)
- [아키텍처](#아키텍처)
- [패키지 구조](#패키지-구조)
- [로컬 실행](#로컬-실행)
- [Docker 실행](#docker-실행)
- [테스트](#테스트)
- [API 개요](#api-개요)
- [운영 및 보안 메모](#운영-및-보안-메모)

## 프로젝트 개요

이 프로젝트는 카카오톡 테마 제작 서비스를 위한 단일 모듈 백엔드 애플리케이션입니다. 테마 편집 데이터, 이미지 리소스, 색상 스타일, 디자인 컴포넌트, 게시판 콘텐츠, 사용자 인증을 하나의 Spring Boot 애플리케이션에서 처리합니다.

서비스의 핵심 목표는 다음과 같습니다.

- 사용자가 직접 만든 테마와 디자인 컴포넌트를 저장하고 재사용할 수 있게 한다.
- Android/iOS 테마 패키징에 필요한 리소스 편집 흐름을 백엔드에서 관리한다.
- 테마 게시판과 디자인 게시판을 통해 사용자 제작물을 공유한다.
- JWT, OAuth2, Redis 기반 인증 구조로 로그인과 토큰 재발급을 처리한다.
- 파일 저장소를 로컬/S3 구현체로 분리해 개발 환경과 배포 환경을 구분한다.

## 주요 기능

| 영역 | 기능                                                      |
| --- |---------------------------------------------------------|
| 사용자/인증 | 로컬 회원가입, 로그인, 로그아웃, 토큰 재발급, Kakao OAuth2 연동, 사용자 프로필 수정 |
| 테마 | 테마 생성, 상세 조회, 수정, 완료 처리, 공개/인기/북마크 테마 조회                |
| 테마 패키징 | Android 테마 리소스 편집, iOS 테마 패키지 생성, 이미지/색상 스타일 반영         |
| 디자인 컴포넌트 | 단일/다중 업로드, 컴포넌트 타입/색상 스타일 매핑, 사용자별 업로드/북마크 조회           |
| 게시판 | 테마 게시글, 디자인 게시글, 대표 이미지 업로드, 목록/상세 조회, 수정, 삭제           |
| 커뮤니티 | 댓글, 댓글 좋아요, 게시글 좋아요, 북마크, 카테고리, 태그                      |
| 카탈로그 | 사용자의 테마와 디자인 컴포넌트를 통합한 커스텀 컴포넌트 조회                      |
| Seed | 개발용 사용자, 게시글, 댓글, 테마, 디자인 컴포넌트 데이터 생성                   |

## 기술 스택

| 분류 | 기술 |
| --- | --- |
| Language | Java 17 |
| Framework | Spring Boot 3.4.3, Spring Web, Spring Validation |
| Security | Spring Security, JWT, OAuth2 Client, Kakao OAuth |
| Data | Spring Data JPA, QueryDSL, MySQL, Redis |
| Mapping | MapStruct, Lombok |
| File Storage | Local File Storage, AWS S3, CloudFront |
| API 문서 | springdoc-openapi, Swagger UI |
| Test | JUnit 5, Spring Boot Test, Mockito, H2, Embedded Redis |
| Build/Runtime | Gradle, Docker, Docker Compose |

빌드 산출물은 `kakao-theme-maker.jar` 이름으로 생성됩니다.

## 아키텍처

요청 처리는 다음 계층을 기준으로 분리되어 있습니다.

```text
Client
  -> Controller
  -> Facade / Service
  -> RepositorySupport / Repository
  -> Database

File Upload
  -> FileManager
  -> Local Storage or S3
```

- `Controller`: HTTP 요청, multipart payload, 인증 principal을 받아 유스케이스 계층에 위임합니다.
- `Facade`: 여러 도메인 서비스, 트랜잭션, 파일 업로드/삭제를 조합하는 애플리케이션 유스케이스를 담당합니다.
- `Service`: 엔티티 조회, 검증, 상태 변경, 단일 도메인 규칙을 처리합니다.
- `RepositorySupport`: QueryDSL 기반 목록 조회, projection, 동적 조건, 정렬을 담당합니다.
- `Repository`: Spring Data JPA 기반 CRUD를 담당합니다.
- `FileManager`: 파일 저장소를 추상화하며 로컬 저장소와 S3 저장소 구현을 분리합니다.

## 패키지 구조

```text
src/main/java/com/komentum
|-- auth                # JWT 생성/검증, 토큰 타입
|-- catalog             # 사용자 커스텀 컴포넌트 통합 조회
|-- config              # QueryDSL, Swagger, Web, multipart 설정
|-- designcomponent     # 디자인 컴포넌트, 타입, 색상 스타일, 플랫폼 매핑
|-- global              # 공통 응답, 예외, 보안, 파일 저장소, 유틸, properties
|-- post                # 게시글, 게시판, 댓글, 좋아요, 북마크, 카테고리, 태그
|-- seed                # 개발용 seed 데이터 생성
|-- theme               # 테마 편집, Android/iOS 리소스 처리, 테마 조회/관리
`-- user                # 사용자, 인증, OAuth2/Kakao, Redis 토큰 저장
```

생성 코드는 `src/main/generated`에 위치하며 직접 수정하지 않습니다.

## 로컬 실행

### 요구 사항

- JDK 17
- MySQL 8.x
- Redis 6.x 이상
- Docker. Android 테마 패키징 기능은 Docker CLI와 apktool 관련 이미지를 사용합니다.

### 설정 파일

Gradle 빌드와 실행 과정은 다음 설정 파일을 `src/main/resources` 또는 `src/test/resources`로 복사하도록 구성되어 있습니다.

```text
backend_config/dev/monolithic/application.yml
backend_config/dev/monolithic/application-test.yml
```

로컬 실행 전 `application.yml`에는 최소한 다음 값이 필요합니다.

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/theme_database
    username: root
    password: <DB_PASSWORD>
  data:
    redis:
      host: localhost
      port: 6379

jwt:
  secret: <JWT_SECRET>

file:
  base-url: http://localhost:8080
  storage: local
```

Kakao OAuth, AWS S3, CloudFront, allowed origin, token 만료 시간은 환경별 설정 파일이나 환경 변수로 관리합니다. 실제 secret 값은 저장소에 커밋하지 않습니다.

### 실행 명령

```bash
./gradlew bootRun
```

기본 서버 포트는 `8080`입니다.

Swagger UI가 활성화된 환경에서는 다음 주소에서 API를 확인할 수 있습니다.

```text
http://localhost:8080/swagger-ui/index.html
```

## Docker 실행

Docker Compose 구성은 backend, MySQL, Redis 서비스를 함께 실행합니다.

```bash
docker compose -f docker/docker-compose.yml up -d
```

주요 포트는 다음과 같습니다.

| 서비스 | 컨테이너 포트 | 호스트 포트 |
| --- | ---: | ---: |
| Backend | 8080 | 28080 |
| MySQL | 3306 | 23306 |
| Redis | 6379 | compose 내부 네트워크 |

Compose 실행에는 `docker/.env` 파일이 필요합니다.

```env
MYSQL_ROOT_PASSWORD=<DB_PASSWORD>
MYSQL_DATABASE=theme_database
MYSQL_ROOT_HOST=%
```

Docker 이미지 빌드 시 Gradle이 `backend_config`의 설정 파일을 복사하므로, 배포 환경에서는 설정 파일과 secret 관리 방식을 먼저 확정해야 합니다.

## 테스트

전체 테스트는 다음 명령으로 실행합니다.

```bash
./gradlew test
```

테스트는 JUnit 5 기반이며 H2와 Embedded Redis를 사용합니다. Gradle의 `copyTestConfig` task가 `backend_config/dev/monolithic/application-test.yml`을 `src/test/resources`로 복사하므로, 해당 설정 파일이 없으면 테스트가 시작 전에 실패할 수 있습니다.

테스트 작성 시 권장 기준은 다음과 같습니다.

- Controller 변경은 MockMvc 기반 테스트를 추가합니다.
- 파일 업로드 기능은 DB rollback뿐 아니라 업로드 파일 정리까지 검증합니다.
- 인증/인가 기능은 owner/admin 성공/실패 케이스를 모두 확인합니다.
- QueryDSL 목록 조회는 정렬, pageable, 좋아요/북마크 계산을 함께 확인합니다.

## API 개요

응답은 도메인별 DTO 또는 공통 응답 래퍼를 사용합니다. 인증이 필요한 API는 `Authorization: Bearer <ACCESS_TOKEN>` 헤더 또는 refresh token cookie 흐름을 전제로 합니다.

### Auth / User

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/auth/local/sign-up` | 로컬 회원가입 |
| `POST` | `/api/auth/local/sign-in` | 로컬 로그인 및 토큰 발급 |
| `POST` | `/api/auth/local/sign-out` | 로그아웃 및 저장된 토큰 삭제 |
| `POST` | `/api/auth/reissue` | refresh token 기반 access token 재발급 |
| `GET` | `/api/users/me` | 현재 로그인 사용자 조회 |
| `PATCH` | `/api/users/me/profile-image` | 프로필 이미지 수정 |
| `PUT` | `/api/users/{public_user_id}/follow` | 사용자 팔로우 |
| `DELETE` | `/api/users/{public_user_id}/follow` | 사용자 팔로우 해제 |

### Theme

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/themes` | 새 테마 생성 |
| `PUT` | `/api/themes/{themeComponentId}` | 테마 색상/이미지/상태 수정 |
| `POST` | `/api/themes/{themeComponentId}/packages/ios` | iOS 테마 패키지 생성 |
| `PUT` | `/api/themes/{id}/done` | 테마 완료 처리 |
| `GET` | `/api/themes/public` | 공개 테마 목록 조회 |
| `GET` | `/api/themes/popular` | 인기 테마 조회 |
| `GET` | `/api/themes/bookmarked` | 북마크한 테마 조회 |

### Design Component

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/design-components` | 디자인 컴포넌트 단일 업로드 |
| `POST` | `/api/design-components/bulk` | 디자인 컴포넌트 대량 업로드 |
| `GET` | `/api/design-components` | 디자인 컴포넌트 목록 조회 |
| `GET` | `/api/design-components/uploaded` | 내가 업로드한 컴포넌트 조회 |
| `GET` | `/api/design-components/bookmarked` | 북마크한 컴포넌트 조회 |
| `PUT` | `/api/design-components/{id}` | 디자인 컴포넌트 수정 |
| `DELETE` | `/api/design-components/{id}` | 디자인 컴포넌트 삭제 |

### Board / Post

| Method | Path | 설명 |
| --- | --- | --- |
| `GET` | `/api/theme-boards` | 테마 게시글 목록 조회 |
| `POST` | `/api/theme-boards` | 테마 게시글 생성 |
| `PUT` | `/api/theme-boards/{post_id}` | 테마 게시글 수정 |
| `GET` | `/api/design-boards` | 디자인 게시글 목록 조회 |
| `POST` | `/api/design-boards` | 디자인 게시글 생성 |
| `PATCH` | `/api/design-boards/{post_id}` | 디자인 게시글 수정 |

### Community / Catalog

| Method | Path | 설명 |
| --- | --- | --- |
| `POST` | `/api/posts/{post_id}/comments` | 댓글 작성 |
| `POST` | `/api/comments/{commentId}/like` | 댓글 좋아요 |
| `POST` | `/api/posts/{post_id}/prefer` | 게시글 좋아요 |
| `PUT` | `/api/bookmarks/posts/{post_id}` | 게시글 북마크 |
| `POST` | `/api/categories` | 카테고리 생성 |
| `GET` | `/api/users/me/custom-components` | 사용자 커스텀 컴포넌트 통합 조회 |

### Admin Seed

| Method | Path | 설명 |
| --- | --- | --- |
| `PUT` | `/api/component-types/seed` | 컴포넌트 타입 seed 반영 |
| `PUT` | `/api/color-styles/seed` | 색상 스타일 seed 반영 |
| `PUT` | `/api/platform-component-types/seeds` | 플랫폼별 컴포넌트 타입 seed 반영 |
| `PUT` | `/api/platform-color-styles/seeds` | 플랫폼별 색상 스타일 seed 반영 |

## 운영 및 보안 메모

- JWT secret, Kakao OAuth client secret, AWS access key, DB password는 코드와 README에 직접 기록하지 않습니다.
- 운영 환경에서는 Swagger UI와 개발용 인증 API 노출 여부를 별도로 제한해야 합니다.
- 파일 저장소를 `local`로 사용할 때는 정적 리소스 URL prefix와 실제 업로드 경로가 일치해야 합니다.
- S3 저장소를 사용할 때는 bucket, region, CloudFront 도메인, IAM 권한을 환경별로 분리합니다.
- refresh token rotation은 Redis 저장값을 기준으로 처리되므로 Redis 장애 또는 데이터 초기화가 로그인 세션에 영향을 줄 수 있습니다.
- Android 테마 패키징은 Docker socket과 임시 디렉터리를 사용하므로 배포 환경에서 컨테이너 권한을 신중하게 제한해야 합니다.
