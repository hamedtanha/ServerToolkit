package de.hamedtanha.servertoolkit.feature.ssh.test

import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeyMaterial
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshPrivateKeySourceResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshPrivateKeySource

class TrackingSshPrivateKeySource : SshPrivateKeySource {

    var invalidateCallCount: Int = 0
        private set

    var isInvalidated: Boolean = false
        private set

    override suspend fun <T> consume(
        block: suspend SshPrivateKeyMaterial.() -> T,
    ): SshPrivateKeySourceResult<T> {
        error("Consumption is not expected in ownership-boundary tests.")
    }

    override fun invalidate(): Boolean {
        if (isInvalidated) {
            return false
        }

        isInvalidated = true
        invalidateCallCount += 1
        return true
    }

    override fun toString(): String {
        return "TrackingSshPrivateKeySource(REDACTED)"
    }
}
