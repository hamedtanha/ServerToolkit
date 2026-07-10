package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryEntry
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryStatus

internal fun SshConnectionHistoryEntry.toUiState(): SshConnectionHistoryItemUiState {
    return SshConnectionHistoryItemUiState(
        id = id,
        endpointLabel = "$host:$port",
        username = username,
        status = status,
        statusLabel = status.toStatusLabel(),
        attemptedAtEpochMillis = attemptedAtEpochMillis,
        completedAtEpochMillis = completedAtEpochMillis,
        errorLabel = connectionError?.toStatusLabel(),
    )
}

private fun SshConnectionHistoryStatus.toStatusLabel(): String {
    return when (this) {
        SshConnectionHistoryStatus.Connected -> "Connected"
        SshConnectionHistoryStatus.Failed -> "Failed"
        SshConnectionHistoryStatus.Cancelled -> "Cancelled"
    }
}
