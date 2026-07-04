package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.AddServerUiState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class EditServerViewModel @Inject constructor(
    private val serverRepository: ServerRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val serverId: String? = savedStateHandle[SERVER_ID_ARGUMENT]
    private var originalServer: Server? = null

    private val _uiState = MutableStateFlow(
        AddServerUiState(
            title = "Edit server",
            description = "Update the connection details for this server.",
            formMessage = "Loading server details.",
        ),
    )
    val uiState: StateFlow<AddServerUiState> = _uiState.asStateFlow()

    init {
        loadServer()
    }

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
        val loadedServer = originalServer

        if (loadedServer == null) {
            _uiState.value = validatedState.copy(
                formMessage = "Server could not be loaded.",
            )
            return
        }

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
                serverRepository.saveServer(validatedState.toUpdatedServer(loadedServer))
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
                        formMessage = throwable.message ?: "Server could not be updated.",
                    )
                }
            }
        }
    }

    private fun loadServer() {
        val resolvedServerId = serverId

        if (resolvedServerId.isNullOrBlank()) {
            _uiState.update { currentState ->
                currentState.copy(formMessage = "Server id is missing.")
            }
            return
        }

        viewModelScope.launch {
            runCatching {
                serverRepository.getServerById(resolvedServerId)
            }.onSuccess { server ->
                if (server == null) {
                    _uiState.update { currentState ->
                        currentState.copy(formMessage = "Server could not be found.")
                    }
                    return@onSuccess
                }

                originalServer = server
                _uiState.value = server.toUiState()
            }.onFailure { throwable ->
                _uiState.update { currentState ->
                    currentState.copy(
                        formMessage = throwable.message ?: "Server could not be loaded.",
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

    private fun Server.toUiState(): AddServerUiState {
        return AddServerUiState(
            title = "Edit server",
            description = "Update the connection details for this server.",
            name = name,
            host = host,
            port = sshPort.toString(),
            username = sshUsername.orEmpty(),
        )
    }

    private fun AddServerUiState.toUpdatedServer(server: Server): Server {
        return server.copy(
            name = name.trim(),
            host = host.trim(),
            sshPort = requireNotNull(port.toIntOrNull()),
            sshUsername = username.trim(),
        )
    }

    private companion object {
        const val SERVER_ID_ARGUMENT = "serverId"
    }
}
