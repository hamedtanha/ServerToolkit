package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Stable SSH host trust lookup key.
 *
 * Host trust is intentionally bound to the resolved connection endpoint and remains separate
 * from generic Server Inventory persistence.
 */
data class SshHostEndpoint(
    val serverId: String,
    val host: String,
    val port: Int,
) {
    init {
        require(serverId.isNotBlank()) {
            "Server id must not be blank."
        }
        require(host.isNotBlank()) {
            "Host must not be blank."
        }
        require(port in VALID_PORT_RANGE) {
            "SSH port must be between 1 and 65535."
        }
    }

    private companion object {
        val VALID_PORT_RANGE = 1..65535
    }
}
