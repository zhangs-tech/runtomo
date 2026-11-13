package com.zhangs_tech.runtomo.server.activity

import com.zhangs_tech.runtomo.server.activity.dto.SyncRequest
import com.zhangs_tech.runtomo.server.activity.dto.SyncResponse
import com.zhangs_tech.runtomo.server.auth.CurrentUserId
import com.zhangs_tech.runtomo.server.game.GameData
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
class ActivityController(private val activityService: ActivityService, private val gameData: GameData) {
    @PutMapping("/me/days/{date}")
    fun sync(
        @CurrentUserId userId: UUID,
        @PathVariable date: LocalDate,
        @RequestBody request: SyncRequest,
    ): SyncResponse = SyncResponse.from(activityService.sync(userId, date, request.steps), gameData.tunables.strideM)
}
