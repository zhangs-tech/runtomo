package com.zhangs_tech.runtomo.server

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(properties = ["runtomo.google.web-client-id=test-client-id"])
@AutoConfigureMockMvc
class HealthProbeTests(@Autowired val mockMvc: MockMvc) {

    @Test
    fun `liveness probe needs no token`() {
        mockMvc.get("/livez").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("UP") }
        }
    }

    @Test
    fun `readiness probe needs no token`() {
        mockMvc.get("/readyz").andExpect {
            status { isOk() }
            jsonPath("$.status") { value("UP") }
        }
    }

    @Test
    fun `other actuator endpoints need a token`() {
        mockMvc.get("/actuator/health").andExpect { status { isUnauthorized() } }
    }
}
