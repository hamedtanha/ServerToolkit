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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SshConnectionHistoryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val connectionHistoryRepository: SshConnectionHistoryRepository,
) : ViewModel() {

    private val serverId: String = checkNotNull(
        savedStateHandle[SshConnectionHistoryDestination.SERVER_ID_ARGUMENT],
    )

    private val _uiState = MutableStateFlow(
        SshConnectionHistoryUiState(
            serverId = serverId,
            isLoading = true,
        ),
    )
    val uiState: StateFlow<SshConnectionHistoryUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        observeConnectionHistory(showLoading = true)
    }

    fun onRetryLoad() {
        observeConnectionHistory(
            showLoading = _uiState.value.entries.isEmpty(),
        )
    }

    private fun observeConnectionHistory(showLoading: Boolean) {
        observationJob?.cancel()

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = showLoading,
                errorMessage = null,
            )
        }

        observationJob = viewModelScope.launch {
            try {
                connectionHistoryRepository
                    .observeConnectionHistoryForServer(serverId)
                    .collect { entries ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                entries = entries.map { entry -> entry.toUiState() },
                                isLoading = false,
                                errorMessage = null,
                            )
                        }
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = LOAD_ERROR_MESSAGE,
                    )
                }
            }
        }
    }

    private companion object {
        const val LOAD_ERROR_MESSAGE = "SSH connection history could not be loaded."
    }
}
