package de.hamedtanha.servertoolkit.feature.savedcommands.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.repository.SavedCommandRepository
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state.SavedCommandsUiState
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
class SavedCommandsViewModel @Inject constructor(
    private val savedCommandRepository: SavedCommandRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SavedCommandsUiState(isLoading = true),
    )
    val uiState: StateFlow<SavedCommandsUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        observeSavedCommands(showLoading = true)
    }

    fun onRetryLoad() {
        observeSavedCommands(
            showLoading = _uiState.value.commands.isEmpty(),
        )
    }

    private fun observeSavedCommands(
        showLoading: Boolean,
    ) {
        observationJob?.cancel()

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = showLoading,
                errorMessage = null,
            )
        }

        observationJob = viewModelScope.launch {
            try {
                savedCommandRepository
                    .observeSavedCommands()
                    .collect { commands ->
                        _uiState.value = SavedCommandsUiState(
                            commands = commands,
                        )
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
        const val LOAD_ERROR_MESSAGE =
            "Saved commands could not be loaded."
    }
}
