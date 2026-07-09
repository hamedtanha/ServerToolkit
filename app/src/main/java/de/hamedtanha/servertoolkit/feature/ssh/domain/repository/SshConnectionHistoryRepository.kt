package de.hamedtanha.servertoolkit.feature.ssh.domain.repository

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryEntry
import kotlinx.coroutines.flow.Flow

interface SshConnectionHistoryRepository {

    fun observeConnectionHistory(): Flow<List<SshConnectionHistoryEntry>>

    fun observeConnectionHistoryForServer(serverId: String): Flow<List<SshConnectionHistoryEntry>>

    suspend fun getConnectionHistoryEntry(entryId: String): SshConnectionHistoryEntry?

    suspend fun saveConnectionHistoryEntry(entry: SshConnectionHistoryEntry)

    suspend fun deleteConnectionHistoryEntry(entryId: String)
}
