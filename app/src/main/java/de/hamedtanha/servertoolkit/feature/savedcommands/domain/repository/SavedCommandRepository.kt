package de.hamedtanha.servertoolkit.feature.savedcommands.domain.repository

import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import kotlinx.coroutines.flow.Flow

interface SavedCommandRepository {

    fun observeSavedCommands(): Flow<List<SavedCommand>>

    suspend fun getSavedCommand(savedCommandId: String): SavedCommand?

    suspend fun createSavedCommand(savedCommand: SavedCommand)

    suspend fun updateSavedCommand(savedCommand: SavedCommand)

    suspend fun deleteSavedCommand(savedCommandId: String)
}
