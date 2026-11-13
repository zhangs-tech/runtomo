package com.zhangs_tech.runtomo

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/** 2026-11-14 12:00 in UTC+7. */
val testClock: Clock = Clock.fixed(Instant.parse("2026-11-14T05:00:00Z"), ZoneOffset.UTC)
