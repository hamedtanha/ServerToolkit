package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Domain-level error categories for SSH command execution planning.
 *
 * These values are intentionally independent from SSHJ so presentation code remains stable if the
 * underlying SSH implementation changes.
 */
enum class SshCommandExecutionError {
    SessionNotFound,
    ChannelOpenFailed,
    CommandExecutionFailed,
    CommandTimedOut,
    CommandCancelled,
    UnsupportedConfiguration,
    Unknown,
}
