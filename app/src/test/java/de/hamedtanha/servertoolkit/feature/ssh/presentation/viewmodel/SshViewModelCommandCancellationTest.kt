package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
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
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostKeyObservationService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.sshConnectedResult
import de.hamedtanha.servertoolkit.navigation.SshDestination
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class SshViewModelCommandCancellationTest {

    @Test
    fun `ignores command cancellation when session is invalidated before command completes`() = runBlocking {
        val commandStarted = CompletableDeferred<Unit>()
        val releaseCommand = CompletableDeferred<Unit>()
        val commandExecutionService = CancellingSshCommandExecutionService(
            onExecute = {
                commandStarted.complete(Unit)
                releaseCommand.await()
            },
        )
        val viewModel = createViewModel(commandExecutionService)

        viewModel.onConnectionResultReceived(sshConnectedResult())
        viewModel.onCommandChanged("uptime")

        val execution = launch {
            try {
                viewModel.executeCommand()
                fail("Expected CancellationException")
            } catch (error: CancellationException) {
                assertEquals("Command cancelled", error.message)
            }
        }

        commandStarted.await()
        viewModel.onConnectionResultReceived(
            SshConnectionResult.Failed(SshConnectionError.AuthenticationRequired),
        )

        releaseCommand.complete(Unit)
        execution.join()

        assertEquals(1, commandExecutionService.executeCallCount)
        assertEquals(SshCommandExecutionStatus.Idle, viewModel.uiState.value.commandExecution.status)
        assertEquals("", viewModel.uiState.value.commandExecution.stdout)
        assertEquals("", viewModel.uiState.value.commandExecution.stderr)
        assertNull(viewModel.uiState.value.commandExecution.exitStatus)
        assertFalse(viewModel.uiState.value.commandExecution.hasOutput)
    }

    private class CancellingSshCommandExecutionService(
        private val onExecute: suspend (SshCommandRequest) -> Unit,
    ) : SshCommandExecutionService {

        var executeCallCount: Int = 0
            private set

        override suspend fun execute(request: SshCommandRequest): SshCommandExecutionResult {
            executeCallCount += 1
            onExecute(request)
            throw CancellationException("Command cancelled")
        }
    }

    private fun createViewModel(
        commandExecutionService: SshCommandExecutionService,
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
                connectionService = FakeSshConnectionService(sshConnectedResult()),
                hostKeyObservationService = FakeSshHostKeyObservationService(
                    SshHostKeyObservationResult.Observed(observedHostKey()),
                ),
                hostTrustDecisionUseCase = hostTrustDecisionUseCase,
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
