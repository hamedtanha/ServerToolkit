package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

/**
 * Executes SSHJ authentication against an already-connected and already-verified SSHJ client.
 *
 * This boundary intentionally does not open network connections and does not perform host-key
 * verification. Real connection setup must only call this executor after the trusted host-key
 * verifier is installed on the SSHJ client.
 */
class SshjAuthenticationExecutor @Inject constructor() {

    internal fun authenticate(
        client: SshjAuthenticatedClient,
        mapping: SshjAuthenticationMapping,
    ): SshjAuthenticationExecutionResult {
        return when (mapping) {
            is SshjAuthenticationMapping.None -> {
                SshjAuthenticationExecutionResult.Failed(SshConnectionError.AuthenticationRequired)
            }

            is SshjAuthenticationMapping.Password -> {
                if (!mapping.hasSensitiveValue) {
                    SshjAuthenticationExecutionResult.Failed(SshConnectionError.AuthenticationRequired)
                } else {
                    try {
                        client.authPassword(
                            username = mapping.username,
                            password = mapping.password,
                        )
                        SshjAuthenticationExecutionResult.Authenticated
                    } catch (error: SshjAuthenticationFailedException) {
                        SshjAuthenticationExecutionResult.Failed(SshConnectionError.AuthenticationRequired)
                    } catch (error: CancellationException) {
                        throw error
                    } catch (error: Exception) {
                        SshjAuthenticationExecutionResult.Failed(SshConnectionError.Unknown)
                    }
                }
            }

            is SshjAuthenticationMapping.PrivateKeyPassphrase -> {
                SshjAuthenticationExecutionResult.Failed(SshConnectionError.UnsupportedConfiguration)
            }
        }
    }
}

/**
 * Stable result for SSHJ authentication execution.
 *
 * The result maps third-party SSHJ failures to domain-level connection errors without exposing SSHJ
 * exception types outside the data-layer boundary.
 */
internal sealed interface SshjAuthenticationExecutionResult {

    data object Authenticated : SshjAuthenticationExecutionResult

    data class Failed(
        val error: SshConnectionError,
    ) : SshjAuthenticationExecutionResult
}

/**
 * Minimal client capability required by the authentication executor.
 *
 * This abstraction keeps executor tests independent from real network sockets and prevents the
 * executor from taking ownership of full SSHJ clients or sessions.
 */
internal interface SshjAuthenticatedClient {

    fun authPassword(
        username: String,
        password: String,
    )
}

/**
 * Data-layer authentication failure used to isolate SSHJ-specific authentication exceptions.
 */
internal class SshjAuthenticationFailedException(
    cause: Throwable? = null,
) : RuntimeException(cause)
