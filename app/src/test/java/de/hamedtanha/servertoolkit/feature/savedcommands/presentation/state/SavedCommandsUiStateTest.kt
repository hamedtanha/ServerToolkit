package de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state

import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedCommandsUiStateTest {

    @Test
    fun `loading state is not empty content`() {
        val state = SavedCommandsUiState(isLoading = true)

        assertFalse(state.isEmpty)
        assertFalse(state.hasCommands)
        assertFalse(state.hasBlockingError)
        assertFalse(state.hasNonBlockingError)
    }

    @Test
    fun `successful empty observation exposes empty content`() {
        val state = SavedCommandsUiState()

        assertTrue(state.isEmpty)
        assertFalse(state.hasCommands)
        assertFalse(state.hasBlockingError)
    }

    @Test
    fun `failure without commands is blocking`() {
        val state = SavedCommandsUiState(
            errorMessage = "Saved commands could not be loaded.",
        )

        assertTrue(state.hasBlockingError)
        assertFalse(state.hasNonBlockingError)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `failure with commands preserves non blocking content`() {
        val state = SavedCommandsUiState(
            commands = listOf(savedCommand()),
            errorMessage = "Saved commands could not be loaded.",
        )

        assertTrue(state.hasCommands)
        assertTrue(state.hasNonBlockingError)
        assertFalse(state.hasBlockingError)
    }

    private fun savedCommand(): SavedCommand {
        return SavedCommand(
            id = "saved-command-1",
            name = "List services",
            command = "systemctl list-units --type=service",
            createdAtEpochMillis = 1_000L,
        )
    }
}
