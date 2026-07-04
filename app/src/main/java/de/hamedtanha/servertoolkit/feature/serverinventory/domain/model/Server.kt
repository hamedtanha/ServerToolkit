package de.hamedtanha.servertoolkit.feature.serverinventory.domain.model

data class Server(
    val id: String,
    val name: String,
    val host: String,
    val sshPort: Int = DEFAULT_SSH_PORT,
    val sshUsername: String? = null,
    val environment: ServerEnvironment = ServerEnvironment.UNKNOWN,
    val category: String? = null,
    val tags: List<String> = emptyList(),
    val isFavorite: Boolean = false,
    val description: String? = null,
) {
    companion object {
        const val DEFAULT_SSH_PORT = 22
    }
}
