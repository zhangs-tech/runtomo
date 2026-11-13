package com.zhangs_tech.runtomo.starter

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.tomo.Rarity
import com.zhangs_tech.runtomo.tomo.Species
import com.zhangs_tech.runtomo.tomo.TomoArt
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme
import kotlinx.coroutines.delay

@Composable
fun ChooseStarterScreen(
    modifier: Modifier = Modifier,
    viewModel: ChooseStarterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BackHandler(enabled = uiState.isNaming && uiState.greeting == null, onBack = viewModel::cancelNaming)
    BackHandler(enabled = uiState.greeting != null, onBack = viewModel::finishGreeting)
    ChooseStarterContent(
        uiState = uiState,
        onRetryLoad = viewModel::loadStarters,
        onSelect = viewModel::select,
        onConfirmSelection = viewModel::confirmSelection,
        onNicknameChange = viewModel::onNicknameChange,
        onSave = viewModel::save,
        onCancelNaming = viewModel::cancelNaming,
        onFinishGreeting = viewModel::finishGreeting,
        modifier = modifier,
    )
}

@Composable
fun ChooseStarterContent(
    uiState: ChooseStarterUiState,
    onRetryLoad: () -> Unit,
    onSelect: (Species) -> Unit,
    onConfirmSelection: () -> Unit,
    onNicknameChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancelNaming: () -> Unit,
    onFinishGreeting: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val selected = uiState.selected
            val greeting = uiState.greeting
            when {
                greeting != null -> HelloStep(greeting, onFinishGreeting)
                uiState.isLoading -> CircularProgressIndicator()
                uiState.starters.isEmpty() -> LoadFailed(uiState.error, onRetryLoad)
                uiState.isNaming && selected != null -> NicknameStep(
                    species = selected,
                    uiState = uiState,
                    onNicknameChange = onNicknameChange,
                    onSave = onSave,
                    onBack = onCancelNaming,
                )
                else -> PickStep(uiState, onSelect, onConfirmSelection)
            }
        }
    }
}

@Composable
private fun ColumnScope.PickStep(
    uiState: ChooseStarterUiState,
    onSelect: (Species) -> Unit,
    onConfirmSelection: () -> Unit,
) {
    Text(stringResource(R.string.choose_starter_intro), style = MaterialTheme.typography.titleMedium)
    Text(stringResource(R.string.choose_starter_title), style = MaterialTheme.typography.headlineMedium)
    uiState.starters.forEach { species ->
        StarterCard(species, isSelected = species == uiState.selected, onClick = { onSelect(species) })
    }
    uiState.selected?.let { selected ->
        Button(onClick = onConfirmSelection, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.choose_starter_confirm, selected.name))
        }
    }
}

@Composable
private fun StarterCard(species: Species, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TomoArt(species, size = 64.dp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(species.name, style = MaterialTheme.typography.titleLarge)
                if (isSelected) {
                    Text(species.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.NicknameStep(
    species: Species,
    uiState: ChooseStarterUiState,
    onNicknameChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    TomoArt(species, size = 128.dp)
    Text(
        text = stringResource(R.string.nickname_title, species.name),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
    )
    OutlinedTextField(
        value = uiState.nickname,
        onValueChange = onNicknameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.nickname_label)) },
        supportingText = { Text(stringResource(R.string.nickname_rule)) },
        isError = uiState.error == StarterError.InvalidNickname,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSave() }),
    )
    uiState.error?.takeIf { it != StarterError.InvalidNickname }?.let { ErrorText(it) }
    if (uiState.isSaving) {
        CircularProgressIndicator()
    } else {
        Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (uiState.error == null) R.string.nickname_confirm else R.string.retry))
        }
        TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
    }
}

/** UC-02 step 9: the new Tomo hops and says hello, then the app goes to the Home Screen. */
@Composable
private fun HelloStep(greeting: Greeting, onFinish: () -> Unit) {
    val offsetY = remember { Animatable(0f) }
    LaunchedEffect(greeting) {
        repeat(HELLO_HOPS) {
            offsetY.animateTo(-24f, tween(180))
            offsetY.animateTo(0f, tween(220))
        }
        delay(HELLO_PAUSE_MILLIS)
        onFinish()
    }
    Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 2.dp, shadowElevation = 2.dp) {
        Text(
            text = stringResource(R.string.starter_hello, greeting.nickname),
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
    }
    TomoArt(greeting.species, size = 160.dp, modifier = Modifier.graphicsLayer { translationY = offsetY.value.dp.toPx() })
    Text(stringResource(R.string.starter_hello_body), textAlign = TextAlign.Center)
    Button(onClick = onFinish, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.starter_hello_continue)) }
}

private const val HELLO_HOPS = 3
private const val HELLO_PAUSE_MILLIS = 1_800L

@Composable
private fun LoadFailed(error: StarterError?, onRetry: () -> Unit) {
    error?.let { ErrorText(it) }
    OutlinedButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
}

@Composable
private fun ErrorText(error: StarterError) {
    Text(
        text = stringResource(
            if (error == StarterError.NoConnection) R.string.error_no_connection else R.string.error_generic,
        ),
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Center,
    )
}

private val previewStarters = listOf(
    Species("luppy", "Luppy", Rarity.COMMON, "Luppy runs hot."),
    Species("rocky", "Rocky", Rarity.COMMON, "Rocky is in no hurry."),
    Species("aeroblast", "Aeroblast", Rarity.COMMON, "Aeroblast is quick as the wind."),
)

@Preview(showBackground = true)
@Composable
private fun PickStepPreview() {
    RuntomoTheme {
        ChooseStarterContent(
            uiState = ChooseStarterUiState(starters = previewStarters, isLoading = false, selected = previewStarters[0]),
            onRetryLoad = {}, onSelect = {}, onConfirmSelection = {}, onNicknameChange = {}, onSave = {}, onCancelNaming = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NicknameStepPreview() {
    RuntomoTheme {
        ChooseStarterContent(
            uiState = ChooseStarterUiState(
                starters = previewStarters,
                isLoading = false,
                selected = previewStarters[1],
                isNaming = true,
                nickname = "Rocky",
            ),
            onRetryLoad = {}, onSelect = {}, onConfirmSelection = {}, onNicknameChange = {}, onSave = {}, onCancelNaming = {},
        )
    }
}
