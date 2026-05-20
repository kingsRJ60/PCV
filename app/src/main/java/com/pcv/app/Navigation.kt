package com.pcv.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home      : Screen("home",      "Home",     Icons.Filled.Dashboard)
    object Audio     : Screen("audio",     "Audio",    Icons.Filled.MusicNote)
    object Video     : Screen("video",     "Video",    Icons.Filled.VideoFile)
    object Ads       : Screen("ads",       "Ads",      Icons.Filled.Campaign)
    object Scheduler : Screen("scheduler", "Schedule", Icons.Filled.Schedule)
    object CodeLab   : Screen("codelab",   "Code Lab", Icons.Filled.Code)
}

val BOTTOM_NAV_ITEMS = listOf(
    Screen.Home,
    Screen.Audio,
    Screen.Video,
    Screen.Ads,
    Screen.Scheduler,
    Screen.CodeLab
)
