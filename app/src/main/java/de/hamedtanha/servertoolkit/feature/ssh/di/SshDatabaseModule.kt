package de.hamedtanha.servertoolkit.feature.ssh.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import de.hamedtanha.servertoolkit.core.database.ServerToolkitDatabase
import de.hamedtanha.servertoolkit.feature.ssh.data.local.dao.SshTrustedHostKeyDao

@Module
@InstallIn(SingletonComponent::class)
object SshDatabaseModule {

    @Provides
    fun provideSshTrustedHostKeyDao(
        database: ServerToolkitDatabase,
    ): SshTrustedHostKeyDao {
        return database.sshTrustedHostKeyDao()
    }
}
