package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshSessionLifecycleService
import javax.inject.Inject

/**
 * SSHJ-backed session lifecycle shell.
 *
 * Real session registration is intentionally not implemented yet. Until a dedicated session owner
 * is added, close requests cannot resolve a live SSHJ session and therefore return NotFound.
 */
class SshjSessionLifecycleService @Inject constructor() : SshSessionLifecycleService {

    override suspend fun close(sessionHandle: SshSessionHandle): SshSessionCloseResult {
        return SshSessionCloseResult.NotFound
    }
}
