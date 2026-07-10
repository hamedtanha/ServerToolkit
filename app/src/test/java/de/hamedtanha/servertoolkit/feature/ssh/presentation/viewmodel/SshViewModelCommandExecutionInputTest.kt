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
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshCommandExecutionService
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.ConfirmSshHostTrustUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshCommandExecutionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustDecisionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustEvaluator
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshCommandExecutionStatus
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostKeyObservationService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.sshConnectedResult
import de.hamedtanha.servertoolkit.navigation.SshDestination
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SshViewModelCommandExecutionInputTest {

    @Test
    fun `ignores blank command request before session validation`() = runBlocking {
        val commandExecutionService = FakeSshCommandExecutionService()
        val viewModel = createViewModel(
            serverId = "server-1",
            commandExecutionService = commandExecutionService,
        )

        viewModel.onCommandChanged("   ")
        viewModel.executeCommand()

        assertEquals(0, commandExecutionService.executeCallCount)
        assertEquals(SshCommandExecutionStatus.Idle, viewModel.uiState.value.commandExecution.status)
        assertEquals("No command entered", viewModel.uiState.value.commandExecution.statusLabel)
        assertEquals(
            "Enter a command before running it.",
            viewModel.uiState.value.commandExecution.message,
        )
        assertFalse(viewModel.uiState.value.commandExecution.canExecute)
    }

    @Test
    fun `ignores command text changes while command execution is running`() = runBlocking {
        val commandStarted = CompletableDeferred<Unit>()
        val releaseCommand = CompletableDeferred<Unit>()
        val commandExecutionService = FakeSshCommandExecutionService(
            result = SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "ok",
                    stderr = "",
                    exitStatus = 0,
                ),
            ),
            onExecute = { request ->
                assertEquals("uptime", request.command)
                commandStarted.complete(Unit)
                releaseCommand.await()
            },
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            commandExecutionService = commandExecutionService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())
        viewModel.onCommandChanged("uptime")

        val execution = launch {
            viewModel.executeCommand()
        }

        commandStarted.await()

        viewModel.onCommandChanged("whoami")

        assertEquals("uptime", viewModel.uiState.value.commandExecution.command)

        releaseCommand.complete(Unit)
        execution.join()

        assertEquals(1, commandExecutionService.executeCallCount)
        assertEquals("uptime", commandExecutionService.lastRequest?.command)
        assertEquals("uptime", viewModel.uiState.value.commandExecution.command)
        assertEquals(SshCommandExecutionStatus.Completed, viewModel.uiState.value.commandExecution.status)
        assertEquals("ok", viewModel.uiState.value.commandExecution.stdout)
    }

    private class FakeSshCommandExecutionService(
        private val result: SshCommandExecutionResult = SshCommandExecutionResult.Completed(
            SshCommandExecutionOutput(
                stdout = "",
                stderr = "",
                exitStatus = 0,
            ),
        ),
        private val onExecute: suspend (SshCommandRequest) -> Unit = {},
    ) : SshCommandExecutionService {

        var lastRequest: SshCommandRequest? = null
            private set

        var executeCallCount: Int = 0
            private set

        override suspend fun execute(request: SshCommandRequest): SshCommandExecutionResult {
            executeCallCount += 1
            lastRequest = request
            onExecute(request)
            return result
        }
    }

    private fun createViewModel(
        serverId: String,
        commandExecutionService: FakeSshCommandExecutionService,
    ): SshViewModel {
        val hostTrustRepository = FakeSshHostTrustRepository(
            initialTrustedHostKey = trustedHostKey(),
        )
        val hostTrustDecisionUseCase = SshHostTrustDecisionUseCase(
            hostTrustEvaluator = SshHostTrustEvaluator(hostTrustRepository),
        )

        return SshViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(SshDestination.SERVER_ID_ARGUMENT to serverId),
            ),
            connectionAttemptUseCase = SshConnectionAttemptUseCase(
                connectionTargetResolver = FakeConnectionTargetResolver(resolvedTarget()),
                connectionService = FakeSshConnectionService(sshConnectedResult()),
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
            commandExecutionUseCase = SshCommandExecutionUseCase(commandExecutionService),
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
