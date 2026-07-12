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

internal fun SshConnectionError.toStatusLabel(): String {
    return when (this) {
        SshConnectionError.TargetNotFound -> "Server unavailable"
        SshConnectionError.MissingConnectionMetadata -> "Connection details missing"
        SshConnectionError.UnknownHost -> "Host unavailable"
        SshConnectionError.ConnectionTimeout -> "Connection timed out"
        SshConnectionError.AuthenticationRequired -> "Authentication required"
        SshConnectionError.PrivateKeyUnavailable -> "Private key unavailable"
        SshConnectionError.PrivateKeyEmpty -> "Private key is empty"
        SshConnectionError.PrivateKeyTooLarge -> "Private key is too large"
        SshConnectionError.PrivateKeyUnsupportedFormat -> "Private key format unsupported"
        SshConnectionError.PrivateKeyInvalid -> "Private key is invalid"
        SshConnectionError.PrivateKeyPassphraseRequired -> "Private key passphrase required"
        SshConnectionError.PrivateKeyPassphraseRejected -> "Private key passphrase rejected"
        SshConnectionError.AuthenticationRejected -> "Authentication rejected"
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
        SshConnectionError.PrivateKeyUnavailable -> "The selected private key could not be read."
        SshConnectionError.PrivateKeyEmpty -> "The selected private-key document is empty."
        SshConnectionError.PrivateKeyTooLarge -> "The selected private-key document exceeds the allowed size."
        SshConnectionError.PrivateKeyUnsupportedFormat -> "The selected private-key format is not supported."
        SshConnectionError.PrivateKeyInvalid -> "The selected private key could not be parsed."
        SshConnectionError.PrivateKeyPassphraseRequired -> "This private key requires a passphrase."
        SshConnectionError.PrivateKeyPassphraseRejected -> "The private-key passphrase was not accepted."
        SshConnectionError.AuthenticationRejected -> "The SSH server rejected the supplied credentials."
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
        SshConnectionError.PrivateKeyUnavailable -> "Select the private-key document again, then retry the connection."
        SshConnectionError.PrivateKeyEmpty -> "Select a valid OpenSSH v1 RSA or Ed25519 private-key document."
        SshConnectionError.PrivateKeyTooLarge -> "Select a private-key document no larger than 256 KiB."
        SshConnectionError.PrivateKeyUnsupportedFormat -> "Use an OpenSSH v1 RSA or Ed25519 private key."
        SshConnectionError.PrivateKeyInvalid -> "Use a valid OpenSSH v1 RSA or Ed25519 private key."
        SshConnectionError.PrivateKeyPassphraseRequired -> "Enter the private-key passphrase and try again."
        SshConnectionError.PrivateKeyPassphraseRejected -> "Check the passphrase and try again."
        SshConnectionError.AuthenticationRejected -> "Verify the username and authentication credentials, then try again."
        SshConnectionError.HostTrustRequired -> "No SSH session was opened because the server identity must be reviewed first."
        SshConnectionError.UnsupportedConfiguration -> "Use a supported SSH configuration for this connection attempt."
        SshConnectionError.Unknown -> "No SSH session was opened."
    }
}
