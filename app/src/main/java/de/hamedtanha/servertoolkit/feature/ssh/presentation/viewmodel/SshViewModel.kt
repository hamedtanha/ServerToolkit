package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshConnectionService
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
    private val connectionService: SshConnectionService,
) : ViewModel() {

    private val serverId: String = checkNotNull(savedStateHandle[SshDestination.SERVER_ID_ARGUMENT])

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
        _uiState.value = _uiState.value.copy(
            status = SshConnectionStatus.Connecting,
            statusLabel = "Connecting",
            message = "Preparing connection.",
            detail = "This action is limited to the current adapter shell.",
        )

        val result = connectionService.connect(
            SshConnectionRequest(
                serverId = serverId,
                host = "placeholder.invalid",
                port = 22,
                username = "placeholder",
            ),
        )

        onConnectionResultReceived(result)
    }

    internal fun onConnectionResultReceived(result: SshConnectionResult) {
        _uiState.value = _uiState.value.withConnectionResult(result)
    }
}
