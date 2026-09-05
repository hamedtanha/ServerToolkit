package de.hamedtanha.servertoolkit.feature.ssh.test

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshConnectionService

class FakeSshConnectionService(
    var result: SshConnectionResult,
    private val onConnect: suspend (SshConnectionRequest) -> Unit = {},
    private val onDiscardUndeliveredSession: (SshSessionHandle) -> Unit = {},
) : SshConnectionService {

    var lastRequest: SshConnectionRequest? = null
        private set

    var connectCallCount: Int = 0
        private set

    val discardedUndeliveredSessions = mutableListOf<SshSessionHandle>()

    override suspend fun connect(request: SshConnectionRequest): SshConnectionResult {
        connectCallCount += 1
        lastRequest = request
        onConnect(request)
        return result
    }

    override fun discardUndeliveredSession(sessionHandle: SshSessionHandle) {
        discardedUndeliveredSessions += sessionHandle
        onDiscardUndeliveredSession(sessionHandle)
    }
}
