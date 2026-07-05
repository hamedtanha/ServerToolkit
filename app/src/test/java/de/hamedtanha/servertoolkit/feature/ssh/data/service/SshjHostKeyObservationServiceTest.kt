package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SshjHostKeyObservationServiceTest {

    @Test
    fun `returns unavailable while real SSH host-key observation is disabled`() = runBlocking {
        val service = SshjHostKeyObservationService()

        val result = service.observeHostKey(
            SshConnectionRequest(
                serverId = "server-1",
                host = "example.com",
                port = 22,
                username = "admin",
            ),
        )

        assertEquals(SshHostKeyObservationResult.Unavailable, result)
    }
}
