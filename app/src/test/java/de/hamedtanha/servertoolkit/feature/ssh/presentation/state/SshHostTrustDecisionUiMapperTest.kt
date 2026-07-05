package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SshHostTrustDecisionUiMapperTest {

    @Test
    fun `maps review required decision into host key review state`() {
        val uiState = SshUiState(serverId = "server-1")
            .withHostTrustDecision(
                SshHostTrustDecision.ReviewRequired(observedHostKey()),
            )

        assertEquals(SshConnectionStatus.Failed, uiState.status)
        assertEquals("Server identity review required", uiState.statusLabel)
        assertEquals("Review the server identity before connecting.", uiState.message)
        assertEquals("server-1", uiState.hostKeyReview?.serverId)
        assertEquals("example.com", uiState.hostKeyReview?.host)
        assertEquals(22, uiState.hostKeyReview?.port)
        assertEquals("SHA256:abc123", uiState.hostKeyReview?.displayFingerprint)
    }

    @Test
    fun `maps accepted decision into trusted state without review data`() {
        val uiState = SshUiState(serverId = "server-1")
            .withHostTrustDecision(
                SshHostTrustDecision.Accepted(trustedHostKey()),
            )

        assertEquals(SshConnectionStatus.NotStarted, uiState.status)
        assertEquals("Server identity trusted", uiState.statusLabel)
        assertEquals("Server identity has been trusted.", uiState.message)
        assertNull(uiState.hostKeyReview)
    }

    @Test
    fun `maps changed host key decision into blocked review state`() {
        val uiState = SshUiState(serverId = "server-1")
            .withHostTrustDecision(
                SshHostTrustDecision.BlockedChangedHostKey(
                    trustedHostKey = trustedHostKey(
                        fingerprint = fingerprint(value = "trusted-fingerprint"),
                    ),
                    observedHostKey = observedHostKey(
                        fingerprint = fingerprint(value = "observed-fingerprint"),
                    ),
                ),
            )

        assertEquals(SshConnectionStatus.Failed, uiState.status)
        assertEquals("Server identity changed", uiState.statusLabel)
        assertEquals("The server identity changed and the connection is blocked.", uiState.message)
        assertEquals("SHA256:observed-fingerprint", uiState.hostKeyReview?.displayFingerprint)
    }

    private fun endpoint(): SshHostEndpoint {
        return SshHostEndpoint(
            serverId = "server-1",
            host = "example.com",
            port = 22,
        )
    }

    private fun fingerprint(
        value: String = "abc123",
    ): SshHostKeyFingerprint {
        return SshHostKeyFingerprint(
            algorithm = "SHA256",
            value = value,
        )
    }

    private fun observedHostKey(
        fingerprint: SshHostKeyFingerprint = fingerprint(),
    ): SshObservedHostKey {
        return SshObservedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint,
        )
    }

    private fun trustedHostKey(
        fingerprint: SshHostKeyFingerprint = fingerprint(),
    ): SshTrustedHostKey {
        return SshTrustedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint,
        )
    }
}
