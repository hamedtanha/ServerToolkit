package de.hamedtanha.servertoolkit.feature.serverinventory.data.local.dao

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.entity.ServerEntity
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
    fun upsertServer_whenServerExists_replacesServer() = runBlocking {
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
    fun deleteServer_whenServerExists_removesServer() = runBlocking {
        val server = testServerEntity(id = "server-1", name = "Production Alpha")

        serverDao.upsertServer(server)
        serverDao.deleteServer("server-1")

        assertNull(serverDao.getServerById("server-1"))
        assertTrue(serverDao.observeServers().first().isEmpty())
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
