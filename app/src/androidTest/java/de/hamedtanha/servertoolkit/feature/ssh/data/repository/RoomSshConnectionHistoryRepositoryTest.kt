package de.hamedtanha.servertoolkit.feature.ssh.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.entity.ServerEntity
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryEntry
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryStatus
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
class RoomSshConnectionHistoryRepositoryTest {

    private lateinit var database: ServerToolkitDatabase
    private lateinit var repository: RoomSshConnectionHistoryRepository

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            ServerToolkitDatabase::class.java,
        ).build()
        repository = RoomSshConnectionHistoryRepository(database.sshConnectionHistoryDao())

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
    fun saveConnectionHistoryEntry_whenEntryDoesNotExist_persistsDomainModel() = runBlocking {
        val entry = connectionHistoryEntry()

        repository.saveConnectionHistoryEntry(entry)

        assertEquals(
            entry,
            repository.getConnectionHistoryEntry("history-1"),
        )
    }

    @Test
    fun observeConnectionHistory_returnsMappedNewestEntriesFirst() = runBlocking {
        val olderEntry = connectionHistoryEntry(
            id = "history-1",
            attemptedAtEpochMillis = 1_000,
        )
        val newerEntry = connectionHistoryEntry(
            id = "history-2",
            attemptedAtEpochMillis = 2_000,
        )

        repository.saveConnectionHistoryEntry(olderEntry)
        repository.saveConnectionHistoryEntry(newerEntry)

        assertEquals(
            listOf(newerEntry, olderEntry),
            repository.observeConnectionHistory().first(),
        )
    }

    @Test
    fun observeConnectionHistoryForServer_filtersMappedEntriesByServer() = runBlocking {
        val firstServerEntry = connectionHistoryEntry(
            id = "history-1",
            serverId = "server-1",
        )
        val secondServerEntry = connectionHistoryEntry(
            id = "history-2",
            serverId = "server-2",
            host = "secondary.example.com",
        )

        repository.saveConnectionHistoryEntry(firstServerEntry)
        repository.saveConnectionHistoryEntry(secondServerEntry)

        assertEquals(
            listOf(firstServerEntry),
            repository.observeConnectionHistoryForServer("server-1").first(),
        )
    }

    @Test
    fun saveConnectionHistoryEntry_whenEntryAlreadyExists_doesNotReplaceSilently() = runBlocking {
        val originalEntry = connectionHistoryEntry(status = SshConnectionHistoryStatus.Connected)
        val changedEntry = connectionHistoryEntry(status = SshConnectionHistoryStatus.Cancelled)

        repository.saveConnectionHistoryEntry(originalEntry)

        try {
            repository.saveConnectionHistoryEntry(changedEntry)
            fail("Expected SQLiteConstraintException")
        } catch (error: SQLiteConstraintException) {
            assertEquals(
                originalEntry,
                repository.getConnectionHistoryEntry("history-1"),
            )
        }
    }

    @Test
    fun deleteConnectionHistoryEntry_whenEntryExists_removesDomainModel() = runBlocking {
        repository.saveConnectionHistoryEntry(connectionHistoryEntry())

        repository.deleteConnectionHistoryEntry("history-1")

        assertNull(repository.getConnectionHistoryEntry("history-1"))
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

    private fun connectionHistoryEntry(
        id: String = "history-1",
        serverId: String = "server-1",
        host: String = "example.com",
        status: SshConnectionHistoryStatus = SshConnectionHistoryStatus.Connected,
        attemptedAtEpochMillis: Long = 1_000,
    ): SshConnectionHistoryEntry {
        return SshConnectionHistoryEntry(
            id = id,
            serverId = serverId,
            host = host,
            port = 22,
            username = "admin",
            status = status,
            attemptedAtEpochMillis = attemptedAtEpochMillis,
            completedAtEpochMillis = attemptedAtEpochMillis + 1_000,
        )
    }
}
