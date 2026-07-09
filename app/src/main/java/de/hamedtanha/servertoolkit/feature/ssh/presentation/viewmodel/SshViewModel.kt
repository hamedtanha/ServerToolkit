package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationMethod
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionAttemptOutcome
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.ConfirmSshHostTrustUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshCommandExecutionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshAuthenticationInputUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionStatus
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.asRunning
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.asSessionUnavailable
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.withCommandText
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.withConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.withExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.withHostTrustDecision
import de.hamedtanha.servertoolkit.navigation.SshDestination
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SshViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val connectionAttemptUseCase: SshConnectionAttemptUseCase,
    private val confirmHostTrustUseCase: ConfirmSshHostTrustUseCase,
    private val commandExecutionUseCase: SshCommandExecutionUseCase,
) : ViewModel() {

    private val serverId: String = checkNotNull(savedStateHandle[SshDestination.SERVER_ID_ARGUMENT])

    private var isConnectionAttemptInProgress: Boolean = false

    private var isCommandExecutionInProgress: Boolean = false

    private var isHostKeyConfirmationInProgress: Boolean = false

    private var activeSessionHandle: SshSessionHandle? = null

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

    fun onCommandChanged(command: String) {
        if (isCommandExecutionInProgress) {
            return
        }

        _uiState.value = _uiState.value.copy(
            commandExecution = _uiState.value.commandExecution.withCommandText(command),
        )
    }

    fun onExecuteCommandClicked() {
        viewModelScope.launch {
            executeCommand()
        }
    }

    internal suspend fun connect() {
        if (isConnectionAttemptInProgress || activeSessionHandle != null) {
            return
        }

        isConnectionAttemptInProgress = true
        activeSessionHandle = null

        try {
            _uiState.value = _uiState.value.copy(
                status = SshConnectionStatus.Connecting,
                statusLabel = "Connecting",
                message = "Starting connection attempt.",
                detail = "The connection attempt is contained by timeout, cancellation, and error-mapping boundaries.",
                hostKeyReview = null,
                commandExecution = _uiState.value.commandExecution.asSessionUnavailable(),
            )

            val authenticationInput = pendingAuthenticationSecrets.toAuthenticationInput(
                method = _uiState.value.authenticationInput.selectedMethod,
            )
            val outcome = connectionAttemptUseCase(
                serverId = serverId,
                authenticationInput = authenticationInput,
            )
            onConnectionAttemptOutcomeReceived(outcome)
        } finally {
            pendingAuthenticationSecrets.clear()
            _uiState.value = _uiState.value.copy(
                authenticationInput = SshAuthenticationInputUiState(),
            )
            isConnectionAttemptInProgress = false
        }
    }

    internal fun onConnectionResultReceived(result: SshConnectionResult) {
        pendingObservedHostKey = null
        clearAuthenticationInputState()
        activeSessionHandle = when (result) {
            is SshConnectionResult.Connected -> result.sessionHandle
            is SshConnectionResult.Failed -> null
        }
        val commandExecution = when (result) {
            is SshConnectionResult.Connected -> _uiState.value.commandExecution
            is SshConnectionResult.Failed -> _uiState.value.commandExecution.asSessionUnavailable()
        }

        _uiState.value = _uiState.value.withConnectionResult(result).copy(
            commandExecution = commandExecution,
        )
    }

    internal fun onConnectionAttemptOutcomeReceived(outcome: SshConnectionAttemptOutcome) {
        when (outcome) {
            is SshConnectionAttemptOutcome.ConnectionResult -> onConnectionResultReceived(
                outcome.result,
            )

            is SshConnectionAttemptOutcome.HostTrustDecisionRequired -> {
                clearAuthenticationInputState()
                onHostTrustDecisionReceived(outcome.decision)
            }
        }
    }

    internal fun onHostTrustDecisionReceived(decision: SshHostTrustDecision) {
        activeSessionHandle = null

        pendingObservedHostKey = when (decision) {
            is SshHostTrustDecision.ReviewRequired -> decision.observedHostKey
            is SshHostTrustDecision.BlockedChangedHostKey -> null
            is SshHostTrustDecision.Accepted -> null
        }

        _uiState.value = _uiState.value.withHostTrustDecision(decision).copy(
            commandExecution = _uiState.value.commandExecution.asSessionUnavailable(),
        )
    }

    internal suspend fun executeCommand() {
        if (isCommandExecutionInProgress) {
            return
        }

        if (!_uiState.value.commandExecution.canExecute) {
            return
        }

        val sessionHandle = activeSessionHandle
        if (sessionHandle == null) {
            _uiState.value = _uiState.value.copy(
                commandExecution = _uiState.value.commandExecution.withExecutionResult(
                    SshCommandExecutionResult.Failed(SshCommandExecutionError.SessionNotFound),
                ),
            )
            return
        }

        if (!_uiState.value.canExecuteCommand) {
            return
        }

        isCommandExecutionInProgress = true

        try {
            val command = _uiState.value.commandExecution.command

            _uiState.value = _uiState.value.copy(
                commandExecution = _uiState.value.commandExecution.asRunning(),
            )

            val result = commandExecutionUseCase(
                sessionHandle = sessionHandle,
                command = command,
            )

            if (activeSessionHandle == sessionHandle) {
                _uiState.value = _uiState.value.copy(
                    commandExecution = _uiState.value.commandExecution.withExecutionResult(result),
                )
            }
        } catch (error: CancellationException) {
            if (activeSessionHandle == sessionHandle) {
                _uiState.value = _uiState.value.copy(
                    commandExecution = _uiState.value.commandExecution.withExecutionResult(
                        SshCommandExecutionResult.Failed(SshCommandExecutionError.CommandCancelled),
                    ),
                )
            }
            throw error
        } finally {
            isCommandExecutionInProgress = false
        }
    }

    internal suspend fun confirmPendingHostKey() {
        if (isHostKeyConfirmationInProgress) {
            return
        }

        val observedHostKey = pendingObservedHostKey ?: return

        isHostKeyConfirmationInProgress = true

        try {
            val decision = confirmHostTrustUseCase(observedHostKey)
            onHostTrustDecisionReceived(decision)
        } finally {
            isHostKeyConfirmationInProgress = false
        }
    }

    private fun clearPendingHostKeyReview(
        message: String,
        detail: String,
    ) {
        activeSessionHandle = null
        pendingObservedHostKey = null

        _uiState.value = _uiState.value.copy(
            status = SshConnectionStatus.Failed,
            statusLabel = "Server identity review cancelled",
            message = message,
            detail = detail,
            hostKeyReview = null,
            commandExecution = _uiState.value.commandExecution.asSessionUnavailable(),
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
