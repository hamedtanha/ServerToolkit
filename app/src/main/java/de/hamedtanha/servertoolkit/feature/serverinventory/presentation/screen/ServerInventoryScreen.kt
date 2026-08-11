package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.hamedtanha.servertoolkit.ui.designsystem.theme.ServerToolkitButtonShape
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.ServerEnvironment
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerInventoryFilter
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerInventoryUiState
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel.ServerInventoryViewModel

@Composable
fun ServerInventoryRoute(
    onAddServerClick: () -> Unit,
    onEditServerClick: (String) -> Unit,
    onConnectServerClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServerInventoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ServerInventoryScreen(
        uiState = uiState,
        onAddServerClick = onAddServerClick,
        onEditServerClick = onEditServerClick,
        onConnectServerClick = onConnectServerClick,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onEnvironmentFilterChanged = viewModel::onEnvironmentFilterChanged,
        onFavoritesOnlyChanged = viewModel::onFavoritesOnlyChanged,
        onClearFilters = viewModel::onClearFilters,
        onDeleteServerConfirmed = viewModel::onDeleteServerConfirmed,
        modifier = modifier,
    )
}

@Composable
fun ServerInventoryScreen(
    uiState: ServerInventoryUiState,
    onAddServerClick: () -> Unit,
    onEditServerClick: (String) -> Unit,
    onConnectServerClick: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onEnvironmentFilterChanged: (ServerEnvironment?) -> Unit,
    onFavoritesOnlyChanged: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onDeleteServerConfirmed: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var serverPendingDeletion by remember {
        mutableStateOf<Server?>(null)
    }

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
        uiState.hasVisibleServers || uiState.isFilterResultEmpty -> ServerInventoryLoadedContent(
            servers = uiState.servers,
            totalServerCount = uiState.totalServerCount,
            filter = uiState.filter,
            operationMessage = uiState.operationMessage,
            onAddServerClick = onAddServerClick,
            onEditServerClick = onEditServerClick,
            onConnectServerClick = onConnectServerClick,
            onSearchQueryChanged = onSearchQueryChanged,
            onEnvironmentFilterChanged = onEnvironmentFilterChanged,
            onFavoritesOnlyChanged = onFavoritesOnlyChanged,
            onClearFilters = onClearFilters,
            onDeleteServerClick = { server ->
                serverPendingDeletion = server
            },
            modifier = modifier,
        )
    }

    serverPendingDeletion?.let { server ->
        DeleteServerConfirmationDialog(
            server = server,
            onDismiss = {
                serverPendingDeletion = null
            },
            onConfirmDelete = {
                onDeleteServerConfirmed(server.id)
                serverPendingDeletion = null
            },
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
                shape = ServerToolkitButtonShape,
                onClick = onAddServerClick,
            ) {
                Text(text = "Add server")
            }
        },
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
    totalServerCount: Int,
    filter: ServerInventoryFilter,
    operationMessage: String?,
    onAddServerClick: () -> Unit,
    onEditServerClick: (String) -> Unit,
    onConnectServerClick: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onEnvironmentFilterChanged: (ServerEnvironment?) -> Unit,
    onFavoritesOnlyChanged: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    onDeleteServerClick: (Server) -> Unit,
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
            text = serverCountText(
                visibleServerCount = servers.size,
                totalServerCount = totalServerCount,
                hasActiveFilter = filter.hasActiveFilter,
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        operationMessage?.let { message ->
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ServerInventoryFilterControls(
            filter = filter,
            onSearchQueryChanged = onSearchQueryChanged,
            onEnvironmentFilterChanged = onEnvironmentFilterChanged,
            onFavoritesOnlyChanged = onFavoritesOnlyChanged,
            onClearFilters = onClearFilters,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(

            shape = ServerToolkitButtonShape,
            onClick = onAddServerClick,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Add server")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (servers.isEmpty()) {
            ServerInventoryNoMatchingServersContent(
                onClearFilters = onClearFilters,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(
                    items = servers,
                    key = { server -> server.id },
                ) { server ->
                    ServerInventoryListItem(
                        server = server,
                        onEditServerClick = onEditServerClick,
                        onConnectServerClick = onConnectServerClick,
                        onDeleteServerClick = onDeleteServerClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun ServerInventoryFilterControls(
    filter: ServerInventoryFilter,
    onSearchQueryChanged: (String) -> Unit,
    onEnvironmentFilterChanged: (ServerEnvironment?) -> Unit,
    onFavoritesOnlyChanged: (Boolean) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = filter.searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                Text(text = "Search servers")
            },
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                FilterChip(
                    selected = filter.environment == null,
                    onClick = {
                        onEnvironmentFilterChanged(null)
                    },
                    label = {
                        Text(text = "All")
                    },
                )
            }

            items(ServerEnvironment.entries.toList()) { environment ->
                FilterChip(
                    selected = filter.environment == environment,
                    onClick = {
                        onEnvironmentFilterChanged(environment)
                    },
                    label = {
                        Text(text = environment.name)
                    },
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FilterChip(
                selected = filter.favoritesOnly,
                onClick = {
                    onFavoritesOnlyChanged(!filter.favoritesOnly)
                },
                label = {
                    Text(text = "Favorites only")
                },
            )

            if (filter.hasActiveFilter) {
                TextButton(onClick = onClearFilters) {
                    Text(text = "Clear filters")
                }
            }
        }
    }
}

@Composable
private fun ServerInventoryNoMatchingServersContent(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "No matching servers",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Try changing the current search or filter criteria.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onClearFilters) {
            Text(text = "Clear filters")
        }
    }
}

@Composable
private fun ServerInventoryListItem(
    server: Server,
    onEditServerClick: (String) -> Unit,
    onConnectServerClick: (String) -> Unit,
    onDeleteServerClick: (Server) -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
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

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = {
                        onConnectServerClick(server.id)
                    },
                ) {
                    Text(text = "Connect")
                }

                TextButton(
                    onClick = {
                        onEditServerClick(server.id)
                    },
                ) {
                    Text(text = "Edit")
                }

                TextButton(
                    onClick = {
                        onDeleteServerClick(server)
                    },
                ) {
                    Text(text = "Delete")
                }
            }
        }
    }
}

@Composable
private fun DeleteServerConfirmationDialog(
    server: Server,
    onConfirmDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Delete server?")
        },
        text = {
            Text(text = "This removes ${server.name} from the local inventory.")
        },
        confirmButton = {
            TextButton(onClick = onConfirmDelete) {
                Text(text = "Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        },
    )
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

private fun serverCountText(
    visibleServerCount: Int,
    totalServerCount: Int,
    hasActiveFilter: Boolean,
): String {
    return if (hasActiveFilter) {
        "$visibleServerCount of $totalServerCount servers shown."
    } else {
        "$totalServerCount servers configured."
    }
}
