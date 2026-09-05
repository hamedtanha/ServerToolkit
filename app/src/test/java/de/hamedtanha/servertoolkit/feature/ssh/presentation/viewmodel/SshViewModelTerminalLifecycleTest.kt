package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.feature.savedcommands.test.FakeSavedCommandRepository
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
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.ConfirmSshHostTrustUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshCommandExecutionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustDecisionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustEvaluator
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostKeyObservationService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshSessionLifecycleService
import de.hamedtanha.servertoolkit.feature.ssh.test.sshConnectedResult
import de.hamedtanha.servertoolkit.navigation.SshDestination
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SshViewModelTerminalLifecycleTest {

    @Test
    fun `permanent owner clearing abandons active session exactly once`() {
        val lifecycleService = FakeSshSessionLifecycleService()
        val viewModel = createViewModel(lifecycleService)
        val owner = store(viewModel)
        val connected = sshConnectedResult()

        viewModel.onConnectionResultReceived(connected)

        owner.viewModelStore.clear()

        assertEquals(0, lifecycleService.closeCallCount)
        assertEquals(1, lifecycleService.abandonCallCount)
        assertEquals(connected.sessionHandle, lifecycleService.lastAbandonedSessionHandle)

        owner.viewModelStore.clear()

        assertEquals(1, lifecycleService.abandonCallCount)
    }

    @Test
    fun `permanent owner clearing without active session does not abandon`() {
        val lifecycleService = FakeSshSessionLifecycleService()
        val viewModel = createViewModel(lifecycleService)
        val owner = store(viewModel)

        owner.viewModelStore.clear()

        assertEquals(0, lifecycleService.closeCallCount)
        assertEquals(0, lifecycleService.abandonCallCount)
    }

    @Test
    fun `successful workflow exit before owner clearing does not abandon again`() = runBlocking {
        val lifecycleService = FakeSshSessionLifecycleService(
            result = SshSessionCloseResult.Closed,
        )
        val viewModel = createViewModel(lifecycleService)
        val owner = store(viewModel)

        viewModel.onConnectionResultReceived(sshConnectedResult())

        assertTrue(viewModel.onWorkflowExit())
        owner.viewModelStore.clear()

        assertEquals(1, lifecycleService.closeCallCount)
        assertEquals(0, lifecycleService.abandonCallCount)
    }

    @Test
    fun `owner clearing during ordinary close waits for close result before abandonment`() = runBlocking {
        val closeStarted = CompletableDeferred<Unit>()
        val releaseClose = CompletableDeferred<Unit>()
        val lifecycleService = FakeSshSessionLifecycleService(
            result = SshSessionCloseResult.Failed,
            onClose = {
                closeStarted.complete(Unit)
                releaseClose.await()
            },
        )
        val viewModel = createViewModel(lifecycleService)
        val owner = store(viewModel)
        val connected = sshConnectedResult()

        viewModel.onConnectionResultReceived(connected)

        val exit = async {
            viewModel.onWorkflowExit()
        }

        closeStarted.await()
        owner.viewModelStore.clear()

        assertEquals(1, lifecycleService.closeCallCount)
        assertEquals(0, lifecycleService.abandonCallCount)

        releaseClose.complete(Unit)

        assertFalse(exit.await())
        assertEquals(1, lifecycleService.closeCallCount)
        assertEquals(1, lifecycleService.abandonCallCount)
        assertEquals(connected.sessionHandle, lifecycleService.lastAbandonedSessionHandle)
    }

    @Test
    fun `owner clearing during command execution transfers active session to abandonment`() = runBlocking {
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
            lifecycleService = lifecycleService,
            commandExecutionService = commandExecutionService,
        )
        val owner = store(viewModel)
        val connected = sshConnectedResult()

        viewModel.onConnectionResultReceived(connected)
        viewModel.onCommandChanged("uptime")

        val command = launch {
            viewModel.executeCommand()
        }

        commandStarted.await()
        owner.viewModelStore.clear()

        assertEquals(1, lifecycleService.abandonCallCount)
        assertEquals(connected.sessionHandle, lifecycleService.lastAbandonedSessionHandle)

        releaseCommand.complete(Unit)
        command.join()
    }

    private fun store(viewModel: SshViewModel): TestViewModelStoreOwner {
        val owner = TestViewModelStoreOwner()
        val provider = ViewModelProvider(
            owner,
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return viewModel as T
                }
            },
        )

        provider[SshViewModel::class.java]
        return owner
    }

    private fun createViewModel(
        lifecycleService: FakeSshSessionLifecycleService,
        commandExecutionService: SshCommandExecutionService = FakeSshCommandExecutionService(),
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
            savedCommandRepository = FakeSavedCommandRepository(),
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
            sessionLifecycleService = lifecycleService,
        )
    }

    private class TestViewModelStoreOwner : ViewModelStoreOwner {
        override val viewModelStore: ViewModelStore = ViewModelStore()
    }

    private class FakeSshCommandExecutionService(
        private val onExecute: suspend (SshCommandRequest) -> Unit = {},
    ) : SshCommandExecutionService {

        override suspend fun execute(request: SshCommandRequest): SshCommandExecutionResult {
            onExecute(request)
            return SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "",
                    stderr = "",
                    exitStatus = 0,
                ),
            )
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
