package de.hamedtanha.servertoolkit.feature.ssh.domain.model

/**
 * Result of planning SSH command execution.
 *
 * This is intentionally a planning result. Real command execution remains disabled until the command
 * channel lifecycle implementation is introduced and tested.
 */
sealed interface SshCommandExecutionPlanningResult {

    data class Planned(
        val plan: SshCommandExecutionPlan,
    ) : SshCommandExecutionPlanningResult

    data class Failed(
        val error: SshCommandExecutionError,
    ) : SshCommandExecutionPlanningResult
}
