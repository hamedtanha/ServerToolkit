package de.hamedtanha.servertoolkit.feature.savedcommands.domain.factory

import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand

interface SavedCommandFactory {

    fun create(
        name: String,
        command: String,
    ): SavedCommand
}
