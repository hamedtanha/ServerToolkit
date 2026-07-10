package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import de.hamedtanha.servertoolkit.core.test.MainDispatcherRule
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryEntry
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryStatus
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.navigation.SshConnectionHistoryDestination
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SshConnectionHistoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `observes mapped connection history for navigation server`() = runTest {
        val repository = FakeSshConnectionHistoryRepository()
        repository.saveConnectionHistoryEntry(historyEntry())
        val viewModel = createViewModel(repository)

        val uiState = viewModel.uiState.first { state -> !state.isLoading }

        assertEquals("server-1", uiState.serverId)
        assertEquals(1, uiState.entries.size)
        assertEquals("history-1", uiState.entries.single().id)
        assertTrue(uiState.hasEntries)
        assertFalse(uiState.isEmpty)
    }

    @Test
    fun `exposes empty state when server has no connection history`() = runTest {
        val viewModel = createViewModel(FakeSshConnectionHistoryRepository())

        val uiState = viewModel.uiState.first { state -> !state.isLoading }

        assertTrue(uiState.isEmpty)
        assertFalse(uiState.hasEntries)
        assertEquals(null, uiState.errorMessage)
    }

    @Test
    fun `maps repository observation failure to stable error state`() = runTest {
        val viewModel = createViewModel(FailingConnectionHistoryRepository())

        val uiState = viewModel.uiState.first { state -> !state.isLoading }

        assertEquals(
            "SSH connection history could not be loaded.",
            uiState.errorMessage,
        )
        assertFalse(uiState.isEmpty)
        assertFalse(uiState.hasEntries)
    }

    private fun createViewModel(
        repository: SshConnectionHistoryRepository,
    ): SshConnectionHistoryViewModel {
        return SshConnectionHistoryViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    SshConnectionHistoryDestination.SERVER_ID_ARGUMENT to "server-1",
                ),
            ),
            connectionHistoryRepository = repository,
        )
    }

    private fun historyEntry(): SshConnectionHistoryEntry {
        return SshConnectionHistoryEntry(
            id = "history-1",
            serverId = "server-1",
            host = "example.com",
            port = 2222,
            username = "admin",
            status = SshConnectionHistoryStatus.Connected,
            attemptedAtEpochMillis = 1_000L,
            completedAtEpochMillis = 2_000L,
        )
    }

    private class FailingConnectionHistoryRepository : SshConnectionHistoryRepository {

        override fun observeConnectionHistory(): Flow<List<SshConnectionHistoryEntry>> {
            return flow {
                throw IllegalStateException("History unavailable")
            }
        }

        override fun observeConnectionHistoryForServer(
            serverId: String,
        ): Flow<List<SshConnectionHistoryEntry>> {
            return flow {
                throw IllegalStateException("History unavailable")
            }
        }

        override suspend fun getConnectionHistoryEntry(
            entryId: String,
        ): SshConnectionHistoryEntry? {
            error("Not used in this test.")
        }

        override suspend fun saveConnectionHistoryEntry(
            entry: SshConnectionHistoryEntry,
        ) {
            error("Not used in this test.")
        }

        override suspend fun deleteConnectionHistoryEntry(
            entryId: String,
        ) {
            error("Not used in this test.")
        }
    }
}
