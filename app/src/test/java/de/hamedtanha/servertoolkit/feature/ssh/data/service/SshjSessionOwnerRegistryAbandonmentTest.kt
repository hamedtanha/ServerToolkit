package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SshjSessionOwnerRegistryAbandonmentTest {

    @Test
    fun `discard removes ownership before waiting for running command to release owner lock`() {
        val handle = sshSessionHandle()
        val registry = SshjSessionOwnerRegistry()
        val commandStarted = CountDownLatch(1)
        val releaseCommand = CountDownLatch(1)
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
            sessionHandle = handle,
            command = "uptime",
        )

        registry.register(
            SshjSessionOwner(
                sessionHandle = handle,
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
            val commandFuture = executor.submit<SshCommandExecutionResult> {
                registry.execute(request)
            }

            assertTrue(commandStarted.await(1, TimeUnit.SECONDS))

            val discardFuture = executor.submit {
                registry.discard(handle)
            }

            val ownershipRemovedDeadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1)
            while (
                registry.contains(handle) &&
                System.nanoTime() < ownershipRemovedDeadline
            ) {
                Thread.sleep(5)
            }

            assertFalse(registry.contains(handle))
            assertEquals(1L, closeActionCalled.count)

            releaseCommand.countDown()

            assertEquals(expectedResult, commandFuture.get(1, TimeUnit.SECONDS))
            discardFuture.get(1, TimeUnit.SECONDS)
            assertTrue(closeActionCalled.await(1, TimeUnit.SECONDS))
        } finally {
            releaseCommand.countDown()
            executor.shutdownNow()
        }
    }
}
