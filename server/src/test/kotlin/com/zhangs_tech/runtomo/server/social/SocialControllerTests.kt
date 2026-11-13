package com.zhangs_tech.runtomo.server.social

import com.zhangs_tech.runtomo.server.RestConfig
import com.zhangs_tech.runtomo.server.activity.InMemoryDailyStatsRepository
import com.zhangs_tech.runtomo.server.coin.CoinReason
import com.zhangs_tech.runtomo.server.coin.InMemoryCoinRepository
import com.zhangs_tech.runtomo.server.food.FoodController
import com.zhangs_tech.runtomo.server.food.FoodService
import com.zhangs_tech.runtomo.server.food.InMemoryFeedingRepository
import com.zhangs_tech.runtomo.server.food.InMemoryFoodInventoryRepository
import com.zhangs_tech.runtomo.server.game.GameCalendar
import com.zhangs_tech.runtomo.server.game.RewardRules
import com.zhangs_tech.runtomo.server.game.testDaytimeClock
import com.zhangs_tech.runtomo.server.game.testGameData
import com.zhangs_tech.runtomo.server.tomo.InMemoryTomoRepository
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
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.LocalDate

@WebMvcTest(SocialController::class, FoodController::class)
@Import(
    RestConfig::class,
    FriendService::class,
    LeaderboardService::class,
    FoodService::class,
    ApiExceptionHandler::class,
    SocialControllerTests.Config::class,
)
class SocialControllerTests(
    @Autowired val mockMvc: MockMvc,
    @Autowired val userRepository: InMemoryUserRepository,
    @Autowired val tomoRepository: InMemoryTomoRepository,
    @Autowired val coinRepository: InMemoryCoinRepository,
    @Autowired val inventory: InMemoryFoodInventoryRepository,
) {
    @TestConfiguration
    class Config {
        @Bean fun gameData() = testGameData
        @Bean fun gameCalendar() = GameCalendar(testDaytimeClock)
        @Bean fun rewardRules() = RewardRules(testGameData)
        @Bean fun userRepository() = InMemoryUserRepository()
        @Bean fun tomoRepository() = InMemoryTomoRepository()
        @Bean fun coinRepository(users: InMemoryUserRepository) = InMemoryCoinRepository(users)
        @Bean fun dailyStatsRepository() = InMemoryDailyStatsRepository()
        @Bean fun friendRepository() = InMemoryFriendRepository()
        @Bean fun friendRequestRepository() = InMemoryFriendRequestRepository()
        @Bean fun foodInventoryRepository() = InMemoryFoodInventoryRepository()
        @Bean fun feedingRepository() = InMemoryFeedingRepository()
    }

    private fun player(code: String): User {
        val user = userRepository.create("sub-$code", "Player $code", code)!!
        val tomo = tomoRepository.create(user.id, "luppy", "Mochi", LocalDate.of(2026, 11, 1))
        userRepository.setEquippedTomo(user.id, tomo.id)
        return userRepository.findById(user.id)!!
    }

    private fun User.token() = jwt().jwt { it.subject(id.toString()) }

    @Test
    fun `look up, request, accept, leaderboard and remove`() {
        val kai = player("k1")
        val ana = player("a1")

        mockMvc.get("/users/by-code/a1") { with(kai.token()) }.andExpect {
            status { isOk() }
            jsonPath("$.displayName") { value("Player a1") }
            jsonPath("$.tomo.nickname") { value("Mochi") }
            jsonPath("$.tomo.bondPoints") { doesNotExist() }
        }
        mockMvc.post("/friend-requests") {
            with(kai.token())
            contentType = MediaType.APPLICATION_JSON
            content = """{"toUserId":"${ana.id}"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.status") { value("SENT") }
        }
        val requestId = mockMvc.get("/friend-requests") { with(ana.token()) }.andExpect {
            jsonPath("$[0].from.displayName") { value("Player k1") }
        }.andReturn().response.contentAsString.substringAfter("\"id\":\"").substringBefore("\"")

        mockMvc.post("/friend-requests/$requestId/accept") { with(ana.token()) }.andExpect { status { isNoContent() } }
        mockMvc.get("/leaderboard?period=today") { with(kai.token()) }.andExpect {
            status { isOk() }
            jsonPath("$.period") { value("today") }
            jsonPath("$.rows.length()") { value(2) }
        }
        mockMvc.get("/friends") { with(kai.token()) }.andExpect { jsonPath("$[0].player.displayName") { value("Player a1") } }
        mockMvc.delete("/friends/${ana.id}") { with(kai.token()) }.andExpect { status { isNoContent() } }
        mockMvc.get("/friends") { with(ana.token()) }.andExpect { jsonPath("$.length()") { value(0) } }
    }

    @Test
    fun `look up problems have their own types`() {
        val kai = player("k2")

        mockMvc.get("/users/by-code/nobody") { with(kai.token()) }.andExpect { status { isNotFound() } }
        mockMvc.get("/users/by-code/k2") { with(kai.token()) }.andExpect {
            status { isConflict() }
            jsonPath("$.type") { value("/problems/own-friend-code") }
        }
    }

    @Test
    fun `an unknown leaderboard period returns 400`() {
        mockMvc.get("/leaderboard?period=year") { with(player("k3").token()) }.andExpect { status { isBadRequest() } }
    }

    @Test
    fun `buy and feed, then a full Tomo returns 409 feeding-limit`() {
        val kai = player("k4")
        coinRepository.record(kai.id, 200, CoinReason.ACTIVITY, null)
        val tomoId = kai.equippedTomoId!!

        mockMvc.get("/food") { with(kai.token()) }.andExpect {
            jsonPath("$.items[0].code") { value("banana") }
            jsonPath("$.feedsLeftToday") { value(3) }
        }
        mockMvc.post("/food/banana/purchase") { with(kai.token()) }.andExpect {
            status { isOk() }
            jsonPath("$.coinBalance") { value(150) }
            jsonPath("$.items[0].quantity") { value(1) }
        }
        mockMvc.post("/me/tomos/$tomoId/feed") {
            with(kai.token())
            contentType = MediaType.APPLICATION_JSON
            content = """{"foodCode":"banana"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.pantry.tomo.bondPoints") { value(3) }
            jsonPath("$.pantry.feedsLeftToday") { value(2) }
        }
        repeat(2) {
            inventory.add(kai.id, "banana")
            mockMvc.post("/me/tomos/$tomoId/feed") {
                with(kai.token())
                contentType = MediaType.APPLICATION_JSON
                content = """{"foodCode":"banana"}"""
            }
        }
        inventory.add(kai.id, "banana")
        mockMvc.post("/me/tomos/$tomoId/feed") {
            with(kai.token())
            contentType = MediaType.APPLICATION_JSON
            content = """{"foodCode":"banana"}"""
        }.andExpect {
            status { isConflict() }
            jsonPath("$.type") { value("/problems/feeding-limit") }
        }
    }

    @Test
    fun `buy without enough TomoCoins returns 409 not-enough-coins`() {
        mockMvc.post("/food/bento/purchase") { with(player("k5").token()) }.andExpect {
            status { isConflict() }
            jsonPath("$.type") { value("/problems/not-enough-coins") }
            jsonPath("$.shortfall") { value(150) }
        }
    }
}
