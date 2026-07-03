package de.hamedtanha.servertoolkit.feature.serverinventory.data.repository

import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@Singleton
class InMemoryServerRepository @Inject constructor() : ServerRepository {

    private val servers = MutableStateFlow<List<Server>>(emptyList())

    override fun observeServers(): Flow<List<Server>> {
        return servers.asStateFlow()
    }

    override suspend fun getServerById(serverId: String): Server? {
        return servers.value.firstOrNull { server ->
            server.id == serverId
        }
    }

    override suspend fun saveServer(server: Server) {
        servers.update { currentServers ->
            val existingServerIndex = currentServers.indexOfFirst { existingServer ->
                existingServer.id == server.id
            }

            if (existingServerIndex == -1) {
                currentServers + server
            } else {
                currentServers.toMutableList().also { updatedServers ->
                    updatedServers[existingServerIndex] = server
                }
            }
        }
    }

    override suspend fun deleteServer(serverId: String) {
        servers.update { currentServers ->
            currentServers.filterNot { server ->
                server.id == serverId
            }
        }
    }
}
