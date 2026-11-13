package com.zhangs_tech.runtomo.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.tomo.Mood
import com.zhangs_tech.runtomo.tomo.Tomo
import com.zhangs_tech.runtomo.tomo.TomoArt
import com.zhangs_tech.runtomo.tomo.TomoProfileDialog
import kotlinx.coroutines.delay
import java.text.NumberFormat

/** The equipped Tomo on the Home Screen. The Player can tap it (UC-09) or open its profile card. */
@Composable
fun TomoCard(tomo: Tomo, mood: Mood, todaySteps: Int, speechBubbleOneIn: Int, onFeed: () -> Unit, canFeed: Boolean) {
    val reactions = remember { TomoReactions() }
    var reaction by remember { mutableStateOf<TomoReaction?>(null) }
    var tapCount by remember { mutableIntStateOf(0) }
    var showProfile by rememberSaveable { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }

    LaunchedEffect(tapCount) {
        if (tapCount == 0) return@LaunchedEffect
        when (reaction) {
            TomoReaction.TooMuch -> repeat(3) {
                rotation.animateTo(-10f, tween(60))
                rotation.animateTo(10f, tween(60))
            }
            is TomoReaction.Normal -> {
                scale.animateTo(1.15f, tween(120))
                scale.animateTo(1f, tween(160))
            }
            else -> Unit
        }
        rotation.snapTo(0f)
        delay(BUBBLE_MILLIS)
        reaction = null
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val bubble = reaction?.let { bubbleText(it, tomo.nickname, todaySteps) }
        AnimatedVisibility(visible = bubble != null, enter = fadeIn(), exit = fadeOut()) {
            Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 2.dp, shadowElevation = 2.dp) {
                Text(
                    text = bubble.orEmpty(),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
        TomoArt(
            species = tomo.species,
            size = 160.dp,
            modifier = Modifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    rotationZ = rotation.value
                }
                .clickable(onClickLabel = tomo.nickname, role = Role.Button) {
                    reaction = reactions.onTap(mood, todaySteps, speechBubbleOneIn)
                    tapCount++
                },
        )
        Text(tomo.nickname, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = stringResource(R.string.tomo_status, stringResource(mood.labelRes), tomo.bondLevel),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // UC-18 needs the server, so Feed waits until the app is online.
            FilledTonalButton(onClick = onFeed, enabled = canFeed) { Text(stringResource(R.string.feed)) }
            TextButton(onClick = { showProfile = true }, modifier = Modifier.heightIn(min = 48.dp)) {
                Text(stringResource(R.string.view_profile))
            }
        }
    }

    if (showProfile) {
        TomoProfileDialog(tomo, onDismiss = { showProfile = false }) {
            TextButton(onClick = { showProfile = false }) { Text(stringResource(R.string.close)) }
        }
    }
}

@Composable
private fun bubbleText(reaction: TomoReaction, nickname: String, todaySteps: Int): String? = when (reaction) {
    TomoReaction.TooMuch -> stringResource(R.string.tomo_too_much, nickname)
    is TomoReaction.Normal -> when (reaction.speech) {
        SpeechLine.WALK -> stringResource(R.string.speech_walk)
        SpeechLine.STEPS_TODAY -> stringResource(R.string.speech_steps_today, NumberFormat.getInstance().format(todaySteps))
        SpeechLine.FUN -> stringResource(R.string.speech_fun)
        SpeechLine.READY -> stringResource(R.string.speech_ready)
        null -> null
    }
}

val Mood.labelRes: Int
    get() = when (this) {
        Mood.EXCITED -> R.string.mood_excited
        Mood.HAPPY -> R.string.mood_happy
        Mood.RELAXED -> R.string.mood_relaxed
    }

private const val BUBBLE_MILLIS = 2_500L
