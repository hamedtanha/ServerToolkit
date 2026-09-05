package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import de.hamedtanha.servertoolkit.core.test.MainDispatcherRule
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.test.FakeServerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EditServerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial load failure uses stable message and retry reloads once`() = runTest {
        val repository = FakeServerRepository()
        var loadAttempt = 0
        repository.getByIdAction = {
            loadAttempt += 1
            if (loadAttempt == 1) {
                throw IllegalStateException("database path /private/server.db")
            }
            server()
        }
        val viewModel = createViewModel(repository)

        assertEquals(1, loadAttempt)
        assertEquals("Server could not be loaded.", viewModel.uiState.value.formMessage)
        assertTrue(viewModel.uiState.value.canRetryLoad)
        assertFalse(viewModel.uiState.value.isLoading)

        viewModel.onRetryLoad()

        val state = viewModel.uiState.value
        assertEquals(2, loadAttempt)
        assertEquals("Production", state.name)
        assertEquals("prod.example.com", state.host)
        assertNull(state.formMessage)
        assertFalse(state.canRetryLoad)
        assertFalse(state.isLoading)
    }

    @Test
    fun `update failure preserves edited form and uses stable message`() = runTest {
        val repository = FakeServerRepository().apply {
            getByIdAction = { server() }
            saveAction = {
                throw IllegalStateException("SQLITE_CONSTRAINT internal detail")
            }
        }
        val viewModel = createViewModel(repository)
        viewModel.onNameChanged("Updated production")

        viewModel.onSaveClicked()

        val state = viewModel.uiState.value
        assertEquals("Updated production", state.name)
        assertEquals("Server could not be updated.", state.formMessage)
        assertFalse(state.isSaving)
        assertFalse(state.isSaved)
    }

    @Test
    fun `initial load cancellation is not converted into load failure`() = runTest {
        val repository = FakeServerRepository().apply {
            getByIdAction = {
                throw CancellationException("cancel load")
            }
        }
        val viewModel = createViewModel(repository)

        val state = viewModel.uiState.value
        assertEquals("Loading server details.", state.formMessage)
        assertTrue(state.isLoading)
        assertFalse(state.canRetryLoad)
    }

    @Test
    fun `update cancellation is not converted into update failure`() = runTest {
        val repository = FakeServerRepository().apply {
            getByIdAction = { server() }
            saveAction = {
                throw CancellationException("cancel update")
            }
        }
        val viewModel = createViewModel(repository)
        viewModel.onNameChanged("Updated production")

        viewModel.onSaveClicked()

        val state = viewModel.uiState.value
        assertEquals("Updated production", state.name)
        assertNull(state.formMessage)
        assertTrue(state.isSaving)
        assertFalse(state.isSaved)
    }

    private fun createViewModel(repository: FakeServerRepository): EditServerViewModel {
        return EditServerViewModel(
            serverRepository = repository,
            savedStateHandle = SavedStateHandle(mapOf("serverId" to "server-1")),
        )
    }

    private fun server(): Server {
        return Server(
            id = "server-1",
            name = "Production",
            host = "prod.example.com",
            sshPort = 22,
            sshUsername = "admin",
        )
    }
}
