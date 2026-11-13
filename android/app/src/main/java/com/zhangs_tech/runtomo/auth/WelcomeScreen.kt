package com.zhangs_tech.runtomo.auth

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.zhangs_tech.runtomo.tomo.Rarity
import com.zhangs_tech.runtomo.tomo.Species
import com.zhangs_tech.runtomo.tomo.TomoArt
import com.zhangs_tech.runtomo.tomo.TomoPose
import com.zhangs_tech.runtomo.ui.components.GroundedGlow
import com.zhangs_tech.runtomo.ui.components.PrimaryButton
import com.zhangs_tech.runtomo.ui.components.RuntomoCard
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f, fill = false))
            StarterTrioHero()
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.choose_starter_intro),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            uiState.error?.let { error ->
                RuntomoCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                ) {
                    Text(
                        text = stringResource(error.messageRes),
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            if (uiState.isSigningIn) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
            } else {
                PrimaryButton(onClick = onSignInClick, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text(stringResource(R.string.sign_in_with_google))
                }
            }
            Spacer(Modifier.weight(1f, fill = false))
        }
    }
}

@Composable
private fun StarterTrioHero() {
    Box(contentAlignment = Alignment.Center) {
        GroundedGlow(modifier = Modifier.size(width = 260.dp, height = 176.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy((-16).dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            TomoArt(rockyHero, size = 96.dp, pose = TomoPose.HAPPY, modifier = Modifier.padding(bottom = 6.dp))
            TomoArt(luppyHero, size = 128.dp, pose = TomoPose.EXCITED)
            TomoArt(aeroblastHero, size = 96.dp, pose = TomoPose.HAPPY, modifier = Modifier.padding(bottom = 6.dp))
        }
    }
}

private val luppyHero = Species("luppy", "Luppy", Rarity.COMMON, "")
private val rockyHero = Species("rocky", "Rocky", Rarity.COMMON, "")
private val aeroblastHero = Species("aeroblast", "Aeroblast", Rarity.COMMON, "")

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

@Preview(showBackground = true)
@Composable
private fun WelcomeIdlePreview() {
    RuntomoTheme {
        WelcomeContent(uiState = WelcomeUiState(), onSignInClick = {})
    }
}
