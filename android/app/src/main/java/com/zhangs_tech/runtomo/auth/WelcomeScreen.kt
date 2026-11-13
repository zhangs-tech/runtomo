package com.zhangs_tech.runtomo.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme

@Composable
fun WelcomeScreen(
    modifier: Modifier = Modifier,
    viewModel: WelcomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    WelcomeContent(
        uiState = uiState,
        onSignInClick = { viewModel.signIn(context) },
        modifier = modifier,
    )
}

@Composable
fun WelcomeContent(
    uiState: WelcomeUiState,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(stringResource(R.string.welcome_title), style = MaterialTheme.typography.headlineMedium)
            uiState.error?.let { error ->
                Text(
                    text = stringResource(error.messageRes),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }
            if (uiState.isSigningIn) {
                CircularProgressIndicator()
            } else {
                Button(onClick = onSignInClick) {
                    Text(stringResource(R.string.sign_in_with_google))
                }
            }
        }
    }
}

@get:StringRes
private val SignInError.messageRes: Int
    get() = when (this) {
        SignInError.NoConnection -> R.string.error_no_connection
        SignInError.NoGoogleAccount -> R.string.error_no_google_account
        SignInError.SignInFailed -> R.string.error_sign_in_failed
    }

@Preview(showBackground = true)
@Composable
private fun WelcomeContentPreview() {
    RuntomoTheme {
        WelcomeContent(uiState = WelcomeUiState(error = SignInError.NoConnection), onSignInClick = {})
    }
}
