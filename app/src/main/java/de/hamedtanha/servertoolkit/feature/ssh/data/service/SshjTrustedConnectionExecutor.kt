package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.UUID
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.userauth.UserAuthException

/**
 * Opens an SSHJ transport connection only with a trusted host-key verifier installed.
 *
 * After successful authentication, this executor transfers SSHJ client ownership to an
 * SshjSessionOwner. It must close the client on every failure path and must not expose SSHJ types
 * outside the data-layer boundary.
 */
internal interface SshjTrustedConnectionExecutor {

    fun connectAndAuthenticate(
        request: SshConnectionRequest,
        trustedHostKey: SshTrustedHostKey,
        authenticationMapping: SshjAuthenticationMapping,
    ): SshjTrustedConnectionExecutionResult
}

internal sealed interface SshjTrustedConnectionExecutionResult {

    data class Connected(
        val sessionOwner: SshjSessionOwner,
    ) : SshjTrustedConnectionExecutionResult

    data class Failed(
        val error: SshConnectionError,
    ) : SshjTrustedConnectionExecutionResult
}

internal class SshjNetworkTrustedConnectionExecutor(
    private val trustedHostKeyVerifierFactory: SshjTrustedHostKeyVerifierFactory,
    private val authenticationExecutor: SshjAuthenticationExecutor,
) : SshjTrustedConnectionExecutor {

    override fun connectAndAuthenticate(
        request: SshConnectionRequest,
        trustedHostKey: SshTrustedHostKey,
        authenticationMapping: SshjAuthenticationMapping,
    ): SshjTrustedConnectionExecutionResult {
        val client = SSHClient()
        var ownershipTransferred = false

        return try {
            client.connectTimeout = SSHJ_TRUSTED_CONNECTION_TIMEOUT_MILLIS
            client.timeout = SSHJ_TRUSTED_CONNECTION_TIMEOUT_MILLIS
            client.addHostKeyVerifier(
                trustedHostKeyVerifierFactory.create(trustedHostKey),
            )
            client.connect(request.host, request.port)

            when (
                val result = authenticationExecutor.authenticate(
                    client = SshjAuthenticatedClientAdapter(client),
                    mapping = authenticationMapping,
                )
            ) {
                SshjAuthenticationExecutionResult.Authenticated -> {
                    val sessionOwner = SshjSessionOwner(
                        sessionHandle = request.toSessionHandle(),
                        closeAction = {
                            client.close()
                        },
                        commandExecutionAction = {
                            SshCommandExecutionResult.Failed(
                                SshCommandExecutionError.UnsupportedConfiguration,
                            )
                        },
                    )
                    ownershipTransferred = true
                    SshjTrustedConnectionExecutionResult.Connected(sessionOwner)
                }

                is SshjAuthenticationExecutionResult.Failed -> {
                    SshjTrustedConnectionExecutionResult.Failed(result.error)
                }
            }
        } catch (error: UnknownHostException) {
            SshjTrustedConnectionExecutionResult.Failed(SshConnectionError.UnknownHost)
        } catch (error: SocketTimeoutException) {
            SshjTrustedConnectionExecutionResult.Failed(SshConnectionError.ConnectionTimeout)
        } catch (error: IOException) {
            SshjTrustedConnectionExecutionResult.Failed(SshConnectionError.Unknown)
        } finally {
            if (!ownershipTransferred) {
                runCatching {
                    client.close()
                }
            }
        }
    }
}

private class SshjAuthenticatedClientAdapter(
    private val client: SSHClient,
) : SshjAuthenticatedClient {

    override fun authPassword(
        username: String,
        password: String,
    ) {
        try {
            client.authPassword(username, password)
        } catch (error: UserAuthException) {
            throw SshjAuthenticationFailedException(error)
        }
    }
}

private fun SshConnectionRequest.toSessionHandle(): SshSessionHandle {
    return SshSessionHandle(
        sessionId = UUID.randomUUID().toString(),
        serverId = serverId,
        host = host,
        port = port,
        username = username,
    )
}

private const val SSHJ_TRUSTED_CONNECTION_TIMEOUT_MILLIS = 10_000
