package de.hamedtanha.servertoolkit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.hamedtanha.servertoolkit.feature.dashboard.presentation.screen.DashboardRoute

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = DashboardDestination.route,
    ) {
        composable(route = DashboardDestination.route) {
            DashboardRoute()
        }
    }
}
