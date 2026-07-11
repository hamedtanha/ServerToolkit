package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Stable failure categories for private-key source lifecycle and bounded content access.
 */
enum class SshPrivateKeySourceError {
    AlreadyConsumed,
    Invalidated,
    DocumentUnavailable,
    EmptyDocument,
    DocumentTooLarge,
    ReadFailed,
}
