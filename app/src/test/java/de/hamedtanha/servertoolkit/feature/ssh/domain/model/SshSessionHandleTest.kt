package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SshSessionHandleTest {

    @Test
    fun `creates project-owned session handle with non-sensitive metadata`() {
        val handle = sshSessionHandle()

        assertEquals("session-1", handle.sessionId)
        assertEquals("server-1", handle.serverId)
        assertEquals("example.com", handle.host)
        assertEquals(22, handle.port)
        assertEquals("admin", handle.username)
    }

    @Test
    fun `rejects blank session id`() {
        expectInvalid("SSH session id must not be blank.") {
            sshSessionHandle(sessionId = " ")
        }
    }

    @Test
    fun `rejects blank server id`() {
        expectInvalid("SSH session server id must not be blank.") {
            sshSessionHandle(serverId = " ")
        }
    }

    @Test
    fun `rejects blank host`() {
        expectInvalid("SSH session host must not be blank.") {
            sshSessionHandle(host = " ")
        }
    }

    @Test
    fun `rejects invalid port`() {
        expectInvalid("SSH session port must be between 1 and 65535.") {
            sshSessionHandle(port = 0)
        }
    }

    @Test
    fun `rejects blank username`() {
        expectInvalid("SSH session username must not be blank.") {
            sshSessionHandle(username = " ")
        }
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
