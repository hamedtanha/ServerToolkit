package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetInvalidReason
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationMethod
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionAttemptOutcome
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshCommandExecutionService
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.ConfirmSshHostTrustUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshCommandExecutionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustDecisionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustEvaluator
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshCommandExecutionStatus
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionStatus
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostKeyObservationService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.TrackingSshPrivateKeySource
import de.hamedtanha.servertoolkit.navigation.SshDestination
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import de.hamedtanha.servertoolkit.feature.ssh.test.sshConnectedResult

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
            result = sshConnectedResult(),
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
    fun `ignores connection attempt while active session is already connected`() = runBlocking {
        val service = FakeSshConnectionService(
            result = sshConnectedResult(),
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            service = service,
        )

        viewModel.connect()
        viewModel.connect()

        assertEquals(1, service.connectCallCount)
        assertEquals(SshConnectionStatus.Connected, viewModel.uiState.value.status)
    }

    @Test
    fun `uses resolved target metadata for connection request`() = runBlocking {
        val service = FakeSshConnectionService(sshConnectedResult())
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
        val service = FakeSshConnectionService(sshConnectedResult())
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
        val service = FakeSshConnectionService(sshConnectedResult())
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
            result = sshConnectedResult(),
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
    fun `executes command through active connected session`() = runBlocking {
        val commandExecutionService = FakeSshCommandExecutionService(
            result = SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "ok",
                    stderr = "",
                    exitStatus = 0,
                ),
            ),
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            commandExecutionService = commandExecutionService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())
        viewModel.onCommandChanged("uptime")
        viewModel.executeCommand()

        assertEquals(1, commandExecutionService.executeCallCount)
        assertEquals("uptime", commandExecutionService.lastRequest?.command)
        assertEquals(SshCommandExecutionStatus.Completed, viewModel.uiState.value.commandExecution.status)
        assertEquals("ok", viewModel.uiState.value.commandExecution.stdout)
        assertEquals(0, viewModel.uiState.value.commandExecution.exitStatus)
    }

    @Test
    fun `clears previous command output when connection fails`() = runBlocking {
        val commandExecutionService = FakeSshCommandExecutionService(
            result = SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "old output",
                    stderr = "",
                    exitStatus = 0,
                ),
            ),
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            commandExecutionService = commandExecutionService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())
        viewModel.onCommandChanged("uptime")
        viewModel.executeCommand()

        assertEquals("old output", viewModel.uiState.value.commandExecution.stdout)

        viewModel.onConnectionResultReceived(
            SshConnectionResult.Failed(SshConnectionError.AuthenticationRequired),
        )

        assertEquals("uptime", viewModel.uiState.value.commandExecution.command)
        assertEquals(SshCommandExecutionStatus.Idle, viewModel.uiState.value.commandExecution.status)
        assertEquals("", viewModel.uiState.value.commandExecution.stdout)
        assertEquals("", viewModel.uiState.value.commandExecution.stderr)
        assertNull(viewModel.uiState.value.commandExecution.exitStatus)
        assertFalse(viewModel.uiState.value.commandExecution.hasOutput)
    }

    @Test
    fun `keeps command output when reconnect attempt is ignored while active session is connected`() = runBlocking {
        val commandExecutionService = FakeSshCommandExecutionService(
            result = SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "old output",
                    stderr = "",
                    exitStatus = 0,
                ),
            ),
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            commandExecutionService = commandExecutionService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())
        viewModel.onCommandChanged("uptime")
        viewModel.executeCommand()

        assertEquals("old output", viewModel.uiState.value.commandExecution.stdout)

        viewModel.connect()

        assertEquals(SshConnectionStatus.Connected, viewModel.uiState.value.status)
        assertEquals("uptime", viewModel.uiState.value.commandExecution.command)
        assertEquals(SshCommandExecutionStatus.Completed, viewModel.uiState.value.commandExecution.status)
        assertEquals("old output", viewModel.uiState.value.commandExecution.stdout)
        assertEquals("", viewModel.uiState.value.commandExecution.stderr)
        assertEquals(0, viewModel.uiState.value.commandExecution.exitStatus)
        assertTrue(viewModel.uiState.value.commandExecution.hasOutput)
    }

    @Test
    fun `clears previous command output when host key review is required`() = runBlocking {
        val commandExecutionService = FakeSshCommandExecutionService(
            result = SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "old output",
                    stderr = "",
                    exitStatus = 0,
                ),
            ),
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            commandExecutionService = commandExecutionService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())
        viewModel.onCommandChanged("uptime")
        viewModel.executeCommand()

        assertEquals("old output", viewModel.uiState.value.commandExecution.stdout)

        viewModel.onHostTrustDecisionReceived(
            SshHostTrustDecision.ReviewRequired(observedHostKey()),
        )

        assertEquals("uptime", viewModel.uiState.value.commandExecution.command)
        assertEquals(SshCommandExecutionStatus.Idle, viewModel.uiState.value.commandExecution.status)
        assertEquals("", viewModel.uiState.value.commandExecution.stdout)
        assertEquals("", viewModel.uiState.value.commandExecution.stderr)
        assertNull(viewModel.uiState.value.commandExecution.exitStatus)
        assertFalse(viewModel.uiState.value.commandExecution.hasOutput)
    }

    @Test
    fun `maps command execution without active session to failed state`() = runBlocking {
        val commandExecutionService = FakeSshCommandExecutionService()
        val viewModel = createViewModel(
            serverId = "server-1",
            commandExecutionService = commandExecutionService,
        )

        viewModel.onCommandChanged("uptime")
        viewModel.executeCommand()

        assertEquals(0, commandExecutionService.executeCallCount)
        assertEquals(SshCommandExecutionStatus.Failed, viewModel.uiState.value.commandExecution.status)
        assertEquals(
            "No active SSH session was found.",
            viewModel.uiState.value.commandExecution.message,
        )
    }

    @Test
    fun `clears active command session handle when connection fails`() = runBlocking {
        val commandExecutionService = FakeSshCommandExecutionService()
        val viewModel = createViewModel(
            serverId = "server-1",
            commandExecutionService = commandExecutionService,
        )

        viewModel.onConnectionResultReceived(sshConnectedResult())
        viewModel.onConnectionResultReceived(
            SshConnectionResult.Failed(SshConnectionError.AuthenticationRequired),
        )
        viewModel.onCommandChanged("uptime")
        viewModel.executeCommand()

        assertEquals(0, commandExecutionService.executeCallCount)
        assertEquals(SshCommandExecutionStatus.Failed, viewModel.uiState.value.commandExecution.status)
        assertEquals(
            "No active SSH session was found.",
            viewModel.uiState.value.commandExecution.message,
        )
    }

    @Test
    fun `ignores duplicate command execution while one command is running`() = runBlocking {
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
            onExecute = {
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

        val firstExecution = launch {
            viewModel.executeCommand()
        }

        commandStarted.await()

        val duplicateExecution = launch {
            viewModel.executeCommand()
        }

        duplicateExecution.join()

        assertEquals(1, commandExecutionService.executeCallCount)

        releaseCommand.complete(Unit)
        firstExecution.join()

        assertEquals(SshCommandExecutionStatus.Completed, viewModel.uiState.value.commandExecution.status)
    }

    @Test
    fun `ignores command result when session is invalidated before command completes`() = runBlocking {
        val commandStarted = CompletableDeferred<Unit>()
        val releaseCommand = CompletableDeferred<Unit>()
        val commandExecutionService = FakeSshCommandExecutionService(
            result = SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "stale output",
                    stderr = "",
                    exitStatus = 0,
                ),
            ),
            onExecute = {
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
        assertEquals("Authentication required", viewModel.uiState.value.statusLabel)
        assertEquals("Enter valid SSH authentication details before connecting.", viewModel.uiState.value.message)
    }

    @Test
    fun `maps connection attempt host trust outcome into review state`() {
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onConnectionAttemptOutcomeReceived(
            SshConnectionAttemptOutcome.HostTrustDecisionRequired(
                SshHostTrustDecision.ReviewRequired(observedHostKey()),
            ),
        )

        assertEquals("Server identity review required", viewModel.uiState.value.statusLabel)
        assertEquals("example.com", viewModel.uiState.value.hostKeyReview?.host)
    }

    @Test
    fun `maps host key review required decision into ui state`() {
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onHostTrustDecisionReceived(
            SshHostTrustDecision.ReviewRequired(observedHostKey()),
        )

        assertEquals(SshConnectionStatus.Failed, viewModel.uiState.value.status)
        assertEquals("Server identity review required", viewModel.uiState.value.statusLabel)
        assertEquals("example.com", viewModel.uiState.value.hostKeyReview?.host)
        assertEquals("SHA256:abc123", viewModel.uiState.value.hostKeyReview?.displayFingerprint)
    }

    @Test
    fun `confirm pending host key trusts reviewed identity`() = runBlocking {
        val repository = FakeSshHostTrustRepository()
        val viewModel = createViewModel(
            serverId = "server-1",
            hostTrustRepository = repository,
        )

        viewModel.onHostTrustDecisionReceived(
            SshHostTrustDecision.ReviewRequired(observedHostKey()),
        )
        viewModel.confirmPendingHostKey()

        assertEquals("Server identity trusted", viewModel.uiState.value.statusLabel)
        assertNull(viewModel.uiState.value.hostKeyReview)
        assertEquals(1, repository.saveCallCount)
        assertEquals(trustedHostKey(), repository.trustedHostKey)
    }

    @Test
    fun `confirm pending host key is ignored when no review is pending`() = runBlocking {
        val repository = FakeSshHostTrustRepository()
        val viewModel = createViewModel(
            serverId = "server-1",
            hostTrustRepository = repository,
        )

        viewModel.confirmPendingHostKey()

        assertEquals(0, repository.saveCallCount)
        assertNull(viewModel.uiState.value.hostKeyReview)
    }

    @Test
    fun `cancel host key review clears pending review state`() {
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onHostTrustDecisionReceived(
            SshHostTrustDecision.ReviewRequired(observedHostKey()),
        )
        viewModel.onCancelHostKeyReviewClicked()

        assertEquals(SshConnectionStatus.Failed, viewModel.uiState.value.status)
        assertEquals("Server identity review cancelled", viewModel.uiState.value.statusLabel)
        assertEquals("Server identity review was cancelled.", viewModel.uiState.value.message)
        assertNull(viewModel.uiState.value.hostKeyReview)
    }

    @Test
    fun `changed host key decision does not create confirmable pending review`() = runBlocking {
        val repository = FakeSshHostTrustRepository(
            initialTrustedHostKey = trustedHostKey(
                fingerprint = fingerprint(value = "trusted-fingerprint"),
            ),
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            hostTrustRepository = repository,
        )

        viewModel.onHostTrustDecisionReceived(
            SshHostTrustDecision.BlockedChangedHostKey(
                trustedHostKey = trustedHostKey(
                    fingerprint = fingerprint(value = "trusted-fingerprint"),
                ),
                observedHostKey = observedHostKey(
                    fingerprint = fingerprint(value = "observed-fingerprint"),
                ),
            ),
        )
        viewModel.confirmPendingHostKey()

        assertEquals("Server identity changed", viewModel.uiState.value.statusLabel)
        assertEquals(0, repository.saveCallCount)
        assertEquals(
            "trusted-fingerprint",
            repository.trustedHostKey?.fingerprint?.value,
        )
    }

    @Test
    fun `password input updates presence flag without exposing secret in ui state`() {
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPasswordChanged("secret-password")

        assertEquals(
            SshAuthenticationMethod.PASSWORD,
            viewModel.uiState.value.authenticationInput.selectedMethod,
        )
        assertTrue(viewModel.uiState.value.authenticationInput.hasPasswordInput)
        assertFalse(viewModel.uiState.value.authenticationInput.hasPrivateKeySource)
        assertFalse(viewModel.uiState.value.authenticationInput.hasPrivateKeyPassphraseInput)
        assertFalse(viewModel.uiState.value.toString().contains("secret-password"))
    }

    @Test
    fun `private key passphrase input updates presence flag without exposing secret in ui state`() {
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPrivateKeyPassphraseChanged("secret-passphrase")

        assertEquals(
            SshAuthenticationMethod.PRIVATE_KEY,
            viewModel.uiState.value.authenticationInput.selectedMethod,
        )
        assertFalse(viewModel.uiState.value.authenticationInput.hasPasswordInput)
        assertFalse(viewModel.uiState.value.authenticationInput.hasPrivateKeySource)
        assertTrue(viewModel.uiState.value.authenticationInput.hasPrivateKeyPassphraseInput)
        assertFalse(viewModel.uiState.value.toString().contains("secret-passphrase"))
    }

    @Test
    fun `selecting authentication method clears sensitive input flags`() {
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPasswordChanged("secret-password")
        viewModel.onAuthenticationMethodSelected(SshAuthenticationMethod.PRIVATE_KEY)

        assertEquals(
            SshAuthenticationMethod.PRIVATE_KEY,
            viewModel.uiState.value.authenticationInput.selectedMethod,
        )
        assertFalse(viewModel.uiState.value.authenticationInput.hasPasswordInput)
        assertFalse(viewModel.uiState.value.authenticationInput.hasPrivateKeySource)
        assertFalse(viewModel.uiState.value.authenticationInput.hasPrivateKeyPassphraseInput)
        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
    }

    @Test
    fun `clears authentication input state`() {
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPasswordChanged("secret-password")
        viewModel.onAuthenticationInputCleared()

        assertEquals(
            SshAuthenticationMethod.PASSWORD,
            viewModel.uiState.value.authenticationInput.selectedMethod,
        )
        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
    }

    @Test
    fun `connect passes password authentication input through request boundary without exposing secret`() = runBlocking {
        var observedPassword = ""
        val service = FakeSshConnectionService(
            result = sshConnectedResult(),
            onConnect = { request ->
                val passwordInput = request.authenticationInput as SshAuthenticationInput.Password
                observedPassword = passwordInput.password
                assertFalse(request.toString().contains("secret-password"))
            },
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            service = service,
        )

        viewModel.onPasswordChanged("secret-password")
        viewModel.connect()

        assertEquals("secret-password", observedPassword)
        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
        assertFalse(viewModel.uiState.value.toString().contains("secret-password"))
        assertFalse(service.lastRequest.toString().contains("secret-password"))
    }

    @Test
    fun `connect clears authentication input state when cancelled`() = runBlocking {
        val service = FakeSshConnectionService(
            result = sshConnectedResult(),
            onConnect = {
                throw CancellationException("Connection cancelled")
            },
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            service = service,
        )

        viewModel.onPasswordChanged("secret-password")

        try {
            viewModel.connect()
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("Connection cancelled", error.message)
        }

        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
        assertFalse(viewModel.uiState.value.toString().contains("secret-password"))
    }

    @Test
    fun `connection result clears authentication input state`() {
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPasswordChanged("secret-password")
        viewModel.onConnectionResultReceived(sshConnectedResult())

        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
        assertFalse(viewModel.uiState.value.toString().contains("secret-password"))
    }

    @Test
    fun `private key selection exposes only a non-sensitive presence flag`() {
        val source = TrackingSshPrivateKeySource()
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPrivateKeySourceSelected(source)

        assertEquals(
            SshAuthenticationMethod.PRIVATE_KEY,
            viewModel.uiState.value.authenticationInput.selectedMethod,
        )
        assertTrue(viewModel.uiState.value.authenticationInput.hasPrivateKeySource)
        assertEquals(0, source.invalidateCallCount)
        assertFalse(viewModel.uiState.value.toString().contains(source.toString()))
    }

    @Test
    fun `replacing private key invalidates the previous pending source`() {
        val firstSource = TrackingSshPrivateKeySource()
        val secondSource = TrackingSshPrivateKeySource()
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPrivateKeySourceSelected(firstSource)
        viewModel.onPrivateKeySourceSelected(secondSource)

        assertEquals(1, firstSource.invalidateCallCount)
        assertEquals(0, secondSource.invalidateCallCount)
        assertTrue(viewModel.uiState.value.authenticationInput.hasPrivateKeySource)
    }

    @Test
    fun `cancelling private key selection invalidates pending source`() {
        val source = TrackingSshPrivateKeySource()
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPrivateKeySourceSelected(source)
        viewModel.onPrivateKeySelectionCancelled()

        assertEquals(1, source.invalidateCallCount)
        assertEquals(
            SshAuthenticationMethod.PRIVATE_KEY,
            viewModel.uiState.value.authenticationInput.selectedMethod,
        )
        assertFalse(viewModel.uiState.value.authenticationInput.hasPrivateKeySource)
        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
    }

    @Test
    fun `switching to password invalidates pending private key source`() {
        val source = TrackingSshPrivateKeySource()
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPrivateKeySourceSelected(source)
        viewModel.onAuthenticationMethodSelected(SshAuthenticationMethod.PASSWORD)

        assertEquals(1, source.invalidateCallCount)
        assertEquals(
            SshAuthenticationMethod.PASSWORD,
            viewModel.uiState.value.authenticationInput.selectedMethod,
        )
        assertFalse(viewModel.uiState.value.authenticationInput.hasPrivateKeySource)
    }

    @Test
    fun `workflow exit invalidates pending private key source`() {
        val source = TrackingSshPrivateKeySource()
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPrivateKeySourceSelected(source)
        viewModel.onWorkflowExit()

        assertEquals(1, source.invalidateCallCount)
        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
    }

    @Test
    fun `host key review invalidates pending private key source`() {
        val source = TrackingSshPrivateKeySource()
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPrivateKeySourceSelected(source)
        viewModel.onHostTrustDecisionReceived(
            SshHostTrustDecision.ReviewRequired(observedHostKey()),
        )

        assertEquals(1, source.invalidateCallCount)
        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
    }

    @Test
    fun `connect transfers private key source exactly once`() = runBlocking {
        val source = TrackingSshPrivateKeySource()
        val service = FakeSshConnectionService(
            result = sshConnectedResult(),
            onConnect = { request ->
                val input = request.authenticationInput as SshAuthenticationInput.PrivateKey
                assertTrue(input.hasPrivateKeySource)
                val transferredSource = input.takePrivateKeySource()
                assertTrue(transferredSource === source)
                assertNull(input.takePrivateKeySource())
                transferredSource?.invalidate()
            },
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            service = service,
        )

        viewModel.onPrivateKeySourceSelected(source)
        viewModel.onPrivateKeyPassphraseChanged("secret-passphrase")
        viewModel.connect()

        assertEquals(1, source.invalidateCallCount)
        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
    }

    @Test
    fun `connection cancellation invalidates transferred private key source`() = runBlocking {
        val source = TrackingSshPrivateKeySource()
        val service = FakeSshConnectionService(
            result = sshConnectedResult(),
            onConnect = {
                throw CancellationException("Connection cancelled")
            },
        )
        val viewModel = createViewModel(
            serverId = "server-1",
            service = service,
        )

        viewModel.onPrivateKeySourceSelected(source)
        viewModel.onPrivateKeyPassphraseChanged("secret-passphrase")

        try {
            viewModel.connect()
            fail("Expected CancellationException")
        } catch (error: CancellationException) {
            assertEquals("Connection cancelled", error.message)
        }

        assertEquals(1, source.invalidateCallCount)
        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
    }

    @Test
    fun `authentication ui disposal clears password input`() {
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPasswordChanged("secret-password")
        viewModel.onAuthenticationInputUiDisposed()

        assertEquals(
            SshAuthenticationMethod.PASSWORD,
            viewModel.uiState.value.authenticationInput.selectedMethod,
        )
        assertFalse(viewModel.uiState.value.authenticationInput.hasPasswordInput)
        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
    }

    @Test
    fun `authentication ui disposal clears passphrase without invalidating pending source`() {
        val source = TrackingSshPrivateKeySource()
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onPrivateKeySourceSelected(source)
        viewModel.onPrivateKeyPassphraseChanged("secret-passphrase")
        viewModel.onAuthenticationInputUiDisposed()

        assertEquals(
            SshAuthenticationMethod.PRIVATE_KEY,
            viewModel.uiState.value.authenticationInput.selectedMethod,
        )
        assertTrue(viewModel.uiState.value.authenticationInput.hasPrivateKeySource)
        assertFalse(
            viewModel.uiState.value.authenticationInput.hasPrivateKeyPassphraseInput,
        )
        assertEquals(0, source.invalidateCallCount)
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
        resolver: FakeConnectionTargetResolver = FakeConnectionTargetResolver(resolvedTarget()),
        service: FakeSshConnectionService = FakeSshConnectionService(sshConnectedResult()),
        observationService: FakeSshHostKeyObservationService = FakeSshHostKeyObservationService(
            SshHostKeyObservationResult.Observed(observedHostKey()),
        ),
        hostTrustRepository: FakeSshHostTrustRepository = FakeSshHostTrustRepository(
            initialTrustedHostKey = trustedHostKey(),
        ),
        commandExecutionService: FakeSshCommandExecutionService = FakeSshCommandExecutionService(),
    ): SshViewModel {
        val hostTrustDecisionUseCase = SshHostTrustDecisionUseCase(
            hostTrustEvaluator = SshHostTrustEvaluator(hostTrustRepository),
        )

        return SshViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(SshDestination.SERVER_ID_ARGUMENT to serverId),
            ),
            connectionAttemptUseCase = SshConnectionAttemptUseCase(
                connectionTargetResolver = resolver,
                connectionService = service,
                hostKeyObservationService = observationService,
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

    private fun connectionRequest(): SshConnectionRequest {
        return SshConnectionRequest(
            serverId = "server-1",
            host = "example.com",
            port = 22,
            username = "admin",
        )
    }

    private fun endpoint(): SshHostEndpoint {
        return SshHostEndpoint(
            serverId = "server-1",
            host = "example.com",
            port = 22,
        )
    }

    private fun fingerprint(
        value: String = "abc123",
    ): SshHostKeyFingerprint {
        return SshHostKeyFingerprint(
            algorithm = "SHA256",
            value = value,
        )
    }

    private fun observedHostKey(
        fingerprint: SshHostKeyFingerprint = fingerprint(),
    ): SshObservedHostKey {
        return SshObservedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint,
        )
    }

    private fun trustedHostKey(
        fingerprint: SshHostKeyFingerprint = fingerprint(),
    ): SshTrustedHostKey {
        return SshTrustedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint,
        )
    }
}
