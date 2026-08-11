package de.hamedtanha.servertoolkit.feature.ssh.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import de.hamedtanha.servertoolkit.ui.designsystem.theme.ServerToolkitButtonShape
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshSavedCommandSelectorUiState

@Composable
internal fun SshSavedCommandSelectorContent(
    selector: SshSavedCommandSelectorUiState,
    onRetry: () -> Unit,
    onCancel: () -> Unit,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!selector.isVisible) {
        return
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(SSH_SAVED_COMMAND_SELECTOR_TEST_TAG),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Saved commands",
                style = MaterialTheme.typography.titleSmall,
            )

            when (selector) {
                SshSavedCommandSelectorUiState.Hidden -> Unit

                SshSavedCommandSelectorUiState.Loading -> {
                    SavedCommandSelectorProgress(
                        message = "Loading saved commands.",
                    )
                }

                SshSavedCommandSelectorUiState.Empty -> {
                    Text(
                        text = "No saved commands are available.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                is SshSavedCommandSelectorUiState.Failure -> {
                    SavedCommandSelectorErrorContent(
                        message = selector.errorMessage,
                        isRetrying = selector.isRetrying,
                        onRetry = onRetry,
                    )
                }

                is SshSavedCommandSelectorUiState.Content -> {
                    selector.errorMessage?.let { message ->
                        SavedCommandSelectorErrorContent(
                            message = message,
                            isRetrying = selector.isRetrying,
                            onRetry = onRetry,
                        )
                    }

                    if (
                        selector.errorMessage == null &&
                        selector.isRetrying
                    ) {
                        SavedCommandSelectorProgress(
                            message = "Retrying saved commands.",
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement =
                            Arrangement.spacedBy(8.dp),
                    ) {
                        items(
                            items = selector.commands,
                            key = { command -> command.id },
                        ) { command ->
                            SavedCommandSelectionItem(
                                command = command,
                                onSelect = onSelect,
                            )
                        }
                    }
                }
            }

            OutlinedButton(

                shape = ServerToolkitButtonShape,
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Cancel")
            }
        }
    }
}

@Composable
private fun SavedCommandSelectorProgress(
    message: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator()
    }

    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
    )
}

@Composable
private fun SavedCommandSelectorErrorContent(
    message: String,
    isRetrying: Boolean,
    onRetry: () -> Unit,
) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
    )

    if (isRetrying) {
        SavedCommandSelectorProgress(
            message = "Retrying saved commands.",
        )
    }

    Button(

        shape = ServerToolkitButtonShape,
        onClick = onRetry,
        enabled = !isRetrying,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = if (isRetrying) {
                "Retrying"
            } else {
                "Retry"
            },
        )
    }
}

@Composable
private fun SavedCommandSelectionItem(
    command: SavedCommand,
    onSelect: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = command.name,
                style = MaterialTheme.typography.titleSmall,
            )

            Text(
                text = command.command,
                style = MaterialTheme.typography.bodyMedium,
                fontFamily = FontFamily.Monospace,
            )

            OutlinedButton(

                shape = ServerToolkitButtonShape,
                onClick = {
                    onSelect(command.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(
                        "$SSH_SAVED_COMMAND_USE_TEST_TAG_PREFIX${command.id}",
                    ),
            ) {
                Text(text = "Use command")
            }
        }
    }
}

internal const val SSH_COMMAND_INPUT_TEST_TAG =
    "ssh-command-input"

internal const val SSH_OPEN_SAVED_COMMAND_SELECTOR_TEST_TAG =
    "ssh-open-saved-command-selector"

internal const val SSH_SAVED_COMMAND_SELECTOR_TEST_TAG =
    "ssh-saved-command-selector"

internal const val SSH_SAVED_COMMAND_USE_TEST_TAG_PREFIX =
    "ssh-saved-command-use-"
