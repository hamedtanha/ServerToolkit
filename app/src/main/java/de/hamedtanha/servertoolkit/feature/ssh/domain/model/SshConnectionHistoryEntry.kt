package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Non-sensitive domain record for an SSH connection attempt.
 *
 * This model intentionally stores connection metadata and result classification only. It must not
 * contain passwords, passphrases, private keys, SSHJ clients, sockets, sessions, or command output.
 */
data class SshConnectionHistoryEntry(
    val id: String,
    val serverId: String,
    val host: String,
    val port: Int,
    val username: String,
    val status: SshConnectionHistoryStatus,
    val attemptedAtEpochMillis: Long,
    val completedAtEpochMillis: Long? = null,
    val connectionError: SshConnectionError? = null,
) {
    init {
        require(id.isNotBlank()) {
            "SSH connection history id must not be blank."
        }
        require(serverId.isNotBlank()) {
            "SSH connection history server id must not be blank."
        }
        require(host.isNotBlank()) {
            "SSH connection history host must not be blank."
        }
        require(port in VALID_PORT_RANGE) {
            "SSH connection history port must be between 1 and 65535."
        }
        require(username.isNotBlank()) {
            "SSH connection history username must not be blank."
        }
        require(attemptedAtEpochMillis > 0) {
            "SSH connection history attempt time must be positive."
        }
        require(completedAtEpochMillis == null || completedAtEpochMillis >= attemptedAtEpochMillis) {
            "SSH connection history completion time must not be before the attempt time."
        }
        require(status != SshConnectionHistoryStatus.Failed || connectionError != null) {
            "Failed SSH connection history entries must include a connection error."
        }
        require(status == SshConnectionHistoryStatus.Failed || connectionError == null) {
            "Only failed SSH connection history entries may include a connection error."
        }
    }

    private companion object {
        val VALID_PORT_RANGE = 1..65535
    }
}
