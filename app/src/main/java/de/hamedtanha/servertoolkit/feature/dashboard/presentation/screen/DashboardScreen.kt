package de.hamedtanha.servertoolkit.feature.dashboard.presentation.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.hamedtanha.servertoolkit.feature.dashboard.presentation.state.DashboardUiState
import de.hamedtanha.servertoolkit.feature.dashboard.presentation.viewmodel.DashboardViewModel

@Composable
fun DashboardRoute(
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
) {
    Text(text = uiState.title)
}
