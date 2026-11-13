package com.zhangs_tech.runtomo.server.quest

import com.zhangs_tech.runtomo.server.auth.CurrentUserId
import com.zhangs_tech.runtomo.server.quest.dto.ClaimQuestResponse
import com.zhangs_tech.runtomo.server.quest.dto.QuestResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/quests")
class QuestController(private val questService: QuestService) {
    @GetMapping("/today")
    fun getTodayQuests(@CurrentUserId userId: UUID): List<QuestResponse> =
        questService.todayQuests(userId).map(QuestResponse::from)

    @PostMapping("/{code}/claim")
    fun claim(@CurrentUserId userId: UUID, @PathVariable code: String): ClaimQuestResponse {
        val balance = questService.claim(userId, code)
        return ClaimQuestResponse(balance, questService.todayQuests(userId).map(QuestResponse::from))
    }
}
