package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshSessionLifecycleService
import javax.inject.Inject

/**
 * SSHJ-backed session lifecycle service.
 *
 * Real session opening is intentionally not implemented yet. This service delegates close requests
 * to the data-layer owner registry so future SSHJ session cleanup remains outside ViewModels and
 * outside domain/presentation models.
 */
class SshjSessionLifecycleService @Inject constructor(
    private val sessionOwnerRegistry: SshjSessionOwnerRegistry,
) : SshSessionLifecycleService {

    override suspend fun close(sessionHandle: SshSessionHandle): SshSessionCloseResult {
        return sessionOwnerRegistry.close(sessionHandle)
    }
}
