package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Domain-level error categories for SSH connection attempts.
 *
 * These values are intentionally independent from any third-party SSH library so presentation
 * code can remain stable if the underlying implementation changes.
 */
enum class SshConnectionError {
    TargetNotFound,
    MissingConnectionMetadata,
    UnknownHost,
    ConnectionTimeout,
    AuthenticationRequired,
    PrivateKeyUnavailable,
    PrivateKeyEmpty,
    PrivateKeyTooLarge,
    PrivateKeyUnsupportedFormat,
    PrivateKeyInvalid,
    PrivateKeyPassphraseRequired,
    PrivateKeyPassphraseRejected,
    AuthenticationRejected,
    HostTrustRequired,
    UnsupportedConfiguration,
    Unknown,
}
