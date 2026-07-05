package de.hamedtanha.servertoolkit.feature.ssh.test

import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.resolver.ConnectionTargetResolver

class FakeConnectionTargetResolver(
    var resolution: ConnectionTargetResolution,
    private val onResolve: suspend (String) -> Unit = {},
) : ConnectionTargetResolver {

    var requestedServerId: String? = null
        private set

    var resolveCallCount: Int = 0
        private set

    override suspend fun resolve(serverId: String): ConnectionTargetResolution {
        resolveCallCount += 1
        requestedServerId = serverId
        onResolve(serverId)
        return resolution
    }
}
