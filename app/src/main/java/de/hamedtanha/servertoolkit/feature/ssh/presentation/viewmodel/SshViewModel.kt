package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostTrustDecision
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.ConfirmSshHostTrustUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
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

            val result = connectionAttemptUseCase(serverId)
            onConnectionResultReceived(result)
        } finally {
            isConnectionAttemptInProgress = false
        }
    }

    internal fun onConnectionResultReceived(result: SshConnectionResult) {
        pendingObservedHostKey = null
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
}
