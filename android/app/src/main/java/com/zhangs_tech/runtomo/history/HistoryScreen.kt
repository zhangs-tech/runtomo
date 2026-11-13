package com.zhangs_tech.runtomo.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.core.formatKm
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }
    HistoryContent(uiState, onRetry = viewModel::load, onLoadMore = viewModel::loadMore, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryContent(
    uiState: HistoryUiState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.history_title)) }) },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                uiState.loadFailed -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(stringResource(R.string.error_no_connection), textAlign = TextAlign.Center)
                    OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                }
                else -> HistoryList(uiState, onLoadMore)
            }
        }
    }
}

@Composable
private fun HistoryList(uiState: HistoryUiState, onLoadMore: () -> Unit) {
    val listState = rememberLazyListState()
    val atEnd by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 2
        }
    }
    LaunchedEffect(atEnd, uiState.nextCursor) { if (atEnd) onLoadMore() }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        uiState.totals?.let { totals -> item { TotalsCard(totals) } }
        if (uiState.days.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.history_empty),
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        items(uiState.days, key = { it.date }) { DayRow(it) }
        if (uiState.isLoadingMore) {
            item { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
        }
    }
}

@Composable
private fun TotalsCard(totals: PeriodTotals) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            TotalColumn(stringResource(R.string.history_this_week), totals.week, Modifier.weight(1f))
            TotalColumn(stringResource(R.string.history_this_month), totals.month, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TotalColumn(label: String, total: ActivityTotal, modifier: Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Text("${formatKm(total.distanceM)} km", style = MaterialTheme.typography.headlineSmall)
        Text(stringResource(R.string.history_steps, formatNumber(total.steps)))
    }
}

@Composable
private fun DayRow(day: HistoryDay) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(formatDay(day.date), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.history_day_activity, formatNumber(day.steps.toLong()), formatKm(day.distanceM)))
            Text(
                text = stringResource(R.string.history_day_rewards, day.coins, day.bond),
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun formatNumber(value: Long): String = NumberFormat.getInstance().format(value)

private fun formatDay(isoDate: String): String = LocalDate.parse(isoDate).format(DateTimeFormatter.ofPattern("EEE, d MMM"))

@Preview(showBackground = true)
@Composable
private fun HistoryContentPreview() {
    RuntomoTheme {
        HistoryContent(
            uiState = HistoryUiState(
                isLoading = false,
                totals = PeriodTotals(ActivityTotal(12_000, 9000), ActivityTotal(40_000, 30_000)),
                days = listOf(HistoryDay("2026-11-13", 6000, 4500, 450, 45), HistoryDay("2026-11-12", 2000, 1500, 150, 15)),
            ),
            onRetry = {},
            onLoadMore = {},
        )
    }
}
