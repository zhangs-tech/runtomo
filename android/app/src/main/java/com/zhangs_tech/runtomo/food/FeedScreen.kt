package com.zhangs_tech.runtomo.food

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
import com.zhangs_tech.runtomo.tomo.Tomo
import com.zhangs_tech.runtomo.tomo.TomoArt
import com.zhangs_tech.runtomo.tomo.TomoPose
import com.zhangs_tech.runtomo.ui.components.CoinAmount
import com.zhangs_tech.runtomo.ui.components.CoinBalanceChip
import com.zhangs_tech.runtomo.ui.components.CompactButton
import com.zhangs_tech.runtomo.ui.components.CompactOutlinedButton
import com.zhangs_tech.runtomo.ui.components.GroundedGlow
import com.zhangs_tech.runtomo.ui.components.Pill
import com.zhangs_tech.runtomo.ui.components.RuntomoCard
import com.zhangs_tech.runtomo.ui.components.SecondaryButton
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme
import java.text.NumberFormat

@Composable
fun FeedScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    FeedContent(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::load,
        onBuy = viewModel::buy,
        onFeed = viewModel::feed,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedContent(
    uiState: FeedUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onBuy: (String) -> Unit,
    onFeed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tomo = uiState.pantry?.tomo
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(tomo?.let { stringResource(R.string.feed_title, it.nickname) } ?: stringResource(R.string.feed)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.navigate_back))
                    }
                },
                actions = {
                    uiState.pantry?.let {
                        val coins = NumberFormat.getInstance().format(it.coinBalance)
                        CoinBalanceChip(
                            text = coins,
                            description = stringResource(R.string.coin_balance, coins),
                            modifier = Modifier.padding(end = 16.dp),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val pantry = uiState.pantry
            when {
                pantry != null -> PantryBody(pantry, uiState, onBuy, onFeed)
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                else -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(stringResource(R.string.error_no_connection), textAlign = TextAlign.Center)
                    SecondaryButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                }
            }
        }
    }
}

@Composable
private fun PantryBody(pantry: Pantry, uiState: FeedUiState, onBuy: (String) -> Unit, onFeed: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val tomo = pantry.tomo
        if (tomo == null) {
            Text(stringResource(R.string.feed_no_tomo))
            return@Column
        }
        EatingTomo(tomo, uiState.message)
        Text(
            text = if (pantry.feedsLeftToday > 0) {
                stringResource(R.string.feed_left_today, tomo.nickname, pantry.feedsLeftToday)
            } else {
                stringResource(R.string.feed_full_today, tomo.nickname)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        uiState.message?.let { MessageCard(it, tomo.nickname) }
        uiState.error?.let {
            Text(
                text = stringResource(if (it == FeedError.NoConnection) R.string.error_no_connection else R.string.error_generic),
                color = MaterialTheme.colorScheme.error,
            )
        }
        pantry.items.forEach { item ->
            FoodRow(
                item = item,
                isBusy = uiState.busyCode != null,
                canGive = item.quantity > 0 && pantry.feedsLeftToday > 0,
                onBuy = { onBuy(item.code) },
                onGive = { onFeed(item.code) },
            )
        }
    }
}

/** UC-18 step 5: a happy hop after eating, and a shake when the Tomo is full. */
@Composable
private fun EatingTomo(tomo: Tomo, message: FeedMessage?) {
    val scale = remember { Animatable(1f) }
    val rotation = remember { Animatable(0f) }
    LaunchedEffect(message) {
        when (message) {
            is FeedMessage.Fed -> repeat(2) {
                scale.animateTo(1.15f, tween(120))
                scale.animateTo(1f, tween(140))
            }
            is FeedMessage.Full -> repeat(2) {
                rotation.animateTo(-8f, tween(70))
                rotation.animateTo(8f, tween(70))
            }
            else -> Unit
        }
        rotation.snapTo(0f)
    }
    Box(contentAlignment = Alignment.Center) {
        GroundedGlow(modifier = Modifier.size(200.dp))
        TomoArt(
            species = tomo.species,
            size = 160.dp,
            pose = if (message is FeedMessage.Fed) TomoPose.HAPPY else TomoPose.DEFAULT,
            modifier = Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                rotationZ = rotation.value
            },
        )
    }
    Text(tomo.nickname, style = MaterialTheme.typography.headlineMedium)
    Pill(stringResource(R.string.bond_level_short, tomo.bondLevel))
}

@Composable
private fun MessageCard(message: FeedMessage, nickname: String) {
    RuntomoCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        borderColor = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            when (message) {
                is FeedMessage.Fed -> {
                    Text(stringResource(R.string.feed_fed, nickname, message.foodName, message.bond))
                    message.newBondLevel?.let {
                        Text(stringResource(R.string.rewards_level_up, it), style = MaterialTheme.typography.titleMedium)
                    }
                }
                is FeedMessage.Full -> Text(stringResource(R.string.feed_full, nickname, message.foodName))
                is FeedMessage.Bought -> Text(stringResource(R.string.feed_bought, message.foodName))
                is FeedMessage.NotEnoughCoins ->
                    Text(stringResource(R.string.feed_not_enough, formatKm(message.distanceNeededM)))
            }
        }
    }
}

@Composable
private fun FoodRow(item: FoodItem, isBusy: Boolean, canGive: Boolean, onBuy: () -> Unit, onGive: () -> Unit) {
    RuntomoCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // The food art has a white background, so it sits on a white tile in both themes.
            foodImage(item.code)?.let {
                Image(
                    painter = painterResource(it),
                    contentDescription = null,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(Color.White)
                        .border(1.5.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.small)
                        .padding(4.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(item.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.feed_bond, item.bond),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(R.string.feed_owned, item.quantity),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(
                modifier = Modifier.width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                CompactButton(
                    onClick = onGive,
                    enabled = canGive && !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.feed_give)) }
                CompactOutlinedButton(
                    onClick = onBuy,
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = BuyPadding,
                ) {
                    Text(stringResource(R.string.feed_buy))
                    Spacer(Modifier.width(8.dp))
                    CoinAmount(
                        text = item.cost.toString(),
                        description = stringResource(R.string.feed_cost, item.cost),
                        iconSize = 16.dp,
                    )
                }
            }
        }
    }
}

private val BuyPadding = PaddingValues(start = 16.dp, end = 12.dp)

/** Art from assets/foods. Food codes are permanent, so the app can map them. */
@DrawableRes
private fun foodImage(code: String): Int? = when (code) {
    "banana" -> R.drawable.food_banana
    "onigiri" -> R.drawable.food_onigiri
    "bento" -> R.drawable.food_bento
    else -> null
}

@Preview(showBackground = true)
@Composable
private fun FeedContentPreview() {
    val luppy = Species("luppy", "Luppy", Rarity.COMMON, "Runs hot.")
    RuntomoTheme {
        FeedContent(
            uiState = FeedUiState(
                isLoading = false,
                pantry = Pantry(
                    items = listOf(FoodItem("banana", "Banana", 50, 3, 2), FoodItem("bento", "Bento", 150, 10, 0)),
                    coinBalance = 320,
                    tomo = Tomo("t1", luppy, "Mochi", 40, 1, 4000, "2026-11-01"),
                    feedsLeftToday = 2,
                ),
                message = FeedMessage.Fed("Banana", 3, null),
            ),
            onBack = {},
            onRetry = {},
            onBuy = {},
            onFeed = {},
        )
    }
}
