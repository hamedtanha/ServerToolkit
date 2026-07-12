package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeySourceError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeySourceResult
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

    internal suspend fun authenticate(
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

            is SshjAuthenticationMapping.PrivateKey -> {
                authenticatePrivateKey(
                    client = client,
                    mapping = mapping,
                )
            }
        }
    }

    private suspend fun authenticatePrivateKey(
        client: SshjAuthenticatedClient,
        mapping: SshjAuthenticationMapping.PrivateKey,
    ): SshjAuthenticationExecutionResult {
        val source = mapping.takePrivateKeySource()
            ?: return SshjAuthenticationExecutionResult.Failed(
                SshConnectionError.PrivateKeyUnavailable,
            )

        return try {
            when (
                val sourceResult = source.consume {
                    useBytes { privateKeyBytes, privateKeySize ->
                        client.authPrivateKey(
                            username = mapping.username,
                            privateKeyBytes = privateKeyBytes,
                            privateKeySize = privateKeySize,
                            passphrase = mapping.passphrase,
                        )
                    }
                }
            ) {
                is SshPrivateKeySourceResult.Success -> {
                    SshjAuthenticationExecutionResult.Authenticated
                }

                is SshPrivateKeySourceResult.Failure -> {
                    SshjAuthenticationExecutionResult.Failed(
                        sourceResult.error.toConnectionError(),
                    )
                }
            }
        } catch (_: SshjPrivateKeyAuthenticationException.UnsupportedFormat) {
            SshjAuthenticationExecutionResult.Failed(
                SshConnectionError.PrivateKeyUnsupportedFormat,
            )
        } catch (_: SshjPrivateKeyAuthenticationException.InvalidKey) {
            SshjAuthenticationExecutionResult.Failed(SshConnectionError.PrivateKeyInvalid)
        } catch (_: SshjPrivateKeyAuthenticationException.PassphraseRequired) {
            SshjAuthenticationExecutionResult.Failed(
                SshConnectionError.PrivateKeyPassphraseRequired,
            )
        } catch (_: SshjPrivateKeyAuthenticationException.PassphraseRejected) {
            SshjAuthenticationExecutionResult.Failed(
                SshConnectionError.PrivateKeyPassphraseRejected,
            )
        } catch (_: SshjPrivateKeyAuthenticationException.AuthenticationRejected) {
            SshjAuthenticationExecutionResult.Failed(SshConnectionError.AuthenticationRejected)
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            SshjAuthenticationExecutionResult.Failed(SshConnectionError.Unknown)
        }
    }
}

private fun SshPrivateKeySourceError.toConnectionError(): SshConnectionError {
    return when (this) {
        SshPrivateKeySourceError.AlreadyConsumed,
        SshPrivateKeySourceError.Invalidated,
        SshPrivateKeySourceError.DocumentUnavailable,
        SshPrivateKeySourceError.ReadFailed,
        -> SshConnectionError.PrivateKeyUnavailable

        SshPrivateKeySourceError.EmptyDocument -> SshConnectionError.PrivateKeyEmpty
        SshPrivateKeySourceError.DocumentTooLarge -> SshConnectionError.PrivateKeyTooLarge
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

    fun authPrivateKey(
        username: String,
        privateKeyBytes: ByteArray,
        privateKeySize: Int,
        passphrase: String,
    )
}

/**
 * Data-layer authentication failure used to isolate SSHJ-specific authentication exceptions.
 */
internal class SshjAuthenticationFailedException(
    cause: Throwable? = null,
) : RuntimeException(cause)

/**
 * Stable data-layer private-key authentication failures.
 *
 * SSHJ exception types must not escape through the project-owned authentication boundary.
 */
internal sealed class SshjPrivateKeyAuthenticationException(
    cause: Throwable? = null,
) : RuntimeException(cause) {

    class UnsupportedFormat(
        cause: Throwable? = null,
    ) : SshjPrivateKeyAuthenticationException(cause)

    class InvalidKey(
        cause: Throwable? = null,
    ) : SshjPrivateKeyAuthenticationException(cause)

    class PassphraseRequired(
        cause: Throwable? = null,
    ) : SshjPrivateKeyAuthenticationException(cause)

    class PassphraseRejected(
        cause: Throwable? = null,
    ) : SshjPrivateKeyAuthenticationException(cause)

    class AuthenticationRejected(
        cause: Throwable? = null,
    ) : SshjPrivateKeyAuthenticationException(cause)
}
