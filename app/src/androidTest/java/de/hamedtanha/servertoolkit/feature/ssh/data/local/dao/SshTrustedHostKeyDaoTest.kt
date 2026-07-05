package de.hamedtanha.servertoolkit.feature.ssh.data.local.dao

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshTrustedHostKeyEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SshTrustedHostKeyDaoTest {

    private lateinit var database: ServerToolkitDatabase
    private lateinit var trustedHostKeyDao: SshTrustedHostKeyDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            ServerToolkitDatabase::class.java,
        ).build()
        trustedHostKeyDao = database.sshTrustedHostKeyDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun insertTrustedHostKey_whenKeyDoesNotExist_persistsEntity() = runBlocking {
        val entity = trustedHostKeyEntity()

        trustedHostKeyDao.insertTrustedHostKey(entity)

        assertEquals(
            entity,
            trustedHostKeyDao.getTrustedHostKey(
                serverId = "server-1",
                host = "example.com",
                port = 22,
            ),
        )
    }

    @Test
    fun insertTrustedHostKey_whenKeyAlreadyExists_doesNotReplaceSilently() = runBlocking {
        val originalEntity = trustedHostKeyEntity(fingerprintValue = "trusted-fingerprint")
        val changedEntity = trustedHostKeyEntity(fingerprintValue = "changed-fingerprint")

        trustedHostKeyDao.insertTrustedHostKey(originalEntity)

        try {
            trustedHostKeyDao.insertTrustedHostKey(changedEntity)
            fail("Expected SQLiteConstraintException")
        } catch (error: SQLiteConstraintException) {
            assertEquals(
                originalEntity,
                trustedHostKeyDao.getTrustedHostKey(
                    serverId = "server-1",
                    host = "example.com",
                    port = 22,
                ),
            )
        }
    }

    @Test
    fun deleteTrustedHostKey_whenKeyExists_removesEntity() = runBlocking {
        val entity = trustedHostKeyEntity()

        trustedHostKeyDao.insertTrustedHostKey(entity)
        trustedHostKeyDao.deleteTrustedHostKey(
            serverId = "server-1",
            host = "example.com",
            port = 22,
        )

        assertNull(
            trustedHostKeyDao.getTrustedHostKey(
                serverId = "server-1",
                host = "example.com",
                port = 22,
            ),
        )
    }

    private fun trustedHostKeyEntity(
        fingerprintValue: String = "abc123",
    ): SshTrustedHostKeyEntity {
        return SshTrustedHostKeyEntity(
            serverId = "server-1",
            host = "example.com",
            port = 22,
            fingerprintAlgorithm = "SHA256",
            fingerprintValue = fingerprintValue,
        )
    }
}
