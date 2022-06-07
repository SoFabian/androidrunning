package com.sofa.nerdrunning.navigation

import androidx.navigation.NavController

class Navigation(private val navController: NavController) {

    fun goBackToHome() {
        goBackTo(Routes.home)
    }

    fun goBackTo(route: String) {
        navController.popBackStack(route, inclusive = false)
    }

    fun goTo(route: String) {
        navController.navigate(route)
    }

}