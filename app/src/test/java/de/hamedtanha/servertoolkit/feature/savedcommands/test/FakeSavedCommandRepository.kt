package de.hamedtanha.servertoolkit.feature.savedcommands.test

import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.repository.SavedCommandRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map

class FakeSavedCommandRepository(
    initialCommands: List<SavedCommand> = emptyList(),
) : SavedCommandRepository {

    private val observationUpdates =
        MutableSharedFlow<Result<List<SavedCommand>>>(replay = 1)

    private var currentCommands: List<SavedCommand> = initialCommands
    private val createStartedSignal = CompletableDeferred<Unit>()
    private val createReleaseSignal = CompletableDeferred<Unit>()
    private val deleteStartedSignal = CompletableDeferred<Unit>()
    private val deleteReleaseSignal = CompletableDeferred<Unit>()

    val createArguments: MutableList<SavedCommand> = mutableListOf()
    val deleteArguments: MutableList<String> = mutableListOf()

    var observeCallCount: Int = 0
        private set

    var createCallCount: Int = 0
        private set

    var deleteCallCount: Int = 0
        private set

    var createFailure: Throwable? = null
    var deleteFailure: Throwable? = null
    var suspendCreateOperations: Boolean = false
    var suspendDeleteOperations: Boolean = false

    init {
        emitObservation(Result.success(currentCommands))
    }

    override fun observeSavedCommands(): Flow<List<SavedCommand>> {
        observeCallCount += 1

        return observationUpdates.map { result ->
            result.getOrThrow()
        }
    }

    override suspend fun getSavedCommand(
        savedCommandId: String,
    ): SavedCommand? {
        return currentCommands.firstOrNull { command ->
            command.id == savedCommandId
        }
    }

    override suspend fun createSavedCommand(
        savedCommand: SavedCommand,
    ) {
        createCallCount += 1
        createArguments += savedCommand
        createStartedSignal.complete(Unit)

        if (suspendCreateOperations) {
            createReleaseSignal.await()
        }

        createFailure?.let { failure ->
            throw failure
        }

        currentCommands = listOf(savedCommand) +
            currentCommands.filterNot { command ->
                command.id == savedCommand.id
            }

        emitObservation(Result.success(currentCommands))
    }

    override suspend fun updateSavedCommand(
        savedCommand: SavedCommand,
    ) {
        val existingIndex = currentCommands.indexOfFirst { command ->
            command.id == savedCommand.id
        }

        check(existingIndex >= 0) {
            "Saved command update target does not exist."
        }

        currentCommands = currentCommands.toMutableList().also { commands ->
            commands[existingIndex] = savedCommand
        }

        emitObservation(Result.success(currentCommands))
    }

    override suspend fun deleteSavedCommand(
        savedCommandId: String,
    ) {
        deleteCallCount += 1
        deleteArguments += savedCommandId
        deleteStartedSignal.complete(Unit)

        if (suspendDeleteOperations) {
            deleteReleaseSignal.await()
        }

        deleteFailure?.let { failure ->
            throw failure
        }

        currentCommands = currentCommands.filterNot { command ->
            command.id == savedCommandId
        }

        emitObservation(Result.success(currentCommands))
    }

    fun emitSavedCommands(
        commands: List<SavedCommand>,
    ) {
        currentCommands = commands
        emitObservation(Result.success(currentCommands))
    }

    fun failObservation(
        error: Throwable = IllegalStateException(
            "Saved command observation failed.",
        ),
    ) {
        emitObservation(Result.failure(error))
    }

    suspend fun awaitCreateStarted() {
        createStartedSignal.await()
    }

    fun releaseCreate() {
        createReleaseSignal.complete(Unit)
    }

    suspend fun awaitDeleteStarted() {
        deleteStartedSignal.await()
    }

    fun releaseDelete() {
        deleteReleaseSignal.complete(Unit)
    }

    private fun emitObservation(
        result: Result<List<SavedCommand>>,
    ) {
        check(observationUpdates.tryEmit(result)) {
            "Fake saved command observation update could not be emitted."
        }
    }
}
