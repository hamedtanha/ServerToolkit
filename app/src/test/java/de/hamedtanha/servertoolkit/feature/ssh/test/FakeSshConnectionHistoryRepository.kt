package de.hamedtanha.servertoolkit.feature.ssh.test

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryEntry
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshConnectionHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeSshConnectionHistoryRepository(
    private val onSave: suspend (SshConnectionHistoryEntry) -> Unit = {},
) : SshConnectionHistoryRepository {

    private val entries = MutableStateFlow<List<SshConnectionHistoryEntry>>(emptyList())

    val savedEntries: List<SshConnectionHistoryEntry>
        get() = entries.value

    var saveCallCount: Int = 0
        private set

    override fun observeConnectionHistory(): Flow<List<SshConnectionHistoryEntry>> {
        return entries
    }

    override fun observeConnectionHistoryForServer(
        serverId: String,
    ): Flow<List<SshConnectionHistoryEntry>> {
        return entries.map { historyEntries ->
            historyEntries.filter { entry -> entry.serverId == serverId }
        }
    }

    override suspend fun getConnectionHistoryEntry(
        entryId: String,
    ): SshConnectionHistoryEntry? {
        return entries.value.firstOrNull { entry -> entry.id == entryId }
    }

    override suspend fun saveConnectionHistoryEntry(
        entry: SshConnectionHistoryEntry,
    ) {
        onSave(entry)

        check(entries.value.none { existingEntry -> existingEntry.id == entry.id }) {
            "SSH connection history entry already exists."
        }

        saveCallCount += 1
        entries.value = entries.value + entry
    }

    override suspend fun deleteConnectionHistoryEntry(
        entryId: String,
    ) {
        entries.value = entries.value.filterNot { entry -> entry.id == entryId }
    }
}
