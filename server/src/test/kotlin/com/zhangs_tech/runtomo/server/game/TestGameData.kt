package com.zhangs_tech.runtomo.server.game

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

val testTunables = Tunables(
    strideM = 0.75,
    maxStepsPerDay = 100_000,
    coinTiers = listOf(CoinTier(upToM = 5000, coinsPerKm = 100), CoinTier(upToM = 10_000, coinsPerKm = 50)),
    bondPerKm = 10,
    bondLevelThresholds = listOf(0, 50, 150, 300, 500, 750, 1050, 1400, 1800, 2250),
    rewardScreenMinDistanceM = 500,
    sleepStartHour = 23,
    sleepEndHour = 6,
    activeDistanceM = 1000,
)

val testGameData = GameData(
    species = listOf(
        Species("luppy", "Luppy", Rarity.COMMON, starter = true, description = "A puppy"),
        Species("rocky", "Rocky", Rarity.COMMON, starter = true, description = "A tortoise"),
        Species("nova", "Nova", Rarity.EPIC, description = "Not a starter"),
    ),
    tunables = testTunables,
    quests = listOf(
        Quest("walk-1km", "Walk or run 1 km", QuestMetric.DISTANCE, target = 1000, reward = 100),
        Quest("walk-3km", "Walk or run 3 km", QuestMetric.DISTANCE, target = 3000, reward = 200),
        Quest("steps-5000", "Take 5,000 steps", QuestMetric.STEPS, target = 5000, reward = 150),
    ),
)

/** 2026-11-13 20:00 UTC, which is already 2026-11-14 03:00 in UTC+7. */
val testClock: Clock = Clock.fixed(Instant.parse("2026-11-13T20:00:00Z"), ZoneOffset.UTC)

/** 2026-11-14 12:00 in UTC+7, a daytime hour. */
val testDaytimeClock: Clock = Clock.fixed(Instant.parse("2026-11-14T05:00:00Z"), ZoneOffset.UTC)
