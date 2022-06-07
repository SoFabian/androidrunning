package com.sofa.nerdrunning.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sofa.nerdrunning.finishedrun.FinishedRunView
import com.sofa.nerdrunning.finishedrun.ListFinishedRunsView
import com.sofa.nerdrunning.home.HomeView
import com.sofa.nerdrunning.liverun.LiveRunView

@Composable
fun MainNavHost(navController: NavHostController) {
    val navigation = Navigation(navController)

    NavHost(navController, startDestination = Routes.home) {

        composable(Routes.home) {
            HomeView(navigation)
        }

        composable(Routes.liveRun) {
            LiveRunView(navigation)
        }

        composable(Routes.finishedRun,
            arguments = listOf(navArgument(Routes.Args.runId) { type = NavType.LongType })
        ) {
            val runId = it.arguments?.getLong(Routes.Args.runId)
            if (runId != null) {
                FinishedRunView(runId, navigation)
            }
        }

        composable(Routes.listFinishedRuns) {
            ListFinishedRunsView(navigation)
        }
    }
}
