package de.hamedtanha.servertoolkit.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.dao.ServerDao
import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.entity.ServerEntity

@Database(
    entities = [ServerEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class ServerToolkitDatabase : RoomDatabase() {

    abstract fun serverDao(): ServerDao
}
