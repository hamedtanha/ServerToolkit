package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel

import de.hamedtanha.servertoolkit.core.test.MainDispatcherRule
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.ServerEnvironment
import de.hamedtanha.servertoolkit.feature.serverinventory.test.FakeServerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ServerInventoryViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial observation failure uses stable message and retry restarts once`() = runTest {
        val repository = FakeServerRepository()
        var attempt = 0
        repository.observeAction = {
            attempt += 1
            if (attempt == 1) {
                flow {
                    throw IllegalStateException("sqlite: /private/internal/path")
                }
            } else {
                flowOf(listOf(server(id = "server-1", name = "Alpha")))
            }
        }
        val viewModel = ServerInventoryViewModel(repository)

        assertEquals(1, repository.observeCallCount)
        assertEquals(
            "Server inventory could not be loaded.",
            viewModel.uiState.value.errorMessage,
        )
        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.onRetryLoad()

        assertEquals(2, repository.observeCallCount)
        assertNull(viewModel.uiState.value.errorMessage)
        assertEquals(listOf("server-1"), viewModel.uiState.value.servers.map { it.id })
        assertEquals(1, viewModel.uiState.value.totalServerCount)
    }

    @Test
    fun `later observation failure preserves data filter and filterability`() = runTest {
        val repository = FakeServerRepository()
        val emissions = Channel<List<Server>>(Channel.UNLIMITED)
        repository.observeAction = { emissions.receiveAsFlow() }
        val viewModel = ServerInventoryViewModel(repository)
        val alpha = server(
            id = "server-1",
            name = "Alpha",
            environment = ServerEnvironment.PRODUCTION,
        )
        val beta = server(
            id = "server-2",
            name = "Beta",
            environment = ServerEnvironment.STAGING,
        )

        emissions.send(listOf(alpha, beta))
        viewModel.onSearchQueryChanged("Alpha")
        emissions.close(IllegalStateException("provider internals"))
        advanceUntilIdle()

        val failedState = viewModel.uiState.value
        assertEquals("Alpha", failedState.filter.searchQuery)
        assertEquals(2, failedState.totalServerCount)
        assertEquals(listOf("server-1"), failedState.servers.map { it.id })
        assertEquals(
            "Server inventory could not be loaded.",
            failedState.errorMessage,
        )

        viewModel.onSearchQueryChanged("Beta")

        assertEquals("Beta", viewModel.uiState.value.filter.searchQuery)
        assertEquals(listOf("server-2"), viewModel.uiState.value.servers.map { it.id })
        assertEquals(2, viewModel.uiState.value.totalServerCount)
    }

    @Test
    fun `delete failure uses stable message without leaking exception text`() = runTest {
        val repository = FakeServerRepository().apply {
            deleteAction = {
                throw IllegalStateException("SQLITE_CONSTRAINT servers.id")
            }
        }
        val viewModel = ServerInventoryViewModel(repository)

        viewModel.onDeleteServerConfirmed("server-1")

        assertEquals("Server could not be deleted.", viewModel.uiState.value.operationMessage)
    }

    @Test
    fun `delete cancellation is not converted into repository failure message`() = runTest {
        val repository = FakeServerRepository().apply {
            deleteAction = {
                throw CancellationException("cancel delete")
            }
        }
        val viewModel = ServerInventoryViewModel(repository)

        viewModel.onDeleteServerConfirmed("server-1")

        assertNull(viewModel.uiState.value.operationMessage)
        assertTrue(repository.observeCallCount == 1)
    }

    private fun server(
        id: String,
        name: String,
        environment: ServerEnvironment = ServerEnvironment.UNKNOWN,
    ): Server {
        return Server(
            id = id,
            name = name,
            host = "$id.example.com",
            sshUsername = "admin",
            environment = environment,
        )
    }
}
