package com.zhangs_tech.runtomo.social

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.core.formatKm
import com.zhangs_tech.runtomo.tomo.Rarity
import com.zhangs_tech.runtomo.tomo.Species
import com.zhangs_tech.runtomo.tomo.SpeciesSilhouette
import com.zhangs_tech.runtomo.tomo.TomoArt
import com.zhangs_tech.runtomo.tomo.TomoPose
import com.zhangs_tech.runtomo.ui.components.CompactButton
import com.zhangs_tech.runtomo.ui.components.GroundedGlow
import com.zhangs_tech.runtomo.ui.components.PrimaryButton
import com.zhangs_tech.runtomo.ui.components.RuntomoCard
import com.zhangs_tech.runtomo.ui.components.RuntomoFilterChip
import com.zhangs_tech.runtomo.ui.components.SecondaryButton
import com.zhangs_tech.runtomo.ui.components.TonalButton
import com.zhangs_tech.runtomo.ui.components.journalTextFieldColors
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme
import kotlinx.coroutines.launch

private enum class SocialSection(val labelRes: Int) {
    LEADERBOARD(R.string.social_leaderboard),
    FRIENDS(R.string.social_friends),
    ADD_FRIEND(R.string.social_add_friend),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(modifier: Modifier = Modifier) {
    var section by rememberSaveable { mutableIntStateOf(SocialSection.LEADERBOARD.ordinal) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.social_title), style = MaterialTheme.typography.headlineSmall) },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            PrimaryTabRow(
                selectedTabIndex = section,
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                SocialSection.entries.forEach {
                    val selected = section == it.ordinal
                    Tab(
                        selected = selected,
                        onClick = { section = it.ordinal },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        text = { Text(stringResource(it.labelRes)) },
                    )
                }
            }
            when (SocialSection.entries[section]) {
                SocialSection.LEADERBOARD -> LeaderboardSection(onAddFriend = { section = SocialSection.ADD_FRIEND.ordinal })
                SocialSection.FRIENDS -> FriendsSection(onAddFriend = { section = SocialSection.ADD_FRIEND.ordinal })
                SocialSection.ADD_FRIEND -> AddFriendSection()
            }
        }
    }
}

@Composable
private fun LeaderboardSection(onAddFriend: () -> Unit, viewModel: LeaderboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }
    LeaderboardContent(uiState, onPeriodChange = viewModel::setPeriod, onRetry = viewModel::load, onAddFriend = onAddFriend)
}

@Composable
fun LeaderboardContent(
    uiState: LeaderboardUiState,
    onPeriodChange: (LeaderboardPeriod) -> Unit,
    onRetry: () -> Unit,
    onAddFriend: () -> Unit,
) {
    var selected by remember { mutableStateOf<LeaderboardRow?>(null) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LeaderboardPeriod.entries.forEach { period ->
                RuntomoFilterChip(
                    selected = uiState.period == period,
                    onClick = { onPeriodChange(period) },
                    label = stringResource(period.labelRes),
                )
            }
        }
        val board = uiState.leaderboard
        when {
            board != null && board.rows.size <= 1 -> EmptySocialCard(
                message = stringResource(R.string.leaderboard_empty),
                onAddFriend = onAddFriend,
            )
            board != null -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.shapes.small)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        painterResource(R.drawable.ic_walk),
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = if (board.ahead != null && board.gapM != null) {
                            stringResource(R.string.leaderboard_behind, formatKm(board.gapM), board.ahead.displayName)
                        } else {
                            stringResource(R.string.leaderboard_first)
                        },
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                board.rows.forEach { row -> LeaderboardRowCard(row, onClick = { selected = row }) }
            }
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            else -> ErrorWithRetry(uiState.error, onRetry)
        }
    }
    selected?.let { row ->
        PlayerCardDialog(
            player = row.player,
            details = listOf(stringResource(R.string.leaderboard_km, formatKm(row.distanceM))),
            onDismiss = { selected = null },
        )
    }
}

@Composable
private fun LeaderboardRowCard(row: LeaderboardRow, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    RuntomoCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(role = Role.Button, onClick = onClick),
        containerColor = if (row.isMe) colors.primaryContainer else colors.surfaceContainerLowest,
        borderColor = if (row.isMe) colors.primary else colors.outlineVariant,
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "#${row.rank}",
                style = MaterialTheme.typography.titleLarge,
                color = if (row.isMe) colors.primary else colors.onSurfaceVariant,
                modifier = Modifier.widthIn(min = 32.dp),
            )
            PlayerTomoArt(row.player, size = 44)
            Text(
                text = if (row.isMe) stringResource(R.string.leaderboard_you, row.player.displayName) else row.player.displayName,
                style = if (row.isMe) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge,
                color = if (row.isMe) colors.onPrimaryContainer else colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.leaderboard_km, formatKm(row.distanceM)),
                style = MaterialTheme.typography.titleLarge,
                color = if (row.isMe) colors.onPrimaryContainer else colors.onSurface,
            )
        }
    }
}

@Composable
private fun EmptySocialCard(message: String, onAddFriend: () -> Unit) {
    RuntomoCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ic_social_filled),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            PrimaryButton(onClick = onAddFriend) { Text(stringResource(R.string.social_add_friend)) }
        }
    }
}

@Composable
private fun FriendsSection(onAddFriend: () -> Unit, viewModel: FriendsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }
    FriendsContent(
        uiState = uiState,
        onRetry = viewModel::load,
        onAccept = viewModel::accept,
        onDecline = viewModel::decline,
        onSelect = viewModel::select,
        onRemove = viewModel::remove,
        onAddFriend = onAddFriend,
    )
}

@Composable
fun FriendsContent(
    uiState: FriendsUiState,
    onRetry: () -> Unit,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit,
    onSelect: (Friend?) -> Unit,
    onRemove: (Friend) -> Unit,
    onAddFriend: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (uiState.limitReached) Text(stringResource(R.string.friends_limit), color = MaterialTheme.colorScheme.error)
        if (uiState.requests.isNotEmpty()) {
            Text(stringResource(R.string.friends_requests), style = MaterialTheme.typography.titleMedium)
            uiState.requests.forEach { request ->
                RuntomoCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlayerTomoArt(request.from, size = 44)
                        Text(request.from.displayName, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        TextButton(onClick = { onDecline(request.id) }, enabled = uiState.busyId == null) {
                            Text(stringResource(R.string.friends_decline))
                        }
                        CompactButton(onClick = { onAccept(request.id) }, enabled = uiState.busyId == null) {
                            Text(stringResource(R.string.friends_accept))
                        }
                    }
                }
            }
        }
        if (uiState.friends.isNotEmpty() || uiState.requests.isNotEmpty() || uiState.isLoading || uiState.error != null) {
            Text(stringResource(R.string.social_friends), style = MaterialTheme.typography.titleMedium)
        }
        when {
            uiState.friends.isNotEmpty() -> uiState.friends.forEach { friend ->
                RuntomoCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .clickable(role = Role.Button) { onSelect(friend) },
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PlayerTomoArt(friend.player, size = 44)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(friend.player.displayName, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = friend.distancesText(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            uiState.error != null -> ErrorWithRetry(uiState.error, onRetry)
            else -> EmptySocialCard(
                message = stringResource(R.string.friends_empty),
                onAddFriend = onAddFriend,
            )
        }
    }

    uiState.selectedFriend?.let { friend ->
        var confirmRemove by rememberSaveable { mutableStateOf(false) }
        if (confirmRemove) {
            AlertDialog(
                onDismissRequest = { confirmRemove = false },
                text = { Text(stringResource(R.string.friends_remove_confirm, friend.player.displayName)) },
                confirmButton = {
                    TextButton(onClick = { onRemove(friend) }) { Text(stringResource(R.string.friends_remove)) }
                },
                dismissButton = { TextButton(onClick = { confirmRemove = false }) { Text(stringResource(R.string.cancel)) } },
            )
        } else {
            PlayerCardDialog(
                player = friend.player,
                details = listOf(friend.distancesText()),
                onDismiss = { onSelect(null) },
                onRemove = { confirmRemove = true },
            )
        }
    }
}

@Composable
private fun Friend.distancesText() =
    stringResource(R.string.friends_distances, formatKm(todayM), formatKm(weekM), formatKm(monthM))

@Composable
private fun AddFriendSection(viewModel: AddFriendViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val shareText = uiState.myFriendCode?.let { stringResource(R.string.add_friend_share_text, it) }
    AddFriendContent(
        uiState = uiState,
        onCopy = { code ->
            scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(code, code))) }
        },
        onShare = {
            val intent = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, shareText)
            context.startActivity(Intent.createChooser(intent, null))
        },
        onCodeChange = viewModel::onCodeChange,
        onLookUp = viewModel::lookUp,
        onSend = viewModel::sendRequest,
    )
}

@Composable
fun AddFriendContent(
    uiState: AddFriendUiState,
    onCopy: (String) -> Unit,
    onShare: () -> Unit,
    onCodeChange: (String) -> Unit,
    onLookUp: () -> Unit,
    onSend: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        uiState.myFriendCode?.let { code ->
            RuntomoCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = stringResource(R.string.add_friend_your_code),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = code,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SecondaryButton(onClick = { onCopy(code) }, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.add_friend_copy))
                        }
                        PrimaryButton(onClick = onShare, modifier = Modifier.weight(1f)) {
                            Text(stringResource(R.string.add_friend_share))
                        }
                    }
                }
            }
        }
        OutlinedTextField(
            value = uiState.code,
            onValueChange = onCodeChange,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.small,
            colors = journalTextFieldColors(),
            label = { Text(stringResource(R.string.add_friend_code_label)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onLookUp() }),
        )
        TonalButton(
            onClick = onLookUp,
            enabled = !uiState.isBusy && uiState.code.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.add_friend_look_up))
        }
        uiState.preview?.let { player ->
            RuntomoCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlayerTomoArt(player, size = 44)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(player.displayName, style = MaterialTheme.typography.titleMedium)
                        player.tomo?.let {
                            Text(
                                text = stringResource(R.string.friend_tomo, it.nickname),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    CompactButton(onClick = onSend, enabled = !uiState.isBusy) {
                        Text(stringResource(R.string.add_friend_send))
                    }
                }
            }
        }
        uiState.result?.let { Text(it.text(), style = MaterialTheme.typography.titleMedium) }
        uiState.error?.let { ErrorText(it) }
        if (uiState.isBusy) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

@Composable
private fun AddFriendResult.text(): String = when (this) {
    AddFriendResult.NotFound -> stringResource(R.string.add_friend_not_found)
    AddFriendResult.OwnCode -> stringResource(R.string.add_friend_own_code)
    AddFriendResult.AlreadyFriends -> stringResource(R.string.add_friend_already_friends)
    AddFriendResult.LimitReached -> stringResource(R.string.friends_limit)
    is AddFriendResult.Sent -> stringResource(R.string.add_friend_sent, displayName)
    is AddFriendResult.NowFriends -> stringResource(R.string.add_friend_now_friends, displayName)
}

/** A friend's profile card (UC-16 step 5). It shows only what UC-15 BR-2 allows. */
@Composable
private fun PlayerCardDialog(
    player: PlayerCard,
    details: List<String>,
    onDismiss: () -> Unit,
    onRemove: (() -> Unit)? = null,
) {
    Dialog(onDismissRequest = onDismiss) {
        RuntomoCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    GroundedGlow(modifier = Modifier.size(128.dp))
                    PlayerTomoArt(player, size = 96)
                }
                Text(player.displayName, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                player.tomo?.let {
                    Text(
                        text = stringResource(R.string.friend_tomo, it.nickname),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                details.forEach {
                    Text(it, style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    onRemove?.let {
                        TextButton(onClick = it) { Text(stringResource(R.string.friends_remove)) }
                    }
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.close)) }
                }
            }
        }
    }
}

@Composable
private fun PlayerTomoArt(player: PlayerCard, size: Int = 40) {
    val tomo = player.tomo
    if (tomo != null) TomoArt(tomo.species, size = size.dp, pose = TomoPose.AVATAR) else SpeciesSilhouette(null, size = size.dp)
}

@Composable
private fun ErrorWithRetry(error: SocialError?, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        error?.let { ErrorText(it) }
        SecondaryButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
private fun ErrorText(error: SocialError) {
    Text(
        text = stringResource(if (error == SocialError.NoConnection) R.string.error_no_connection else R.string.error_generic),
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
    )
}

private val LeaderboardPeriod.labelRes: Int
    get() = when (this) {
        LeaderboardPeriod.TODAY -> R.string.period_today
        LeaderboardPeriod.WEEK -> R.string.period_week
        LeaderboardPeriod.MONTH -> R.string.period_month
    }

@Preview(showBackground = true)
@Composable
private fun LeaderboardContentPreview() {
    val luppy = Species("luppy", "Luppy", Rarity.COMMON, "Runs hot.")
    val rocky = Species("rocky", "Rocky", Rarity.COMMON, "Steady.")
    val ana = PlayerCard("u2", "Ana", DisplayedTomo("Pebble", rocky))
    val kai = PlayerCard("u1", "Kai", DisplayedTomo("Mochi", luppy))
    RuntomoTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.padding(top = 24.dp)) {
                LeaderboardContent(
                    uiState = LeaderboardUiState(
                        isLoading = false,
                        leaderboard = Leaderboard(
                            rows = listOf(
                                LeaderboardRow(1, ana, 14_200, isMe = false),
                                LeaderboardRow(2, kai, 12_400, isMe = true),
                            ),
                            ahead = ana,
                            gapM = 1_800,
                        ),
                    ),
                    onPeriodChange = {},
                    onRetry = {},
                    onAddFriend = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FriendsEmptyPreview() {
    RuntomoTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.padding(top = 24.dp)) {
                FriendsContent(
                    uiState = FriendsUiState(isLoading = false),
                    onRetry = {},
                    onAccept = {},
                    onDecline = {},
                    onSelect = {},
                    onRemove = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddFriendContentPreview() {
    val rocky = Species("rocky", "Rocky", Rarity.COMMON, "Steady.")
    RuntomoTheme {
        Surface(color = MaterialTheme.colorScheme.background, modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.padding(top = 24.dp)) {
                AddFriendContent(
                    uiState = AddFriendUiState(
                        myFriendCode = "4829-1034",
                        code = "12345678",
                        preview = PlayerCard("u2", "Ana", DisplayedTomo("Pebble", rocky)),
                    ),
                    onCopy = {},
                    onShare = {},
                    onCodeChange = {},
                    onLookUp = {},
                    onSend = {},
                )
            }
        }
    }
}
