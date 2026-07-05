package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle

/**
 * SSHJ data-layer planner for future command channel execution.
 *
 * This planner validates ownership boundaries only. It must not open SSHJ command channels until
 * command/channel lifecycle execution is implemented in a separate reviewed slice.
 */
internal interface SshjCommandChannelPlanner {

    fun plan(request: SshCommandRequest): SshjCommandChannelPlanningResult
}

internal sealed interface SshjCommandChannelPlanningResult {

    data class Planned(
        val plan: SshjCommandChannelPlan,
    ) : SshjCommandChannelPlanningResult

    data class Failed(
        val error: SshCommandExecutionError,
    ) : SshjCommandChannelPlanningResult
}

/**
 * Data-layer command channel plan.
 *
 * This model intentionally does not contain SSHJ channel, stream, or socket objects.
 */
internal data class SshjCommandChannelPlan(
    val sessionHandle: SshSessionHandle,
    val command: String,
    val timeoutMillis: Long,
)

internal class SshjSessionRegistryCommandChannelPlanner(
    private val sessionOwnerRegistry: SshjSessionOwnerRegistry,
) : SshjCommandChannelPlanner {

    override fun plan(request: SshCommandRequest): SshjCommandChannelPlanningResult {
        return if (sessionOwnerRegistry.contains(request.sessionHandle)) {
            SshjCommandChannelPlanningResult.Planned(
                SshjCommandChannelPlan(
                    sessionHandle = request.sessionHandle,
                    command = request.command,
                    timeoutMillis = request.timeoutMillis,
                ),
            )
        } else {
            SshjCommandChannelPlanningResult.Failed(SshCommandExecutionError.SessionNotFound)
        }
    }
}
