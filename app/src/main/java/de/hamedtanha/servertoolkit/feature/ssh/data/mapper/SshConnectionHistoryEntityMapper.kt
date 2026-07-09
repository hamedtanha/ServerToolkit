package de.hamedtanha.servertoolkit.feature.ssh.data.mapper

import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshConnectionHistoryEntity
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryEntry
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryStatus

internal fun SshConnectionHistoryEntry.toEntity(): SshConnectionHistoryEntity {
    return SshConnectionHistoryEntity(
        id = id,
        serverId = serverId,
        host = host,
        port = port,
        username = username,
        status = status.name,
        attemptedAtEpochMillis = attemptedAtEpochMillis,
        completedAtEpochMillis = completedAtEpochMillis,
        connectionError = connectionError?.name,
    )
}

internal fun SshConnectionHistoryEntity.toDomain(): SshConnectionHistoryEntry {
    return SshConnectionHistoryEntry(
        id = id,
        serverId = serverId,
        host = host,
        port = port,
        username = username,
        status = SshConnectionHistoryStatus.valueOf(status),
        attemptedAtEpochMillis = attemptedAtEpochMillis,
        completedAtEpochMillis = completedAtEpochMillis,
        connectionError = connectionError?.let(SshConnectionError::valueOf),
    )
}
