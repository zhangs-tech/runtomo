package com.zhangs_tech.runtomo.main

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.zhangs_tech.runtomo.R
import com.zhangs_tech.runtomo.history.HistoryScreen
import com.zhangs_tech.runtomo.home.HomeScreen
import com.zhangs_tech.runtomo.social.SocialScreen
import com.zhangs_tech.runtomo.tomo.TomosScreen

enum class Tab(@StringRes val labelRes: Int, @DrawableRes val iconRes: Int) {
    HOME(R.string.tab_home, R.drawable.ic_home),
    TOMOS(R.string.tab_tomos, R.drawable.ic_tomos),
    HISTORY(R.string.tab_history, R.drawable.ic_history),
    SOCIAL(R.string.tab_social, R.drawable.ic_social),
}

/** The signed-in app: Home, Tomos, History and Social tabs. Back on another tab returns to Home. */
@Composable
fun MainScreen(onOpenSummon: () -> Unit, onOpenFeed: () -> Unit, modifier: Modifier = Modifier) {
    var selectedTab by rememberSaveable { mutableStateOf(Tab.HOME) }
    BackHandler(enabled = selectedTab != Tab.HOME) { selectedTab = Tab.HOME }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                Tab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = tab == selectedTab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(painterResource(tab.iconRes), contentDescription = null) },
                        label = { Text(stringResource(tab.labelRes)) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).consumeWindowInsets(innerPadding)) {
            when (selectedTab) {
                Tab.HOME -> HomeScreen(onOpenFeed = onOpenFeed)
                Tab.TOMOS -> TomosScreen(onOpenSummon = onOpenSummon)
                Tab.HISTORY -> HistoryScreen()
                Tab.SOCIAL -> SocialScreen()
            }
        }
    }
}
