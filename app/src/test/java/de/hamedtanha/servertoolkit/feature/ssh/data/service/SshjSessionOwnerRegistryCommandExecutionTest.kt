package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
}
