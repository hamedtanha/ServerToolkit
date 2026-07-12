package de.hamedtanha.servertoolkit.feature.ssh.data.service

import org.junit.Assert.fail
import org.junit.Test

class OpenSshPrivateKeyMetadataValidatorTest {

    private val validator =
        OpenSshPrivateKeyMetadataValidator()

    @Test
    fun `accepts unencrypted OpenSSH metadata`() {
        val privateKey =
            SshPrivateKeyTestFixtures.openSshEd25519Unencrypted()

        validator.validate(
            privateKeyBytes = privateKey,
            privateKeySize = privateKey.size,
        )
    }

    @Test
    fun `accepts encrypted OpenSSH metadata at maximum bcrypt rounds`() {
        val privateKey =
            SshPrivateKeyTestFixtures
                .openSshEd25519EncryptedWithBcryptRounds(
                    MAX_OPENSSH_BCRYPT_KDF_ROUNDS,
                )

        validator.validate(
            privateKeyBytes = privateKey,
            privateKeySize = privateKey.size,
        )
    }

    @Test
    fun `rejects encrypted OpenSSH metadata above maximum bcrypt rounds`() {
        assertUnsupported(
            SshPrivateKeyTestFixtures
                .openSshEd25519EncryptedWithBcryptRounds(
                    MAX_OPENSSH_BCRYPT_KDF_ROUNDS + 1L,
                ),
        )
    }

    @Test
    fun `rejects indented decoy before excessive-round OpenSSH block`() {
        val decoy =
            SshPrivateKeyTestFixtures.openSshEd25519Encrypted()
        val excessive =
            SshPrivateKeyTestFixtures
                .openSshEd25519EncryptedWithBcryptRounds(
                    MAX_OPENSSH_BCRYPT_KDF_ROUNDS + 1L,
                )

        val privateKey =
            byteArrayOf(' '.code.toByte()) +
                decoy +
                byteArrayOf('\n'.code.toByte()) +
                excessive

        assertInvalid(privateKey)
    }

    @Test
    fun `rejects encrypted OpenSSH metadata with zero bcrypt rounds`() {
        assertUnsupported(
            SshPrivateKeyTestFixtures
                .openSshEd25519EncryptedWithBcryptRounds(
                    0L,
                ),
        )
    }

    private fun assertInvalid(
        privateKey: ByteArray,
    ) {
        try {
            validator.validate(
                privateKeyBytes = privateKey,
                privateKeySize = privateKey.size,
            )
            fail("Expected invalid OpenSSH metadata.")
        } catch (_: SshjPrivateKeyAuthenticationException.InvalidKey) {
            // Expected.
        }
    }

    private fun assertUnsupported(
        privateKey: ByteArray,
    ) {
        try {
            validator.validate(
                privateKeyBytes = privateKey,
                privateKeySize = privateKey.size,
            )
            fail("Expected unsupported OpenSSH metadata.")
        } catch (_: SshjPrivateKeyAuthenticationException.UnsupportedFormat) {
            // Expected.
        }
    }
}
