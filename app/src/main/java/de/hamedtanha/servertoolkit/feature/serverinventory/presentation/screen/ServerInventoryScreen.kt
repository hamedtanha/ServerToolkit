package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerInventoryUiState
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel.ServerInventoryViewModel

@Composable
fun ServerInventoryRoute(
    onAddServerClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServerInventoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ServerInventoryScreen(
        uiState = uiState,
        onAddServerClick = onAddServerClick,
        modifier = modifier,
    )
}

@Composable
fun ServerInventoryScreen(
    uiState: ServerInventoryUiState,
    onAddServerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        uiState.isLoading -> ServerInventoryLoadingContent(modifier = modifier)
        uiState.errorMessage != null -> ServerInventoryErrorContent(
            message = uiState.errorMessage,
            modifier = modifier,
        )
        uiState.isInventoryEmpty -> ServerInventoryEmptyContent(
            onAddServerClick = onAddServerClick,
            modifier = modifier,
        )
        uiState.isFilterResultEmpty -> ServerInventoryEmptyFilterContent(modifier = modifier)
        uiState.hasVisibleServers -> ServerInventoryLoadedContent(
            servers = uiState.servers,
            onAddServerClick = onAddServerClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun ServerInventoryEmptyContent(
    onAddServerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ServerInventoryMessageContent(
        title = "No servers yet",
        message = "Add your first server to start building your infrastructure inventory.",
        modifier = modifier,
        action = {
            Button(
                onClick = onAddServerClick,
            ) {
                Text(text = "Add server")
            }
        },
    )
}

@Composable
private fun ServerInventoryEmptyFilterContent(
    modifier: Modifier = Modifier,
) {
    ServerInventoryMessageContent(
        title = "No matching servers",
        message = "Try changing the current search or filter criteria.",
        modifier = modifier,
    )
}

@Composable
private fun ServerInventoryLoadingContent(
    modifier: Modifier = Modifier,
) {
    ServerInventoryCenteredContent(modifier = modifier) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ServerInventoryErrorContent(
    message: String,
    modifier: Modifier = Modifier,
) {
    ServerInventoryMessageContent(
        title = "Server inventory could not be loaded",
        message = message,
        modifier = modifier,
        isError = true,
    )
}

@Composable
private fun ServerInventoryLoadedContent(
    servers: List<Server>,
    onAddServerClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "Servers",
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${servers.size} servers configured.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAddServerClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Add server")
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = servers,
                key = { server -> server.id },
            ) { server ->
                ServerInventoryListItem(server = server)
            }
        }
    }
}

@Composable
private fun ServerInventoryListItem(
    server: Server,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = server.name,
                style = MaterialTheme.typography.titleMedium,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${server.host}:${server.sshPort}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            server.sshUsername?.takeIf { username ->
                username.isNotBlank()
            }?.let { username ->
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "User: $username",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Environment: ${server.environment}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ServerInventoryMessageContent(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    action: @Composable (() -> Unit)? = null,
) {
    ServerInventoryCenteredContent(modifier = modifier) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (action != null) {
            Spacer(modifier = Modifier.height(24.dp))
            action()
        }
    }
}

@Composable
private fun ServerInventoryCenteredContent(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}
