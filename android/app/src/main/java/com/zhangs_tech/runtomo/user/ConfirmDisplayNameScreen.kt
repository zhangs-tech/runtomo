package com.zhangs_tech.runtomo.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme

@Composable
fun ConfirmDisplayNameScreen(
    modifier: Modifier = Modifier,
    viewModel: ConfirmDisplayNameViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ConfirmDisplayNameContent(
        uiState = uiState,
        onDisplayNameChange = viewModel::onDisplayNameChange,
        onConfirm = viewModel::confirm,
        modifier = modifier,
    )
}

@Composable
fun ConfirmDisplayNameContent(
    uiState: ConfirmDisplayNameUiState,
    onDisplayNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.display_name_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
            )
            if (uiState.isLoading) {
                CircularProgressIndicator()
                return@Column
            }
            OutlinedTextField(
                value = uiState.displayName,
                onValueChange = onDisplayNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.display_name_label)) },
                supportingText = { Text(stringResource(R.string.display_name_rule)) },
                isError = uiState.error == DisplayNameError.Invalid,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onConfirm() }),
            )
            uiState.error?.takeIf { it != DisplayNameError.Invalid }?.let { error ->
                Text(
                    text = stringResource(
                        if (error == DisplayNameError.NoConnection) R.string.error_no_connection else R.string.error_generic,
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
            if (uiState.isSaving) {
                CircularProgressIndicator()
            } else {
                Button(onClick = onConfirm, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.display_name_confirm))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ConfirmDisplayNameContentPreview() {
    RuntomoTheme {
        ConfirmDisplayNameContent(
            uiState = ConfirmDisplayNameUiState(displayName = "Kai Tan", isLoading = false),
            onDisplayNameChange = {},
            onConfirm = {},
        )
    }
}
