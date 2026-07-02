package de.hamedtanha.servertoolkit.model
data class Server(
    val id: Long = 0,
    val name: String,
    val host: String,
    val sshPort: Int = 22,
    val username: String,
    val description: String = "",
    val isFavorite: Boolean = false,
    val isEnabled: Boolean = true
)