package de.hamedtanha.servertoolkit.feature.ssh.data.service

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionError
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshConnectionService
import javax.inject.Inject
import net.schmizz.sshj.SSHClient

/**
 * SSHJ-backed connection service shell.
 *
 * This class intentionally does not open a network connection yet. It establishes the data-layer
 * boundary that will later contain SSHJ-specific connection logic while keeping SSHJ types out of
 * domain and presentation code.
 */
class SshjConnectionService @Inject constructor(
    private val authenticationAdapter: SshjAuthenticationAdapter,
) : SshConnectionService {

    override suspend fun connect(request: SshConnectionRequest): SshConnectionResult {
        val authenticationMapping = authenticationAdapter.map(request)

        return try {
            SSHClient().use {
                SshConnectionResult.Failed(SshConnectionError.UnsupportedConfiguration)
            }
        } finally {
            authenticationMapping.clearSensitiveValues()
        }
    }
}
