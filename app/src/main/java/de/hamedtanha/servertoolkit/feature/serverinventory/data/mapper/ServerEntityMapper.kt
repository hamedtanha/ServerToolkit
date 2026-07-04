package de.hamedtanha.servertoolkit.feature.serverinventory.data.mapper

import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.entity.ServerEntity
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.ServerEnvironment

private const val TAG_SEPARATOR = "\u001F"

internal fun Server.toEntity(): ServerEntity {
    return ServerEntity(
        id = id,
        name = name,
        host = host,
        sshPort = sshPort,
        sshUsername = sshUsername,
        environment = environment.name,
        category = category,
        tags = tags.joinToString(separator = TAG_SEPARATOR),
        isFavorite = isFavorite,
        description = description,
    )
}

internal fun ServerEntity.toDomain(): Server {
    return Server(
        id = id,
        name = name,
        host = host,
        sshPort = sshPort,
        sshUsername = sshUsername,
        environment = environment.toServerEnvironment(),
        category = category,
        tags = tags.toTagList(),
        isFavorite = isFavorite,
        description = description,
    )
}

private fun String.toServerEnvironment(): ServerEnvironment {
    return runCatching {
        ServerEnvironment.valueOf(this)
    }.getOrDefault(ServerEnvironment.UNKNOWN)
}

private fun String.toTagList(): List<String> {
    return if (isBlank()) {
        emptyList()
    } else {
        split(TAG_SEPARATOR).filter { tag ->
            tag.isNotBlank()
        }
    }
}
