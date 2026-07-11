package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Stable project-owned result for one-shot private-key source consumption.
 */
sealed interface SshPrivateKeySourceResult<out T> {

    data class Success<T>(
        val value: T,
    ) : SshPrivateKeySourceResult<T> {

        override fun toString(): String {
            return "SshPrivateKeySourceResult.Success(REDACTED_VALUE)"
        }
    }

    data class Failure(
        val error: SshPrivateKeySourceError,
    ) : SshPrivateKeySourceResult<Nothing>
}
