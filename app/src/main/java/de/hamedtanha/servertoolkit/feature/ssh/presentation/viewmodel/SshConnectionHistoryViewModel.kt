package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.ssh.domain.repository.SshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionHistoryUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.toUiState
import de.hamedtanha.servertoolkit.navigation.SshConnectionHistoryDestination
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SshConnectionHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    connectionHistoryRepository: SshConnectionHistoryRepository,
) : ViewModel() {

    private val serverId: String = checkNotNull(
        savedStateHandle[SshConnectionHistoryDestination.SERVER_ID_ARGUMENT],
    )

    val uiState: StateFlow<SshConnectionHistoryUiState> =
        connectionHistoryRepository.observeConnectionHistoryForServer(serverId)
            .map { entries ->
                SshConnectionHistoryUiState(
                    serverId = serverId,
                    entries = entries.map { entry -> entry.toUiState() },
                )
            }
            .catch {
                emit(
                    SshConnectionHistoryUiState(
                        serverId = serverId,
                        errorMessage = "SSH connection history could not be loaded.",
                    ),
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = SshConnectionHistoryUiState(
                    serverId = serverId,
                    isLoading = true,
                ),
            )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
