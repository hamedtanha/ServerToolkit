package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SshjSessionLifecycleServiceTest {

    @Test
    fun `returns not found when no session owner is registered`() = runBlocking {
        val service = sshjSessionLifecycleService()

        val result = service.close(sshSessionHandle())

        assertEquals(SshSessionCloseResult.NotFound, result)
    }

    @Test
    fun `closes registered session through owner registry`() = runBlocking {
        val registry = SshjSessionOwnerRegistry()
        val service = sshjSessionLifecycleService(registry)
        val handle = sshSessionHandle()
        var closed = false

        registry.register(
            SshjSessionOwner(
                sessionHandle = handle,
                closeAction = {
                    closed = true
                },
            ),
        )

        val result = service.close(handle)

        assertEquals(SshSessionCloseResult.Closed, result)
        assertTrue(closed)
    }

    @Test
    fun `completes session cleanup when caller is cancelled during close`() = runBlocking {
        val registry = SshjSessionOwnerRegistry()
        val service = sshjSessionLifecycleService(registry)
        val handle = sshSessionHandle()
        val closeStarted = CompletableDeferred<Unit>()
        val releaseClose = CountDownLatch(1)
        var closeCompleted = false
        var closeResult: SshSessionCloseResult? = null

        registry.register(
            SshjSessionOwner(
                sessionHandle = handle,
                closeAction = {
                    closeStarted.complete(Unit)
                    releaseClose.await()
                    closeCompleted = true
                },
            ),
        )

        val closeJob = launch {
            closeResult = service.close(handle)
        }

        withTimeout(1_000) {
            closeStarted.await()
        }

        closeJob.cancel()
        releaseClose.countDown()
        closeJob.join()

        assertTrue(closeCompleted)
        assertEquals(SshSessionCloseResult.Closed, closeResult)
        assertEquals(SshSessionCloseResult.NotFound, registry.close(handle))
    }

    private fun sshjSessionLifecycleService(
        registry: SshjSessionOwnerRegistry = SshjSessionOwnerRegistry(),
    ): SshjSessionLifecycleService {
        return SshjSessionLifecycleService(
            sessionOwnerRegistry = registry,
        )
    }
}
