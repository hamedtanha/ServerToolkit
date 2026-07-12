package de.hamedtanha.servertoolkit.feature.ssh.data.service

import java.nio.charset.StandardCharsets
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.KeyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test

class SshjPrivateKeyProviderFactoryTest {

    private val factory = SshjPrivateKeyProviderFactory()

    @Test
    fun `loads unencrypted OpenSSH Ed25519 private key`() {
        assertLoadsPrivateKey(
            privateKey = SshPrivateKeyTestFixtures.openSshEd25519Unencrypted(),
            passphrase = "",
            expectedKeyType = KeyType.ED25519,
        )
    }

    @Test
    fun `loads passphrase protected OpenSSH Ed25519 private key`() {
        assertLoadsPrivateKey(
            privateKey = SshPrivateKeyTestFixtures.openSshEd25519Encrypted(),
            passphrase = SshPrivateKeyTestFixtures.PASSPHRASE,
            expectedKeyType = KeyType.ED25519,
        )
    }

    @Test
    fun `loads unencrypted OpenSSH RSA private key`() {
        assertLoadsPrivateKey(
            privateKey = SshPrivateKeyTestFixtures.openSshRsaUnencrypted(),
            passphrase = "",
            expectedKeyType = KeyType.RSA,
        )
    }

    @Test
    fun `loads passphrase protected OpenSSH RSA private key`() {
        assertLoadsPrivateKey(
            privateKey = SshPrivateKeyTestFixtures.openSshRsaEncrypted(),
            passphrase = SshPrivateKeyTestFixtures.PASSPHRASE,
            expectedKeyType = KeyType.RSA,
        )
    }

    @Test
    fun `maps missing encrypted key passphrase to passphrase required`() {
        val privateKey = SshPrivateKeyTestFixtures.openSshEd25519Encrypted()

        withClient { client ->
            assertThrows(
                SshjPrivateKeyAuthenticationException.PassphraseRequired::class.java,
            ) {
                factory.create(
                    client = client,
                    privateKeyBytes = privateKey,
                    privateKeySize = privateKey.size,
                    passphrase = "",
                )
            }
        }
    }

    @Test
    fun `maps incorrect encrypted key passphrase to passphrase rejected`() {
        val privateKey = SshPrivateKeyTestFixtures.openSshRsaEncrypted()

        withClient { client ->
            assertThrows(
                SshjPrivateKeyAuthenticationException.PassphraseRejected::class.java,
            ) {
                factory.create(
                    client = client,
                    privateKeyBytes = privateKey,
                    privateKeySize = privateKey.size,
                    passphrase = "incorrect-test-passphrase",
                )
            }
        }
    }

    @Test
    fun `rejects valid OpenSSH ECDSA private key as unsupported`() {
        assertUnsupportedPrivateKey(
            privateKey =
                SshPrivateKeyTestFixtures.openSshEcdsaUnencrypted(),
            passphrase = "",
        )
    }

    @Test
    fun `rejects encrypted OpenSSH key with excessive bcrypt rounds`() {
        assertUnsupportedPrivateKey(
            privateKey =
                SshPrivateKeyTestFixtures.openSshEd25519EncryptedWithBcryptRounds(
                    MAX_OPENSSH_BCRYPT_KDF_ROUNDS + 1L,
                ),
            passphrase = SshPrivateKeyTestFixtures.PASSPHRASE,
        )
    }

    @Test
    fun `rejects valid unencrypted PKCS8 RSA private key as unsupported`() {
        assertUnsupportedPrivateKey(
            privateKey = SshPrivateKeyTestFixtures.pkcs8RsaUnencrypted(),
            passphrase = "",
        )
    }

    @Test
    fun `rejects valid encrypted PKCS8 RSA private key as unsupported`() {
        assertUnsupportedPrivateKey(
            privateKey = SshPrivateKeyTestFixtures.pkcs8RsaEncrypted(),
            passphrase = "servertoolkit-test-only",
        )
    }

    @Test
    fun `rejects non OpenSSH private key format`() {
        val privateKey = privateKeyDocument(
            label = "PRIVATE KEY",
            body = "AAAA",
        )

        withClient { client ->
            assertThrows(
                SshjPrivateKeyAuthenticationException.UnsupportedFormat::class.java,
            ) {
                factory.create(
                    client = client,
                    privateKeyBytes = privateKey,
                    privateKeySize = privateKey.size,
                    passphrase = "",
                )
            }
        }
    }

    @Test
    fun `maps malformed OpenSSH private key to invalid key`() {
        val privateKey = privateKeyDocument(
            label = "OPENSSH PRIVATE KEY",
            body = "not-valid-base64",
        )

        withClient { client ->
            assertThrows(
                SshjPrivateKeyAuthenticationException.InvalidKey::class.java,
            ) {
                factory.create(
                    client = client,
                    privateKeyBytes = privateKey,
                    privateKeySize = privateKey.size,
                    passphrase = "",
                )
            }
        }
    }

    @Test
    fun `rejects invalid private key size before parsing`() {
        val privateKey = byteArrayOf(1)

        withClient { client ->
            assertThrows(IllegalArgumentException::class.java) {
                factory.create(
                    client = client,
                    privateKeyBytes = privateKey,
                    privateKeySize = 0,
                    passphrase = "",
                )
            }
        }
    }

    private fun assertUnsupportedPrivateKey(
        privateKey: ByteArray,
        passphrase: String,
    ) {
        withClient { client ->
            assertThrows(
                SshjPrivateKeyAuthenticationException.UnsupportedFormat::class.java,
            ) {
                factory.create(
                    client = client,
                    privateKeyBytes = privateKey,
                    privateKeySize = privateKey.size,
                    passphrase = passphrase,
                )
            }
        }
    }

    private fun privateKeyDocument(
        label: String,
        body: String,
    ): ByteArray {
        return buildString {
            append("-----BEGIN ")
            append(label)
            appendLine("-----")
            appendLine(body)
            append("-----END ")
            append(label)
            append("-----")
        }.toByteArray(StandardCharsets.UTF_8)
    }

    private fun assertLoadsPrivateKey(
        privateKey: ByteArray,
        passphrase: String,
        expectedKeyType: KeyType,
    ) {
        withClient { client ->
            val keyProvider = factory.create(
                client = client,
                privateKeyBytes = privateKey,
                privateKeySize = privateKey.size,
                passphrase = passphrase,
            )

            assertEquals(expectedKeyType, keyProvider.type)
            assertNotNull(keyProvider.public)
            assertNotNull(keyProvider.private)
        }
    }

    private fun withClient(
        block: (SSHClient) -> Unit,
    ) {
        val client = SshjClientFactory().createClient()

        try {
            block(client)
        } finally {
            runCatching {
                client.close()
            }
        }
    }
}
