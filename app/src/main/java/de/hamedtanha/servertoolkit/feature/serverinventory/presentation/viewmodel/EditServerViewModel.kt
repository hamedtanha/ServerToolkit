package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerFormUiState
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
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
    private var loadJob: Job? = null

    private val _uiState = MutableStateFlow(
        ServerFormUiState(
            title = "Edit server",
            description = "Update the connection details for this server.",
            formMessage = LOADING_MESSAGE,
            isLoading = true,
        ),
    )
    val uiState: StateFlow<ServerFormUiState> = _uiState.asStateFlow()

    init {
        loadServer()
    }

    fun onRetryLoad() {
        if (originalServer == null) {
            loadServer()
        }
    }

    fun onNameChanged(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                name = name,
                nameError = validateRequiredText(name, "Server name"),
                formMessage = null,
                isSaved = false,
            )
        }
    }

    fun onHostChanged(host: String) {
        _uiState.update { currentState ->
            currentState.copy(
                host = host,
                hostError = validateRequiredText(host, "Host"),
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
                usernameError = validateRequiredText(username, "Username"),
                formMessage = null,
                isSaved = false,
            )
        }
    }

    fun onSaveClicked() {
        val validatedState = validateState(_uiState.value)
        val loadedServer = originalServer

        if (loadedServer == null) {
            _uiState.value = validatedState.copy(formMessage = LOAD_ERROR_MESSAGE)
            return
        }

        if (!validatedState.canSave) {
            _uiState.value = validatedState.copy(formMessage = "Please fix the highlighted fields.")
            return
        }

        viewModelScope.launch {
            _uiState.value = validatedState.copy(isSaving = true, formMessage = null)

            try {
                serverRepository.saveServer(validatedState.toUpdatedServer(loadedServer))
                _uiState.update { currentState ->
                    currentState.copy(isSaving = false, isSaved = true, formMessage = null)
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isSaving = false,
                        isSaved = false,
                        formMessage = UPDATE_ERROR_MESSAGE,
                    )
                }
            }
        }
    }

    private fun loadServer() {
        val resolvedServerId = serverId

        if (resolvedServerId.isNullOrBlank()) {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = false,
                    canRetryLoad = false,
                    formMessage = "Server id is missing.",
                )
            }
            return
        }

        loadJob?.cancel()

        _uiState.update { currentState ->
            currentState.copy(
                isLoading = true,
                canRetryLoad = false,
                formMessage = LOADING_MESSAGE,
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val server = serverRepository.getServerById(resolvedServerId)

                if (server == null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            canRetryLoad = false,
                            formMessage = "Server could not be found.",
                        )
                    }
                    return@launch
                }

                originalServer = server
                _uiState.value = server.toUiState()
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        canRetryLoad = true,
                        formMessage = LOAD_ERROR_MESSAGE,
                    )
                }
            }
        }
    }

    private fun validateState(state: ServerFormUiState): ServerFormUiState {
        return state.copy(
            nameError = validateRequiredText(state.name, "Server name"),
            hostError = validateRequiredText(state.host, "Host"),
            portError = validatePort(state.port),
            usernameError = validateRequiredText(state.username, "Username"),
        )
    }

    private fun validateRequiredText(value: String, fieldName: String): String? {
        return if (value.isBlank()) "$fieldName is required." else null
    }

    private fun validatePort(port: String): String? {
        val parsedPort = port.toIntOrNull()

        return when {
            port.isBlank() -> "Port is required."
            parsedPort == null -> "Port must be a number."
            parsedPort !in ServerFormUiState.MIN_PORT..ServerFormUiState.MAX_PORT ->
                "Port must be between ${ServerFormUiState.MIN_PORT} and ${ServerFormUiState.MAX_PORT}."
            else -> null
        }
    }

    private fun Server.toUiState(): ServerFormUiState {
        return ServerFormUiState(
            title = "Edit server",
            description = "Update the connection details for this server.",
            name = name,
            host = host,
            port = sshPort.toString(),
            username = sshUsername.orEmpty(),
        )
    }

    private fun ServerFormUiState.toUpdatedServer(server: Server): Server {
        return server.copy(
            name = name.trim(),
            host = host.trim(),
            sshPort = requireNotNull(port.toIntOrNull()),
            sshUsername = username.trim(),
        )
    }

    private companion object {
        const val SERVER_ID_ARGUMENT = "serverId"
        const val LOADING_MESSAGE = "Loading server details."
        const val LOAD_ERROR_MESSAGE = "Server could not be loaded."
        const val UPDATE_ERROR_MESSAGE = "Server could not be updated."
    }
}
