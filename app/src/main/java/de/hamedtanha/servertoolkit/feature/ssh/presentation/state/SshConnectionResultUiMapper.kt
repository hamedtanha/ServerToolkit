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
        is SshConnectionResult.Connected -> copy(
            status = SshConnectionStatus.Connected,
            statusLabel = "Connected",
            message = "SSH connection is ready.",
            detail = "A project-owned SSH session handle was opened.",
            hostKeyReview = null,
        )

        is SshConnectionResult.Failed -> copy(
            status = SshConnectionStatus.Failed,
            statusLabel = result.error.toStatusLabel(),
            message = result.error.toUserMessage(),
            detail = result.error.toUserDetail(),
            hostKeyReview = null,
        )
    }
}

private fun SshConnectionError.toStatusLabel(): String {
    return when (this) {
        SshConnectionError.TargetNotFound -> "Server unavailable"
        SshConnectionError.MissingConnectionMetadata -> "Connection details missing"
        SshConnectionError.UnknownHost -> "Host unavailable"
        SshConnectionError.ConnectionTimeout -> "Connection timed out"
        SshConnectionError.AuthenticationRequired -> "Authentication required"
        SshConnectionError.HostTrustRequired -> "Server identity review required"
        SshConnectionError.UnsupportedConfiguration -> "Unsupported SSH configuration"
        SshConnectionError.Unknown -> "Connection failed"
    }
}

private fun SshConnectionError.toUserMessage(): String {
    return when (this) {
        SshConnectionError.TargetNotFound -> "The selected server could not be found."
        SshConnectionError.MissingConnectionMetadata -> "The selected server is missing required SSH connection metadata."
        SshConnectionError.UnknownHost -> "The server host could not be resolved."
        SshConnectionError.ConnectionTimeout -> "The connection attempt timed out."
        SshConnectionError.AuthenticationRequired -> "Enter valid SSH authentication details before connecting."
        SshConnectionError.HostTrustRequired -> "Review and trust the server identity before connecting."
        SshConnectionError.UnsupportedConfiguration -> "This SSH configuration is not supported yet."
        SshConnectionError.Unknown -> "The connection attempt failed."
    }
}

private fun SshConnectionError.toUserDetail(): String {
    return when (this) {
        SshConnectionError.TargetNotFound -> "The server inventory entry may have been deleted or is no longer available."
        SshConnectionError.MissingConnectionMetadata -> "Check that the server has a host, port, and SSH username before connecting."
        SshConnectionError.UnknownHost -> "Check the host name or network connectivity, then try again."
        SshConnectionError.ConnectionTimeout -> "The SSH session was not opened before the connection timeout elapsed."
        SshConnectionError.AuthenticationRequired -> "No SSH session was opened because authentication did not complete successfully."
        SshConnectionError.HostTrustRequired -> "No SSH session was opened because the server identity must be reviewed first."
        SshConnectionError.UnsupportedConfiguration -> "Use a supported password-based SSH configuration for this connection attempt."
        SshConnectionError.Unknown -> "No SSH session was opened."
    }
}
