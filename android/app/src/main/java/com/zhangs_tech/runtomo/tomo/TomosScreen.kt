package com.zhangs_tech.runtomo.tomo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme

@Composable
fun TomosScreen(
    onOpenSummon: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TomosViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.load() }
    TomosContent(
        uiState = uiState,
        actions = TomosActions(
            onRetry = viewModel::load,
            onSortOrderChange = viewModel::setSortOrder,
            onSelect = viewModel::select,
            onDismiss = viewModel::dismiss,
            onEquip = viewModel::equip,
            onStartRename = viewModel::startRename,
            onCancelRename = viewModel::cancelRename,
            onRename = viewModel::rename,
            onOpenSummon = {
                viewModel.dismiss()
                onOpenSummon()
            },
        ),
        modifier = modifier,
    )
}

data class TomosActions(
    val onRetry: () -> Unit = {},
    val onSortOrderChange: (SortOrder) -> Unit = {},
    val onSelect: (String) -> Unit = {},
    val onDismiss: () -> Unit = {},
    val onEquip: (String) -> Unit = {},
    val onStartRename: () -> Unit = {},
    val onCancelRename: () -> Unit = {},
    val onRename: (tomoId: String, nickname: String) -> Unit = { _, _ -> },
    val onOpenSummon: () -> Unit = {},
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TomosContent(uiState: TomosUiState, actions: TomosActions, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tomos_title)) },
                actions = { TextButton(onClick = actions.onOpenSummon) { Text(stringResource(R.string.summon)) } },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            val collection = uiState.collection
            when {
                collection != null -> CollectionGrid(collection, uiState, actions)
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                else -> Column(
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(stringResource(R.string.error_no_connection), textAlign = TextAlign.Center)
                    OutlinedButton(onClick = actions.onRetry) { Text(stringResource(R.string.retry)) }
                }
            }
        }
    }

    uiState.selectedEntry?.let { entry ->
        val tomo = entry.tomo
        if (tomo == null) {
            NotOwnedDialog(entry.species, actions)
        } else {
            OwnedDialog(tomo, isEquipped = tomo.id == uiState.collection?.equippedTomoId, uiState, actions)
        }
    }
}

@Composable
private fun CollectionGrid(collection: TomoCollection, uiState: TomosUiState, actions: TomosActions) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 104.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.tomos_collected, collection.collectedCount, collection.totalCount),
                    style = MaterialTheme.typography.titleMedium,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SortOrder.entries.forEach { order ->
                        FilterChip(
                            selected = uiState.sortOrder == order,
                            onClick = { actions.onSortOrderChange(order) },
                            label = { Text(stringResource(order.labelRes)) },
                        )
                    }
                }
            }
        }
        items(uiState.sortedEntries, key = { it.species.code }) { entry ->
            CollectionCell(entry, isEquipped = entry.tomo != null && entry.tomo.id == collection.equippedTomoId) {
                actions.onSelect(entry.species.code)
            }
        }
    }
}

@Composable
private fun CollectionCell(entry: CollectionEntry, isEquipped: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier.clickable(onClickLabel = entry.tomo?.nickname, role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val tomo = entry.tomo
        if (tomo != null) {
            TomoArt(entry.species, size = 88.dp)
            Text(tomo.nickname, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            if (isEquipped) {
                Text(
                    text = stringResource(R.string.equipped),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            SpeciesSilhouette(entry.species, size = 88.dp)
            Text(stringResource(R.string.unknown_species), style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun OwnedDialog(tomo: Tomo, isEquipped: Boolean, uiState: TomosUiState, actions: TomosActions) {
    if (uiState.isRenaming) {
        RenameDialog(tomo, uiState, actions)
        return
    }
    TomoProfileDialog(tomo, onDismiss = actions.onDismiss) {
        uiState.error?.let { ErrorText(it) }
        Button(
            onClick = { actions.onEquip(tomo.id) },
            enabled = !isEquipped && !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(if (isEquipped) R.string.equipped else R.string.equip))
        }
        OutlinedButton(onClick = actions.onStartRename, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.rename))
        }
        TextButton(onClick = actions.onDismiss) { Text(stringResource(R.string.close)) }
    }
}

@Composable
private fun RenameDialog(tomo: Tomo, uiState: TomosUiState, actions: TomosActions) {
    var nickname by rememberSaveable(tomo.id) { mutableStateOf(tomo.nickname) }
    AlertDialog(
        onDismissRequest = actions.onCancelRename,
        title = { Text(stringResource(R.string.rename)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = { Text(stringResource(R.string.nickname_label)) },
                    supportingText = { Text(stringResource(R.string.nickname_rule)) },
                    isError = uiState.error == TomosError.InvalidNickname,
                    singleLine = true,
                )
                uiState.error?.takeIf { it != TomosError.InvalidNickname }?.let { ErrorText(it) }
            }
        },
        confirmButton = {
            TextButton(onClick = { actions.onRename(tomo.id, nickname) }, enabled = !uiState.isSaving) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = actions.onCancelRename) { Text(stringResource(R.string.cancel)) } },
    )
}

/** UC-11 3a. */
@Composable
private fun NotOwnedDialog(species: Species, actions: TomosActions) {
    AlertDialog(
        onDismissRequest = actions.onDismiss,
        icon = { SpeciesSilhouette(species, size = 96.dp) },
        title = { Text(species.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(species.rarity.labelRes), color = species.rarity.color)
                Text(stringResource(R.string.not_owned_hint, species.name))
            }
        },
        confirmButton = { TextButton(onClick = actions.onOpenSummon) { Text(stringResource(R.string.summon)) } },
        dismissButton = { TextButton(onClick = actions.onDismiss) { Text(stringResource(R.string.close)) } },
    )
}

@Composable
private fun ErrorText(error: TomosError) {
    Text(
        text = stringResource(
            when (error) {
                TomosError.NoConnection -> R.string.error_no_connection
                TomosError.InvalidNickname -> R.string.nickname_rule
                TomosError.Failed -> R.string.error_generic
            },
        ),
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
    )
}

private val SortOrder.labelRes: Int
    get() = when (this) {
        SortOrder.RARITY -> R.string.sort_rarity
        SortOrder.BOND -> R.string.sort_bond
        SortOrder.DISTANCE -> R.string.sort_distance
    }

@Preview(showBackground = true)
@Composable
private fun TomosContentPreview() {
    val luppy = Species("luppy", "Luppy", Rarity.COMMON, "Runs hot.")
    RuntomoTheme {
        TomosContent(
            uiState = TomosUiState(
                isLoading = false,
                collection = TomoCollection(
                    entries = listOf(
                        CollectionEntry(luppy, Tomo("t1", luppy, "Mochi", 120, 2, 12_000, "2026-11-01")),
                        CollectionEntry(Species("rocky", "Rocky", Rarity.COMMON, "Steady.")),
                    ),
                    collectedCount = 1,
                    totalCount = 2,
                    equippedTomoId = "t1",
                ),
            ),
            actions = TomosActions(),
        )
    }
}
