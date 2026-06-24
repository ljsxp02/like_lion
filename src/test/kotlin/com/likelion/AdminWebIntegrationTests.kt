package com.likelion

import com.fasterxml.jackson.databind.ObjectMapper
import com.likelion.auth.JwtService
import com.likelion.domain.user.UserEntity
import com.likelion.domain.user.UserRepository
import com.likelion.domain.user.UserType
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminWebIntegrationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
) {
    @Test
    fun `관리자 토큰으로 매장 등록 API를 호출하면 실제 매장이 조회된다`() {
        val token = accessTokenFor(UserType.ADMIN)

        val body = mockMvc.post("/api/v1/admin/stores") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content =
                """
                {
                  "name": "웹 등록 매장",
                  "address": "서울 노원구 광운로 20",
                  "location": "광운대 정문",
                  "contact": "02-0000-0000",
                  "thumbnailUrl": null,
                  "latitude": 37.6198,
                  "longitude": 127.0591
                }
                """.trimIndent()
        }.andExpect {
            status { isCreated() }
            jsonPath("$.data.storeId") { isNumber() }
            jsonPath("$.data.name") { value("웹 등록 매장") }
            jsonPath("$.data.isActive") { value(true) }
        }.andReturn().response.contentAsString

        val storeId = objectMapper.readTree(body)
            .required("data")
            .required("storeId")
            .longValue()

        mockMvc.get("/api/v1/stores/$storeId")
            .andExpect {
                status { isOk() }
                jsonPath("$.data.storeId") { value(storeId) }
                jsonPath("$.data.name") { value("웹 등록 매장") }
            }
    }

    @Test
    fun `학생 토큰으로 매장 등록 API를 호출하면 관리자 권한 오류를 반환한다`() {
        val token = accessTokenFor(UserType.STUDENT)

        mockMvc.post("/api/v1/admin/stores") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $token")
            contentType = MediaType.APPLICATION_JSON
            content =
                """
                {
                  "name": "권한 없는 매장",
                  "address": "서울 노원구 광운로 20",
                  "location": null,
                  "contact": null,
                  "thumbnailUrl": null,
                  "latitude": null,
                  "longitude": null
                }
                """.trimIndent()
        }.andExpect {
            status { isForbidden() }
            jsonPath("$.code") { value("AUTH_004") }
        }
    }

    private fun accessTokenFor(userType: UserType): String {
        val user = userRepository.save(
            UserEntity(
                email = "${userType.name.lowercase()}-${System.nanoTime()}@kw.ac.kr",
                passwordHash = "hashed-password",
                name = "$userType User",
                userType = userType,
                isEmailVerified = true,
            ),
        )
        return jwtService.createAccessToken(requireNotNull(user))
    }
}
