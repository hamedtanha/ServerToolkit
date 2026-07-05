package de.hamedtanha.servertoolkit.feature.ssh.domain.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest

/**
 * Project-owned contract for non-interactive SSH command execution.
 *
 * Implementations must hide SSHJ clients, sessions, channels, streams, sockets, and exceptions
 * behind this interface.
 */
interface SshCommandExecutionService {

    suspend fun execute(request: SshCommandRequest): SshCommandExecutionResult
}
