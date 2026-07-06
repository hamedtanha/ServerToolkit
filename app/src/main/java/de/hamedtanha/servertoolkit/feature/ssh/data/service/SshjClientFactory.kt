package de.hamedtanha.servertoolkit.feature.ssh.data.service

import java.security.Security
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.common.SecurityUtils
import net.schmizz.sshj.transport.kex.ECDHNistP
import org.bouncycastle.jce.provider.BouncyCastleProvider

/**
 * Creates SSHJ clients with Android-compatible cryptographic defaults.
 *
 * Android includes a limited platform provider named BC. SSHJ may select that provider by name,
 * which can fail during key exchange because the platform provider does not expose every algorithm
 * SSHJ expects. This factory installs the Bouncy Castle provider bundled with SSHJ dependencies
 * before creating SSH clients.
 */
internal class SshjClientFactory {

    fun createClient(): SSHClient {
        ensureBouncyCastleProvider()

        val config = DefaultConfig().apply {
            setKeyExchangeFactories(
                ECDHNistP.Factory256(),
            )
        }

        return SSHClient(config)
    }

    private fun ensureBouncyCastleProvider() {
        synchronized(SshjClientFactory::class.java) {
            val currentProvider = Security.getProvider(BOUNCY_CASTLE_PROVIDER_NAME)

            if (currentProvider?.javaClass?.name != BOUNCY_CASTLE_PROVIDER_CLASS_NAME) {
                Security.removeProvider(BOUNCY_CASTLE_PROVIDER_NAME)
                Security.insertProviderAt(BouncyCastleProvider(), BOUNCY_CASTLE_PROVIDER_POSITION)
            }

            SecurityUtils.setSecurityProvider(BOUNCY_CASTLE_PROVIDER_NAME)
        }
    }

    private companion object {
        const val BOUNCY_CASTLE_PROVIDER_NAME = "BC"
        const val BOUNCY_CASTLE_PROVIDER_CLASS_NAME =
            "org.bouncycastle.jce.provider.BouncyCastleProvider"
        const val BOUNCY_CASTLE_PROVIDER_POSITION = 1
    }
}
