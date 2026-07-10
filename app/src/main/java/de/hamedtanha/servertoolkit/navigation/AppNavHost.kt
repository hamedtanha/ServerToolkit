package de.hamedtanha.servertoolkit.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.hamedtanha.servertoolkit.feature.dashboard.presentation.screen.DashboardRoute
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.screen.AddServerRoute
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.screen.EditServerRoute
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.screen.ServerInventoryRoute
import de.hamedtanha.servertoolkit.feature.ssh.presentation.screen.SshConnectionHistoryRoute
import de.hamedtanha.servertoolkit.feature.ssh.presentation.screen.SshRoute

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
            ServerInventoryRoute(
                onAddServerClick = {
                    navController.navigate(AddServerDestination.route) {
                        launchSingleTop = true
                    }
                },
                onEditServerClick = { serverId ->
                    navController.navigate(EditServerDestination.createRoute(serverId)) {
                        launchSingleTop = true
                    }
                },
                onConnectServerClick = { serverId ->
                    navController.navigate(SshDestination.createRoute(serverId)) {
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(route = AddServerDestination.route) {
            AddServerRoute(
                onNavigateBack = {
                    navController.navigateUp()
                },
            )
        }

        composable(route = EditServerDestination.route) {
            EditServerRoute(
                onNavigateBack = {
                    navController.navigateUp()
                },
            )
        }

        composable(route = SshDestination.route) { backStackEntry ->
            SshRoute(
                onNavigateBack = {
                    navController.navigateUp()
                },
                onOpenConnectionHistory = {
                    val serverId = checkNotNull(
                        backStackEntry.arguments?.getString(
                            SshDestination.SERVER_ID_ARGUMENT,
                        ),
                    )
                    navController.navigate(
                        SshConnectionHistoryDestination.createRoute(serverId),
                    ) {
                        launchSingleTop = true
                    }
                },
            )
        }

        composable(route = SshConnectionHistoryDestination.route) {
            SshConnectionHistoryRoute(
                onNavigateBack = {
                    navController.navigateUp()
                },
            )
        }
    }
}
