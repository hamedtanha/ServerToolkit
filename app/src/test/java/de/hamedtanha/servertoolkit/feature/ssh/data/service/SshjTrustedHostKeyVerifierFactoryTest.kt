package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import java.security.KeyPairGenerator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SshjTrustedHostKeyVerifierFactoryTest {

    private val factory = SshjTrustedHostKeyVerifierFactory()

    @Test
    fun `accepts matching host port and fingerprint`() {
        val publicKey = generatePublicKey()
        val verifier = factory.create(
            trustedHostKey(
                fingerprint = publicKey.toSshjHostKeyFingerprint(),
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
                fingerprint = publicKey.toSshjHostKeyFingerprint(),
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
                fingerprint = publicKey.toSshjHostKeyFingerprint(),
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
    fun `rejects mismatched fingerprint`() {
        val publicKey = generatePublicKey()
        val changedPublicKey = generatePublicKey()
        val verifier = factory.create(
            trustedHostKey(
                fingerprint = publicKey.toSshjHostKeyFingerprint(),
            ),
        )

        val accepted = verifier.verify(
            "example.com",
            22,
            changedPublicKey,
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
            algorithm = "MD5",
            value = "MD5:fingerprint",
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

    private fun generatePublicKey() = KeyPairGenerator
        .getInstance("RSA")
        .generateKeyPair()
        .public
}
