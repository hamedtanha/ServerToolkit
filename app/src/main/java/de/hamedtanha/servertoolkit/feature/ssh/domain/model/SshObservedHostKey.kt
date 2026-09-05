package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Host key observed during an SSH connection attempt.
 *
 * [fingerprint] is the canonical user-visible fingerprint for current trust review. Historical
 * fingerprints are retained only as compatibility candidates so unchanged keys trusted by older
 * Server Toolkit versions can still be verified without rewriting stored trust.
 *
 * This model is safe to expose to the UI because it contains only non-secret host identity
 * information.
 */
data class SshObservedHostKey(
    val endpoint: SshHostEndpoint,
    val fingerprint: SshHostKeyFingerprint,
    val legacyFingerprints: Set<SshHostKeyFingerprint> = emptySet(),
)
