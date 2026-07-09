package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision

internal fun SshUiState.withHostTrustDecision(
    decision: SshHostTrustDecision,
): SshUiState {
    return when (decision) {
        is SshHostTrustDecision.ReviewRequired -> copy(
            status = SshConnectionStatus.Failed,
            statusLabel = "Server identity review required",
            message = "Review the server identity before connecting.",
            detail = "Confirm the fingerprint only if it matches the server you expect.",
            hostKeyReview = decision.observedHostKey.toReviewUiState(),
        )

        is SshHostTrustDecision.Accepted -> copy(
            status = SshConnectionStatus.NotStarted,
            statusLabel = "Server identity trusted",
            message = "Server identity has been trusted.",
            detail = "Start the SSH connection again to use the trusted server identity.",
            hostKeyReview = null,
        )

        is SshHostTrustDecision.BlockedChangedHostKey -> copy(
            status = SshConnectionStatus.Failed,
            statusLabel = "Server identity changed",
            message = "The server identity changed and the connection is blocked.",
            detail = "Do not continue until the changed host key has been verified through a separate replacement flow.",
            hostKeyReview = decision.observedHostKey.toReviewUiState(),
        )
    }
}
