package de.hamedtanha.servertoolkit.feature.ssh.domain.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult

/**
 * Project-owned contract for attempting SSH connections.
 *
 * Implementations must hide third-party SSH library types behind this interface.
 */
interface SshConnectionService {
    suspend fun connect(request: SshConnectionRequest): SshConnectionResult
}
