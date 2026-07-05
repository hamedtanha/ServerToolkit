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

class ConfirmSshHostTrustUseCaseTest {

    @Test
    fun `trusts unknown host key after explicit confirmation`() = runBlocking {
        val observedHostKey = observedHostKey()
        val repository = FakeSshHostTrustRepository()
        val useCase = createUseCase(repository)

        val decision = useCase(observedHostKey)

        val expectedTrustedHostKey = SshTrustedHostKey(
            endpoint = observedHostKey.endpoint,
            fingerprint = observedHostKey.fingerprint,
        )
        assertEquals(
            SshHostTrustDecision.Accepted(expectedTrustedHostKey),
            decision,
        )
        assertEquals(expectedTrustedHostKey, repository.trustedHostKey)
        assertEquals(1, repository.saveCallCount)
    }

    @Test
    fun `returns accepted when host key is already trusted`() = runBlocking {
        val trustedHostKey = trustedHostKey()
        val repository = FakeSshHostTrustRepository(
            initialTrustedHostKey = trustedHostKey,
        )
        val useCase = createUseCase(repository)

        val decision = useCase(observedHostKey())

        assertEquals(
            SshHostTrustDecision.Accepted(trustedHostKey),
            decision,
        )
        assertEquals(0, repository.saveCallCount)
    }

    @Test
    fun `blocks changed host key without replacing trusted key`() = runBlocking {
        val trustedHostKey = trustedHostKey(
            fingerprint = fingerprint(value = "trusted-fingerprint"),
        )
        val observedHostKey = observedHostKey(
            fingerprint = fingerprint(value = "observed-fingerprint"),
        )
        val repository = FakeSshHostTrustRepository(
            initialTrustedHostKey = trustedHostKey,
        )
        val useCase = createUseCase(repository)

        val decision = useCase(observedHostKey)

        assertEquals(
            SshHostTrustDecision.BlockedChangedHostKey(
                trustedHostKey = trustedHostKey,
                observedHostKey = observedHostKey,
            ),
            decision,
        )
        assertEquals(trustedHostKey, repository.trustedHostKey)
        assertEquals(0, repository.saveCallCount)
    }

    private fun createUseCase(
        repository: FakeSshHostTrustRepository,
    ): ConfirmSshHostTrustUseCase {
        val decisionUseCase = SshHostTrustDecisionUseCase(
            hostTrustEvaluator = SshHostTrustEvaluator(repository),
        )

        return ConfirmSshHostTrustUseCase(
            hostTrustDecisionUseCase = decisionUseCase,
            hostTrustRepository = repository,
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
