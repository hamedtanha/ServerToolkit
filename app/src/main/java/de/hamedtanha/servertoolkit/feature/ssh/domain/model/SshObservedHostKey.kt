package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Host key observed during an SSH connection attempt.
 *
 * This model is safe to expose to the UI because it contains only non-secret host identity
 * information required for user trust review.
 */
data class SshObservedHostKey(
    val endpoint: SshHostEndpoint,
    val fingerprint: SshHostKeyFingerprint,
)
