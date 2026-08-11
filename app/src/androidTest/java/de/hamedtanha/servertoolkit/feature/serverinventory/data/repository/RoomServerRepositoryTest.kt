package de.hamedtanha.servertoolkit.feature.serverinventory.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.ServerEnvironment
import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshConnectionHistoryEntity
import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshTrustedHostKeyEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomServerRepositoryTest {

    private lateinit var database: ServerToolkitDatabase
    private lateinit var repository: RoomServerRepository

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            ServerToolkitDatabase::class.java,
        ).build()
        repository = RoomServerRepository(database.serverDao())
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun saveServer_whenServerDoesNotExist_persistsDomainModel() = runBlocking {
        val server = testServer(id = "server-1", name = "Production Alpha")

        repository.saveServer(server)

        assertEquals(server, repository.getServerById("server-1"))
        assertEquals(listOf(server), repository.observeServers().first())
    }

    @Test
    fun saveServer_whenServerExists_updatesDomainModel() = runBlocking {
        val originalServer = testServer(id = "server-1", name = "Production Alpha")
        val updatedServer = originalServer.copy(
            name = "Production Alpha Updated",
            host = "alpha-updated.example.com",
            sshPort = 2222,
            sshUsername = "admin",
            tags = listOf("production", "updated"),
        )

        repository.saveServer(originalServer)
        repository.saveServer(updatedServer)

        assertEquals(updatedServer, repository.getServerById("server-1"))
        assertEquals(listOf(updatedServer), repository.observeServers().first())
    }

    @Test
    fun saveServer_metadataOnlyUpdate_preservesTrustedHostKeyAndConnectionHistory() = runBlocking {
        val originalServer = testServer(id = "server-1", name = "Production Alpha")
        val trustedHostKey = testTrustedHostKeyEntity(originalServer)
        val history = testConnectionHistoryEntity(originalServer)

        repository.saveServer(originalServer)
        database.sshTrustedHostKeyDao().insertTrustedHostKey(trustedHostKey)
        database.sshConnectionHistoryDao().insertConnectionHistoryEntry(history)

        val updatedServer = originalServer.copy(
            name = "Production Alpha Renamed",
            description = "Updated metadata only",
        )
        repository.saveServer(updatedServer)

        assertEquals(updatedServer, repository.getServerById(originalServer.id))
        assertEquals(
            trustedHostKey,
            database.sshTrustedHostKeyDao().getTrustedHostKey(
                serverId = originalServer.id,
                host = originalServer.host,
                port = originalServer.sshPort,
            ),
        )
        assertEquals(
            history,
            database.sshConnectionHistoryDao().getConnectionHistoryEntry(history.id),
        )
    }

    @Test
    fun saveServer_usernameOnlyUpdate_preservesTrustedHostKeyAndConnectionHistory() = runBlocking {
        val originalServer = testServer(id = "server-1", name = "Production Alpha")
        val trustedHostKey = testTrustedHostKeyEntity(originalServer)
        val history = testConnectionHistoryEntity(originalServer)

        repository.saveServer(originalServer)
        database.sshTrustedHostKeyDao().insertTrustedHostKey(trustedHostKey)
        database.sshConnectionHistoryDao().insertConnectionHistoryEntry(history)

        val updatedServer = originalServer.copy(sshUsername = "admin")
        repository.saveServer(updatedServer)

        assertEquals(updatedServer, repository.getServerById(originalServer.id))
        assertEquals(
            trustedHostKey,
            database.sshTrustedHostKeyDao().getTrustedHostKey(
                serverId = originalServer.id,
                host = originalServer.host,
                port = originalServer.sshPort,
            ),
        )
        assertEquals(
            history,
            database.sshConnectionHistoryDao().getConnectionHistoryEntry(history.id),
        )
    }

    @Test
    fun saveServer_endpointUpdate_preservesHistoryAndDoesNotCascadeDeleteOldEndpointTrust() =
        runBlocking {
            val originalServer = testServer(id = "server-1", name = "Production Alpha")
            val trustedHostKey = testTrustedHostKeyEntity(originalServer)
            val history = testConnectionHistoryEntity(originalServer)

            repository.saveServer(originalServer)
            database.sshTrustedHostKeyDao().insertTrustedHostKey(trustedHostKey)
            database.sshConnectionHistoryDao().insertConnectionHistoryEntry(history)

            val updatedServer = originalServer.copy(
                host = "alpha-new.example.com",
                sshPort = 2222,
            )
            repository.saveServer(updatedServer)

            assertEquals(updatedServer, repository.getServerById(originalServer.id))
            assertEquals(
                history,
                database.sshConnectionHistoryDao().getConnectionHistoryEntry(history.id),
            )
            assertEquals(
                trustedHostKey,
                database.sshTrustedHostKeyDao().getTrustedHostKey(
                    serverId = originalServer.id,
                    host = originalServer.host,
                    port = originalServer.sshPort,
                ),
            )
            assertNull(
                database.sshTrustedHostKeyDao().getTrustedHostKey(
                    serverId = updatedServer.id,
                    host = updatedServer.host,
                    port = updatedServer.sshPort,
                ),
            )
        }

    @Test
    fun deleteServer_whenServerExists_removesDomainModel() = runBlocking {
        val server = testServer(id = "server-1", name = "Production Alpha")

        repository.saveServer(server)
        repository.deleteServer("server-1")

        assertNull(repository.getServerById("server-1"))
        assertTrue(repository.observeServers().first().isEmpty())
    }

    @Test
    fun deleteServer_whenServerHasTrustedHostKey_cascadesTrustedHostKey() = runBlocking {
        val server = testServer(id = "server-1", name = "Production Alpha")

        repository.saveServer(server)
        database.sshTrustedHostKeyDao().insertTrustedHostKey(testTrustedHostKeyEntity(server))

        repository.deleteServer("server-1")

        assertNull(repository.getServerById("server-1"))
        assertNull(
            database.sshTrustedHostKeyDao().getTrustedHostKey(
                serverId = "server-1",
                host = "alpha.example.com",
                port = 22,
            ),
        )
    }

    private fun testTrustedHostKeyEntity(server: Server): SshTrustedHostKeyEntity {
        return SshTrustedHostKeyEntity(
            serverId = server.id,
            host = server.host,
            port = server.sshPort,
            fingerprintAlgorithm = "SHA256",
            fingerprintValue = "abc123",
        )
    }

    private fun testConnectionHistoryEntity(server: Server): SshConnectionHistoryEntity {
        return SshConnectionHistoryEntity(
            id = "history-1",
            serverId = server.id,
            host = server.host,
            port = server.sshPort,
            username = checkNotNull(server.sshUsername),
            status = "SUCCEEDED",
            attemptedAtEpochMillis = 1_000L,
            completedAtEpochMillis = 2_000L,
            connectionError = null,
        )
    }

    private fun testServer(
        id: String,
        name: String,
    ): Server {
        return Server(
            id = id,
            name = name,
            host = "alpha.example.com",
            sshPort = 22,
            sshUsername = "root",
            environment = ServerEnvironment.PRODUCTION,
            category = "Linux",
            tags = listOf("production", "ssh"),
            isFavorite = true,
            description = "Primary production server",
        )
    }
}
