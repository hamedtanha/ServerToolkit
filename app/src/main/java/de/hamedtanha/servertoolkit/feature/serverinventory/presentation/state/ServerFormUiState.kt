package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state

data class ServerFormUiState(
    val title: String = "Add server",
    val description: String = "Enter the connection details for this server.",
    val name: String = "",
    val host: String = "",
    val port: String = "22",
    val username: String = "",
    val nameError: String? = null,
    val hostError: String? = null,
    val portError: String? = null,
    val usernameError: String? = null,
    val formMessage: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
) {
    val canSave: Boolean
        get() = !isSaving &&
            name.isNotBlank() &&
            host.isNotBlank() &&
            username.isNotBlank() &&
            port.toIntOrNull()?.let { it in MIN_PORT..MAX_PORT } == true &&
            nameError == null &&
            hostError == null &&
            portError == null &&
            usernameError == null

    companion object {
        const val MIN_PORT = 1
        const val MAX_PORT = 65535
    }
}
