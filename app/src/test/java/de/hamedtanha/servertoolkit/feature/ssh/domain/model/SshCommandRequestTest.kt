package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SshCommandRequestTest {

    @Test
    fun `creates command request with default timeout`() {
        val request = SshCommandRequest(
            sessionHandle = sshSessionHandle(),
            command = "uptime",
        )

        assertEquals(sshSessionHandle(), request.sessionHandle)
        assertEquals("uptime", request.command)
        assertEquals(DEFAULT_SSH_COMMAND_TIMEOUT_MILLIS, request.timeoutMillis)
    }

    @Test
    fun `creates command execution plan with non-sshj metadata`() {
        val plan = SshCommandExecutionPlan(
            sessionHandle = sshSessionHandle(),
            command = "systemctl status ssh",
            timeoutMillis = 10_000,
        )

        assertEquals(sshSessionHandle(), plan.sessionHandle)
        assertEquals("systemctl status ssh", plan.command)
        assertEquals(10_000, plan.timeoutMillis)
    }

    @Test
    fun `rejects blank command request`() {
        expectInvalid("SSH command must not be blank.") {
            SshCommandRequest(
                sessionHandle = sshSessionHandle(),
                command = " ",
            )
        }
    }

    @Test
    fun `rejects invalid command request timeout`() {
        expectInvalid("SSH command timeout must be between 1 and 300000 milliseconds.") {
            SshCommandRequest(
                sessionHandle = sshSessionHandle(),
                command = "uptime",
                timeoutMillis = 0,
            )
        }
    }

    @Test
    fun `rejects blank command execution plan command`() {
        expectInvalid("SSH command plan command must not be blank.") {
            SshCommandExecutionPlan(
                sessionHandle = sshSessionHandle(),
                command = " ",
                timeoutMillis = 10_000,
            )
        }
    }

    @Test
    fun `rejects invalid command execution plan timeout`() {
        expectInvalid("SSH command plan timeout must be between 1 and 300000 milliseconds.") {
            SshCommandExecutionPlan(
                sessionHandle = sshSessionHandle(),
                command = "uptime",
                timeoutMillis = MAX_SSH_COMMAND_TIMEOUT_MILLIS + 1,
            )
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
