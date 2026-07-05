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
                    'server-1',
                    'Production',
                    'example.com',
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

            execSQL(
                """
                INSERT INTO ssh_trusted_host_keys (
                    server_id,
                    host,
                    port,
                    fingerprint_algorithm,
                    fingerprint_value
                ) VALUES (
                    'server-1',
                    'example.com',
                    22,
                    'SHA256',
                    'abc123'
                )
                """.trimIndent(),
            )
            close()
        }
    }

    private companion object {
        const val TEST_DATABASE_NAME = "server-toolkit-migration-test"
    }
}
