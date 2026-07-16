package de.hamedtanha.servertoolkit.feature.savedcommands.data.factory

import de.hamedtanha.servertoolkit.feature.savedcommands.domain.factory.SavedCommandFactory
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import java.util.UUID
import javax.inject.Inject

class DefaultSavedCommandFactory @Inject constructor() : SavedCommandFactory {

    override fun create(
        name: String,
        command: String,
    ): SavedCommand {
        return SavedCommand(
            id = UUID.randomUUID().toString(),
            name = name,
            command = command,
            createdAtEpochMillis = System.currentTimeMillis(),
        )
    }
}
