package de.hamedtanha.servertoolkit.feature.savedcommands.data.local.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.savedcommands.data.local.entity.SavedCommandEntity
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
class SavedCommandDaoTest {

    private lateinit var database: ServerToolkitDatabase
    private lateinit var savedCommandDao: SavedCommandDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            ServerToolkitDatabase::class.java,
        ).build()
        savedCommandDao = database.savedCommandDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertSavedCommand_whenCommandDoesNotExist_persistsExactEntity() = runBlocking {
        val entity = savedCommandEntity(command = "  df -h\n")

        savedCommandDao.insertSavedCommand(entity)

        assertEquals(
            entity,
            savedCommandDao.getSavedCommand("command-1"),
        )
    }

    @Test
    fun observeSavedCommands_returnsNewestCommandsFirst() = runBlocking {
        val olderCommand = savedCommandEntity(
            id = "command-1",
            createdAtEpochMillis = 1_000,
        )
        val newerCommand = savedCommandEntity(
            id = "command-2",
            name = "Memory usage",
            command = "free -m",
            createdAtEpochMillis = 2_000,
        )

        savedCommandDao.insertSavedCommand(olderCommand)
        savedCommandDao.insertSavedCommand(newerCommand)

        assertEquals(
            listOf(newerCommand, olderCommand),
            savedCommandDao.observeSavedCommands().first(),
        )
    }

    @Test
    fun observeSavedCommands_whenCreationTimesMatch_usesIdAsStableTieBreaker() = runBlocking {
        val secondCommand = savedCommandEntity(
            id = "command-2",
            name = "Memory usage",
            command = "free -m",
        )
        val firstCommand = savedCommandEntity(id = "command-1")

        savedCommandDao.insertSavedCommand(secondCommand)
        savedCommandDao.insertSavedCommand(firstCommand)

        assertEquals(
            listOf(firstCommand, secondCommand),
            savedCommandDao.observeSavedCommands().first(),
        )
    }

    @Test
    fun insertSavedCommand_whenIdentifierAlreadyExists_doesNotReplaceSilently() = runBlocking {
        val originalEntity = savedCommandEntity(command = "df -h")
        val changedEntity = savedCommandEntity(command = "df -i")

        savedCommandDao.insertSavedCommand(originalEntity)

        try {
            savedCommandDao.insertSavedCommand(changedEntity)
            fail("Expected SQLiteConstraintException")
        } catch (_: SQLiteConstraintException) {
            assertEquals(
                originalEntity,
                savedCommandDao.getSavedCommand("command-1"),
            )
        }
    }

    @Test
    fun deleteSavedCommand_whenCommandExists_removesEntity() = runBlocking {
        savedCommandDao.insertSavedCommand(savedCommandEntity())

        savedCommandDao.deleteSavedCommand("command-1")

        assertNull(savedCommandDao.getSavedCommand("command-1"))
    }

    private fun savedCommandEntity(
        id: String = "command-1",
        name: String = "Disk usage",
        command: String = "df -h",
        createdAtEpochMillis: Long = 1_000,
    ): SavedCommandEntity {
        return SavedCommandEntity(
            id = id,
            name = name,
            command = command,
            createdAtEpochMillis = createdAtEpochMillis,
        )
    }
}
