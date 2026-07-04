package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state

import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.ServerEnvironment
import org.junit.Assert.assertEquals
import org.junit.Test

class ServerInventoryFilterMatcherTest {

    @Test
    fun applyServerInventoryFilter_whenFilterIsEmpty_returnsAllServers() {
        val servers = testServers()

        val result = servers.applyServerInventoryFilter(ServerInventoryFilter())

        assertEquals(servers, result)
    }

    @Test
    fun applyServerInventoryFilter_whenSearchMatchesName_returnsMatchingServers() {
        val result = testServers().applyServerInventoryFilter(
            ServerInventoryFilter(searchQuery = "production"),
        )

        assertEquals(listOf(testServers()[0]), result)
    }

    @Test
    fun applyServerInventoryFilter_whenSearchMatchesHost_returnsMatchingServers() {
        val result = testServers().applyServerInventoryFilter(
            ServerInventoryFilter(searchQuery = "beta.example"),
        )

        assertEquals(listOf(testServers()[1]), result)
    }

    @Test
    fun applyServerInventoryFilter_whenSearchMatchesUsername_returnsMatchingServers() {
        val result = testServers().applyServerInventoryFilter(
            ServerInventoryFilter(searchQuery = "deploy"),
        )

        assertEquals(listOf(testServers()[1]), result)
    }

    @Test
    fun applyServerInventoryFilter_whenSearchMatchesTag_returnsMatchingServers() {
        val result = testServers().applyServerInventoryFilter(
            ServerInventoryFilter(searchQuery = "edge"),
        )

        assertEquals(listOf(testServers()[2]), result)
    }

    @Test
    fun applyServerInventoryFilter_whenEnvironmentIsSelected_returnsMatchingServers() {
        val result = testServers().applyServerInventoryFilter(
            ServerInventoryFilter(environment = ServerEnvironment.STAGING),
        )

        assertEquals(listOf(testServers()[1]), result)
    }

    @Test
    fun applyServerInventoryFilter_whenFavoritesOnlyIsEnabled_returnsFavoriteServers() {
        val result = testServers().applyServerInventoryFilter(
            ServerInventoryFilter(favoritesOnly = true),
        )

        assertEquals(listOf(testServers()[0], testServers()[2]), result)
    }

    @Test
    fun applyServerInventoryFilter_whenFiltersAreCombined_returnsIntersection() {
        val result = testServers().applyServerInventoryFilter(
            ServerInventoryFilter(
                searchQuery = "web",
                environment = ServerEnvironment.PRODUCTION,
                favoritesOnly = true,
            ),
        )

        assertEquals(listOf(testServers()[0]), result)
    }

    private fun testServers(): List<Server> {
        return listOf(
            Server(
                id = "server-1",
                name = "Production Web",
                host = "alpha.example.com",
                sshUsername = "admin",
                environment = ServerEnvironment.PRODUCTION,
                category = "Linux",
                tags = listOf("web", "critical"),
                isFavorite = true,
            ),
            Server(
                id = "server-2",
                name = "Staging Database",
                host = "beta.example.com",
                sshUsername = "deploy",
                environment = ServerEnvironment.STAGING,
                category = "Database",
                tags = listOf("postgres"),
                isFavorite = false,
            ),
            Server(
                id = "server-3",
                name = "Lab Edge",
                host = "edge.local",
                sshUsername = null,
                environment = ServerEnvironment.HOMELAB,
                category = "Network",
                tags = listOf("edge", "router"),
                isFavorite = true,
            ),
        )
    }
}
