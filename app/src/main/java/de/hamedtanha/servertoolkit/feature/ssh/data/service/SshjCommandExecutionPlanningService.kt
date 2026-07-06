package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionPlan
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionPlanningResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshCommandExecutionPlanningService
import javax.inject.Inject

/**
 * SSHJ-backed command execution planning service.
 *
 * This service verifies that a project-owned session handle resolves to a data-layer owned SSH
 * session before execution opens a short-lived command channel.
 */
class SshjCommandExecutionPlanningService @Inject constructor(
    sessionOwnerRegistry: SshjSessionOwnerRegistry,
) : SshCommandExecutionPlanningService {

    private val commandChannelPlanner: SshjCommandChannelPlanner =
        SshjSessionRegistryCommandChannelPlanner(sessionOwnerRegistry)

    override suspend fun plan(request: SshCommandRequest): SshCommandExecutionPlanningResult {
        return when (val result = commandChannelPlanner.plan(request)) {
            is SshjCommandChannelPlanningResult.Planned -> {
                SshCommandExecutionPlanningResult.Planned(
                    SshCommandExecutionPlan(
                        sessionHandle = result.plan.sessionHandle,
                        command = result.plan.command,
                        timeoutMillis = result.plan.timeoutMillis,
                    ),
                )
            }

            is SshjCommandChannelPlanningResult.Failed -> {
                SshCommandExecutionPlanningResult.Failed(result.error)
            }
        }
    }
}
