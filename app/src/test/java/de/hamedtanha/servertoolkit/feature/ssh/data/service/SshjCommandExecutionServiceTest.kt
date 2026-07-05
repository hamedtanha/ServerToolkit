package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SshjCommandExecutionServiceTest {

    @Test
    fun `executes command through registered session owner`() = runBlocking {
        val sessionHandle = sshSessionHandle()
        val registry = SshjSessionOwnerRegistry()
        val request = SshCommandRequest(
            sessionHandle = sessionHandle,
            command = "uptime",
        )
        val expectedResult = SshCommandExecutionResult.Completed(
            SshCommandExecutionOutput(
                stdout = "ok",
                stderr = "",
                exitStatus = 0,
            ),
        )
        registry.register(
            SshjSessionOwner(
                sessionHandle = sessionHandle,
                closeAction = {},
                commandExecutionAction = {
                    expectedResult
                },
            ),
        )
        val service = SshjCommandExecutionService(registry)

        val result = service.execute(request)

        assertEquals(expectedResult, result)
    }

    @Test
    fun `fails command execution when session owner does not exist`() = runBlocking {
        val service = SshjCommandExecutionService(SshjSessionOwnerRegistry())

        val result = service.execute(
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
