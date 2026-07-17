package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationMethod

data class SshUiState(
    val title: String = "SSH connection",
    val serverId: String = "",
    val status: SshConnectionStatus = SshConnectionStatus.NotStarted,
    val statusLabel: String = "Not connected",
    val message: String = "Ready to start an SSH connection attempt.",
    val detail: String = "The workflow opens a project-owned SSH session for non-interactive command execution. Saved Commands can populate the command input, while terminal UI and persistent credentials remain intentionally out of scope.",
    val hostKeyReview: SshHostKeyReviewUiState? = null,
    val authenticationInput: SshAuthenticationInputUiState = SshAuthenticationInputUiState(),
    val commandExecution: SshCommandExecutionUiState = SshCommandExecutionUiState(),
    val savedCommandSelector: SshSavedCommandSelectorUiState =
        SshSavedCommandSelectorUiState.Hidden,
) {

    val isHostKeyReviewRequired: Boolean
        get() = hostKeyReview != null

    val canEditAuthenticationInput: Boolean
        get() = status != SshConnectionStatus.Connecting &&
            status != SshConnectionStatus.Connected &&
            status != SshConnectionStatus.Disconnecting

    val canStartConnection: Boolean
        get() = canEditAuthenticationInput &&
            (
                authenticationInput.selectedMethod != SshAuthenticationMethod.PRIVATE_KEY ||
                    authenticationInput.hasPrivateKeySource
                )

    val canDisconnect: Boolean
        get() = status == SshConnectionStatus.Connected &&
            commandExecution.status != SshCommandExecutionStatus.Running

    val canEditCommandInput: Boolean
        get() = status == SshConnectionStatus.Connected &&
            commandExecution.status != SshCommandExecutionStatus.Running

    val canOpenSavedCommandSelector: Boolean
        get() = canEditCommandInput && !savedCommandSelector.isVisible

    val canExecuteCommand: Boolean
        get() = status == SshConnectionStatus.Connected && commandExecution.canExecute
}
