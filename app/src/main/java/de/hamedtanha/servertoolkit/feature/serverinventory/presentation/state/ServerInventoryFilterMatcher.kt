package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state

import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.ServerEnvironment

internal fun List<Server>.applyServerInventoryFilter(
    filter: ServerInventoryFilter,
): List<Server> {
    return filter { server ->
        server.matchesSearchQuery(filter.searchQuery) &&
            server.matchesEnvironment(filter.environment) &&
            server.matchesFavoriteRequirement(filter.favoritesOnly)
    }
}

private fun Server.matchesSearchQuery(query: String): Boolean {
    val normalizedQuery = query.trim()

    if (normalizedQuery.isBlank()) {
        return true
    }

    return searchableValues().any { value ->
        value.contains(normalizedQuery, ignoreCase = true)
    }
}

private fun Server.searchableValues(): List<String> {
    return buildList {
        add(name)
        add(host)
        sshUsername?.let(::add)
        category?.let(::add)
        addAll(tags)
    }
}

private fun Server.matchesEnvironment(environment: ServerEnvironment?): Boolean {
    return environment == null || this.environment == environment
}

private fun Server.matchesFavoriteRequirement(favoritesOnly: Boolean): Boolean {
    return !favoritesOnly || isFavorite
}
