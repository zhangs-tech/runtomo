package com.zhangs_tech.runtomo.core

import java.util.Locale
import kotlin.math.floor

/** UC-03 BR-3. The server sends the stride, so the app only uses it to show distance while offline. */
fun distanceM(steps: Int, strideM: Double): Int = floor(steps * strideM).toInt()

/** Formats meters as kilometers with one decimal, for example 2.8. Rounds down, so 4.99 km never shows as 5.0. */
fun formatKm(meters: Long): String = String.format(Locale.US, "%.1f", floor(meters / 100.0) / 10)

fun formatKm(meters: Int): String = formatKm(meters.toLong())
