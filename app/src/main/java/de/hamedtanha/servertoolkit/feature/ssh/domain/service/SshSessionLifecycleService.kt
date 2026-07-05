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

    suspend fun close(sessionHandle: SshSessionHandle): SshSessionCloseResult
}
