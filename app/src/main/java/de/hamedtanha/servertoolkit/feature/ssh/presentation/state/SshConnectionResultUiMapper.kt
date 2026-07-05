package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult

/**
 * Maps domain-level SSH connection results into UI-safe state values.
 */
internal fun SshUiState.withConnectionResult(
    result: SshConnectionResult,
): SshUiState {
    return when (result) {
        SshConnectionResult.Connected -> copy(
            status = SshConnectionStatus.Connected,
            statusLabel = "Connected",
            message = "SSH connection is ready.",
            detail = "Session handling will be introduced in a later implementation slice.",
        )

        is SshConnectionResult.Failed -> copy(
            status = SshConnectionStatus.Failed,
            statusLabel = "Connection failed",
            message = result.error.toUserMessage(),
            detail = "No SSH session was opened.",
        )
    }
}

private fun SshConnectionError.toUserMessage(): String {
    return when (this) {
        SshConnectionError.TargetNotFound -> "The selected server could not be found."
        SshConnectionError.MissingConnectionMetadata -> "The selected server is missing required SSH connection metadata."
        SshConnectionError.UnknownHost -> "The server host could not be resolved."
        SshConnectionError.ConnectionTimeout -> "The connection attempt timed out."
        SshConnectionError.AuthenticationRequired -> "Authentication is required before connecting."
        SshConnectionError.HostTrustRequired -> "Server identity review is required before connecting."
        SshConnectionError.UnsupportedConfiguration -> "This SSH configuration is not supported yet."
        SshConnectionError.Unknown -> "The connection attempt failed."
    }
}
