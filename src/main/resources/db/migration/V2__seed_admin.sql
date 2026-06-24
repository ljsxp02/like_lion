-- 관리자 계정 시드.
-- password_hash에 평문을 넣어도 로그인 시 AuthService가 평문 일치 확인 후 BCrypt로 자동 업그레이드한다.
-- 매장 데이터는 시드하지 않는다(관리자 API로 등록).
INSERT INTO users (email, password_hash, name, user_type, is_email_verified)
VALUES ('admin@likelion.uk', 'Admin1234!', '관리자', 'ADMIN', TRUE);
