package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns data-layer SSH session resources by project-owned session id.
 *
 * This registry is intentionally kept in the SSHJ data layer. Domain, presentation, and ViewModel
 * code must only work with SshSessionHandle and must never own SSHJ clients, sessions, sockets, or
 * command channels directly.
 */
@Singleton
class SshjSessionOwnerRegistry @Inject constructor() {

    private val sessionOwners = mutableMapOf<String, SshjSessionOwner>()

    internal fun register(owner: SshjSessionOwner): Boolean {
        return synchronized(this) {
            val sessionId = owner.sessionHandle.sessionId
            if (sessionOwners.containsKey(sessionId)) {
                false
            } else {
                sessionOwners[sessionId] = owner
                true
            }
        }
    }

    internal fun contains(sessionHandle: SshSessionHandle): Boolean {
        return synchronized(this) {
            sessionOwners.containsKey(sessionHandle.sessionId)
        }
    }

    internal fun close(sessionHandle: SshSessionHandle): SshSessionCloseResult {
        val owner = synchronized(this) {
            sessionOwners[sessionHandle.sessionId]
        } ?: return SshSessionCloseResult.NotFound

        return try {
            owner.close()
            synchronized(this) {
                if (sessionOwners[sessionHandle.sessionId] === owner) {
                    sessionOwners.remove(sessionHandle.sessionId)
                }
            }
            SshSessionCloseResult.Closed
        } catch (error: Exception) {
            SshSessionCloseResult.Failed
        }
    }
}

/**
 * Data-layer owner for a live SSHJ-backed session resource.
 *
 * The close action will later wrap SSHJ client/session cleanup. It is modeled as a callback now so
 * the ownership and cleanup contract can be tested before real network sessions are enabled.
 */
internal class SshjSessionOwner(
    val sessionHandle: SshSessionHandle,
    private val closeAction: () -> Unit,
) {

    fun close() {
        closeAction()
    }
}
