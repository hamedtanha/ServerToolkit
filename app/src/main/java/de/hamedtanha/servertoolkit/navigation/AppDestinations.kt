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

object EditServerDestination : NavigationDestination {
    const val SERVER_ID_ARGUMENT: String = "serverId"

    override val route: String = "edit_server/{$SERVER_ID_ARGUMENT}"

    fun createRoute(serverId: String): String {
        return "edit_server/$serverId"
    }
}

object SshDestination : NavigationDestination {
    const val SERVER_ID_ARGUMENT: String = "serverId"

    override val route: String = "ssh/{$SERVER_ID_ARGUMENT}"

    fun createRoute(serverId: String): String {
        return "ssh/$serverId"
    }
}

object SshConnectionHistoryDestination : NavigationDestination {
    const val SERVER_ID_ARGUMENT: String = "serverId"

    override val route: String = "ssh_history/{$SERVER_ID_ARGUMENT}"

    fun createRoute(serverId: String): String {
        return "ssh_history/$serverId"
    }
}
