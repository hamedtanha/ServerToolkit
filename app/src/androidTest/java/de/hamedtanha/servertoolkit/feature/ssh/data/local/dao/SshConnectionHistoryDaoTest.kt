package de.hamedtanha.servertoolkit.feature.ssh.data.local.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.entity.ServerEntity
import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshConnectionHistoryEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SshConnectionHistoryDaoTest {

    private lateinit var database: ServerToolkitDatabase
    private lateinit var connectionHistoryDao: SshConnectionHistoryDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            ServerToolkitDatabase::class.java,
        ).build()
        connectionHistoryDao = database.sshConnectionHistoryDao()

        runBlocking {
            database.serverDao().upsertServer(testServerEntity())
            database.serverDao().upsertServer(
                testServerEntity(
                    id = "server-2",
                    host = "secondary.example.com",
                ),
            )
        }
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertConnectionHistoryEntry_whenEntryDoesNotExist_persistsEntity() = runBlocking {
        val entity = connectionHistoryEntity()

        connectionHistoryDao.insertConnectionHistoryEntry(entity)

        assertEquals(
            entity,
            connectionHistoryDao.getConnectionHistoryEntry("history-1"),
        )
    }

    @Test
    fun observeConnectionHistory_returnsNewestEntriesFirst() = runBlocking {
        val olderEntry = connectionHistoryEntity(
            id = "history-1",
            attemptedAtEpochMillis = 1_000,
        )
        val newerEntry = connectionHistoryEntity(
            id = "history-2",
            attemptedAtEpochMillis = 2_000,
        )

        connectionHistoryDao.insertConnectionHistoryEntry(olderEntry)
        connectionHistoryDao.insertConnectionHistoryEntry(newerEntry)

        assertEquals(
            listOf(newerEntry, olderEntry),
            connectionHistoryDao.observeConnectionHistory().first(),
        )
    }

    @Test
    fun observeConnectionHistoryForServer_filtersByServerAndReturnsNewestEntriesFirst() = runBlocking {
        val firstServerOlderEntry = connectionHistoryEntity(
            id = "history-1",
            serverId = "server-1",
            attemptedAtEpochMillis = 1_000,
        )
        val firstServerNewerEntry = connectionHistoryEntity(
            id = "history-2",
            serverId = "server-1",
            attemptedAtEpochMillis = 2_000,
        )
        val secondServerEntry = connectionHistoryEntity(
            id = "history-3",
            serverId = "server-2",
            host = "secondary.example.com",
            attemptedAtEpochMillis = 3_000,
        )

        connectionHistoryDao.insertConnectionHistoryEntry(firstServerOlderEntry)
        connectionHistoryDao.insertConnectionHistoryEntry(firstServerNewerEntry)
        connectionHistoryDao.insertConnectionHistoryEntry(secondServerEntry)

        assertEquals(
            listOf(firstServerNewerEntry, firstServerOlderEntry),
            connectionHistoryDao.observeConnectionHistoryForServer("server-1").first(),
        )
    }

    @Test
    fun insertConnectionHistoryEntry_whenEntryAlreadyExists_doesNotReplaceSilently() = runBlocking {
        val originalEntity = connectionHistoryEntity(status = "Connected")
        val changedEntity = connectionHistoryEntity(status = "Cancelled")

        connectionHistoryDao.insertConnectionHistoryEntry(originalEntity)

        try {
            connectionHistoryDao.insertConnectionHistoryEntry(changedEntity)
            fail("Expected SQLiteConstraintException")
        } catch (error: SQLiteConstraintException) {
            assertEquals(
                originalEntity,
                connectionHistoryDao.getConnectionHistoryEntry("history-1"),
            )
        }
    }

    @Test
    fun deleteConnectionHistoryEntry_whenEntryExists_removesEntity() = runBlocking {
        connectionHistoryDao.insertConnectionHistoryEntry(connectionHistoryEntity())

        connectionHistoryDao.deleteConnectionHistoryEntry("history-1")

        assertNull(connectionHistoryDao.getConnectionHistoryEntry("history-1"))
    }

    @Test
    fun deleteServer_whenHistoryExists_cascadeDeletesHistory() = runBlocking {
        connectionHistoryDao.insertConnectionHistoryEntry(connectionHistoryEntity())

        database.serverDao().deleteServer("server-1")

        assertEquals(
            emptyList<SshConnectionHistoryEntity>(),
            connectionHistoryDao.observeConnectionHistory().first(),
        )
    }

    private fun testServerEntity(
        id: String = "server-1",
        host: String = "example.com",
    ): ServerEntity {
        return ServerEntity(
            id = id,
            name = "Production",
            host = host,
            sshPort = 22,
            sshUsername = "admin",
            environment = "PRODUCTION",
            category = "Linux",
            tags = "production",
            isFavorite = true,
            description = "Primary server",
        )
    }

    private fun connectionHistoryEntity(
        id: String = "history-1",
        serverId: String = "server-1",
        host: String = "example.com",
        status: String = "Connected",
        attemptedAtEpochMillis: Long = 1_000,
    ): SshConnectionHistoryEntity {
        return SshConnectionHistoryEntity(
            id = id,
            serverId = serverId,
            host = host,
            port = 22,
            username = "admin",
            status = status,
            attemptedAtEpochMillis = attemptedAtEpochMillis,
            completedAtEpochMillis = attemptedAtEpochMillis + 1_000,
            connectionError = null,
        )
    }
}
