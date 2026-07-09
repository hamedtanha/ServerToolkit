package de.hamedtanha.servertoolkit.feature.ssh.data.repository

import de.hamedtanha.servertoolkit.feature.ssh.data.local.dao.SshConnectionHistoryDao
import de.hamedtanha.servertoolkit.feature.ssh.data.mapper.toDomain
import de.hamedtanha.servertoolkit.feature.ssh.data.mapper.toEntity
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryEntry
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshConnectionHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSshConnectionHistoryRepository @Inject constructor(
    private val connectionHistoryDao: SshConnectionHistoryDao,
) : SshConnectionHistoryRepository {

    override fun observeConnectionHistory(): Flow<List<SshConnectionHistoryEntry>> {
        return connectionHistoryDao.observeConnectionHistory()
            .map { entries -> entries.map { entry -> entry.toDomain() } }
    }

    override fun observeConnectionHistoryForServer(
        serverId: String,
    ): Flow<List<SshConnectionHistoryEntry>> {
        return connectionHistoryDao.observeConnectionHistoryForServer(serverId)
            .map { entries -> entries.map { entry -> entry.toDomain() } }
    }

    override suspend fun getConnectionHistoryEntry(
        entryId: String,
    ): SshConnectionHistoryEntry? {
        return connectionHistoryDao.getConnectionHistoryEntry(entryId)?.toDomain()
    }

    override suspend fun saveConnectionHistoryEntry(
        entry: SshConnectionHistoryEntry,
    ) {
        connectionHistoryDao.insertConnectionHistoryEntry(entry.toEntity())
    }

    override suspend fun deleteConnectionHistoryEntry(
        entryId: String,
    ) {
        connectionHistoryDao.deleteConnectionHistoryEntry(entryId)
    }
}
