package de.hamedtanha.servertoolkit.feature.ssh.domain.usecase

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.DEFAULT_SSH_COMMAND_TIMEOUT_MILLIS
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshCommandExecutionService
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

class SshCommandExecutionUseCase @Inject constructor(
    private val commandExecutionService: SshCommandExecutionService,
) {

    suspend operator fun invoke(
        sessionHandle: SshSessionHandle,
        command: String,
        timeoutMillis: Long = DEFAULT_SSH_COMMAND_TIMEOUT_MILLIS,
    ): SshCommandExecutionResult {
        return try {
            val request = SshCommandRequest(
                sessionHandle = sessionHandle,
                command = command,
                timeoutMillis = timeoutMillis,
            )

            withTimeout(timeoutMillis) {
                commandExecutionService.execute(request)
            }
        } catch (error: TimeoutCancellationException) {
            SshCommandExecutionResult.Failed(SshCommandExecutionError.CommandTimedOut)
        } catch (error: CancellationException) {
            throw error
        } catch (error: IllegalArgumentException) {
            SshCommandExecutionResult.Failed(SshCommandExecutionError.UnsupportedConfiguration)
        } catch (error: Exception) {
            SshCommandExecutionResult.Failed(SshCommandExecutionError.Unknown)
        }
    }
}
