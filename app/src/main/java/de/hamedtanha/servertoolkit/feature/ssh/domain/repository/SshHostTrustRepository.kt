package de.hamedtanha.servertoolkit.feature.ssh.domain.repository

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey

interface SshHostTrustRepository {

    suspend fun getTrustedHostKey(endpoint: SshHostEndpoint): SshTrustedHostKey?

    /**
     * Persists a host key that has been explicitly trusted by the user.
     *
     * Implementations must not silently replace an existing trusted host key for the same endpoint.
     * A changed host key requires a separate explicit replacement flow.
     */
    suspend fun saveTrustedHostKey(trustedHostKey: SshTrustedHostKey)

    suspend fun removeTrustedHostKey(endpoint: SshHostEndpoint)
}
