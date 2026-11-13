package com.zhangs_tech.runtomo.starter

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
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
import com.zhangs_tech.runtomo.tomo.TomoPose
import com.zhangs_tech.runtomo.ui.components.GroundedGlow
import com.zhangs_tech.runtomo.ui.components.PrimaryButton
import com.zhangs_tech.runtomo.ui.components.RuntomoCard
import com.zhangs_tech.runtomo.ui.components.SecondaryButton
import com.zhangs_tech.runtomo.ui.components.journalTextFieldColors
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
    Text(
        text = stringResource(R.string.choose_starter_title),
        style = MaterialTheme.typography.headlineMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(bottom = 4.dp),
    )
    uiState.starters.forEach { species ->
        StarterCard(species, isSelected = species == uiState.selected, onClick = { onSelect(species) })
    }
    uiState.selected?.let { selected ->
        PrimaryButton(onClick = onConfirmSelection, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Text(stringResource(R.string.choose_starter_confirm, selected.name))
        }
    }
}

@Composable
private fun StarterCard(species: Species, isSelected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val artScale by animateFloatAsState(if (isSelected) 1.08f else 1f, tween(180), label = "starter")
    RuntomoCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .selectable(selected = isSelected, onClick = onClick, role = Role.RadioButton),
        containerColor = if (isSelected) colors.primaryContainer else colors.surfaceContainerLowest,
        borderColor = if (isSelected) colors.primary else colors.outlineVariant,
        borderWidth = if (isSelected) 2.dp else 1.5.dp,
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TomoArt(
                species = species,
                size = 76.dp,
                pose = if (isSelected) TomoPose.HAPPY else TomoPose.DEFAULT,
                modifier = Modifier.graphicsLayer {
                    scaleX = artScale
                    scaleY = artScale
                },
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = species.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = if (isSelected) colors.onPrimaryContainer else colors.onSurface,
                )
                if (isSelected) {
                    Text(
                        text = species.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onPrimaryContainer,
                    )
                }
            }
            if (isSelected) {
                Icon(
                    painterResource(R.drawable.ic_check_circle),
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(24.dp),
                )
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
    Box(contentAlignment = Alignment.Center) {
        GroundedGlow(modifier = Modifier.size(180.dp))
        TomoArt(species, size = 140.dp, pose = TomoPose.HAPPY)
    }
    Text(
        text = stringResource(R.string.nickname_title, species.name),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
    )
    OutlinedTextField(
        value = uiState.nickname,
        onValueChange = onNicknameChange,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        label = { Text(stringResource(R.string.nickname_label)) },
        supportingText = { Text(stringResource(R.string.nickname_rule)) },
        colors = journalTextFieldColors(),
        isError = uiState.error == StarterError.InvalidNickname,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onSave() }),
    )
    uiState.error?.takeIf { it != StarterError.InvalidNickname }?.let { ErrorText(it) }
    if (uiState.isSaving) {
        CircularProgressIndicator()
    } else {
        PrimaryButton(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(if (uiState.error == null) R.string.nickname_confirm else R.string.retry))
        }
        TextButton(onClick = onBack, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(R.string.back))
        }
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
    RuntomoCard {
        Text(
            text = stringResource(R.string.starter_hello, greeting.nickname),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
    }
    Box(contentAlignment = Alignment.Center) {
        GroundedGlow(modifier = Modifier.size(200.dp))
        TomoArt(
            greeting.species,
            size = 160.dp,
            pose = TomoPose.EXCITED,
            modifier = Modifier.graphicsLayer { translationY = offsetY.value.dp.toPx() },
        )
    }
    Text(
        text = stringResource(R.string.starter_hello_body),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
    PrimaryButton(onClick = onFinish, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.starter_hello_continue))
    }
}

private const val HELLO_HOPS = 3
private const val HELLO_PAUSE_MILLIS = 1_800L

@Composable
private fun LoadFailed(error: StarterError?, onRetry: () -> Unit) {
    error?.let { ErrorText(it) }
    SecondaryButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
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
