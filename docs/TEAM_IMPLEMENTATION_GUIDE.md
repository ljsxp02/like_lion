# 팀원별 Service 구현 가이드

작성일: 2026-06-18

이 문서는 QR/Admin을 제외하고 Auth, Category/Store, Favorite/MyPage 구현을 나눠 진행하기 위한 작업 지시서다.

공통 기준 문서:

- API 계약: `docs/API_SPEC_COMPLETED.md`
- 프로젝트/endpoint 구조: `docs/PROJECT_ENDPOINT_OVERVIEW.md`
- DB 기준: `docs/DB_SCHEMA.md`

## 1. 이번 구현 범위

이번에 구현할 파트:

```text
A: Auth
B: Category + Store 조회
C: Favorite + MyPage
```

이번에 구현하지 않는 파트:

```text
QR 인증
Admin API
```

QR/Admin은 Controller와 Service 골격이 있어도 이번 범위에서는 건드리지 않는다. 단, 다른 파트 구현에 필요한 Entity/Repository query 추가는 가능하다.

### 최종 구현 제외 범위

아래 기능은 이번 프로젝트에서 구현하지 않는다.

- 실제 JWT/Spring Security 인증 및 권한 관리
- QR 검증, 중복 사용 방지, 혜택 사용 기록 처리
- 운영자 마이페이지와 Admin 매장·메뉴·혜택 관리 기능
- 프론트엔드 지도, GPS, 바텀시트, 화면 이동, QR 카메라 UI
- 식사·카페·주류·기타 등의 매장 업종 카테고리 및 업종별 필터

현재 존재하는 `MockCurrentUserProvider`, QR/Admin Controller·Service 골격과 더미 응답은 시연용 또는 추후 확장 지점으로만 유지한다. 매장 업종 카테고리를 위한 별도 DB 테이블이나 migration도 추가하지 않는다. 지도 응답의 `categories`는 빈 배열로 유지한다. 위 기능은 이번 프로젝트의 완료 기준과 테스트 범위에 포함하지 않는다.

## 2. 공통 작업 원칙

### API 응답

Controller 응답은 기존처럼 `ApiResponse<T>`를 사용한다.

```kotlin
ApiResponse.ok(message = "...", data = ...)
ApiResponse.created(message = "...", data = ...)
```

생성 API는 `ApiResponse.created(...)`와 `@ResponseStatus(HttpStatus.CREATED)`를 같이 사용한다.

### 예외 처리

Service에서 에러가 필요하면 문자열을 직접 만들지 않는다.

```kotlin
throw ApiException(ErrorCode.STORE_404)
```

필요한 코드가 없으면 `common/ErrorCode.kt`에 먼저 추가하고 사용한다. 공통 ErrorCode 추가는 팀에 공유한다.

### 현재 로그인 사용자

로그인 사용자 ID가 필요하면 하드코딩하지 않는다.

금지:

```kotlin
val userId = 1024L
```

권장:

```kotlin
class FavoriteService(
    private val currentUserProvider: CurrentUserProvider,
) {
    fun addFavorite(storeId: Long) {
        val userId = currentUserProvider.currentUserId()
    }
}
```

현재는 `MockCurrentUserProvider`가 고정 사용자를 반환한다. 나중에 JWT/Security가 붙으면 Provider 구현만 교체한다.

### DB 변경

기존 `V1__init_schema.sql`은 수정하지 않는다.

새 컬럼이나 인덱스가 필요하면:

```text
src/main/resources/db/migration/V2__description.sql
```

처럼 새 migration을 추가한다. 여러 명이 동시에 `V2`를 만들면 충돌나므로 DB 담당자에게 번호를 확인한다.

### Entity 관계

기본은 ID 참조다.

```kotlin
var userId: Long
var storeId: Long
```

임의로 `@ManyToOne`, `@OneToMany`를 추가하지 않는다. 현재 예외는 `BenefitEntity.targetColleges`, `BenefitEntity.targetDepartments`뿐이다.

### 테스트

작업 후 최소한 아래 명령은 통과해야 한다.

```powershell
.\gradlew.bat test
```

가능하면 담당 Service 단위 테스트 또는 Controller 테스트를 추가한다.

## 3. A 파트: Auth 구현

담당 파일:

```text
src/main/kotlin/com/likelion/auth/AuthController.kt
src/main/kotlin/com/likelion/auth/AuthService.kt
src/main/kotlin/com/likelion/auth/AuthDtos.kt
src/main/kotlin/com/likelion/domain/user/UserEntity.kt
src/main/kotlin/com/likelion/domain/user/UserRepository.kt
src/main/kotlin/com/likelion/domain/auth/EmailVerificationCodeEntity.kt
src/main/kotlin/com/likelion/domain/auth/EmailVerificationCodeRepository.kt
```

### 구현할 endpoint

| Method | Endpoint | 구현 내용 |
| --- | --- | --- |
| POST | `/api/v1/auth/email/send-code` | 이메일 인증 코드 생성/저장 |
| POST | `/api/v1/auth/email/verify` | 인증 코드 검증, verificationToken 발급 |
| POST | `/api/v1/auth/signup` | 이메일 인증 없이 회원가입 |
| POST | `/api/v1/auth/login` | 로그인, 비밀번호 검증, 토큰 반환 |

### 구현 상세

#### 이메일 인증 코드 발송

해야 할 일:

- 이메일 형식 validation은 DTO에 있으므로 Service에서는 도메인 정책 검증
- 학교 이메일 도메인 확인. 예: `@kw.ac.kr`
- 인증 코드 생성
- 만료 시간 5분 적용
- 재발송 제한 60초 적용
- `EmailVerificationCodeEntity` 저장

응답:

```json
{
  "email": "student@kw.ac.kr",
  "expiresInSeconds": 300,
  "resendAvailableInSeconds": 60
}
```

주의:

- 실제 메일 발송은 의존성이 아직 없으므로, 이번 구현에서 진짜 메일 발송까지 할지 팀장이 결정해야 한다.
- 메일 발송을 하지 않는다면 code를 로그로 남기거나 테스트용으로 고정하지 말고, 개발용 임시 정책을 문서에 남긴다.

#### 이메일 인증 코드 확인

해야 할 일:

- 이메일 기준 최신 인증 코드 조회
- 만료 여부 확인
- 이미 인증 완료된 코드인지 확인
- 코드 일치 여부 확인
- 성공 시 `verificationToken` 생성 및 저장
- `verified=true` 처리

주요 에러:

| 상황 | ErrorCode |
| --- | --- |
| 인증 요청 값 오류 | `AUTH_400` |
| 코드 불일치/만료 | `AUTH_401` |

#### 회원가입

해야 할 일:

- 이메일 중복 확인
- `SignupUserType`은 `STUDENT`, `OWNER`만 허용
- 현재 시연 버전은 비밀번호 원문 저장. 운영 전 해싱 필요
- `UserEntity` 저장
- `isEmailVerified=false`로 저장
- 로그인 성공 시 accessToken/refreshToken 반환

주의:

- 일반 회원가입에서 `ADMIN`을 만들면 안 된다.
- 학생이면 `collegeId`, `departmentId`가 필요할 수 있다.
- 사장님이면 `storeId`가 필요할 수 있다.
- 이 조건을 강제할지, nullable 허용 후 나중에 보완할지는 API 명세 기준으로 맞춘다.

주요 에러:

| 상황 | ErrorCode |
| --- | --- |
| 인증 토큰 오류 | `AUTH_400` |
| 이미 가입된 이메일 | `USER_409` |

#### 로그인

해야 할 일:

- 이메일로 사용자 조회
- 비밀번호 검증
- accessToken/refreshToken 반환
- user 정보 반환

주의:

- 현재 JWT/Security 의존성이 없으면 토큰 생성은 임시 구현이 될 수 있다.
- JWT 담당이 따로 있다면 토큰 발급 인터페이스만 먼저 분리해도 된다.

### Auth 완료 기준

- 더미 응답 제거
- Repository 기반으로 동작
- 중복 이메일 처리
- 인증 코드 만료/재발송 제한 처리
- 회원가입에서 `ADMIN` 생성 불가
- `.\gradlew.bat test` 통과

## 4. B 파트: Category + Store 조회 구현

담당 파일:

```text
src/main/kotlin/com/likelion/category/*
src/main/kotlin/com/likelion/store/*
src/main/kotlin/com/likelion/domain/category/*
src/main/kotlin/com/likelion/domain/store/*
src/main/kotlin/com/likelion/domain/menu/*
src/main/kotlin/com/likelion/domain/benefit/*
src/main/kotlin/com/likelion/domain/favorite/*
```

### 구현할 endpoint

| Method | Endpoint | 구현 내용 |
| --- | --- | --- |
| GET | `/api/v1/colleges` | 단과대 목록 조회 |
| GET | `/api/v1/departments` | 학과 목록 조회, collegeId 필터 |
| GET | `/api/v1/stores` | 매장 목록 조회, 필터/페이지네이션 |
| GET | `/api/v1/stores/map` | 지도용 매장 목록 조회 |
| GET | `/api/v1/stores/search` | 검색 자동완성 |
| GET | `/api/v1/stores/{storeId}/summary` | 바텀시트용 매장 요약 |
| GET | `/api/v1/stores/{storeId}` | 매장 상세 |

### Category 구현 상세

해야 할 일:

- `CollegeRepository`, `DepartmentRepository` 사용
- 단과대는 이름순 또는 id순 정렬 기준 정하기
- `collegeId`가 있으면 해당 단과대 학과만 조회
- 존재하지 않는 `collegeId`를 빈 배열로 줄지 `COMMON_400`으로 줄지 명세 기준 확인

추천:

- 필터 옵션 API라면 존재하지 않는 `collegeId`는 빈 배열보다 `COMMON_400`이 더 명확하다.

### Store 목록 구현

`GET /api/v1/stores`

Query:

```text
collegeId?
departmentId?
keyword?
favoriteOnly?
page=0
size=20
```

해야 할 일:

- `isActive=true` 매장만 조회
- `keyword`가 있으면 매장명 검색
- `collegeId`, `departmentId`가 있으면 혜택 대상 기준으로 필터
- `page`, `size` 페이지네이션 적용
- 응답은 `PageResponse<StoreListItemResponse>`

주의:

- `favoriteOnly=true`이면 현재 로그인 사용자가 필요하다.
- 현재 인증은 mock이므로 `CurrentUserProvider`를 주입해서 사용한다.
- 비로그인 상태를 코드에서 아직 구분할 수 없다면 TODO를 남기고 mock user 기준으로 구현한다.

### 지도용 매장 조회

`GET /api/v1/stores/map`

해야 할 일:

- `isActive=true` 매장만 조회
- 좌표가 있으면 반경 필터 적용 가능
- 좌표 거리 계산이 복잡하면 1차 구현은 전체 active 매장을 내려주고 TODO로 남긴다.
- 응답은 `MapStoresResponse(stores = [...])`

주의:

- `latitude`, `longitude`, `radiusMeters` 중 일부만 들어온 경우 정책을 정해야 한다.
- 일반적으로 좌표 필터는 위도/경도가 둘 다 있을 때만 적용한다.

### 검색 자동완성

`GET /api/v1/stores/search`

해야 할 일:

- `keyword` 필수
- `limit` 기본값 10
- `isActive=true` 매장만 검색
- 결과 없으면 `suggestions=[]`

주의:

- 빈 문자열 keyword는 `COMMON_400` 처리 권장

### 매장 요약/상세

해야 할 일:

- 존재하지 않는 매장은 `STORE_404`
- 비활성/삭제 매장은 일반 조회에서 제외
- 상세 응답에는 메뉴와 혜택 목록 포함
- 메뉴는 `displayOrder` 기준 정렬
- 혜택은 활성 혜택만 내려줄지 전체 내려줄지 명세 기준 확인. 일반 사용자는 활성 혜택만 권장

### Store 완료 기준

- 더미 응답 제거
- Category/Store/Menu/Benefit/Favorite Repository 사용
- 매장 목록 페이지네이션 동작
- `STORE_404` 처리
- `isActive=false`, `deletedAt` 매장 일반 조회 제외
- `.\gradlew.bat test` 통과

## 5. C 파트: Favorite + MyPage 구현

담당 파일:

```text
src/main/kotlin/com/likelion/favorite/*
src/main/kotlin/com/likelion/mypage/*
src/main/kotlin/com/likelion/domain/favorite/*
src/main/kotlin/com/likelion/domain/user/*
src/main/kotlin/com/likelion/domain/store/*
src/main/kotlin/com/likelion/domain/benefitusage/*
src/main/kotlin/com/likelion/common/auth/*
```

### 구현할 endpoint

| Method | Endpoint | 구현 내용 |
| --- | --- | --- |
| POST | `/api/v1/stores/{storeId}/favorite` | 즐겨찾기 추가 |
| DELETE | `/api/v1/stores/{storeId}/favorite` | 즐겨찾기 해제 |
| GET | `/api/v1/me/favorites` | 내 즐겨찾기 목록 |
| GET | `/api/v1/me` | 마이페이지 조회 |
| GET | `/api/v1/me/benefit-usages` | 내 혜택 사용 내역 |

### Favorite 구현 상세

#### 즐겨찾기 추가

해야 할 일:

- `CurrentUserProvider.currentUserId()`로 사용자 ID 조회
- storeId 매장 존재 확인
- 비활성/삭제 매장은 즐겨찾기 불가
- 이미 즐겨찾기한 경우 `FAVORITE_409`
- `FavoriteEntity` 저장

주의:

- 사용자 ID 하드코딩 금지
- `STORE_404`와 `FAVORITE_409`를 구분한다.

#### 즐겨찾기 해제

정책:

- 멱등 처리
- 이미 즐겨찾기가 없어도 성공
- 매장 자체가 없으면 `STORE_404`

해야 할 일:

- 현재 사용자 ID 조회
- 매장 존재 확인
- favorite이 있으면 삭제
- 없으면 그대로 성공 응답

응답:

```json
{
  "storeId": 1,
  "isFavorite": false
}
```

#### 내 즐겨찾기 목록

해야 할 일:

- 현재 사용자 ID 기준 조회
- 페이지네이션 적용
- `StoreListItemResponse` 형태로 반환
- 삭제/비활성 매장은 제외

### MyPage 구현 상세

#### 마이페이지 조회

해야 할 일:

- 현재 사용자 ID 기준 사용자 조회
- 없으면 `USER_404`
- 단과대/학과 정보 포함
- favoriteCount 계산
- benefitUsageCount 계산

주의:

- 응답 DTO가 명세와 완전히 같은지 확인한다.
- 현재 코드가 `collegeName`, `departmentName` 형태라면 명세의 object 구조와 맞출지 팀장이 결정해야 한다.

#### 혜택 사용 내역 조회

해야 할 일:

- 현재 사용자 ID 기준 조회
- `page`, `size` 적용
- 최신 사용 순 정렬 권장
- `PageResponse<BenefitUsageResponse>` 반환

### Favorite/MyPage 완료 기준

- 더미 응답 제거
- `CurrentUserProvider` 사용
- 즐겨찾기 중복/멱등 정책 구현
- 마이페이지 count 실제 계산
- 혜택 사용 내역 페이지네이션 동작
- `.\gradlew.bat test` 통과

## 6. 서로 충돌나지 않게 지킬 것

### 공통 파일 수정 금지에 가까운 파일

아래 파일은 여러 파트가 동시에 만지면 충돌 가능성이 크다. 수정 전 공유한다.

```text
common/ApiResponse.kt
common/ApiException.kt
common/ErrorCode.kt
common/GlobalExceptionHandler.kt
common/auth/*
domain/common/BaseTimeEntity.kt
src/main/resources/db/migration/*
```

### Entity 변경 규칙

담당 파트에서 필요한 Repository query 추가는 가능하다.

하지만 Entity 컬럼 추가/삭제/타입 변경은 DB migration과 연결되므로 혼자 바꾸지 않는다.

### Migration 번호

DB 변경이 필요하면 먼저 팀에 번호를 예약한다.

예:

```text
V2__add_store_category.sql
V3__add_user_refresh_token.sql
```

### QR/Admin 건드리지 않기

이번 범위에서는 QR/Admin 구현을 하지 않는다.

단, Store/Favorite/MyPage 구현 중 QR/Admin Entity가 필요하다면 읽기만 하고 구조 변경은 공유 후 진행한다.

## 7. PR 또는 작업 완료 체크리스트

각 담당자는 작업 완료 전에 확인한다.

```text
[ ] 더미 응답 제거 또는 남긴 이유 TODO 명시
[ ] API_SPEC_COMPLETED.md와 request/response 필드명 일치
[ ] Repository 기반 조회/저장 구현
[ ] 필요한 ErrorCode 사용
[ ] CurrentUserProvider 사용, userId 하드코딩 없음
[ ] Entity 변경 시 migration 포함
[ ] ./gradlew.bat test 통과
```

## 8. 추천 작업 순서

```text
1. A(Auth)가 User/EmailVerification 기본 기능 구현
2. B(Category)가 단과대/학과 조회를 먼저 구현
3. B(Store)가 매장 조회를 구현
4. C(Favorite)가 CurrentUserProvider 기준으로 즐겨찾기 구현
5. C(MyPage)가 사용자/즐겨찾기/혜택 사용 내역을 연결
6. 이후 Security/JWT 담당이 MockCurrentUserProvider를 교체
```

Auth가 완전히 끝나기 전에도 B/C는 `MockCurrentUserProvider` 기준으로 개발할 수 있다.
