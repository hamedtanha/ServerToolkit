package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprintEncoding
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustStatus
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshHostTrustRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SshHostTrustEvaluatorTest {

    @Test
    fun `returns unknown when no trusted host key exists`() = runBlocking {
        val observedHostKey = observedHostKey()
        val evaluator = SshHostTrustEvaluator(
            hostTrustRepository = FakeSshHostTrustRepository(),
        )

        val status = evaluator.evaluate(observedHostKey)

        assertEquals(
            SshHostTrustStatus.Unknown(observedHostKey),
            status,
        )
    }

    @Test
    fun `returns trusted when canonical fingerprint matches`() = runBlocking {
        val trustedHostKey = trustedHostKey(
            fingerprint = canonicalFingerprint(),
        )
        val observedHostKey = observedHostKey(
            fingerprint = canonicalFingerprint(),
        )
        val evaluator = SshHostTrustEvaluator(
            hostTrustRepository = FakeSshHostTrustRepository(
                trustedHostKey = trustedHostKey,
            ),
        )

        val status = evaluator.evaluate(observedHostKey)

        assertEquals(
            SshHostTrustStatus.Trusted(trustedHostKey),
            status,
        )
    }

    @Test
    fun `returns trusted when historical Java sha256 fingerprint matches legacy candidate`() = runBlocking {
        val legacyFingerprint = legacyJavaSha256Fingerprint()
        val trustedHostKey = trustedHostKey(
            fingerprint = legacyFingerprint,
        )
        val evaluator = SshHostTrustEvaluator(
            hostTrustRepository = FakeSshHostTrustRepository(
                trustedHostKey = trustedHostKey,
            ),
        )

        val status = evaluator.evaluate(
            observedHostKey(
                fingerprint = canonicalFingerprint(),
                legacyFingerprints = setOf(legacyFingerprint),
            ),
        )

        assertEquals(
            SshHostTrustStatus.Trusted(trustedHostKey),
            status,
        )
    }

    @Test
    fun `returns trusted when historical SSHJ md5 fingerprint matches legacy candidate`() = runBlocking {
        val legacyFingerprint = legacySshjMd5Fingerprint()
        val trustedHostKey = trustedHostKey(
            fingerprint = legacyFingerprint,
        )
        val evaluator = SshHostTrustEvaluator(
            hostTrustRepository = FakeSshHostTrustRepository(
                trustedHostKey = trustedHostKey,
            ),
        )

        val status = evaluator.evaluate(
            observedHostKey(
                fingerprint = canonicalFingerprint(),
                legacyFingerprints = setOf(legacyFingerprint),
            ),
        )

        assertEquals(
            SshHostTrustStatus.Trusted(trustedHostKey),
            status,
        )
    }

    @Test
    fun `returns changed when canonical fingerprint differs for the same endpoint`() = runBlocking {
        val trustedHostKey = trustedHostKey(
            fingerprint = canonicalFingerprint(value = "trusted-fingerprint"),
        )
        val observedHostKey = observedHostKey(
            fingerprint = canonicalFingerprint(value = "observed-fingerprint"),
        )
        val evaluator = SshHostTrustEvaluator(
            hostTrustRepository = FakeSshHostTrustRepository(
                trustedHostKey = trustedHostKey,
            ),
        )

        val status = evaluator.evaluate(observedHostKey)

        assertEquals(
            SshHostTrustStatus.Changed(
                trustedHostKey = trustedHostKey,
                observedHostKey = observedHostKey,
            ),
            status,
        )
    }

    @Test
    fun `returns changed when historical fingerprint does not match any legacy candidate`() = runBlocking {
        val trustedHostKey = trustedHostKey(
            fingerprint = legacyJavaSha256Fingerprint(value = "trusted-legacy-fingerprint"),
        )
        val observedHostKey = observedHostKey(
            fingerprint = canonicalFingerprint(),
            legacyFingerprints = setOf(
                legacyJavaSha256Fingerprint(value = "observed-legacy-fingerprint"),
                legacySshjMd5Fingerprint(),
            ),
        )
        val evaluator = SshHostTrustEvaluator(
            hostTrustRepository = FakeSshHostTrustRepository(
                trustedHostKey = trustedHostKey,
            ),
        )

        val status = evaluator.evaluate(observedHostKey)

        assertEquals(
            SshHostTrustStatus.Changed(
                trustedHostKey = trustedHostKey,
                observedHostKey = observedHostKey,
            ),
            status,
        )
    }

    @Test
    fun `does not equate canonical and historical Java sha256 fingerprints with identical values`() = runBlocking {
        val trustedHostKey = trustedHostKey(
            fingerprint = legacyJavaSha256Fingerprint(value = "same-value"),
        )
        val observedHostKey = observedHostKey(
            fingerprint = canonicalFingerprint(value = "same-value"),
        )
        val evaluator = SshHostTrustEvaluator(
            hostTrustRepository = FakeSshHostTrustRepository(
                trustedHostKey = trustedHostKey,
            ),
        )

        val status = evaluator.evaluate(observedHostKey)

        assertEquals(
            SshHostTrustStatus.Changed(
                trustedHostKey = trustedHostKey,
                observedHostKey = observedHostKey,
            ),
            status,
        )
    }

    @Test
    fun `returns unknown when endpoint differs from trusted host key`() = runBlocking {
        val evaluator = SshHostTrustEvaluator(
            hostTrustRepository = FakeSshHostTrustRepository(
                trustedHostKey = trustedHostKey(
                    endpoint = endpoint(serverId = "server-2"),
                ),
            ),
        )
        val observedHostKey = observedHostKey(
            endpoint = endpoint(serverId = "server-1"),
        )

        val status = evaluator.evaluate(observedHostKey)

        assertEquals(
            SshHostTrustStatus.Unknown(observedHostKey),
            status,
        )
    }

    private fun endpoint(
        serverId: String = "server-1",
        host: String = "example.com",
        port: Int = 22,
    ): SshHostEndpoint {
        return SshHostEndpoint(
            serverId = serverId,
            host = host,
            port = port,
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
        endpoint: SshHostEndpoint = endpoint(),
        fingerprint: SshHostKeyFingerprint = canonicalFingerprint(),
        legacyFingerprints: Set<SshHostKeyFingerprint> = emptySet(),
    ): SshObservedHostKey {
        return SshObservedHostKey(
            endpoint = endpoint,
            fingerprint = fingerprint,
            legacyFingerprints = legacyFingerprints,
        )
    }

    private fun trustedHostKey(
        endpoint: SshHostEndpoint = endpoint(),
        fingerprint: SshHostKeyFingerprint = canonicalFingerprint(),
    ): SshTrustedHostKey {
        return SshTrustedHostKey(
            endpoint = endpoint,
            fingerprint = fingerprint,
        )
    }

    private class FakeSshHostTrustRepository(
        private val trustedHostKey: SshTrustedHostKey? = null,
    ) : SshHostTrustRepository {

        override suspend fun getTrustedHostKey(
            endpoint: SshHostEndpoint,
        ): SshTrustedHostKey? {
            return trustedHostKey?.takeIf {
                it.endpoint == endpoint
            }
        }

        override suspend fun saveTrustedHostKey(
            trustedHostKey: SshTrustedHostKey,
        ) = Unit

        override suspend fun removeTrustedHostKey(
            endpoint: SshHostEndpoint,
        ) = Unit
    }
}
