package com.likelion.auth

import com.likelion.common.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
) {
    @PostMapping("/email/send-code")
    fun sendEmailCode(
        @Valid @RequestBody request: SendEmailCodeRequest,
    ): ApiResponse<SendEmailCodeResponse> =
        ApiResponse.ok(message = "인증 코드가 발송되었습니다.", data = authService.sendEmailCode(request))

    @PostMapping("/email/verify")
    fun verifyEmail(
        @Valid @RequestBody request: VerifyEmailRequest,
    ): ApiResponse<VerifyEmailResponse> =
        ApiResponse.ok(message = "이메일 인증이 완료되었습니다.", data = authService.verifyEmail(request))

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signup(
        @Valid @RequestBody request: SignupRequest,
    ): ApiResponse<SignupResponse> =
        ApiResponse.created(message = "회원가입이 완료되었습니다.", data = authService.signup(request))

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody request: LoginRequest,
    ): ApiResponse<LoginResponse> =
        ApiResponse.ok(message = "로그인되었습니다.", data = authService.login(request))
}
