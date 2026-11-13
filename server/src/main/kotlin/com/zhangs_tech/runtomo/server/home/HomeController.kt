package com.zhangs_tech.runtomo.server.home

import com.zhangs_tech.runtomo.server.auth.CurrentUserId
import com.zhangs_tech.runtomo.server.game.GameData
import com.zhangs_tech.runtomo.server.home.dto.HomeResponse
import com.zhangs_tech.runtomo.server.home.dto.TodayResponse
import com.zhangs_tech.runtomo.server.quest.dto.QuestResponse
import com.zhangs_tech.runtomo.server.tomo.dto.TomoResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class HomeController(private val homeService: HomeService, private val gameData: GameData) {
    @GetMapping("/home")
    fun getHome(@CurrentUserId userId: UUID): HomeResponse {
        val home = homeService.getHome(userId)
        return HomeResponse(
            displayName = home.user.displayName,
            tomo = home.tomo?.let { TomoResponse.from(it, gameData.findSpecies(it.speciesCode)!!) },
            mood = home.mood,
            today = TodayResponse(home.today.localDate, home.today.steps, home.today.distanceM),
            dailyGoalM = gameData.tunables.dailyGoalM,
            strideM = gameData.tunables.strideM,
            coinBalance = home.user.coinBalance,
            quests = home.quests.map(QuestResponse::from),
            lastSyncDate = home.lastSyncDate,
            speechBubbleOneIn = gameData.tunables.speechBubbleOneIn,
        )
    }
}
