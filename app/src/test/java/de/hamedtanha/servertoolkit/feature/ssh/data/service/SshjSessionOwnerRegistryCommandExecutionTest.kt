package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.fail
import org.junit.Test

class SshjSessionOwnerRegistryCommandExecutionTest {

    @Test
    fun `executes command through registered session owner`() {
        val sessionHandle = sshSessionHandle()
        val registry = SshjSessionOwnerRegistry()
        var receivedRequest: SshCommandRequest? = null
        var ownerClosed = false
        val expectedResult = SshCommandExecutionResult.Completed(
            SshCommandExecutionOutput(
                stdout = "ok",
                stderr = "",
                exitStatus = 0,
            ),
        )
        val request = SshCommandRequest(
            sessionHandle = sessionHandle,
            command = "uptime",
        )

        registry.register(
            SshjSessionOwner(
                sessionHandle = sessionHandle,
                closeAction = {
                    ownerClosed = true
                },
                commandExecutionAction = {
                    receivedRequest = it
                    expectedResult
                },
            ),
        )

        val result = registry.execute(request)

        assertEquals(expectedResult, result)
        assertEquals(request, receivedRequest)
        assertFalse(ownerClosed)
    }

    @Test
    fun `fails command execution when session owner does not exist`() {
        val registry = SshjSessionOwnerRegistry()

        val result = registry.execute(
            SshCommandRequest(
                sessionHandle = sshSessionHandle(),
                command = "uptime",
            ),
        )

        assertEquals(
            SshCommandExecutionResult.Failed(SshCommandExecutionError.SessionNotFound),
            result,
        )
    }

    @Test
    fun `preserves cancellation from registered session owner command execution`() {
        val sessionHandle = sshSessionHandle()
        val registry = SshjSessionOwnerRegistry()
        val request = SshCommandRequest(
            sessionHandle = sessionHandle,
            command = "uptime",
        )
        registry.register(
            SshjSessionOwner(
                sessionHandle = sessionHandle,
                closeAction = {},
                commandExecutionAction = {
                    throw CancellationException("cancelled")
                },
            ),
        )

        try {
            registry.execute(request)
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("cancelled", error.message)
        }
    }
}
