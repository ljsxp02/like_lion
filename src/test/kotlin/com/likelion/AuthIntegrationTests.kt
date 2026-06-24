package com.likelion

import com.likelion.auth.AuthService
import com.likelion.auth.LoginRequest
import com.likelion.auth.SignupRequest
import com.likelion.auth.SignupUserType
import com.likelion.common.ApiException
import com.likelion.common.ErrorCode
import com.likelion.domain.user.UserRepository
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertFailsWith

@SpringBootTest
@Transactional
class AuthIntegrationTests @Autowired constructor(
    private val authService: AuthService,
    private val userRepository: UserRepository,
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

        val loginResponse = authService.login(
            LoginRequest(
                email = "demo@kw.ac.kr",
                password = "password1234",
            ),
        )

        assertEquals(savedUser.id, loginResponse.user.userId)
        assertFalse(loginResponse.user.isEmailVerified)
        assertNotNull(loginResponse.accessToken)
        assertNotNull(loginResponse.refreshToken)
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
}
