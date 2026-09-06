package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.Server
import de.hamedtanha.servertoolkit.feature.serverinventory.domain.model.ServerEnvironment
import de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state.ServerInventoryUiState
import de.hamedtanha.servertoolkit.ui.designsystem.theme.ServerToolkitTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ServerInventoryCollectionLayoutUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun constrainedWidthAtMaximumFontScalePreservesContentAndActions() {
        setConstrainedInventory()

        composeTestRule
            .onNodeWithText(LONG_SERVER_NAME)
            .performScrollTo()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(SERVER_INVENTORY_PRIMARY_CONTENT_TEST_TAG)
            .assertWidthIsAtLeast(MIN_PRIMARY_CONTENT_WIDTH)

        composeTestRule
            .onNodeWithText("$LONG_HOST:22")
            .performScrollTo()
            .assertIsDisplayed()

        listOf("Connect", "Edit", "Delete").forEach { action ->
            composeTestRule
                .onNodeWithText(action)
                .performScrollTo()
                .assertIsDisplayed()
                .assertHasClickAction()
        }
    }

    private fun setConstrainedInventory() {
        composeTestRule.setContent {
            val baseDensity = LocalDensity.current

            CompositionLocalProvider(
                LocalDensity provides Density(
                    density = baseDensity.density,
                    fontScale = MAXIMUM_FONT_SCALE,
                ),
            ) {
                ServerToolkitTheme(
                    darkTheme = false,
                ) {
                    Box(
                        modifier = Modifier
                            .width(CONSTRAINED_WIDTH)
                            .fillMaxHeight(),
                    ) {
                        ServerInventoryScreen(
                            uiState = ServerInventoryUiState(
                                servers = listOf(stressServer()),
                                totalServerCount = 1,
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
                }
            }
        }
    }

    private fun stressServer(): Server {
        return Server(
            id = "stress-server-1",
            name = LONG_SERVER_NAME,
            host = LONG_HOST,
            sshPort = 22,
            sshUsername = "operations-administrator-with-a-long-account-name",
            environment = ServerEnvironment.PRODUCTION,
        )
    }

    private companion object {
        const val LONG_SERVER_NAME =
            "Primary Production Gateway for Long Operational Workloads"
        const val LONG_HOST =
            "primary-production-gateway-with-a-long-hostname.example.internal"
        const val MAXIMUM_FONT_SCALE = 2f
        val MIN_PRIMARY_CONTENT_WIDTH = 200.dp
        val CONSTRAINED_WIDTH = 320.dp
    }
}
