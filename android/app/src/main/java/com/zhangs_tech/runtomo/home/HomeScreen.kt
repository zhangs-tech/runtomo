package com.zhangs_tech.runtomo.home

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.core.formatKm
import com.zhangs_tech.runtomo.quest.Quest
import com.zhangs_tech.runtomo.quest.QuestMetric
import com.zhangs_tech.runtomo.quest.QuestStatus
import com.zhangs_tech.runtomo.tomo.Mood
import com.zhangs_tech.runtomo.tomo.Rarity
import com.zhangs_tech.runtomo.tomo.Species
import com.zhangs_tech.runtomo.tomo.Tomo
import com.zhangs_tech.runtomo.tomo.TomoArt
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme
import java.text.NumberFormat

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract(),
        viewModel::onStepsPermissionResult,
    )
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

    HomeContent(
        uiState = uiState,
        actions = HomeActions(
            onRefresh = viewModel::refresh,
            onAllowSteps = { permissionLauncher.launch(viewModel.stepsPermissions) },
            onOpenHealthConnect = {
                context.startActivity(Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS))
            },
            onClaimQuest = viewModel::claimQuest,
            onDismissRewards = viewModel::dismissRewards,
            onDismissMissedYou = viewModel::dismissMissedYou,
            onDismissError = viewModel::dismissError,
            onSignOut = viewModel::signOut,
        ),
        modifier = modifier,
    )
}

data class HomeActions(
    val onRefresh: () -> Unit = {},
    val onAllowSteps: () -> Unit = {},
    val onOpenHealthConnect: () -> Unit = {},
    val onClaimQuest: (String) -> Unit = {},
    val onDismissRewards: () -> Unit = {},
    val onDismissMissedYou: () -> Unit = {},
    val onDismissError: () -> Unit = {},
    val onSignOut: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(uiState: HomeUiState, actions: HomeActions, modifier: Modifier = Modifier) {
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = uiState.error?.takeIf { uiState.home != null }?.let { stringResource(it.messageRes) }
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            actions.onDismissError()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_title)) },
                actions = {
                    uiState.home?.let {
                        Text(
                            text = stringResource(R.string.coin_balance, NumberFormat.getInstance().format(it.coinBalance)),
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                    OverflowMenu(onSignOut = actions.onSignOut)
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = actions.onRefresh,
            modifier = Modifier.fillMaxSize().padding(innerPadding),
        ) {
            val home = uiState.home
            when {
                home != null -> HomeBody(home, uiState, actions)
                uiState.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                else -> LoadFailed(uiState.error, actions.onRefresh)
            }
        }
    }

    uiState.rewards?.let { RewardDialog(it, actions.onDismissRewards) }
}

@Composable
private fun OverflowMenu(onSignOut: () -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(painterResource(R.drawable.ic_more_vert), contentDescription = stringResource(R.string.more_options))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.sign_out)) },
                onClick = {
                    expanded = false
                    onSignOut()
                },
            )
        }
    }
}

@Composable
private fun HomeBody(home: HomeData, uiState: HomeUiState, actions: HomeActions) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (uiState.isOffline) {
            AssistChip(onClick = actions.onRefresh, label = { Text(stringResource(R.string.offline_label)) })
        }
        home.tomo?.let { tomo ->
            if (uiState.showMissedYou) MissedYouCard(tomo.nickname, actions.onDismissMissedYou)
            TomoCard(tomo, home.mood)
        }
        TodayCard(home)
        when (uiState.stepsAccess) {
            StepsAccess.NotGranted, StepsAccess.Refused -> StepsAccessCard(uiState.stepsAccess, actions)
            StepsAccess.Granted -> if (!uiState.canReadInBackground) BackgroundNoteCard(actions.onOpenHealthConnect)
            StepsAccess.Unknown -> Unit
        }
        QuestsCard(home.quests, uiState.claimingQuestCode, uiState.isOffline, actions.onClaimQuest)
    }
}

@Composable
private fun MissedYouCard(nickname: String, onDismiss: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.missed_you, nickname), modifier = Modifier.weight(1f))
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dismiss)) }
        }
    }
}

@Composable
private fun TomoCard(tomo: Tomo, mood: Mood) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TomoArt(tomo.species, size = 160.dp)
        Text(tomo.nickname, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = stringResource(R.string.tomo_status, stringResource(mood.labelRes), tomo.bondLevel),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun TodayCard(home: HomeData) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.today_distance, formatKm(home.today.distanceM), formatKm(home.dailyGoalM)),
                style = MaterialTheme.typography.headlineSmall,
            )
            LinearProgressIndicator(
                progress = { (home.today.distanceM.toFloat() / home.dailyGoalM).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(stringResource(R.string.today_steps, NumberFormat.getInstance().format(home.today.steps)))
        }
    }
}

@Composable
private fun StepsAccessCard(stepsAccess: StepsAccess, actions: HomeActions) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.steps_access_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.steps_access_body))
            if (stepsAccess == StepsAccess.Refused) {
                Text(stringResource(R.string.steps_access_refused))
                OutlinedButton(onClick = actions.onOpenHealthConnect) {
                    Text(stringResource(R.string.open_health_connect))
                }
            } else {
                Button(onClick = actions.onAllowSteps) { Text(stringResource(R.string.steps_access_allow)) }
            }
        }
    }
}

@Composable
private fun BackgroundNoteCard(onOpenHealthConnect: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.background_note))
            TextButton(onClick = onOpenHealthConnect) { Text(stringResource(R.string.open_health_connect)) }
        }
    }
}

@Composable
private fun QuestsCard(quests: List<Quest>, claimingQuestCode: String?, isOffline: Boolean, onClaim: (String) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.quests_title), style = MaterialTheme.typography.titleMedium)
            quests.forEach { quest ->
                QuestRow(quest, isClaiming = quest.code == claimingQuestCode, canClaim = !isOffline, onClaim = onClaim)
            }
        }
    }
}

@Composable
private fun QuestRow(quest: Quest, isClaiming: Boolean, canClaim: Boolean, onClaim: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(quest.title)
            LinearProgressIndicator(
                progress = { (quest.progress.toFloat() / quest.target).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = stringResource(R.string.quest_reward, quest.reward),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        when (quest.status) {
            QuestStatus.CLAIMED -> Text(
                text = stringResource(R.string.quest_claimed),
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
            )
            QuestStatus.CLAIMABLE -> FilledTonalButton(
                onClick = { onClaim(quest.code) },
                enabled = canClaim && !isClaiming,
            ) { Text(stringResource(R.string.quest_claim)) }
            QuestStatus.IN_PROGRESS -> Unit
        }
    }
}

@Composable
private fun LoadFailed(error: HomeError?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(error?.messageRes ?: R.string.error_generic), textAlign = TextAlign.Center)
        OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@get:StringRes
private val HomeError.messageRes: Int
    get() = when (this) {
        HomeError.NoConnection -> R.string.error_no_connection
        HomeError.Failed -> R.string.error_generic
    }

@get:StringRes
private val Mood.labelRes: Int
    get() = when (this) {
        Mood.SLEEPY -> R.string.mood_sleepy
        Mood.EXCITED -> R.string.mood_excited
        Mood.HAPPY -> R.string.mood_happy
        Mood.RELAXED -> R.string.mood_relaxed
        Mood.BORED -> R.string.mood_bored
    }

internal val previewHome = HomeData(
    displayName = "Kai",
    tomo = Tomo(
        id = "t1",
        species = Species("luppy", "Luppy", Rarity.COMMON, "Luppy runs hot."),
        nickname = "Mochi",
        bondPoints = 28,
        bondLevel = 1,
        distanceTogetherM = 2800,
        birthday = "2026-11-14",
    ),
    mood = Mood.HAPPY,
    today = Today("2026-11-14", 3734, 2800),
    dailyGoalM = 5000,
    strideM = 0.75,
    coinBalance = 280,
    quests = listOf(
        Quest("walk-1km", "Walk or run 1 km", QuestMetric.DISTANCE, 1000, 2800, 100, QuestStatus.CLAIMABLE),
        Quest("walk-3km", "Walk or run 3 km", QuestMetric.DISTANCE, 3000, 2800, 200, QuestStatus.IN_PROGRESS),
        Quest("steps-5000", "Take 5,000 steps", QuestMetric.STEPS, 5000, 3734, 150, QuestStatus.IN_PROGRESS),
    ),
    lastSyncDate = "2026-11-14",
)

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    RuntomoTheme {
        HomeContent(
            uiState = HomeUiState(isLoading = false, home = previewHome, stepsAccess = StepsAccess.Granted),
            actions = HomeActions(),
        )
    }
}
