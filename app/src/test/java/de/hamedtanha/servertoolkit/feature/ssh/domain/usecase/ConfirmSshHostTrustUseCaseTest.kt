package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprintEncoding
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class ConfirmSshHostTrustUseCaseTest {

    @Test
    fun `trusts unknown host key using canonical fingerprint after explicit confirmation`() = runBlocking {
        val observedHostKey = observedHostKey(
            fingerprint = canonicalFingerprint(),
            legacyFingerprints = setOf(
                legacyJavaSha256Fingerprint(),
                legacySshjMd5Fingerprint(),
            ),
        )
        val repository = FakeSshHostTrustRepository()
        val useCase = createUseCase(repository)

        val decision = useCase(observedHostKey)

        val expectedTrustedHostKey = SshTrustedHostKey(
            endpoint = observedHostKey.endpoint,
            fingerprint = canonicalFingerprint(),
        )
        assertEquals(
            SshHostTrustDecision.Accepted(expectedTrustedHostKey),
            decision,
        )
        assertEquals(expectedTrustedHostKey, repository.trustedHostKey)
        assertEquals(1, repository.saveCallCount)
    }

    @Test
    fun `returns accepted without rewrite when canonical host key is already trusted`() = runBlocking {
        val trustedHostKey = trustedHostKey(
            fingerprint = canonicalFingerprint(),
        )
        val repository = FakeSshHostTrustRepository(
            initialTrustedHostKey = trustedHostKey,
        )
        val useCase = createUseCase(repository)

        val decision = useCase(observedHostKey())

        assertEquals(
            SshHostTrustDecision.Accepted(trustedHostKey),
            decision,
        )
        assertEquals(trustedHostKey, repository.trustedHostKey)
        assertEquals(0, repository.saveCallCount)
    }

    @Test
    fun `returns accepted without rewrite when historical Java sha256 host key matches`() = runBlocking {
        val legacyFingerprint = legacyJavaSha256Fingerprint()
        val trustedHostKey = trustedHostKey(
            fingerprint = legacyFingerprint,
        )
        val repository = FakeSshHostTrustRepository(
            initialTrustedHostKey = trustedHostKey,
        )
        val useCase = createUseCase(repository)

        val decision = useCase(
            observedHostKey(
                fingerprint = canonicalFingerprint(),
                legacyFingerprints = setOf(legacyFingerprint),
            ),
        )

        assertEquals(
            SshHostTrustDecision.Accepted(trustedHostKey),
            decision,
        )
        assertEquals(trustedHostKey, repository.trustedHostKey)
        assertEquals(0, repository.saveCallCount)
    }

    @Test
    fun `returns accepted without rewrite when historical SSHJ md5 host key matches`() = runBlocking {
        val legacyFingerprint = legacySshjMd5Fingerprint()
        val trustedHostKey = trustedHostKey(
            fingerprint = legacyFingerprint,
        )
        val repository = FakeSshHostTrustRepository(
            initialTrustedHostKey = trustedHostKey,
        )
        val useCase = createUseCase(repository)

        val decision = useCase(
            observedHostKey(
                fingerprint = canonicalFingerprint(),
                legacyFingerprints = setOf(legacyFingerprint),
            ),
        )

        assertEquals(
            SshHostTrustDecision.Accepted(trustedHostKey),
            decision,
        )
        assertEquals(trustedHostKey, repository.trustedHostKey)
        assertEquals(0, repository.saveCallCount)
    }

    @Test
    fun `blocks changed host key without replacing historical trusted key`() = runBlocking {
        val trustedHostKey = trustedHostKey(
            fingerprint = legacyJavaSha256Fingerprint(value = "trusted-fingerprint"),
        )
        val observedHostKey = observedHostKey(
            fingerprint = canonicalFingerprint(value = "observed-canonical-fingerprint"),
            legacyFingerprints = setOf(
                legacyJavaSha256Fingerprint(value = "observed-legacy-fingerprint"),
                legacySshjMd5Fingerprint(),
            ),
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

    private fun canonicalFingerprint(
        value: String = "canonical-fingerprint",
    ): SshHostKeyFingerprint {
        return SshHostKeyFingerprint(
            algorithm = "SHA256",
            value = value,
            encoding = SshHostKeyFingerprintEncoding.OpenSshWire,
        )
    }

    private fun legacyJavaSha256Fingerprint(
        value: String = "legacy-java-sha256",
    ): SshHostKeyFingerprint {
        return SshHostKeyFingerprint(
            algorithm = "SHA256",
            value = value,
            encoding = SshHostKeyFingerprintEncoding.LegacyJavaPublicKey,
        )
    }

    private fun legacySshjMd5Fingerprint(
        value: String = "aa:bb:cc:dd",
    ): SshHostKeyFingerprint {
        return SshHostKeyFingerprint(
            algorithm = "MD5",
            value = value,
            encoding = SshHostKeyFingerprintEncoding.OpenSshWire,
        )
    }

    private fun observedHostKey(
        fingerprint: SshHostKeyFingerprint = canonicalFingerprint(),
        legacyFingerprints: Set<SshHostKeyFingerprint> = emptySet(),
    ): SshObservedHostKey {
        return SshObservedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint,
            legacyFingerprints = legacyFingerprints,
        )
    }

    private fun trustedHostKey(
        fingerprint: SshHostKeyFingerprint = canonicalFingerprint(),
    ): SshTrustedHostKey {
        return SshTrustedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint,
        )
    }
}
