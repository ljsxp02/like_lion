package com.likelion.auth

import com.likelion.domain.user.UserType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class SendEmailCodeRequest(
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String,
)

data class SendEmailCodeResponse(
    val email: String,
    val expiresInSeconds: Int,
    val resendAvailableInSeconds: Int,
)

data class VerifyEmailRequest(
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String,
    @field:NotBlank(message = "인증 코드는 필수입니다.")
    val code: String,
)

data class VerifyEmailResponse(
    val email: String,
    val verificationToken: String,
)

data class SignupRequest(
    @field:NotBlank(message = "인증 토큰은 필수입니다.")
    val verificationToken: String,
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String,
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,
    @field:NotBlank(message = "이름은 필수입니다.")
    val name: String,
    @field:NotNull(message = "사용자 유형은 필수입니다.")
    val userType: SignupUserType,
    val collegeId: Long?,
    val departmentId: Long?,
    val storeId: Long?,
)

data class SignupResponse(
    val userId: Long,
    val userType: SignupUserType,
    val accessToken: String,
    val refreshToken: String,
)

data class LoginRequest(
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    @field:NotBlank(message = "이메일은 필수입니다.")
    val email: String,
    @field:NotBlank(message = "비밀번호는 필수입니다.")
    val password: String,
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: AuthUserResponse,
)

data class AuthUserResponse(
    val userId: Long,
    val email: String,
    val name: String,
    val userType: UserType,
    val isEmailVerified: Boolean,
)

enum class SignupUserType {
    STUDENT,
    OWNER,
}
