package de.hamedtanha.servertoolkit.feature.serverinventory.data.connection

import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetInvalidReason
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.core.connection.domain.resolver.ConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository
import javax.inject.Inject

class ServerInventoryConnectionTargetResolver @Inject constructor(
    private val serverRepository: ServerRepository,
) : ConnectionTargetResolver {

    override suspend fun resolve(serverId: String): ConnectionTargetResolution {
        val server = serverRepository.getServerById(serverId)
            ?: return ConnectionTargetResolution.NotFound

        val host = server.host.trim()
        val username = server.sshUsername?.trim()

        return when {
            host.isBlank() -> ConnectionTargetResolution.Invalid(
                ConnectionTargetInvalidReason.MissingHost,
            )

            server.sshPort !in VALID_PORT_RANGE -> ConnectionTargetResolution.Invalid(
                ConnectionTargetInvalidReason.InvalidPort,
            )

            username.isNullOrBlank() -> ConnectionTargetResolution.Invalid(
                ConnectionTargetInvalidReason.MissingUsername,
            )

            else -> ConnectionTargetResolution.Resolved(
                RemoteConnectionTarget(
                    serverId = server.id,
                    host = host,
                    port = server.sshPort,
                    username = username,
                ),
            )
        }
    }

    private companion object {
        val VALID_PORT_RANGE = 1..65535
    }
}
