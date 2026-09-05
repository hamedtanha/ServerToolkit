package de.hamedtanha.servertoolkit.feature.serverinventory.test

import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.repository.ServerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeServerRepository : ServerRepository {

    var observeCallCount: Int = 0
        private set

    var observeAction: () -> Flow<List<Server>> = { flowOf(emptyList()) }
    var getByIdAction: suspend (String) -> Server? = { null }
    var saveAction: suspend (Server) -> Unit = {}
    var deleteAction: suspend (String) -> Unit = {}

    override fun observeServers(): Flow<List<Server>> {
        observeCallCount += 1
        return observeAction()
    }

    override suspend fun getServerById(serverId: String): Server? {
        return getByIdAction(serverId)
    }

    override suspend fun saveServer(server: Server) {
        saveAction(server)
    }

    override suspend fun deleteServer(serverId: String) {
        deleteAction(serverId)
    }
}
