package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state

import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.ServerEnvironment

data class ServerInventoryFilter(
    val searchQuery: String = "",
    val environment: ServerEnvironment? = null,
    val favoritesOnly: Boolean = false,
) {
    val hasActiveFilter: Boolean
        get() = searchQuery.isNotBlank() || environment != null || favoritesOnly
}
