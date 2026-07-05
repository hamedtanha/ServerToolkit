package de.hamedtanha.servertoolkit.feature.ssh.domain.model

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

    class PrivateKeyPassphrase(
        passphrase: String,
    ) : SshAuthenticationInput {

        var passphrase: String = passphrase
            private set

        override val method: SshAuthenticationMethod = SshAuthenticationMethod.PRIVATE_KEY

        override val hasSensitiveValue: Boolean
            get() = passphrase.isNotEmpty()

        override fun clearSensitiveValues() {
            passphrase = ""
        }

        override fun toString(): String {
            return "SshAuthenticationInput.PrivateKeyPassphrase(REDACTED)"
        }
    }
}
