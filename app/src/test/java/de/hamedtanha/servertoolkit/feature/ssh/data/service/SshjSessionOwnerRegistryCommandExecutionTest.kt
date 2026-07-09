package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
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

    @Test
    fun `serializes close while command execution is running`() {
        val sessionHandle = sshSessionHandle()
        val registry = SshjSessionOwnerRegistry()
        val commandStarted = CountDownLatch(1)
        val releaseCommand = CountDownLatch(1)
        val closeStarted = CountDownLatch(1)
        val closeFinished = CountDownLatch(1)
        val closeActionCalled = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(2)
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
                    closeActionCalled.countDown()
                },
                commandExecutionAction = {
                    commandStarted.countDown()
                    assertTrue(releaseCommand.await(1, TimeUnit.SECONDS))
                    expectedResult
                },
            ),
        )

        try {
            val executeFuture = executor.submit<SshCommandExecutionResult> {
                registry.execute(request)
            }

            assertTrue(commandStarted.await(1, TimeUnit.SECONDS))

            val closeFuture = executor.submit {
                closeStarted.countDown()
                val result = registry.close(sessionHandle)
                assertEquals(SshSessionCloseResult.Closed, result)
                closeFinished.countDown()
            }

            assertTrue(closeStarted.await(1, TimeUnit.SECONDS))
            assertFalse(closeFinished.await(100, TimeUnit.MILLISECONDS))
            assertEquals(1L, closeActionCalled.count)

            releaseCommand.countDown()

            assertEquals(expectedResult, executeFuture.get(1, TimeUnit.SECONDS))
            assertTrue(closeFinished.await(1, TimeUnit.SECONDS))
            closeFuture.get(1, TimeUnit.SECONDS)

            assertTrue(closeActionCalled.await(1, TimeUnit.SECONDS))
            assertFalse(registry.contains(sessionHandle))
        } finally {
            executor.shutdownNow()
        }
    }
}
