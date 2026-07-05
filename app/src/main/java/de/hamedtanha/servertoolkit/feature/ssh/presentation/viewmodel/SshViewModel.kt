package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionStatus
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.withConnectionResult
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
) : ViewModel() {

    private val serverId: String = checkNotNull(savedStateHandle[SshDestination.SERVER_ID_ARGUMENT])

    private var isConnectionAttemptInProgress: Boolean = false

    private val _uiState = MutableStateFlow(
        SshUiState(serverId = serverId),
    )
    val uiState: StateFlow<SshUiState> = _uiState.asStateFlow()

    fun onConnectClicked() {
        viewModelScope.launch {
            connect()
        }
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
            )

            val result = connectionAttemptUseCase(serverId)
            onConnectionResultReceived(result)
        } finally {
            isConnectionAttemptInProgress = false
        }
    }

    internal fun onConnectionResultReceived(result: SshConnectionResult) {
        _uiState.value = _uiState.value.withConnectionResult(result)
    }
}
