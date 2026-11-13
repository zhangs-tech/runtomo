package com.zhangs_tech.runtomo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zhangs_tech.runtomo.navigation.RuntomoNavDisplay
import com.zhangs_tech.runtomo.ui.theme.RuntomoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val sessionState by viewModel.sessionState.collectAsStateWithLifecycle()
            RuntomoTheme {
                RuntomoNavDisplay(sessionState = sessionState)
            }
        }
    }
}
