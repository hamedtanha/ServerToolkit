package de.hamedtanha.servertoolkit.feature.ssh.domain.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult

/**
 * Project-owned contract for observing a remote SSH host key before authentication.
 *
 * Implementations must not authenticate or open long-lived sessions as part of observation.
 */
interface SshHostKeyObservationService {

    suspend fun observeHostKey(request: SshConnectionRequest): SshHostKeyObservationResult
}
