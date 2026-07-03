package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.AddServerUiState
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class AddServerViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddServerUiState())
    val uiState: StateFlow<AddServerUiState> = _uiState.asStateFlow()

    fun onNameChanged(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                name = name,
                nameError = validateRequiredText(
                    value = name,
                    fieldName = "Server name",
                ),
                formMessage = null,
                isSaved = false,
            )
        }
    }

    fun onHostChanged(host: String) {
        _uiState.update { currentState ->
            currentState.copy(
                host = host,
                hostError = validateRequiredText(
                    value = host,
                    fieldName = "Host",
                ),
                formMessage = null,
                isSaved = false,
            )
        }
    }

    fun onPortChanged(port: String) {
        _uiState.update { currentState ->
            currentState.copy(
                port = port,
                portError = validatePort(port),
                formMessage = null,
                isSaved = false,
            )
        }
    }

    fun onUsernameChanged(username: String) {
        _uiState.update { currentState ->
            currentState.copy(
                username = username,
                usernameError = validateRequiredText(
                    value = username,
                    fieldName = "Username",
                ),
                formMessage = null,
                isSaved = false,
            )
        }
    }

    fun onSaveClicked() {
        val validatedState = validateState(_uiState.value)

        if (!validatedState.canSave) {
            _uiState.value = validatedState.copy(
                formMessage = "Please fix the highlighted fields.",
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = validatedState.copy(
                isSaving = true,
                formMessage = null,
            )

            runCatching {
                serverRepository.saveServer(validatedState.toServer())
            }.onSuccess {
                _uiState.update { currentState ->
                    currentState.copy(
                        isSaving = false,
                        isSaved = true,
                        formMessage = null,
                    )
                }
            }.onFailure { throwable ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isSaving = false,
                        isSaved = false,
                        formMessage = throwable.message ?: "Server could not be saved.",
                    )
                }
            }
        }
    }

    private fun validateState(state: AddServerUiState): AddServerUiState {
        return state.copy(
            nameError = validateRequiredText(
                value = state.name,
                fieldName = "Server name",
            ),
            hostError = validateRequiredText(
                value = state.host,
                fieldName = "Host",
            ),
            portError = validatePort(state.port),
            usernameError = validateRequiredText(
                value = state.username,
                fieldName = "Username",
            ),
        )
    }

    private fun validateRequiredText(
        value: String,
        fieldName: String,
    ): String? {
        return if (value.isBlank()) {
            "$fieldName is required."
        } else {
            null
        }
    }

    private fun validatePort(port: String): String? {
        val parsedPort = port.toIntOrNull()

        return when {
            port.isBlank() -> "Port is required."
            parsedPort == null -> "Port must be a number."
            parsedPort !in AddServerUiState.MIN_PORT..AddServerUiState.MAX_PORT ->
                "Port must be between ${AddServerUiState.MIN_PORT} and ${AddServerUiState.MAX_PORT}."
            else -> null
        }
    }

    private fun AddServerUiState.toServer(): Server {
        return Server(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            host = host.trim(),
            sshPort = requireNotNull(port.toIntOrNull()),
            sshUsername = username.trim(),
        )
    }
}
