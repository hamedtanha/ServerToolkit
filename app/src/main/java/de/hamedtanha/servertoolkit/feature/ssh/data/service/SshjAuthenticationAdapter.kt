package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationMethod
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import javax.inject.Inject

/**
 * Maps project-owned SSH authentication input into SSHJ adapter-level authentication intent.
 *
 * This adapter does not execute authentication. It keeps SSHJ-specific authentication preparation
 * behind the data-layer boundary while preserving the existing ephemeral secret-clearing behavior.
 */
class SshjAuthenticationAdapter @Inject constructor() {

    internal fun map(request: SshConnectionRequest): SshjAuthenticationMapping {
        return when (val input = request.authenticationInput) {
            SshAuthenticationInput.None -> SshjAuthenticationMapping.None(
                username = request.username,
            )

            is SshAuthenticationInput.Password -> SshjAuthenticationMapping.Password(
                username = request.username,
                input = input,
            )

            is SshAuthenticationInput.PrivateKeyPassphrase -> SshjAuthenticationMapping.PrivateKeyPassphrase(
                username = request.username,
                input = input,
            )
        }
    }
}

/**
 * SSHJ adapter-level authentication intent.
 *
 * Implementations must not expose secrets through string representations. Sensitive values remain
 * backed by the project-owned authentication input so clearing this mapping clears the original
 * per-attempt secret input as well.
 */
internal sealed interface SshjAuthenticationMapping {

    val username: String

    val method: SshAuthenticationMethod?

    val hasSensitiveValue: Boolean

    fun clearSensitiveValues()

    data class None(
        override val username: String,
    ) : SshjAuthenticationMapping {

        override val method: SshAuthenticationMethod? = null

        override val hasSensitiveValue: Boolean = false

        override fun clearSensitiveValues() = Unit
    }

    class Password(
        override val username: String,
        private val input: SshAuthenticationInput.Password,
    ) : SshjAuthenticationMapping {

        val password: String
            get() = input.password

        override val method: SshAuthenticationMethod = SshAuthenticationMethod.PASSWORD

        override val hasSensitiveValue: Boolean
            get() = input.hasSensitiveValue

        override fun clearSensitiveValues() {
            input.clearSensitiveValues()
        }

        override fun toString(): String {
            return "SshjAuthenticationMapping.Password(REDACTED)"
        }
    }

    class PrivateKeyPassphrase(
        override val username: String,
        private val input: SshAuthenticationInput.PrivateKeyPassphrase,
    ) : SshjAuthenticationMapping {

        val passphrase: String
            get() = input.passphrase

        override val method: SshAuthenticationMethod = SshAuthenticationMethod.PRIVATE_KEY

        override val hasSensitiveValue: Boolean
            get() = input.hasSensitiveValue

        override fun clearSensitiveValues() {
            input.clearSensitiveValues()
        }

        override fun toString(): String {
            return "SshjAuthenticationMapping.PrivateKeyPassphrase(REDACTED)"
        }
    }
}
