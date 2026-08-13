package ai.macao.app.onboarding.viewmodel

import ai.macao.app.onboarding.data.model.OnboardingData
import ai.macao.app.onboarding.data.repository.UserRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ─── UI State ────────────────────────────────────────────────────────────────

/**
 * Immutable snapshot of everything the UI needs to render the onboarding flow.
 *
 * All five answer fields live here. They are updated one by one as the
 * user progresses through the steps — NO Firestore writes happen until
 * [isComplete] flips to true.
 */
data class OnboardingUiState(
    /** Current active step (1–7). Steps 6–7 are post-onboarding screens. */
    val currentStep: Int = 1,

    // ── Accumulated answers ───────────────────────────────────────────────
    val nativeLanguage: String = "",
    val learningLanguage: String = "",
    val proficiencyLevel: String = "",
    val learningReason: String = "",
    val ageGroup: String = "",

    // ── Async state ──────────────────────────────────────────────────────
    /** True while the Firestore write is in-flight. */
    val isLoading: Boolean = false,

    /** Non-null when a Firestore error occurred. Cleared after the user dismisses. */
    val error: String? = null,

    /** Flips to true after the user taps the CTA on the All Set screen. */
    val isComplete: Boolean = false,
)

// ─── ViewModel ───────────────────────────────────────────────────────────────

/**
 * Owns all onboarding state and orchestrates the single Firestore write.
 *
 * Design principles:
 * - Each `select*` function updates in-memory state only.
 * - [finishOnboarding] is called exactly once — on the last step's selection.
 * - The UI never calls the repository directly.
 */
class OnboardingViewModel(
    private val repository: UserRepository = UserRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    // ─── Step 1: Native language ─────────────────────────────────────────────

    fun selectNativeLanguage(language: String) {
        _uiState.update { it.copy(nativeLanguage = language, currentStep = 2) }
    }

    // ─── Step 2: Learning language ───────────────────────────────────────────

    fun selectLearningLanguage(language: String) {
        _uiState.update { it.copy(learningLanguage = language, currentStep = 3) }
    }

    // ─── Step 3: Proficiency level ───────────────────────────────────────────

    fun selectProficiencyLevel(level: String) {
        _uiState.update { it.copy(proficiencyLevel = level, currentStep = 4) }
    }

    // ─── Step 4: Learning reason ─────────────────────────────────────────────

    fun selectLearningReason(reason: String) {
        _uiState.update { it.copy(learningReason = reason, currentStep = 5) }
    }

    // ─── Step 5: Age group (triggers Firestore write) ────────────────────────

    fun selectAgeGroup(ageGroup: String) {
        _uiState.update { it.copy(ageGroup = ageGroup) }
        finishOnboarding(ageGroup)
    }

    // ─── Navigation ──────────────────────────────────────────────────────────

    /** Steps back one screen; no-op on step 1. */
    fun goBack() {
        val currentStep = _uiState.value.currentStep
        if (currentStep in 2..5) {
            _uiState.update { it.copy(currentStep = currentStep - 1) }
        }
    }

    /** Clears the current error so the UI can dismiss the error message. */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Called by [CompilingDataScreen] when its animation completes.
     * Moves the flow to step 7 (All Set screen).
     */
    fun advanceToAllSet() {
        _uiState.update { it.copy(currentStep = 7) }
    }

    /**
     * Called when the user taps "Let's Start Learning" on the All Set screen.
     * Sets [isComplete] = true → [ProfileSetupFlow] navigates to MainApp.
     */
    fun confirmComplete() {
        _uiState.update { it.copy(isComplete = true) }
    }

    // ─── Single Firestore write ──────────────────────────────────────────────

    /**
     * Builds [OnboardingData] from current state and writes it to Firestore.
     *
     * This is the ONLY place where Firestore is written during onboarding.
     * On success, [OnboardingUiState.isComplete] = true → UI navigates away.
     * On failure, [OnboardingUiState.error] is set → UI shows error message.
     */
    private fun finishOnboarding(ageGroup: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val state = _uiState.value
            val onboardingData = OnboardingData(
                nativeLanguage   = state.nativeLanguage,
                learningLanguage = state.learningLanguage,
                proficiencyLevel = state.proficiencyLevel,
                learningReason   = state.learningReason,
                ageGroup         = ageGroup,
                completed        = true,
            )

            repository.saveOnboarding(onboardingData)
                .onSuccess {
                    // Move to step 6 (Compiling screen).
                    // isComplete is NOT set here — the user must tap
                    // the CTA button on the All Set screen (step 7).
                    _uiState.update { it.copy(isLoading = false, currentStep = 6) }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.localizedMessage
                                ?: "Failed to save profile. Please try again.",
                        )
                    }
                }
        }
    }

    // ─── Factory ─────────────────────────────────────────────────────────────

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OnboardingViewModel() as T
            }
        }
    }
}
