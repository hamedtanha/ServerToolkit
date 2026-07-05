package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Result of non-interactive SSH command execution.
 *
 * Command output may contain sensitive operational data. Callers must avoid logging or persisting
 * this result casually.
 */
sealed interface SshCommandExecutionResult {
    data class Completed(
        val output: SshCommandExecutionOutput,
    ) : SshCommandExecutionResult

    data class Failed(
        val error: SshCommandExecutionError,
    ) : SshCommandExecutionResult
}

/**
 * Project-owned SSH command output.
 *
 * This model must not expose SSHJ sessions, channels, streams, sockets, or exceptions.
 */
data class SshCommandExecutionOutput(
    val stdout: String,
    val stderr: String,
    val exitStatus: Int?,
) {
    override fun toString(): String {
        return "SshCommandExecutionOutput(stdout=REDACTED, stderr=REDACTED, exitStatus=$exitStatus)"
    }
}
