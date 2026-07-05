package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetInvalidReason
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationMethod
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.ConfirmSshHostTrustUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustDecisionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustEvaluator
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionStatus
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
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
    fun `updates authentication username without sensitive input`() {
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onAuthenticationUsernameChanged("admin")

        assertEquals("admin", viewModel.uiState.value.authenticationInput.username)
        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
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
        assertFalse(viewModel.uiState.value.authenticationInput.hasPrivateKeyPassphraseInput)
        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
    }

    @Test
    fun `clears authentication input state`() {
        val viewModel = createViewModel(serverId = "server-1")

        viewModel.onAuthenticationUsernameChanged("admin")
        viewModel.onPasswordChanged("secret-password")
        viewModel.onAuthenticationInputCleared()

        assertEquals("", viewModel.uiState.value.authenticationInput.username)
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
            result = SshConnectionResult.Connected,
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
            result = SshConnectionResult.Connected,
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
        viewModel.onConnectionResultReceived(SshConnectionResult.Connected)

        assertFalse(viewModel.uiState.value.authenticationInput.hasSensitiveInput)
        assertFalse(viewModel.uiState.value.toString().contains("secret-password"))
    }

    private fun createViewModel(
        serverId: String,
        resolver: FakeConnectionTargetResolver = FakeConnectionTargetResolver(resolvedTarget()),
        service: FakeSshConnectionService = FakeSshConnectionService(SshConnectionResult.Connected),
        hostTrustRepository: FakeSshHostTrustRepository = FakeSshHostTrustRepository(),
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
                timeoutMillis = 1_000,
            ),
            confirmHostTrustUseCase = ConfirmSshHostTrustUseCase(
                hostTrustDecisionUseCase = hostTrustDecisionUseCase,
                hostTrustRepository = hostTrustRepository,
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
