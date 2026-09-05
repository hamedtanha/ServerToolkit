package de.hamedtanha.servertoolkit.feature.serverinventory.presentation.viewmodel

import de.hamedtanha.servertoolkit.core.test.MainDispatcherRule
import de.hamedtanha.servertoolkit.feature.serverinventory.test.FakeServerRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AddServerViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `save failure uses stable message without leaking repository exception`() = runTest {
        val repository = FakeServerRepository().apply {
            saveAction = {
                throw IllegalStateException("SQLITE_FULL /private/database/path")
            }
        }
        val viewModel = AddServerViewModel(repository)
        populateValidForm(viewModel)

        viewModel.onSaveClicked()

        val state = viewModel.uiState.value
        assertEquals("Server could not be saved.", state.formMessage)
        assertFalse(state.isSaving)
        assertFalse(state.isSaved)
    }

    @Test
    fun `save cancellation is not converted into repository failure message`() = runTest {
        val repository = FakeServerRepository().apply {
            saveAction = {
                throw CancellationException("cancel save")
            }
        }
        val viewModel = AddServerViewModel(repository)
        populateValidForm(viewModel)

        viewModel.onSaveClicked()

        val state = viewModel.uiState.value
        assertNull(state.formMessage)
        assertFalse(state.isSaved)
        assertTrue(state.isSaving)
    }

    private fun populateValidForm(viewModel: AddServerViewModel) {
        viewModel.onNameChanged("Production")
        viewModel.onHostChanged("prod.example.com")
        viewModel.onPortChanged("22")
        viewModel.onUsernameChanged("admin")
    }
}
