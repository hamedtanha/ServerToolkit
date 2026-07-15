package de.hamedtanha.servertoolkit.feature.savedcommands.data.mapper

import de.hamedtanha.servertoolkit.feature.savedcommands.data.local.entity.SavedCommandEntity
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import org.junit.Assert.assertEquals
import org.junit.Test

class SavedCommandEntityMapperTest {

    @Test
    fun `maps saved command domain model to entity without changing command text`() {
        val domain = savedCommand(command = "  printf 'hello'\n")

        val entity = domain.toEntity()

        assertEquals("command-1", entity.id)
        assertEquals("Print greeting", entity.name)
        assertEquals("  printf 'hello'\n", entity.command)
        assertEquals(1_000L, entity.createdAtEpochMillis)
    }

    @Test
    fun `maps saved command entity to domain model without changing command text`() {
        val entity = SavedCommandEntity(
            id = "command-1",
            name = "Print greeting",
            command = "  printf 'hello'\n",
            createdAtEpochMillis = 1_000,
        )

        val domain = entity.toDomain()

        assertEquals("command-1", domain.id)
        assertEquals("Print greeting", domain.name)
        assertEquals("  printf 'hello'\n", domain.command)
        assertEquals(1_000L, domain.createdAtEpochMillis)
    }

    @Test
    fun `round trip preserves complete saved command`() {
        val domain = savedCommand(command = "printf 'line 1\\nline 2\\n'")

        assertEquals(domain, domain.toEntity().toDomain())
    }

    private fun savedCommand(
        command: String = "printf 'hello'",
    ): SavedCommand {
        return SavedCommand(
            id = "command-1",
            name = "Print greeting",
            command = command,
            createdAtEpochMillis = 1_000,
        )
    }
}
