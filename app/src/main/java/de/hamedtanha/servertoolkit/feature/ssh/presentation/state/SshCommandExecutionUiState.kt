package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

/**
 * UI-safe state for non-interactive SSH command execution.
 *
 * Command output can contain sensitive operational data. This state must not be persisted or logged.
 */
data class SshCommandExecutionUiState(
    val command: String = "",
    val status: SshCommandExecutionStatus = SshCommandExecutionStatus.Idle,
    val statusLabel: String = "No command executed",
    val message: String = "Connect to an SSH session before running a command.",
    val detail: String = "Command execution is non-interactive and does not provide terminal emulation.",
    val stdout: String = "",
    val stderr: String = "",
    val exitStatus: Int? = null,
) {

    val canExecute: Boolean
        get() = command.isNotBlank() && status != SshCommandExecutionStatus.Running

    val hasOutput: Boolean
        get() = stdout.isNotBlank() || stderr.isNotBlank() || exitStatus != null
}

enum class SshCommandExecutionStatus {
    Idle,
    Running,
    Completed,
    Failed,
}
