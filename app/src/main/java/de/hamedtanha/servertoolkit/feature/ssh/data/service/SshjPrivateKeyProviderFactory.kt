package de.hamedtanha.servertoolkit.feature.ssh.data.service

import com.hierynomus.sshj.common.KeyDecryptionFailedException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.Factory
import net.schmizz.sshj.common.KeyType
import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider
import net.schmizz.sshj.userauth.keyprovider.KeyFormat
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil
import net.schmizz.sshj.userauth.password.PasswordUtils

/**
 * Creates validated SSHJ key providers from short-lived in-memory private-key material.
 *
 * This factory performs parsing and key-type validation entirely inside the SSH data layer.
 * It does not create temporary files and does not retain the supplied byte buffer.
 */
internal class SshjPrivateKeyProviderFactory(
    private val metadataValidator: OpenSshPrivateKeyMetadataValidator =
        OpenSshPrivateKeyMetadataValidator(),
) {

    fun create(
        client: SSHClient,
        privateKeyBytes: ByteArray,
        privateKeySize: Int,
        passphrase: String,
    ): FileKeyProvider {
        require(privateKeySize in 1..privateKeyBytes.size) {
            "Private-key size must be within the supplied buffer."
        }

        val keyFormat = detectPrivateKeyFormat(
            privateKeyBytes = privateKeyBytes,
            privateKeySize = privateKeySize,
        )

        if (keyFormat != KeyFormat.OpenSSHv1) {
            throw SshjPrivateKeyAuthenticationException.UnsupportedFormat()
        }

        metadataValidator.validate(
            privateKeyBytes = privateKeyBytes,
            privateKeySize = privateKeySize,
        )

        val keyProvider = Factory.Named.Util.create(
            client.transport.config.fileKeyProviderFactories,
            keyFormat.toString(),
        ) ?: throw SshjPrivateKeyAuthenticationException.UnsupportedFormat()

        validateKeyProvider(
            keyProvider = keyProvider,
            privateKeyBytes = privateKeyBytes,
            privateKeySize = privateKeySize,
            passphrase = passphrase,
        )

        return keyProvider
    }

    private fun validateKeyProvider(
        keyProvider: FileKeyProvider,
        privateKeyBytes: ByteArray,
        privateKeySize: Int,
        passphrase: String,
    ) {
        val passphraseChars = passphrase.toCharArray()

        try {
            InputStreamReader(
                ByteArrayInputStream(
                    privateKeyBytes,
                    0,
                    privateKeySize,
                ),
                StandardCharsets.UTF_8,
            ).use { privateKeyReader ->
                keyProvider.init(
                    privateKeyReader,
                    PasswordUtils.createOneOff(passphraseChars),
                )

                val keyType = keyProvider.type
                keyProvider.private

                if (keyType != KeyType.RSA && keyType != KeyType.ED25519) {
                    throw SshjPrivateKeyAuthenticationException.UnsupportedFormat()
                }
            }
        } catch (error: SshjPrivateKeyAuthenticationException) {
            throw error
        } catch (error: Exception) {
            if (error.containsCause<KeyDecryptionFailedException>()) {
                if (passphrase.isEmpty()) {
                    throw SshjPrivateKeyAuthenticationException.PassphraseRequired(error)
                }

                throw SshjPrivateKeyAuthenticationException.PassphraseRejected(error)
            }

            throw SshjPrivateKeyAuthenticationException.InvalidKey(error)
        } finally {
            passphraseChars.fill('\u0000')
        }
    }

    private fun detectPrivateKeyFormat(
        privateKeyBytes: ByteArray,
        privateKeySize: Int,
    ): KeyFormat {
        return try {
            InputStreamReader(
                ByteArrayInputStream(
                    privateKeyBytes,
                    0,
                    privateKeySize,
                ),
                StandardCharsets.UTF_8,
            ).use { privateKeyReader ->
                KeyProviderUtil.detectKeyFileFormat(
                    privateKeyReader,
                    false,
                )
            }
        } catch (error: IOException) {
            throw SshjPrivateKeyAuthenticationException.InvalidKey(error)
        }
    }
}

private inline fun <reified T : Throwable> Throwable.containsCause(): Boolean {
    var current: Throwable? = this

    while (current != null) {
        if (current is T) {
            return true
        }

        current = current.cause
    }

    return false
}
