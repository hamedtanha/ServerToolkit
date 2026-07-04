package de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import de.hamedtanha.servertoolkit.feature.ssh.domain.model.SshConnectionResult
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshUiState
import de.hamedtanha.servertoolkit.feature.ssh.presentation.state.withConnectionResult
import de.hamedtanha.servertoolkit.navigation.SshDestination
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class SshViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val serverId: String = checkNotNull(savedStateHandle[SshDestination.SERVER_ID_ARGUMENT])

    private val _uiState = MutableStateFlow(
        SshUiState(serverId = serverId),
    )
    val uiState: StateFlow<SshUiState> = _uiState.asStateFlow()

    internal fun onConnectionResultReceived(result: SshConnectionResult) {
        _uiState.value = _uiState.value.withConnectionResult(result)
    }
}
