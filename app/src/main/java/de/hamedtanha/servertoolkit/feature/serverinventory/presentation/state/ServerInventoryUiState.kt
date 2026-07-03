package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state

import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server

data class ServerInventoryUiState(
    val servers: List<Server> = emptyList(),
    val filter: ServerInventoryFilter = ServerInventoryFilter(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
) {
    val isInventoryEmpty: Boolean
        get() = servers.isEmpty() && !filter.hasActiveFilter && !isLoading && errorMessage == null

    val isFilterResultEmpty: Boolean
        get() = servers.isEmpty() && filter.hasActiveFilter && !isLoading && errorMessage == null

    val hasVisibleServers: Boolean
        get() = servers.isNotEmpty()
}

