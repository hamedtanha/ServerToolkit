package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand

sealed interface SshSavedCommandSelectorUiState {

    val isVisible: Boolean
        get() = this !is Hidden

    val selectableCommands: List<SavedCommand>
        get() = when (this) {
            is Content -> commands
            else -> emptyList()
        }

    data object Hidden : SshSavedCommandSelectorUiState

    data object Loading : SshSavedCommandSelectorUiState

    data object Empty : SshSavedCommandSelectorUiState

    data class Content(
        val commands: List<SavedCommand>,
        val errorMessage: String? = null,
        val isRetrying: Boolean = false,
    ) : SshSavedCommandSelectorUiState {

        init {
            require(commands.isNotEmpty()) {
                "Saved command selector content must contain at least one command."
            }
            require(errorMessage == null || errorMessage.isNotBlank()) {
                "Saved command selector content error must not be blank."
            }
        }
    }

    data class Failure(
        val errorMessage: String,
        val isRetrying: Boolean = false,
    ) : SshSavedCommandSelectorUiState {

        init {
            require(errorMessage.isNotBlank()) {
                "Saved command selector failure message must not be blank."
            }
        }
    }
}
