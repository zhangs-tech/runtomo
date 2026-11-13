package com.zhangs_tech.runtomo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Material 3 draws selected chips and the nav indicator with secondaryContainer, so the
// secondary roles carry the ember tint. Moss (success) is tertiary.
private val LightColorScheme = lightColorScheme(
    primary = Ember,
    onPrimary = Paper,
    primaryContainer = EmberTint,
    onPrimaryContainer = EmberDeep,
    inversePrimary = EmberLight,
    secondary = Bark,
    onSecondary = Paper,
    secondaryContainer = EmberTint,
    onSecondaryContainer = EmberDeep,
    tertiary = Moss,
    onTertiary = Paper,
    tertiaryContainer = MossTint,
    onTertiaryContainer = MossDeep,
    background = Paper,
    onBackground = Ink,
    surface = Paper,
    onSurface = Ink,
    surfaceVariant = PaperHigh,
    onSurfaceVariant = InkSoft,
    surfaceTint = Ember,
    inverseSurface = Ink,
    inverseOnSurface = Paper,
    outline = InkFaint,
    outlineVariant = PaperEdge,
    surfaceBright = Paper,
    surfaceDim = PaperHigh,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = PaperLow,
    surfaceContainer = PaperSunk,
    surfaceContainerHigh = PaperHigh,
    surfaceContainerHighest = PaperEdge,
)

// Cards use surfaceContainerLowest. In the dark scheme it is a lifted brown, not the darkest
// color, so cards stand out from the background in both schemes.
private val DarkColorScheme = darkColorScheme(
    primary = EmberLight,
    onPrimary = EmberDeep,
    primaryContainer = EmberNightWash,
    onPrimaryContainer = EmberTint,
    inversePrimary = Ember,
    secondary = BarkLight,
    onSecondary = EmberDeep,
    secondaryContainer = EmberNight,
    onSecondaryContainer = EmberTint,
    tertiary = MossLight,
    onTertiary = MossDeep,
    tertiaryContainer = MossNight,
    onTertiaryContainer = MossTint,
    errorContainer = ErrorNightWash,
    onErrorContainer = ErrorNightInk,
    background = Night,
    onBackground = NightInk,
    surface = Night,
    onSurface = NightInk,
    surfaceVariant = NightEdge,
    onSurfaceVariant = NightInkSoft,
    surfaceTint = EmberLight,
    inverseSurface = NightInk,
    inverseOnSurface = Ink,
    outline = NightInkFaint,
    outlineVariant = NightEdge,
    surfaceBright = NightHigh,
    surfaceDim = Night,
    surfaceContainerLowest = NightCard,
    surfaceContainerLow = NightLow,
    surfaceContainer = NightSunk,
    surfaceContainerHigh = NightHigh,
    surfaceContainerHighest = NightEdge,
)

private val RuntomoShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun RuntomoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        shapes = RuntomoShapes,
        content = content,
    )
}
