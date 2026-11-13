package com.zhangs_tech.runtomo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zhangs_tech.runtomo.R

/** Rounded display face for titles and big numbers. It matches the soft Tomo art. */
val Fredoka = FontFamily(
    Font(R.font.fredoka_medium, FontWeight.Medium),
    Font(R.font.fredoka_semibold, FontWeight.SemiBold),
)

val Nunito = FontFamily(
    Font(R.font.nunito_medium, FontWeight.Medium),
    Font(R.font.nunito_semibold, FontWeight.SemiBold),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_extrabold, FontWeight.ExtraBold),
)

private fun display(size: Int, lineHeight: Int) = TextStyle(
    fontFamily = Fredoka,
    fontWeight = FontWeight.SemiBold,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
)

private fun nunito(size: Int, lineHeight: Int, weight: FontWeight, letterSpacing: Double = 0.0) = TextStyle(
    fontFamily = Nunito,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = letterSpacing.sp,
)

val Typography = Typography(
    displayLarge = display(57, 64),
    displayMedium = display(45, 52),
    displaySmall = display(36, 44),
    headlineLarge = display(32, 40),
    headlineMedium = display(28, 36),
    headlineSmall = display(24, 32),
    titleLarge = display(22, 28),
    titleMedium = nunito(16, 24, FontWeight.ExtraBold, 0.1),
    titleSmall = nunito(14, 20, FontWeight.Bold, 0.1),
    bodyLarge = nunito(16, 24, FontWeight.Medium, 0.2),
    bodyMedium = nunito(14, 20, FontWeight.Medium, 0.2),
    bodySmall = nunito(12, 16, FontWeight.SemiBold, 0.3),
    labelLarge = nunito(15, 20, FontWeight.ExtraBold, 0.2),
    labelMedium = nunito(12, 16, FontWeight.Bold, 0.4),
    labelSmall = nunito(11, 16, FontWeight.Bold, 0.4),
)
