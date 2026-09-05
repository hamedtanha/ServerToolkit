package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.ServerEnvironment
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerInventoryFilter
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerInventoryUiState
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.applyServerInventoryFilter
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
class ServerInventoryViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ServerInventoryUiState(isLoading = true),
    )
    val uiState: StateFlow<ServerInventoryUiState> = _uiState.asStateFlow()

    private var latestServers: List<Server> = emptyList()
    private var observationJob: Job? = null

    init {
        observeServers(showLoading = true)
    }

    fun onRetryLoad() {
        observeServers(
            showLoading = _uiState.value.totalServerCount == 0,
        )
    }

    fun onSearchQueryChanged(searchQuery: String) {
        updateFilter { currentFilter ->
            currentFilter.copy(searchQuery = searchQuery)
        }
    }

    fun onEnvironmentFilterChanged(environment: ServerEnvironment?) {
        updateFilter { currentFilter ->
            currentFilter.copy(environment = environment)
        }
    }

    fun onFavoritesOnlyChanged(favoritesOnly: Boolean) {
        updateFilter { currentFilter ->
            currentFilter.copy(favoritesOnly = favoritesOnly)
        }
    }

    fun onClearFilters() {
        updateFilter { ServerInventoryFilter() }
    }

    fun onDeleteServerConfirmed(serverId: String) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(operationMessage = null)
            }

            try {
                serverRepository.deleteServer(serverId)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(operationMessage = DELETE_ERROR_MESSAGE)
                }
            }
        }
    }

    private fun observeServers(showLoading: Boolean) {
        observationJob?.cancel()

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = showLoading,
                errorMessage = null,
            )
        }

        observationJob = viewModelScope.launch {
            try {
                serverRepository.observeServers().collect { servers ->
                    latestServers = servers

                    _uiState.update { currentState ->
                        currentState.copy(
                            servers = servers.applyServerInventoryFilter(currentState.filter),
                            totalServerCount = servers.size,
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

    private fun updateFilter(
        transform: (ServerInventoryFilter) -> ServerInventoryFilter,
    ) {
        _uiState.update { currentState ->
            val updatedFilter = transform(currentState.filter)
            currentState.copy(
                servers = latestServers.applyServerInventoryFilter(updatedFilter),
                filter = updatedFilter,
            )
        }
    }

    private companion object {
        const val LOAD_ERROR_MESSAGE = "Server inventory could not be loaded."
        const val DELETE_ERROR_MESSAGE = "Server could not be deleted."
    }
}
