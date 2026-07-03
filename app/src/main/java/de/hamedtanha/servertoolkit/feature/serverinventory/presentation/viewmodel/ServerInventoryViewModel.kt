package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel

import androidx.lifecycle.ViewModel
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerInventoryUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ServerInventoryViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ServerInventoryUiState())
    val uiState: StateFlow<ServerInventoryUiState> = _uiState.asStateFlow()
}
