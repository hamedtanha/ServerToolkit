package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionStatus
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import de.hamedtanha.servertoolkit.navigation.SshDestination
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class SshViewModelTest {

    @Test
    fun `initializes ui state with navigation server id`() {
        val viewModel = createViewModel(serverId = "server-1")

        assertEquals("server-1", viewModel.uiState.value.serverId)
        assertEquals(SshConnectionStatus.NotStarted, viewModel.uiState.value.status)
    }

    @Test
    fun `sets connecting state before invoking connection service`() = runBlocking {
        lateinit var viewModel: SshViewModel
        val service = FakeSshConnectionService(
            result = SshConnectionResult.Failed(SshConnectionError.UnsupportedConfiguration),
            onConnect = {
                assertEquals(SshConnectionStatus.Connecting, viewModel.uiState.value.status)
                assertEquals("Connecting", viewModel.uiState.value.statusLabel)
            },
        )
        viewModel = createViewModel(
            serverId = "server-1",
            service = service,
        )

        viewModel.connect()

        assertEquals(SshConnectionStatus.Failed, viewModel.uiState.value.status)
        assertEquals("placeholder.invalid", service.lastRequest?.host)
    }

    @Test
    fun `maps fake connected result into ui state`() = runBlocking {
        val service = FakeSshConnectionService(
            result = SshConnectionResult.Connected,
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            service = service,
        )

        val result = service.connect(connectionRequest())
        viewModel.onConnectionResultReceived(result)

        assertEquals(SshConnectionStatus.Connected, viewModel.uiState.value.status)
        assertEquals("Connected", viewModel.uiState.value.statusLabel)
        assertEquals("SSH connection is ready.", viewModel.uiState.value.message)
    }

    @Test
    fun `maps fake failure result into ui state`() = runBlocking {
        val service = FakeSshConnectionService(
            result = SshConnectionResult.Failed(SshConnectionError.AuthenticationRequired),
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            service = service,
        )

        val result = service.connect(connectionRequest())
        viewModel.onConnectionResultReceived(result)

        assertEquals(SshConnectionStatus.Failed, viewModel.uiState.value.status)
        assertEquals("Connection failed", viewModel.uiState.value.statusLabel)
        assertEquals("Authentication is required before connecting.", viewModel.uiState.value.message)
    }

    private fun createViewModel(
        serverId: String,
        service: FakeSshConnectionService = FakeSshConnectionService(SshConnectionResult.Connected),
    ): SshViewModel {
        return SshViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(SshDestination.SERVER_ID_ARGUMENT to serverId),
            ),
            connectionService = service,
        )
    }

    private fun connectionRequest(): SshConnectionRequest {
        return SshConnectionRequest(
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
        )
    }
}
