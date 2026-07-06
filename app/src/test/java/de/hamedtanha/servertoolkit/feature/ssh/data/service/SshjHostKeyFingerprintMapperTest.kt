package de.hamedtanha.servertoolkit.feature.ssh.data.service

import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.PublicKey
import java.util.Base64
import org.junit.Assert.assertEquals
import org.junit.Test

class SshjHostKeyFingerprintMapperTest {

    @Test
    fun `maps public key to sha256 host key fingerprint`() {
        val publicKey = generatePublicKey()

        val fingerprint = publicKey.toSshjHostKeyFingerprint()

        assertEquals("SHA256", fingerprint.algorithm)
        assertEquals(publicKey.expectedSha256Fingerprint(), fingerprint.value)
    }

    private fun PublicKey.expectedSha256Fingerprint(): String {
        val digest = MessageDigest
            .getInstance("SHA-256")
            .digest(encoded)

        return Base64
            .getEncoder()
            .withoutPadding()
            .encodeToString(digest)
    }

    private fun generatePublicKey() = KeyPairGenerator
        .getInstance("RSA")
        .generateKeyPair()
        .public
}
