package com.zhangs_tech.runtomo.server.tomo

import com.zhangs_tech.runtomo.server.RestConfig
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.testClock
import com.zhangs_tech.runtomo.server.game.testGameData
import com.zhangs_tech.runtomo.server.user.InMemoryUserRepository
import com.zhangs_tech.runtomo.server.user.User
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
import org.springframework.test.web.servlet.post

@WebMvcTest(TomoController::class)
@Import(RestConfig::class, TomoService::class, ApiExceptionHandler::class, TomoControllerTests.Config::class)
class TomoControllerTests(
    @Autowired val mockMvc: MockMvc,
    @Autowired val userRepository: InMemoryUserRepository,
) {
    @TestConfiguration
    class Config {
        @Bean
        fun gameData() = testGameData

        @Bean
        fun gameCalendar() = GameCalendar(testClock)

        @Bean
        fun userRepository() = InMemoryUserRepository()

        @Bean
        fun tomoRepository() = InMemoryTomoRepository()
    }

    @Test
    fun `get starters returns only starter species`() {
        mockMvc.get("/species/starters") { with(jwt()) }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].code") { value("luppy") }
            jsonPath("$[0].rarity") { value("COMMON") }
        }
    }

    @Test
    fun `get starters without a token returns 401`() {
        mockMvc.get("/species/starters").andExpect { status { isUnauthorized() } }
    }

    @Test
    fun `choose starter returns 201 with the new Tomo`() {
        val user = createUser("sub-1")

        chooseStarter(user, """{"speciesCode":"luppy","nickname":"Mochi"}""").andExpect {
            status { isCreated() }
            jsonPath("$.nickname") { value("Mochi") }
            jsonPath("$.species.name") { value("Luppy") }
            jsonPath("$.bondLevel") { value(1) }
            jsonPath("$.birthday") { value("2026-11-14") }
        }
    }

    @Test
    fun `choose starter twice returns 409`() {
        val user = createUser("sub-2")
        chooseStarter(user, """{"speciesCode":"luppy","nickname":"Mochi"}""")

        chooseStarter(user, """{"speciesCode":"rocky","nickname":"Rocky"}""").andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `choose starter with an invalid nickname returns 400 problem`() {
        val user = createUser("sub-3")

        chooseStarter(user, """{"speciesCode":"luppy","nickname":""}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.detail") { value("Nickname must be 1 to 16 characters") }
        }
    }

    private fun createUser(subject: String): User =
        userRepository.create(subject, "Runner0001", subject.padStart(8, '0'))!!

    private fun chooseStarter(user: User, body: String) =
        mockMvc.post("/me/tomos/starter") {
            with(jwt().jwt { it.subject(user.id.toString()) })
            contentType = MediaType.APPLICATION_JSON
            content = body
        }
}
