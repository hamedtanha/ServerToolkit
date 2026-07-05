package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshCommandExecutionService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * SSHJ-backed command execution service.
 *
 * This service resolves command execution through the data-layer session owner registry and keeps
 * blocking SSHJ command/channel lifecycle work on an I/O dispatcher.
 */
class SshjCommandExecutionService @Inject constructor(
    private val sessionOwnerRegistry: SshjSessionOwnerRegistry,
) : SshCommandExecutionService {

    override suspend fun execute(request: SshCommandRequest): SshCommandExecutionResult {
        return try {
            withContext(Dispatchers.IO) {
                sessionOwnerRegistry.execute(request)
            }
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            SshCommandExecutionResult.Failed(SshCommandExecutionError.Unknown)
        }
    }
}
