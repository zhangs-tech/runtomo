package com.zhangs_tech.runtomo.server.activity

import com.zhangs_tech.runtomo.server.RestConfig
import com.zhangs_tech.runtomo.server.coin.InMemoryCoinRepository
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.RewardRules
import com.zhangs_tech.runtomo.server.game.testDaytimeClock
import com.zhangs_tech.runtomo.server.game.testGameData
import com.zhangs_tech.runtomo.server.home.HomeController
import com.zhangs_tech.runtomo.server.home.HomeService
import com.zhangs_tech.runtomo.server.quest.InMemoryQuestClaimRepository
import com.zhangs_tech.runtomo.server.quest.QuestController
import com.zhangs_tech.runtomo.server.quest.QuestService
import com.zhangs_tech.runtomo.server.tomo.InMemoryTomoRepository
import com.zhangs_tech.runtomo.server.tomo.MoodRules
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
import org.springframework.test.web.servlet.put
import java.time.LocalDate

@WebMvcTest(ActivityController::class, HomeController::class, QuestController::class)
@Import(
    RestConfig::class,
    ActivityService::class,
    HomeService::class,
    QuestService::class,
    ApiExceptionHandler::class,
    ActivityControllerTests.Config::class,
)
class ActivityControllerTests(
    @Autowired val mockMvc: MockMvc,
    @Autowired val userRepository: InMemoryUserRepository,
    @Autowired val tomoRepository: InMemoryTomoRepository,
) {
    @TestConfiguration
    class Config {
        @Bean fun gameData() = testGameData
        @Bean fun gameCalendar() = GameCalendar(testDaytimeClock)
        @Bean fun rewardRules() = RewardRules(testGameData)
        @Bean fun moodRules() = MoodRules(testGameData)
        @Bean fun userRepository() = InMemoryUserRepository()
        @Bean fun tomoRepository() = InMemoryTomoRepository()
        @Bean fun questClaimRepository() = InMemoryQuestClaimRepository()
        @Bean fun dailyStatsRepository() = InMemoryDailyStatsRepository()
        @Bean fun coinRepository(users: InMemoryUserRepository) = InMemoryCoinRepository(users)
    }

    private val today = "2026-11-14"

    @Test
    fun `sync returns totals and rewards`() {
        val user = createPlayer("sub-1")

        sync(user, today, """{"steps":1600}""").andExpect {
            status { isOk() }
            jsonPath("$.distanceM") { value(1200) }
            jsonPath("$.strideM") { value(0.75) }
            jsonPath("$.coinBalance") { value(120) }
            jsonPath("$.mood") { value("HAPPY") }
            jsonPath("$.rewards.coins") { value(120) }
            jsonPath("$.rewards.tomoNickname") { value("Mochi") }
            jsonPath("$.rewards.completedQuests[0].code") { value("walk-1km") }
            jsonPath("$.rewards.showRewardScreen") { value(true) }
        }
    }

    @Test
    fun `sync for another day returns 422 day-closed`() {
        val user = createPlayer("sub-2")

        sync(user, "2026-11-13", """{"steps":1600}""").andExpect {
            status { isUnprocessableContent() }
            jsonPath("$.type") { value("/problems/day-closed") }
        }
    }

    @Test
    fun `sync with negative steps returns 400`() {
        val user = createPlayer("sub-3")

        sync(user, today, """{"steps":-1}""").andExpect { status { isBadRequest() } }
    }

    @Test
    fun `home returns Tomo, today, goal, quests and last sync`() {
        val user = createPlayer("sub-4")
        sync(user, today, """{"steps":1600}""")

        mockMvc.get("/home") { with(jwt().jwt { it.subject(user.id.toString()) }) }.andExpect {
            status { isOk() }
            jsonPath("$.tomo.nickname") { value("Mochi") }
            jsonPath("$.tomo.species.code") { value("luppy") }
            jsonPath("$.today.steps") { value(1600) }
            jsonPath("$.dailyGoalM") { value(5000) }
            jsonPath("$.coinBalance") { value(120) }
            jsonPath("$.quests[0].status") { value("CLAIMABLE") }
            jsonPath("$.lastSyncDate") { value(today) }
        }
    }

    @Test
    fun `claim a quest returns the new balance, a second claim returns 409`() {
        val user = createPlayer("sub-5")
        sync(user, today, """{"steps":1600}""")

        claim(user, "walk-1km").andExpect {
            status { isOk() }
            jsonPath("$.coinBalance") { value(220) }
            jsonPath("$.quests[0].status") { value("CLAIMED") }
        }
        claim(user, "walk-1km").andExpect { status { isConflict() } }
    }

    @Test
    fun `claim an unknown quest returns 404`() {
        claim(createPlayer("sub-6"), "unknown").andExpect { status { isNotFound() } }
    }

    private fun createPlayer(subject: String): User {
        val user = userRepository.create(subject, "Runner0001", subject.padStart(8, '0'))!!
        val tomo = tomoRepository.create(user.id, "luppy", "Mochi", LocalDate.parse(today))
        userRepository.setEquippedTomo(user.id, tomo.id)
        return user
    }

    private fun sync(user: User, date: String, body: String) =
        mockMvc.put("/me/days/$date") {
            with(jwt().jwt { it.subject(user.id.toString()) })
            contentType = MediaType.APPLICATION_JSON
            content = body
        }

    private fun claim(user: User, code: String) =
        mockMvc.post("/quests/$code/claim") { with(jwt().jwt { it.subject(user.id.toString()) }) }
}
