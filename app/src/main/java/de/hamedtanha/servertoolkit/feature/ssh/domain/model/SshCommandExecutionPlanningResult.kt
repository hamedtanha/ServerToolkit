package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Result of validating whether a non-interactive SSH command can be executed.
 *
 * Planning remains separate from execution so session-handle validation and failure mapping stay
 * project-owned instead of leaking SSHJ objects outside the data layer.
 */
sealed interface SshCommandExecutionPlanningResult {

    data class Planned(
        val plan: SshCommandExecutionPlan,
    ) : SshCommandExecutionPlanningResult

    data class Failed(
        val error: SshCommandExecutionError,
    ) : SshCommandExecutionPlanningResult
}
