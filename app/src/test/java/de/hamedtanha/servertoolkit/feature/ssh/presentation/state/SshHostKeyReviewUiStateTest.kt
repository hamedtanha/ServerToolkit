package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import org.junit.Assert.assertEquals
import org.junit.Test

class SshHostKeyReviewUiStateTest {

    @Test
    fun `maps observed host key into review ui state`() {
        val review = observedHostKey().toReviewUiState()

        assertEquals("server-1", review.serverId)
        assertEquals("example.com", review.host)
        assertEquals(22, review.port)
        assertEquals("SHA256", review.fingerprintAlgorithm)
        assertEquals("abc123", review.fingerprintValue)
        assertEquals("SHA256:abc123", review.displayFingerprint)
    }

    private fun observedHostKey(): SshObservedHostKey {
        return SshObservedHostKey(
            endpoint = SshHostEndpoint(
                serverId = "server-1",
                host = "example.com",
                port = 22,
            ),
            fingerprint = SshHostKeyFingerprint(
                algorithm = "SHA256",
                value = "abc123",
            ),
        )
    }
}
