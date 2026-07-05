package de.hamedtanha.servertoolkit.feature.ssh.domain.repository

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey

interface SshHostTrustRepository {

    suspend fun getTrustedHostKey(endpoint: SshHostEndpoint): SshTrustedHostKey?

    suspend fun saveTrustedHostKey(trustedHostKey: SshTrustedHostKey)

    suspend fun removeTrustedHostKey(endpoint: SshHostEndpoint)
}
