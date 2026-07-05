package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult

/**
 * Maps domain-level SSH command execution results into UI-safe state values.
 */
internal fun SshCommandExecutionUiState.withCommandText(
    command: String,
): SshCommandExecutionUiState {
    return copy(
        command = command,
        status = SshCommandExecutionStatus.Idle,
        statusLabel = "Command ready",
        message = "Command is ready to run.",
        detail = "Output will be shown only for this active screen state.",
        stdout = "",
        stderr = "",
        exitStatus = null,
    )
}

internal fun SshCommandExecutionUiState.asRunning(): SshCommandExecutionUiState {
    return copy(
        status = SshCommandExecutionStatus.Running,
        statusLabel = "Running command",
        message = "Command execution is in progress.",
        detail = "The command is executed through the active project-owned SSH session handle.",
        stdout = "",
        stderr = "",
        exitStatus = null,
    )
}

internal fun SshCommandExecutionUiState.withExecutionResult(
    result: SshCommandExecutionResult,
): SshCommandExecutionUiState {
    return when (result) {
        is SshCommandExecutionResult.Completed -> copy(
            status = SshCommandExecutionStatus.Completed,
            statusLabel = "Command completed",
            message = "Command execution completed.",
            detail = "Exit status: ${result.output.exitStatus ?: "unknown"}",
            stdout = result.output.stdout,
            stderr = result.output.stderr,
            exitStatus = result.output.exitStatus,
        )

        is SshCommandExecutionResult.Failed -> copy(
            status = SshCommandExecutionStatus.Failed,
            statusLabel = "Command failed",
            message = result.error.toUserMessage(),
            detail = "No terminal session was opened.",
            stdout = "",
            stderr = "",
            exitStatus = null,
        )
    }
}

private fun SshCommandExecutionError.toUserMessage(): String {
    return when (this) {
        SshCommandExecutionError.SessionNotFound -> "No active SSH session was found."
        SshCommandExecutionError.ChannelOpenFailed -> "The SSH command channel could not be opened."
        SshCommandExecutionError.CommandExecutionFailed -> "The command could not be executed."
        SshCommandExecutionError.CommandTimedOut -> "The command timed out."
        SshCommandExecutionError.CommandCancelled -> "The command was cancelled."
        SshCommandExecutionError.UnsupportedConfiguration -> "This command configuration is not supported."
        SshCommandExecutionError.Unknown -> "Command execution failed."
    }
}
