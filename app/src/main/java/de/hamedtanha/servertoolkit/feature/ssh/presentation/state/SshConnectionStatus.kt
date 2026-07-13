package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

/**
 * UI-safe connection status used by the SSH presentation layer.
 */
enum class SshConnectionStatus {
    NotStarted,
    Connecting,
    Connected,
    Disconnecting,
    Failed,
}
