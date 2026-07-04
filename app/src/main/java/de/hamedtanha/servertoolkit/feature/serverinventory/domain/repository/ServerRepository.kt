package de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository

import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import kotlinx.coroutines.flow.Flow

interface ServerRepository {

    fun observeServers(): Flow<List<Server>>

    suspend fun getServerById(serverId: String): Server?

    suspend fun saveServer(server: Server)

    suspend fun deleteServer(serverId: String)
}
