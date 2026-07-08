package de.hamedtanha.servertoolkit.feature.ssh.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.entity.ServerEntity

@Entity(
    tableName = "ssh_trusted_host_keys",
    primaryKeys = ["server_id", "host", "port"],
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
    ],
)
data class SshTrustedHostKeyEntity(
    @ColumnInfo(name = "server_id")
    val serverId: String,
    val host: String,
    val port: Int,
    @ColumnInfo(name = "fingerprint_algorithm")
    val fingerprintAlgorithm: String,
    @ColumnInfo(name = "fingerprint_value")
    val fingerprintValue: String,
)
