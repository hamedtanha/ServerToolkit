package de.hamedtanha.servertoolkit.feature.savedcommands.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state.SavedCommandCreateFormUiState
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state.SavedCommandsUiState
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.viewmodel.SavedCommandsViewModel

@Composable
fun SavedCommandsRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SavedCommandsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SavedCommandsScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetryLoad = viewModel::onRetryLoad,
        onOpenCreate = viewModel::onOpenCreate,
        onCancelCreate = viewModel::onCancelCreate,
        onCreateNameChanged = viewModel::onCreateNameChanged,
        onCreateCommandChanged = viewModel::onCreateCommandChanged,
        onCreateConfirmed = viewModel::onCreateConfirmed,
        modifier = modifier,
    )
}

@Composable
fun SavedCommandsScreen(
    uiState: SavedCommandsUiState,
    onNavigateBack: () -> Unit,
    onRetryLoad: () -> Unit,
    onOpenCreate: () -> Unit,
    onCancelCreate: () -> Unit,
    onCreateNameChanged: (String) -> Unit,
    onCreateCommandChanged: (String) -> Unit,
    onCreateConfirmed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "Saved Commands",
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Reusable command text stored locally on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onOpenCreate,
            enabled = !uiState.isLoading &&
                !uiState.hasBlockingError &&
                !uiState.isCreateVisible,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Add saved command")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading && !uiState.hasCommands -> {
                SavedCommandsLoadingContent(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }

            uiState.hasBlockingError -> {
                SavedCommandsMessageContent(
                    title = "Saved commands could not be loaded",
                    message = requireNotNull(uiState.errorMessage),
                    onRetryLoad = onRetryLoad,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }

            uiState.isEmpty -> {
                SavedCommandsMessageContent(
                    title = "No saved commands",
                    message = "Create a saved command to reuse operational text later.",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }

            uiState.hasCommands -> {
                SavedCommandsContent(
                    uiState = uiState,
                    onRetryLoad = onRetryLoad,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Back")
        }
    }

    uiState.createForm?.let { createForm ->
        SavedCommandCreateDialog(
            form = createForm,
            onNameChanged = onCreateNameChanged,
            onCommandChanged = onCreateCommandChanged,
            onCancel = onCancelCreate,
            onCreate = onCreateConfirmed,
        )
    }
}

@Composable
private fun SavedCommandCreateDialog(
    form: SavedCommandCreateFormUiState,
    onNameChanged: (String) -> Unit,
    onCommandChanged: (String) -> Unit,
    onCancel: () -> Unit,
    onCreate: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {
            if (!form.isSaving) {
                onCancel()
            }
        },
        title = {
            Text(text = "Create saved command")
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "The command is stored as entered and is not executed.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedTextField(
                    value = form.name,
                    onValueChange = onNameChanged,
                    label = {
                        Text(text = "Name")
                    },
                    supportingText = form.nameError?.let { nameError ->
                        {
                            Text(text = nameError)
                        }
                    },
                    isError = form.nameError != null,
                    enabled = !form.isSaving,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                OutlinedTextField(
                    value = form.command,
                    onValueChange = onCommandChanged,
                    label = {
                        Text(text = "Command text")
                    },
                    supportingText = form.commandError?.let { commandError ->
                        {
                            Text(text = commandError)
                        }
                    },
                    isError = form.commandError != null,
                    enabled = !form.isSaving,
                    minLines = 4,
                    maxLines = 10,
                    modifier = Modifier.fillMaxWidth(),
                )

                form.errorMessage?.let { errorMessage ->
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCreate,
                enabled = !form.isSaving,
            ) {
                if (form.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Saving")
                } else {
                    Text(text = "Create")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancel,
                enabled = !form.isSaving,
            ) {
                Text(text = "Cancel")
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !form.isSaving,
            dismissOnClickOutside = !form.isSaving,
        ),
    )
}

@Composable
private fun SavedCommandsLoadingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun SavedCommandsMessageContent(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    onRetryLoad: (() -> Unit)? = null,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            onRetryLoad?.let { retry ->
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = retry,
                ) {
                    Text(text = "Retry")
                }
            }
        }
    }
}

@Composable
private fun SavedCommandsContent(
    uiState: SavedCommandsUiState,
    onRetryLoad: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        if (uiState.hasNonBlockingError) {
            SavedCommandsObservationWarning(
                message = requireNotNull(uiState.errorMessage),
                onRetryLoad = onRetryLoad,
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                items = uiState.commands,
                key = { command -> command.id },
            ) { command ->
                SavedCommandItem(command)
            }
        }
    }
}

@Composable
private fun SavedCommandsObservationWarning(
    message: String,
    onRetryLoad: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRetryLoad,
            ) {
                Text(text = "Retry")
            }
        }
    }
}

@Composable
private fun SavedCommandItem(
    command: SavedCommand,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = command.name,
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                text = command.command,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
