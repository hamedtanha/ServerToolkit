package de.hamedtanha.servertoolkit.feature.savedcommands.data.repository

import de.hamedtanha.servertoolkit.feature.savedcommands.data.local.dao.SavedCommandDao
import de.hamedtanha.servertoolkit.feature.savedcommands.data.mapper.toDomain
import de.hamedtanha.servertoolkit.feature.savedcommands.data.mapper.toEntity
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.repository.SavedCommandRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomSavedCommandRepository @Inject constructor(
    private val savedCommandDao: SavedCommandDao,
) : SavedCommandRepository {

    override fun observeSavedCommands(): Flow<List<SavedCommand>> {
        return savedCommandDao.observeSavedCommands()
            .map { commands -> commands.map { command -> command.toDomain() } }
    }

    override suspend fun getSavedCommand(savedCommandId: String): SavedCommand? {
        return savedCommandDao.getSavedCommand(savedCommandId)?.toDomain()
    }

    override suspend fun createSavedCommand(savedCommand: SavedCommand) {
        savedCommandDao.insertSavedCommand(savedCommand.toEntity())
    }

    override suspend fun deleteSavedCommand(savedCommandId: String) {
        savedCommandDao.deleteSavedCommand(savedCommandId)
    }
}
