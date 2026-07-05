package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Project-owned SSH session handle.
 *
 * This handle contains only non-sensitive session metadata. It must not expose SSHJ session
 * objects, sockets, credentials, private keys, passphrases, or command execution channels.
 */
data class SshSessionHandle(
    val sessionId: String,
    val serverId: String,
    val host: String,
    val port: Int,
    val username: String,
) {

    init {
        require(sessionId.isNotBlank()) {
            "SSH session id must not be blank."
        }
        require(serverId.isNotBlank()) {
            "SSH session server id must not be blank."
        }
        require(host.isNotBlank()) {
            "SSH session host must not be blank."
        }
        require(port in 1..65_535) {
            "SSH session port must be between 1 and 65535."
        }
        require(username.isNotBlank()) {
            "SSH session username must not be blank."
        }
    }
}
