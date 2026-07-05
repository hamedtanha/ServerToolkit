package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Host key explicitly trusted by the user.
 *
 * Persistent implementations must store this separately from generic server inventory metadata.
 */
data class SshTrustedHostKey(
    val endpoint: SshHostEndpoint,
    val fingerprint: SshHostKeyFingerprint,
)
