package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerInventoryUiState
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ServerInventoryViewModel @Inject constructor(
    serverRepository: ServerRepository,
) : ViewModel() {

    val uiState: StateFlow<ServerInventoryUiState> = serverRepository.observeServers()
        .map { servers ->
            ServerInventoryUiState(
                servers = servers,
            )
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

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
