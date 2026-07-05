package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Result of closing a project-owned SSH session handle.
 */
sealed interface SshSessionCloseResult {

    data object Closed : SshSessionCloseResult

    data object NotFound : SshSessionCloseResult

    data object Failed : SshSessionCloseResult
}
