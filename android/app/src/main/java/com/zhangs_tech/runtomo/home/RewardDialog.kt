package com.zhangs_tech.runtomo.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.activity.CompletedQuest
import com.zhangs_tech.runtomo.activity.Rewards
import com.zhangs_tech.runtomo.core.formatKm
import com.zhangs_tech.runtomo.tomo.Rarity
import com.zhangs_tech.runtomo.tomo.Species
import com.zhangs_tech.runtomo.tomo.TomoArt
import com.zhangs_tech.runtomo.tomo.TomoPose
import com.zhangs_tech.runtomo.ui.components.CoinAmount
import com.zhangs_tech.runtomo.ui.components.GroundedGlow
import com.zhangs_tech.runtomo.ui.components.Pill
import com.zhangs_tech.runtomo.ui.components.PrimaryButton
import com.zhangs_tech.runtomo.ui.components.RuntomoCard
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Distance Together milestones in meters (UC-06 3a). */
private val MILESTONES_M = listOf(10_000L, 42_195L, 100_000L)

/** The milestone that [rewards] crossed, or null. */
fun crossedMilestoneM(rewards: Rewards): Long? {
    val beforeM = rewards.distanceTogetherM - rewards.distanceM
    return MILESTONES_M.lastOrNull { it in (beforeM + 1)..rewards.distanceTogetherM }
}

/** UC-06 View New Rewards. */
@Composable
fun RewardDialog(rewards: Rewards, species: Species?, onDone: () -> Unit) {
    Dialog(onDismissRequest = onDone) {
        RewardContent(rewards, onDone, species = species)
    }
}

@Composable
fun RewardContent(rewards: Rewards, onDone: () -> Unit, modifier: Modifier = Modifier, species: Species? = null) {
    val scale = remember { Animatable(0.9f) }
    val alpha = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { scale.animateTo(1f, spring(dampingRatio = 0.72f, stiffness = Spring.StiffnessMedium)) }
        launch { alpha.animateTo(1f, tween(180)) }
    }

    RuntomoCard(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                this.alpha = alpha.value
            },
        shape = MaterialTheme.shapes.large,
    ) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            species?.let {
                Box(contentAlignment = Alignment.Center) {
                    GroundedGlow(modifier = Modifier.size(180.dp))
                    TomoArt(it, size = 144.dp, pose = TomoPose.EXCITED)
                }
            }
            val km = formatKm(rewards.distanceM)
            Text(
                text = rewards.tomoNickname?.let { stringResource(R.string.rewards_walked, it, km) }
                    ?: stringResource(R.string.rewards_you_walked, km),
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 21.sp, lineHeight = 27.sp),
                textAlign = TextAlign.Center,
            )
            val showCoins = rewards.coins > 0 || !rewards.coinCapReached
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StaggeredChip(index = 0) {
                    Pill(text = stringResource(R.string.rewards_bond_only, rewards.bond))
                }
                if (showCoins) {
                    StaggeredChip(index = 1) {
                        CoinAmount(
                            text = stringResource(R.string.quest_reward_short, rewards.coins),
                            description = stringResource(R.string.quest_reward, rewards.coins),
                            style = MaterialTheme.typography.labelLarge,
                            iconSize = 18.dp,
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceContainer, CircleShape)
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        )
                    }
                }
            }
            if (rewards.coins == 0 && rewards.coinCapReached) {
                Text(
                    text = stringResource(R.string.rewards_coin_cap),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (rewards.bondLevelAfter > rewards.bondLevelBefore) {
                StaggeredChip(index = if (showCoins) 2 else 1) {
                    LevelUpBanner(rewards.bondLevelAfter)
                }
            }
            if (rewards.tomoNickname != null) {
                Text(
                    text = stringResource(R.string.rewards_distance_together, formatKm(rewards.distanceTogetherM)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            crossedMilestoneM(rewards)?.let {
                MossBanner(stringResource(R.string.rewards_milestone, formatKm(it)))
            }
            rewards.completedQuests.forEach {
                MossBanner(stringResource(R.string.rewards_quest_complete, it.title))
            }
            PrimaryButton(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Text(stringResource(R.string.done))
            }
        }
    }
}

@Composable
private fun StaggeredChip(index: Int, content: @Composable () -> Unit) {
    val alpha = remember { Animatable(0f) }
    val offsetY = remember { Animatable(10f) }
    LaunchedEffect(Unit) {
        delay(index * 70L)
        launch { alpha.animateTo(1f, tween(200)) }
        launch { offsetY.animateTo(0f, tween(220)) }
    }
    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha.value
            translationY = offsetY.value.dp.toPx()
        },
    ) {
        content()
    }
}

@Composable
private fun LevelUpBanner(level: Int) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.rewards_level_up, level),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun MossBanner(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.shapes.small)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(R.drawable.ic_check_circle),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onTertiaryContainer,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RewardContentPreview() {
    val luppy = Species("luppy", "Luppy", Rarity.COMMON, "Runs hot.")
    RuntomoTheme {
        Box(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            RewardContent(
                rewards = Rewards(
                    distanceM = 1200,
                    coins = 120,
                    coinCapReached = false,
                    bond = 12,
                    bondLevelBefore = 1,
                    bondLevelAfter = 2,
                    distanceTogetherM = 10_400,
                    tomoNickname = "Mochi",
                    completedQuests = listOf(CompletedQuest("walk-1km", "Walk or run 1 km", 100)),
                    showRewardScreen = true,
                ),
                onDone = {},
                species = luppy,
            )
        }
    }
}
