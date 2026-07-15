package de.hamedtanha.servertoolkit.feature.savedcommands.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.hamedtanha.servertoolkit.feature.savedcommands.data.local.entity.SavedCommandEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedCommandDao {

    @Query(
        """
        SELECT * FROM saved_commands
        ORDER BY created_at_epoch_millis DESC, id ASC
        """,
    )
    fun observeSavedCommands(): Flow<List<SavedCommandEntity>>

    @Query(
        """
        SELECT * FROM saved_commands
        WHERE id = :savedCommandId
        LIMIT 1
        """,
    )
    suspend fun getSavedCommand(savedCommandId: String): SavedCommandEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSavedCommand(entity: SavedCommandEntity)

    @Query(
        """
        DELETE FROM saved_commands
        WHERE id = :savedCommandId
        """,
    )
    suspend fun deleteSavedCommand(savedCommandId: String)
}
