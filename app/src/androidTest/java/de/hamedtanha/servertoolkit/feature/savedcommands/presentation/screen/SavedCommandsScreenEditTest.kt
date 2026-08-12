package de.hamedtanha.servertoolkit.feature.savedcommands.presentation.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.hamedtanha.servertoolkit.feature.savedcommands.domain.model.SavedCommand
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state.SavedCommandEditFormUiState
import de.hamedtanha.servertoolkit.feature.savedcommands.presentation.state.SavedCommandsUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SavedCommandsScreenEditTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun editActionRequestsCorrectSavedCommand() {
        var requestedCommandId: String? = null

        setScreen(
            uiState = SavedCommandsUiState(
                commands = listOf(savedCommand()),
            ),
            onEditRequested = { commandId ->
                requestedCommandId = commandId
            },
        )

        composeTestRule
            .onNodeWithTag(
                "${SAVED_COMMAND_EDIT_BUTTON_TEST_TAG_PREFIX}saved-command-1",
            )
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        assertEquals("saved-command-1", requestedCommandId)
    }

    @Test
    fun editDialogShowsExistingValues() {
        setScreen(
            uiState = SavedCommandsUiState(
                commands = listOf(savedCommand()),
                editForm = editForm(),
            ),
        )

        composeTestRule
            .onNodeWithText("Edit saved command")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(SAVED_COMMAND_EDIT_NAME_TEST_TAG)
            .assertIsDisplayed()
            .assertTextContains("System status")

        composeTestRule
            .onNodeWithTag(SAVED_COMMAND_EDIT_COMMAND_TEST_TAG)
            .assertIsDisplayed()
            .assertTextContains("systemctl status")
    }

    @Test
    fun editDialogExposesSaveAndCancelActions() {
        var saveCallCount = 0
        var cancelCallCount = 0

        setScreen(
            uiState = SavedCommandsUiState(
                commands = listOf(savedCommand()),
                editForm = editForm(),
            ),
            onEditConfirmed = {
                saveCallCount += 1
            },
            onCancelEdit = {
                cancelCallCount += 1
            },
        )

        composeTestRule
            .onNodeWithTag(SAVED_COMMAND_EDIT_SAVE_TEST_TAG)
            .assertIsDisplayed()
            .assertIsEnabled()
            .performClick()

        composeTestRule
            .onNodeWithText("Cancel")
            .assertIsDisplayed()
            .performClick()

        assertEquals(1, saveCallCount)
        assertEquals(1, cancelCallCount)
    }

    @Test
    fun savingEditDisablesInputsAndSaveAction() {
        setScreen(
            uiState = SavedCommandsUiState(
                commands = listOf(savedCommand()),
                editForm = editForm(
                    isSaving = true,
                ),
            ),
        )

        composeTestRule
            .onNodeWithTag(SAVED_COMMAND_EDIT_NAME_TEST_TAG)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag(SAVED_COMMAND_EDIT_COMMAND_TEST_TAG)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag(SAVED_COMMAND_EDIT_SAVE_TEST_TAG)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithText("Saving")
            .assertIsDisplayed()
    }

    @Test
    fun failedEditShowsErrorAndRetryAction() {
        var saveCallCount = 0

        setScreen(
            uiState = SavedCommandsUiState(
                commands = listOf(savedCommand()),
                editForm = editForm(
                    errorMessage = "Saved command could not be updated.",
                ),
            ),
            onEditConfirmed = {
                saveCallCount += 1
            },
        )

        composeTestRule
            .onNodeWithText("Saved command could not be updated.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(SAVED_COMMAND_EDIT_SAVE_TEST_TAG)
            .assertIsEnabled()
            .performClick()

        assertEquals(1, saveCallCount)
    }

    private fun setScreen(
        uiState: SavedCommandsUiState,
        onEditRequested: (String) -> Unit = {},
        onCancelEdit: () -> Unit = {},
        onEditConfirmed: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                SavedCommandsScreen(
                    uiState = uiState,
                    onNavigateBack = {},
                    onRetryLoad = {},
                    onOpenCreate = {},
                    onCancelCreate = {},
                    onCreateNameChanged = {},
                    onCreateCommandChanged = {},
                    onCreateConfirmed = {},
                    onEditRequested = onEditRequested,
                    onCancelEdit = onCancelEdit,
                    onEditNameChanged = {},
                    onEditCommandChanged = {},
                    onEditConfirmed = onEditConfirmed,
                    onDeleteRequested = {},
                    onCancelDelete = {},
                    onDeleteConfirmed = {},
                )
            }
        }
    }

    private fun savedCommand(): SavedCommand {
        return SavedCommand(
            id = "saved-command-1",
            name = "System status",
            command = "systemctl status",
            createdAtEpochMillis = 1_000L,
        )
    }

    private fun editForm(
        isSaving: Boolean = false,
        errorMessage: String? = null,
    ): SavedCommandEditFormUiState {
        return SavedCommandEditFormUiState(
            savedCommandId = "saved-command-1",
            name = "System status",
            command = "systemctl status",
            isSaving = isSaving,
            errorMessage = errorMessage,
        )
    }
}
