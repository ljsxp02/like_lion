package com.likelion

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.options

@SpringBootTest
@AutoConfigureMockMvc
class DeploymentReadinessTests @Autowired constructor(
    private val mockMvc: MockMvc,
) {
    @Test
    fun `vercel frontend origin is allowed`() {
        mockMvc.options("/api/v1/auth/login") {
            header(HttpHeaders.ORIGIN, "https://team1-fe-1m1a.vercel.app")
            header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
        }.andExpect {
            status { isOk() }
            header {
                string(
                    HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                    "https://team1-fe-1m1a.vercel.app",
                )
            }
        }
    }

    @Test
    fun `unknown origin is not allowed`() {
        mockMvc.options("/api/v1/auth/login") {
            header(HttpHeaders.ORIGIN, "https://example.com")
            header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
        }.andExpect {
            status { isForbidden() }
        }
    }

    @Test
    fun `health endpoint is available`() {
        mockMvc.get("/actuator/health")
            .andExpect {
                status { isOk() }
                jsonPath("$.status") { value("UP") }
            }
    }
}
