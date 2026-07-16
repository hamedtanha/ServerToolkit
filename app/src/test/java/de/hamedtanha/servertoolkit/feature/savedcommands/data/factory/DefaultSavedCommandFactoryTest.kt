package de.hamedtanha.servertoolkit.feature.savedcommands.data.factory

import java.util.UUID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultSavedCommandFactoryTest {

    @Test
    fun `creates valid saved command while preserving supplied values`() {
        val factory = DefaultSavedCommandFactory()
        val command = "\n  echo exact text  \n"

        val savedCommand = factory.create(
            name = "Exact command",
            command = command,
        )

        UUID.fromString(savedCommand.id)
        assertEquals("Exact command", savedCommand.name)
        assertEquals(command, savedCommand.command)
        assertTrue(savedCommand.createdAtEpochMillis > 0L)
    }
}
