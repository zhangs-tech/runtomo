package com.zhangs_tech.runtomo.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Welcome : Route

    @Serializable
    data object ConfirmDisplayName : Route

    @Serializable
    data object ChooseStarter : Route

    @Serializable
    data object Home : Route
}
