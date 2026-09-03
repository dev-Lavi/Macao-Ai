package ai.macao.app.home.viewmodel

import ai.macao.app.data.network.ProfileSummaryResponse
import ai.macao.app.data.repository.ProfileRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val profile: ProfileSummaryResponse) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    private val repository: ProfileRepository = ProfileRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            repository.getProfileSummary()
                .onSuccess { summary ->
                    _uiState.value = ProfileUiState.Success(summary)
                }
                .onFailure { error ->
                    _uiState.value = ProfileUiState.Error(
                        error.localizedMessage ?: "Failed to load profile. Please check your connection."
                    )
                }
        }
    }

    fun refresh() {
        loadProfile()
    }
}
