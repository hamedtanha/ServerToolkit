package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Project-owned command execution plan.
 *
 * This model represents a validated non-interactive command execution plan. It must not expose
 * SSHJ clients, sessions, channels, streams, sockets, or credential values.
 */
data class SshCommandExecutionPlan(
    val sessionHandle: SshSessionHandle,
    val command: String,
    val timeoutMillis: Long,
) {

    init {
        require(command.isNotBlank()) {
            "SSH command plan command must not be blank."
        }
        require(timeoutMillis in 1..MAX_SSH_COMMAND_TIMEOUT_MILLIS) {
            "SSH command plan timeout must be between 1 and $MAX_SSH_COMMAND_TIMEOUT_MILLIS milliseconds."
        }
    }

    override fun toString(): String {
        return "SshCommandExecutionPlan(sessionHandle=$sessionHandle, command=REDACTED, timeoutMillis=$timeoutMillis)"
    }
}
