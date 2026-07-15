package de.hamedtanha.servertoolkit.feature.savedcommands.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
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
class RoomSavedCommandRepositoryTest {

    private lateinit var database: ServerToolkitDatabase
    private lateinit var repository: RoomSavedCommandRepository

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            ServerToolkitDatabase::class.java,
        ).build()
        repository = RoomSavedCommandRepository(database.savedCommandDao())
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun saveSavedCommand_whenCommandDoesNotExist_persistsExactDomainModel() = runBlocking {
        val command = savedCommand(command = "  df -h\n")

        repository.saveSavedCommand(command)

        assertEquals(
            command,
            repository.getSavedCommand("command-1"),
        )
    }

    @Test
    fun observeSavedCommands_returnsMappedNewestCommandsFirst() = runBlocking {
        val olderCommand = savedCommand(
            id = "command-1",
            createdAtEpochMillis = 1_000,
        )
        val newerCommand = savedCommand(
            id = "command-2",
            name = "Memory usage",
            command = "free -m",
            createdAtEpochMillis = 2_000,
        )

        repository.saveSavedCommand(olderCommand)
        repository.saveSavedCommand(newerCommand)

        assertEquals(
            listOf(newerCommand, olderCommand),
            repository.observeSavedCommands().first(),
        )
    }

    @Test
    fun saveSavedCommand_whenIdentifierAlreadyExists_doesNotReplaceSilently() = runBlocking {
        val originalCommand = savedCommand(command = "df -h")
        val changedCommand = savedCommand(command = "df -i")

        repository.saveSavedCommand(originalCommand)

        try {
            repository.saveSavedCommand(changedCommand)
            fail("Expected SQLiteConstraintException")
        } catch (_: SQLiteConstraintException) {
            assertEquals(
                originalCommand,
                repository.getSavedCommand("command-1"),
            )
        }
    }

    @Test
    fun deleteSavedCommand_whenCommandExists_removesDomainModel() = runBlocking {
        repository.saveSavedCommand(savedCommand())

        repository.deleteSavedCommand("command-1")

        assertNull(repository.getSavedCommand("command-1"))
    }

    private fun savedCommand(
        id: String = "command-1",
        name: String = "Disk usage",
        command: String = "df -h",
        createdAtEpochMillis: Long = 1_000,
    ): SavedCommand {
        return SavedCommand(
            id = id,
            name = name,
            command = command,
            createdAtEpochMillis = createdAtEpochMillis,
        )
    }
}
