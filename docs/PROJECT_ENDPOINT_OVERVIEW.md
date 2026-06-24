# like_lion 프로젝트 구성 및 팀 작업 가이드

작성일: 2026-06-18

이 문서는 현재 코드 기준의 백엔드 스켈레톤, 공통 기반, 팀원별 구현 경계를 설명한다. API 상세 계약은 `docs/API_SPEC_COMPLETED.md`, DB 기준은 `docs/DB_SCHEMA.md`를 우선한다.

## 1. 현재 상태

현재 프로젝트는 Spring Boot/Kotlin 백엔드 스켈레톤이다. Controller/DTO는 보강 API 명세에 맞춰 있고, JPA Entity/Repository, Flyway 초기 schema, 공통 예외 코드까지 1차 기반이 준비되어 있다.

서비스 내부 로직은 아직 대부분 더미 반환이다. 팀원은 각자 맡은 Service에 Repository와 외부 모듈을 연결해 실제 로직을 채우면 된다.

## 2. 기술 스택

| 항목 | 현재 구성 |
| --- | --- |
| Language | Kotlin 2.3.0 |
| Framework | Spring Boot 3.5.0 |
| Java | JVM Toolchain 17 |
| Web | `spring-boot-starter-web` |
| Validation | `spring-boot-starter-validation` |
| JSON | `jackson-module-kotlin` |
| DB/JPA | `spring-boot-starter-data-jpa` |
| Migration | Flyway |
| Local DB | H2 MySQL mode |
| RDS 대상 | MySQL profile |
| Test | `spring-boot-starter-test`, `kotlin("test")` |
| Security/JWT | 아직 없음 |
| Mail | 아직 없음 |

## 3. 프로젝트 구조

```text
src/main/kotlin/com/likelion
├─ LikeLionApplication.kt
├─ common
│  ├─ ApiResponse.kt
│  ├─ ApiException.kt
│  ├─ ErrorCode.kt
│  ├─ GlobalExceptionHandler.kt
│  └─ auth
│     ├─ CurrentUser.kt
│     ├─ CurrentUserProvider.kt
│     └─ SecurityContextCurrentUserProvider.kt
├─ auth
├─ category
├─ store
├─ favorite
├─ mypage
├─ qr
├─ admin
└─ domain
   ├─ auth
   ├─ benefit
   ├─ benefitusage
   ├─ category
   ├─ common
   ├─ favorite
   ├─ menu
   ├─ qr
   ├─ store
   └─ user
```

```text
src/main/resources
├─ application.yml
└─ db/migration
   └─ V1__init_schema.sql
```

## 4. 요청 처리 흐름

```text
Client
  -> Controller
  -> Request DTO validation
  -> Service
  -> Repository / external module
  -> Response DTO
  -> ApiResponse<T>
```

예외 흐름:

```text
throw ApiException(ErrorCode.STORE_404)
  -> GlobalExceptionHandler
  -> ApiResponse.error(...)
```

## 5. 공통 응답과 HTTP Status

모든 응답 body는 `ApiResponse<T>`를 사용한다.

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "응답 메시지",
  "data": {}
}
```

| 함수 | body status | code | 용도 |
| --- | ---: | --- | --- |
| `ApiResponse.ok(...)` | 200 | `OK` | 조회/수정/삭제 성공 |
| `ApiResponse.created(...)` | 201 | `CREATED` | 생성 성공 |
| `ApiResponse.error(...)` | 지정값 | 지정값 | 실패 응답 |

생성 API는 실제 HTTP status도 201이어야 하므로 Controller에 `@ResponseStatus(HttpStatus.CREATED)`를 같이 붙인다. 현재 회원가입, 즐겨찾기 추가, 관리자 매장/메뉴/혜택 등록에는 적용되어 있다.

## 6. 공통 예외 규칙

서비스에서 문자열 code를 직접 만들지 않는다.

```kotlin
throw ApiException(ErrorCode.STORE_404)
```

필요한 에러가 없으면 `ErrorCode`에 먼저 추가한 뒤 사용한다.

현재 주요 코드:

```text
COMMON_400, COMMON_500
AUTH_001, AUTH_003, AUTH_004, AUTH_400, AUTH_401, AUTH_409
USER_404, USER_409
STORE_404
FAVORITE_409
QR_400, QR_404, QR_409
ADMIN_STORE_404, ADMIN_MENU_404, ADMIN_BENEFIT_404
```

## 7. JWT 인증 규칙

Spring Security가 Bearer JWT를 검증하고 `SecurityContextCurrentUserProvider`가 로그인 사용자를 제공한다. 로그인 사용자 정보가 필요한 Service는 직접 사용자 ID를 받지 않고 `CurrentUserProvider`를 주입한다.

```kotlin
class FavoriteService(
    private val currentUserProvider: CurrentUserProvider,
) {
    fun addFavorite(storeId: Long) {
        val userId = currentUserProvider.currentUserId()
    }
}
```

회원가입 비밀번호는 BCrypt 해시로 저장한다. 로그인 성공 시 1시간 유효한 JWT access token을 반환하며, 즐겨찾기와 마이페이지 API는 인증이 필요하다.

## 8. UserType 기준

사용자 유형의 기준 enum은 `com.likelion.domain.user.UserType`이다.

```text
STUDENT
OWNER
ADMIN
```

일반 회원가입 request는 ADMIN을 받으면 안 되므로 `auth.SignupUserType`을 별도로 둔다.

```text
SignupUserType = STUDENT, OWNER
```

정리하면:

| 용도 | 타입 |
| --- | --- |
| DB 사용자 유형 | `domain.user.UserType` |
| 로그인/마이페이지 응답 | `domain.user.UserType` |
| 회원가입 request/response | `auth.SignupUserType` |

## 9. Entity 관계 규칙

기본 원칙은 단순 ID 참조다.

예:

```kotlin
var storeId: Long
var userId: Long
```

현재 의도적으로 객체 관계를 둔 곳은 혜택 대상뿐이다.

```kotlin
BenefitEntity.targetColleges: MutableSet<CollegeEntity>
BenefitEntity.targetDepartments: MutableSet<DepartmentEntity>
```

이유는 보강 명세에서 혜택 등록 request가 `collegeIds`, `departmentIds` 배열을 받기 때문이다. DB에는 중간 테이블 `benefit_target_colleges`, `benefit_target_departments`가 있고, 별도 Target Entity/Repository는 두지 않는다.

팀원이 임의로 `@ManyToOne`, `@OneToMany`를 추가하지 않는다. 필요하면 먼저 공유하고 규칙을 바꾼다.

## 10. DB와 Migration 규칙

DB 구조는 Flyway migration이 기준이다.

```text
src/main/resources/db/migration/V1__init_schema.sql
```

JPA 설정은 `ddl-auto: validate`다. Entity와 DB schema가 다르면 서버가 뜨지 않는다.

DB 변경 규칙:

- 기존 `V1__init_schema.sql`을 수정하지 않는다.
- 변경이 필요하면 `V2__...sql`을 추가한다.
- Entity 변경과 migration 변경은 같은 PR/커밋에 같이 포함한다.
- DB 상세 기준은 `docs/DB_SCHEMA.md`를 따른다.

RDS MySQL 연결은 `mysql` profile을 쓴다.

```powershell
$env:SPRING_PROFILES_ACTIVE="mysql"
$env:DB_URL="jdbc:mysql://{host}:{port}/{database}?serverTimezone=Asia/Seoul&characterEncoding=utf8"
$env:DB_USERNAME="{username}"
$env:DB_PASSWORD="{password}"
.\gradlew.bat bootRun
```

## 11. Repository 추가 규칙

Repository는 각 domain 패키지에 둔다.

예:

```text
domain/store/StoreRepository.kt
domain/favorite/FavoriteRepository.kt
```

기본 CRUD는 이미 `JpaRepository`가 제공한다. 필요한 조회는 담당자가 추가하되, 아래 기준을 따른다.

- 단건 조회: `findBy...`
- 존재 확인: `existsBy...`
- 목록 조회: `findAllBy...`
- 활성 매장 조회는 `isActive=true` 조건을 포함한다.
- soft delete 대상은 일반 사용자 조회에서 제외한다.
- 복잡한 검색은 처음부터 무리하게 JPQL을 만들지 말고 Service/Repository 책임을 분리해서 PR에서 논의한다.

## 12. Endpoint 지도

```text
/api/v1
├─ /auth
│  ├─ POST /email/send-code
│  ├─ POST /email/verify
│  ├─ POST /signup
│  └─ POST /login
├─ /colleges
├─ /departments
├─ /stores
│  ├─ GET /
│  ├─ GET /map
│  ├─ GET /search
│  ├─ GET /{storeId}
│  ├─ GET /{storeId}/summary
│  └─ POST/DELETE /{storeId}/favorite
├─ /me
│  ├─ GET /
│  ├─ GET /favorites
│  └─ GET /benefit-usages
├─ /qr
│  └─ POST /verify
└─ /admin
   ├─ POST /stores
   ├─ PATCH /stores/{storeId}
   ├─ DELETE /stores/{storeId}
   ├─ POST /stores/{storeId}/menus
   ├─ PATCH /menus/{menuId}
   ├─ POST /stores/{storeId}/benefits
   ├─ PATCH /benefits/{benefitId}
   └─ POST /stores/{storeId}/qr/regenerate
```

## 13. 팀원별 구현 가능 범위

### Auth 담당

- `AuthService`
- 이메일 인증 코드 저장/검증
- 비밀번호 해싱
- 로그인
- JWT 발급
- JWT 검증과 SecurityContext 사용자 연동

### Store 담당

- `StoreService`
- 매장 리스트 필터링
- 지도용 조회
- 검색 자동완성
- 상세/요약 조회
- `STORE_404` 처리

### Favorite/MyPage 담당

- `FavoriteService`
- `MyPageService`
- `CurrentUserProvider` 사용
- 즐겨찾기 중복 처리
- 혜택 사용 내역 페이지네이션

### Admin 담당

- `AdminService`
- 매장/메뉴/혜택 등록, 수정, 비활성화
- QR 토큰 재발급
- 관리자 권한은 JWT/Security 도입 전까지 TODO로 표시

### QR 담당

- `QrService`
- QR 토큰 검증
- 중복 인증 제한
- `BenefitUsageEntity` 생성

## 14. 아직 공통으로 주의할 점

- 실제 인증/권한 검사는 아직 없다.
- 메일 발송은 아직 없다.
- Service는 대부분 더미 데이터를 반환한다.
- seed data는 아직 없다.
- 테스트는 Spring context 로딩 수준이다.
- DB 변경은 반드시 migration으로 한다.

## 15. 검증 명령

```powershell
.\gradlew.bat test
```

현재 이 명령은 Flyway migration 적용, JPA entity/schema validate, Spring context 로딩까지 확인한다.
