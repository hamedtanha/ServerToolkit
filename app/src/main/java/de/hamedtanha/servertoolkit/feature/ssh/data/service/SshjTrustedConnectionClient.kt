package de.hamedtanha.servertoolkit.feature.ssh.data.service

import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import net.schmizz.sshj.userauth.UserAuthException

/**
 * Creates trusted-connection clients behind a project-owned boundary.
 *
 * This wrapper preserves SshjClientFactory as the source of real SSHJ clients while allowing
 * SshjNetworkTrustedConnectionExecutor cleanup and ownership behavior to be unit-tested.
 */
internal class SshjDefaultTrustedConnectionClientFactory(
    private val clientFactory: SshjClientFactory = SshjClientFactory(),
) : SshjTrustedConnectionClientFactory {

    override fun createClient(): SshjTrustedConnectionClient {
        return SshjTrustedConnectionClientAdapter(clientFactory.createClient())
    }
}

/**
 * Creates trusted-connection SSHJ clients behind a project-owned boundary.
 */
internal fun interface SshjTrustedConnectionClientFactory {

    fun createClient(): SshjTrustedConnectionClient
}

/**
 * Minimal trusted-connection client capability required by SshjNetworkTrustedConnectionExecutor.
 */
internal interface SshjTrustedConnectionClient : SshjAuthenticatedClient {

    var connectTimeout: Int

    var timeout: Int

    fun addHostKeyVerifier(verifier: HostKeyVerifier)

    fun connect(
        host: String,
        port: Int,
    )

    fun close()

    fun asCommandChannelClient(): SshjCommandChannelClient
}

private class SshjTrustedConnectionClientAdapter(
    private val client: SSHClient,
) : SshjTrustedConnectionClient {

    override var connectTimeout: Int
        get() = client.connectTimeout
        set(value) {
            client.connectTimeout = value
        }

    override var timeout: Int
        get() = client.timeout
        set(value) {
            client.timeout = value
        }

    override fun addHostKeyVerifier(verifier: HostKeyVerifier) {
        client.addHostKeyVerifier(verifier)
    }

    override fun connect(
        host: String,
        port: Int,
    ) {
        client.connect(host, port)
    }

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

    override fun close() {
        client.close()
    }

    override fun asCommandChannelClient(): SshjCommandChannelClient {
        return SshjCommandChannelClientAdapter(client)
    }
}
