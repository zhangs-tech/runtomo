package com.zhangs_tech.runtomo.server.summon

import com.zhangs_tech.runtomo.server.RestConfig
import com.zhangs_tech.runtomo.server.activity.DailyStats
import com.zhangs_tech.runtomo.server.activity.HistoryController
import com.zhangs_tech.runtomo.server.activity.HistoryService
import com.zhangs_tech.runtomo.server.activity.InMemoryDailyStatsRepository
import com.zhangs_tech.runtomo.server.coin.CoinReason
import com.zhangs_tech.runtomo.server.coin.InMemoryCoinRepository
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.testDaytimeClock
import com.zhangs_tech.runtomo.server.game.testGameData
import com.zhangs_tech.runtomo.server.quest.InMemoryQuestClaimRepository
import com.zhangs_tech.runtomo.server.tomo.InMemoryTomoRepository
import com.zhangs_tech.runtomo.server.tomo.TomoController
import com.zhangs_tech.runtomo.server.tomo.TomoService
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
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.time.LocalDate
import java.util.UUID

@WebMvcTest(TomoController::class, SummonController::class, HistoryController::class)
@Import(
    RestConfig::class,
    TomoService::class,
    SummonService::class,
    HistoryService::class,
    ApiExceptionHandler::class,
    CollectionControllerTests.Config::class,
)
class CollectionControllerTests(
    @Autowired val mockMvc: MockMvc,
    @Autowired val userRepository: InMemoryUserRepository,
    @Autowired val tomoRepository: InMemoryTomoRepository,
    @Autowired val coinRepository: InMemoryCoinRepository,
    @Autowired val dailyStatsRepository: InMemoryDailyStatsRepository,
) {
    @TestConfiguration
    class Config {
        @Bean fun gameData() = testGameData
        @Bean fun gameCalendar() = GameCalendar(testDaytimeClock)
        @Bean fun summonRoller() = SummonRoller(FixedRandom(*IntArray(10)))
        @Bean fun userRepository() = InMemoryUserRepository()
        @Bean fun tomoRepository() = InMemoryTomoRepository()
        @Bean fun coinRepository(users: InMemoryUserRepository) = InMemoryCoinRepository(users)
        @Bean fun summonRepository() = InMemorySummonRepository()
        @Bean fun pityRepository() = InMemoryPityRepository()
        @Bean fun dailyStatsRepository() = InMemoryDailyStatsRepository()
        @Bean fun questClaimRepository() = InMemoryQuestClaimRepository()
    }

    private val today = LocalDate.of(2026, 11, 14)

    private fun createPlayer(subject: String): User {
        val user = userRepository.create(subject, "Runner0001", subject.padStart(8, '0'))!!
        val tomo = tomoRepository.create(user.id, "luppy", "Mochi", today)
        userRepository.setEquippedTomo(user.id, tomo.id)
        return user
    }

    private fun User.token() = jwt().jwt { it.subject(id.toString()) }

    @Test
    fun `collection lists the roster with progress`() {
        val user = createPlayer("sub-1")

        mockMvc.get("/me/tomos") { with(user.token()) }.andExpect {
            status { isOk() }
            jsonPath("$.collectedCount") { value(1) }
            jsonPath("$.totalCount") { value(3) }
            jsonPath("$.entries[0].tomo.nickname") { value("Mochi") }
            jsonPath("$.entries[1].tomo") { value(null) }
        }
    }

    @Test
    fun `equip and rename an owned Tomo, another Player's Tomo returns 404`() {
        val user = createPlayer("sub-2")
        val rocky = tomoRepository.create(user.id, "rocky", "Rocky", today)
        val other = createPlayer("sub-3")

        mockMvc.put("/me/equipped-tomo") {
            with(user.token())
            contentType = MediaType.APPLICATION_JSON
            content = """{"tomoId":"${rocky.id}"}"""
        }.andExpect { status { isNoContent() } }
        mockMvc.patch("/me/tomos/${rocky.id}") {
            with(user.token())
            contentType = MediaType.APPLICATION_JSON
            content = """{"nickname":"Pebble"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.nickname") { value("Pebble") }
        }
        mockMvc.put("/me/equipped-tomo") {
            with(other.token())
            contentType = MediaType.APPLICATION_JSON
            content = """{"tomoId":"${rocky.id}"}"""
        }.andExpect { status { isNotFound() } }
    }

    @Test
    fun `banner shows rates from the weights and the pity counter`() {
        val user = createPlayer("sub-4")

        mockMvc.get("/banners/standard") { with(user.token()) }.andExpect {
            status { isOk() }
            jsonPath("$.cost") { value(1000) }
            jsonPath("$.pityRemaining") { value(3) }
            jsonPath("$.pool[0].ratePercent") { value(90.0) }
            jsonPath("$.rarityRates[1].rarity") { value("EPIC") }
            jsonPath("$.rarityRates[1].ratePercent") { value(10.0) }
        }
    }

    @Test
    fun `summon returns 201 with the result, then latest returns it`() {
        val user = createPlayer("sub-5")
        coinRepository.record(user.id, 1000, CoinReason.ACTIVITY, null)

        mockMvc.post("/summons") {
            with(user.token())
            contentType = MediaType.APPLICATION_JSON
            content = """{"bannerCode":"standard"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.species.code") { value("luppy") }
            jsonPath("$.wasDuplicate") { value(true) }
            jsonPath("$.duplicateCoins") { value(200) }
            jsonPath("$.coinBalance") { value(200) }
        }
        mockMvc.get("/summons?limit=1") { with(user.token()) }.andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(1) }
            jsonPath("$[0].species.code") { value("luppy") }
        }
    }

    @Test
    fun `summon without enough TomoCoins returns 409 with the shortfall`() {
        val user = createPlayer("sub-6")

        mockMvc.post("/summons") {
            with(user.token())
            contentType = MediaType.APPLICATION_JSON
            content = """{"bannerCode":"standard"}"""
        }.andExpect {
            status { isConflict() }
            jsonPath("$.type") { value("/problems/not-enough-coins") }
            jsonPath("$.shortfall") { value(1000) }
            jsonPath("$.distanceNeededM") { value(10000) }
        }
    }

    @Test
    fun `history returns totals and days`() {
        val user = createPlayer("sub-7")
        dailyStatsRepository.save(DailyStats(user.id, today, 1600, 1200, 120, 12))

        mockMvc.get("/me/totals") { with(user.token()) }.andExpect {
            status { isOk() }
            jsonPath("$.week.distanceM") { value(1200) }
            jsonPath("$.month.steps") { value(1600) }
        }
        mockMvc.get("/me/days?limit=30") { with(user.token()) }.andExpect {
            status { isOk() }
            jsonPath("$.days[0].date") { value("2026-11-14") }
            jsonPath("$.days[0].coins") { value(120) }
            jsonPath("$.nextCursor") { value(null) }
        }
    }

    @Test
    fun `an unknown banner returns 404`() {
        mockMvc.get("/banners/unknown") { with(createPlayer("sub-8").token()) }.andExpect { status { isNotFound() } }
    }
}
