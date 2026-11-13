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
    activeDistanceM = 1000,
    speechBubbleOneIn = 3,
    feedsPerTomoPerDay = 3,
    maxFriends = 2,
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
    banners = listOf(
        Banner(
            code = "standard",
            name = "Standard Egg",
            cost = 1000,
            pityThreshold = 3,
            duplicateCoins = 200,
            species = listOf(BannerSpecies("luppy", 900), BannerSpecies("nova", 100)),
        ),
        Banner("commons", "Commons", cost = 1000, pityThreshold = 3, duplicateCoins = 200, species = listOf(BannerSpecies("luppy", 1))),
    ),
    foods = listOf(Food("banana", "Banana", cost = 50, bond = 3), Food("bento", "Bento", cost = 150, bond = 10)),
)

/** 2026-11-13 20:00 UTC, which is already 2026-11-14 03:00 in UTC+7. */
val testClock: Clock = Clock.fixed(Instant.parse("2026-11-13T20:00:00Z"), ZoneOffset.UTC)

/** 2026-11-14 12:00 in UTC+7, a daytime hour. */
val testDaytimeClock: Clock = Clock.fixed(Instant.parse("2026-11-14T05:00:00Z"), ZoneOffset.UTC)
