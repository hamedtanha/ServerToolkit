package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import kotlinx.coroutines.runBlocking
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

    private fun sshjSessionLifecycleService(
        registry: SshjSessionOwnerRegistry = SshjSessionOwnerRegistry(),
    ): SshjSessionLifecycleService {
        return SshjSessionLifecycleService(
            sessionOwnerRegistry = registry,
        )
    }
}
