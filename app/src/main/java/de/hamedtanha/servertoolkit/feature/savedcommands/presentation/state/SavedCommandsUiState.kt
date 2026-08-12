package de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state

import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand

data class SavedCommandCreateFormUiState(
    val name: String = "",
    val command: String = "",
    val nameError: String? = null,
    val commandError: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

data class SavedCommandEditFormUiState(
    val savedCommandId: String,
    val name: String,
    val command: String,
    val nameError: String? = null,
    val commandError: String? = null,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

data class SavedCommandDeleteConfirmationUiState(
    val savedCommandId: String,
    val savedCommandName: String,
    val isDeleting: Boolean = false,
    val errorMessage: String? = null,
)

data class SavedCommandsUiState(
    val commands: List<SavedCommand> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createForm: SavedCommandCreateFormUiState? = null,
    val editForm: SavedCommandEditFormUiState? = null,
    val deleteConfirmation: SavedCommandDeleteConfirmationUiState? = null,
) {
    val isEmpty: Boolean
        get() = commands.isEmpty() && !isLoading && errorMessage == null

    val hasCommands: Boolean
        get() = commands.isNotEmpty()

    val hasBlockingError: Boolean
        get() = commands.isEmpty() && !isLoading && errorMessage != null

    val hasNonBlockingError: Boolean
        get() = commands.isNotEmpty() && errorMessage != null

    val isCreateVisible: Boolean
        get() = createForm != null

    val isEditVisible: Boolean
        get() = editForm != null

    val isDeleteVisible: Boolean
        get() = deleteConfirmation != null
}
