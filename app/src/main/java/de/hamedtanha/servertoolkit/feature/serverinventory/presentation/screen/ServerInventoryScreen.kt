package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerInventoryUiState
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel.ServerInventoryViewModel

@Composable
fun ServerInventoryRoute(
    modifier: Modifier = Modifier,
    viewModel: ServerInventoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ServerInventoryScreen(
        uiState = uiState,
        modifier = modifier,
    )
}

@Composable
fun ServerInventoryScreen(
    uiState: ServerInventoryUiState,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> {
            ServerInventoryLoadingContent(modifier = modifier)
        }

        uiState.errorMessage != null -> {
            ServerInventoryErrorContent(
                message = uiState.errorMessage,
                modifier = modifier,
            )
        }

        uiState.isEmpty -> {
            ServerInventoryEmptyContent(modifier = modifier)
        }

        else -> {
            ServerInventoryLoadedContent(
                serverCount = uiState.servers.size,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun ServerInventoryEmptyContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No servers yet",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Your server inventory will appear here after servers are added.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ServerInventoryLoadingContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ServerInventoryErrorContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Server inventory could not be loaded",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error,
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ServerInventoryLoadedContent(
    serverCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$serverCount servers",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )

        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = "Server list rendering will be implemented in a later step.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
