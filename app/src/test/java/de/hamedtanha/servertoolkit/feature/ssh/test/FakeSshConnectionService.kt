package de.hamedtanha.servertoolkit.feature.ssh.test

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshConnectionService

class FakeSshConnectionService(
    var result: SshConnectionResult,
) : SshConnectionService {

    var lastRequest: SshConnectionRequest? = null
        private set

    override suspend fun connect(request: SshConnectionRequest): SshConnectionResult {
        lastRequest = request
        return result
    }
}
