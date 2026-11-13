package com.zhangs_tech.runtomo.server.game

enum class QuestMetric { DISTANCE, STEPS }

data class Quest(
    val code: String,
    val title: String,
    val metric: QuestMetric,
    val target: Int,
    val reward: Int,
) {
    fun progress(steps: Int, distanceM: Int): Int = when (metric) {
        QuestMetric.DISTANCE -> distanceM
        QuestMetric.STEPS -> steps
    }

    fun isComplete(steps: Int, distanceM: Int): Boolean = progress(steps, distanceM) >= target
}
