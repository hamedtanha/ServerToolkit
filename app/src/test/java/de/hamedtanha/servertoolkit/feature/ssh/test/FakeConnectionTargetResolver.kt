package de.hamedtanha.servertoolkit.feature.ssh.test

import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.resolver.ConnectionTargetResolver

class FakeConnectionTargetResolver(
    var resolution: ConnectionTargetResolution,
) : ConnectionTargetResolver {

    override suspend fun resolve(serverId: String): ConnectionTargetResolution {
        return resolution
    }
}
