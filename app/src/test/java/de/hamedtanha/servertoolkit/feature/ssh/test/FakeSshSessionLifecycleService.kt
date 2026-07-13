package de.hamedtanha.servertoolkit.feature.ssh.test

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionCloseResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshSessionHandle
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshSessionLifecycleService

class FakeSshSessionLifecycleService(
    var result: SshSessionCloseResult = SshSessionCloseResult.Closed,
    private val onClose: suspend (SshSessionHandle) -> Unit = {},
) : SshSessionLifecycleService {

    var closeCallCount: Int = 0
        private set

    var lastSessionHandle: SshSessionHandle? = null
        private set

    override suspend fun close(
        sessionHandle: SshSessionHandle,
    ): SshSessionCloseResult {
        closeCallCount += 1
        lastSessionHandle = sessionHandle
        onClose(sessionHandle)
        return result
    }
}
