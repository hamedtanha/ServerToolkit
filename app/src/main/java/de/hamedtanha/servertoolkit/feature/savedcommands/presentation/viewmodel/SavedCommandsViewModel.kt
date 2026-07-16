package de.hamedtanha.servertoolkit.feature.savedcommands.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.factory.SavedCommandFactory
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.repository.SavedCommandRepository
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state.SavedCommandCreateFormUiState
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
            if (currentState.createForm != null) {
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
