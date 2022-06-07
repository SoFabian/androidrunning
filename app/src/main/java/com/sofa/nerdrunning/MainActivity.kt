package com.sofa.nerdrunning

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.sofa.nerdrunning.log.logDebug
import com.sofa.nerdrunning.navigation.MainNavHost
import com.sofa.nerdrunning.permissions.PermissionHandler
import com.sofa.nerdrunning.ui.theme.NerdRunningTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logDebug("MainActivity", "onCreate")
        setContent {
            val multiplePermissionsState = rememberMultiplePermissionsState(PERMISSIONS)
            val navController = rememberNavController()
            NerdRunningTheme {
                PermissionHandler(multiplePermissionsState, { navigateToSettingsScreen() }) {
                    MainNavHost(navController)
                }
            }
        }
    }

    private fun navigateToSettingsScreen() {
        application.startActivity(
            Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", application.packageName, null)
            )
        )
    }

    companion object {
        private val PERMISSIONS = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH,
        )
    }

}

