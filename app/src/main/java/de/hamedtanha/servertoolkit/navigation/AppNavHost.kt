package de.hamedtanha.servertoolkit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.hamedtanha.servertoolkit.feature.dashboard.presentation.screen.DashboardRoute
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.screen.ServerInventoryRoute

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = DashboardDestination.route,
    ) {
        composable(route = DashboardDestination.route) {
            DashboardRoute(
                onOpenServerInventory = {
                    navController.navigate(ServerInventoryDestination.route) {
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(route = ServerInventoryDestination.route) {
            ServerInventoryRoute()
        }
    }
}
