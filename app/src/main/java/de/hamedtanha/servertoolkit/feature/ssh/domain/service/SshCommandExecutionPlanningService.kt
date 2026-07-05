package de.hamedtanha.servertoolkit.feature.ssh.domain.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionPlanningResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest

/**
 * Project-owned contract for planning non-interactive SSH command execution.
 *
 * Implementations must hide SSHJ clients, sessions, channels, streams, sockets, and exceptions
 * behind this interface.
 */
interface SshCommandExecutionPlanningService {

    suspend fun plan(request: SshCommandRequest): SshCommandExecutionPlanningResult
}
