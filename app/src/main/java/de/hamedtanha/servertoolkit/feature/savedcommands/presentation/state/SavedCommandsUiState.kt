package de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state

import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand

data class SavedCommandsUiState(
    val commands: List<SavedCommand> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean
        get() = commands.isEmpty() && !isLoading && errorMessage == null

    val hasCommands: Boolean
        get() = commands.isNotEmpty()

    val hasBlockingError: Boolean
        get() = commands.isEmpty() && !isLoading && errorMessage != null

    val hasNonBlockingError: Boolean
        get() = commands.isNotEmpty() && errorMessage != null
}
