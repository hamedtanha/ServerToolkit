package de.hamedtanha.servertoolkit.feature.ssh.data.service

import java.security.KeyPairGenerator
import net.schmizz.sshj.common.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class SshjHostKeyFingerprintMapperTest {

    @Test
    fun `maps public key to sshj host key fingerprint`() {
        val publicKey = generatePublicKey()

        val fingerprint = publicKey.toSshjHostKeyFingerprint()

        assertEquals("MD5", fingerprint.algorithm)
        assertEquals(SecurityUtils.getFingerprint(publicKey), fingerprint.value)
    }

    private fun generatePublicKey() = KeyPairGenerator
        .getInstance("RSA")
        .generateKeyPair()
        .public
}
