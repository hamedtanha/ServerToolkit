package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.test.sshSessionHandle
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SshjSessionLifecycleServiceTest {

    @Test
    fun `returns not found while real session registration is disabled`() = runBlocking {
        val service = SshjSessionLifecycleService()

        val result = service.close(sshSessionHandle())

        assertEquals(SshSessionCloseResult.NotFound, result)
    }
}
