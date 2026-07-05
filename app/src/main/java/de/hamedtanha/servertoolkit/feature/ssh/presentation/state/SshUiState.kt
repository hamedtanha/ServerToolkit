package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

data class SshUiState(
    val title: String = "SSH connection",
    val serverId: String = "",
    val status: SshConnectionStatus = SshConnectionStatus.NotStarted,
    val statusLabel: String = "Not connected",
    val message: String = "SSH connectivity is not implemented yet.",
    val detail: String = "This placeholder prepares the navigation and UI boundary for the future SSH workflow.",
    val hostKeyReview: SshHostKeyReviewUiState? = null,
    val authenticationInput: SshAuthenticationInputUiState = SshAuthenticationInputUiState(),
    val commandExecution: SshCommandExecutionUiState = SshCommandExecutionUiState(),
) {

    val isHostKeyReviewRequired: Boolean
        get() = hostKeyReview != null

    val canExecuteCommand: Boolean
        get() = status == SshConnectionStatus.Connected && commandExecution.canExecute
}
