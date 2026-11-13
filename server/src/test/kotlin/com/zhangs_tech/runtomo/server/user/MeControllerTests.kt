package com.zhangs_tech.runtomo.server.user

import com.zhangs_tech.runtomo.server.RestConfig
import com.zhangs_tech.runtomo.server.web.ApiExceptionHandler
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import java.util.UUID

@WebMvcTest(MeController::class)
@Import(RestConfig::class, UserService::class, ApiExceptionHandler::class, MeControllerTests.Config::class)
class MeControllerTests(
    @Autowired val mockMvc: MockMvc,
    @Autowired val userRepository: InMemoryUserRepository,
) {
    @TestConfiguration
    class Config {
        @Bean
        fun userRepository() = InMemoryUserRepository()
    }

    @Test
    fun `get me returns the Player`() {
        val user = userRepository.create("sub-1", "Runner0001", "00000001")!!

        mockMvc.get("/me") { with(jwt().jwt { it.subject(user.id.toString()) }) }.andExpect {
            status { isOk() }
            jsonPath("$.displayName") { value("Runner0001") }
            jsonPath("$.friendCode") { value("00000001") }
            jsonPath("$.coinBalance") { value(0) }
            jsonPath("$.equippedTomoId") { value(null) }
        }
    }

    @Test
    fun `get me for a deleted user returns 401`() {
        mockMvc.get("/me") { with(jwt().jwt { it.subject(UUID.randomUUID().toString()) }) }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `patch me changes the display name`() {
        val user = userRepository.create("sub-2", "Runner0002", "00000002")!!

        patchMe(user, """{"displayName":"Kai"}""").andExpect {
            status { isOk() }
            jsonPath("$.displayName") { value("Kai") }
        }
    }

    @Test
    fun `patch me with a short display name returns 400 problem`() {
        val user = userRepository.create("sub-3", "Runner0003", "00000003")!!

        patchMe(user, """{"displayName":"Al"}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.detail") { value("Display name must be 3 to 20 characters") }
        }
    }

    private fun patchMe(user: User, body: String) =
        mockMvc.patch("/me") {
            with(jwt().jwt { it.subject(user.id.toString()) })
            contentType = MediaType.APPLICATION_JSON
            content = body
        }
}
