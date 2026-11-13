package com.zhangs_tech.runtomo.quest

import kotlinx.serialization.Serializable

enum class QuestMetric { DISTANCE, STEPS }

enum class QuestStatus { IN_PROGRESS, CLAIMABLE, CLAIMED }

@Serializable
data class Quest(
    val code: String,
    val title: String,
    val metric: QuestMetric,
    val target: Int,
    val progress: Int,
    val reward: Int,
    val status: QuestStatus,
) {
    /** Progress for new totals. The server stays the judge when the Player claims. */
    fun withTotals(steps: Int, distanceM: Int): Quest {
        val newProgress = maxOf(progress, if (metric == QuestMetric.DISTANCE) distanceM else steps)
        val newStatus = if (status == QuestStatus.IN_PROGRESS && newProgress >= target) QuestStatus.CLAIMABLE else status
        return copy(progress = newProgress, status = newStatus)
    }
}
