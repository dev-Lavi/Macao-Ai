package ai.macao.app.home.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ai.macao.app.data.network.AnalyticsResponse
import ai.macao.app.data.repository.AnalyticsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AnalyticsUiState {
    object Loading : AnalyticsUiState
    data class Success(
        val data: AnalyticsResponse,
        val selectedDateStr: String,
    ) : AnalyticsUiState
    data class Error(val message: String) : AnalyticsUiState
}

class AnalyticsViewModel(
    private val repository: AnalyticsRepository = AnalyticsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnalyticsUiState>(AnalyticsUiState.Loading)
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        loadAnalytics()
    }

    fun loadAnalytics(languageCode: String? = null) {
        _uiState.value = AnalyticsUiState.Loading
        viewModelScope.launch {
            repository.getAnalyticsSummary(languageCode)
                .onSuccess { response ->
                    val defaultDate = response.dateStrip.dates.firstOrNull { it.isToday }?.dateStr
                        ?: response.dateStrip.dates.firstOrNull()?.dateStr
                        ?: ""
                    _uiState.value = AnalyticsUiState.Success(
                        data = response,
                        selectedDateStr = defaultDate
                    )
                }
                .onFailure { error ->
                    _uiState.value = AnalyticsUiState.Error(
                        error.localizedMessage ?: "Failed to load analytics. Please try again."
                    )
                }
        }
    }

    fun refresh(languageCode: String? = null) {
        viewModelScope.launch {
            repository.getAnalyticsSummary(languageCode)
                .onSuccess { response ->
                    val currentSelected = (_uiState.value as? AnalyticsUiState.Success)?.selectedDateStr
                    val defaultDate = currentSelected?.ifEmpty { null }
                        ?: response.dateStrip.dates.firstOrNull { it.isToday }?.dateStr
                        ?: ""
                    _uiState.value = AnalyticsUiState.Success(
                        data = response,
                        selectedDateStr = defaultDate
                    )
                }
                .onFailure { error ->
                    if (_uiState.value is AnalyticsUiState.Loading) {
                        _uiState.value = AnalyticsUiState.Error(
                            error.localizedMessage ?: "Failed to load analytics."
                        )
                    }
                }
        }
    }

    fun selectDate(dateStr: String) {
        val current = _uiState.value
        if (current is AnalyticsUiState.Success) {
            _uiState.value = current.copy(selectedDateStr = dateStr)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AnalyticsViewModel() as T
            }
        }
    }
}
