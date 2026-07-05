package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
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
    fun `returns trusted when endpoint and fingerprint match`() = runBlocking {
        val trustedHostKey = trustedHostKey()
        val evaluator = SshHostTrustEvaluator(
            hostTrustRepository = FakeSshHostTrustRepository(
                trustedHostKey = trustedHostKey,
            ),
        )

        val status = evaluator.evaluate(observedHostKey())

        assertEquals(
            SshHostTrustStatus.Trusted(trustedHostKey),
            status,
        )
    }

    @Test
    fun `returns changed when fingerprint differs for the same endpoint`() = runBlocking {
        val trustedHostKey = trustedHostKey(
            fingerprint = fingerprint(value = "trusted-fingerprint"),
        )
        val observedHostKey = observedHostKey(
            fingerprint = fingerprint(value = "observed-fingerprint"),
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

    private fun fingerprint(
        algorithm: String = "SHA256",
        value: String = "abc123",
    ): SshHostKeyFingerprint {
        return SshHostKeyFingerprint(
            algorithm = algorithm,
            value = value,
        )
    }

    private fun observedHostKey(
        endpoint: SshHostEndpoint = endpoint(),
        fingerprint: SshHostKeyFingerprint = fingerprint(),
    ): SshObservedHostKey {
        return SshObservedHostKey(
            endpoint = endpoint,
            fingerprint = fingerprint,
        )
    }

    private fun trustedHostKey(
        endpoint: SshHostEndpoint = endpoint(),
        fingerprint: SshHostKeyFingerprint = fingerprint(),
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
