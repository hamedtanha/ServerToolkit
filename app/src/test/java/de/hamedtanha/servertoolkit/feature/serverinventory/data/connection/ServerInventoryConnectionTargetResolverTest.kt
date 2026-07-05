package de.hamedtanha.servertoolkit.feature.serverinventory.data.connection

import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetInvalidReason
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ServerInventoryConnectionTargetResolverTest {

    @Test
    fun `resolves server inventory metadata into connection target`() = runBlocking {
        val resolver = ServerInventoryConnectionTargetResolver(
            serverRepository = FakeServerRepository(
                server = Server(
                    id = "server-1",
                    name = "Production",
                    host = " example.com ",
                    sshPort = 2222,
                    sshUsername = " admin ",
                ),
            ),
        )

        val result = resolver.resolve("server-1") as ConnectionTargetResolution.Resolved

        assertEquals("server-1", result.target.serverId)
        assertEquals("example.com", result.target.host)
        assertEquals(2222, result.target.port)
        assertEquals("admin", result.target.username)
    }

    @Test
    fun `returns not found when server does not exist`() = runBlocking {
        val resolver = ServerInventoryConnectionTargetResolver(
            serverRepository = FakeServerRepository(server = null),
        )

        val result = resolver.resolve("missing-server")

        assertEquals(ConnectionTargetResolution.NotFound, result)
    }

    @Test
    fun `returns invalid when host is blank`() = runBlocking {
        val resolver = ServerInventoryConnectionTargetResolver(
            serverRepository = FakeServerRepository(
                server = Server(
                    id = "server-1",
                    name = "Broken",
                    host = " ",
                    sshUsername = "admin",
                ),
            ),
        )

        val result = resolver.resolve("server-1") as ConnectionTargetResolution.Invalid

        assertEquals(ConnectionTargetInvalidReason.MissingHost, result.reason)
    }

    @Test
    fun `returns invalid when port is outside valid range`() = runBlocking {
        val resolver = ServerInventoryConnectionTargetResolver(
            serverRepository = FakeServerRepository(
                server = Server(
                    id = "server-1",
                    name = "Broken",
                    host = "example.com",
                    sshPort = 0,
                    sshUsername = "admin",
                ),
            ),
        )

        val result = resolver.resolve("server-1") as ConnectionTargetResolution.Invalid

        assertEquals(ConnectionTargetInvalidReason.InvalidPort, result.reason)
    }

    @Test
    fun `returns invalid when username is missing`() = runBlocking {
        val resolver = ServerInventoryConnectionTargetResolver(
            serverRepository = FakeServerRepository(
                server = Server(
                    id = "server-1",
                    name = "Broken",
                    host = "example.com",
                    sshUsername = null,
                ),
            ),
        )

        val result = resolver.resolve("server-1") as ConnectionTargetResolution.Invalid

        assertEquals(ConnectionTargetInvalidReason.MissingUsername, result.reason)
    }

    private class FakeServerRepository(
        private val server: Server?,
    ) : ServerRepository {

        override fun observeServers(): Flow<List<Server>> {
            return flowOf(server?.let(::listOf).orEmpty())
        }

        override suspend fun getServerById(serverId: String): Server? {
            return server?.takeIf { it.id == serverId }
        }

        override suspend fun saveServer(server: Server) = Unit

        override suspend fun deleteServer(serverId: String) = Unit
    }
}
