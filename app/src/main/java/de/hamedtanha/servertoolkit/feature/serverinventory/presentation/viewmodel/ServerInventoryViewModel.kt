package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerInventoryUiState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ServerInventoryViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
) : ViewModel() {

    private val operationMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ServerInventoryUiState> = serverRepository.observeServers()
        .map { servers ->
            ServerInventoryUiState(
                servers = servers,
            )
        }
        .combine(operationMessage) { state, message ->
            state.copy(operationMessage = message)
        }
        .catch { throwable ->
            emit(
                ServerInventoryUiState(
                    errorMessage = throwable.message ?: "Unknown server inventory error.",
                ),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ServerInventoryUiState(isLoading = true),
        )

    fun onDeleteServerConfirmed(serverId: String) {
        viewModelScope.launch {
            operationMessage.value = null

            runCatching {
                serverRepository.deleteServer(serverId)
            }.onFailure {
                operationMessage.value = "Server could not be deleted."
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
