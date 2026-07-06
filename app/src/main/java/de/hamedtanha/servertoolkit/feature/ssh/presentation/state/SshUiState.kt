package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

data class SshUiState(
    val title: String = "SSH connection",
    val serverId: String = "",
    val status: SshConnectionStatus = SshConnectionStatus.NotStarted,
    val statusLabel: String = "Not connected",
    val message: String = "Ready to start an SSH connection attempt.",
    val detail: String = "The workflow opens a project-owned SSH session for non-interactive command execution. Terminal UI, saved commands, and persistent credentials are intentionally out of scope.",
    val hostKeyReview: SshHostKeyReviewUiState? = null,
    val authenticationInput: SshAuthenticationInputUiState = SshAuthenticationInputUiState(),
    val commandExecution: SshCommandExecutionUiState = SshCommandExecutionUiState(),
) {

    val isHostKeyReviewRequired: Boolean
        get() = hostKeyReview != null

    val canStartConnection: Boolean
        get() = status != SshConnectionStatus.Connecting &&
            status != SshConnectionStatus.Connected

    val canExecuteCommand: Boolean
        get() = status == SshConnectionStatus.Connected && commandExecution.canExecute
}
