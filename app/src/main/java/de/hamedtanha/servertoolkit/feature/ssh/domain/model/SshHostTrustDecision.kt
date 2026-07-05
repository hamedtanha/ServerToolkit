package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Domain-level decision for an observed SSH host key.
 *
 * This model separates host identity review from the actual SSH connection attempt so unknown
 * or changed host keys cannot be accepted as an incidental side effect of connecting.
 */
sealed interface SshHostTrustDecision {

    data class ReviewRequired(
        val observedHostKey: SshObservedHostKey,
    ) : SshHostTrustDecision

    data class Accepted(
        val trustedHostKey: SshTrustedHostKey,
    ) : SshHostTrustDecision

    data class BlockedChangedHostKey(
        val trustedHostKey: SshTrustedHostKey,
        val observedHostKey: SshObservedHostKey,
    ) : SshHostTrustDecision
}
