package de.hamedtanha.servertoolkit.feature.savedcommands.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "saved_commands",
    indices = [
        Index(value = ["created_at_epoch_millis"]),
    ],
)
data class SavedCommandEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    @ColumnInfo(name = "command_text")
    val command: String,
    @ColumnInfo(name = "created_at_epoch_millis")
    val createdAtEpochMillis: Long,
)
