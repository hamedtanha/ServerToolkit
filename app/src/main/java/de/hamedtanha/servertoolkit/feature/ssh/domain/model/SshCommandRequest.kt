package de.hamedtanha.servertoolkit.feature.ssh.domain.model

const val DEFAULT_SSH_COMMAND_TIMEOUT_MILLIS: Long = 30_000
const val MAX_SSH_COMMAND_TIMEOUT_MILLIS: Long = 300_000

/**
 * Project-owned request for non-interactive SSH command execution.
 *
 * The command text must not be logged or persisted casually because users may accidentally include
 * sensitive values in operational commands.
 */
data class SshCommandRequest(
    val sessionHandle: SshSessionHandle,
    val command: String,
    val timeoutMillis: Long = DEFAULT_SSH_COMMAND_TIMEOUT_MILLIS,
) {

    init {
        require(command.isNotBlank()) {
            "SSH command must not be blank."
        }
        require(timeoutMillis in 1..MAX_SSH_COMMAND_TIMEOUT_MILLIS) {
            "SSH command timeout must be between 1 and $MAX_SSH_COMMAND_TIMEOUT_MILLIS milliseconds."
        }
    }

    override fun toString(): String {
        return "SshCommandRequest(sessionHandle=$sessionHandle, command=REDACTED, timeoutMillis=$timeoutMillis)"
    }
}
