package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import de.hamedtanha.servertoolkit.core.connection.domain.model.ConnectionTargetResolution
import de.hamedtanha.servertoolkit.core.connection.domain.model.RemoteConnectionTarget
import de.hamedtanha.servertoolkit.core.test.MainDispatcherRule
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.repository.SavedCommandRepository
import de.hamedtanha.servertoolkit.feature.savedcommands.test.FakeSavedCommandRepository
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionOutput
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandExecutionResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshCommandRequest
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostEndpoint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyFingerprint
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshHostKeyObservationResult
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshObservedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshTrustedHostKey
import de.hamedtanha.servertoolkit.feature.ssh.domain.service.SshCommandExecutionService
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.ConfirmSshHostTrustUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshCommandExecutionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshConnectionAttemptUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustDecisionUseCase
import de.hamedtanha.servertoolkit.feature.ssh.domain.usecase.SshHostTrustEvaluator
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshSavedCommandSelectorUiState
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeConnectionTargetResolver
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionHistoryRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshConnectionService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostKeyObservationService
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshHostTrustRepository
import de.hamedtanha.servertoolkit.feature.ssh.test.FakeSshSessionLifecycleService
import de.hamedtanha.servertoolkit.feature.ssh.test.sshConnectedResult
import de.hamedtanha.servertoolkit.navigation.SshDestination
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SshViewModelSavedCommandSelectorTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `opens selector lazily and cancellation preserves manual command`() = runTest {
        val repository = PendingSavedCommandRepository()
        val fixture = createFixture(repository)

        fixture.viewModel.onConnectionResultReceived(sshConnectedResult())
        fixture.viewModel.onCommandChanged("manual command")

        assertEquals(0, repository.observeCallCount)

        fixture.viewModel.onOpenSavedCommandSelector()

        assertEquals(1, repository.observeCallCount)
        assertEquals(
            SshSavedCommandSelectorUiState.Loading,
            fixture.viewModel.uiState.value.savedCommandSelector,
        )

        fixture.viewModel.onCancelSavedCommandSelector()
        repository.collectionCancelled.await()

        assertEquals(
            SshSavedCommandSelectorUiState.Hidden,
            fixture.viewModel.uiState.value.savedCommandSelector,
        )
        assertEquals(
            "manual command",
            fixture.viewModel.uiState.value.commandExecution.command,
        )
    }

    @Test
    fun `loading selector keeps manual command entry usable`() = runTest {
        val repository = PendingSavedCommandRepository()
        val fixture = createFixture(repository)

        fixture.viewModel.onConnectionResultReceived(sshConnectedResult())
        fixture.viewModel.onOpenSavedCommandSelector()

        assertEquals(
            SshSavedCommandSelectorUiState.Loading,
            fixture.viewModel.uiState.value.savedCommandSelector,
        )

        fixture.viewModel.onCommandChanged("manual while loading")

        assertEquals(
            "manual while loading",
            fixture.viewModel.uiState.value.commandExecution.command,
        )

        fixture.viewModel.onCancelSavedCommandSelector()
        repository.collectionCancelled.await()
    }

    @Test
    fun `empty repository keeps manual command entry usable`() = runTest {
        val repository = FakeSavedCommandRepository()
        val fixture = createFixture(repository)

        fixture.viewModel.onConnectionResultReceived(sshConnectedResult())
        fixture.viewModel.onOpenSavedCommandSelector()

        fixture.viewModel.uiState.first { state ->
            state.savedCommandSelector is SshSavedCommandSelectorUiState.Empty
        }

        fixture.viewModel.onCommandChanged("whoami")

        assertEquals(
            "whoami",
            fixture.viewModel.uiState.value.commandExecution.command,
        )
    }

    @Test
    fun `content preserves repository ordering`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(
                savedCommand(id = "command-2", name = "Second"),
                savedCommand(id = "command-1", name = "First"),
            ),
        )
        val fixture = createFixture(repository)

        fixture.viewModel.onConnectionResultReceived(sshConnectedResult())
        fixture.viewModel.onOpenSavedCommandSelector()

        val selector = fixture.viewModel.uiState.first { state ->
            state.savedCommandSelector is SshSavedCommandSelectorUiState.Content
        }.savedCommandSelector as SshSavedCommandSelectorUiState.Content

        assertEquals(
            listOf("command-2", "command-1"),
            selector.commands.map { command -> command.id },
        )
    }

    @Test
    fun `selection replaces input exactly and never performs ssh work`() = runTest {
        val exactCommand = "  printf 'first\n  second'  "
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(
                savedCommand(
                    id = "exact-command",
                    command = exactCommand,
                ),
            ),
        )
        val fixture = createFixture(repository)

        fixture.viewModel.onConnectionResultReceived(sshConnectedResult())
        fixture.viewModel.onCommandChanged("existing command")
        fixture.viewModel.onOpenSavedCommandSelector()

        fixture.viewModel.uiState.first { state ->
            state.savedCommandSelector is SshSavedCommandSelectorUiState.Content
        }

        fixture.viewModel.onSavedCommandSelected("exact-command")

        assertEquals(
            exactCommand,
            fixture.viewModel.uiState.value.commandExecution.command,
        )
        assertEquals(
            SshSavedCommandSelectorUiState.Hidden,
            fixture.viewModel.uiState.value.savedCommandSelector,
        )

        fixture.viewModel.onSavedCommandSelected("exact-command")

        assertEquals(0, fixture.connectionService.connectCallCount)
        assertEquals(0, fixture.commandExecutionService.executeCallCount)
        assertEquals(0, fixture.connectionHistoryRepository.saveCallCount)
        assertEquals(0, fixture.sessionLifecycleService.closeCallCount)

        fixture.viewModel.onCommandChanged("$exactCommand edited")

        assertEquals(
            "$exactCommand edited",
            fixture.viewModel.uiState.value.commandExecution.command,
        )
    }

    @Test
    fun `unknown stable identifier preserves input and reports selection failure`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(savedCommand()),
        )
        val fixture = createFixture(repository)

        fixture.viewModel.onConnectionResultReceived(sshConnectedResult())
        fixture.viewModel.onCommandChanged("existing command")
        fixture.viewModel.onOpenSavedCommandSelector()

        fixture.viewModel.uiState.first { state ->
            state.savedCommandSelector is SshSavedCommandSelectorUiState.Content
        }

        fixture.viewModel.onSavedCommandSelected("missing-command")

        val selector =
            fixture.viewModel.uiState.value.savedCommandSelector as
                SshSavedCommandSelectorUiState.Content

        assertEquals(
            "existing command",
            fixture.viewModel.uiState.value.commandExecution.command,
        )
        assertEquals(
            "Saved command is no longer available.",
            selector.errorMessage,
        )
        assertTrue(selector.isVisible)
        assertEquals(0, fixture.commandExecutionService.executeCallCount)
    }

    @Test
    fun `initial observation failure is retryable`() = runTest {
        val repository = FakeSavedCommandRepository()
        repository.failObservation()

        val fixture = createFixture(repository)

        fixture.viewModel.onConnectionResultReceived(sshConnectedResult())
        fixture.viewModel.onOpenSavedCommandSelector()

        val failedSelector = fixture.viewModel.uiState.first { state ->
            state.savedCommandSelector is SshSavedCommandSelectorUiState.Failure
        }.savedCommandSelector as SshSavedCommandSelectorUiState.Failure

        assertEquals(
            "Saved commands could not be loaded.",
            failedSelector.errorMessage,
        )
        assertEquals(1, repository.observeCallCount)

        repository.emitSavedCommands(
            listOf(savedCommand(id = "recovered-command")),
        )

        fixture.viewModel.onRetrySavedCommandSelector()

        val recoveredSelector = fixture.viewModel.uiState.first { state ->
            val selector = state.savedCommandSelector

            selector is SshSavedCommandSelectorUiState.Content &&
                !selector.isRetrying &&
                selector.errorMessage == null
        }.savedCommandSelector as SshSavedCommandSelectorUiState.Content

        assertEquals(
            listOf("recovered-command"),
            recoveredSelector.commands.map { command -> command.id },
        )
        assertEquals(2, repository.observeCallCount)
    }

    @Test
    fun `duplicate retry is ignored while retry observation is active`() = runTest {
        val repository = RetryPendingSavedCommandRepository()
        val fixture = createFixture(repository)

        fixture.viewModel.onConnectionResultReceived(sshConnectedResult())
        fixture.viewModel.onOpenSavedCommandSelector()

        fixture.viewModel.uiState.first { state ->
            state.savedCommandSelector is
                SshSavedCommandSelectorUiState.Failure
        }

        fixture.viewModel.onRetrySavedCommandSelector()

        assertEquals(2, repository.observeCallCount)

        val retryingSelector =
            fixture.viewModel.uiState.value.savedCommandSelector as
                SshSavedCommandSelectorUiState.Failure

        assertTrue(retryingSelector.isRetrying)

        fixture.viewModel.onRetrySavedCommandSelector()

        assertEquals(2, repository.observeCallCount)

        fixture.viewModel.onCancelSavedCommandSelector()
        repository.retryCollectionCancelled.await()
    }

    @Test
    fun `later observation failure preserves loaded commands`() = runTest {
        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(
                savedCommand(id = "loaded-command"),
            ),
        )
        val fixture = createFixture(repository)

        fixture.viewModel.onConnectionResultReceived(sshConnectedResult())
        fixture.viewModel.onOpenSavedCommandSelector()

        fixture.viewModel.uiState.first { state ->
            state.savedCommandSelector is SshSavedCommandSelectorUiState.Content
        }

        repository.failObservation()

        val failedSelector = fixture.viewModel.uiState.first { state ->
            val selector = state.savedCommandSelector

            selector is SshSavedCommandSelectorUiState.Content &&
                selector.errorMessage != null
        }.savedCommandSelector as SshSavedCommandSelectorUiState.Content

        assertEquals(
            listOf("loaded-command"),
            failedSelector.commands.map { command -> command.id },
        )
        assertEquals(
            "Saved commands could not be loaded.",
            failedSelector.errorMessage,
        )
    }

    @Test
    fun `explicit execution closes selector and blocks input selection changes`() = runTest {
        val commandStarted = CompletableDeferred<Unit>()
        val releaseCommand = CompletableDeferred<Unit>()

        val commandExecutionService = RecordingSshCommandExecutionService(
            onExecute = { request ->
                assertEquals("uptime", request.command)
                commandStarted.complete(Unit)
                releaseCommand.await()
            },
        )

        val repository = FakeSavedCommandRepository(
            initialCommands = listOf(
                savedCommand(
                    id = "whoami-command",
                    command = "whoami",
                ),
            ),
        )

        val fixture = createFixture(
            savedCommandRepository = repository,
            commandExecutionService = commandExecutionService,
        )

        fixture.viewModel.onConnectionResultReceived(sshConnectedResult())
        fixture.viewModel.onCommandChanged("uptime")
        fixture.viewModel.onOpenSavedCommandSelector()

        fixture.viewModel.uiState.first { state ->
            state.savedCommandSelector is SshSavedCommandSelectorUiState.Content
        }

        val execution = launch {
            fixture.viewModel.executeCommand()
        }

        commandStarted.await()

        assertEquals(
            SshSavedCommandSelectorUiState.Hidden,
            fixture.viewModel.uiState.value.savedCommandSelector,
        )

        fixture.viewModel.onOpenSavedCommandSelector()
        fixture.viewModel.onSavedCommandSelected("whoami-command")
        fixture.viewModel.onCommandChanged("hostname")

        assertEquals(
            "uptime",
            fixture.viewModel.uiState.value.commandExecution.command,
        )
        assertFalse(
            fixture.viewModel.uiState.value.savedCommandSelector.isVisible,
        )

        releaseCommand.complete(Unit)
        execution.join()

        assertEquals(1, commandExecutionService.executeCallCount)
        assertEquals("uptime", commandExecutionService.lastRequest?.command)
    }

    private fun createFixture(
        savedCommandRepository: SavedCommandRepository,
        commandExecutionService: RecordingSshCommandExecutionService =
            RecordingSshCommandExecutionService(),
    ): Fixture {
        val connectionService =
            FakeSshConnectionService(sshConnectedResult())

        val connectionHistoryRepository =
            FakeSshConnectionHistoryRepository()

        val sessionLifecycleService =
            FakeSshSessionLifecycleService()

        val hostTrustRepository = FakeSshHostTrustRepository(
            initialTrustedHostKey = trustedHostKey(),
        )

        val hostTrustDecisionUseCase = SshHostTrustDecisionUseCase(
            hostTrustEvaluator = SshHostTrustEvaluator(
                hostTrustRepository,
            ),
        )

        val viewModel = SshViewModel(
            savedStateHandle = SavedStateHandle(
                mapOf(
                    SshDestination.SERVER_ID_ARGUMENT to "server-1",
                ),
            ),
            savedCommandRepository = savedCommandRepository,
            connectionAttemptUseCase = SshConnectionAttemptUseCase(
                connectionTargetResolver =
                    FakeConnectionTargetResolver(resolvedTarget()),
                connectionService = connectionService,
                hostKeyObservationService =
                    FakeSshHostKeyObservationService(
                        SshHostKeyObservationResult.Observed(
                            observedHostKey(),
                        ),
                    ),
                hostTrustDecisionUseCase = hostTrustDecisionUseCase,
                connectionHistoryRepository =
                    connectionHistoryRepository,
                timeoutMillis = 1_000,
            ),
            confirmHostTrustUseCase = ConfirmSshHostTrustUseCase(
                hostTrustDecisionUseCase = hostTrustDecisionUseCase,
                hostTrustRepository = hostTrustRepository,
            ),
            commandExecutionUseCase =
                SshCommandExecutionUseCase(
                    commandExecutionService,
                ),
            sessionLifecycleService = sessionLifecycleService,
        )

        return Fixture(
            viewModel = viewModel,
            connectionService = connectionService,
            connectionHistoryRepository =
                connectionHistoryRepository,
            commandExecutionService = commandExecutionService,
            sessionLifecycleService = sessionLifecycleService,
        )
    }

    private fun savedCommand(
        id: String = "saved-command-1",
        name: String = "System status",
        command: String = "uptime",
    ): SavedCommand {
        return SavedCommand(
            id = id,
            name = name,
            command = command,
            createdAtEpochMillis = 1_000L,
        )
    }

    private fun resolvedTarget(): ConnectionTargetResolution.Resolved {
        return ConnectionTargetResolution.Resolved(
            RemoteConnectionTarget(
                serverId = "server-1",
                host = "example.com",
                port = 2222,
                username = "admin",
            ),
        )
    }

    private fun endpoint(): SshHostEndpoint {
        return SshHostEndpoint(
            serverId = "server-1",
            host = "example.com",
            port = 22,
        )
    }

    private fun observedHostKey(): SshObservedHostKey {
        return SshObservedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint(),
        )
    }

    private fun trustedHostKey(): SshTrustedHostKey {
        return SshTrustedHostKey(
            endpoint = endpoint(),
            fingerprint = fingerprint(),
        )
    }

    private fun fingerprint(): SshHostKeyFingerprint {
        return SshHostKeyFingerprint(
            algorithm = "SHA256",
            value = "abc123",
        )
    }

    private data class Fixture(
        val viewModel: SshViewModel,
        val connectionService: FakeSshConnectionService,
        val connectionHistoryRepository:
            FakeSshConnectionHistoryRepository,
        val commandExecutionService:
            RecordingSshCommandExecutionService,
        val sessionLifecycleService:
            FakeSshSessionLifecycleService,
    )

    private class RecordingSshCommandExecutionService(
        private val result: SshCommandExecutionResult =
            SshCommandExecutionResult.Completed(
                SshCommandExecutionOutput(
                    stdout = "ok",
                    stderr = "",
                    exitStatus = 0,
                ),
            ),
        private val onExecute:
            suspend (SshCommandRequest) -> Unit = {},
    ) : SshCommandExecutionService {

        var executeCallCount: Int = 0
            private set

        var lastRequest: SshCommandRequest? = null
            private set

        override suspend fun execute(
            request: SshCommandRequest,
        ): SshCommandExecutionResult {
            executeCallCount += 1
            lastRequest = request
            onExecute(request)
            return result
        }
    }

    private class RetryPendingSavedCommandRepository :
        SavedCommandRepository {

        var observeCallCount: Int = 0
            private set

        val retryCollectionCancelled =
            CompletableDeferred<Unit>()

        override fun observeSavedCommands():
            Flow<List<SavedCommand>> {
            observeCallCount += 1

            return if (observeCallCount == 1) {
                flow {
                    throw IllegalStateException(
                        "Initial Saved Command observation failed.",
                    )
                }
            } else {
                flow {
                    try {
                        awaitCancellation()
                    } finally {
                        retryCollectionCancelled.complete(Unit)
                    }
                }
            }
        }

        override suspend fun getSavedCommand(
            savedCommandId: String,
        ): SavedCommand? {
            error("Saved command lookup must not be used.")
        }

        override suspend fun createSavedCommand(
            savedCommand: SavedCommand,
        ) {
            error("Saved command creation must not be used.")
        }

        override suspend fun updateSavedCommand(
            savedCommand: SavedCommand,
        ) {
            error("Saved command update must not be used.")
        }

        override suspend fun deleteSavedCommand(
            savedCommandId: String,
        ) {
            error("Saved command deletion must not be used.")
        }
    }

    private class PendingSavedCommandRepository :
        SavedCommandRepository {

        var observeCallCount: Int = 0
            private set

        val collectionCancelled = CompletableDeferred<Unit>()

        override fun observeSavedCommands():
            Flow<List<SavedCommand>> {
            observeCallCount += 1

            return flow {
                try {
                    awaitCancellation()
                } finally {
                    collectionCancelled.complete(Unit)
                }
            }
        }

        override suspend fun getSavedCommand(
            savedCommandId: String,
        ): SavedCommand? {
            error("Saved command lookup must not be used by selection.")
        }

        override suspend fun createSavedCommand(
            savedCommand: SavedCommand,
        ) {
            error("Saved command creation is not part of SSH selection.")
        }

        override suspend fun updateSavedCommand(
            savedCommand: SavedCommand,
        ) {
            error("Saved command update is not part of SSH selection.")
        }

        override suspend fun deleteSavedCommand(
            savedCommandId: String,
        ) {
            error("Saved command deletion is not part of SSH selection.")
        }
    }
}
