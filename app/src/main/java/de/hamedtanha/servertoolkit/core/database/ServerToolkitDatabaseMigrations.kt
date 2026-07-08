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

val MIGRATION_2_3 = object : Migration(2, 3) {

    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `ssh_trusted_host_keys_new` (
                `server_id` TEXT NOT NULL,
                `host` TEXT NOT NULL,
                `port` INTEGER NOT NULL,
                `fingerprint_algorithm` TEXT NOT NULL,
                `fingerprint_value` TEXT NOT NULL,
                PRIMARY KEY(`server_id`, `host`, `port`),
                FOREIGN KEY(`server_id`) REFERENCES `servers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            INSERT INTO `ssh_trusted_host_keys_new` (
                `server_id`,
                `host`,
                `port`,
                `fingerprint_algorithm`,
                `fingerprint_value`
            )
            SELECT
                `server_id`,
                `host`,
                `port`,
                `fingerprint_algorithm`,
                `fingerprint_value`
            FROM `ssh_trusted_host_keys`
            WHERE `server_id` IN (
                SELECT `id` FROM `servers`
            )
            """.trimIndent(),
        )
        db.execSQL("DROP TABLE `ssh_trusted_host_keys`")
        db.execSQL("ALTER TABLE `ssh_trusted_host_keys_new` RENAME TO `ssh_trusted_host_keys`")
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_ssh_trusted_host_keys_server_id` " +
                "ON `ssh_trusted_host_keys` (`server_id`)",
        )
    }
}
