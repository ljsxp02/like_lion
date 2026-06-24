# Like Lion DB Schema

이 문서는 팀 공통 DB 기준이다. 로컬 개발과 RDS 배포 모두 `src/main/resources/db/migration/V1__init_schema.sql`을 기준으로 생성한다.

## 운영 기준

- 기본 DB 대상은 MySQL 호환 DB다.
- 로컬 기본 실행은 H2 MySQL mode를 사용한다.
- JPA는 `ddl-auto: validate`만 사용한다. 테이블 생성/변경은 Flyway migration으로만 한다.
- 컬럼명은 DB에서는 `snake_case`, Kotlin/Jackson DTO는 `camelCase`를 사용한다.
- 삭제 가능한 주요 리소스는 물리 삭제보다 soft delete를 우선한다.

## 핵심 테이블

| 테이블 | 역할 |
| --- | --- |
| `users` | 학생, 사장님, 관리자 계정 |
| `colleges` | 단과대 |
| `departments` | 학과 |
| `stores` | 제휴 매장 |
| `menus` | 매장 메뉴 |
| `benefits` | 매장 혜택 |
| `benefit_target_colleges` | 혜택과 대상 단과대를 연결하는 중간 테이블 |
| `benefit_target_departments` | 혜택과 대상 학과를 연결하는 중간 테이블 |
| `favorites` | 사용자 즐겨찾기 |
| `qr_tokens` | 매장 QR 토큰 |
| `benefit_usages` | 혜택 사용 이력 |
| `email_verification_codes` | 이메일 인증 코드 |

## 팀 작업 규칙

- Entity/Repository 이름과 테이블명은 이미 추가된 골격을 따른다.
- 혜택 대상 단과대/학과는 별도 Entity를 두지 않고 `BenefitEntity.targetColleges`, `BenefitEntity.targetDepartments`로 다룬다.
- 새 컬럼이 필요하면 기존 `V1__init_schema.sql`을 수정하지 말고 `V2__...sql`을 추가한다.
- 서비스 로직에서 예외는 문자열을 직접 만들지 말고 `ErrorCode`와 `ApiException`을 사용한다.
- 로그인 사용자 ID가 필요하면 직접 mock 값을 쓰지 말고 `CurrentUserProvider`를 주입한다.
- `CurrentUserProvider`는 검증된 JWT의 SecurityContext 사용자 정보를 반환한다.

## 관계 요약

- `departments.college_id -> colleges.id`
- `users.college_id -> colleges.id`
- `users.department_id -> departments.id`
- `users.store_id -> stores.id`
- `menus.store_id -> stores.id`
- `benefits.store_id -> stores.id`
- `favorites.user_id -> users.id`
- `favorites.store_id -> stores.id`
- `qr_tokens.store_id -> stores.id`
- `benefit_usages.user_id -> users.id`
- `benefit_usages.store_id -> stores.id`
- `benefit_usages.qr_token_id -> qr_tokens.id`

## RDS 연결

RDS MySQL에 연결할 때는 `mysql` profile을 사용한다.

```powershell
$env:SPRING_PROFILES_ACTIVE="mysql"
$env:DB_URL="jdbc:mysql://{host}:{port}/{database}?serverTimezone=Asia/Seoul&characterEncoding=utf8"
$env:DB_USERNAME="{username}"
$env:DB_PASSWORD="{password}"
.\gradlew.bat bootRun
```

서버 시작 시 Flyway가 아직 적용되지 않은 migration을 순서대로 적용한다.
