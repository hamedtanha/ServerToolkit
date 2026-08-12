package de.hamedtanha.servertoolkit.feature.savedcommands.presentation.viewmodel

import de.hamedtanha.servertoolkit.core.test.MainDispatcherRule
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.factory.SavedCommandFactory
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
        val viewModel = createViewModel(PendingSavedCommandRepository())

        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `exposes empty state for empty repository emission`() = runTest {
        val viewModel = createViewModel(FakeSavedCommandRepository())

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

        val viewModel = createViewModel(repository)

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

        val viewModel = createViewModel(repository)

        val failedState = viewModel.uiState.first { currentState ->
            currentState.hasBlockingError
        }

        assertEquals(
            "Saved commands could not be loaded.",
            failedState.errorMessage,
        )

        repository.emitSavedCommands(listOf(savedCommand()))
        viewModel.onRetryLoad()

        val recoveredState = viewModel.uiState.first { currentState ->
            currentState.hasCommands && currentState.errorMessage == null
        }

        assertEquals("saved-command-1", recoveredState.commands.single().id)
        assertEquals(2, repository.observeCallCount)
    }

    @Test
    fun `preserves loaded commands when observation later fails`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        val viewModel = createViewModel(repository)

        viewModel.uiState.first { currentState -> currentState.hasCommands }
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

    @Test
    fun `opens and cancels create form`() = runTest {
        val viewModel = createViewModel(FakeSavedCommandRepository())
        viewModel.uiState.first { state -> !state.isLoading }

        viewModel.onOpenCreate()
        assertTrue(viewModel.uiState.value.isCreateVisible)

        viewModel.onCancelCreate()
        assertFalse(viewModel.uiState.value.isCreateVisible)
    }

    @Test
    fun `rejects blank normalized name`() = runTest {
        val repository = FakeSavedCommandRepository()
        val viewModel = createViewModel(repository)

        openCreateForm(viewModel, name = "   ", command = "echo ok")
        viewModel.onCreateConfirmed()

        assertEquals(
            "Name is required.",
            viewModel.uiState.value.createForm?.nameError,
        )
        assertEquals(0, repository.createCallCount)
    }

    @Test
    fun `rejects control characters in normalized name`() = runTest {
        val repository = FakeSavedCommandRepository()
        val viewModel = createViewModel(repository)

        openCreateForm(
            viewModel,
            name = "List\u0000services",
            command = "echo ok",
        )
        viewModel.onCreateConfirmed()

        assertEquals(
            "Name must not contain control characters.",
            viewModel.uiState.value.createForm?.nameError,
        )
        assertEquals(0, repository.createCallCount)
    }

    @Test
    fun `accepts name at maximum length`() = runTest {
        val repository = FakeSavedCommandRepository()
        val viewModel = createViewModel(repository)
        val name = "n".repeat(SavedCommand.MAX_NAME_LENGTH)

        openCreateForm(viewModel, name = name, command = "echo ok")
        viewModel.onCreateConfirmed()

        viewModel.uiState.first { state -> !state.isCreateVisible }
        assertEquals(name, repository.createArguments.single().name)
    }

    @Test
    fun `rejects name beyond maximum length`() = runTest {
        val repository = FakeSavedCommandRepository()
        val viewModel = createViewModel(repository)
        val name = "n".repeat(SavedCommand.MAX_NAME_LENGTH + 1)

        openCreateForm(viewModel, name = name, command = "echo ok")
        viewModel.onCreateConfirmed()

        assertEquals(
            "Name must not exceed ${SavedCommand.MAX_NAME_LENGTH} characters.",
            viewModel.uiState.value.createForm?.nameError,
        )
        assertEquals(0, repository.createCallCount)
    }

    @Test
    fun `rejects blank command text`() = runTest {
        val repository = FakeSavedCommandRepository()
        val viewModel = createViewModel(repository)

        openCreateForm(
            viewModel,
            name = "List services",
            command = " \n\t ",
        )
        viewModel.onCreateConfirmed()

        assertEquals(
            "Command text is required.",
            viewModel.uiState.value.createForm?.commandError,
        )
        assertEquals(0, repository.createCallCount)
    }

    @Test
    fun `accepts command at maximum length`() = runTest {
        val repository = FakeSavedCommandRepository()
        val viewModel = createViewModel(repository)
        val command = "x".repeat(SavedCommand.MAX_COMMAND_LENGTH)

        openCreateForm(
            viewModel,
            name = "Maximum command",
            command = command,
        )
        viewModel.onCreateConfirmed()

        viewModel.uiState.first { state -> !state.isCreateVisible }
        assertEquals(command, repository.createArguments.single().command)
    }

    @Test
    fun `rejects command beyond maximum length`() = runTest {
        val repository = FakeSavedCommandRepository()
        val viewModel = createViewModel(repository)
        val command = "x".repeat(SavedCommand.MAX_COMMAND_LENGTH + 1)

        openCreateForm(
            viewModel,
            name = "Too large",
            command = command,
        )
        viewModel.onCreateConfirmed()

        assertEquals(
            "Command text must not exceed ${SavedCommand.MAX_COMMAND_LENGTH} characters.",
            viewModel.uiState.value.createForm?.commandError,
        )
        assertEquals(0, repository.createCallCount)
    }

    @Test
    fun `trims name and preserves exact command through creation`() = runTest {
        val repository = FakeSavedCommandRepository()
        val viewModel = createViewModel(repository)
        val exactCommand = "\n  printf 'hello'  \n"

        openCreateForm(
            viewModel,
            name = "  Print greeting  ",
            command = exactCommand,
        )
        viewModel.onCreateConfirmed()

        val completedState = viewModel.uiState.first { state ->
            !state.isCreateVisible && state.hasCommands
        }
        val createdCommand = repository.createArguments.single()

        assertEquals("Print greeting", createdCommand.name)
        assertEquals(exactCommand, createdCommand.command)
        assertEquals("generated-saved-command", createdCommand.id)
        assertEquals(4_000L, createdCommand.createdAtEpochMillis)
        assertEquals(createdCommand, completedState.commands.single())
    }

    @Test
    fun `creation failure preserves loaded commands and form input`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        repository.createFailure = IllegalStateException("Database unavailable")
        val viewModel = createViewModel(repository)

        viewModel.uiState.first { state -> state.hasCommands }
        openCreateForm(
            viewModel,
            name = "  Failed command  ",
            command = "  echo failed  ",
        )
        viewModel.onCreateConfirmed()

        val failedState = viewModel.uiState.first { state ->
            state.createForm?.errorMessage != null
        }

        val failedForm = requireNotNull(failedState.createForm)

        assertEquals(1, failedState.commands.size)
        assertEquals("  Failed command  ", failedForm.name)
        assertEquals("  echo failed  ", failedForm.command)
        assertFalse(failedForm.isSaving)
        assertEquals(
            "Saved command could not be created.",
            failedForm.errorMessage,
        )
    }

    @Test
    fun `prevents duplicate create confirmation while saving`() = runTest {
        val repository = FakeSavedCommandRepository().apply {
            suspendCreateOperations = true
        }
        val viewModel = createViewModel(repository)

        openCreateForm(
            viewModel,
            name = "List services",
            command = "systemctl list-units",
        )
        viewModel.onCreateConfirmed()
        repository.awaitCreateStarted()

        assertTrue(requireNotNull(viewModel.uiState.value.createForm).isSaving)

        viewModel.onCreateConfirmed()
        assertEquals(1, repository.createCallCount)

        repository.releaseCreate()
        viewModel.uiState.first { state -> !state.isCreateVisible }
        assertEquals(1, repository.createCallCount)
    }

    @Test
    fun `opens and cancels edit form without changing persistence`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onEditRequested("saved-command-1")

        val form = requireNotNull(viewModel.uiState.value.editForm)

        assertEquals("saved-command-1", form.savedCommandId)
        assertEquals("List services", form.name)
        assertEquals(
            "systemctl list-units --type=service",
            form.command,
        )

        viewModel.onCancelEdit()

        assertFalse(viewModel.uiState.value.isEditVisible)
        assertEquals(0, repository.updateCallCount)
    }

    @Test
    fun `does not open edit form for unknown identifier`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onEditRequested("missing-command")

        assertFalse(viewModel.uiState.value.isEditVisible)
        assertEquals(0, repository.updateCallCount)
    }

    @Test
    fun `edit validation reuses name and command rules`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onEditRequested("saved-command-1")
        viewModel.onEditNameChanged("   ")
        viewModel.onEditCommandChanged(" \n\t ")
        viewModel.onEditConfirmed()

        val form = requireNotNull(viewModel.uiState.value.editForm)

        assertEquals("Name is required.", form.nameError)
        assertEquals("Command text is required.", form.commandError)
        assertEquals(0, repository.updateCallCount)
    }

    @Test
    fun `edit preserves identity creation time and exact command text`() = runTest {
        val originalCommand = savedCommand(
            id = "saved-command-1",
            createdAtEpochMillis = 1_234L,
        )
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(originalCommand),
        )
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        val exactCommand = "\n  printf 'updated'  \n"

        viewModel.onEditRequested("saved-command-1")
        viewModel.onEditNameChanged("  Updated command  ")
        viewModel.onEditCommandChanged(exactCommand)
        viewModel.onEditConfirmed()

        val completedState = viewModel.uiState.first { state ->
            !state.isEditVisible &&
                state.commands.singleOrNull()?.name == "Updated command"
        }

        val updatedCommand = repository.updateArguments.single()

        assertEquals("saved-command-1", updatedCommand.id)
        assertEquals(1_234L, updatedCommand.createdAtEpochMillis)
        assertEquals("Updated command", updatedCommand.name)
        assertEquals(exactCommand, updatedCommand.command)
        assertEquals(updatedCommand, completedState.commands.single())
    }

    @Test
    fun `update failure preserves loaded command and edited form input`() = runTest {
        val originalCommand = savedCommand()
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(originalCommand),
        ).apply {
            updateFailure = IllegalStateException("Database unavailable")
        }
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onEditRequested("saved-command-1")
        viewModel.onEditNameChanged("  Failed update  ")
        viewModel.onEditCommandChanged("  echo failed  ")
        viewModel.onEditConfirmed()

        val failedState = viewModel.uiState.first { state ->
            state.editForm?.errorMessage != null
        }
        val failedForm = requireNotNull(failedState.editForm)

        assertEquals(originalCommand, failedState.commands.single())
        assertEquals("  Failed update  ", failedForm.name)
        assertEquals("  echo failed  ", failedForm.command)
        assertFalse(failedForm.isSaving)
        assertEquals(
            "Saved command could not be updated.",
            failedForm.errorMessage,
        )
        assertEquals(1, repository.updateCallCount)

        repository.updateFailure = null
        viewModel.onEditConfirmed()

        viewModel.uiState.first { state ->
            !state.isEditVisible &&
                state.commands.singleOrNull()?.name == "Failed update"
        }

        assertEquals(2, repository.updateCallCount)
    }

    @Test
    fun `missing edit target before save preserves form and does not update`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onEditRequested("saved-command-1")
        viewModel.onEditNameChanged("Updated command")

        repository.emitSavedCommands(emptyList())
        viewModel.uiState.first { state -> state.commands.isEmpty() }

        viewModel.onEditConfirmed()

        val form = requireNotNull(viewModel.uiState.value.editForm)

        assertEquals("saved-command-1", form.savedCommandId)
        assertEquals("Updated command", form.name)
        assertEquals(
            "Saved command could not be updated.",
            form.errorMessage,
        )
        assertEquals(0, repository.updateCallCount)
    }

    @Test
    fun `prevents duplicate edit confirmation and cancellation while saving`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        ).apply {
            suspendUpdateOperations = true
        }
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onEditRequested("saved-command-1")
        viewModel.onEditNameChanged("Updated command")
        viewModel.onEditConfirmed()
        repository.awaitUpdateStarted()

        assertTrue(
            requireNotNull(viewModel.uiState.value.editForm).isSaving,
        )

        viewModel.onEditConfirmed()
        viewModel.onCancelEdit()

        assertEquals(1, repository.updateCallCount)
        assertTrue(viewModel.uiState.value.isEditVisible)

        repository.releaseUpdate()

        viewModel.uiState.first { state ->
            !state.isEditVisible &&
                state.commands.singleOrNull()?.name == "Updated command"
        }

        assertEquals(1, repository.updateCallCount)
    }

    @Test
    fun `does not expose create edit and delete workflows together`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onOpenCreate()
        viewModel.onEditRequested("saved-command-1")
        viewModel.onDeleteRequested("saved-command-1")

        assertTrue(viewModel.uiState.value.isCreateVisible)
        assertFalse(viewModel.uiState.value.isEditVisible)
        assertFalse(viewModel.uiState.value.isDeleteVisible)

        viewModel.onCancelCreate()
        viewModel.onEditRequested("saved-command-1")
        viewModel.onOpenCreate()
        viewModel.onDeleteRequested("saved-command-1")

        assertFalse(viewModel.uiState.value.isCreateVisible)
        assertTrue(viewModel.uiState.value.isEditVisible)
        assertFalse(viewModel.uiState.value.isDeleteVisible)

        viewModel.onCancelEdit()
        viewModel.onDeleteRequested("saved-command-1")
        viewModel.onOpenCreate()
        viewModel.onEditRequested("saved-command-1")

        assertFalse(viewModel.uiState.value.isCreateVisible)
        assertFalse(viewModel.uiState.value.isEditVisible)
        assertTrue(viewModel.uiState.value.isDeleteVisible)
    }

    @Test
    fun `selects and cancels delete target without changing persistence`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onDeleteRequested("saved-command-1")

        val confirmation = requireNotNull(
            viewModel.uiState.value.deleteConfirmation,
        )
        assertEquals("saved-command-1", confirmation.savedCommandId)
        assertEquals("List services", confirmation.savedCommandName)

        viewModel.onCancelDelete()

        assertFalse(viewModel.uiState.value.isDeleteVisible)
        assertEquals(0, repository.deleteCallCount)
        assertEquals(1, viewModel.uiState.value.commands.size)
    }

    @Test
    fun `does not open delete confirmation for unknown identifier`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onDeleteRequested("missing-command")

        assertFalse(viewModel.uiState.value.isDeleteVisible)
        assertEquals(0, repository.deleteCallCount)
    }

    @Test
    fun `does not expose create and delete workflows together`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onOpenCreate()
        viewModel.onDeleteRequested("saved-command-1")

        assertTrue(viewModel.uiState.value.isCreateVisible)
        assertFalse(viewModel.uiState.value.isDeleteVisible)

        viewModel.onCancelCreate()
        viewModel.onDeleteRequested("saved-command-1")
        viewModel.onOpenCreate()

        assertFalse(viewModel.uiState.value.isCreateVisible)
        assertTrue(viewModel.uiState.value.isDeleteVisible)
    }

    @Test
    fun `confirmed deletion calls repository once and relies on observation`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onDeleteRequested("saved-command-1")
        viewModel.onDeleteConfirmed()

        val completedState = viewModel.uiState.first { state ->
            !state.isDeleteVisible && state.isEmpty
        }

        assertTrue(completedState.commands.isEmpty())
        assertEquals(1, repository.deleteCallCount)
        assertEquals(listOf("saved-command-1"), repository.deleteArguments)
    }

    @Test
    fun `deletion failure preserves loaded command and allows cancellation`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        repository.deleteFailure = IllegalStateException("Database unavailable")
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onDeleteRequested("saved-command-1")
        viewModel.onDeleteConfirmed()

        val failedState = viewModel.uiState.first { state ->
            state.deleteConfirmation?.errorMessage != null
        }
        val confirmation = requireNotNull(failedState.deleteConfirmation)

        assertEquals(1, failedState.commands.size)
        assertEquals("saved-command-1", failedState.commands.single().id)
        assertFalse(confirmation.isDeleting)
        assertEquals(
            "Saved command could not be deleted.",
            confirmation.errorMessage,
        )

        viewModel.onCancelDelete()

        assertFalse(viewModel.uiState.value.isDeleteVisible)
        assertEquals(1, viewModel.uiState.value.commands.size)
    }

    @Test
    fun `retries deletion after failure`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        repository.deleteFailure = IllegalStateException("Database unavailable")
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onDeleteRequested("saved-command-1")
        viewModel.onDeleteConfirmed()
        viewModel.uiState.first { state ->
            state.deleteConfirmation?.errorMessage != null
        }

        repository.deleteFailure = null
        viewModel.onDeleteConfirmed()

        viewModel.uiState.first { state ->
            !state.isDeleteVisible && state.isEmpty
        }

        assertEquals(2, repository.deleteCallCount)
        assertEquals(
            listOf("saved-command-1", "saved-command-1"),
            repository.deleteArguments,
        )
    }

    @Test
    fun `prevents duplicate delete confirmation while deleting`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        ).apply {
            suspendDeleteOperations = true
        }
        val viewModel = createViewModel(repository)
        viewModel.uiState.first { state -> state.hasCommands }

        viewModel.onDeleteRequested("saved-command-1")
        viewModel.onDeleteConfirmed()
        repository.awaitDeleteStarted()

        assertTrue(
            requireNotNull(
                viewModel.uiState.value.deleteConfirmation,
            ).isDeleting,
        )

        viewModel.onDeleteConfirmed()
        viewModel.onCancelDelete()

        assertEquals(1, repository.deleteCallCount)
        assertTrue(viewModel.uiState.value.isDeleteVisible)

        repository.releaseDelete()
        viewModel.uiState.first { state ->
            !state.isDeleteVisible && state.isEmpty
        }

        assertEquals(1, repository.deleteCallCount)
    }

    private fun createViewModel(
        repository: SavedCommandRepository,
    ): SavedCommandsViewModel {
        return SavedCommandsViewModel(
            savedCommandRepository = repository,
            savedCommandFactory = FixedSavedCommandFactory(),
        )
    }

    private fun openCreateForm(
        viewModel: SavedCommandsViewModel,
        name: String,
        command: String,
    ) {
        viewModel.onOpenCreate()
        viewModel.onCreateNameChanged(name)
        viewModel.onCreateCommandChanged(command)
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

    private class FixedSavedCommandFactory : SavedCommandFactory {

        override fun create(
            name: String,
            command: String,
        ): SavedCommand {
            return SavedCommand(
                id = "generated-saved-command",
                name = name,
                command = command,
                createdAtEpochMillis = 4_000L,
            )
        }
    }

    private class PendingSavedCommandRepository : SavedCommandRepository {

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

        override suspend fun updateSavedCommand(
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
