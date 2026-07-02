package de.hamedtanha.servertoolkit.feature.dashboard.presentation.viewmodel

import androidx.lifecycle.ViewModel
import de.hamedtanha.servertoolkit.feature.dashboard.presentation.state.DashboardUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())

    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
}

