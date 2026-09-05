package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprintEncoding
import java.security.MessageDigest
import java.security.PublicKey
import java.util.Base64
import net.schmizz.sshj.common.Buffer
import net.schmizz.sshj.common.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SshjHostKeyFingerprintMapperTest {

    @Test
    fun `maps fixed RSA public key to OpenSSH sha256 known answer`() {
        val publicKey = publicKeyFromOpenSsh(RSA_OPENSSH_PUBLIC_KEY)

        val fingerprint = publicKey.toOpenSshSha256Fingerprint()

        assertEquals("SHA256", fingerprint.algorithm)
        assertEquals(SshHostKeyFingerprintEncoding.OpenSshWire, fingerprint.encoding)
        assertEquals(RSA_OPENSSH_SHA256_FINGERPRINT, fingerprint.value)
    }

    @Test
    fun `maps fixed Ed25519 public key to OpenSSH sha256 known answer`() {
        val publicKey = publicKeyFromOpenSsh(ED25519_OPENSSH_PUBLIC_KEY)

        val fingerprint = publicKey.toOpenSshSha256Fingerprint()

        assertEquals("SHA256", fingerprint.algorithm)
        assertEquals(SshHostKeyFingerprintEncoding.OpenSshWire, fingerprint.encoding)
        assertEquals(ED25519_OPENSSH_SHA256_FINGERPRINT, fingerprint.value)
    }

    @Test
    fun `preserves historical Java encoded sha256 mapping only as legacy encoding`() {
        val publicKey = publicKeyFromOpenSsh(RSA_OPENSSH_PUBLIC_KEY)
        val expectedLegacyValue = Base64
            .getEncoder()
            .withoutPadding()
            .encodeToString(
                MessageDigest.getInstance("SHA-256").digest(publicKey.encoded),
            )

        val legacyFingerprint = publicKey.toLegacyJavaEncodedSha256Fingerprint()
        val canonicalFingerprint = publicKey.toOpenSshSha256Fingerprint()

        assertEquals("SHA256", legacyFingerprint.algorithm)
        assertEquals(
            SshHostKeyFingerprintEncoding.LegacyJavaPublicKey,
            legacyFingerprint.encoding,
        )
        assertEquals(expectedLegacyValue, legacyFingerprint.value)
        assertNotEquals(canonicalFingerprint, legacyFingerprint)
    }

    @Test
    fun `preserves historical SSHJ md5 mapping for legacy trust verification`() {
        val publicKey = publicKeyFromOpenSsh(ED25519_OPENSSH_PUBLIC_KEY)

        val fingerprint = publicKey.toLegacySshjMd5Fingerprint()

        assertEquals("MD5", fingerprint.algorithm)
        assertEquals(SshHostKeyFingerprintEncoding.OpenSshWire, fingerprint.encoding)
        assertEquals(SecurityUtils.getFingerprint(publicKey), fingerprint.value)
    }

    private fun publicKeyFromOpenSsh(openSshPublicKey: String): PublicKey {
        val encodedKey = openSshPublicKey
            .substringAfter(' ')
            .substringBefore(' ')
        val sshPublicKeyBlob = Base64.getDecoder().decode(encodedKey)
        return Buffer.PlainBuffer(sshPublicKeyBlob).readPublicKey()
    }

    private companion object {
        // Fixed test-only RSA public key generated independently in OpenSSH public-key format.
        // The expected SHA-256 value is the digest of the decoded SSH public-key blob, not an
        // expectation derived through the production mapper.
        const val RSA_OPENSSH_PUBLIC_KEY =
            "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDfI6qmnaFl0GyllScg83v+DlJO5EbC+dLU9Zbqg0KU0vZwZIsATxfcDiLWX81D8vCxe2gKOX3bdyRYiIsUBhhgnQV4w13uguz/FFlt7cMrZbu5uqJLPy/EUdf1aUNAUph/PF4cD2P/ChI34078T8wP//9qyZVaPNW25OxsmHhzCqeUa0Qq/e3zeQUVViYVP3nz6cVxnFq1w4DyOogJFm29eVJEGtClWE5DLQA9NcFaXBfFP4Znxd3bw6dGqNWiVhK67N6Wj0ROOODT4SdR3hlvm9YICfGEQG40ioTSIUMSiI0J/VGhyESD7AN37uZ4oyH1wDVznLFVOodQzjog3iyh"
        const val RSA_OPENSSH_SHA256_FINGERPRINT =
            "JNLByQsTp6YjRq1xgdhSMqHOo1Gczypf6hceOo3C7O4"

        // SSHJ v0.38.0 upstream test fixture and its fixed SHA-256 verifier expectation.
        const val ED25519_OPENSSH_PUBLIC_KEY =
            "ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAIDAdJiRkkBM8yC8seTEoAn2PfwbLKrkcahZ0xxPoWICJ"
        const val ED25519_OPENSSH_SHA256_FINGERPRINT =
            "oQGbQTujGeNIgh0ONthcEpA/BHxtt3rcYY+NxXTxQjs"
    }
}
