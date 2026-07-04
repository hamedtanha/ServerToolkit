package de.hamedtanha.servertoolkit.feature.serverinventory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servers")
data class ServerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val host: String,
    @ColumnInfo(name = "ssh_port")
    val sshPort: Int,
    @ColumnInfo(name = "ssh_username")
    val sshUsername: String?,
    val environment: String,
    val category: String?,
    val tags: String,
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean,
    val description: String?,
)
