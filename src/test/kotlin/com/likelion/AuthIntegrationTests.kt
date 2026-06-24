package com.likelion

import com.likelion.auth.AuthService
import com.likelion.auth.JwtService
import com.likelion.auth.LoginRequest
import com.likelion.auth.SignupRequest
import com.likelion.auth.SignupUserType
import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import com.likelion.domain.user.UserRepository
import com.likelion.domain.user.UserEntity
import com.likelion.domain.user.UserType
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith

@SpringBootTest
@Transactional
class AuthIntegrationTests @Autowired constructor(
    private val authService: AuthService,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
    private val userRepository: UserRepository,
    private val entityManager: EntityManager,
) {
    @Test
    fun `email verification 없이 회원가입 후 로그인할 수 있다`() {
        val signupResponse = authService.signup(
            SignupRequest(
                email = "demo@kw.ac.kr",
                password = "password1234",
                name = "Demo User",
                userType = SignupUserType.STUDENT,
                collegeId = null,
                departmentId = null,
                storeId = null,
            ),
        )

        val savedUser = userRepository.findByEmail("demo@kw.ac.kr")
        assertNotNull(savedUser)
        assertEquals(savedUser.id, signupResponse.userId)
        assertFalse(savedUser.isEmailVerified)
        assertFalse(savedUser.passwordHash == "password1234")
        assertEquals(true, passwordEncoder.matches("password1234", savedUser.passwordHash))

        entityManager.flush()
        entityManager.clear()

        val loginResponse = authService.login(
            LoginRequest(
                email = "demo@kw.ac.kr",
                password = "password1234",
            ),
        )

        assertEquals(savedUser.id, loginResponse.user.userId)
        assertFalse(loginResponse.user.isEmailVerified)
        assertNotNull(loginResponse.accessToken)
        assertEquals(3600, loginResponse.expiresInSeconds)
        assertEquals(savedUser.id, jwtService.parseAccessToken(loginResponse.accessToken).userId)
    }

    @Test
    fun `중복 이메일 회원가입은 거부한다`() {
        val request = SignupRequest(
            email = "duplicate@kw.ac.kr",
            password = "password1234",
            name = "Demo User",
            userType = SignupUserType.STUDENT,
            collegeId = null,
            departmentId = null,
            storeId = null,
        )
        authService.signup(request)

        val exception = assertFailsWith<ApiException> {
            authService.signup(request)
        }

        assertEquals(ErrorCode.USER_409, exception.errorCode)
    }

    @Test
    fun `비밀번호가 다르면 로그인할 수 없다`() {
        authService.signup(
            SignupRequest(
                email = "login@kw.ac.kr",
                password = "password1234",
                name = "Demo User",
                userType = SignupUserType.STUDENT,
                collegeId = null,
                departmentId = null,
                storeId = null,
            ),
        )

        val exception = assertFailsWith<ApiException> {
            authService.login(
                LoginRequest(
                    email = "login@kw.ac.kr",
                    password = "wrong-password",
                ),
            )
        }

        assertEquals(ErrorCode.AUTH_401, exception.errorCode)
    }

    @Test
    fun `기존 평문 비밀번호 계정은 첫 로그인 시 BCrypt로 전환한다`() {
        userRepository.save(
            UserEntity(
                email = "legacy@kw.ac.kr",
                passwordHash = "legacy-password",
                name = "Legacy User",
                userType = UserType.STUDENT,
            ),
        )
        entityManager.flush()
        entityManager.clear()

        authService.login(
            LoginRequest(
                email = "legacy@kw.ac.kr",
                password = "legacy-password",
            ),
        )
        entityManager.flush()
        entityManager.clear()

        val migratedUser = requireNotNull(userRepository.findByEmail("legacy@kw.ac.kr"))
        assertFalse(migratedUser.passwordHash == "legacy-password")
        assertEquals(true, passwordEncoder.matches("legacy-password", migratedUser.passwordHash))
    }
}
