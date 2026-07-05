package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationMethod
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.ConfirmSshHostTrustUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshAuthenticationInputUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionStatus
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.withConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.withHostTrustDecision
import de.hamedtanha.servertoolkit.navigation.SshDestination
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SshViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val connectionAttemptUseCase: SshConnectionAttemptUseCase,
    private val confirmHostTrustUseCase: ConfirmSshHostTrustUseCase,
) : ViewModel() {

    private val serverId: String = checkNotNull(savedStateHandle[SshDestination.SERVER_ID_ARGUMENT])

    private var isConnectionAttemptInProgress: Boolean = false

    private var pendingObservedHostKey: SshObservedHostKey? = null

    private val pendingAuthenticationSecrets = PendingAuthenticationSecrets()

    private val _uiState = MutableStateFlow(
        SshUiState(serverId = serverId),
    )
    val uiState: StateFlow<SshUiState> = _uiState.asStateFlow()

    fun onConnectClicked() {
        viewModelScope.launch {
            connect()
        }
    }

    fun onConfirmHostKeyClicked() {
        viewModelScope.launch {
            confirmPendingHostKey()
        }
    }

    fun onCancelHostKeyReviewClicked() {
        clearPendingHostKeyReview(
            message = "Server identity review was cancelled.",
            detail = "No SSH session was opened.",
        )
    }

    fun onAuthenticationMethodSelected(method: SshAuthenticationMethod) {
        pendingAuthenticationSecrets.clear()

        _uiState.value = _uiState.value.copy(
            authenticationInput = _uiState.value.authenticationInput.copy(
                selectedMethod = method,
                hasPasswordInput = false,
                hasPrivateKeyPassphraseInput = false,
            ),
        )
    }

    fun onAuthenticationUsernameChanged(username: String) {
        _uiState.value = _uiState.value.copy(
            authenticationInput = _uiState.value.authenticationInput.copy(
                username = username,
            ),
        )
    }

    fun onPasswordChanged(password: String) {
        pendingAuthenticationSecrets.password = password
        pendingAuthenticationSecrets.privateKeyPassphrase = ""

        _uiState.value = _uiState.value.copy(
            authenticationInput = _uiState.value.authenticationInput.copy(
                selectedMethod = SshAuthenticationMethod.PASSWORD,
                hasPasswordInput = password.isNotEmpty(),
                hasPrivateKeyPassphraseInput = false,
            ),
        )
    }

    fun onPrivateKeyPassphraseChanged(passphrase: String) {
        pendingAuthenticationSecrets.password = ""
        pendingAuthenticationSecrets.privateKeyPassphrase = passphrase

        _uiState.value = _uiState.value.copy(
            authenticationInput = _uiState.value.authenticationInput.copy(
                selectedMethod = SshAuthenticationMethod.PRIVATE_KEY,
                hasPasswordInput = false,
                hasPrivateKeyPassphraseInput = passphrase.isNotEmpty(),
            ),
        )
    }

    fun onAuthenticationInputCleared() {
        clearAuthenticationInputState()
    }

    internal suspend fun connect() {
        if (isConnectionAttemptInProgress) {
            return
        }

        isConnectionAttemptInProgress = true

        try {
            _uiState.value = _uiState.value.copy(
                status = SshConnectionStatus.Connecting,
                statusLabel = "Connecting",
                message = "Starting connection attempt.",
                detail = "The connection attempt is contained by timeout, cancellation, and error-mapping boundaries.",
                hostKeyReview = null,
            )

            val authenticationInput = pendingAuthenticationSecrets.toAuthenticationInput(
                method = _uiState.value.authenticationInput.selectedMethod,
            )
            val result = connectionAttemptUseCase(
                serverId = serverId,
                authenticationInput = authenticationInput,
            )
            onConnectionResultReceived(result)
        } finally {
            isConnectionAttemptInProgress = false
        }
    }

    internal fun onConnectionResultReceived(result: SshConnectionResult) {
        pendingObservedHostKey = null
        clearAuthenticationInputState()
        _uiState.value = _uiState.value.withConnectionResult(result)
    }

    internal fun onHostTrustDecisionReceived(decision: SshHostTrustDecision) {
        pendingObservedHostKey = when (decision) {
            is SshHostTrustDecision.ReviewRequired -> decision.observedHostKey
            is SshHostTrustDecision.BlockedChangedHostKey -> null
            is SshHostTrustDecision.Accepted -> null
        }

        _uiState.value = _uiState.value.withHostTrustDecision(decision)
    }

    internal suspend fun confirmPendingHostKey() {
        val observedHostKey = pendingObservedHostKey ?: return

        val decision = confirmHostTrustUseCase(observedHostKey)
        onHostTrustDecisionReceived(decision)
    }

    private fun clearPendingHostKeyReview(
        message: String,
        detail: String,
    ) {
        pendingObservedHostKey = null

        _uiState.value = _uiState.value.copy(
            status = SshConnectionStatus.Failed,
            statusLabel = "Server identity review cancelled",
            message = message,
            detail = detail,
            hostKeyReview = null,
        )
    }

    private fun clearAuthenticationInputState() {
        pendingAuthenticationSecrets.clear()

        _uiState.value = _uiState.value.copy(
            authenticationInput = SshAuthenticationInputUiState(),
        )
    }
}

private class PendingAuthenticationSecrets {

    var password: String = ""

    var privateKeyPassphrase: String = ""

    fun toAuthenticationInput(method: SshAuthenticationMethod): SshAuthenticationInput {
        return when (method) {
            SshAuthenticationMethod.PASSWORD -> SshAuthenticationInput.Password(password)
            SshAuthenticationMethod.PRIVATE_KEY -> SshAuthenticationInput.PrivateKeyPassphrase(
                privateKeyPassphrase,
            )
        }
    }

    fun clear() {
        password = ""
        privateKeyPassphrase = ""
    }
}
