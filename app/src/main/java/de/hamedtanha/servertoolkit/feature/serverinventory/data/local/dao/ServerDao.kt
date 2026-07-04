package de.hamedtanha.servertoolkit.feature.serverinventory.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.entity.ServerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ServerDao {

    @Query("SELECT * FROM servers ORDER BY name COLLATE NOCASE ASC, host COLLATE NOCASE ASC")
    fun observeServers(): Flow<List<ServerEntity>>

    @Query("SELECT * FROM servers WHERE id = :serverId LIMIT 1")
    suspend fun getServerById(serverId: String): ServerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertServer(server: ServerEntity)

    @Query("DELETE FROM servers WHERE id = :serverId")
    suspend fun deleteServer(serverId: String)
}
