package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import de.hamedtanha.servertoolkit.core.test.MainDispatcherRule
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryEntry
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionHistoryStatus
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.navigation.SshConnectionHistoryDestination
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
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

        val uiState = viewModel.uiState.value

        assertEquals("server-1", uiState.serverId)
        assertEquals(1, uiState.entries.size)
        assertEquals("history-1", uiState.entries.single().id)
        assertTrue(uiState.hasEntries)
        assertFalse(uiState.isEmpty)
        assertFalse(uiState.isLoading)
    }

    @Test
    fun `exposes empty state when server has no connection history`() = runTest {
        val viewModel = createViewModel(FakeSshConnectionHistoryRepository())

        val uiState = viewModel.uiState.value

        assertTrue(uiState.isEmpty)
        assertFalse(uiState.hasEntries)
        assertNull(uiState.errorMessage)
        assertFalse(uiState.isLoading)
    }

    @Test
    fun `initial observation failure uses stable message and retry restarts once`() = runTest {
        val repository = ScriptedConnectionHistoryRepository()
        var attempt = 0
        repository.observeAction = {
            attempt += 1
            if (attempt == 1) {
                flow {
                    throw IllegalStateException("sqlite: /private/history.db")
                }
            } else {
                flowOf(listOf(historyEntry(id = "history-2")))
            }
        }
        val viewModel = createViewModel(repository)

        assertEquals(1, repository.observeCallCount)
        assertEquals(
            "SSH connection history could not be loaded.",
            viewModel.uiState.value.errorMessage,
        )
        assertTrue(viewModel.uiState.value.entries.isEmpty())
        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.onRetryLoad()

        assertEquals(2, repository.observeCallCount)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals("history-2", viewModel.uiState.value.entries.single().id)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `later observation failure preserves entries and retry replaces them`() = runTest {
        val repository = ScriptedConnectionHistoryRepository()
        var attempt = 0
        repository.observeAction = {
            attempt += 1
            if (attempt == 1) {
                flow {
                    emit(listOf(historyEntry(id = "history-1")))
                    throw IllegalStateException("provider internals")
                }
            } else {
                flowOf(listOf(historyEntry(id = "history-2")))
            }
        }
        val viewModel = createViewModel(repository)

        val failedState = viewModel.uiState.value
        assertEquals("history-1", failedState.entries.single().id)
        assertEquals(
            "SSH connection history could not be loaded.",
            failedState.errorMessage,
        )
        assertTrue(failedState.hasEntries)

        viewModel.onRetryLoad()

        assertEquals(2, repository.observeCallCount)
        assertEquals("history-2", viewModel.uiState.value.entries.single().id)
        assertNull(viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `observation cancellation is not mapped to repository failure message`() = runTest {
        val repository = ScriptedConnectionHistoryRepository().apply {
            observeAction = {
                flow {
                    throw CancellationException("cancel history observation")
                }
            }
        }
        val viewModel = createViewModel(repository)

        assertEquals(1, repository.observeCallCount)
        assertNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.entries.isEmpty())
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

    private fun historyEntry(
        id: String = "history-1",
    ): SshConnectionHistoryEntry {
        return SshConnectionHistoryEntry(
            id = id,
            serverId = "server-1",
            host = "example.com",
            port = 2222,
            username = "admin",
            status = SshConnectionHistoryStatus.Connected,
            attemptedAtEpochMillis = 1_000L,
            completedAtEpochMillis = 2_000L,
        )
    }

    private class ScriptedConnectionHistoryRepository : SshConnectionHistoryRepository {

        var observeCallCount: Int = 0
            private set

        var observeAction: () -> Flow<List<SshConnectionHistoryEntry>> = {
            flowOf(emptyList())
        }

        override fun observeConnectionHistory(): Flow<List<SshConnectionHistoryEntry>> {
            return observeAction()
        }

        override fun observeConnectionHistoryForServer(
            serverId: String,
        ): Flow<List<SshConnectionHistoryEntry>> {
            observeCallCount += 1
            return observeAction()
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
