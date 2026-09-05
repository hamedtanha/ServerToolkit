package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
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

    @Test
    fun `preserves cancellation from registered session owner`() = runBlocking {
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
        val service = SshjCommandExecutionService(registry)

        try {
            service.execute(request)
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("cancelled", error.message)
        }
    }

    @Test
    fun `coroutine cancellation interrupts blocking registered command execution`() = runBlocking {
        val sessionHandle = sshSessionHandle()
        val registry = SshjSessionOwnerRegistry()
        val commandStarted = CountDownLatch(1)
        val releaseCommand = CountDownLatch(1)
        val commandInterrupted = AtomicBoolean(false)
        val request = SshCommandRequest(
            sessionHandle = sessionHandle,
            command = "tail -f /var/log/example.log",
            timeoutMillis = 5_000,
        )
        registry.register(
            SshjSessionOwner(
                sessionHandle = sessionHandle,
                closeAction = {},
                commandExecutionAction = {
                    commandStarted.countDown()
                    try {
                        releaseCommand.await()
                        SshCommandExecutionResult.Completed(
                            SshCommandExecutionOutput(
                                stdout = "",
                                stderr = "",
                                exitStatus = 0,
                            ),
                        )
                    } catch (error: InterruptedException) {
                        commandInterrupted.set(true)
                        Thread.currentThread().interrupt()
                        throw error
                    }
                },
            ),
        )
        val service = SshjCommandExecutionService(registry)
        var observedCancellation: CancellationException? = null

        try {
            val job = launch {
                try {
                    service.execute(request)
                    fail("Expected CancellationException")
                } catch (error: CancellationException) {
                    observedCancellation = error
                }
            }

            assertTrue(commandStarted.await(1, TimeUnit.SECONDS))
            job.cancel(CancellationException("cancel blocking command"))
            withTimeout(2_000) {
                job.join()
            }
        } finally {
            releaseCommand.countDown()
        }

        assertTrue(commandInterrupted.get())
        assertTrue(observedCancellation is CancellationException)
    }
}
