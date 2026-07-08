package de.hamedtanha.servertoolkit.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServerToolkitDatabaseMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ServerToolkitDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migrate1To2_createsTrustedHostKeyTableAndPreservesServers() {
        helper.createDatabase(TEST_DATABASE_NAME, 1).apply {
            insertServer(
                serverId = "server-1",
                host = "example.com",
            )
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DATABASE_NAME,
            2,
            true,
            MIGRATION_1_2,
        ).apply {
            query("SELECT COUNT(*) FROM servers").use { cursor ->
                cursor.moveToFirst()
                check(cursor.getInt(0) == 1) {
                    "Expected server inventory rows to survive migration."
                }
            }

            insertTrustedHostKey(
                serverId = "server-1",
                host = "example.com",
                fingerprintValue = "abc123",
            )
            close()
        }
    }

    @Test
    fun migrate2To3_addsTrustedHostKeyCascadeAndDropsOrphanedTrustedHostKeys() {
        helper.createDatabase(TEST_DATABASE_NAME, 2).apply {
            insertServer(
                serverId = "server-1",
                host = "example.com",
            )
            insertTrustedHostKey(
                serverId = "server-1",
                host = "example.com",
                fingerprintValue = "trusted-fingerprint",
            )
            insertTrustedHostKey(
                serverId = "orphaned-server",
                host = "orphaned.example.com",
                fingerprintValue = "orphaned-fingerprint",
            )
            close()
        }

        helper.runMigrationsAndValidate(
            TEST_DATABASE_NAME,
            3,
            true,
            MIGRATION_2_3,
        ).apply {
            query("SELECT COUNT(*) FROM ssh_trusted_host_keys").use { cursor ->
                cursor.moveToFirst()
                check(cursor.getInt(0) == 1) {
                    "Expected migration to keep only trusted host keys with existing servers."
                }
            }

            setForeignKeyConstraintsEnabled(true)
            execSQL("DELETE FROM servers WHERE id = 'server-1'")

            query("SELECT COUNT(*) FROM ssh_trusted_host_keys").use { cursor ->
                cursor.moveToFirst()
                check(cursor.getInt(0) == 0) {
                    "Expected trusted host keys to be cascade-deleted with their server."
                }
            }
            close()
        }
    }

    private fun androidx.sqlite.db.SupportSQLiteDatabase.insertServer(
        serverId: String,
        host: String,
    ) {
        execSQL(
            """
            INSERT INTO servers (
                id,
                name,
                host,
                ssh_port,
                ssh_username,
                environment,
                category,
                tags,
                is_favorite,
                description
            ) VALUES (
                '$serverId',
                'Production',
                '$host',
                22,
                'admin',
                'PRODUCTION',
                'Linux',
                'production',
                1,
                'Primary server'
            )
            """.trimIndent(),
        )
    }

    private fun androidx.sqlite.db.SupportSQLiteDatabase.insertTrustedHostKey(
        serverId: String,
        host: String,
        fingerprintValue: String,
    ) {
        execSQL(
            """
            INSERT INTO ssh_trusted_host_keys (
                server_id,
                host,
                port,
                fingerprint_algorithm,
                fingerprint_value
            ) VALUES (
                '$serverId',
                '$host',
                22,
                'SHA256',
                '$fingerprintValue'
            )
            """.trimIndent(),
        )
    }

    private companion object {
        const val TEST_DATABASE_NAME = "server-toolkit-migration-test"
    }
}
