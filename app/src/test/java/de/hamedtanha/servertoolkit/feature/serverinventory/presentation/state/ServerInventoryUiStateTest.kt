package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.state

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ServerInventoryUiStateTest {

    @Test
    fun inventoryEmpty_whenLastFilteredServerWasDeleted_returnsTrue() {
        val uiState = ServerInventoryUiState(
            servers = emptyList(),
            totalServerCount = 0,
            filter = ServerInventoryFilter(searchQuery = "production"),
        )

        assertTrue(uiState.isInventoryEmpty)
        assertFalse(uiState.isFilterResultEmpty)
        assertFalse(uiState.hasVisibleServers)
    }

    @Test
    fun filterResultEmpty_whenInventoryStillContainsServers_returnsTrue() {
        val uiState = ServerInventoryUiState(
            servers = emptyList(),
            totalServerCount = 1,
            filter = ServerInventoryFilter(searchQuery = "no-match"),
        )

        assertFalse(uiState.isInventoryEmpty)
        assertTrue(uiState.isFilterResultEmpty)
        assertFalse(uiState.hasVisibleServers)
    }
}
