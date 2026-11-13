package com.zhangs_tech.runtomo.server.quest

import com.zhangs_tech.runtomo.server.game.Quest

enum class QuestStatus { IN_PROGRESS, CLAIMABLE, CLAIMED }

data class QuestProgress(val quest: Quest, val progress: Int, val status: QuestStatus)
