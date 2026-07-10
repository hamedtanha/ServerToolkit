package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryStatus

data class SshConnectionHistoryItemUiState(
    val id: String,
    val endpointLabel: String,
    val username: String,
    val status: SshConnectionHistoryStatus,
    val statusLabel: String,
    val attemptedAtEpochMillis: Long,
    val completedAtEpochMillis: Long?,
    val errorLabel: String?,
) {
    val durationMillis: Long?
        get() = completedAtEpochMillis?.minus(attemptedAtEpochMillis)
}

data class SshConnectionHistoryUiState(
    val serverId: String = "",
    val entries: List<SshConnectionHistoryItemUiState> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean
        get() = entries.isEmpty() && !isLoading && errorMessage == null

    val hasEntries: Boolean
        get() = entries.isNotEmpty()
}
