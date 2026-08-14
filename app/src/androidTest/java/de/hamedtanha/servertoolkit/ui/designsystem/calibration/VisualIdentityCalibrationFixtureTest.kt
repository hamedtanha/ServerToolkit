package de.hamedtanha.servertoolkit.ui.designsystem.calibration

import android.graphics.Bitmap
import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.hamedtanha.servertoolkit.feature.dashboard.presentation.screen.DashboardScreen
import de.hamedtanha.servertoolkit.feature.dashboard.presentation.state.DashboardUiState
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.screen.SavedCommandsScreen
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state.SavedCommandsUiState
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.ServerEnvironment
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.screen.ServerFormScreen
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.screen.ServerInventoryScreen
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerFormUiState
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerInventoryUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.screen.SshScreen
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshCommandExecutionStatus
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshCommandExecutionUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshConnectionStatus
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshUiState
import de.hamedtanha.servertoolkit.ui.designsystem.theme.DefaultServerToolkitVisualProfile
import de.hamedtanha.servertoolkit.ui.designsystem.theme.ServerToolkitTheme
import java.io.File
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VisualIdentityCalibrationFixtureTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun captureF01DashboardLight() {
        captureDashboard(
            darkTheme = false,
            fileName = "F01-light.png",
        )
    }

    @Test
    fun captureF01DashboardDark() {
        captureDashboard(
            darkTheme = true,
            fileName = "F01-dark.png",
        )
    }

    @Test
    fun captureF02ServerInventoryLight() {
        captureServerInventory(
            darkTheme = false,
            fileName = "F02-light.png",
        )
    }

    @Test
    fun captureF02ServerInventoryDark() {
        captureServerInventory(
            darkTheme = true,
            fileName = "F02-dark.png",
        )
    }

    @Test
    fun captureF03ServerFormLight() {
        captureServerForm(
            darkTheme = false,
            fileName = "F03-light.png",
        )
    }

    @Test
    fun captureF03ServerFormDark() {
        captureServerForm(
            darkTheme = true,
            fileName = "F03-dark.png",
        )
    }

    @Test
    fun captureF04SshLight() {
        captureSsh(
            darkTheme = false,
            fileName = "F04-light.png",
        )
    }

    @Test
    fun captureF04SshDark() {
        captureSsh(
            darkTheme = true,
            fileName = "F04-dark.png",
        )
    }

    @Test
    fun captureF05SavedCommandsLight() {
        captureSavedCommands(
            darkTheme = false,
            fileName = "F05-light.png",
        )
    }

    @Test
    fun captureF05SavedCommandsDark() {
        captureSavedCommands(
            darkTheme = true,
            fileName = "F05-dark.png",
        )
    }

    private fun captureDashboard(
        darkTheme: Boolean,
        fileName: String,
    ) {
        setFixtureContent(darkTheme = darkTheme) {
            DashboardScreen(
                uiState = DashboardUiState(
                    title = "Server Toolkit",
                ),
                onOpenServerInventory = {},
                onOpenSavedCommands = {},
            )
        }

        captureRoot(fileName)
    }

    private fun captureServerInventory(
        darkTheme: Boolean,
        fileName: String,
    ) {
        setFixtureContent(darkTheme = darkTheme) {
            ServerInventoryScreen(
                uiState = ServerInventoryUiState(
                    servers = calibrationServers(),
                    totalServerCount = 2,
                ),
                onAddServerClick = {},
                onEditServerClick = {},
                onConnectServerClick = {},
                onSearchQueryChanged = {},
                onEnvironmentFilterChanged = {},
                onFavoritesOnlyChanged = {},
                onClearFilters = {},
                onDeleteServerConfirmed = {},
            )
        }

        captureRoot(fileName)
    }

    private fun captureServerForm(
        darkTheme: Boolean,
        fileName: String,
    ) {
        setFixtureContent(darkTheme = darkTheme) {
            ServerFormScreen(
                uiState = ServerFormUiState(
                    title = "Add server",
                    description = "Enter the connection details for this server.",
                    name = "Production Gateway",
                    host = "203.0.113.10",
                    port = "22",
                    username = "ops",
                ),
                onNameChanged = {},
                onHostChanged = {},
                onPortChanged = {},
                onUsernameChanged = {},
                onSaveClicked = {},
                onNavigateBack = {},
            )
        }

        captureRoot(fileName)
    }

    private fun captureSsh(
        darkTheme: Boolean,
        fileName: String,
    ) {
        setFixtureContent(darkTheme = darkTheme) {
            SshScreen(
                uiState = calibrationSshUiState(),
                onAuthenticationMethodSelect = {},
                onPrivateKeySelectClick = {},
                onConnectClick = {},
                onDisconnectClick = {},
                onConfirmHostKeyClick = {},
                onCancelHostKeyReviewClick = {},
                onPasswordChange = {},
                onPrivateKeyPassphraseChange = {},
                onCommandChange = {},
                onOpenSavedCommandSelector = {},
                onRetrySavedCommandSelector = {},
                onCancelSavedCommandSelector = {},
                onSavedCommandSelect = {},
                onExecuteCommandClick = {},
                onOpenConnectionHistory = {},
                onNavigateBack = {},
            )
        }

        captureRoot(fileName)
    }

    private fun captureSavedCommands(
        darkTheme: Boolean,
        fileName: String,
    ) {
        setFixtureContent(darkTheme = darkTheme) {
            SavedCommandsScreen(
                uiState = SavedCommandsUiState(
                    commands = calibrationSavedCommands(),
                ),
                onNavigateBack = {},
                onRetryLoad = {},
                onOpenCreate = {},
                onCancelCreate = {},
                onCreateNameChanged = {},
                onCreateCommandChanged = {},
                onCreateConfirmed = {},
                onEditRequested = {},
                onCancelEdit = {},
                onEditNameChanged = {},
                onEditCommandChanged = {},
                onEditConfirmed = {},
                onDeleteRequested = {},
                onCancelDelete = {},
                onDeleteConfirmed = {},
            )
        }

        captureRoot(fileName)
    }

    private fun setFixtureContent(
        darkTheme: Boolean,
        content: @Composable () -> Unit,
    ) {
        assumeCalibrationCaptureEnabled()
        requireCaptureSourceRef()
        requireEvidenceStage()
        requireBaselineFontScale()

        composeTestRule.setContent {
            ServerToolkitTheme(
                darkTheme = darkTheme,
                visualProfile = DefaultServerToolkitVisualProfile,
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ) {
                    content()
                }
            }
        }

        composeTestRule.waitForIdle()
    }

    private fun assumeCalibrationCaptureEnabled() {
        val enabled = InstrumentationRegistry
            .getArguments()
            .getString(CAPTURE_ENABLED_ARGUMENT)
            ?.toBooleanStrictOrNull()
            ?: false

        assumeTrue(
            "Visual identity calibration capture is opt-in.",
            enabled,
        )
    }

    private fun requireCaptureSourceRef(): String {
        val sourceRef = InstrumentationRegistry
            .getArguments()
            .getString(SOURCE_REF_ARGUMENT)
            ?.trim()
            .orEmpty()

        check(sourceRef.isNotEmpty()) {
            "Visual calibration requires -e $SOURCE_REF_ARGUMENT <commit-or-ref>."
        }

        return sourceRef
    }

    private fun requireEvidenceStage(): String {
        val stage = InstrumentationRegistry
            .getArguments()
            .getString(EVIDENCE_STAGE_ARGUMENT)
            ?.trim()
            .orEmpty()

        check(EVIDENCE_STAGE_PATTERN.matches(stage)) {
            "Visual calibration requires -e $EVIDENCE_STAGE_ARGUMENT " +
                "<lowercase-stage-name>; received '$stage'."
        }

        return stage
    }

    private fun requireBaselineFontScale() {
        val configuration = InstrumentationRegistry
            .getInstrumentation()
            .targetContext
            .resources
            .configuration

        check(configuration.fontScale == BASELINE_FONT_SCALE) {
            "Visual calibration requires fontScale=$BASELINE_FONT_SCALE, " +
                "but the device reports fontScale=${configuration.fontScale}."
        }
    }

    private fun captureRoot(fileName: String) {
        composeTestRule.waitForIdle()

        val bitmap = composeTestRule
            .onRoot()
            .captureToImage()
            .asAndroidBitmap()

        val directory = evidenceDirectory()
        val outputFile = File(directory, fileName)

        outputFile.outputStream().use { output ->
            check(
                bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    PNG_QUALITY,
                    output,
                ),
            ) {
                "Failed to encode calibration evidence: ${outputFile.absolutePath}"
            }
        }

        writeEnvironmentMetadata(directory)

        println("VISUAL_IDENTITY_EVIDENCE=${outputFile.absolutePath}")
    }

    private fun evidenceDirectory(): File {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        val externalRoot = requireNotNull(context.getExternalFilesDir(null)) {
            "External files directory is unavailable."
        }

        val directory = File(
            externalRoot,
            "$EVIDENCE_ROOT/${requireEvidenceStage()}",
        )

        check(directory.isDirectory || directory.mkdirs()) {
            "Failed to create calibration evidence directory: ${directory.absolutePath}"
        }

        return directory
    }

    private fun writeEnvironmentMetadata(directory: File) {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        val metrics = context.resources.displayMetrics
        val configuration = context.resources.configuration

        File(
            directory,
            ENVIRONMENT_FILE_NAME,
        ).writeText(
            buildString {
                appendLine("evidenceStage=${requireEvidenceStage()}")
                appendLine("fixtureSourceRef=${requireCaptureSourceRef()}")
                appendLine("manufacturer=${Build.MANUFACTURER}")
                appendLine("model=${Build.MODEL}")
                appendLine("api=${Build.VERSION.SDK_INT}")
                appendLine("widthPixels=${metrics.widthPixels}")
                appendLine("heightPixels=${metrics.heightPixels}")
                appendLine("densityDpi=${metrics.densityDpi}")
                appendLine("fontScale=${configuration.fontScale}")
                appendLine(
                    "dynamicColorPolicy=" +
                        DefaultServerToolkitVisualProfile.dynamicColorPolicy,
                )
            },
        )
    }

    private fun calibrationServers(): List<Server> {
        return listOf(
            Server(
                id = "server-prod-01",
                name = "Production Gateway",
                host = "203.0.113.10",
                sshPort = 22,
                sshUsername = "ops",
                environment = ServerEnvironment.PRODUCTION,
            ),
            Server(
                id = "server-stage-01",
                name = "Staging API",
                host = "198.51.100.20",
                sshPort = 2222,
                sshUsername = "deploy",
                environment = ServerEnvironment.STAGING,
            ),
        )
    }

    private fun calibrationSavedCommands(): List<SavedCommand> {
        return listOf(
            SavedCommand(
                id = "command-uptime",
                name = "System uptime",
                command = "uptime",
                createdAtEpochMillis = 1_000L,
            ),
            SavedCommand(
                id = "command-disk",
                name = "Disk usage",
                command = "df -h",
                createdAtEpochMillis = 2_000L,
            ),
        )
    }

    private fun calibrationSshUiState(): SshUiState {
        return SshUiState(
            serverId = "server-prod-01",
            status = SshConnectionStatus.Connected,
            statusLabel = "Connected",
            message = "SSH connection is ready.",
            detail = "A project-owned SSH session handle was opened.",
            commandExecution = SshCommandExecutionUiState(
                command = "uptime",
                status = SshCommandExecutionStatus.Completed,
                statusLabel = "Command completed",
                message = "Command execution completed.",
                detail = "Exit status: 0",
                stdout = "up 14 days, 3:21",
                stderr = "",
                exitStatus = 0,
            ),
        )
    }

    private companion object {
        const val EVIDENCE_ROOT = "visual-identity-calibration"
        const val ENVIRONMENT_FILE_NAME = "environment.txt"
        const val PNG_QUALITY = 100
        const val BASELINE_FONT_SCALE = 1.0f

        const val CAPTURE_ENABLED_ARGUMENT = "visualIdentityCalibration"
        const val EVIDENCE_STAGE_ARGUMENT = "visualIdentityStage"
        const val SOURCE_REF_ARGUMENT = "visualIdentitySourceRef"

        val EVIDENCE_STAGE_PATTERN = Regex("[a-z0-9]+(?:-[a-z0-9]+)*")
    }
}
