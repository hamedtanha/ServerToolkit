package de.hamedtanha.servertoolkit.feature.ssh.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.entity.ServerEntity

@Entity(
    tableName = "ssh_connection_history",
    foreignKeys = [
        ForeignKey(
            entity = ServerEntity::class,
            parentColumns = ["id"],
            childColumns = ["server_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["server_id"]),
        Index(value = ["attempted_at_epoch_millis"]),
    ],
)
data class SshConnectionHistoryEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "server_id")
    val serverId: String,
    val host: String,
    val port: Int,
    val username: String,
    val status: String,
    @ColumnInfo(name = "attempted_at_epoch_millis")
    val attemptedAtEpochMillis: Long,
    @ColumnInfo(name = "completed_at_epoch_millis")
    val completedAtEpochMillis: Long?,
    @ColumnInfo(name = "connection_error")
    val connectionError: String?,
)
