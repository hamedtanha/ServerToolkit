package de.hamedtanha.servertoolkit.feature.ssh.domain.model

sealed interface SshConnectionAttemptOutcome {

    data class ConnectionResult(
        val result: SshConnectionResult,
    ) : SshConnectionAttemptOutcome

    data class HostTrustDecisionRequired(
        val decision: SshHostTrustDecision,
    ) : SshConnectionAttemptOutcome
}
