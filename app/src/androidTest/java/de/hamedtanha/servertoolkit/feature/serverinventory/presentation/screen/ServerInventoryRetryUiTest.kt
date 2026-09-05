package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.screen

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerFormUiState
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerInventoryUiState
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServerInventoryRetryUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun blockingInventoryFailureRetryInvokesCallback() {
        var retryCallCount = 0

        setInventoryScreen(
            uiState = ServerInventoryUiState(
                errorMessage = "Server inventory could not be loaded.",
            ),
            onRetryLoad = {
                retryCallCount += 1
            },
        )

        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
            .performClick()

        assertEquals(1, retryCallCount)
    }

    @Test
    fun laterInventoryFailureKeepsDataVisibleAndRetryInvokesCallback() {
        var retryCallCount = 0

        setInventoryScreen(
            uiState = ServerInventoryUiState(
                servers = listOf(server()),
                totalServerCount = 1,
                errorMessage = "Server inventory could not be loaded.",
            ),
            onRetryLoad = {
                retryCallCount += 1
            },
        )

        composeTestRule
            .onNodeWithText("Production")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Server inventory could not be loaded.")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Retry")
            .assertIsDisplayed()
            .performClick()

        assertEquals(1, retryCallCount)
    }

    @Test
    fun editLoadFailureRetryInvokesCallback() {
        var retryCallCount = 0

        composeTestRule.setContent {
            MaterialTheme {
                ServerFormScreen(
                    uiState = ServerFormUiState(
                        title = "Edit server",
                        description = "Update the connection details for this server.",
                        formMessage = "Server could not be loaded.",
                        canRetryLoad = true,
                    ),
                    onNameChanged = {},
                    onHostChanged = {},
                    onPortChanged = {},
                    onUsernameChanged = {},
                    onSaveClicked = {},
                    onNavigateBack = {},
                    onRetryLoad = {
                        retryCallCount += 1
                    },
                )
            }
        }

        composeTestRule
            .onNodeWithText("Retry loading server")
            .assertIsDisplayed()
            .performClick()

        assertEquals(1, retryCallCount)
    }

    private fun setInventoryScreen(
        uiState: ServerInventoryUiState,
        onRetryLoad: () -> Unit,
    ) {
        composeTestRule.setContent {
            MaterialTheme {
                ServerInventoryScreen(
                    uiState = uiState,
                    onAddServerClick = {},
                    onEditServerClick = {},
                    onConnectServerClick = {},
                    onSearchQueryChanged = {},
                    onEnvironmentFilterChanged = {},
                    onFavoritesOnlyChanged = {},
                    onClearFilters = {},
                    onDeleteServerConfirmed = {},
                    onRetryLoad = onRetryLoad,
                )
            }
        }
    }

    private fun server(): Server {
        return Server(
            id = "server-1",
            name = "Production",
            host = "prod.example.com",
            sshUsername = "admin",
        )
    }
}
