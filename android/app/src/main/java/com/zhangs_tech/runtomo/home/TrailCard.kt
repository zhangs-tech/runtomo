package com.zhangs_tech.runtomo.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.core.formatKm
import com.zhangs_tech.runtomo.tomo.Species
import com.zhangs_tech.runtomo.tomo.TomoArt
import com.zhangs_tech.runtomo.tomo.TomoPose
import com.zhangs_tech.runtomo.tomo.activityFacesRight
import com.zhangs_tech.runtomo.ui.components.RuntomoCard
import com.zhangs_tech.runtomo.ui.components.SunBrush
import com.zhangs_tech.runtomo.ui.components.animatedProgress
import com.zhangs_tech.runtomo.ui.theme.Fredoka
import java.text.NumberFormat

/** Today's distance toward the daily goal, drawn as a trail the Tomo runs along. */
@Composable
fun TrailCard(home: HomeData) {
    RuntomoCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val km = formatKm(home.today.distanceM)
            val full = stringResource(R.string.today_distance, km, formatKm(home.dailyGoalM))
            val bigStyle = SpanStyle(fontFamily = Fredoka, fontSize = 40.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(
                text = buildAnnotatedString {
                    append(full)
                    val start = full.indexOf(km)
                    if (start >= 0) addStyle(bigStyle, start, start + km.length)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Trail(
                progress = home.today.distanceM.toFloat() / home.dailyGoalM,
                species = home.tomo?.species,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(
                    painterResource(R.drawable.ic_footprint),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.today_steps, NumberFormat.getInstance().format(home.today.steps)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun Trail(progress: Float, species: Species?) {
    val target = progress.coerceIn(0f, 1f)
    val shown = animatedProgress(target)
    val trackColor = MaterialTheme.colorScheme.surfaceContainer
    val goalColor = MaterialTheme.colorScheme.outline
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(TrailHeight)
            .semantics { progressBarRangeInfo = ProgressBarRangeInfo(target, 0f..1f) },
    ) {
        val runnerTravel = maxWidth - RunnerSize
        Canvas(modifier = Modifier.fillMaxWidth().height(TrackHeight).align(Alignment.BottomStart).offset(y = -TrackLift)) {
            val radius = CornerRadius(size.height / 2)
            drawRoundRect(trackColor, cornerRadius = radius)
            // The unwalked part of the trail is a dashed path to the goal.
            val midY = size.height / 2
            drawLine(
                color = goalColor,
                start = Offset(size.width * shown, midY),
                end = Offset(size.width - size.height / 2, midY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 6.dp.toPx())),
            )
            if (shown > 0f) {
                val runnerCenter = RunnerSize.toPx() / 2 + runnerTravel.toPx() * shown
                val width = runnerCenter.coerceAtLeast(size.height)
                drawRoundRect(SunBrush, size = Size(width, size.height), cornerRadius = radius)
            }
        }
        if (species != null) {
            val mirror = if (species.activityFacesRight) 1f else -1f
            Box(modifier = Modifier.offset(x = runnerTravel * shown).align(Alignment.BottomStart)) {
                TomoArt(
                    species = species,
                    size = RunnerSize,
                    pose = TomoPose.ACTIVITY,
                    modifier = Modifier.graphicsLayer { scaleX = mirror },
                )
            }
        }
    }
}

private val RunnerSize = 52.dp
private val TrackHeight = 12.dp
private val TrackLift = 6.dp
private val TrailHeight = 56.dp
