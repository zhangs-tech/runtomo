package com.zhangs_tech.runtomo.server.game

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

val testGameData = GameData(
    species = listOf(
        Species("luppy", "Luppy", Rarity.COMMON, starter = true, description = "A puppy"),
        Species("rocky", "Rocky", Rarity.COMMON, starter = true, description = "A tortoise"),
        Species("nova", "Nova", Rarity.EPIC, description = "Not a starter"),
    ),
)

/** 2026-11-13 20:00 UTC, which is already 2026-11-14 in UTC+7. */
val testClock: Clock = Clock.fixed(Instant.parse("2026-11-13T20:00:00Z"), ZoneOffset.UTC)
