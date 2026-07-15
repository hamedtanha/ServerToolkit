package de.hamedtanha.servertoolkit.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import de.hamedtanha.servertoolkit.feature.savedcommands.data.local.dao.SavedCommandDao
import de.hamedtanha.servertoolkit.feature.savedcommands.data.local.entity.SavedCommandEntity
import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.dao.ServerDao
import de.hamedtanha.servertoolkit.feature.serverinventory.data.local.entity.ServerEntity
import de.hamedtanha.servertoolkit.feature.ssh.data.local.dao.SshConnectionHistoryDao
import de.hamedtanha.servertoolkit.feature.ssh.data.local.dao.SshTrustedHostKeyDao
import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshConnectionHistoryEntity
import de.hamedtanha.servertoolkit.feature.ssh.data.local.entity.SshTrustedHostKeyEntity

@Database(
    entities = [
        ServerEntity::class,
        SshTrustedHostKeyEntity::class,
        SshConnectionHistoryEntity::class,
        SavedCommandEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class ServerToolkitDatabase : RoomDatabase() {

    abstract fun serverDao(): ServerDao

    abstract fun sshTrustedHostKeyDao(): SshTrustedHostKeyDao

    abstract fun sshConnectionHistoryDao(): SshConnectionHistoryDao

    abstract fun savedCommandDao(): SavedCommandDao
}
