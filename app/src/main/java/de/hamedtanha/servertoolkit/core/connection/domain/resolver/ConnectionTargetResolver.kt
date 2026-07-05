package de.hamedtanha.servertoolkit.core.connection.domain.resolver

import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution

interface ConnectionTargetResolver {

    suspend fun resolve(serverId: String): ConnectionTargetResolution
}
