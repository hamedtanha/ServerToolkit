package de.hamedtanha.servertoolkit.navigation

object DashboardDestination : NavigationDestination {
    override val route: String = "dashboard"
}

object ServerInventoryDestination : NavigationDestination {
    override val route: String = "server_inventory"
}

object AddServerDestination : NavigationDestination {
    override val route: String = "add_server"
}