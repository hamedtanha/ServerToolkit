package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

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

    internal fun execute(request: SshCommandRequest): SshCommandExecutionResult {
        val owner = synchronized(this) {
            sessionOwners[request.sessionHandle.sessionId]
        } ?: return SshCommandExecutionResult.Failed(SshCommandExecutionError.SessionNotFound)

        return owner.execute(request)
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
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            SshSessionCloseResult.Failed
        }
    }
}

/**
 * Data-layer owner for a live SSHJ-backed session resource.
 *
 * The close action wraps concrete SSHJ cleanup while keeping SSHJ clients, sessions, sockets, and
 * future command channels out of domain, presentation, and ViewModel code.
 */
internal class SshjSessionOwner(
    val sessionHandle: SshSessionHandle,
    private val closeAction: () -> Unit,
    private val commandExecutionAction: (SshCommandRequest) -> SshCommandExecutionResult = {
        SshCommandExecutionResult.Failed(SshCommandExecutionError.UnsupportedConfiguration)
    },
) {

    fun execute(request: SshCommandRequest): SshCommandExecutionResult {
        return commandExecutionAction(request)
    }

    fun close() {
        closeAction()
    }
}
