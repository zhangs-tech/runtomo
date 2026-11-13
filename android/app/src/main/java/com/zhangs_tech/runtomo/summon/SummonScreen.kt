package com.zhangs_tech.runtomo.summon

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.core.formatKm
import com.zhangs_tech.runtomo.tomo.Rarity
import com.zhangs_tech.runtomo.tomo.Species
import com.zhangs_tech.runtomo.tomo.TomoArt
import com.zhangs_tech.runtomo.tomo.color
import com.zhangs_tech.runtomo.tomo.labelRes
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SummonScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SummonViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SummonContent(
        uiState = uiState,
        actions = SummonActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onSummon = viewModel::requestSummon,
            onConfirm = viewModel::confirmSummon,
            onCancel = viewModel::cancelSummon,
            onCheckResult = viewModel::checkResult,
            onHatched = viewModel::onHatched,
            onEquip = viewModel::equip,
            onStartRename = viewModel::startRename,
            onCancelRename = viewModel::cancelRename,
            onRename = viewModel::rename,
            onDone = viewModel::done,
        ),
        modifier = modifier,
    )
}

data class SummonActions(
    val onBack: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onSummon: () -> Unit = {},
    val onConfirm: () -> Unit = {},
    val onCancel: () -> Unit = {},
    val onCheckResult: () -> Unit = {},
    val onHatched: () -> Unit = {},
    val onEquip: () -> Unit = {},
    val onStartRename: () -> Unit = {},
    val onCancelRename: () -> Unit = {},
    val onRename: (String) -> Unit = {},
    val onDone: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummonContent(uiState: SummonUiState, actions: SummonActions, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(uiState.banner?.name ?: stringResource(R.string.summon)) },
                navigationIcon = {
                    IconButton(onClick = actions.onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.navigate_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val banner = uiState.banner
            when (val phase = uiState.phase) {
                is SummonPhase.Hatching -> Hatching(phase.result.species.rarity, actions.onHatched)
                is SummonPhase.Revealed -> Revealed(phase.result, uiState, actions)
                SummonPhase.Checking, SummonPhase.Summoning ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                SummonPhase.CheckFailed -> Message(
                    text = stringResource(R.string.summon_check_failed),
                    buttonText = stringResource(R.string.summon_check_again),
                    onClick = actions.onCheckResult,
                )
                SummonPhase.Idle, SummonPhase.Confirming -> when {
                    banner != null -> BannerDetails(banner, uiState, actions)
                    uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    else -> Message(
                        text = stringResource(R.string.error_no_connection),
                        buttonText = stringResource(R.string.retry),
                        onClick = actions.onRetry,
                    )
                }
            }
        }
    }

    val banner = uiState.banner
    if (uiState.phase == SummonPhase.Confirming && banner != null) {
        AlertDialog(
            onDismissRequest = actions.onCancel,
            title = { Text(stringResource(R.string.summon_confirm_title)) },
            text = { Text(stringResource(R.string.summon_confirm_body, formatNumber(banner.cost.toLong()))) },
            confirmButton = { TextButton(onClick = actions.onConfirm) { Text(stringResource(R.string.summon)) } },
            dismissButton = { TextButton(onClick = actions.onCancel) { Text(stringResource(R.string.cancel)) } },
        )
    }
}

@Composable
private fun BannerDetails(banner: Banner, uiState: SummonUiState, actions: SummonActions) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(stringResource(R.string.summon_balance, formatNumber(banner.coinBalance)), style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.summon_cost, formatNumber(banner.cost.toLong())))
        banner.pityRemaining?.let { Text(stringResource(R.string.summon_pity, it)) }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.summon_rates_title), style = MaterialTheme.typography.titleMedium)
                banner.rarityRates.forEach { rate ->
                    RateRow(stringResource(rate.rarity.labelRes), rate.ratePercent, rate.rarity.color)
                }
                banner.pool.forEach { entry -> RateRow(entry.species.name, entry.ratePercent) }
            }
        }
        uiState.distanceNeededM?.let {
            Text(stringResource(R.string.summon_not_enough, formatKm(it)), color = MaterialTheme.colorScheme.error)
        }
        uiState.error?.let { error ->
            Text(
                text = stringResource(
                    when (error) {
                        SummonError.LostConnection -> R.string.summon_lost_connection
                        SummonError.NoConnection -> R.string.error_no_connection
                        else -> R.string.error_generic
                    },
                ),
                color = MaterialTheme.colorScheme.error,
            )
        }
        Button(onClick = actions.onSummon, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.summon)) }
    }
}

@Composable
private fun RateRow(label: String, ratePercent: Double, color: Color = Color.Unspecified) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = color)
        Text(stringResource(R.string.summon_rate, String.format(Locale.US, "%.1f", ratePercent)))
    }
}

private val EggShape = GenericShape { size, _ ->
    addOval(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, size.height))
}

/** UC-12 step 7: the egg shakes while the background glows in the rarity's color. */
@Composable
private fun Hatching(rarity: Rarity, onHatched: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(HATCH_MILLIS)
        onHatched()
    }
    val transition = rememberInfiniteTransition(label = "egg")
    val angle by transition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(tween(120), RepeatMode.Reverse),
        label = "shake",
    )
    val glow by transition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "glow",
    )
    Column(
        modifier = Modifier.fillMaxSize().background(rarity.color.copy(alpha = glow)),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(width = 120.dp, height = 150.dp)
                .rotate(angle)
                .background(Color(0xFFFFF4DC), EggShape),
        )
        Text(stringResource(R.string.summon_hatching), style = MaterialTheme.typography.titleMedium)
    }
}

/** UC-12 steps 8 and 9, and 5a. */
@Composable
private fun Revealed(result: SummonResult, uiState: SummonUiState, actions: SummonActions) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val species = result.species
        Box(
            modifier = Modifier.size(200.dp).background(species.rarity.color.copy(alpha = 0.25f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            TomoArt(species, size = 144.dp)
        }
        Text(result.tomo?.nickname ?: species.name, style = MaterialTheme.typography.headlineMedium)
        Text(stringResource(species.rarity.labelRes), color = species.rarity.color, style = MaterialTheme.typography.titleMedium)
        val tomo = result.tomo
        if (tomo == null) {
            Text(
                text = stringResource(R.string.summon_duplicate, species.name, result.duplicateCoins),
                textAlign = TextAlign.Center,
            )
        } else {
            Text(stringResource(R.string.summon_new_tomo), style = MaterialTheme.typography.titleLarge)
            uiState.error?.takeIf { it != SummonError.InvalidNickname }?.let {
                Text(stringResource(R.string.error_generic), color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = actions.onEquip,
                enabled = !uiState.isEquipped && !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(if (uiState.isEquipped) R.string.equipped else R.string.equip))
            }
            OutlinedButton(onClick = actions.onStartRename, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.rename))
            }
            if (uiState.isRenaming) RenameDialog(tomo.nickname, uiState, actions)
        }
        TextButton(onClick = actions.onDone) { Text(stringResource(R.string.done)) }
    }
}

@Composable
private fun RenameDialog(currentNickname: String, uiState: SummonUiState, actions: SummonActions) {
    var nickname by rememberSaveable { mutableStateOf(currentNickname) }
    AlertDialog(
        onDismissRequest = actions.onCancelRename,
        title = { Text(stringResource(R.string.rename)) },
        text = {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text(stringResource(R.string.nickname_label)) },
                supportingText = { Text(stringResource(R.string.nickname_rule)) },
                isError = uiState.error == SummonError.InvalidNickname,
                singleLine = true,
            )
        },
        confirmButton = {
            TextButton(onClick = { actions.onRename(nickname) }, enabled = !uiState.isSaving) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = actions.onCancelRename) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun Message(text: String, buttonText: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, textAlign = TextAlign.Center)
        OutlinedButton(onClick = onClick) { Text(buttonText) }
    }
}

private fun formatNumber(value: Long): String = NumberFormat.getInstance().format(value)

private const val HATCH_MILLIS = 1800L

@Preview(showBackground = true)
@Composable
private fun SummonContentPreview() {
    val luppy = Species("luppy", "Luppy", Rarity.COMMON, "Runs hot.")
    RuntomoTheme {
        SummonContent(
            uiState = SummonUiState(
                isLoading = false,
                banner = Banner(
                    code = "standard",
                    name = "Standard Egg",
                    cost = 1000,
                    coinBalance = 1250,
                    pool = listOf(PoolEntry(luppy, 100.0)),
                    rarityRates = listOf(RarityRate(Rarity.COMMON, 100.0)),
                ),
            ),
            actions = SummonActions(),
        )
    }
}
