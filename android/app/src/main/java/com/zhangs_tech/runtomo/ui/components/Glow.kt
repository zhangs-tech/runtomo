package com.zhangs_tech.runtomo.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.luminance
import com.zhangs_tech.runtomo.ui.theme.Sun

/** A soft radial glow to put behind a Tomo or an egg. */
@Composable
fun Glow(modifier: Modifier = Modifier, color: Color = Sun) {
    val peakAlpha = glowPeakAlpha()
    Canvas(modifier = modifier) { drawGlow(color, peakAlpha) }
}

/** A [Glow] with a faint ground shadow, for a Tomo that stands. */
@Composable
fun GroundedGlow(modifier: Modifier = Modifier, color: Color = Sun) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val peakAlpha = glowPeakAlpha(isDark)
    val ground = if (isDark) Color.Black.copy(alpha = 0.28f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    Canvas(modifier = modifier) {
        drawGlow(color, peakAlpha)
        val groundWidth = size.width * 0.5f
        drawOval(
            color = ground,
            topLeft = Offset((size.width - groundWidth) / 2, size.height * 0.80f),
            size = Size(groundWidth, size.height * 0.07f),
        )
    }
}

@Composable
private fun glowPeakAlpha(isDark: Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f): Float =
    if (isDark) 0.16f else 0.42f

private fun DrawScope.drawGlow(color: Color, peakAlpha: Float) {
    drawCircle(
        Brush.radialGradient(
            0f to color.copy(alpha = peakAlpha),
            0.55f to color.copy(alpha = peakAlpha * 0.30f),
            0.94f to color.copy(alpha = 0f),
        ),
    )
}
