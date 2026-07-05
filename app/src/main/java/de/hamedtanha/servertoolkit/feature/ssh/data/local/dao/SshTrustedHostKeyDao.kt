package de.hamedtanha.servertoolkit.feature.ssh.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshTrustedHostKeyEntity

@Dao
interface SshTrustedHostKeyDao {

    @Query(
        """
        SELECT * FROM ssh_trusted_host_keys
        WHERE server_id = :serverId AND host = :host AND port = :port
        LIMIT 1
        """,
    )
    suspend fun getTrustedHostKey(
        serverId: String,
        host: String,
        port: Int,
    ): SshTrustedHostKeyEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertTrustedHostKey(entity: SshTrustedHostKeyEntity)

    @Query(
        """
        DELETE FROM ssh_trusted_host_keys
        WHERE server_id = :serverId AND host = :host AND port = :port
        """,
    )
    suspend fun deleteTrustedHostKey(
        serverId: String,
        host: String,
        port: Int,
    )
}
