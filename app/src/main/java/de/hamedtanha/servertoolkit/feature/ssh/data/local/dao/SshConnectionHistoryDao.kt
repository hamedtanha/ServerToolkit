package de.hamedtanha.servertoolkit.feature.ssh.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshConnectionHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SshConnectionHistoryDao {

    @Query(
        """
        SELECT * FROM ssh_connection_history
        ORDER BY attempted_at_epoch_millis DESC
        """,
    )
    fun observeConnectionHistory(): Flow<List<SshConnectionHistoryEntity>>

    @Query(
        """
        SELECT * FROM ssh_connection_history
        WHERE server_id = :serverId
        ORDER BY attempted_at_epoch_millis DESC
        """,
    )
    fun observeConnectionHistoryForServer(serverId: String): Flow<List<SshConnectionHistoryEntity>>

    @Query(
        """
        SELECT * FROM ssh_connection_history
        WHERE id = :entryId
        LIMIT 1
        """,
    )
    suspend fun getConnectionHistoryEntry(entryId: String): SshConnectionHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertConnectionHistoryEntry(entity: SshConnectionHistoryEntity)

    @Query(
        """
        DELETE FROM ssh_connection_history
        WHERE id = :entryId
        """,
    )
    suspend fun deleteConnectionHistoryEntry(entryId: String)
}
