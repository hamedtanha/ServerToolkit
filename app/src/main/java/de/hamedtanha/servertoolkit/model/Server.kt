package de.hamedtanha.servertoolkit.model
data class Server(
    val id: String,
    val name: String,
    val host: String,
    val sshPort: Int = 22,
    val username: String,
    val description: String = "",
    val favorite: Boolean = false,
    val enabled: Boolean = true
)