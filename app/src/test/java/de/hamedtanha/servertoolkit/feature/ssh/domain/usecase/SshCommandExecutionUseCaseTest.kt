package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.DEFAULT_SSH_COMMAND_TIMEOUT_MILLIS
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshCommandExecutionService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SshCommandExecutionUseCaseTest {

    @Test
    fun `executes command through command execution service`() = runBlocking {
        val sessionHandle = sshSessionHandle()
        val expectedResult = SshCommandExecutionResult.Completed(
            SshCommandExecutionOutput(
                stdout = "ok",
                stderr = "",
                exitStatus = 0,
            ),
        )
        val service = FakeSshCommandExecutionService(
            result = expectedResult,
        )
        val useCase = SshCommandExecutionUseCase(service)

        val result = useCase(
            sessionHandle = sessionHandle,
            command = "uptime",
        )

        assertEquals(expectedResult, result)
        assertEquals(sessionHandle, service.receivedRequest?.sessionHandle)
        assertEquals("uptime", service.receivedRequest?.command)
        assertEquals(DEFAULT_SSH_COMMAND_TIMEOUT_MILLIS, service.receivedRequest?.timeoutMillis)
    }

    @Test
    fun `passes custom timeout to command request`() = runBlocking {
        val service = FakeSshCommandExecutionService()
        val useCase = SshCommandExecutionUseCase(service)

        useCase(
            sessionHandle = sshSessionHandle(),
            command = "whoami",
            timeoutMillis = 5_000,
        )

        assertEquals(5_000L, service.receivedRequest?.timeoutMillis)
    }

    @Test
    fun `maps complete operation timeout to command timed out`() = runBlocking {
        val service = FakeSshCommandExecutionService(
            delayMillis = 5_000,
        )
        val useCase = SshCommandExecutionUseCase(service)

        val result = useCase(
            sessionHandle = sshSessionHandle(),
            command = "sleep 10",
            timeoutMillis = 50,
        )

        assertEquals(
            SshCommandExecutionResult.Failed(SshCommandExecutionError.CommandTimedOut),
            result,
        )
        assertEquals(50L, service.receivedRequest?.timeoutMillis)
    }

    @Test
    fun `maps invalid command request to unsupported configuration`() = runBlocking {
        val service = FakeSshCommandExecutionService()
        val useCase = SshCommandExecutionUseCase(service)

        val result = useCase(
            sessionHandle = sshSessionHandle(),
            command = " ",
        )

        assertEquals(
            SshCommandExecutionResult.Failed(SshCommandExecutionError.UnsupportedConfiguration),
            result,
        )
        assertNull(service.receivedRequest)
    }

    @Test
    fun `maps unexpected service failure to unknown command execution failure`() = runBlocking {
        val service = FakeSshCommandExecutionService(
            failure = IllegalStateException("Unexpected command execution failure."),
        )
        val useCase = SshCommandExecutionUseCase(service)

        val result = useCase(
            sessionHandle = sshSessionHandle(),
            command = "uptime",
        )

        assertEquals(
            SshCommandExecutionResult.Failed(SshCommandExecutionError.Unknown),
            result,
        )
    }

    @Test
    fun `preserves coroutine cancellation`() = runBlocking {
        val service = FakeSshCommandExecutionService(
            failure = CancellationException("Command execution was cancelled."),
        )
        val useCase = SshCommandExecutionUseCase(service)

        val result = runCatching {
            useCase(
                sessionHandle = sshSessionHandle(),
                command = "uptime",
            )
        }

        assertEquals(true, result.exceptionOrNull() is CancellationException)
    }

    private class FakeSshCommandExecutionService(
        private val result: SshCommandExecutionResult = SshCommandExecutionResult.Completed(
            SshCommandExecutionOutput(
                stdout = "",
                stderr = "",
                exitStatus = 0,
            ),
        ),
        private val failure: Exception? = null,
        private val delayMillis: Long = 0,
    ) : SshCommandExecutionService {

        var receivedRequest: SshCommandRequest? = null
            private set

        override suspend fun execute(request: SshCommandRequest): SshCommandExecutionResult {
            receivedRequest = request
            failure?.let { throw it }
            if (delayMillis > 0) {
                delay(delayMillis)
            }
            return result
        }
    }

    private fun sshSessionHandle(): SshSessionHandle {
        return SshSessionHandle(
            sessionId = "session-1",
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
        )
    }
}
