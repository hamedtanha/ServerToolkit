package de.hamedtanha.servertoolkit.feature.ssh.presentation.state

import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class SshSavedCommandSelectorUiStateTest {

    @Test
    fun `hidden is the only non-visible selector state`() {
        assertFalse(SshSavedCommandSelectorUiState.Hidden.isVisible)

        listOf(
            SshSavedCommandSelectorUiState.Loading,
            SshSavedCommandSelectorUiState.Empty,
            SshSavedCommandSelectorUiState.Content(
                commands = listOf(savedCommand()),
            ),
            SshSavedCommandSelectorUiState.Failure(
                errorMessage = "Saved commands could not be loaded.",
            ),
        ).forEach { state ->
            assertTrue(state.isVisible)
        }
    }

    @Test
    fun `content preserves selectable commands during retry and non-blocking failure`() {
        val commands = listOf(
            savedCommand(
                id = "command-1",
                command = "  printf 'first value'  ",
            ),
            savedCommand(
                id = "command-2",
                command = "printf 'second\nvalue'",
            ),
        )

        val state = SshSavedCommandSelectorUiState.Content(
            commands = commands,
            errorMessage = "Saved command updates could not be loaded.",
            isRetrying = true,
        )

        assertEquals(commands, state.selectableCommands)
        assertEquals(
            "Saved command updates could not be loaded.",
            state.errorMessage,
        )
        assertTrue(state.isRetrying)
    }

    @Test
    fun `states without loaded content expose no selectable commands`() {
        listOf(
            SshSavedCommandSelectorUiState.Hidden,
            SshSavedCommandSelectorUiState.Loading,
            SshSavedCommandSelectorUiState.Empty,
            SshSavedCommandSelectorUiState.Failure(
                errorMessage = "Saved commands could not be loaded.",
            ),
        ).forEach { state ->
            assertTrue(state.selectableCommands.isEmpty())
        }
    }

    @Test
    fun `content requires at least one command`() {
        assertThrows(IllegalArgumentException::class.java) {
            SshSavedCommandSelectorUiState.Content(
                commands = emptyList(),
            )
        }
    }

    @Test
    fun `content failure message must not be blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            SshSavedCommandSelectorUiState.Content(
                commands = listOf(savedCommand()),
                errorMessage = "   ",
            )
        }
    }

    @Test
    fun `blocking failure message must not be blank`() {
        assertThrows(IllegalArgumentException::class.java) {
            SshSavedCommandSelectorUiState.Failure(
                errorMessage = "",
            )
        }
    }

    private fun savedCommand(
        id: String = "command-1",
        command: String = "uptime",
    ): SavedCommand {
        return SavedCommand(
            id = id,
            name = "System status",
            command = command,
            createdAtEpochMillis = 1_000L,
        )
    }
}
