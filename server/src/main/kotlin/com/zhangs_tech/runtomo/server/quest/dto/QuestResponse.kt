package com.zhangs_tech.runtomo.server.quest.dto

import com.zhangs_tech.runtomo.server.game.QuestMetric
import com.zhangs_tech.runtomo.server.quest.QuestProgress
import com.zhangs_tech.runtomo.server.quest.QuestStatus

data class QuestResponse(
    val code: String,
    val title: String,
    val metric: QuestMetric,
    val target: Int,
    val progress: Int,
    val reward: Int,
    val status: QuestStatus,
) {
    companion object {
        fun from(questProgress: QuestProgress) = with(questProgress) {
            QuestResponse(quest.code, quest.title, quest.metric, quest.target, progress, quest.reward, status)
        }
    }
}
