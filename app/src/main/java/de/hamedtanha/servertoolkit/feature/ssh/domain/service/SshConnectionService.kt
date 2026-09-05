package de.hamedtanha.servertoolkit.feature.ssh.domain.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle

/**
 * Project-owned contract for attempting SSH connections.
 *
 * Implementations must hide third-party SSH library types behind this interface.
 *
 * A connected session remains rollback-capable until its handle has been safely delivered to the
 * workflow owner. Callers that lose a connected result to cancellation or timeout must discard that
 * undelivered session through [discardUndeliveredSession].
 */
interface SshConnectionService {

    suspend fun connect(request: SshConnectionRequest): SshConnectionResult

    /**
     * Best-effort rollback for a connected session that was never delivered to its workflow owner.
     *
     * This operation is distinct from normal user/session lifecycle close semantics. Implementations
     * must remove application ownership of the abandoned session and must not let cleanup failure
     * replace the caller's primary cancellation, timeout, or failure outcome.
     */
    fun discardUndeliveredSession(sessionHandle: SshSessionHandle)
}
