package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.repository.SavedCommandRepository
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationInput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshAuthenticationMethod
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionAttemptOutcome
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshPrivateKeySource
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshSessionLifecycleService
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.ConfirmSshHostTrustUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshCommandExecutionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshAuthenticationInputUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionStatus
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshSavedCommandSelectorUiState
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@HiltViewModel
class SshViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val savedCommandRepository: SavedCommandRepository,
    private val connectionAttemptUseCase: SshConnectionAttemptUseCase,
    private val confirmHostTrustUseCase: ConfirmSshHostTrustUseCase,
    private val commandExecutionUseCase: SshCommandExecutionUseCase,
    private val sessionLifecycleService: SshSessionLifecycleService,
) : ViewModel() {

    private val serverId: String = checkNotNull(savedStateHandle[SshDestination.SERVER_ID_ARGUMENT])

    private var isConnectionAttemptInProgress: Boolean = false

    private var isCommandExecutionInProgress: Boolean = false

    private var isHostKeyConfirmationInProgress: Boolean = false

    private var isSessionCloseInProgress: Boolean = false

    private var isWorkflowOwnerCleared: Boolean = false

    private var savedCommandObservationJob: Job? = null

    private var activeSessionHandle: SshSessionHandle? = null

    private var pendingObservedHostKey: SshObservedHostKey? = null

    private var pendingPrivateKeySource: SshPrivateKeySource? = null

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

    fun onDisconnectClicked() {
        viewModelScope.launch {
            disconnect()
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
        clearAuthenticationInputState()

        _uiState.value = _uiState.value.copy(
            authenticationInput = SshAuthenticationInputUiState(
                selectedMethod = method,
            ),
        )
    }

    fun onPasswordChanged(password: String) {
        clearPendingPrivateKeySource()
        pendingAuthenticationSecrets.password = password
        pendingAuthenticationSecrets.privateKeyPassphrase = ""

        _uiState.value = _uiState.value.copy(
            authenticationInput = _uiState.value.authenticationInput.copy(
                selectedMethod = SshAuthenticationMethod.PASSWORD,
                hasPasswordInput = password.isNotEmpty(),
                hasPrivateKeySource = false,
                hasPrivateKeyPassphraseInput = false,
            ),
        )
    }

    fun onPrivateKeySourceSelected(source: SshPrivateKeySource) {
        if (
            isConnectionAttemptInProgress ||
            isSessionCloseInProgress ||
            activeSessionHandle != null
        ) {
            source.invalidate()
            return
        }

        replacePendingPrivateKeySource(source)
        pendingAuthenticationSecrets.password = ""

        _uiState.value = _uiState.value.copy(
            authenticationInput = _uiState.value.authenticationInput.copy(
                selectedMethod = SshAuthenticationMethod.PRIVATE_KEY,
                hasPasswordInput = false,
                hasPrivateKeySource = true,
            ),
        )
    }

    fun onPrivateKeySelectionCancelled() {
        clearPendingPrivateKeySource()
        pendingAuthenticationSecrets.privateKeyPassphrase = ""

        _uiState.value = _uiState.value.copy(
            authenticationInput = SshAuthenticationInputUiState(
                selectedMethod = SshAuthenticationMethod.PRIVATE_KEY,
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
                hasPrivateKeySource = pendingPrivateKeySource != null,
                hasPrivateKeyPassphraseInput = passphrase.isNotEmpty(),
            ),
        )
    }

    fun onAuthenticationInputCleared() {
        clearAuthenticationInputState()
    }

    fun onAuthenticationInputUiDisposed() {
        pendingAuthenticationSecrets.clear()

        _uiState.value = _uiState.value.copy(
            authenticationInput = _uiState.value.authenticationInput.copy(
                hasPasswordInput = false,
                hasPrivateKeySource = pendingPrivateKeySource != null,
                hasPrivateKeyPassphraseInput = false,
            ),
        )
    }

    suspend fun onWorkflowExit(): Boolean {
        if (isSessionCloseInProgress) {
            return false
        }

        closeSavedCommandSelector()
        clearAuthenticationInputState()

        if (isConnectionAttemptInProgress) {
            _uiState.value = _uiState.value.copy(
                message = "Connection attempt is still running.",
                detail = "Wait for the current connection attempt to finish before leaving.",
            )
            return false
        }

        if (isCommandExecutionInProgress) {
            _uiState.value = _uiState.value.copy(
                message = "Command execution is still running.",
                detail = "Wait for the current command to finish before leaving.",
            )
            return false
        }

        return closeActiveSession(SshSessionCloseIntent.WorkflowExit)
    }

    internal suspend fun disconnect() {
        if (isSessionCloseInProgress) {
            return
        }

        closeSavedCommandSelector()
        clearAuthenticationInputState()

        if (isConnectionAttemptInProgress) {
            _uiState.value = _uiState.value.copy(
                message = "Connection attempt is still running.",
                detail = "Wait for the current connection attempt to finish before disconnecting.",
            )
            return
        }

        if (isCommandExecutionInProgress) {
            _uiState.value = _uiState.value.copy(
                message = "Command execution is still running.",
                detail = "Wait for the current command to finish before disconnecting.",
            )
            return
        }

        closeActiveSession(SshSessionCloseIntent.UserRequest)
    }

    private suspend fun closeActiveSession(
        intent: SshSessionCloseIntent,
    ): Boolean {
        if (isSessionCloseInProgress) {
            return false
        }

        val sessionHandle = activeSessionHandle ?: return true
        val commandExecutionBeforeClose = _uiState.value.commandExecution

        isSessionCloseInProgress = true
        activeSessionHandle = null

        _uiState.value = _uiState.value.copy(
            status = SshConnectionStatus.Disconnecting,
            statusLabel = "Disconnecting",
            message = when (intent) {
                SshSessionCloseIntent.WorkflowExit ->
                    "Closing the active SSH session."

                SshSessionCloseIntent.UserRequest ->
                    "Disconnecting from the SSH server."
            },
            detail = when (intent) {
                SshSessionCloseIntent.WorkflowExit ->
                    "Navigation will continue after session cleanup completes."

                SshSessionCloseIntent.UserRequest ->
                    "The SSH session will remain unavailable while cleanup completes."
            },
            commandExecution = _uiState.value.commandExecution.asSessionUnavailable(),
        )

        return try {
            when (sessionLifecycleService.close(sessionHandle)) {
                SshSessionCloseResult.Closed -> {
                    _uiState.value = _uiState.value.copy(
                        status = SshConnectionStatus.NotStarted,
                        statusLabel = "Not connected",
                        message = when (intent) {
                            SshSessionCloseIntent.WorkflowExit ->
                                "SSH session closed."

                            SshSessionCloseIntent.UserRequest ->
                                "SSH session disconnected."
                        },
                        detail = when (intent) {
                            SshSessionCloseIntent.WorkflowExit ->
                                "The active SSH session was released before leaving the workflow."

                            SshSessionCloseIntent.UserRequest ->
                                "You can start a new SSH connection when ready."
                        },
                        commandExecution =
                            _uiState.value.commandExecution.asSessionUnavailable(),
                    )
                    true
                }

                SshSessionCloseResult.NotFound -> {
                    _uiState.value = _uiState.value.copy(
                        status = SshConnectionStatus.NotStarted,
                        statusLabel = "Not connected",
                        message = when (intent) {
                            SshSessionCloseIntent.WorkflowExit ->
                                "SSH session was already closed."

                            SshSessionCloseIntent.UserRequest ->
                                "SSH session was already disconnected."
                        },
                        detail = when (intent) {
                            SshSessionCloseIntent.WorkflowExit ->
                                "No active session resource remains for this workflow."

                            SshSessionCloseIntent.UserRequest ->
                                "No active SSH session resource remains."
                        },
                        commandExecution =
                            _uiState.value.commandExecution.asSessionUnavailable(),
                    )
                    true
                }

                SshSessionCloseResult.Failed -> {
                    if (isWorkflowOwnerCleared) {
                        sessionLifecycleService.abandon(sessionHandle)
                    } else {
                        activeSessionHandle = sessionHandle

                        _uiState.value = _uiState.value.copy(
                            status = SshConnectionStatus.Connected,
                            statusLabel = "Connected",
                            message = when (intent) {
                                SshSessionCloseIntent.WorkflowExit ->
                                    "SSH session could not be closed."

                                SshSessionCloseIntent.UserRequest ->
                                    "SSH session could not be disconnected."
                            },
                            detail = when (intent) {
                                SshSessionCloseIntent.WorkflowExit ->
                                    "Leaving the workflow was cancelled. Try again to retry cleanup."

                                SshSessionCloseIntent.UserRequest ->
                                    "The active session was preserved. Try disconnecting again."
                            },
                            commandExecution = commandExecutionBeforeClose,
                        )
                    }
                    false
                }
            }
        } catch (error: CancellationException) {
            if (isWorkflowOwnerCleared) {
                sessionLifecycleService.abandon(sessionHandle)
            } else {
                activeSessionHandle = sessionHandle

                _uiState.value = _uiState.value.copy(
                    status = SshConnectionStatus.Connected,
                    statusLabel = "Connected",
                    message = when (intent) {
                        SshSessionCloseIntent.WorkflowExit ->
                            "SSH session cleanup was cancelled."

                        SshSessionCloseIntent.UserRequest ->
                            "SSH disconnect was cancelled."
                    },
                    detail = when (intent) {
                        SshSessionCloseIntent.WorkflowExit ->
                            "The workflow remains active so session cleanup can be retried."

                        SshSessionCloseIntent.UserRequest ->
                            "The active session was preserved so disconnect can be retried."
                    },
                    commandExecution = commandExecutionBeforeClose,
                )
            }

            throw error
        } finally {
            isSessionCloseInProgress = false
        }
    }

    fun onCommandChanged(command: String) {
        updateCommandText(command)
    }

    fun onOpenSavedCommandSelector() {
        if (!_uiState.value.canOpenSavedCommandSelector) {
            return
        }

        _uiState.value = _uiState.value.copy(
            savedCommandSelector = SshSavedCommandSelectorUiState.Loading,
        )

        observeSavedCommands()
    }

    fun onRetrySavedCommandSelector() {
        val currentState = _uiState.value

        if (!currentState.canEditCommandInput) {
            return
        }

        val retryState = when (
            val selector = currentState.savedCommandSelector
        ) {
            is SshSavedCommandSelectorUiState.Content -> {
                if (
                    selector.errorMessage == null ||
                    selector.isRetrying
                ) {
                    return
                }

                selector.copy(
                    errorMessage = null,
                    isRetrying = true,
                )
            }

            is SshSavedCommandSelectorUiState.Failure -> {
                if (selector.isRetrying) {
                    return
                }

                selector.copy(isRetrying = true)
            }

            SshSavedCommandSelectorUiState.Hidden,
            SshSavedCommandSelectorUiState.Loading,
            SshSavedCommandSelectorUiState.Empty,
            -> return
        }

        _uiState.value = currentState.copy(
            savedCommandSelector = retryState,
        )

        observeSavedCommands()
    }

    fun onCancelSavedCommandSelector() {
        if (!_uiState.value.savedCommandSelector.isVisible) {
            return
        }

        closeSavedCommandSelector()
    }

    fun onSavedCommandSelected(savedCommandId: String) {
        val currentState = _uiState.value
        val selector = currentState.savedCommandSelector

        if (
            !currentState.canEditCommandInput ||
            !selector.isVisible
        ) {
            return
        }

        val savedCommand = selector.selectableCommands.firstOrNull { command ->
            command.id == savedCommandId
        }

        if (savedCommand == null) {
            if (selector is SshSavedCommandSelectorUiState.Content) {
                _uiState.value = currentState.copy(
                    savedCommandSelector = selector.copy(
                        errorMessage = SAVED_COMMAND_SELECTION_ERROR_MESSAGE,
                        isRetrying = false,
                    ),
                )
            }

            return
        }

        if (updateCommandText(savedCommand.command)) {
            closeSavedCommandSelector()
        }
    }

    fun onExecuteCommandClicked() {
        viewModelScope.launch {
            executeCommand()
        }
    }

    internal suspend fun connect() {
        if (
            isConnectionAttemptInProgress ||
            isSessionCloseInProgress ||
            activeSessionHandle != null
        ) {
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

            val authenticationInput = takeAuthenticationInput()
            _uiState.value = _uiState.value.copy(
                authenticationInput = SshAuthenticationInputUiState(),
            )

            val outcome = connectionAttemptUseCase(
                serverId = serverId,
                authenticationInput = authenticationInput,
            )
            onConnectionAttemptOutcomeReceived(outcome)
        } finally {
            clearAuthenticationInputState()
            isConnectionAttemptInProgress = false
        }
    }

    internal fun onConnectionResultReceived(result: SshConnectionResult) {
        closeSavedCommandSelector()
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
                onHostTrustDecisionReceived(outcome.decision)
            }
        }
    }

    internal fun onHostTrustDecisionReceived(decision: SshHostTrustDecision) {
        closeSavedCommandSelector()
        activeSessionHandle = null
        clearAuthenticationInputState()

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

        closeSavedCommandSelector()
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

    override fun onCleared() {
        isWorkflowOwnerCleared = true

        val sessionHandle = activeSessionHandle
        activeSessionHandle = null

        savedCommandObservationJob?.cancel()
        savedCommandObservationJob = null
        clearAuthenticationInputState()

        if (sessionHandle != null) {
            sessionLifecycleService.abandon(sessionHandle)
        }

        super.onCleared()
    }

    private fun updateCommandText(command: String): Boolean {
        if (isCommandExecutionInProgress) {
            return false
        }

        val currentState = _uiState.value

        _uiState.value = currentState.copy(
            commandExecution = currentState.commandExecution.withCommandText(command),
        )

        return true
    }

    private fun observeSavedCommands() {
        savedCommandObservationJob?.cancel()

        savedCommandObservationJob = viewModelScope.launch {
            try {
                savedCommandRepository
                    .observeSavedCommands()
                    .collect { commands ->
                        val currentState = _uiState.value

                        if (!currentState.savedCommandSelector.isVisible) {
                            return@collect
                        }

                        val selector = if (commands.isEmpty()) {
                            SshSavedCommandSelectorUiState.Empty
                        } else {
                            SshSavedCommandSelectorUiState.Content(
                                commands = commands,
                            )
                        }

                        _uiState.value = currentState.copy(
                            savedCommandSelector = selector,
                        )
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                val currentState = _uiState.value

                if (!currentState.savedCommandSelector.isVisible) {
                    return@launch
                }

                val loadedCommands =
                    currentState.savedCommandSelector.selectableCommands

                val failureState = if (loadedCommands.isEmpty()) {
                    SshSavedCommandSelectorUiState.Failure(
                        errorMessage = SAVED_COMMAND_LOAD_ERROR_MESSAGE,
                    )
                } else {
                    SshSavedCommandSelectorUiState.Content(
                        commands = loadedCommands,
                        errorMessage = SAVED_COMMAND_LOAD_ERROR_MESSAGE,
                    )
                }

                _uiState.value = currentState.copy(
                    savedCommandSelector = failureState,
                )
            }
        }
    }

    private fun closeSavedCommandSelector() {
        savedCommandObservationJob?.cancel()
        savedCommandObservationJob = null

        if (!_uiState.value.savedCommandSelector.isVisible) {
            return
        }

        _uiState.value = _uiState.value.copy(
            savedCommandSelector = SshSavedCommandSelectorUiState.Hidden,
        )
    }

    private fun clearPendingHostKeyReview(
        message: String,
        detail: String,
    ) {
        activeSessionHandle = null
        pendingObservedHostKey = null
        clearAuthenticationInputState()

        _uiState.value = _uiState.value.copy(
            status = SshConnectionStatus.Failed,
            statusLabel = "Server identity review cancelled",
            message = message,
            detail = detail,
            hostKeyReview = null,
            commandExecution = _uiState.value.commandExecution.asSessionUnavailable(),
        )
    }

    private fun takeAuthenticationInput(): SshAuthenticationInput {
        return when (_uiState.value.authenticationInput.selectedMethod) {
            SshAuthenticationMethod.PASSWORD -> SshAuthenticationInput.Password(
                pendingAuthenticationSecrets.takePassword(),
            )

            SshAuthenticationMethod.PRIVATE_KEY -> {
                val source = pendingPrivateKeySource
                pendingPrivateKeySource = null

                if (source == null) {
                    pendingAuthenticationSecrets.clear()
                    SshAuthenticationInput.None
                } else {
                    SshAuthenticationInput.PrivateKey(
                        privateKeySource = source,
                        passphrase = pendingAuthenticationSecrets.takePrivateKeyPassphrase(),
                    )
                }
            }
        }
    }

    private fun replacePendingPrivateKeySource(source: SshPrivateKeySource) {
        pendingPrivateKeySource?.invalidate()
        pendingPrivateKeySource = source
    }

    private fun clearPendingPrivateKeySource() {
        pendingPrivateKeySource?.invalidate()
        pendingPrivateKeySource = null
    }

    private fun clearAuthenticationInputState() {
        clearPendingPrivateKeySource()
        pendingAuthenticationSecrets.clear()

        _uiState.value = _uiState.value.copy(
            authenticationInput = SshAuthenticationInputUiState(),
        )
    }

    private companion object {
        const val SAVED_COMMAND_LOAD_ERROR_MESSAGE =
            "Saved commands could not be loaded."

        const val SAVED_COMMAND_SELECTION_ERROR_MESSAGE =
            "Saved command is no longer available."
    }
}

private enum class SshSessionCloseIntent {
    WorkflowExit,
    UserRequest,
}

private class PendingAuthenticationSecrets {

    var password: String = ""

    var privateKeyPassphrase: String = ""

    fun takePassword(): String {
        val currentPassword = password
        clear()
        return currentPassword
    }

    fun takePrivateKeyPassphrase(): String {
        val currentPassphrase = privateKeyPassphrase
        clear()
        return currentPassphrase
    }

    fun clear() {
        password = ""
        privateKeyPassphrase = ""
    }
}
