# Like Lion API 명세서 완성본

작성일: 2026-06-18

## 1. 공통 규칙

### Base URL

```text
/api/v1
```

### 인증

인증 필요 API는 HTTP Header에 Bearer 토큰을 전달한다.

```http
Authorization: Bearer {accessToken}
```

### 공통 응답 형식

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "요청이 성공했습니다.",
  "data": {}
}
```

실패 응답:

```json
{
  "success": false,
  "status": 400,
  "code": "COMMON_400",
  "message": "요청 값이 올바르지 않습니다.",
  "data": null
}
```

### 네이밍

JSON 필드는 프론트엔드 사용 편의성을 기준으로 `camelCase`를 사용한다. 기존 초안에 있던 `store_id`, `user_type`, `thumbnail_url` 등은 응답 예시에서 `storeId`, `userType`, `thumbnailUrl`로 정리했다.

### 페이지네이션

목록 API는 다음 쿼리 파라미터와 응답 구조를 사용한다.

| 항목 | 타입 | 기본값 | 설명 |
| --- | --- | --- | --- |
| page | Integer | 0 | 0부터 시작하는 페이지 번호 |
| size | Integer | 20 | 한 페이지 데이터 개수 |

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0,
  "hasNext": false
}
```

## 2. Error Code

| Code | HTTP Status | 설명 |
| --- | ---: | --- |
| OK | 200 | 요청 성공 |
| CREATED | 201 | 리소스 생성 성공 |
| NO_CONTENT | 204 | 응답 본문 없는 성공 |
| COMMON_400 | 400 | 요청 파라미터 또는 본문 값 오류 |
| COMMON_500 | 500 | 서버 내부 오류 |
| AUTH_001 | 401 | 인증 토큰 없음 또는 유효하지 않음 |
| AUTH_003 | 403 | 접근 권한 없음 |
| AUTH_004 | 403 | 관리자 권한 없음 |
| AUTH_400 | 400 | 인증 요청 값 오류 |
| AUTH_401 | 401 | 이메일, 비밀번호 또는 인증 코드 불일치 |
| AUTH_409 | 409 | 이미 처리된 인증/가입 요청 |
| USER_404 | 404 | 사용자를 찾을 수 없음 |
| USER_409 | 409 | 이미 가입된 사용자 |
| STORE_404 | 404 | 매장을 찾을 수 없음 |
| FAVORITE_409 | 409 | 이미 즐겨찾기한 매장 |
| QR_400 | 400 | QR 토큰 형식 오류 |
| QR_404 | 404 | QR 토큰 또는 대상 매장을 찾을 수 없음 |
| QR_409 | 409 | 이미 사용 처리된 QR 인증 |
| ADMIN_STORE_404 | 404 | 관리자 대상 매장을 찾을 수 없음 |
| ADMIN_MENU_404 | 404 | 관리자 대상 메뉴를 찾을 수 없음 |
| ADMIN_BENEFIT_404 | 404 | 관리자 대상 혜택을 찾을 수 없음 |

## 3. API 목록

### 프로젝트 구현 범위 안내

이번 프로젝트에서는 다음 기능을 구현하지 않는다.

- 역할별 세부 권한 처리
- QR 토큰 검증, 중복 인증 제한, 혜택 사용 이력 저장
- 운영자 마이페이지와 Admin 매장·메뉴·혜택 관리
- 프론트엔드 지도/GPS, 바텀시트, 화면 이동, QR 카메라 화면
- 식사·카페·주류·기타 등의 매장 업종 카테고리와 업종별 필터

로그인은 JWT access token을 발급하며 즐겨찾기와 마이페이지는 JWT의 실제 사용자 ID를 기준으로 동작한다. 업종 카테고리용 DB 구조는 추가하지 않으며 지도용 매장 응답의 `categories`는 빈 배열을 반환한다.

| 도메인 | API명 | Method | Endpoint | 인증 | 상태 |
| --- | --- | --- | --- | --- | --- |
| Auth | 이메일 인증 코드 발송 | POST | `/api/v1/auth/email/send-code` | No | 명세 완료 |
| Auth | 이메일 인증 코드 확인 | POST | `/api/v1/auth/email/verify` | No | 명세 완료 |
| Auth | 회원가입 완료 | POST | `/api/v1/auth/signup` | No | 명세 완료 |
| Auth | 로그인 | POST | `/api/v1/auth/login` | No | 구현 완료 |
| Category/Filter | 단과대 목록 조회 | GET | `/api/v1/colleges` | No | 명세 완료 |
| Category/Filter | 학과 목록 조회 | GET | `/api/v1/departments` | No | 명세 완료 |
| Store | 매장 리스트 조회 | GET | `/api/v1/stores` | Optional | 명세 완료 |
| Store | 지도용 매장 목록 조회 | GET | `/api/v1/stores/map` | No | 명세 완료 |
| Store | 매장 검색 자동완성 | GET | `/api/v1/stores/search` | No | 명세 완료 |
| Store | 매장 바텀시트 정보 조회 | GET | `/api/v1/stores/{storeId}/summary` | Optional | 명세 완료 |
| Store | 매장 상세 정보 조회 | GET | `/api/v1/stores/{storeId}` | Optional | 명세 완료 |
| Favorite | 즐겨찾기 추가 | POST | `/api/v1/stores/{storeId}/favorite` | Yes | 명세 완료 |
| Favorite | 즐겨찾기 해제 | DELETE | `/api/v1/stores/{storeId}/favorite` | Yes | 명세 완료 |
| Favorite | 내 즐겨찾기 목록 조회 | GET | `/api/v1/me/favorites` | Yes | 명세 완료 |
| MyPage | 마이페이지 조회 | GET | `/api/v1/me` | Yes | 명세 완료 |
| QR/Benefit Usage | QR 인증 요청 | POST | `/api/v1/qr/verify` | Yes | 구현 완료 |
| QR/Benefit Usage | 내 혜택 사용 내역 조회 | GET | `/api/v1/me/benefit-usages` | Yes | 명세 완료 |
| Admin | 관리자 매장 등록 | POST | `/api/v1/admin/stores` | Admin | 구현 완료 |
| Admin | 관리자 매장 수정 | PATCH | `/api/v1/admin/stores/{storeId}` | Admin | 구현 완료 |
| Admin | 관리자 매장 비활성화 | DELETE | `/api/v1/admin/stores/{storeId}` | Admin | 구현 완료 |
| Admin | 관리자 QR 토큰 재발급 | POST | `/api/v1/admin/stores/{storeId}/qr/regenerate` | Admin | 구현 완료 |
| Admin | 관리자 메뉴 등록 | POST | `/api/v1/admin/stores/{storeId}/menus` | Admin | 구현 완료 |
| Admin | 관리자 메뉴 수정 | PATCH | `/api/v1/admin/menus/{menuId}` | Admin | 구현 완료 |
| Admin | 관리자 혜택 등록 | POST | `/api/v1/admin/stores/{storeId}/benefits` | Admin | 구현 완료 |
| Admin | 관리자 혜택 수정 | PATCH | `/api/v1/admin/benefits/{benefitId}` | Admin | 구현 완료 |

## 4. Endpoint 상세

### 4.1 이메일 인증 코드 발송

`POST /api/v1/auth/email/send-code`

학교 이메일로 인증 코드를 발송한다. 같은 이메일에 대한 재발송은 60초 이후 허용한다.

Request Body:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| email | String | Y | 학교 이메일 | `student@kw.ac.kr` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "인증 코드가 발송되었습니다.",
  "data": {
    "email": "student@kw.ac.kr",
    "expiresInSeconds": 300,
    "resendAvailableInSeconds": 60
  }
}
```

Errors: `AUTH_400`, `AUTH_409`, `COMMON_500`

### 4.2 이메일 인증 코드 확인

`POST /api/v1/auth/email/verify`

> 현재 시연 버전의 회원가입에서는 이메일 인증을 사용하지 않는다. 이 API는 추후 실제 메일 인증 연동을 위해 유지한다.

Request Body:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| email | String | Y | 인증 코드를 받은 이메일 | `student@kw.ac.kr` |
| code | String | Y | 6자리 인증 코드 | `123456` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "이메일 인증이 완료되었습니다.",
  "data": {
    "email": "student@kw.ac.kr",
    "verificationToken": "email-verification-token"
  }
}
```

Errors: `AUTH_400`, `AUTH_401`, `COMMON_500`

### 4.3 회원가입 완료

`POST /api/v1/auth/signup`

Request Body:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| email | String | Y | 이메일 형식의 로그인 아이디 | `student@kw.ac.kr` |
| password | String | Y | 비밀번호. 서버에서 BCrypt 해시로 저장 | `Password123!` |
| name | String | Y | 사용자 이름 | `홍길동` |
| userType | String | Y | 사용자 유형. `STUDENT`, `OWNER` | `STUDENT` |
| collegeId | Long | N | 학생 소속 단과대 ID | `1` |
| departmentId | Long | N | 학생 소속 학과 ID | `3` |
| storeId | Long | N | 사장님 계정과 연결할 매장 ID | `1` |

Success `201`:

```json
{
  "success": true,
  "status": 201,
  "code": "CREATED",
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": 1024,
    "userType": "STUDENT"
  }
}
```

회원가입 후 `/api/v1/auth/login`을 호출해 JWT accessToken을 발급받는다.

Errors: `USER_409`, `COMMON_500`

### 4.4 로그인

`POST /api/v1/auth/login`

Request Body:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| email | String | Y | 가입 이메일 | `student@kw.ac.kr` |
| password | String | Y | 비밀번호 | `Password123!` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "로그인되었습니다.",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6...",
    "expiresInSeconds": 3600,
    "user": {
      "userId": 1024,
      "email": "student@kw.ac.kr",
      "name": "홍길동",
      "userType": "STUDENT",
      "isEmailVerified": false
    }
  }
}
```

Errors: `AUTH_401`, `USER_404`, `COMMON_500`

보호 API 호출 시 로그인 응답의 accessToken을 전달한다.

```http
Authorization: Bearer {accessToken}
```

기존 평문 비밀번호 계정은 최초 로그인 성공 시 BCrypt 해시로 자동 전환된다.

### 4.5 단과대 목록 조회

`GET /api/v1/colleges`

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "단과대 목록을 조회했습니다.",
  "data": {
    "colleges": [
      {
        "collegeId": 1,
        "name": "소프트웨어융합대학"
      }
    ]
  }
}
```

Errors: `COMMON_500`

### 4.6 학과 목록 조회

`GET /api/v1/departments`

Query Parameters:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| collegeId | Long | N | 단과대 ID. 없으면 전체 조회 | `1` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "학과 목록을 조회했습니다.",
  "data": {
    "departments": [
      {
        "departmentId": 3,
        "collegeId": 1,
        "name": "컴퓨터정보공학부"
      }
    ]
  }
}
```

Errors: `COMMON_400`, `COMMON_500`

### 4.7 매장 리스트 조회

`GET /api/v1/stores`

인증 토큰이 있으면 `isFavorite`를 내려주고, 없으면 `false`로 내려준다. 단, `favoriteOnly=true`로 요청하는 경우에는 인증이 필수이며 토큰이 없거나 유효하지 않으면 `AUTH_001`을 반환한다.

Query Parameters:

| key | 타입 | 필수 | 기본값 | 설명 | 예시 |
| --- | --- | --- | --- | --- | --- |
| collegeId | Long | N | - | 단과대 필터 | `1` |
| departmentId | Long | N | - | 학과 필터 | `3` |
| keyword | String | N | - | 매장명 검색어 | `윤스` |
| favoriteOnly | Boolean | N | `false` | 즐겨찾기 매장만 조회. 인증 필요 | `true` |
| page | Integer | N | `0` | 페이지 번호 | `0` |
| size | Integer | N | `20` | 페이지 크기 | `20` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "매장 리스트를 조회했습니다.",
  "data": {
    "content": [
      {
        "storeId": 1,
        "name": "윤스쿡",
        "thumbnailUrl": "https://image.example.com/store1.jpg",
        "address": "서울 노원구 광운로 20",
        "description": "광운대생 인증 시 10% 할인",
        "isFavorite": false
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false
  }
}
```

Errors: `AUTH_001`, `COMMON_400`, `COMMON_500`

### 4.8 지도용 매장 목록 조회

`GET /api/v1/stores/map`

Query Parameters:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| latitude | Double | N | 현재 위도 | `37.6198` |
| longitude | Double | N | 현재 경도 | `127.0591` |
| radiusMeters | Integer | N | 반경 미터 | `1000` |
| collegeId | Long | N | 단과대 필터 | `1` |
| departmentId | Long | N | 학과 필터 | `3` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "지도용 매장 목록을 조회했습니다.",
  "data": {
    "stores": [
      {
        "storeId": 1,
        "name": "장수국수",
        "thumbnailUrl": "https://image.example.com/store1.jpg",
        "address": "서울 노원구 광운로 27 2층",
        "latitude": 37.6198,
        "longitude": 127.0591,
        "categories": ["음식점"]
      }
    ]
  }
}
```

Errors: `COMMON_400`, `COMMON_500`

### 4.9 매장 검색 자동완성

`GET /api/v1/stores/search`

Query Parameters:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| keyword | String | Y | 1자 이상 검색어 | `윤스` |
| limit | Integer | N | 최대 응답 개수. 기본값 10 | `10` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "검색어 자동완성 결과를 조회했습니다.",
  "data": {
    "suggestions": [
      {
        "storeId": 1,
        "name": "윤스쿡",
        "address": "서울 노원구 광운로 20"
      }
    ]
  }
}
```

검색 결과가 없으면 `suggestions: []`를 반환한다.

Errors: `COMMON_400`, `COMMON_500`

### 4.10 매장 바텀시트 정보 조회

`GET /api/v1/stores/{storeId}/summary`

Path Parameters:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| storeId | Long | Y | 매장 ID | `1` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "매장 요약 정보를 조회했습니다.",
  "data": {
    "storeId": 1,
    "name": "윤스쿡",
    "thumbnailUrl": "https://image.example.com/store1.jpg",
    "address": "서울 노원구 광운로 20",
    "description": "광운대생 인증 시 10% 할인",
    "isFavorite": false
  }
}
```

Errors: `STORE_404`, `COMMON_500`

### 4.11 매장 상세 정보 조회

`GET /api/v1/stores/{storeId}`

Path Parameters:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| storeId | Long | Y | 매장 ID | `1` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "매장 상세 정보를 조회했습니다.",
  "data": {
    "storeId": 1,
    "name": "윤스쿡",
    "address": "서울 노원구 광운로 20",
    "location": "광운대학교 정문 근처",
    "contact": "02-123-4567",
    "thumbnailUrl": "https://image.example.com/store1.jpg",
    "description": "광운대생 인증 시 10% 할인",
    "isSchoolWide": true,
    "benefits": [
      {
        "benefitId": 5,
        "title": "광운대생 10% 할인",
        "description": "학생 인증 시 전 메뉴 10% 할인",
        "startDate": "2026-03-01",
        "endDate": "2026-12-31",
        "isActive": true
      }
    ],
    "menus": [
      {
        "menuId": 10,
        "name": "아이스 아메리카노",
        "imageUrl": "https://example.com/menu/americano.png",
        "isRepresentative": true,
        "displayOrder": 1
      }
    ],
    "isFavorite": false
  }
}
```

Errors: `STORE_404`, `COMMON_500`

### 4.12 즐겨찾기 추가

`POST /api/v1/stores/{storeId}/favorite`

Path Parameters:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| storeId | Long | Y | 즐겨찾기할 매장 ID | `1` |

Success `201`:

```json
{
  "success": true,
  "status": 201,
  "code": "CREATED",
  "message": "즐겨찾기에 추가되었습니다.",
  "data": {
    "storeId": 1,
    "isFavorite": true
  }
}
```

Errors: `AUTH_001`, `STORE_404`, `FAVORITE_409`, `COMMON_500`

### 4.13 즐겨찾기 해제

`DELETE /api/v1/stores/{storeId}/favorite`

일반적인 REST DELETE 정책에 맞춰 멱등하게 처리한다. 이미 즐겨찾기 상태가 아니어도 성공으로 응답하며, 매장 자체가 없을 때만 `STORE_404`를 반환한다.

Path Parameters:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| storeId | Long | Y | 즐겨찾기 해제할 매장 ID | `1` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "즐겨찾기가 해제되었습니다.",
  "data": {
    "storeId": 1,
    "isFavorite": false
  }
}
```

Errors: `AUTH_001`, `STORE_404`, `COMMON_500`

### 4.14 내 즐겨찾기 목록 조회

`GET /api/v1/me/favorites`

Query Parameters:

| key | 타입 | 필수 | 기본값 | 설명 | 예시 |
| --- | --- | --- | --- | --- | --- |
| page | Integer | N | `0` | 페이지 번호 | `0` |
| size | Integer | N | `20` | 페이지 크기 | `20` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "즐겨찾기 목록을 조회했습니다.",
  "data": {
    "content": [
      {
        "storeId": 1,
        "name": "윤스쿡",
        "thumbnailUrl": "https://image.example.com/store1.jpg",
        "address": "서울 노원구 광운로 20",
        "description": "광운대생 인증 시 10% 할인",
        "isFavorite": true
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false
  }
}
```

Errors: `AUTH_001`, `COMMON_500`

### 4.15 마이페이지 조회

`GET /api/v1/me`

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "마이페이지를 조회했습니다.",
  "data": {
    "userId": 1024,
    "email": "student@kw.ac.kr",
    "name": "홍길동",
    "userType": "STUDENT",
    "college": {
      "collegeId": 1,
      "name": "소프트웨어융합대학"
    },
    "department": {
      "departmentId": 3,
      "name": "컴퓨터정보공학부"
    },
    "favoriteCount": 3,
    "benefitUsageCount": 5
  }
}
```

Errors: `AUTH_001`, `USER_404`, `COMMON_500`

### 4.16 QR 인증 요청

`POST /api/v1/qr/verify`

활성 QR 토큰을 검증하고, 현재 사용자와 QR 토큰 조합으로 혜택 사용 내역을 저장한다. 같은 사용자가 같은 QR 토큰으로 다시 인증하면 `QR_409`를 반환한다.

Request Body:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| qrToken | String | Y | 매장 QR 토큰 | `qr-token-example` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "QR 인증이 완료되었습니다.",
  "data": {
    "usageId": 88,
    "storeId": 1,
    "storeName": "윤스쿡",
    "verifiedAt": "2026-06-18T12:00:00"
  }
}
```

Errors: `AUTH_001`, `AUTH_003`, `QR_400`, `QR_404`, `QR_409`, `COMMON_500`

### 4.17 내 혜택 사용 내역 조회

`GET /api/v1/me/benefit-usages`

Query Parameters:

| key | 타입 | 필수 | 기본값 | 설명 | 예시 |
| --- | --- | --- | --- | --- | --- |
| page | Integer | N | `0` | 페이지 번호 | `0` |
| size | Integer | N | `20` | 페이지 크기 | `20` |

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "혜택 사용 내역을 조회했습니다.",
  "data": {
    "content": [
      {
        "usageId": 88,
        "storeId": 1,
        "storeName": "윤스쿡",
        "benefitTitle": "광운대생 10% 할인",
        "usedAt": "2026-06-18T12:00:00"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "hasNext": false
  }
}
```

Errors: `AUTH_001`, `COMMON_500`

### 4.18 관리자 매장 등록

`POST /api/v1/admin/stores`

관리자 권한 사용자가 매장을 등록한다. 등록된 매장은 일반 매장 조회 API에서 바로 조회된다.

Request Body:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| name | String | Y | 매장명 | `윤스쿡` |
| address | String | Y | 주소 | `서울 노원구 광운로 20` |
| location | String | N | 위치 설명 | `광운대학교 정문 근처` |
| contact | String | N | 연락처 | `02-123-4567` |
| thumbnailUrl | String | N | 대표 이미지 URL | `https://image.example.com/store1.jpg` |
| latitude | Double | N | 위도 | `37.6198` |
| longitude | Double | N | 경도 | `127.0591` |

Success `201`:

```json
{
  "success": true,
  "status": 201,
  "code": "CREATED",
  "message": "매장이 등록되었습니다.",
  "data": {
    "storeId": 1,
    "name": "윤스쿡",
    "address": "서울 노원구 광운로 20",
    "isActive": true
  }
}
```

Errors: `AUTH_004`, `COMMON_400`, `COMMON_500`

### 4.19 관리자 매장 수정

`PATCH /api/v1/admin/stores/{storeId}`

관리자 권한 사용자가 매장 정보를 수정한다.

Path Parameters:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| storeId | Long | Y | 수정할 매장 ID | `1` |

Request Body는 변경할 필드만 전달한다. `null`은 값 삭제 의도로 사용하지 않고 무시한다.

| key | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| name | String | N | 매장명 |
| address | String | N | 주소 |
| location | String | N | 위치 설명 |
| contact | String | N | 연락처 |
| thumbnailUrl | String | N | 대표 이미지 URL |
| latitude | Double | N | 위도 |
| longitude | Double | N | 경도 |

Success `200`: 등록 응답과 동일한 매장 요약을 반환한다.

Errors: `AUTH_004`, `ADMIN_STORE_404`, `COMMON_400`, `COMMON_500`

### 4.20 관리자 매장 비활성화

`DELETE /api/v1/admin/stores/{storeId}`

매장 삭제는 소프트 삭제로 처리한다. DB row를 물리적으로 제거하지 않고 `isActive=false`와 `deletedAt` 값을 기록해 일반 사용자 조회에서만 숨긴다. 과거 혜택 사용 내역, 즐겨찾기, QR 인증 기록과의 연결을 유지할 수 있어 매장 데이터에는 이 방식이 적합하다.

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "매장이 비활성화되었습니다.",
  "data": {
    "storeId": 1,
    "isActive": false
  }
}
```

Errors: `AUTH_004`, `ADMIN_STORE_404`, `COMMON_500`

### 4.21 관리자 QR 토큰 재발급

`POST /api/v1/admin/stores/{storeId}/qr/regenerate`

기존 QR 토큰은 즉시 무효화한다.

Success `200`:

```json
{
  "success": true,
  "status": 200,
  "code": "OK",
  "message": "QR 토큰이 재발급되었습니다.",
  "data": {
    "storeId": 1,
    "qrToken": "bdf6c9c1-d417-4bb3-b402-80d5206c7b21",
    "qrTokenIssuedAt": "2026-06-18T12:00:00"
  }
}
```

Errors: `AUTH_004`, `ADMIN_STORE_404`, `COMMON_500`

### 4.22 관리자 메뉴 등록

`POST /api/v1/admin/stores/{storeId}/menus`

관리자 권한 사용자가 매장의 메뉴를 등록한다.

Request Body:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| name | String | Y | 메뉴 이름 | `아이스 아메리카노` |
| imageUrl | String | N | 메뉴 이미지 URL | `https://example.com/menu/americano.png` |
| isRepresentative | Boolean | Y | 대표 메뉴 여부 | `true` |
| displayOrder | Integer | Y | 노출 순서 | `1` |

Success `201`:

```json
{
  "success": true,
  "status": 201,
  "code": "CREATED",
  "message": "메뉴가 등록되었습니다.",
  "data": {
    "menuId": 10,
    "storeId": 1,
    "name": "아이스 아메리카노",
    "imageUrl": "https://example.com/menu/americano.png",
    "isRepresentative": true,
    "displayOrder": 1
  }
}
```

Errors: `AUTH_004`, `ADMIN_STORE_404`, `COMMON_400`, `COMMON_500`

### 4.23 관리자 메뉴 수정

`PATCH /api/v1/admin/menus/{menuId}`

관리자 권한 사용자가 메뉴 정보를 수정한다.

Request Body는 변경할 필드만 전달한다.

| key | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| name | String | N | 메뉴 이름 |
| imageUrl | String | N | 메뉴 이미지 URL |
| isRepresentative | Boolean | N | 대표 메뉴 여부 |
| displayOrder | Integer | N | 노출 순서 |

Success `200`: 메뉴 상세를 반환한다.

Errors: `AUTH_004`, `ADMIN_MENU_404`, `COMMON_400`, `COMMON_500`

### 4.24 관리자 혜택 등록

`POST /api/v1/admin/stores/{storeId}/benefits`

한 매장에 여러 혜택 등록을 허용한다.

Request Body:

| key | 타입 | 필수 | 설명 | 예시 |
| --- | --- | --- | --- | --- |
| title | String | Y | 혜택 제목 | `광운대생 10% 할인` |
| description | String | Y | 혜택 설명 | `학생 인증 시 전 메뉴 10% 할인` |
| isSchoolWide | Boolean | Y | 전체 학교 대상 여부 | `true` |
| collegeIds | Long[] | N | 대상 단과대 ID 목록 | `[1]` |
| departmentIds | Long[] | N | 대상 학과 ID 목록 | `[3]` |
| startDate | Date | Y | 혜택 시작일 | `2026-03-01` |
| endDate | Date | Y | 혜택 종료일 | `2026-12-31` |

Success `201`:

```json
{
  "success": true,
  "status": 201,
  "code": "CREATED",
  "message": "혜택이 등록되었습니다.",
  "data": {
    "benefitId": 5,
    "storeId": 1,
    "title": "광운대생 10% 할인",
    "description": "학생 인증 시 전 메뉴 10% 할인",
    "isSchoolWide": true,
    "startDate": "2026-03-01",
    "endDate": "2026-12-31",
    "isActive": true
  }
}
```

Errors: `AUTH_004`, `ADMIN_STORE_404`, `COMMON_400`, `COMMON_500`

### 4.25 관리자 혜택 수정

`PATCH /api/v1/admin/benefits/{benefitId}`

Request Body는 변경할 필드만 전달한다. `endDate`가 지난 혜택은 조회 시 비활성으로 취급한다.

| key | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| title | String | N | 혜택 제목 |
| description | String | N | 혜택 설명 |
| isSchoolWide | Boolean | N | 전체 학교 대상 여부 |
| collegeIds | Long[] | N | 대상 단과대 ID 목록 |
| departmentIds | Long[] | N | 대상 학과 ID 목록 |
| startDate | Date | N | 혜택 시작일 |
| endDate | Date | N | 혜택 종료일 |
| isActive | Boolean | N | 수동 활성화 여부 |

Success `200`: 혜택 상세를 반환한다.

Errors: `AUTH_004`, `ADMIN_BENEFIT_404`, `COMMON_400`, `COMMON_500`

## 5. 확정 정책

다음 항목은 일반적인 API 설계 관례와 프로젝트 결정사항을 반영해 확정했다.

| 항목 | 현재 정리안 |
| --- | --- |
| JSON 필드명 | `camelCase` 통일 |
| 로그인/회원가입 토큰 발급 | 회원가입은 계정 생성만 수행하고, 로그인은 JWT accessToken과 만료 시간을 반환. refreshToken은 현재 발급하지 않음 |
| 회원가입 사용자 유형 | 일반 회원가입은 `STUDENT`, `OWNER`만 허용. `ADMIN`은 별도 내부 절차로 생성 |
| 개인 API 인증 | `/me`, 즐겨찾기, QR 인증, 혜택 사용 내역은 인증 필수 |
| Store 조회 인증 | 비로그인 허용, 토큰이 있으면 즐겨찾기 여부 포함. 단, `favoriteOnly=true`는 인증 필수 |
| QR 인증 API | 인증 필수, 활성 QR 토큰 검증 및 혜택 사용 내역 저장 |
| 관리자 API | 관리자 권한 필수, 매장/메뉴/혜택 관리 및 QR 재발급 구현 |
| 즐겨찾기 해제 멱등성 | 이미 즐겨찾기가 없어도 성공 응답. 매장이 없을 때만 `STORE_404` |
| 매장 삭제 | 소프트 삭제. DB row는 유지하고 일반 사용자 조회에서 숨김 |
| PATCH null 처리 | `null`은 무시, 값 삭제 용도로 사용하지 않음 |
| 혜택 개수 | 한 매장에 여러 혜택 허용 |
| 만료 혜택 | `endDate` 이후 조회 시 비활성 처리 |
| 이메일 인증 코드 | 유효시간 5분, 재발송 제한 60초 |
| QR 중복 인증 | 이미 사용된 토큰 또는 중복 처리 시 `QR_409` |
