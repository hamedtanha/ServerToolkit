package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshCommandExecutionService
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshSessionLifecycleService
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.ConfirmSshHostTrustUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshCommandExecutionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustDecisionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustEvaluator
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionStatus
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostKeyObservationService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshSessionLifecycleService
import de.hamedtanha.servertoolkit.feature.ssh.test.sshConnectedResult
import de.hamedtanha.servertoolkit.navigation.SshDestination
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SshViewModelSessionCloseTest {

    @Test
    fun `workflow exit without active session succeeds without requesting close`() = runBlocking {
        val lifecycleService = FakeSshSessionLifecycleService()
        val viewModel = createViewModel(
            sessionLifecycleService = lifecycleService,
        )

        val canExit = viewModel.onWorkflowExit()

        assertTrue(canExit)
        assertEquals(0, lifecycleService.closeCallCount)
        assertEquals(SshConnectionStatus.NotStarted, viewModel.uiState.value.status)
    }

    @Test
    fun `workflow exit closes active session before allowing navigation`() = runBlocking {
        val lifecycleService = FakeSshSessionLifecycleService(
            result = SshSessionCloseResult.Closed,
        )
        val viewModel = createViewModel(
            sessionLifecycleService = lifecycleService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())

        val canExit = viewModel.onWorkflowExit()

        assertTrue(canExit)
        assertEquals(1, lifecycleService.closeCallCount)
        assertTrue(lifecycleService.lastSessionHandle != null)
        assertEquals(SshConnectionStatus.NotStarted, viewModel.uiState.value.status)
        assertEquals("Not connected", viewModel.uiState.value.statusLabel)
        assertEquals("SSH session closed.", viewModel.uiState.value.message)
    }

    @Test
    fun `workflow exit treats missing session owner as already closed`() = runBlocking {
        val lifecycleService = FakeSshSessionLifecycleService(
            result = SshSessionCloseResult.NotFound,
        )
        val viewModel = createViewModel(
            sessionLifecycleService = lifecycleService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())

        val canExit = viewModel.onWorkflowExit()

        assertTrue(canExit)
        assertEquals(1, lifecycleService.closeCallCount)
        assertEquals(SshConnectionStatus.NotStarted, viewModel.uiState.value.status)
        assertEquals("Not connected", viewModel.uiState.value.statusLabel)
        assertEquals(
            "SSH session was already closed.",
            viewModel.uiState.value.message,
        )
    }

    @Test
    fun `workflow exit waits while connection attempt is running`() = runBlocking {
        val connectionStarted = CompletableDeferred<Unit>()
        val releaseConnection = CompletableDeferred<Unit>()
        val lifecycleService = FakeSshSessionLifecycleService()
        val connectionService = FakeSshConnectionService(
            result = sshConnectedResult(),
            onConnect = {
                connectionStarted.complete(Unit)
                releaseConnection.await()
            },
        )
        val viewModel = createViewModel(
            sessionLifecycleService = lifecycleService,
            connectionService = connectionService,
        )

        val connectionAttempt = launch {
            viewModel.connect()
        }

        connectionStarted.await()

        val canExit = viewModel.onWorkflowExit()

        assertFalse(canExit)
        assertEquals(0, lifecycleService.closeCallCount)
        assertEquals(SshConnectionStatus.Connecting, viewModel.uiState.value.status)
        assertEquals(
            "Connection attempt is still running.",
            viewModel.uiState.value.message,
        )

        releaseConnection.complete(Unit)
        connectionAttempt.join()
    }

    @Test
    fun `successful workflow exit clears previous command output`() = runBlocking {
        val lifecycleService = FakeSshSessionLifecycleService(
            result = SshSessionCloseResult.Closed,
        )
        val commandExecutionService = FakeSshCommandExecutionService(
            result = SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "server output",
                    stderr = "server warning",
                    exitStatus = 0,
                ),
            ),
        )
        val viewModel = createViewModel(
            sessionLifecycleService = lifecycleService,
            commandExecutionService = commandExecutionService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())
        viewModel.onCommandChanged("uptime")
        viewModel.executeCommand()

        assertTrue(viewModel.uiState.value.commandExecution.hasOutput)

        val canExit = viewModel.onWorkflowExit()

        assertTrue(canExit)
        assertEquals(1, lifecycleService.closeCallCount)
        assertFalse(viewModel.uiState.value.commandExecution.hasOutput)
        assertEquals("", viewModel.uiState.value.commandExecution.stdout)
        assertEquals("", viewModel.uiState.value.commandExecution.stderr)
        assertEquals(
            SshConnectionStatus.NotStarted,
            viewModel.uiState.value.status,
        )
    }

    @Test
    fun `failed close keeps session active and supports cleanup retry`() = runBlocking {
        val lifecycleService = FakeSshSessionLifecycleService(
            result = SshSessionCloseResult.Failed,
        )
        val viewModel = createViewModel(
            sessionLifecycleService = lifecycleService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())
        viewModel.onCommandChanged("uptime")

        val firstExitResult = viewModel.onWorkflowExit()

        assertFalse(firstExitResult)
        assertEquals(1, lifecycleService.closeCallCount)
        assertEquals(SshConnectionStatus.Connected, viewModel.uiState.value.status)
        assertEquals("Connected", viewModel.uiState.value.statusLabel)
        assertEquals("uptime", viewModel.uiState.value.commandExecution.command)
        assertEquals(
            "SSH session could not be closed.",
            viewModel.uiState.value.message,
        )

        lifecycleService.result = SshSessionCloseResult.Closed

        val retryExitResult = viewModel.onWorkflowExit()

        assertTrue(retryExitResult)
        assertEquals(2, lifecycleService.closeCallCount)
        assertEquals(SshConnectionStatus.NotStarted, viewModel.uiState.value.status)
    }

    @Test
    fun `duplicate workflow exit does not start a second close operation`() = runBlocking {
        val closeStarted = CompletableDeferred<Unit>()
        val releaseClose = CompletableDeferred<Unit>()
        val lifecycleService = FakeSshSessionLifecycleService(
            onClose = {
                closeStarted.complete(Unit)
                releaseClose.await()
            },
        )
        val viewModel = createViewModel(
            sessionLifecycleService = lifecycleService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())

        val firstExit = async {
            viewModel.onWorkflowExit()
        }

        closeStarted.await()

        assertEquals(SshConnectionStatus.Disconnecting, viewModel.uiState.value.status)
        assertEquals("Disconnecting", viewModel.uiState.value.statusLabel)
        assertFalse(viewModel.uiState.value.canEditAuthenticationInput)
        assertFalse(viewModel.uiState.value.canStartConnection)

        val duplicateExitResult = viewModel.onWorkflowExit()

        assertFalse(duplicateExitResult)
        assertEquals(1, lifecycleService.closeCallCount)

        releaseClose.complete(Unit)

        assertTrue(firstExit.await())
        assertEquals(1, lifecycleService.closeCallCount)
        assertEquals(SshConnectionStatus.NotStarted, viewModel.uiState.value.status)
    }

    @Test
    fun `workflow exit waits while command execution is running`() = runBlocking {
        val commandStarted = CompletableDeferred<Unit>()
        val releaseCommand = CompletableDeferred<Unit>()
        val lifecycleService = FakeSshSessionLifecycleService()
        val commandExecutionService = FakeSshCommandExecutionService(
            onExecute = {
                commandStarted.complete(Unit)
                releaseCommand.await()
            },
        )
        val viewModel = createViewModel(
            sessionLifecycleService = lifecycleService,
            commandExecutionService = commandExecutionService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())
        viewModel.onCommandChanged("uptime")

        val commandExecution = launch {
            viewModel.executeCommand()
        }

        commandStarted.await()

        val canExit = viewModel.onWorkflowExit()

        assertFalse(canExit)
        assertEquals(0, lifecycleService.closeCallCount)
        assertEquals(SshConnectionStatus.Connected, viewModel.uiState.value.status)
        assertEquals(
            "Command execution is still running.",
            viewModel.uiState.value.message,
        )

        releaseCommand.complete(Unit)
        commandExecution.join()
    }

    @Test
    fun `cancelled close restores active session state and propagates cancellation`() = runBlocking {
        val lifecycleService = FakeSshSessionLifecycleService(
            onClose = {
                throw CancellationException("Session close cancelled")
            },
        )
        val viewModel = createViewModel(
            sessionLifecycleService = lifecycleService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())
        viewModel.onCommandChanged("uptime")

        try {
            viewModel.onWorkflowExit()
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("Session close cancelled", error.message)
        }

        assertEquals(1, lifecycleService.closeCallCount)
        assertEquals(SshConnectionStatus.Connected, viewModel.uiState.value.status)
        assertEquals("Connected", viewModel.uiState.value.statusLabel)
        assertEquals("uptime", viewModel.uiState.value.commandExecution.command)
        assertEquals(
            "SSH session cleanup was cancelled.",
            viewModel.uiState.value.message,
        )
    }

    @Test
    fun `explicit disconnect closes active session and allows reconnection`() = runBlocking {
        val lifecycleService = FakeSshSessionLifecycleService(
            result = SshSessionCloseResult.Closed,
        )
        val viewModel = createViewModel(
            sessionLifecycleService = lifecycleService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())

        viewModel.disconnect()

        assertEquals(1, lifecycleService.closeCallCount)
        assertEquals(SshConnectionStatus.NotStarted, viewModel.uiState.value.status)
        assertEquals("Not connected", viewModel.uiState.value.statusLabel)
        assertEquals("SSH session disconnected.", viewModel.uiState.value.message)
        assertTrue(viewModel.uiState.value.canStartConnection)
        assertFalse(viewModel.uiState.value.canDisconnect)
    }

    @Test
    fun `failed explicit disconnect preserves active session and supports retry`() = runBlocking {
        val lifecycleService = FakeSshSessionLifecycleService(
            result = SshSessionCloseResult.Failed,
        )
        val viewModel = createViewModel(
            sessionLifecycleService = lifecycleService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())
        viewModel.onCommandChanged("uptime")

        viewModel.disconnect()

        assertEquals(1, lifecycleService.closeCallCount)
        assertEquals(SshConnectionStatus.Connected, viewModel.uiState.value.status)
        assertEquals("Connected", viewModel.uiState.value.statusLabel)
        assertEquals(
            "SSH session could not be disconnected.",
            viewModel.uiState.value.message,
        )
        assertEquals("uptime", viewModel.uiState.value.commandExecution.command)
        assertTrue(viewModel.uiState.value.canDisconnect)

        lifecycleService.result = SshSessionCloseResult.Closed

        viewModel.disconnect()

        assertEquals(2, lifecycleService.closeCallCount)
        assertEquals(SshConnectionStatus.NotStarted, viewModel.uiState.value.status)
        assertFalse(viewModel.uiState.value.canDisconnect)
    }

    @Test
    fun `duplicate explicit disconnect does not start a second close operation`() = runBlocking {
        val closeStarted = CompletableDeferred<Unit>()
        val releaseClose = CompletableDeferred<Unit>()
        val lifecycleService = FakeSshSessionLifecycleService(
            onClose = {
                closeStarted.complete(Unit)
                releaseClose.await()
            },
        )
        val viewModel = createViewModel(
            sessionLifecycleService = lifecycleService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())

        val firstDisconnect = async {
            viewModel.disconnect()
        }

        closeStarted.await()

        assertEquals(SshConnectionStatus.Disconnecting, viewModel.uiState.value.status)
        assertFalse(viewModel.uiState.value.canDisconnect)

        viewModel.disconnect()

        assertEquals(1, lifecycleService.closeCallCount)

        releaseClose.complete(Unit)
        firstDisconnect.await()

        assertEquals(1, lifecycleService.closeCallCount)
        assertEquals(SshConnectionStatus.NotStarted, viewModel.uiState.value.status)
    }

    private fun createViewModel(
        sessionLifecycleService: SshSessionLifecycleService,
        commandExecutionService: SshCommandExecutionService =
            FakeSshCommandExecutionService(),
        connectionService: FakeSshConnectionService =
            FakeSshConnectionService(sshConnectedResult()),
    ): SshViewModel {
        val hostTrustRepository = FakeSshHostTrustRepository(
            initialTrustedHostKey = trustedHostKey(),
        )
        val hostTrustDecisionUseCase = SshHostTrustDecisionUseCase(
            hostTrustEvaluator = SshHostTrustEvaluator(hostTrustRepository),
        )

        return SshViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(SshDestination.SERVER_ID_ARGUMENT to "server-1"),
            ),
            connectionAttemptUseCase = SshConnectionAttemptUseCase(
                connectionTargetResolver = FakeConnectionTargetResolver(resolvedTarget()),
                connectionService = connectionService,
                hostKeyObservationService = FakeSshHostKeyObservationService(
                    SshHostKeyObservationResult.Observed(observedHostKey()),
                ),
                hostTrustDecisionUseCase = hostTrustDecisionUseCase,
                connectionHistoryRepository = FakeSshConnectionHistoryRepository(),
                timeoutMillis = 1_000,
            ),
            confirmHostTrustUseCase = ConfirmSshHostTrustUseCase(
                hostTrustDecisionUseCase = hostTrustDecisionUseCase,
                hostTrustRepository = hostTrustRepository,
            ),
            commandExecutionUseCase = SshCommandExecutionUseCase(
                commandExecutionService,
            ),
            sessionLifecycleService = sessionLifecycleService,
        )
    }

    private class FakeSshCommandExecutionService(
        private val result: SshCommandExecutionResult =
            SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "",
                    stderr = "",
                    exitStatus = 0,
                ),
            ),
        private val onExecute: suspend (SshCommandRequest) -> Unit = {},
    ) : SshCommandExecutionService {

        override suspend fun execute(
            request: SshCommandRequest,
        ): SshCommandExecutionResult {
            onExecute(request)
            return result
        }
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

    private fun endpoint(): SshHostEndpoint {
        return SshHostEndpoint(
            serverId = "server-1",
            host = "example.com",
            port = 22,
        )
    }

    private fun fingerprint(): SshHostKeyFingerprint {
        return SshHostKeyFingerprint(
            algorithm = "SHA256",
            value = "abc123",
        )
    }

    private fun observedHostKey(): SshObservedHostKey {
        return SshObservedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint(),
        )
    }

    private fun trustedHostKey(): SshTrustedHostKey {
        return SshTrustedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint(),
        )
    }
}
