package ai.macao.app.home.viewmodel

import ai.macao.app.data.network.*
import ai.macao.app.data.repository.LearningRepository
import ai.macao.app.home.data.SpeechPracticeState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface LevelsUiState {
    object Loading : LevelsUiState
    data class Success(val levels: List<LevelResponse>) : LevelsUiState
    data class Error(val message: String) : LevelsUiState
}

sealed interface LessonUiState {
    object Idle : LessonUiState
    object Loading : LessonUiState
    data class Content(
        val lesson: LessonContentResponse,
        val currentIndex: Int = 0,
        val practiceState: SpeechPracticeState = SpeechPracticeState.IDLE
    ) : LessonUiState
    data class Completed(
        val xpEarned: Int,
        val accuracy: Double,
        val isSaving: Boolean = false,
        val error: String? = null
    ) : LessonUiState
    data class Error(val message: String) : LessonUiState
}

class LearningViewModel(
    private val repository: LearningRepository = LearningRepository()
) : ViewModel() {

    private val _levelsState = MutableStateFlow<LevelsUiState>(LevelsUiState.Loading)
    val levelsState: StateFlow<LevelsUiState> = _levelsState.asStateFlow()

    private val _lessonState = MutableStateFlow<LessonUiState>(LessonUiState.Idle)
    val lessonState: StateFlow<LessonUiState> = _lessonState.asStateFlow()

    private var activeLanguageCode: String = "ja"
    private var activeLevelId: String = ""
    private var activeLessonId: String = ""

    /**
     * Loads the levels list for the specified language.
     */
    fun loadLevels(languageCode: String) {
        activeLanguageCode = languageCode
        viewModelScope.launch {
            _levelsState.value = LevelsUiState.Loading
            repository.getLevels(languageCode)
                .onSuccess { list ->
                    _levelsState.value = LevelsUiState.Success(list)
                }
                .onFailure { err ->
                    _levelsState.value = LevelsUiState.Error(err.localizedMessage ?: "Failed to load levels.")
                }
        }
    }

    /**
     * Loads the items for a single lesson and starts it.
     */
    fun startLesson(lessonId: String, languageCode: String, levelId: String) {
        activeLanguageCode = languageCode
        activeLevelId = levelId
        activeLessonId = lessonId

        viewModelScope.launch {
            _lessonState.value = LessonUiState.Loading
            repository.getLessonById(lessonId, languageCode, levelId)
                .onSuccess { lesson ->
                    _lessonState.value = LessonUiState.Content(lesson = lesson)
                }
                .onFailure { err ->
                    _lessonState.value = LessonUiState.Error(err.localizedMessage ?: "Failed to load lesson.")
                }
        }
    }

    /**
     * Simulates speech interaction practice step (tap mic action).
     */
    fun startSpeechPractice() {
        val currentState = _lessonState.value
        if (currentState !is LessonUiState.Content) return

        if (currentState.practiceState != SpeechPracticeState.IDLE &&
            currentState.practiceState != SpeechPracticeState.INCORRECT) return

        viewModelScope.launch {
            // Update to Listening
            _lessonState.value = currentState.copy(practiceState = SpeechPracticeState.LISTENING)
            delay(2000)

            // Update to Processing
            _lessonState.value = currentState.copy(practiceState = SpeechPracticeState.PROCESSING)
            delay(1200)

            // Update to Correct
            _lessonState.value = currentState.copy(practiceState = SpeechPracticeState.CORRECT)
        }
    }

    /**
     * Advances to the next card, or triggers lesson completion if on the last card.
     */
    fun advanceOrComplete() {
        val currentState = _lessonState.value
        if (currentState !is LessonUiState.Content) return

        val items = currentState.lesson.items
        if (currentState.currentIndex < items.lastIndex) {
            // Move to next card
            _lessonState.value = currentState.copy(
                currentIndex = currentState.currentIndex + 1,
                practiceState = SpeechPracticeState.IDLE
            )
        } else {
            // Lesson completed! Accrued XP is 10 XP per vocabulary item.
            val xp = items.size * 10
            _lessonState.value = LessonUiState.Completed(
                xpEarned = xp,
                accuracy = 100.0
            )
        }
    }

    /**
     * Saves progress to the Render API and evaluates subsequent level unlocks.
     */
    fun saveProgressAndUnlock(onComplete: () -> Unit) {
        val completedState = _lessonState.value
        if (completedState !is LessonUiState.Completed) return

        _lessonState.value = completedState.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                // 1. Save lesson progress
                repository.saveProgress(
                    languageCode = activeLanguageCode,
                    levelId = activeLevelId,
                    lessonId = activeLessonId,
                    score = 100, // 100% pronunciation success
                    accuracy = completedState.accuracy,
                    completed = true,
                    timeSpentSeconds = 60 // Simulated time spent
                ).getOrThrow()

                // 2. Evaluates unlocks for next level
                repository.unlockLevel(
                    languageCode = activeLanguageCode,
                    levelId = activeLevelId
                )

                // 3. Reload levels to refresh map
                loadLevels(activeLanguageCode)

                // Reset lesson state and return
                _lessonState.value = LessonUiState.Idle
                onComplete()
            } catch (e: Exception) {
                _lessonState.value = completedState.copy(
                    isSaving = false,
                    error = e.localizedMessage ?: "Failed to save progress. Please try again."
                )
            }
        }
    }

    /**
     * Resets active lesson state back to idle.
     */
    fun cancelLesson() {
        _lessonState.value = LessonUiState.Idle
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LearningViewModel() as T
            }
        }
    }
}
