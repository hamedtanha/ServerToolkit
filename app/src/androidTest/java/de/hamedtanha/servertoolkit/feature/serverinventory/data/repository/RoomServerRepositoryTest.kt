package de.hamedtanha.servertoolkit.feature.serverinventory.data.repository

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.ServerEnvironment
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
    fun saveServer_whenServerExists_replacesDomainModel() = runBlocking {
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
    fun deleteServer_whenServerExists_removesDomainModel() = runBlocking {
        val server = testServer(id = "server-1", name = "Production Alpha")

        repository.saveServer(server)
        repository.deleteServer("server-1")

        assertNull(repository.getServerById("server-1"))
        assertTrue(repository.observeServers().first().isEmpty())
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
