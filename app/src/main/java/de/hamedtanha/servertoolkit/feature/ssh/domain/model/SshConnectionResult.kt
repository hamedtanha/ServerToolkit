package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Result of an SSH connection attempt expressed in project-owned domain language.
 */
sealed interface SshConnectionResult {

    /**
     * The connection attempt reached the domain-level connected state.
     *
     * The exposed handle is project-owned metadata only. It must not expose SSHJ session objects,
     * sockets, credentials, private keys, passphrases, or command execution channels.
     */
    data class Connected(
        val sessionHandle: SshSessionHandle,
    ) : SshConnectionResult

    /**
     * The connection attempt failed with a stable domain-level error category.
     */
    data class Failed(
        val error: SshConnectionError,
    ) : SshConnectionResult
}
