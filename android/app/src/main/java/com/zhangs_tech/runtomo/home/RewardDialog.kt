package com.zhangs_tech.runtomo.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.activity.CompletedQuest
import com.zhangs_tech.runtomo.activity.Rewards
import com.zhangs_tech.runtomo.core.formatKm
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme

/** Distance Together milestones in meters (UC-06 3a). */
private val MILESTONES_M = listOf(10_000L, 42_195L, 100_000L)

/** The milestone that [rewards] crossed, or null. */
fun crossedMilestoneM(rewards: Rewards): Long? {
    val beforeM = rewards.distanceTogetherM - rewards.distanceM
    return MILESTONES_M.lastOrNull { it in (beforeM + 1)..rewards.distanceTogetherM }
}

/** UC-06 View New Rewards. */
@Composable
fun RewardDialog(rewards: Rewards, onDone: () -> Unit) {
    Dialog(onDismissRequest = onDone) {
        RewardContent(rewards, onDone)
    }
}

@Composable
fun RewardContent(rewards: Rewards, onDone: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val km = formatKm(rewards.distanceM)
            Text(
                text = rewards.tomoNickname?.let { stringResource(R.string.rewards_walked, it, km) }
                    ?: stringResource(R.string.rewards_you_walked, km),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (rewards.coins > 0 || !rewards.coinCapReached) {
                    stringResource(R.string.rewards_earned, rewards.bond, rewards.coins)
                } else {
                    stringResource(R.string.rewards_bond_only, rewards.bond)
                },
                style = MaterialTheme.typography.titleMedium,
            )
            if (rewards.coins == 0 && rewards.coinCapReached) {
                Text(stringResource(R.string.rewards_coin_cap), textAlign = TextAlign.Center)
            }
            if (rewards.bondLevelAfter > rewards.bondLevelBefore) {
                Text(
                    text = stringResource(R.string.rewards_level_up, rewards.bondLevelAfter),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (rewards.tomoNickname != null) {
                Text(stringResource(R.string.rewards_distance_together, formatKm(rewards.distanceTogetherM)))
            }
            crossedMilestoneM(rewards)?.let {
                Text(
                    text = stringResource(R.string.rewards_milestone, formatKm(it)),
                    fontWeight = FontWeight.Bold,
                )
            }
            rewards.completedQuests.forEach {
                Text(stringResource(R.string.rewards_quest_complete, it.title))
            }
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.done))
            }
        }
    }
}

@Preview
@Composable
private fun RewardContentPreview() {
    RuntomoTheme {
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
        )
    }
}
