package de.hamedtanha.servertoolkit.feature.ssh.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomSshHostTrustRepositoryTest {

    private lateinit var database: ServerToolkitDatabase
    private lateinit var repository: RoomSshHostTrustRepository

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(
            context,
            ServerToolkitDatabase::class.java,
        ).build()
        repository = RoomSshHostTrustRepository(database.sshTrustedHostKeyDao())
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun saveTrustedHostKey_whenKeyDoesNotExist_persistsDomainModel() = runBlocking {
        val trustedHostKey = trustedHostKey()

        repository.saveTrustedHostKey(trustedHostKey)

        assertEquals(
            trustedHostKey,
            repository.getTrustedHostKey(endpoint()),
        )
    }

    @Test
    fun saveTrustedHostKey_whenKeyAlreadyExists_doesNotReplaceSilently() = runBlocking {
        val originalTrustedHostKey = trustedHostKey(fingerprintValue = "trusted-fingerprint")
        val changedTrustedHostKey = trustedHostKey(fingerprintValue = "changed-fingerprint")

        repository.saveTrustedHostKey(originalTrustedHostKey)

        try {
            repository.saveTrustedHostKey(changedTrustedHostKey)
            fail("Expected SQLiteConstraintException")
        } catch (error: SQLiteConstraintException) {
            assertEquals(
                originalTrustedHostKey,
                repository.getTrustedHostKey(endpoint()),
            )
        }
    }

    @Test
    fun removeTrustedHostKey_whenKeyExists_removesDomainModel() = runBlocking {
        val trustedHostKey = trustedHostKey()

        repository.saveTrustedHostKey(trustedHostKey)
        repository.removeTrustedHostKey(endpoint())

        assertNull(repository.getTrustedHostKey(endpoint()))
    }

    private fun endpoint(): SshHostEndpoint {
        return SshHostEndpoint(
            serverId = "server-1",
            host = "example.com",
            port = 22,
        )
    }

    private fun trustedHostKey(
        fingerprintValue: String = "abc123",
    ): SshTrustedHostKey {
        return SshTrustedHostKey(
            endpoint = endpoint(),
            fingerprint = SshHostKeyFingerprint(
                algorithm = "SHA256",
                value = fingerprintValue,
            ),
        )
    }
}
