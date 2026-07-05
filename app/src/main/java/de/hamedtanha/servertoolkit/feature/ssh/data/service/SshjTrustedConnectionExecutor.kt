package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.userauth.UserAuthException

/**
 * Opens an SSHJ transport connection only with a trusted host-key verifier installed.
 *
 * Authentication is executed inside this boundary while the SSHJ client is still connected. The
 * client is still closed before returning because long-lived session ownership is intentionally not
 * enabled in this implementation gate.
 */
internal interface SshjTrustedConnectionExecutor {

    fun connectAndAuthenticate(
        request: SshConnectionRequest,
        trustedHostKey: SshTrustedHostKey,
        authenticationMapping: SshjAuthenticationMapping,
    ): SshjTrustedConnectionExecutionResult
}

internal sealed interface SshjTrustedConnectionExecutionResult {

    data object Authenticated : SshjTrustedConnectionExecutionResult

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
        return try {
            SSHClient().use { client ->
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
                        SshjTrustedConnectionExecutionResult.Authenticated
                    }

                    is SshjAuthenticationExecutionResult.Failed -> {
                        SshjTrustedConnectionExecutionResult.Failed(result.error)
                    }
                }
            }
        } catch (error: UnknownHostException) {
            SshjTrustedConnectionExecutionResult.Failed(SshConnectionError.UnknownHost)
        } catch (error: SocketTimeoutException) {
            SshjTrustedConnectionExecutionResult.Failed(SshConnectionError.ConnectionTimeout)
        } catch (error: IOException) {
            SshjTrustedConnectionExecutionResult.Failed(SshConnectionError.Unknown)
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

private const val SSHJ_TRUSTED_CONNECTION_TIMEOUT_MILLIS = 10_000
