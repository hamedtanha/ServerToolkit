package de.hamedtanha.servertoolkit.feature.dashboard.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.hamedtanha.servertoolkit.feature.dashboard.presentation.state.DashboardUiState
import de.hamedtanha.servertoolkit.feature.dashboard.presentation.viewmodel.DashboardViewModel
import de.hamedtanha.servertoolkit.ui.designsystem.theme.ServerToolkitDesignSystem

@Composable
fun DashboardRoute(
    onOpenServerInventory: () -> Unit,
    onOpenSavedCommands: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DashboardScreen(
        uiState = uiState,
        onOpenServerInventory = onOpenServerInventory,
        onOpenSavedCommands = onOpenSavedCommands,
    )
}

@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    onOpenServerInventory: () -> Unit,
    onOpenSavedCommands: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = ServerToolkitDesignSystem.spacing

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(spacing.extraLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = uiState.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(spacing.large))

        Text(
            text = "Choose a destination",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(spacing.small))

        Text(
            text = "Open server inventory or manage reusable command text.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(spacing.extraLarge))

        Button(
            onClick = onOpenServerInventory,
        ) {
            Text(text = "Open server inventory")
        }

        Spacer(modifier = Modifier.height(spacing.medium))

        OutlinedButton(
            onClick = onOpenSavedCommands,
        ) {
            Text(text = "Manage saved commands")
        }
    }
}
