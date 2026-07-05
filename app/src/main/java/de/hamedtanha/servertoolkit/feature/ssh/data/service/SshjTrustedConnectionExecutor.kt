package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import net.schmizz.sshj.SSHClient

/**
 * Opens an SSHJ transport connection only with a trusted host-key verifier installed.
 *
 * This executor intentionally closes the SSHJ client before returning. It proves trusted transport
 * connection execution while keeping authentication, session ownership, command execution, and
 * terminal interaction disabled for this implementation gate.
 */
internal interface SshjTrustedConnectionExecutor {

    fun connect(
        request: SshConnectionRequest,
        trustedHostKey: SshTrustedHostKey,
    ): SshjTrustedConnectionExecutionResult
}

internal sealed interface SshjTrustedConnectionExecutionResult {

    data object Connected : SshjTrustedConnectionExecutionResult

    data class Failed(
        val error: SshConnectionError,
    ) : SshjTrustedConnectionExecutionResult
}

internal class SshjNetworkTrustedConnectionExecutor(
    private val trustedHostKeyVerifierFactory: SshjTrustedHostKeyVerifierFactory,
) : SshjTrustedConnectionExecutor {

    override fun connect(
        request: SshConnectionRequest,
        trustedHostKey: SshTrustedHostKey,
    ): SshjTrustedConnectionExecutionResult {
        return try {
            SSHClient().use { client ->
                client.connectTimeout = SSHJ_TRUSTED_CONNECTION_TIMEOUT_MILLIS
                client.timeout = SSHJ_TRUSTED_CONNECTION_TIMEOUT_MILLIS
                client.addHostKeyVerifier(
                    trustedHostKeyVerifierFactory.create(trustedHostKey),
                )
                client.connect(request.host, request.port)
                SshjTrustedConnectionExecutionResult.Connected
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

private const val SSHJ_TRUSTED_CONNECTION_TIMEOUT_MILLIS = 10_000
