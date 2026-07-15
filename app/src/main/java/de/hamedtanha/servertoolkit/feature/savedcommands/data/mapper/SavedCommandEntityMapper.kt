package de.hamedtanha.servertoolkit.feature.savedcommands.data.mapper

import de.hamedtanha.servertoolkit.feature.savedcommands.data.local.entity.SavedCommandEntity
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand

internal fun SavedCommand.toEntity(): SavedCommandEntity {
    return SavedCommandEntity(
        id = id,
        name = name,
        command = command,
        createdAtEpochMillis = createdAtEpochMillis,
    )
}

internal fun SavedCommandEntity.toDomain(): SavedCommand {
    return SavedCommand(
        id = id,
        name = name,
        command = command,
        createdAtEpochMillis = createdAtEpochMillis,
    )
}
