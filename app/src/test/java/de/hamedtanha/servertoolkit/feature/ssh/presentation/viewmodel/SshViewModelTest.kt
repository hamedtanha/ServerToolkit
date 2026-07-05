package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetInvalidReason
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionStatus
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import de.hamedtanha.servertoolkit.navigation.SshDestination
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SshViewModelTest {

    @Test
    fun `initializes ui state with navigation server id`() {
        val viewModel = createViewModel(serverId = "server-1")

        assertEquals("server-1", viewModel.uiState.value.serverId)
        assertEquals(SshConnectionStatus.NotStarted, viewModel.uiState.value.status)
    }

    @Test
    fun `sets connecting state before invoking connection attempt`() = runBlocking {
        lateinit var viewModel: SshViewModel
        val resolver = FakeConnectionTargetResolver(
            resolution = resolvedTarget(),
            onResolve = {
                assertEquals(SshConnectionStatus.Connecting, viewModel.uiState.value.status)
                assertEquals("Connecting", viewModel.uiState.value.statusLabel)
            },
        )
        viewModel = createViewModel(
            serverId = "server-1",
            resolver = resolver,
        )

        viewModel.connect()

        assertEquals(SshConnectionStatus.Connected, viewModel.uiState.value.status)
    }

    @Test
    fun `ignores duplicate connection attempts while one attempt is running`() = runBlocking {
        val serviceStarted = CompletableDeferred<Unit>()
        val releaseService = CompletableDeferred<Unit>()
        val service = FakeSshConnectionService(
            result = SshConnectionResult.Connected,
            onConnect = {
                serviceStarted.complete(Unit)
                releaseService.await()
            },
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            service = service,
        )

        val firstAttempt = launch {
            viewModel.connect()
        }

        serviceStarted.await()

        val duplicateAttempt = launch {
            viewModel.connect()
        }

        duplicateAttempt.join()

        assertEquals(1, service.connectCallCount)

        releaseService.complete(Unit)
        firstAttempt.join()

        assertEquals(SshConnectionStatus.Connected, viewModel.uiState.value.status)
    }

    @Test
    fun `uses resolved target metadata for connection request`() = runBlocking {
        val service = FakeSshConnectionService(SshConnectionResult.Connected)
        val viewModel = createViewModel(
            serverId = "server-1",
            resolver = FakeConnectionTargetResolver(resolvedTarget()),
            service = service,
        )

        viewModel.connect()

        assertEquals("server-1", service.lastRequest?.serverId)
        assertEquals("example.com", service.lastRequest?.host)
        assertEquals(2222, service.lastRequest?.port)
        assertEquals("admin", service.lastRequest?.username)
    }

    @Test
    fun `maps missing target into failed ui state without invoking connection service`() = runBlocking {
        val service = FakeSshConnectionService(SshConnectionResult.Connected)
        val viewModel = createViewModel(
            serverId = "missing-server",
            resolver = FakeConnectionTargetResolver(ConnectionTargetResolution.NotFound),
            service = service,
        )

        viewModel.connect()

        assertEquals(SshConnectionStatus.Failed, viewModel.uiState.value.status)
        assertEquals("The selected server could not be found.", viewModel.uiState.value.message)
        assertNull(service.lastRequest)
    }

    @Test
    fun `maps invalid target into failed ui state without invoking connection service`() = runBlocking {
        val service = FakeSshConnectionService(SshConnectionResult.Connected)
        val viewModel = createViewModel(
            serverId = "server-1",
            resolver = FakeConnectionTargetResolver(
                ConnectionTargetResolution.Invalid(ConnectionTargetInvalidReason.MissingUsername),
            ),
            service = service,
        )

        viewModel.connect()

        assertEquals(SshConnectionStatus.Failed, viewModel.uiState.value.status)
        assertEquals(
            "The selected server is missing required SSH connection metadata.",
            viewModel.uiState.value.message,
        )
        assertNull(service.lastRequest)
    }

    @Test
    fun `maps timeout result into failed ui state`() = runBlocking {
        val viewModel = createViewModel(
            serverId = "server-1",
            service = FakeSshConnectionService(
                result = SshConnectionResult.Failed(SshConnectionError.ConnectionTimeout),
            ),
        )

        viewModel.connect()

        assertEquals(SshConnectionStatus.Failed, viewModel.uiState.value.status)
        assertEquals("The connection attempt timed out.", viewModel.uiState.value.message)
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
        resolver: FakeConnectionTargetResolver = FakeConnectionTargetResolver(resolvedTarget()),
        service: FakeSshConnectionService = FakeSshConnectionService(SshConnectionResult.Connected),
    ): SshViewModel {
        return SshViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(SshDestination.SERVER_ID_ARGUMENT to serverId),
            ),
            connectionAttemptUseCase = SshConnectionAttemptUseCase(
                connectionTargetResolver = resolver,
                connectionService = service,
                timeoutMillis = 1_000,
            ),
        )
    }

    private fun resolvedTarget(): ConnectionTargetResolution.Resolved {
        return ConnectionTargetResolution.Resolved(
            RemoteConnectionTarget(
                serverId = "server-1",
                host = "example.com",
                port = 2222,
                username = "admin",
            ),
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
