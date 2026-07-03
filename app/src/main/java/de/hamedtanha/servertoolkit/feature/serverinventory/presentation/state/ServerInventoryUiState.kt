package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state

import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server

data class ServerInventoryUiState(
    val servers: List<Server> = emptyList(),
    val filter: ServerInventoryFilter = ServerInventoryFilter(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isEmpty: Boolean
        get() = servers.isEmpty() && !isLoading && errorMessage == null

    val hasServers: Boolean
        get() = servers.isNotEmpty()
}
