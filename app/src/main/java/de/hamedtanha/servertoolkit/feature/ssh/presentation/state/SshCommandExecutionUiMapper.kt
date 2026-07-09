package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult

/**
 * Maps domain-level SSH command execution results into UI-safe state values.
 */
internal fun SshCommandExecutionUiState.withCommandText(
    command: String,
): SshCommandExecutionUiState {
    return if (command.isBlank()) {
        copy(
            command = command,
            status = SshCommandExecutionStatus.Idle,
            statusLabel = "No command entered",
            message = "Enter a command before running it.",
            detail = "Command execution requires a non-blank command.",
            stdout = "",
            stderr = "",
            exitStatus = null,
        )
    } else {
        copy(
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

internal fun SshCommandExecutionUiState.asSessionUnavailable(): SshCommandExecutionUiState {
    return copy(
        status = SshCommandExecutionStatus.Idle,
        statusLabel = "No command executed",
        message = "Connect to an SSH session before running a command.",
        detail = "Command execution is non-interactive and does not provide terminal emulation.",
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
            statusLabel = result.error.toStatusLabel(),
            message = result.error.toUserMessage(),
            detail = result.error.toUserDetail(),
            stdout = "",
            stderr = "",
            exitStatus = null,
        )
    }
}

private fun SshCommandExecutionError.toStatusLabel(): String {
    return when (this) {
        SshCommandExecutionError.SessionNotFound -> "No active SSH session"
        SshCommandExecutionError.ChannelOpenFailed -> "Command channel unavailable"
        SshCommandExecutionError.CommandExecutionFailed -> "Command execution failed"
        SshCommandExecutionError.CommandTimedOut -> "Command timed out"
        SshCommandExecutionError.CommandCancelled -> "Command cancelled"
        SshCommandExecutionError.UnsupportedConfiguration -> "Unsupported command configuration"
        SshCommandExecutionError.Unknown -> "Command failed"
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

private fun SshCommandExecutionError.toUserDetail(): String {
    return when (this) {
        SshCommandExecutionError.SessionNotFound -> "Connect to the server again before running a command."
        SshCommandExecutionError.ChannelOpenFailed -> "The active SSH session could not open a non-interactive command channel."
        SshCommandExecutionError.CommandExecutionFailed -> "The command channel opened, but command execution did not complete successfully."
        SshCommandExecutionError.CommandTimedOut -> "The command did not complete before the configured timeout elapsed."
        SshCommandExecutionError.CommandCancelled -> "The command was cancelled and the command channel was cleaned up."
        SshCommandExecutionError.UnsupportedConfiguration -> "Use a supported non-interactive command execution configuration."
        SshCommandExecutionError.Unknown -> "The command did not complete successfully."
    }
}
