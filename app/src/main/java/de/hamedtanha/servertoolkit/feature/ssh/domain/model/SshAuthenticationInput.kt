package de.hamedtanha.servertoolkit.feature.ssh.domain.model

import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshPrivateKeySource

/**
 * Authentication input for a single SSH connection attempt.
 *
 * Implementations may temporarily hold sensitive values. They must redact secrets from string
 * representations and support explicit clearing after an attempt finishes.
 */
sealed interface SshAuthenticationInput {

    val method: SshAuthenticationMethod?

    val hasSensitiveValue: Boolean

    fun clearSensitiveValues()

    data object None : SshAuthenticationInput {

        override val method: SshAuthenticationMethod? = null

        override val hasSensitiveValue: Boolean = false

        override fun clearSensitiveValues() = Unit
    }

    class Password(
        password: String,
    ) : SshAuthenticationInput {

        var password: String = password
            private set

        override val method: SshAuthenticationMethod = SshAuthenticationMethod.PASSWORD

        override val hasSensitiveValue: Boolean
            get() = password.isNotEmpty()

        override fun clearSensitiveValues() {
            password = ""
        }

        override fun toString(): String {
            return "SshAuthenticationInput.Password(REDACTED)"
        }
    }

    class PrivateKey(
        privateKeySource: SshPrivateKeySource,
        passphrase: String,
    ) : SshAuthenticationInput {

        private var source: SshPrivateKeySource? = privateKeySource

        var passphrase: String = passphrase
            private set

        override val method: SshAuthenticationMethod = SshAuthenticationMethod.PRIVATE_KEY

        val hasPrivateKeySource: Boolean
            get() = source != null

        override val hasSensitiveValue: Boolean
            get() = hasPrivateKeySource || passphrase.isNotEmpty()

        internal fun takePrivateKeySource(): SshPrivateKeySource? {
            val currentSource = source
            source = null
            return currentSource
        }

        override fun clearSensitiveValues() {
            source?.invalidate()
            source = null
            passphrase = ""
        }

        override fun toString(): String {
            return "SshAuthenticationInput.PrivateKey(REDACTED)"
        }
    }
}
