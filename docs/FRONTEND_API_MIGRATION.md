# 프론트엔드 API 연동 마이그레이션 가이드


이 문서는 초기 기능/API 명세가 아니라 **현재 백엔드 코드에서 실제 구현된 계약**을 기준으로 한다.

프론트엔드 프로젝트에서 CLI 코딩 에이전트에 이 문서 전체를 제공하고, 아래 `CLI 작업 지시문`을 실행하면 된다.

## CLI 작업 지시문

```text
이 문서를 현재 프론트엔드 프로젝트의 백엔드 API 계약으로 사용해 주세요.

1. 프로젝트 전체에서 기존 API 경로, query parameter, request/response 타입과 필드 사용처를 검색하세요.
2. 아래 "최종 API 계약"에 맞게 API client, TypeScript 타입, hooks, store, 화면의 데이터 접근 코드를 수정하세요.
3. snake_case 필드는 모두 이 문서의 camelCase 필드로 변경하세요.
4. 모든 응답은 ApiResponse<T> 래퍼의 data 내부 값을 사용하도록 맞추세요.
5. QR, Admin, 업종 카테고리 API는 새로 구현하거나 호출하지 마세요.
6. 기존 UI 구조와 디자인은 변경하지 말고 API 연동 코드만 최소 범위로 수정하세요.
7. 수정 후 프로젝트에서 사용하는 lint, typecheck, test, build 명령을 확인해 가능한 검증을 모두 실행하세요.
8. 추측으로 필드를 만들지 말고 안전한 fallback으로 처리하세요.
9. 완료 후 변경 파일, 변경된 API 계약, 남은 수동 확인 사항을 요약하세요.
```

## 공통 계약

Base URL:

```text
/api/v1
```

JSON 이름 규칙:

```text
camelCase
```

모든 성공 응답:

```ts
export interface ApiResponse<T> {
  success: boolean;
  status: number;
  code: string;
  message: string | null;
  data: T;
}
```

모든 실패 응답:

```ts
export interface ApiErrorResponse {
  success: false;
  status: number;
  code: string;
  message: string;
  data: null;
}
```

페이지 응답:

```ts
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
}
```

## 초기 명세에서 변경된 주요 이름

| 초기 이름 | 최종 이름 |
| --- | --- |
| `store_id` | `storeId` |
| `user_id` | `userId` |
| `college_id` | `collegeId` |
| `department_id` | `departmentId` |
| `favorite_only` | `favoriteOnly` |
| `thumbnail_url` | `thumbnailUrl` |
| `is_favorite` | `isFavorite` |
| `is_school_wide` | `isSchoolWide` |
| `is_active` | `isActive` |
| `image_url` | `imageUrl` |
| `is_representative` | `isRepresentative` |
| `display_order` | `displayOrder` |
| `total_elements` | `totalElements` |
| `total_pages` | `totalPages` |
| `has_next` | `hasNext` |
| `access_token` | `accessToken` |
| `user_type` | `userType` |
| `is_email_verified` | `isEmailVerified` |
| `benefit_title` | `benefitTitle` |
| `used_at` | `usedAt` |

## 최종 API 계약

### Auth

#### 이메일 인증 코드 발송

```http
POST /api/v1/auth/email/send-code
Content-Type: application/json
```

```ts
interface SendEmailCodeRequest {
  email: string;
}

interface SendEmailCodeResponse {
  email: string;
  expiresInSeconds: number;
  resendAvailableInSeconds: number;
}
```

#### 이메일 인증 코드 확인

```http
POST /api/v1/auth/email/verify
Content-Type: application/json
```

```ts
interface VerifyEmailRequest {
  email: string;
  code: string;
}

interface VerifyEmailResponse {
  email: string;
  verificationToken: string;
}
```

현재 시연 회원가입은 `verificationToken`을 받지 않는다.

#### 회원가입

```http
POST /api/v1/auth/signup
Content-Type: application/json
```

```ts
type SignupUserType = "STUDENT" | "OWNER";

interface SignupRequest {
  email: string;
  password: string;
  name: string;
  userType: SignupUserType;
  collegeId: number | null;
  departmentId: number | null;
  storeId: number | null;
}

interface SignupResponse {
  userId: number;
  userType: SignupUserType;
}
```

성공 HTTP status는 `201`이다. 회원가입 응답에 토큰은 없다. 회원가입 후 로그인 API를 호출한다.

#### 로그인

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```ts
interface LoginRequest {
  email: string;
  password: string;
}

type UserType = "STUDENT" | "OWNER" | "ADMIN";

interface LoginResponse {
  accessToken: string;
  expiresInSeconds: number;
  user: {
    userId: number;
    email: string;
    name: string;
    userType: UserType;
    isEmailVerified: boolean;
  };
}
```

`accessToken`은 JWT다. 즐겨찾기와 마이페이지 요청에는 다음 헤더를 추가한다.

```http
Authorization: Bearer {accessToken}
```

기존 계정도 같은 로그인 요청을 사용하며, 서버가 최초 로그인 시 비밀번호 저장 형식을 BCrypt로 전환한다.

### Category

#### 단과대 목록

```http
GET /api/v1/colleges
```

```ts
interface CollegeListResponse {
  colleges: Array<{
    collegeId: number;
    name: string;
  }>;
}
```

초기 명세처럼 `data`가 배열인 것이 아니라 `data.colleges`를 사용한다.

#### 학과 목록

```http
GET /api/v1/departments?collegeId={collegeId}
```

`collegeId`는 선택값이다. 생략하면 전체 학과를 반환한다.

```ts
interface DepartmentListResponse {
  departments: Array<{
    departmentId: number;
    collegeId: number;
    name: string;
  }>;
}
```

### Store

#### 매장 목록

```http
GET /api/v1/stores
```

선택 Query:

```ts
interface StoreListQuery {
  collegeId?: number;
  departmentId?: number;
  keyword?: string;
  favoriteOnly?: boolean;
  page?: number; // 기본값 0
  size?: number; // 기본값 20
}
```

```ts
interface StoreListItem {
  storeId: number;
  name: string;
  thumbnailUrl: string | null;
  address: string;
  description: string;
  isFavorite: boolean;
}

type StoreListResponse = PageResponse<StoreListItem>;
```

#### 지도용 매장 목록

```http
GET /api/v1/stores/map
```

선택 Query:

```ts
interface MapStoresQuery {
  latitude?: number;
  longitude?: number;
  radiusMeters?: number;
  collegeId?: number;
  departmentId?: number;
}
```

`latitude`, `longitude`, `radiusMeters`를 사용할 때는 세 값을 모두 전달해야 한다.

```ts
interface MapStoresResponse {
  stores: Array<{
    storeId: number;
    name: string;
    thumbnailUrl: string | null;
    address: string;
    latitude: number;
    longitude: number;
    categories: string[];
  }>;
}
```

업종 카테고리는 구현하지 않으므로 `categories`는 현재 항상 빈 배열이다.

#### 검색 자동완성

```http
GET /api/v1/stores/search?keyword={keyword}&limit={limit}
```

- `keyword`: 필수, 빈 문자열 불가
- `limit`: 선택, 기본값 10

```ts
interface StoreAutocompleteListResponse {
  suggestions: Array<{
    storeId: number;
    name: string;
    address: string;
  }>;
}
```

초기 명세의 `data.stores`가 아니라 `data.suggestions`를 사용한다. `thumbnailUrl`은 없고 `address`가 있다.

#### 매장 요약

```http
GET /api/v1/stores/{storeId}/summary
```

```ts
interface StoreSummaryResponse {
  storeId: number;
  name: string;
  thumbnailUrl: string | null;
  address: string;
  description: string;
  isFavorite: boolean;
}
```

#### 매장 상세

```http
GET /api/v1/stores/{storeId}
```

```ts
interface StoreDetailResponse {
  storeId: number;
  name: string;
  address: string;
  location: string | null;
  contact: string | null;
  thumbnailUrl: string | null;
  description: string;
  isSchoolWide: boolean;
  benefits: Benefit[];
  menus: Menu[];
  isFavorite: boolean;
}

interface Benefit {
  benefitId: number;
  title: string;
  description: string;
  startDate: string; // YYYY-MM-DD
  endDate: string;   // YYYY-MM-DD
  isActive: boolean;
}

interface Menu {
  menuId: number;
  name: string;
  imageUrl: string | null;
  isRepresentative: boolean;
  displayOrder: number;
}
```

초기 명세의 `qrToken`, `qrTokenIssuedAt`, `createdAt`은 현재 응답에 없다.

### Favorite

#### 즐겨찾기 추가

```http
POST /api/v1/stores/{storeId}/favorite
```

성공 HTTP status는 `201`이다.

#### 즐겨찾기 해제

```http
DELETE /api/v1/stores/{storeId}/favorite
```

두 API의 응답:

```ts
interface FavoriteResultResponse {
  storeId: number;
  isFavorite: boolean;
}
```

#### 내 즐겨찾기 목록

```http
GET /api/v1/me/favorites?page={page}&size={size}
```

```ts
type FavoriteListResponse = PageResponse<StoreListItem>;
```

로그인 사용자는 현재 백엔드의 mock 사용자로 처리된다.

### MyPage

#### 마이페이지

```http
GET /api/v1/me
```

```ts
interface MyPageResponse {
  userId: number;
  email: string;
  name: string;
  userType: UserType;
  college: {
    collegeId: number;
    name: string;
  } | null;
  department: {
    departmentId: number;
    name: string;
  } | null;
  favoriteCount: number;
  benefitUsageCount: number;
}
```

#### 내 혜택 사용 내역

```http
GET /api/v1/me/benefit-usages?page={page}&size={size}
```

```ts
interface BenefitUsage {
  usageId: number;
  storeId: number;
  storeName: string;
  benefitTitle: string;
  usedAt: string; // ISO-8601 LocalDateTime
}

type BenefitUsageListResponse = PageResponse<BenefitUsage>;
```

## 프론트에서 제거하거나 호출하지 않을 항목

다음 기능은 프로젝트 구현 범위가 아니다.

- `/api/v1/qr/**`
- `/api/v1/admin/**`
- 업종 카테고리 생성·조회·필터
- 역할별 세부 권한 처리
- QR 카메라 및 QR 인증
- 운영자 마이페이지와 매장 관리

## 프론트 마이그레이션 완료 조건

```text
[ ] API 경로가 이 문서와 일치한다.
[ ] query parameter가 camelCase다.
[ ] request/response 타입이 이 문서와 일치한다.
[ ] 응답 값을 response.data.data 또는 프로젝트 API wrapper의 동등한 방식으로 읽는다.
[ ] data.colleges, data.departments, data.stores, data.suggestions 구조를 구분한다.
[ ] 회원가입 요청에서 verificationToken을 제거했다.
[ ] 회원가입 응답에서 토큰을 읽지 않는다.
[ ] 로그인 응답의 accessToken을 저장하고 보호 API에 Bearer 헤더로 전달한다.
[ ] 로그인 응답에서 refreshToken을 읽지 않고 expiresInSeconds를 사용한다.
[ ] 검색 자동완성에서 suggestions를 사용한다.
[ ] Store 상세에서 존재하지 않는 qrToken/createdAt 필드를 사용하지 않는다.
[ ] nullable 이미지·소속 정보에 fallback 처리가 있다.
[ ] QR/Admin/업종 카테고리 API 호출이 없다.
[ ] lint/typecheck/test/build가 통과한다.
```
