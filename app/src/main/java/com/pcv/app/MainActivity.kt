package com.pcv.app

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.pcv.app.codelab.CodeImprovementEngine
import com.pcv.app.service.MediaSupervisorService
import com.pcv.app.ui.screens.*
import com.pcv.app.ui.theme.PCVTheme

class MainActivity : ComponentActivity() {

    private var service: MediaSupervisorService? = null
    private var isBound = false

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(n: ComponentName?, b: IBinder?) {
            service = (b as MediaSupervisorService.LocalBinder).getService()
            isBound = true
        }
        override fun onServiceDisconnected(n: ComponentName?) { isBound = false }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        ensureOverlayPermission()
        ensureNotificationPermission()
        startAndBind()

        val engine = CodeImprovementEngine(applicationContext)

        setContent {
            PCVTheme {
                val navController = rememberNavController()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            val backStack by navController.currentBackStackEntryAsState()
                            val current = backStack?.destination?.route
                            BOTTOM_NAV_ITEMS.forEach { screen ->
                                NavigationBarItem(
                                    selected  = current == screen.route,
                                    onClick   = {
                                        navController.navigate(screen.route) {
                                            launchSingleTop = true
                                            restoreState    = true
                                        }
                                    },
                                    icon  = { Icon(screen.icon, contentDescription = screen.label) },
                                    label = { Text(screen.label) }
                                )
                            }
                        }
                    }
                ) { padding ->
                    NavHost(
                        navController    = navController,
                        startDestination = Screen.Home.route,
                        modifier         = Modifier.padding(padding)
                    ) {
                        composable(Screen.Home.route)      { HomeScreen(service) }
                        composable(Screen.Audio.route)     { AudioScreen(service) }
                        composable(Screen.Video.route)     { VideoScreen(service) }
                        composable(Screen.Ads.route)       { AdsScreen(service) }
                        composable(Screen.Scheduler.route) { SchedulerScreen(service) }
                        composable(Screen.CodeLab.route)   { CodeLabScreen(engine) }
                    }
                }
            }
        }
    }

    private fun ensureOverlayPermission() {
        if (!Settings.canDrawOverlays(this))
            startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")))
    }

    private fun ensureNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
    }

    private fun startAndBind() {
        val intent = Intent(this, MediaSupervisorService::class.java)
        startForegroundService(intent)
        bindService(intent, conn, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) { unbindService(conn); isBound = false }
    }
}
