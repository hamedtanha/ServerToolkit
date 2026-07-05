package de.hamedtanha.servertoolkit.core.connection.domain.model

/**
 * Non-sensitive connection metadata resolved from application-owned inventory.
 *
 * Authentication input, credentials, trusted host keys, and session state are intentionally
 * excluded from this model.
 */
data class RemoteConnectionTarget(
    val serverId: String,
    val host: String,
    val port: Int,
    val username: String,
)
