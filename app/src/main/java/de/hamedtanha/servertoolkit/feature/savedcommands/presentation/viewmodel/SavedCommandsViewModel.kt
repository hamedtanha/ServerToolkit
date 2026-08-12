package de.hamedtanha.servertoolkit.feature.savedcommands.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.factory.SavedCommandFactory
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.repository.SavedCommandRepository
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state.SavedCommandCreateFormUiState
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state.SavedCommandDeleteConfirmationUiState
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state.SavedCommandEditFormUiState
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state.SavedCommandsUiState
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SavedCommandsViewModel @Inject constructor(
    private val savedCommandRepository: SavedCommandRepository,
    private val savedCommandFactory: SavedCommandFactory,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SavedCommandsUiState(isLoading = true),
    )
    val uiState: StateFlow<SavedCommandsUiState> = _uiState.asStateFlow()

    private var observationJob: Job? = null

    init {
        observeSavedCommands(showLoading = true)
    }

    fun onRetryLoad() {
        observeSavedCommands(
            showLoading = _uiState.value.commands.isEmpty(),
        )
    }

    fun onOpenCreate() {
        _uiState.update { currentState ->
            if (
                currentState.createForm != null ||
                currentState.editForm != null ||
                currentState.deleteConfirmation != null
            ) {
                currentState
            } else {
                currentState.copy(
                    createForm = SavedCommandCreateFormUiState(),
                )
            }
        }
    }

    fun onCancelCreate() {
        _uiState.update { currentState ->
            if (currentState.createForm?.isSaving == true) {
                currentState
            } else {
                currentState.copy(createForm = null)
            }
        }
    }

    fun onCreateNameChanged(name: String) {
        _uiState.update { currentState ->
            val form = currentState.createForm ?: return@update currentState
            if (form.isSaving) {
                currentState
            } else {
                currentState.copy(
                    createForm = form.copy(
                        name = name,
                        nameError = null,
                        errorMessage = null,
                    ),
                )
            }
        }
    }

    fun onCreateCommandChanged(command: String) {
        _uiState.update { currentState ->
            val form = currentState.createForm ?: return@update currentState
            if (form.isSaving) {
                currentState
            } else {
                currentState.copy(
                    createForm = form.copy(
                        command = command,
                        commandError = null,
                        errorMessage = null,
                    ),
                )
            }
        }
    }

    fun onCreateConfirmed() {
        val form = _uiState.value.createForm ?: return
        if (form.isSaving) {
            return
        }

        val normalizedName = form.name.trim()
        val nameError = validateName(normalizedName)
        val commandError = validateCommand(form.command)

        if (nameError != null || commandError != null) {
            _uiState.update { currentState ->
                val currentForm = currentState.createForm ?: return@update currentState
                currentState.copy(
                    createForm = currentForm.copy(
                        nameError = nameError,
                        commandError = commandError,
                        errorMessage = null,
                    ),
                )
            }
            return
        }

        _uiState.update { currentState ->
            val currentForm = currentState.createForm ?: return@update currentState
            currentState.copy(
                createForm = currentForm.copy(
                    isSaving = true,
                    nameError = null,
                    commandError = null,
                    errorMessage = null,
                ),
            )
        }

        viewModelScope.launch {
            try {
                val savedCommand = savedCommandFactory.create(
                    name = normalizedName,
                    command = form.command,
                )
                savedCommandRepository.createSavedCommand(savedCommand)
                _uiState.update { currentState ->
                    currentState.copy(createForm = null)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { currentState ->
                    val currentForm = currentState.createForm
                        ?: return@update currentState
                    currentState.copy(
                        createForm = currentForm.copy(
                            isSaving = false,
                            errorMessage = CREATE_ERROR_MESSAGE,
                        ),
                    )
                }
            }
        }
    }

    fun onEditRequested(savedCommandId: String) {
        _uiState.update { currentState ->
            if (
                currentState.createForm != null ||
                currentState.editForm != null ||
                currentState.deleteConfirmation != null
            ) {
                return@update currentState
            }

            val target = currentState.commands.firstOrNull { command ->
                command.id == savedCommandId
            } ?: return@update currentState

            currentState.copy(
                editForm = SavedCommandEditFormUiState(
                    savedCommandId = target.id,
                    name = target.name,
                    command = target.command,
                ),
            )
        }
    }

    fun onCancelEdit() {
        _uiState.update { currentState ->
            if (currentState.editForm?.isSaving == true) {
                currentState
            } else {
                currentState.copy(editForm = null)
            }
        }
    }

    fun onEditNameChanged(name: String) {
        _uiState.update { currentState ->
            val form = currentState.editForm ?: return@update currentState
            if (form.isSaving) {
                currentState
            } else {
                currentState.copy(
                    editForm = form.copy(
                        name = name,
                        nameError = null,
                        errorMessage = null,
                    ),
                )
            }
        }
    }

    fun onEditCommandChanged(command: String) {
        _uiState.update { currentState ->
            val form = currentState.editForm ?: return@update currentState
            if (form.isSaving) {
                currentState
            } else {
                currentState.copy(
                    editForm = form.copy(
                        command = command,
                        commandError = null,
                        errorMessage = null,
                    ),
                )
            }
        }
    }

    fun onEditConfirmed() {
        val form = _uiState.value.editForm ?: return
        if (form.isSaving) {
            return
        }

        val normalizedName = form.name.trim()
        val nameError = validateName(normalizedName)
        val commandError = validateCommand(form.command)

        if (nameError != null || commandError != null) {
            _uiState.update { currentState ->
                val currentForm = currentState.editForm
                    ?: return@update currentState

                if (currentForm.savedCommandId != form.savedCommandId) {
                    return@update currentState
                }

                currentState.copy(
                    editForm = currentForm.copy(
                        nameError = nameError,
                        commandError = commandError,
                        errorMessage = null,
                    ),
                )
            }
            return
        }

        val target = _uiState.value.commands.firstOrNull { command ->
            command.id == form.savedCommandId
        }

        if (target == null) {
            _uiState.update { currentState ->
                val currentForm = currentState.editForm
                    ?: return@update currentState

                if (currentForm.savedCommandId != form.savedCommandId) {
                    return@update currentState
                }

                currentState.copy(
                    editForm = currentForm.copy(
                        errorMessage = UPDATE_ERROR_MESSAGE,
                    ),
                )
            }
            return
        }

        val updatedCommand = target.copy(
            name = normalizedName,
            command = form.command,
        )

        _uiState.update { currentState ->
            val currentForm = currentState.editForm
                ?: return@update currentState

            if (currentForm.savedCommandId != form.savedCommandId) {
                return@update currentState
            }

            currentState.copy(
                editForm = currentForm.copy(
                    isSaving = true,
                    nameError = null,
                    commandError = null,
                    errorMessage = null,
                ),
            )
        }

        viewModelScope.launch {
            try {
                savedCommandRepository.updateSavedCommand(updatedCommand)

                _uiState.update { currentState ->
                    val currentForm = currentState.editForm
                        ?: return@update currentState

                    if (currentForm.savedCommandId != form.savedCommandId) {
                        return@update currentState
                    }

                    currentState.copy(editForm = null)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { currentState ->
                    val currentForm = currentState.editForm
                        ?: return@update currentState

                    if (currentForm.savedCommandId != form.savedCommandId) {
                        return@update currentState
                    }

                    currentState.copy(
                        editForm = currentForm.copy(
                            isSaving = false,
                            errorMessage = UPDATE_ERROR_MESSAGE,
                        ),
                    )
                }
            }
        }
    }

    fun onDeleteRequested(savedCommandId: String) {
        _uiState.update { currentState ->
            if (
                currentState.createForm != null ||
                currentState.editForm != null ||
                currentState.deleteConfirmation != null
            ) {
                return@update currentState
            }

            val target = currentState.commands.firstOrNull { command ->
                command.id == savedCommandId
            } ?: return@update currentState

            currentState.copy(
                deleteConfirmation = SavedCommandDeleteConfirmationUiState(
                    savedCommandId = target.id,
                    savedCommandName = target.name,
                ),
            )
        }
    }

    fun onCancelDelete() {
        _uiState.update { currentState ->
            if (currentState.deleteConfirmation?.isDeleting == true) {
                currentState
            } else {
                currentState.copy(deleteConfirmation = null)
            }
        }
    }

    fun onDeleteConfirmed() {
        val confirmation = _uiState.value.deleteConfirmation ?: return
        if (confirmation.isDeleting) {
            return
        }

        _uiState.update { currentState ->
            val currentConfirmation = currentState.deleteConfirmation
                ?: return@update currentState

            currentState.copy(
                deleteConfirmation = currentConfirmation.copy(
                    isDeleting = true,
                    errorMessage = null,
                ),
            )
        }

        viewModelScope.launch {
            try {
                savedCommandRepository.deleteSavedCommand(
                    confirmation.savedCommandId,
                )
                _uiState.update { currentState ->
                    currentState.copy(deleteConfirmation = null)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { currentState ->
                    val currentConfirmation = currentState.deleteConfirmation
                        ?: return@update currentState

                    if (
                        currentConfirmation.savedCommandId !=
                        confirmation.savedCommandId
                    ) {
                        return@update currentState
                    }

                    currentState.copy(
                        deleteConfirmation = currentConfirmation.copy(
                            isDeleting = false,
                            errorMessage = DELETE_ERROR_MESSAGE,
                        ),
                    )
                }
            }
        }
    }

    private fun observeSavedCommands(
        showLoading: Boolean,
    ) {
        observationJob?.cancel()

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = showLoading,
                errorMessage = null,
            )
        }

        observationJob = viewModelScope.launch {
            try {
                savedCommandRepository
                    .observeSavedCommands()
                    .collect { commands ->
                        _uiState.update { currentState ->
                            currentState.copy(
                                commands = commands,
                                isLoading = false,
                                errorMessage = null,
                            )
                        }
                    }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = LOAD_ERROR_MESSAGE,
                    )
                }
            }
        }
    }

    private fun validateName(normalizedName: String): String? {
        return when {
            normalizedName.isBlank() -> NAME_REQUIRED_MESSAGE
            normalizedName.any { character -> character.isISOControl() } ->
                NAME_CONTROL_CHARACTER_MESSAGE
            normalizedName.length > SavedCommand.MAX_NAME_LENGTH ->
                NAME_TOO_LONG_MESSAGE
            else -> null
        }
    }

    private fun validateCommand(command: String): String? {
        return when {
            command.isBlank() -> COMMAND_REQUIRED_MESSAGE
            command.length > SavedCommand.MAX_COMMAND_LENGTH ->
                COMMAND_TOO_LONG_MESSAGE
            else -> null
        }
    }

    private companion object {
        const val LOAD_ERROR_MESSAGE =
            "Saved commands could not be loaded."
        const val CREATE_ERROR_MESSAGE =
            "Saved command could not be created."
        const val UPDATE_ERROR_MESSAGE =
            "Saved command could not be updated."
        const val DELETE_ERROR_MESSAGE =
            "Saved command could not be deleted."
        const val NAME_REQUIRED_MESSAGE =
            "Name is required."
        const val NAME_CONTROL_CHARACTER_MESSAGE =
            "Name must not contain control characters."
        const val NAME_TOO_LONG_MESSAGE =
            "Name must not exceed ${SavedCommand.MAX_NAME_LENGTH} characters."
        const val COMMAND_REQUIRED_MESSAGE =
            "Command text is required."
        const val COMMAND_TOO_LONG_MESSAGE =
            "Command text must not exceed ${SavedCommand.MAX_COMMAND_LENGTH} characters."
    }
}
