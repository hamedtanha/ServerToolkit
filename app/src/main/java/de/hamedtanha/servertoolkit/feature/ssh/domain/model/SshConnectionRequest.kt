package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Describes the non-sensitive target information required to start an SSH connection attempt.
 *
 * Authentication input is intentionally excluded from this model until the authentication flow
 * is designed and implemented explicitly.
 */
data class SshConnectionRequest(
    val serverId: String,
    val host: String,
    val port: Int,
    val username: String,
)
