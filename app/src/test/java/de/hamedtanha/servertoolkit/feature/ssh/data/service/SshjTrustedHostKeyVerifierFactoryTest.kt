package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprintEncoding
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import java.security.KeyPairGenerator
import java.security.PublicKey
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SshjTrustedHostKeyVerifierFactoryTest {

    private val factory = SshjTrustedHostKeyVerifierFactory()

    @Test
    fun `accepts matching canonical OpenSSH sha256 fingerprint`() {
        val publicKey = generatePublicKey()
        val verifier = factory.create(
            trustedHostKey(
                fingerprint = publicKey.toOpenSshSha256Fingerprint(),
            ),
        )

        val accepted = verifier.verify(
            "example.com",
            22,
            publicKey,
        )

        assertTrue(accepted)
    }

    @Test
    fun `accepts matching historical Java encoded sha256 fingerprint`() {
        val publicKey = generatePublicKey()
        val verifier = factory.create(
            trustedHostKey(
                fingerprint = publicKey.toLegacyJavaEncodedSha256Fingerprint(),
            ),
        )

        val accepted = verifier.verify(
            "example.com",
            22,
            publicKey,
        )

        assertTrue(accepted)
    }

    @Test
    fun `accepts matching historical SSHJ md5 fingerprint`() {
        val publicKey = generatePublicKey()
        val verifier = factory.create(
            trustedHostKey(
                fingerprint = publicKey.toLegacySshjMd5Fingerprint(),
            ),
        )

        val accepted = verifier.verify(
            "example.com",
            22,
            publicKey,
        )

        assertTrue(accepted)
    }

    @Test
    fun `rejects mismatched host`() {
        val publicKey = generatePublicKey()
        val verifier = factory.create(
            trustedHostKey(
                fingerprint = publicKey.toOpenSshSha256Fingerprint(),
            ),
        )

        val accepted = verifier.verify(
            "changed.example.com",
            22,
            publicKey,
        )

        assertFalse(accepted)
    }

    @Test
    fun `rejects mismatched port`() {
        val publicKey = generatePublicKey()
        val verifier = factory.create(
            trustedHostKey(
                fingerprint = publicKey.toOpenSshSha256Fingerprint(),
            ),
        )

        val accepted = verifier.verify(
            "example.com",
            2222,
            publicKey,
        )

        assertFalse(accepted)
    }

    @Test
    fun `rejects changed key for every supported persisted fingerprint scheme`() {
        val trustedPublicKey = generatePublicKey()
        val changedPublicKey = generatePublicKey()
        val trustedFingerprints = listOf(
            trustedPublicKey.toOpenSshSha256Fingerprint(),
            trustedPublicKey.toLegacyJavaEncodedSha256Fingerprint(),
            trustedPublicKey.toLegacySshjMd5Fingerprint(),
        )

        trustedFingerprints.forEach { trustedFingerprint ->
            val verifier = factory.create(
                trustedHostKey(fingerprint = trustedFingerprint),
            )

            val accepted = verifier.verify(
                "example.com",
                22,
                changedPublicKey,
            )

            assertFalse(
                "Changed key must be rejected for $trustedFingerprint",
                accepted,
            )
        }
    }

    @Test
    fun `rejects unsupported fingerprint scheme`() {
        val publicKey = generatePublicKey()
        val verifier = factory.create(
            trustedHostKey(
                fingerprint = SshHostKeyFingerprint(
                    algorithm = "SHA512",
                    value = "unsupported",
                    encoding = SshHostKeyFingerprintEncoding.OpenSshWire,
                ),
            ),
        )

        val accepted = verifier.verify(
            "example.com",
            22,
            publicKey,
        )

        assertFalse(accepted)
    }

    @Test
    fun `returns no existing algorithms before real known-host integration exists`() {
        val verifier = factory.create(trustedHostKey())

        val algorithms = verifier.findExistingAlgorithms(
            "example.com",
            22,
        )

        assertTrue(algorithms.isEmpty())
    }

    private fun trustedHostKey(
        fingerprint: SshHostKeyFingerprint = SshHostKeyFingerprint(
            algorithm = "SHA256",
            value = "fingerprint",
            encoding = SshHostKeyFingerprintEncoding.OpenSshWire,
        ),
    ): SshTrustedHostKey {
        return SshTrustedHostKey(
            endpoint = SshHostEndpoint(
                serverId = "server-1",
                host = "example.com",
                port = 22,
            ),
            fingerprint = fingerprint,
        )
    }

    private fun generatePublicKey(): PublicKey = KeyPairGenerator
        .getInstance("RSA")
        .generateKeyPair()
        .public
}
