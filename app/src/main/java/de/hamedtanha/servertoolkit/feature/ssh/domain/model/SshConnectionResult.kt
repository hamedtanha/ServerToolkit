package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Result of an SSH connection attempt expressed in project-owned domain language.
 */
sealed interface SshConnectionResult {

    /**
     * The connection attempt reached the domain-level connected state.
     *
     * No session object is exposed yet because session lifecycle handling is not part of this
     * implementation slice.
     */
    data object Connected : SshConnectionResult

    /**
     * The connection attempt failed with a stable domain-level error category.
     */
    data class Failed(
        val error: SshConnectionError,
    ) : SshConnectionResult
}
