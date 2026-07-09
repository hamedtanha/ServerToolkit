package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Domain-level status for a recorded SSH connection attempt.
 */
enum class SshConnectionHistoryStatus {
    Connected,
    Failed,
    Cancelled,
}
