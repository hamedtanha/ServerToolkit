package de.hamedtanha.servertoolkit.feature.savedcommands.presentation.viewmodel

import de.hamedtanha.servertoolkit.core.test.MainDispatcherRule
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.repository.SavedCommandRepository
import de.hamedtanha.servertoolkit.feature.savedcommands.test.FakeSavedCommandRepository
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SavedCommandsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `starts in loading state before repository emission`() = runTest {
        val viewModel = SavedCommandsViewModel(
            PendingSavedCommandRepository(),
        )

        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `exposes empty state for empty repository emission`() = runTest {
        val viewModel = SavedCommandsViewModel(
            FakeSavedCommandRepository(),
        )

        val state = viewModel.uiState.first { currentState ->
            !currentState.isLoading
        }

        assertTrue(state.isEmpty)
        assertFalse(state.hasCommands)
        assertEquals(null, state.errorMessage)
    }

    @Test
    fun `preserves repository ordering for non empty emission`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(
                savedCommand(
                    id = "newer-command",
                    createdAtEpochMillis = 2_000L,
                ),
                savedCommand(
                    id = "older-command",
                    createdAtEpochMillis = 1_000L,
                ),
            ),
        )

        val viewModel = SavedCommandsViewModel(repository)

        val state = viewModel.uiState.first { currentState ->
            currentState.hasCommands
        }

        assertEquals(
            listOf("newer-command", "older-command"),
            state.commands.map { command -> command.id },
        )
    }

    @Test
    fun `maps initial observation failure and retries successfully`() = runTest {
        val repository = FakeSavedCommandRepository()
        repository.failObservation()

        val viewModel = SavedCommandsViewModel(repository)

        val failedState = viewModel.uiState.first { currentState ->
            currentState.hasBlockingError
        }

        assertEquals(
            "Saved commands could not be loaded.",
            failedState.errorMessage,
        )

        repository.emitSavedCommands(
            listOf(savedCommand()),
        )

        viewModel.onRetryLoad()

        val recoveredState = viewModel.uiState.first { currentState ->
            currentState.hasCommands &&
                currentState.errorMessage == null
        }

        assertEquals("saved-command-1", recoveredState.commands.single().id)
        assertEquals(2, repository.observeCallCount)
    }

    @Test
    fun `preserves loaded commands when observation later fails`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )

        val viewModel = SavedCommandsViewModel(repository)

        viewModel.uiState.first { currentState ->
            currentState.hasCommands
        }

        repository.failObservation()

        val failedState = viewModel.uiState.first { currentState ->
            currentState.hasNonBlockingError
        }

        assertEquals(
            listOf("saved-command-1"),
            failedState.commands.map { command -> command.id },
        )
        assertEquals(
            "Saved commands could not be loaded.",
            failedState.errorMessage,
        )
    }

    private fun savedCommand(
        id: String = "saved-command-1",
        createdAtEpochMillis: Long = 1_000L,
    ): SavedCommand {
        return SavedCommand(
            id = id,
            name = "List services",
            command = "systemctl list-units --type=service",
            createdAtEpochMillis = createdAtEpochMillis,
        )
    }

    private class PendingSavedCommandRepository :
        SavedCommandRepository {

        override fun observeSavedCommands(): Flow<List<SavedCommand>> {
            return flow {
                awaitCancellation()
            }
        }

        override suspend fun getSavedCommand(
            savedCommandId: String,
        ): SavedCommand? {
            error("Not used in this test.")
        }

        override suspend fun createSavedCommand(
            savedCommand: SavedCommand,
        ) {
            error("Not used in this test.")
        }

        override suspend fun deleteSavedCommand(
            savedCommandId: String,
        ) {
            error("Not used in this test.")
        }
    }
}
