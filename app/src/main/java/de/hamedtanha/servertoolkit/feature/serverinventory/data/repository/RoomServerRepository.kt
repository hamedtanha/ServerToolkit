package de.hamedtanha.servertoolkit.feature.serverinventory.data.repository

import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.dao.ServerDao
import de.hamedtanha.servertoolkit.feature.serverinventory.data.mapper.toDomain
import de.hamedtanha.servertoolkit.feature.serverinventory.data.mapper.toEntity
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class RoomServerRepository @Inject constructor(
    private val serverDao: ServerDao,
) : ServerRepository {

    override fun observeServers(): Flow<List<Server>> {
        return serverDao.observeServers().map { entities ->
            entities.map { entity ->
                entity.toDomain()
            }
        }
    }

    override suspend fun getServerById(serverId: String): Server? {
        return serverDao.getServerById(serverId)?.toDomain()
    }

    override suspend fun saveServer(server: Server) {
        serverDao.upsertServer(server.toEntity())
    }

    override suspend fun deleteServer(serverId: String) {
        serverDao.deleteServer(serverId)
    }
}
