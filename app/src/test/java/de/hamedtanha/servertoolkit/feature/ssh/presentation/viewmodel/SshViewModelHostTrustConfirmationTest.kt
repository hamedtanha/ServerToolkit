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
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshCommandExecutionService
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.ConfirmSshHostTrustUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshCommandExecutionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustDecisionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustEvaluator
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostKeyObservationService
import de.hamedtanha.servertoolkit.feature.ssh.test.sshConnectedResult
import de.hamedtanha.servertoolkit.navigation.SshDestination
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SshViewModelHostTrustConfirmationTest {

    @Test
    fun `ignores duplicate host key confirmation while confirmation is running`() = runBlocking {
        val repository = DelayingSshHostTrustRepository()
        val viewModel = createViewModel(repository)

        viewModel.onHostTrustDecisionReceived(
            SshHostTrustDecision.ReviewRequired(observedHostKey()),
        )

        val firstConfirmation = launch {
            viewModel.confirmPendingHostKey()
        }

        repository.saveStarted.await()

        val duplicateConfirmation = launch {
            viewModel.confirmPendingHostKey()
        }

        duplicateConfirmation.join()

        repository.releaseSave.complete(Unit)
        firstConfirmation.join()

        assertEquals(1, repository.saveCallCount)
        assertEquals(trustedHostKey(), repository.trustedHostKey)
        assertNull(viewModel.uiState.value.hostKeyReview)
    }

    private class DelayingSshHostTrustRepository : SshHostTrustRepository {

        val saveStarted = CompletableDeferred<Unit>()

        val releaseSave = CompletableDeferred<Unit>()

        var trustedHostKey: SshTrustedHostKey? = null
            private set

        var saveCallCount: Int = 0
            private set

        private var saveInProgress: Boolean = false

        override suspend fun getTrustedHostKey(
            endpoint: SshHostEndpoint,
        ): SshTrustedHostKey? {
            return trustedHostKey?.takeIf { trustedKey ->
                trustedKey.endpoint == endpoint
            }
        }

        override suspend fun saveTrustedHostKey(
            trustedHostKey: SshTrustedHostKey,
        ) {
            if (saveInProgress) {
                saveCallCount += 1
                return
            }

            saveInProgress = true
            saveCallCount += 1
            saveStarted.complete(Unit)

            try {
                releaseSave.await()
                this.trustedHostKey = trustedHostKey
            } finally {
                saveInProgress = false
            }
        }

        override suspend fun removeTrustedHostKey(
            endpoint: SshHostEndpoint,
        ) = Unit
    }

    private class NoOpSshCommandExecutionService : SshCommandExecutionService {

        override suspend fun execute(request: SshCommandRequest): SshCommandExecutionResult {
            return SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "",
                    stderr = "",
                    exitStatus = 0,
                ),
            )
        }
    }

    private fun createViewModel(
        hostTrustRepository: SshHostTrustRepository,
    ): SshViewModel {
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
            commandExecutionUseCase = SshCommandExecutionUseCase(
                NoOpSshCommandExecutionService(),
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
