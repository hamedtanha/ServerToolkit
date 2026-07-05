package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SshHostTrustDecisionUseCaseTest {

    @Test
    fun `returns review required for unknown host key`() = runBlocking {
        val observedHostKey = observedHostKey()
        val useCase = createUseCase(
            repository = FakeSshHostTrustRepository(),
        )

        val decision = useCase(observedHostKey)

        assertEquals(
            SshHostTrustDecision.ReviewRequired(observedHostKey),
            decision,
        )
    }

    @Test
    fun `returns accepted for matching trusted host key`() = runBlocking {
        val trustedHostKey = trustedHostKey()
        val useCase = createUseCase(
            repository = FakeSshHostTrustRepository(
                initialTrustedHostKey = trustedHostKey,
            ),
        )

        val decision = useCase(observedHostKey())

        assertEquals(
            SshHostTrustDecision.Accepted(trustedHostKey),
            decision,
        )
    }

    @Test
    fun `returns blocked changed host key for changed fingerprint`() = runBlocking {
        val trustedHostKey = trustedHostKey(
            fingerprint = fingerprint(value = "trusted-fingerprint"),
        )
        val observedHostKey = observedHostKey(
            fingerprint = fingerprint(value = "observed-fingerprint"),
        )
        val useCase = createUseCase(
            repository = FakeSshHostTrustRepository(
                initialTrustedHostKey = trustedHostKey,
            ),
        )

        val decision = useCase(observedHostKey)

        assertEquals(
            SshHostTrustDecision.BlockedChangedHostKey(
                trustedHostKey = trustedHostKey,
                observedHostKey = observedHostKey,
            ),
            decision,
        )
    }

    private fun createUseCase(
        repository: FakeSshHostTrustRepository,
    ): SshHostTrustDecisionUseCase {
        return SshHostTrustDecisionUseCase(
            hostTrustEvaluator = SshHostTrustEvaluator(repository),
        )
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
