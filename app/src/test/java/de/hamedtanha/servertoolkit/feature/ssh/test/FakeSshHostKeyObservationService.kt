package de.hamedtanha.servertoolkit.feature.ssh.test

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshHostKeyObservationService

class FakeSshHostKeyObservationService(
    var result: SshHostKeyObservationResult = SshHostKeyObservationResult.Unavailable,
) : SshHostKeyObservationService {

    var lastRequest: SshConnectionRequest? = null
        private set

    var observeCallCount: Int = 0
        private set

    override suspend fun observeHostKey(request: SshConnectionRequest): SshHostKeyObservationResult {
        observeCallCount += 1
        lastRequest = request
        return result
    }
}
