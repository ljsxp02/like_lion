package com.likelion.auth

import com.likelion.domain.user.UserType
import org.springframework.stereotype.Service

@Service
class AuthService {
    fun sendEmailCode(request: SendEmailCodeRequest): SendEmailCodeResponse {
        // TODO: kw.ac.kr 도메인 검증, 발송 제한, 인증 코드 저장, 메일 발송 구현
        return SendEmailCodeResponse(
            email = request.email,
            expiresInSeconds = 300,
            resendAvailableInSeconds = 60,
        )
    }

    fun verifyEmail(request: VerifyEmailRequest): VerifyEmailResponse {
        // TODO: 인증 코드 만료/재요청 정책 확정 후 검증 구현
        return VerifyEmailResponse(
            email = request.email,
            verificationToken = "dev-verification-token",
        )
    }

    fun signup(request: SignupRequest): SignupResponse {
        // TODO: 사용자 중복 검사, 비밀번호 해싱, 학생/사장님 필드 분기 저장 구현
        return SignupResponse(
            userId = 1024,
            userType = request.userType,
            accessToken = "dev-access-token",
            refreshToken = "dev-refresh-token",
        )
    }

    fun login(request: LoginRequest): LoginResponse {
        // TODO: 사용자 조회, 비밀번호 검증, JWT 발급 구현
        return LoginResponse(
            accessToken = "dev-access-token",
            refreshToken = "dev-refresh-token",
            user = AuthUserResponse(
                userId = 1024,
                email = request.email,
                name = "홍길동",
                userType = UserType.STUDENT,
                isEmailVerified = true,
            ),
        )
    }
}
