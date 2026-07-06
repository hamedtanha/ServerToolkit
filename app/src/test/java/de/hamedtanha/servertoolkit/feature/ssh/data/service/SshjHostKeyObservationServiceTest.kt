package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import java.security.KeyPairGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SshjHostKeyObservationServiceTest {

    @Test
    fun `maps observed sshj host key into project-owned observed host key`() = runBlocking {
        val publicKey = generatePublicKey()
        val service = SshjHostKeyObservationService(
            hostKeyObserver = SshjHostKeyObserver {
                publicKey
            },
        )

        val result = service.observeHostKey(connectionRequest())

        val observed = result as SshHostKeyObservationResult.Observed
        assertEquals("server-1", observed.observedHostKey.endpoint.serverId)
        assertEquals("example.com", observed.observedHostKey.endpoint.host)
        assertEquals(22, observed.observedHostKey.endpoint.port)
        assertEquals("SHA256", observed.observedHostKey.fingerprint.algorithm)
        assertTrue(observed.observedHostKey.fingerprint.value.isNotBlank())
    }

    @Test
    fun `returns unavailable when sshj host key observer cannot observe a key`() = runBlocking {
        val service = SshjHostKeyObservationService(
            hostKeyObserver = SshjHostKeyObserver {
                null
            },
        )

        val result = service.observeHostKey(connectionRequest())

        assertEquals(SshHostKeyObservationResult.Unavailable, result)
    }

    @Test
    fun `returns unavailable when sshj host key observer fails`() = runBlocking {
        val service = SshjHostKeyObservationService(
            hostKeyObserver = SshjHostKeyObserver {
                throw IllegalStateException("Observation failed")
            },
        )

        val result = service.observeHostKey(connectionRequest())

        assertEquals(SshHostKeyObservationResult.Unavailable, result)
    }

    private fun connectionRequest(): SshConnectionRequest {
        return SshConnectionRequest(
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
        )
    }

    private fun generatePublicKey() = KeyPairGenerator
        .getInstance("RSA")
        .generateKeyPair()
        .public
}
