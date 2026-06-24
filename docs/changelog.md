# Changelog

## 2026-06-25 00:40:00 KST - CORS 와일드카드 패턴 허용 및 관리자 계정 시드

### 변경 파일

- `src/main/kotlin/com/likelion/common/config/CorsConfig.kt`
- `src/main/resources/db/migration/V2__seed_admin.sql` (신규)
- `docs/changelog.md`

### 변경 목적

- 프론트엔드가 Vercel 프리뷰/커스텀 도메인 등 가변 origin에서 호출해도 CORS 에러가 발생하지 않도록 한다.
- ADMIN 권한이 필요한 관리자 API를 곧바로 사용할 수 있도록 초기 관리자 계정을 자동 생성한다.

### 구현 내용

- `CorsConfig`를 `allowedOrigins` → `allowedOriginPatterns`로 변경하여 와일드카드(`*.vercel.app`, `*.likelion.uk`) origin을 허용한다. 정확 origin도 그대로 매칭된다.
- 허용 목록은 `CORS_ALLOWED_ORIGINS` 환경변수로 주입한다. 운영 배포 예시: `https://*.vercel.app,https://likelion.uk,http://likelion.uk,https://*.likelion.uk,http://localhost:5173,http://localhost:*,http://127.0.0.1:*`.
- `V2__seed_admin.sql` 추가: `admin@likelion.uk` / `Admin1234!` (userType=ADMIN) 계정을 시드한다. 평문 비밀번호는 최초 로그인 시 `AuthService`가 평문 일치 확인 후 BCrypt로 자동 업그레이드한다. 매장 데이터는 시드하지 않고 관리자 API로 등록한다.

### 배포 메모 (임시)

- 신규 서버(192.168.0.34)에서 H2 인메모리 → MySQL 8.0 컨테이너로 전환(영구 볼륨 `likelion-mysql-data`), 백엔드는 `mysql` 프로파일로 구동한다.
- 공개 도메인 `be.likelion.uk`는 cloudflared(`--network host`) 터널이 `localhost:8080`으로 포워딩한다.
- 매장 12개(광운대 인근 식당)는 관리자 API로 등록했다. MySQL이라 재시작에도 유지된다.
- 프론트 `fe.likelion.uk`(Vercel)의 직접 라우트 404는 프론트 레포의 `vercel.json` SPA 폴백(`/(.*) → /index.html`) 추가로 해결한다(프론트 영역).

### 실행한 검증과 결과

- `be.likelion.uk` preflight/GET 200, `Access-Control-Allow-Origin`이 호출 origin과 정확 매칭(`team1-fe-1m1a.vercel.app`, 임의 `*.vercel.app` 프리뷰, `fe.likelion.uk`, `localhost:5173`). `evil.com`은 403 차단 확인.
- `admin@likelion.uk` 로그인 200(JWT `userType=ADMIN`), 관리자 API로 매장 등록 201, 일반유저 토큰은 403(`AUTH_004`). 매장 목록 12개 조회.

## 2026-06-24 23:05:00 KST - Admin/QR 스텁 제거 및 실제 저장 로직 구현

### 변경 파일

- `src/main/kotlin/com/likelion/admin/AdminService.kt`
- `src/main/kotlin/com/likelion/qr/QrService.kt`
- `src/main/kotlin/com/likelion/domain/qr/QrTokenRepository.kt`
- `src/test/kotlin/com/likelion/AdminQrIntegrationTests.kt`
- `src/test/kotlin/com/likelion/AdminWebIntegrationTests.kt`
- `docs/API_SPEC_COMPLETED.md`
- `docs/API_SPEC_COMPLETED.csv`
- `docs/PROJECT_ENDPOINT_OVERVIEW.md`
- `docs/changelog.md`

### 변경 목적

- 관리자 매장 등록 API가 실제 저장 없이 하드코딩 응답을 반환하던 문제를 제거한다.
- Admin/QR 계열 API가 실제 Repository 기반으로 매장, 메뉴, 혜택, QR 토큰, 혜택 사용 내역을 저장하고 조회하도록 한다.

### 구현 내용

- `AdminService`에서 `ADMIN` 사용자 유형만 매장/메뉴/혜택 관리와 QR 재발급을 수행하도록 검증한다.
- 매장 등록, 수정, 비활성화가 `StoreRepository`에 실제 반영되도록 했다.
- 메뉴 등록/수정과 혜택 등록/수정이 각각 `MenuRepository`, `BenefitRepository`에 실제 반영되도록 했다.
- QR 재발급 시 기존 활성 QR 토큰을 비활성화하고 새 UUID 토큰을 저장하도록 했다.
- `QrService`에서 활성 QR 토큰을 검증하고, 중복 사용을 막은 뒤 `BenefitUsageEntity`를 저장하도록 했다.
- Admin/QR 통합 테스트를 추가해 저장, 수정, 권한 거부, QR 재발급, QR 인증, 중복 인증, 404 오류를 검증했다.
- Admin Web 통합 테스트를 추가해 `POST /api/v1/admin/stores`가 관리자 JWT로 실제 매장을 저장하고 일반 조회 API에서 조회되는지 검증했다.
- API 명세와 프로젝트 상태 문서의 Admin/QR “추후 개발 예정” 문구를 구현 완료 상태로 갱신했다.

### 실행한 검증 명령과 결과

- `sh ./gradlew test --tests com.likelion.AdminQrIntegrationTests`
- 결과: 성공.
- `sh ./gradlew test`
- 결과: 성공.
- `sh ./gradlew test --tests com.likelion.AdminWebIntegrationTests`
- 결과: 성공.

## 2026-06-20 05:59:28 KST - Favorite/MyPage 실제 로직 1차 구현

### 변경 파일

- `src/main/kotlin/com/likelion/domain/store/StoreRepository.kt`
- `src/main/kotlin/com/likelion/domain/favorite/FavoriteRepository.kt`
- `src/main/kotlin/com/likelion/domain/benefitusage/BenefitUsageRepository.kt`
- `src/main/kotlin/com/likelion/domain/benefit/BenefitRepository.kt`
- `src/main/kotlin/com/likelion/favorite/FavoriteService.kt`
- `src/main/kotlin/com/likelion/mypage/MyPageService.kt`
- `src/test/kotlin/com/likelion/FavoriteMyPageIntegrationTests.kt`
- `docs/changelog.md`

### 변경 목적

- Favorite/MyPage 담당 범위의 더미 응답을 제거하고, API 문서와 팀 구현 가이드에 맞춰 실제 Repository 기반 로직으로 전환한다.
- Auth 모듈 구현 전까지는 `CurrentUserProvider`를 통해 현재 사용자 ID를 주입받는 구조를 유지한다.
- DB schema와 migration은 변경하지 않고, 현재 Entity/Repository 구조 안에서 구현한다.

### 구현 내용

- Favorite Repository에 현재 사용자 즐겨찾기 중 활성 상태이고 삭제되지 않은 매장만 조회/카운트하는 JPQL query를 추가했다.
- Store Repository에 활성 상태이고 `deletedAt`이 없는 매장 단건 조회 메서드를 추가했다.
- BenefitUsage Repository에 사용자별 최신순 페이지 조회와 count 메서드를 추가했다.
- Benefit Repository에 현재 날짜 기준 사용 가능한 첫 번째 활성 혜택 조회 메서드를 추가했다.
- `FavoriteService`에서 즐겨찾기 추가, 중복 검증, 멱등 삭제, 내 즐겨찾기 페이지 조회를 실제 DB 로직으로 구현했다.
- `MyPageService`에서 현재 사용자 조회, 단과대/학과 응답 매핑, 즐겨찾기/혜택 사용 count 계산, 혜택 사용 내역 최신순 페이지 조회를 구현했다.
- `benefit_usages`에는 `benefit_id`가 없으므로, 혜택 사용 내역의 `benefitTitle`은 매장의 현재 활성 혜택 제목을 사용하고 없으면 `"혜택 정보 없음"`으로 반환하도록 했다.
- 통합 테스트를 추가해 H2, Flyway, JPA validate 환경에서 Favorite/MyPage Service 동작을 검증하도록 했다.

### 문서/API 명세 반영 근거

- `docs/API_SPEC_COMPLETED.md`의 Favorite/MyPage endpoint 응답 구조, `STORE_404`, `FAVORITE_409`, 즐겨찾기 해제 멱등성, 페이지네이션 형식을 반영했다.
- `docs/TEAM_IMPLEMENTATION_GUIDE.md`의 `CurrentUserProvider` 사용, DB migration 미수정, Entity 관계 추가 금지, 더미 응답 제거 원칙을 반영했다.
- `docs/DB_SCHEMA.md`의 `favorites`, `benefit_usages`, `stores`, `users`, `colleges`, `departments` 관계를 기준으로 구현했다.

### 실행한 검증 명령과 결과

- 아직 실행 전. 다음 단계에서 `bash gradlew test`를 실행하고 결과를 추가 기록한다.

## 2026-06-20 06:00:18 KST - 테스트 생성자 주입 오류 수정

### 변경 파일

- `src/test/kotlin/com/likelion/FavoriteMyPageIntegrationTests.kt`
- `docs/changelog.md`

### 변경 목적

- `bash gradlew test` 실행 중 통합 테스트 클래스의 생성자 파라미터를 JUnit이 해석하지 못하는 문제를 수정한다.

### 구현 내용

- `FavoriteMyPageIntegrationTests` primary constructor에 `@Autowired`를 추가해 Spring TestContext가 Service/Repository bean을 생성자 주입하도록 했다.

### 문서/API 명세 반영 근거

- API 동작 변경은 없고, 내부 검증을 확실히 하기 위한 테스트 인프라 수정이다.

### 실행한 검증 명령과 결과

- `bash gradlew test`
- 결과: 실패.
- 실패 원인: `No ParameterResolver registered for parameter [FavoriteService favoriteService]` 오류로, 테스트 클래스 생성자에 Spring autowiring이 적용되지 않았다.
- 수정 후 재검증 예정.

## 2026-06-20 06:00:55 KST - 마이페이지 테스트 기대값 수정

### 변경 파일

- `src/test/kotlin/com/likelion/FavoriteMyPageIntegrationTests.kt`
- `docs/changelog.md`

### 변경 목적

- 마이페이지 응답 검증에서 테스트 데이터 생성 방식과 맞지 않는 이메일 기대값을 수정한다.

### 구현 내용

- 테스트 사용자는 중복 방지를 위해 `System.nanoTime()` 기반 이메일을 사용한다.
- `getMyPage()` 응답 이메일 검증을 id 기반 추정 문자열 대신 실제 저장된 `user.email`과 비교하도록 수정했다.

### 문서/API 명세 반영 근거

- API 명세는 현재 사용자 이메일을 그대로 반환해야 하므로, 테스트도 저장된 사용자 이메일을 기준으로 검증하는 것이 맞다.

### 실행한 검증 명령과 결과

- `bash gradlew test`
- 결과: 실패.
- 실패 원인: 기대값 `student-{id}@kw.ac.kr`와 실제 저장값 `student-{nanoTime}@kw.ac.kr` 불일치.
- 수정 후 재검증 예정.

## 2026-06-20 06:01:41 KST - 즐겨찾기 저장 예외 처리 범위 조정

### 변경 파일

- `src/main/kotlin/com/likelion/favorite/FavoriteService.kt`
- `docs/changelog.md`

### 변경 목적

- 즐겨찾기 저장 시 DB 제약 위반 전체를 `FAVORITE_409`로 변환하지 않도록 조정한다.

### 구현 내용

- 즐겨찾기 중복은 저장 전 `existsByUserIdAndStoreId`로 이미 검증하므로, `DataIntegrityViolationException` catch를 제거했다.
- 중복이 아닌 FK 제약 문제까지 중복 즐겨찾기로 오인하지 않도록 했다.

### 문서/API 명세 반영 근거

- `FAVORITE_409`는 API 명세상 “이미 즐겨찾기한 매장”에만 해당하므로, 중복 여부가 확인된 경우에만 해당 코드가 반환되도록 했다.

### 실행한 검증 명령과 결과

- 직전 `bash gradlew test` 결과: 성공.
- 본 수정 후 최종 재검증 예정.

## 2026-06-20 06:02:05 KST - 최종 내부 검증 완료

### 변경 파일

- `docs/changelog.md`

### 변경 목적

- Favorite/MyPage 구현과 테스트 수정 후 최종 검증 결과를 기록한다.

### 구현 내용

- 코드 변경은 없고, 검증 결과만 changelog에 추가했다.

### 문서/API 명세 반영 근거

- 작업 완료 기준 중 “내부 테스트 전부 통과”와 “changelog에 검증 결과 기록” 조건을 충족하기 위한 기록이다.

### 실행한 검증 명령과 결과

- `bash gradlew test`
- 결과: 성공.
- 실행 내용: Kotlin compile, Spring context loading, Flyway migration 적용, JPA schema validate, Favorite/MyPage 통합 테스트 7개와 기존 context test 통과.

## 2026-06-20 06:05:08 KST - Favorite/MyPage 간단 QA 점검

### 변경 파일

- `docs/changelog.md`

### 변경 목적

- 구현 완료 후 API 문서와 팀 구현 가이드를 충실히 따르는지 간단 QA 결과를 기록한다.

### 구현 내용

- 코드 변경은 없고, QA 수행 결과만 changelog에 추가했다.
- Favorite/MyPage Controller의 endpoint, HTTP status, 응답 메시지, DTO 필드가 `docs/API_SPEC_COMPLETED.md`와 일치하는지 확인했다.
- `FavoriteService`, `MyPageService`, 관련 Repository에서 더미 응답, 하드코딩 사용자 ID, 비활성/삭제 매장 노출, 페이지네이션 누락 여부를 정적 점검했다.

### 문서/API 명세 반영 근거

- `POST /api/v1/stores/{storeId}/favorite`는 201 Created와 `"즐겨찾기에 추가되었습니다."` 메시지를 유지한다.
- `DELETE /api/v1/stores/{storeId}/favorite`는 멱등 삭제 정책과 `STORE_404` 정책을 따른다.
- `GET /api/v1/me/favorites`, `GET /api/v1/me`, `GET /api/v1/me/benefit-usages`는 문서의 응답 DTO와 페이지네이션 구조를 따른다.
- `CurrentUserProvider` 사용, DB migration 미수정, Auth/QR/Admin 미수정 원칙을 유지한다.

### 실행한 검증 명령과 결과

- `bash gradlew test`
- 결과: 성공. 기존 최신 빌드 기준 모든 테스트 통과.
- `git diff --check`
- 결과: 성공. 공백/패치 형식 문제 없음.
- `bash gradlew clean test`
- 결과: 성공. 캐시 없이 Kotlin compile, Spring context loading, Flyway migration, JPA schema validate, Favorite/MyPage 통합 테스트 7개와 기존 context test 통과.

## 2026-06-20 06:57:02 KST - 업로드 전 변경 범위 재점검

### 변경 파일

- `src/main/kotlin/com/likelion/common/PageRequestSupport.kt`
- `src/main/kotlin/com/likelion/domain/benefit/BenefitTitleResolver.kt`
- `src/main/kotlin/com/likelion/domain/benefit/BenefitRepository.kt`
- `src/main/kotlin/com/likelion/favorite/FavoriteService.kt`
- `src/main/kotlin/com/likelion/mypage/MyPageService.kt`
- `docs/changelog.md`

### 변경 목적

- 브랜치 업로드 전, Favorite/MyPage 구현에 실제로 필요한 신규 파일이 모두 변경 범위와 changelog에 기록되어 있는지 확인한다.

### 구현 내용

- `PageRequestSupport.kt`는 page/size 검증을 공통화해 음수 page 또는 0 이하 size가 `COMMON_400`으로 처리되게 한다.
- `BenefitTitleResolver.kt`는 Favorite 목록 description과 MyPage 혜택 사용 내역의 `benefitTitle` 조회 정책을 공통화한다.
- `BenefitRepository.findActiveBenefits(...)`는 여러 매장의 현재 활성 혜택을 한 번에 조회해 목록 조회 시 N+1 쿼리를 줄인다.
- `FavoriteService`와 `MyPageService`는 위 공통 컴포넌트를 사용하도록 정리되어 있다.

### 문서/API 명세 반영 근거

- API 명세의 페이지네이션 입력값 오류는 `COMMON_400` 계열로 처리하는 것이 맞다.
- `benefit_usages`에 `benefit_id`가 없어 사용 당시 혜택을 특정할 수 없으므로, 기존 결정대로 매장의 현재 활성 혜택 제목과 fallback을 일관되게 사용한다.

### 실행한 검증 명령과 결과

- `git status -sb`
- 결과: 현재 브랜치 `feature/favorite-mypage`, 업로드 대상 변경 파일 확인.
- `bash gradlew clean test`
- 결과: 성공. Kotlin compile, Spring context loading, Flyway migration, JPA schema validate, Favorite/MyPage 통합 테스트 7개와 기존 context test 통과.
- `git diff --check`
- 결과: 성공. 공백/패치 형식 문제 없음.

## 2026-06-20 06:30:00 KST - Favorite/MyPage 코드 리뷰 개선 적용

### 변경 파일

- `src/main/kotlin/com/likelion/common/PageRequestSupport.kt` (신규)
- `src/main/kotlin/com/likelion/domain/benefit/BenefitTitleResolver.kt` (신규)
- `src/main/kotlin/com/likelion/domain/benefit/BenefitRepository.kt`
- `src/main/kotlin/com/likelion/domain/favorite/FavoriteRepository.kt`
- `src/main/kotlin/com/likelion/favorite/FavoriteService.kt`
- `src/main/kotlin/com/likelion/mypage/MyPageService.kt`
- `src/test/kotlin/com/likelion/FavoriteMyPageIntegrationTests.kt`
- `docs/changelog.md`

### 변경 목적

- 코드 리뷰에서 도출한 개선 사항 중 팀 합의가 필요한 항목(즐겨찾기 목록 `description` 출처)을 제외하고, Favorite/MyPage 내부 품질을 개선한다.
- 목록 조회의 N+1 쿼리를 제거하고, 두 서비스에 중복된 로직을 공통 컴포넌트로 통합한다.
- API 응답 계약과 동작은 그대로 유지하고, 테스트로 회귀를 검증한다.

### 구현 내용

- 혜택 제목 조회를 `BenefitTitleResolver`로 통합했다. Favorite 목록 `description`과 혜택 사용 내역 `benefitTitle`이 각각 중복 보유하던 "현재 활성 혜택 제목 + fallback" 로직을 한 곳으로 모았다.
- `BenefitRepository.findActiveBenefits(storeIds, today)` 배치 조회를 추가해, 목록 항목마다 혜택을 개별 조회하던 N+1을 매장 묶음 1쿼리로 변경했다.
- 혜택 사용 내역의 매장명도 `storeRepository.findAllById`로 한 번에 조회해 항목별 매장 조회 N+1을 제거했다.
- 두 서비스에 중복되던 page/size 검증을 `common.pageRequestOf`로 추출했다.
- 미사용이 된 단건 `BenefitRepository.findFirstBy...`와 각 서비스의 `currentBenefitTitle`/`pageRequest`/fallback 상수 중복을 제거했다.
- `FavoriteRepository`의 즐겨찾기 매장 조회/카운트 JPQL을 암시적 조인(comma join)에서 명시적 `join ... on`으로 정리했다.
- 통합 테스트의 즐겨찾기 해제 케이스를 멱등 성공 / 실제 삭제 / 매장 없음(`STORE_404`) 3개로 분리했다.

### 문서/API 명세 반영 근거

- 응답 DTO 필드, HTTP status, 메시지, 페이지네이션 구조를 변경하지 않아 `docs/API_SPEC_COMPLETED.md` 계약을 그대로 유지한다.
- `docs/TEAM_IMPLEMENTATION_GUIDE.md`의 "필요한 Repository query 추가 허용", "DB migration/Entity 구조 미변경", "CurrentUserProvider 사용" 원칙을 지켰다. DB 스키마와 Entity 컬럼 변경은 없다.
- 즐겨찾기 목록 `description` 출처(혜택 title vs 매장 설명)는 Store 담당과 동일 DTO(`StoreListItemResponse`)를 공유하는 합의 사항이라 이번 작업에서 의도적으로 제외했다.

### 실행한 검증 명령과 결과

- `bash gradlew clean test`
- 결과: 성공. 캐시 없이 Kotlin compile, Spring context loading, Flyway migration, JPA schema validate를 통과했다.
- 테스트: `FavoriteMyPageIntegrationTests` 9개(즐겨찾기 해제 분리로 7→9)와 기존 context test 1개, 합계 10개가 모두 통과했다(실패 0 / 에러 0).
- 명시적 `join ... on` JPQL과 배치 혜택/매장 조회가 H2 환경에서 정상 동작함을 확인했다.
