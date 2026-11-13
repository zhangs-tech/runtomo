package com.zhangs_tech.runtomo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.zhangs_tech.runtomo.auth.SessionState
import com.zhangs_tech.runtomo.auth.WelcomeScreen
import com.zhangs_tech.runtomo.food.FeedScreen
import com.zhangs_tech.runtomo.main.MainScreen
import com.zhangs_tech.runtomo.starter.ChooseStarterScreen
import com.zhangs_tech.runtomo.summon.SummonScreen
import com.zhangs_tech.runtomo.user.ConfirmDisplayNameScreen

fun SessionState.rootRoute(): Route? = when (this) {
    SessionState.Loading -> null
    SessionState.SignedOut -> Route.Welcome
    SessionState.NeedsDisplayName -> Route.ConfirmDisplayName
    SessionState.NeedsStarterTomo -> Route.ChooseStarter
    SessionState.Ready -> Route.Main
}

/**
 * Shows the screens for the current [sessionState]. When the session state changes, the back
 * stack restarts at the new root, so Back cannot return to a screen from an earlier state.
 */
@Composable
fun RuntomoNavDisplay(sessionState: SessionState, modifier: Modifier = Modifier) {
    val rootRoute = sessionState.rootRoute() ?: return
    val backStack = key(rootRoute) { rememberNavBackStack(rootRoute) }

    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            entry<Route.Welcome> { WelcomeScreen() }
            entry<Route.ConfirmDisplayName> { ConfirmDisplayNameScreen() }
            entry<Route.ChooseStarter> { ChooseStarterScreen() }
            entry<Route.Main> {
                MainScreen(onOpenSummon = { backStack.add(Route.Summon) }, onOpenFeed = { backStack.add(Route.Feed) })
            }
            entry<Route.Summon> { SummonScreen(onBack = { backStack.removeLastOrNull() }) }
            entry<Route.Feed> { FeedScreen(onBack = { backStack.removeLastOrNull() }) }
        },
    )
}
