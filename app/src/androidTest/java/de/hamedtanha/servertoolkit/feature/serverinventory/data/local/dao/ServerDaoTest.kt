package de.hamedtanha.servertoolkit.feature.serverinventory.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.entity.ServerEntity
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
class ServerDaoTest {

    private lateinit var database: ServerToolkitDatabase
    private lateinit var serverDao: ServerDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            ServerToolkitDatabase::class.java,
        ).build()
        serverDao = database.serverDao()

        database.openHelper.writableDatabase.query("PRAGMA foreign_keys").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun upsertServer_whenServerDoesNotExist_insertsServer() = runBlocking {
        val server = testServerEntity(id = "server-1", name = "Production Alpha")

        serverDao.upsertServer(server)

        assertEquals(server, serverDao.getServerById("server-1"))
        assertEquals(listOf(server), serverDao.observeServers().first())
    }

    @Test
    fun upsertServer_whenServerExists_updatesServerWithoutReplacingParentRow() = runBlocking {
        val originalServer = testServerEntity(id = "server-1", name = "Production Alpha")
        val updatedServer = originalServer.copy(
            name = "Production Alpha Updated",
            host = "alpha-updated.example.com",
            sshPort = 2222,
            sshUsername = "admin",
        )

        serverDao.upsertServer(originalServer)
        serverDao.upsertServer(updatedServer)

        assertEquals(updatedServer, serverDao.getServerById("server-1"))
        assertEquals(listOf(updatedServer), serverDao.observeServers().first())
    }

    @Test
    fun upsertServer_metadataOnlyUpdate_preservesTrustedHostKeyAndConnectionHistory() = runBlocking {
        val originalServer = testServerEntity(id = "server-1", name = "Production Alpha")
        val trustedHostKey = testTrustedHostKeyEntity(originalServer)
        val history = testConnectionHistoryEntity(originalServer)

        serverDao.upsertServer(originalServer)
        insertChildEvidence(trustedHostKey, history)

        val updatedServer = originalServer.copy(
            name = "Production Alpha Renamed",
            isFavorite = true,
            description = "Updated metadata only",
        )
        serverDao.upsertServer(updatedServer)

        assertEquals(updatedServer, serverDao.getServerById(originalServer.id))
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
    fun upsertServer_usernameOnlyUpdate_preservesTrustedHostKeyAndConnectionHistory() = runBlocking {
        val originalServer = testServerEntity(id = "server-1", name = "Production Alpha")
        val trustedHostKey = testTrustedHostKeyEntity(originalServer)
        val history = testConnectionHistoryEntity(originalServer)

        serverDao.upsertServer(originalServer)
        insertChildEvidence(trustedHostKey, history)

        val updatedServer = originalServer.copy(sshUsername = "admin")
        serverDao.upsertServer(updatedServer)

        assertEquals(updatedServer, serverDao.getServerById(originalServer.id))
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
    fun upsertServer_endpointUpdate_preservesHistorySnapshotAndDoesNotCascadeDeleteOldTrust() = runBlocking {
        val originalServer = testServerEntity(id = "server-1", name = "Production Alpha")
        val trustedHostKey = testTrustedHostKeyEntity(originalServer)
        val history = testConnectionHistoryEntity(originalServer)

        serverDao.upsertServer(originalServer)
        insertChildEvidence(trustedHostKey, history)

        val updatedServer = originalServer.copy(
            host = "alpha-new.example.com",
            sshPort = 2222,
        )
        serverDao.upsertServer(updatedServer)

        assertEquals(updatedServer, serverDao.getServerById(originalServer.id))
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
    fun upsertServer_repeatedUpdates_preserveChildEvidence() = runBlocking {
        val originalServer = testServerEntity(id = "server-1", name = "Production Alpha")
        val trustedHostKey = testTrustedHostKeyEntity(originalServer)
        val history = testConnectionHistoryEntity(originalServer)

        serverDao.upsertServer(originalServer)
        insertChildEvidence(trustedHostKey, history)

        val firstUpdate = originalServer.copy(name = "Production Alpha 2")
        val secondUpdate = firstUpdate.copy(sshUsername = "admin")
        val thirdUpdate = secondUpdate.copy(description = "Third update")

        serverDao.upsertServer(firstUpdate)
        serverDao.upsertServer(secondUpdate)
        serverDao.upsertServer(thirdUpdate)

        assertEquals(thirdUpdate, serverDao.getServerById(originalServer.id))
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
    fun deleteServer_whenServerHasSshChildEvidence_cascadesTrustedHostKeyAndConnectionHistory() =
        runBlocking {
            val server = testServerEntity(id = "server-1", name = "Production Alpha")
            val trustedHostKey = testTrustedHostKeyEntity(server)
            val history = testConnectionHistoryEntity(server)

            serverDao.upsertServer(server)
            insertChildEvidence(trustedHostKey, history)

            serverDao.deleteServer(server.id)

            assertNull(serverDao.getServerById(server.id))
            assertNull(
                database.sshTrustedHostKeyDao().getTrustedHostKey(
                    serverId = server.id,
                    host = server.host,
                    port = server.sshPort,
                ),
            )
            assertNull(
                database.sshConnectionHistoryDao().getConnectionHistoryEntry(history.id),
            )
            assertTrue(serverDao.observeServers().first().isEmpty())
        }

    private suspend fun insertChildEvidence(
        trustedHostKey: SshTrustedHostKeyEntity,
        history: SshConnectionHistoryEntity,
    ) {
        database.sshTrustedHostKeyDao().insertTrustedHostKey(trustedHostKey)
        database.sshConnectionHistoryDao().insertConnectionHistoryEntry(history)
    }

    private fun testTrustedHostKeyEntity(server: ServerEntity): SshTrustedHostKeyEntity {
        return SshTrustedHostKeyEntity(
            serverId = server.id,
            host = server.host,
            port = server.sshPort,
            fingerprintAlgorithm = "SHA256",
            fingerprintValue = "abc123",
        )
    }

    private fun testConnectionHistoryEntity(server: ServerEntity): SshConnectionHistoryEntity {
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

    private fun testServerEntity(
        id: String,
        name: String,
    ): ServerEntity {
        return ServerEntity(
            id = id,
            name = name,
            host = "alpha.example.com",
            sshPort = 22,
            sshUsername = "root",
            environment = "PRODUCTION",
            category = "Linux",
            tags = "production",
            isFavorite = false,
            description = "Primary production server",
        )
    }
}
