package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshHostKeyObservationService
import javax.inject.Inject

/**
 * SSHJ-backed host-key observation shell.
 *
 * This class intentionally does not open a network connection yet. It exists so the connection
 * attempt boundary can depend on project-owned host-key observation behavior before real SSH is
 * enabled.
 */
class SshjHostKeyObservationService @Inject constructor() : SshHostKeyObservationService {

    override suspend fun observeHostKey(request: SshConnectionRequest): SshHostKeyObservationResult {
        return SshHostKeyObservationResult.Unavailable
    }
}
