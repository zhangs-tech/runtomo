package com.zhangs_tech.runtomo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zhangs_tech.runtomo.ui.theme.Sun
import com.zhangs_tech.runtomo.ui.theme.SunDeep

val SunBrush = Brush.horizontalGradient(listOf(Sun, SunDeep))

/** The fraction to draw. It grows from 0 the first time it shows, then follows [target]. */
@Composable
fun animatedProgress(target: Float): Float {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(target) {
        progress.animateTo(target.coerceIn(0f, 1f), tween(PROGRESS_MILLIS, easing = FastOutSlowInEasing))
    }
    return progress.value
}

/** A thick round-ended progress bar. */
@Composable
fun GrowingProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
    brush: Brush = SunBrush,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainer,
) {
    val shown = animatedProgress(progress)
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(progress.coerceIn(0f, 1f), 0f..1f) },
    ) {
        val radius = CornerRadius(size.height / 2)
        drawRoundRect(trackColor, cornerRadius = radius)
        if (shown > 0f) {
            val width = (size.width * shown).coerceAtLeast(size.height)
            drawRoundRect(brush, topLeft = Offset.Zero, size = Size(width, size.height), cornerRadius = radius)
        }
    }
}

private const val PROGRESS_MILLIS = 700
