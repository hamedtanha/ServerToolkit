package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SshConnectionHistoryEntryTest {

    @Test
    fun `creates connected history entry with non-sensitive metadata`() {
        val entry = sshConnectionHistoryEntry()

        assertEquals("history-1", entry.id)
        assertEquals("server-1", entry.serverId)
        assertEquals("example.com", entry.host)
        assertEquals(22, entry.port)
        assertEquals("admin", entry.username)
        assertEquals(SshConnectionHistoryStatus.Connected, entry.status)
        assertEquals(1_000L, entry.attemptedAtEpochMillis)
        assertEquals(2_000L, entry.completedAtEpochMillis)
        assertEquals(null, entry.connectionError)
    }

    @Test
    fun `creates failed history entry with connection error`() {
        val entry = sshConnectionHistoryEntry(
            status = SshConnectionHistoryStatus.Failed,
            connectionError = SshConnectionError.ConnectionTimeout,
        )

        assertEquals(SshConnectionHistoryStatus.Failed, entry.status)
        assertEquals(SshConnectionError.ConnectionTimeout, entry.connectionError)
    }

    @Test
    fun `creates cancelled history entry without connection error`() {
        val entry = sshConnectionHistoryEntry(
            status = SshConnectionHistoryStatus.Cancelled,
            completedAtEpochMillis = null,
        )

        assertEquals(SshConnectionHistoryStatus.Cancelled, entry.status)
        assertEquals(null, entry.completedAtEpochMillis)
        assertEquals(null, entry.connectionError)
    }

    @Test
    fun `rejects blank history id`() {
        expectInvalid("SSH connection history id must not be blank.") {
            sshConnectionHistoryEntry(id = " ")
        }
    }

    @Test
    fun `rejects blank server id`() {
        expectInvalid("SSH connection history server id must not be blank.") {
            sshConnectionHistoryEntry(serverId = " ")
        }
    }

    @Test
    fun `rejects blank host`() {
        expectInvalid("SSH connection history host must not be blank.") {
            sshConnectionHistoryEntry(host = " ")
        }
    }

    @Test
    fun `rejects invalid port`() {
        expectInvalid("SSH connection history port must be between 1 and 65535.") {
            sshConnectionHistoryEntry(port = 0)
        }
    }

    @Test
    fun `rejects blank username`() {
        expectInvalid("SSH connection history username must not be blank.") {
            sshConnectionHistoryEntry(username = " ")
        }
    }

    @Test
    fun `rejects non-positive attempt time`() {
        expectInvalid("SSH connection history attempt time must be positive.") {
            sshConnectionHistoryEntry(attemptedAtEpochMillis = 0)
        }
    }

    @Test
    fun `rejects completion time before attempt time`() {
        expectInvalid("SSH connection history completion time must not be before the attempt time.") {
            sshConnectionHistoryEntry(
                attemptedAtEpochMillis = 2_000,
                completedAtEpochMillis = 1_000,
            )
        }
    }

    @Test
    fun `rejects failed entry without connection error`() {
        expectInvalid("Failed SSH connection history entries must include a connection error.") {
            sshConnectionHistoryEntry(status = SshConnectionHistoryStatus.Failed)
        }
    }

    @Test
    fun `rejects connected entry with connection error`() {
        expectInvalid("Only failed SSH connection history entries may include a connection error.") {
            sshConnectionHistoryEntry(
                status = SshConnectionHistoryStatus.Connected,
                connectionError = SshConnectionError.Unknown,
            )
        }
    }

    @Test
    fun `rejects cancelled entry with connection error`() {
        expectInvalid("Only failed SSH connection history entries may include a connection error.") {
            sshConnectionHistoryEntry(
                status = SshConnectionHistoryStatus.Cancelled,
                connectionError = SshConnectionError.Unknown,
            )
        }
    }

    private fun sshConnectionHistoryEntry(
        id: String = "history-1",
        serverId: String = "server-1",
        host: String = "example.com",
        port: Int = 22,
        username: String = "admin",
        status: SshConnectionHistoryStatus = SshConnectionHistoryStatus.Connected,
        attemptedAtEpochMillis: Long = 1_000,
        completedAtEpochMillis: Long? = 2_000,
        connectionError: SshConnectionError? = null,
    ): SshConnectionHistoryEntry {
        return SshConnectionHistoryEntry(
            id = id,
            serverId = serverId,
            host = host,
            port = port,
            username = username,
            status = status,
            attemptedAtEpochMillis = attemptedAtEpochMillis,
            completedAtEpochMillis = completedAtEpochMillis,
            connectionError = connectionError,
        )
    }

    private fun expectInvalid(
        expectedMessage: String,
        block: () -> Unit,
    ) {
        try {
            block()
            fail("Expected IllegalArgumentException")
        } catch (error: IllegalArgumentException) {
            assertEquals(expectedMessage, error.message)
        }
    }
}
