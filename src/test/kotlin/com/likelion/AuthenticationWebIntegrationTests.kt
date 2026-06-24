package com.likelion

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
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
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationWebIntegrationTests @Autowired constructor(
    private val mockMvc: MockMvc,
    private val objectMapper: ObjectMapper,
) {
    @Test
    fun `protected endpoint rejects unauthenticated request`() {
        mockMvc.get("/api/v1/me")
            .andExpect {
                status { isUnauthorized() }
                jsonPath("$.code") { value("AUTH_001") }
            }
    }

    @Test
    fun `logged in user can access own mypage with bearer token`() {
        val email = "web-${UUID.randomUUID()}@kw.ac.kr"
        val password = "password1234"
        val name = "Web User"

        mockMvc.post("/api/v1/auth/signup") {
            contentType = MediaType.APPLICATION_JSON
            content =
                """
                {
                  "email": "$email",
                  "password": "$password",
                  "name": "$name",
                  "userType": "STUDENT",
                  "collegeId": null,
                  "departmentId": null,
                  "storeId": null
                }
                """.trimIndent()
        }.andExpect {
            status { isCreated() }
        }

        val loginBody = mockMvc.post("/api/v1/auth/login") {
            contentType = MediaType.APPLICATION_JSON
            content =
                """
                {
                  "email": "$email",
                  "password": "$password"
                }
                """.trimIndent()
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.accessToken") { isNotEmpty() }
            jsonPath("$.data.expiresInSeconds") { value(3600) }
            jsonPath("$.data.refreshToken") { doesNotExist() }
        }.andReturn().response.contentAsString

        val accessToken = objectMapper.readTree(loginBody)
            .required("data")
            .required("accessToken")
            .textValue()

        mockMvc.get("/api/v1/me") {
            header(HttpHeaders.AUTHORIZATION, "Bearer $accessToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.data.email") { value(email) }
            jsonPath("$.data.name") { value(name) }
        }
    }

    private fun JsonNode.required(fieldName: String): JsonNode =
        requireNotNull(get(fieldName)) { "Missing JSON field: $fieldName" }
}
