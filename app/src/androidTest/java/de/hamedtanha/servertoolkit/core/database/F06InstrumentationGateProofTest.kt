package de.hamedtanha.servertoolkit.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class F06InstrumentationGateProofTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        ServerToolkitDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun requiredGate_rejectsDeliberatelyFailingInstrumentation() {
        helper.createDatabase(TEST_DATABASE_NAME, 1).close()
        helper.runMigrationsAndValidate(
            TEST_DATABASE_NAME,
            2,
            true,
            MIGRATION_1_2,
        ).close()

        check(false) {
            "F06 deliberate required-gate instrumentation failure."
        }
    }

    private companion object {
        const val TEST_DATABASE_NAME = "f06-required-gate-proof"
    }
}
