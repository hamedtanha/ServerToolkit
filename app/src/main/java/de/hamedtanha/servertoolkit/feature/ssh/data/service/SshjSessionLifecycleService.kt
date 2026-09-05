package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshSessionLifecycleService
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * SSHJ-backed session lifecycle service.
 *
 * This service delegates close requests to the data-layer owner registry so SSHJ session cleanup
 * remains outside ViewModels and outside domain/presentation models.
 */
class SshjSessionLifecycleService @Inject constructor(
    private val sessionOwnerRegistry: SshjSessionOwnerRegistry,
) : SshSessionLifecycleService {

    /**
     * Process-lifetime cleanup scope for sessions whose workflow owner has permanently disappeared.
     *
     * The service is singleton-scoped by [de.hamedtanha.servertoolkit.feature.ssh.di.SshModule].
     * This scope exists only to finish local resource cleanup after ViewModel destruction; it does
     * not provide background SSH session continuity.
     */
    private val abandonmentScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override suspend fun close(sessionHandle: SshSessionHandle): SshSessionCloseResult {
        return withContext(NonCancellable) {
            withContext(Dispatchers.IO) {
                sessionOwnerRegistry.close(sessionHandle)
            }
        }
    }

    override fun abandon(sessionHandle: SshSessionHandle) {
        abandonmentScope.launch {
            sessionOwnerRegistry.discard(sessionHandle)
        }
    }
}
