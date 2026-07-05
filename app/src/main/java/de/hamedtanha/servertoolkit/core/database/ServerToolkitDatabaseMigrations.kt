package de.hamedtanha.servertoolkit.core.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `ssh_trusted_host_keys` (
                `server_id` TEXT NOT NULL,
                `host` TEXT NOT NULL,
                `port` INTEGER NOT NULL,
                `fingerprint_algorithm` TEXT NOT NULL,
                `fingerprint_value` TEXT NOT NULL,
                PRIMARY KEY(`server_id`, `host`, `port`)
            )
            """.trimIndent(),
        )
    }
}
