package com.zhangs_tech.runtomo.server.auth

import com.zhangs_tech.runtomo.server.RestConfig
import com.zhangs_tech.runtomo.server.user.FriendCodeGenerator
import com.zhangs_tech.runtomo.server.user.InMemoryUserRepository
import com.zhangs_tech.runtomo.server.web.ApiExceptionHandler
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@WebMvcTest(AuthController::class)
@Import(
    RestConfig::class,
    AuthService::class,
    TokenService::class,
    FriendCodeGenerator::class,
    ApiExceptionHandler::class,
    AuthControllerTests.Config::class
)
class AuthControllerTests(@Autowired val mockMvc: MockMvc) {

    @TestConfiguration
    class Config {
        @Bean
        fun identityVerifier() = object : IdentityVerifier {
            override fun verify(idToken: String): Identity? =
                if (idToken == "valid") Identity("sub-1", "a@example.com", null) else null
        }

        @Bean
        fun userRepository() = InMemoryUserRepository()

        @Bean
        fun refreshTokenRepository() = InMemoryRefreshTokenRepository()
    }

    @Test
    fun `sign in with invalid token returns 401 problem`() {
        mockMvc.post("/auth/signin") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idToken":"garbage"}"""
        }.andExpect {
            status { isUnauthorized() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.status") { value(401) }
            jsonPath("$.detail") { value("ID token is invalid or expired") }
        }
    }

    @Test
    fun `sign in with valid token returns 200`() {
        mockMvc.post("/auth/signin") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idToken":"valid"}"""
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `sign in with a blank token returns 400`() {
        mockMvc.post("/auth/signin") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idToken":""}"""
        }.andExpect {
            status { isBadRequest() }
        }
    }

    @Test
    fun `refresh with a valid token returns a new token pair`() {
        val refreshToken = signInAndGetRefreshToken()

        mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"refreshToken":"$refreshToken"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.accessToken") { isNotEmpty() }
            jsonPath("$.refreshToken") { isNotEmpty() }
        }
    }

    @Test
    fun `refresh with an unknown token returns 401 problem`() {
        mockMvc.post("/auth/refresh") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"refreshToken":"unknown"}"""
        }.andExpect {
            status { isUnauthorized() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.detail") { value("Refresh token is invalid or expired") }
        }
    }

    @Test
    fun `sign out without an access token returns 204`() {
        val refreshToken = signInAndGetRefreshToken()

        mockMvc.post("/auth/signout") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"refreshToken":"$refreshToken"}"""
        }.andExpect {
            status { isNoContent() }
        }
    }

    private fun signInAndGetRefreshToken(): String {
        val body = mockMvc.post("/auth/signin") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"idToken":"valid"}"""
        }.andReturn().response.contentAsString
        return JsonPath.read(body, "$.refreshToken")
    }
}
