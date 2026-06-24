package com.likelion.auth

import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import com.likelion.domain.auth.EmailVerificationCodeEntity
import com.likelion.domain.auth.EmailVerificationCodeRepository
import com.likelion.domain.user.UserEntity
import com.likelion.domain.user.UserRepository
import com.likelion.domain.user.UserType
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class AuthService(
    private val emailVerificationCodeRepository: EmailVerificationCodeRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun sendEmailCode(request: SendEmailCodeRequest): SendEmailCodeResponse {
        if (!request.email.endsWith("@kw.ac.kr")) {
            throw ApiException(ErrorCode.AUTH_400, "kw.ac.kr 이메일만 사용 가능합니다.")
        }

        val now = LocalDateTime.now()
        val latest = emailVerificationCodeRepository.findFirstByEmailOrderByIdDesc(request.email)
        if (latest != null && now.isBefore(latest.resendAvailableAt)) {
            throw ApiException(ErrorCode.AUTH_409, "재발송 대기 시간이 남아 있습니다.")
        }

        val code = (100000..999999).random().toString()
        emailVerificationCodeRepository.save(
            EmailVerificationCodeEntity(
                email = request.email,
                code = code,
                expiresAt = now.plusSeconds(300),
                resendAvailableAt = now.plusSeconds(60),
            ),
        )

        log.info("[EMAIL-VERIFY] email={} code={}", request.email, code)

        return SendEmailCodeResponse(
            email = request.email,
            expiresInSeconds = 300,
            resendAvailableInSeconds = 60,
        )
    }

    @Transactional
    fun verifyEmail(request: VerifyEmailRequest): VerifyEmailResponse {
        val now = LocalDateTime.now()
        val record = emailVerificationCodeRepository.findFirstByEmailOrderByIdDesc(request.email)
            ?: throw ApiException(ErrorCode.AUTH_400, "인증 코드를 먼저 요청해 주세요.")

        if (record.verified) {
            throw ApiException(ErrorCode.AUTH_409)
        }
        if (now.isAfter(record.expiresAt)) {
            throw ApiException(ErrorCode.AUTH_400, "인증 코드가 만료되었습니다.")
        }
        if (record.code != request.code) {
            throw ApiException(ErrorCode.AUTH_401)
        }

        val token = UUID.randomUUID().toString()
        record.verified = true
        record.verificationToken = token

        return VerifyEmailResponse(
            email = request.email,
            verificationToken = token,
        )
    }

    @Transactional
    fun signup(request: SignupRequest): SignupResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ApiException(ErrorCode.USER_409)
        }

        val userType = when (request.userType) {
            SignupUserType.STUDENT -> UserType.STUDENT
            SignupUserType.OWNER -> UserType.OWNER
        }

        val user = userRepository.save(
            UserEntity(
                email = request.email,
                passwordHash = passwordEncoder.encode(request.password),
                name = request.name,
                userType = userType,
                isEmailVerified = false,
                collegeId = if (request.userType == SignupUserType.STUDENT) request.collegeId else null,
                departmentId = if (request.userType == SignupUserType.STUDENT) request.departmentId else null,
                storeId = if (request.userType == SignupUserType.OWNER) request.storeId else null,
            ),
        )

        return SignupResponse(
            userId = user.id!!,
            userType = request.userType,
        )
    }

    @Transactional
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw ApiException(ErrorCode.USER_404)

        val isBcryptPassword = user.passwordHash.startsWith(BCRYPT_PREFIX)
        val passwordMatches =
            if (isBcryptPassword) {
                passwordEncoder.matches(request.password, user.passwordHash)
            } else {
                user.passwordHash == request.password
            }

        if (!passwordMatches) {
            throw ApiException(ErrorCode.AUTH_401)
        }
        if (!isBcryptPassword) {
            user.passwordHash = passwordEncoder.encode(request.password)
        }

        return LoginResponse(
            accessToken = jwtService.createAccessToken(user),
            expiresInSeconds = jwtService.accessTokenExpirationSeconds,
            user = AuthUserResponse(
                userId = user.id!!,
                email = user.email,
                name = user.name,
                userType = user.userType,
                isEmailVerified = user.isEmailVerified,
            ),
        )
    }

    private companion object {
        const val BCRYPT_PREFIX = "\$2"
    }
}
