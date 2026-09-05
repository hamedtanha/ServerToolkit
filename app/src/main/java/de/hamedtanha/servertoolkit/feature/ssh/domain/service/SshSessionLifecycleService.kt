package de.hamedtanha.servertoolkit.feature.ssh.domain.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle

/**
 * Project-owned contract for SSH session lifecycle operations.
 *
 * Implementations own third-party SSH session objects internally. ViewModels and UI state must not
 * own SSHJ sessions, sockets, command channels, or credential values.
 */
interface SshSessionLifecycleService {

    /**
     * Closes a session while its workflow owner still exists and can observe or retry cleanup.
     */
    suspend fun close(sessionHandle: SshSessionHandle): SshSessionCloseResult

    /**
     * Transfers a session to best-effort cleanup after its workflow owner is permanently gone.
     *
     * This operation must not block an Android lifecycle callback. Implementations must schedule
     * cleanup independently from the cleared workflow scope and must not retain an unreachable
     * session owner merely to preserve retry semantics that no longer exist.
     */
    fun abandon(sessionHandle: SshSessionHandle)
}
