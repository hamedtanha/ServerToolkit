package de.hamedtanha.servertoolkit.feature.savedcommands.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test

class SavedCommandTest {

    @Test
    fun `creates saved command when values satisfy domain boundaries`() {
        val command = SavedCommand(
            id = "command-1",
            name = "Disk usage",
            command = "  df -h\n",
            createdAtEpochMillis = 1_000,
        )

        assertEquals("command-1", command.id)
        assertEquals("Disk usage", command.name)
        assertEquals("  df -h\n", command.command)
        assertEquals(1_000L, command.createdAtEpochMillis)
    }

    @Test
    fun `rejects blank identifiers`() {
        assertInvalidSavedCommand(id = " ")
    }

    @Test
    fun `rejects blank names`() {
        assertInvalidSavedCommand(name = " ")
    }

    @Test
    fun `rejects names with leading or trailing whitespace`() {
        assertInvalidSavedCommand(name = " Disk usage")
        assertInvalidSavedCommand(name = "Disk usage ")
    }

    @Test
    fun `rejects names containing control characters`() {
        assertInvalidSavedCommand(name = "Disk\nusage")
        assertInvalidSavedCommand(name = "Disk\tusage")
    }

    @Test
    fun `rejects names beyond the accepted boundary`() {
        assertInvalidSavedCommand(name = "a".repeat(SavedCommand.MAX_NAME_LENGTH + 1))
    }

    @Test
    fun `rejects blank command text`() {
        assertInvalidSavedCommand(command = " \n\t")
    }

    @Test
    fun `rejects command text beyond the accepted boundary`() {
        assertInvalidSavedCommand(command = "a".repeat(SavedCommand.MAX_COMMAND_LENGTH + 1))
    }

    @Test
    fun `rejects non-positive creation time`() {
        assertInvalidSavedCommand(createdAtEpochMillis = 0)
    }

    private fun assertInvalidSavedCommand(
        id: String = "command-1",
        name: String = "Disk usage",
        command: String = "df -h",
        createdAtEpochMillis: Long = 1_000,
    ) {
        try {
            SavedCommand(
                id = id,
                name = name,
                command = command,
                createdAtEpochMillis = createdAtEpochMillis,
            )
            fail("Expected IllegalArgumentException")
        } catch (_: IllegalArgumentException) {
            // Expected domain validation failure.
        }
    }
}
