package ai.macao.app.home.viewmodel

import ai.macao.app.data.network.*
import ai.macao.app.data.repository.LearningRepository
import ai.macao.app.home.audio.SpeechRecognizerManager
import ai.macao.app.home.audio.SpeechRecognizerManagerImpl
import ai.macao.app.home.audio.TextToSpeechManager
import ai.macao.app.home.audio.TextToSpeechManagerImpl
import ai.macao.app.home.data.ConversationState
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isOwl: Boolean,
    val text: String,
    val romanization: String = "",
    val meaning: String = ""
)

data class ConversationUiState(
    val state: ConversationState = ConversationState.INITIALIZING,
    val sessionId: String = "",
    val turnNumber: Int = 1,
    val totalTurns: Int = 0,
    val currentTarget: ConversationTarget? = null,
    val transcript: String = "",
    val partialTranscript: String = "",
    val error: String? = null,
    val slowMode: Boolean = false,
    val history: List<ChatMessage> = emptyList(),
    val score: Int = 0,
    val xpEarned: Int = 0,
    val currentStreak: Int = 0,
    val levelUnlocked: Boolean = false,
    val unlockMessage: String = "",
    val correctionMessage: ConversationMessageDetail? = null,
    val isSaving: Boolean = false
)

class ConversationViewModel(
    private val repository: LearningRepository,
    private val speechRecognizer: SpeechRecognizerManager,
    private val textToSpeech: TextToSpeechManager
) : ViewModel() {

    companion object {
        private const val TAG = "ConversationViewModel"

        fun provideFactory(context: Context): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val repository = LearningRepository()
                val speechRecognizer = SpeechRecognizerManagerImpl(context.applicationContext)
                val textToSpeech = TextToSpeechManagerImpl(context.applicationContext)
                return ConversationViewModel(repository, speechRecognizer, textToSpeech) as T
            }
        }
    }

    private val _uiState = MutableStateFlow(ConversationUiState())
    val uiState: StateFlow<ConversationUiState> = _uiState.asStateFlow()

    private var activeLanguageCode = "ja"
    private var activeLevelId = ""
    private var activeLessonId = ""
    private var speakJob: Job? = null

    /**
     * Initializes the conversation session on the server and loads the first target phrase.
     */
    fun startConversation(languageCode: String, levelId: String, lessonId: String) {
        activeLanguageCode = languageCode
        activeLevelId = levelId
        activeLessonId = lessonId

        _uiState.update {
            ConversationUiState(
                state = ConversationState.INITIALIZING,
                history = emptyList()
            )
        }

        viewModelScope.launch {
            repository.startConversation(languageCode, levelId, lessonId, "en")
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            state = ConversationState.SHOWING_TARGET,
                            sessionId = response.sessionId,
                            turnNumber = response.turnNumber,
                            totalTurns = response.totalTurns,
                            currentTarget = response.target,
                            error = null
                        )
                    }
                    speakCurrentTarget()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            state = ConversationState.ERROR,
                            error = error.localizedMessage ?: "Failed to start conversation session."
                        )
                    }
                }
        }
    }

    /**
     * Pronounces the current target phrase using Android Text-to-Speech.
     */
    fun speakCurrentTarget() {
        val target = _uiState.value.currentTarget ?: return
        speakJob?.cancel()
        speakJob = viewModelScope.launch {
            _uiState.update { it.copy(state = ConversationState.OWL_SPEAKING) }
            textToSpeech.speak(target.targetText, activeLanguageCode, _uiState.value.slowMode)
            delay(1500)
            _uiState.update { it.copy(state = ConversationState.SHOWING_TARGET) }
        }
    }

    /**
     * Starts listening using SpeechRecognizerManager.
     */
    fun startListening() {
        if (_uiState.value.state != ConversationState.SHOWING_TARGET &&
            _uiState.value.state != ConversationState.RETRY &&
            _uiState.value.state != ConversationState.FEEDBACK) return

        _uiState.update {
            it.copy(
                state = ConversationState.LISTENING,
                transcript = "",
                partialTranscript = ""
            )
        }

        speechRecognizer.startListening(
            languageCode = activeLanguageCode,
            onPartialResult = { partialText ->
                _uiState.update { it.copy(partialTranscript = partialText) }
            },
            onFinalResult = { finalText ->
                _uiState.update {
                    it.copy(
                        state = ConversationState.PROCESSING,
                        transcript = finalText,
                        partialTranscript = ""
                    )
                }
                submitTurn(finalText)
            },
            onError = { errorText ->
                _uiState.update {
                    it.copy(
                        state = ConversationState.SHOWING_TARGET,
                        error = errorText
                    )
                }
            }
        )
    }

    /**
     * Submits the learner's transcript to the Node API.
     */
    private fun submitTurn(transcriptText: String) {
        val state = _uiState.value
        val sessionId = state.sessionId
        val turnNumber = state.turnNumber

        if (transcriptText.isBlank()) {
            _uiState.update {
                it.copy(
                    state = ConversationState.SHOWING_TARGET,
                    error = "No speech input detected. Please try again."
                )
            }
            return
        }

        viewModelScope.launch {
            repository.submitTurn(sessionId, turnNumber, transcriptText)
                .onSuccess { response ->
                    handleTurnResponse(response, transcriptText)
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            state = ConversationState.SHOWING_TARGET,
                            error = error.localizedMessage ?: "Failed to process speech. Please retry."
                        )
                    }
                }
        }
    }

    /**
     * Evaluates turn results, adds messages to history, and updates UI state.
     */
    private fun handleTurnResponse(response: ConversationTurnResponse, userTranscript: String) {
        val currentHistory = _uiState.value.history.toMutableList()

        // 1. Add Learner Transcript bubble
        currentHistory.add(
            ChatMessage(
                isOwl = false,
                text = userTranscript
            )
        )

        if (response.understood) {
            // 2. Play Owl Response
            val reply = response.owlReply
            if (reply != null) {
                // Add Owl Response bubble
                currentHistory.add(
                    ChatMessage(
                        isOwl = true,
                        text = reply.text,
                        romanization = reply.romanization,
                        meaning = reply.meaning
                    )
                )

                _uiState.update {
                    it.copy(
                        history = currentHistory,
                        correctionMessage = null,
                        error = null
                    )
                }

                speakJob?.cancel()
                speakJob = viewModelScope.launch {
                    _uiState.update { it.copy(state = ConversationState.OWL_SPEAKING) }
                    textToSpeech.speak(reply.text, activeLanguageCode, false)
                    delay(2000)

                    if (response.completed) {
                        completeConversation()
                    } else if (response.nextTarget != null) {
                        _uiState.update {
                            it.copy(
                                state = ConversationState.SHOWING_TARGET,
                                turnNumber = response.turnNumber + 1,
                                currentTarget = response.nextTarget
                            )
                        }
                        speakCurrentTarget()
                    }
                }
            } else {
                // Completed session check
                if (response.completed) {
                    completeConversation()
                }
            }
        } else {
            // Understood is false (Needs practice / correction)
            val correction = response.correction
            if (correction != null) {
                _uiState.update {
                    it.copy(
                        state = ConversationState.FEEDBACK,
                        history = currentHistory,
                        correctionMessage = correction
                    )
                }

                speakJob?.cancel()
                speakJob = viewModelScope.launch {
                    textToSpeech.speak(correction.text, activeLanguageCode, false)
                }
            } else {
                _uiState.update {
                    it.copy(
                        state = ConversationState.RETRY,
                        history = currentHistory
                    )
                }
            }
        }
    }

    /**
     * Triggers ASR retry logic, clearing corrections.
     */
    fun retryTurn() {
        _uiState.update {
            it.copy(
                state = ConversationState.SHOWING_TARGET,
                correctionMessage = null
            )
        }
        startListening()
    }

    /**
     * Completes conversation session on server and aggregates final results.
     */
    private fun completeConversation() {
        _uiState.update { it.copy(state = ConversationState.PROCESSING, isSaving = true) }
        viewModelScope.launch {
            repository.completeConversation(_uiState.value.sessionId, 60)
                .onSuccess { response ->
                    _uiState.update {
                        it.copy(
                            state = ConversationState.COMPLETED,
                            isSaving = false,
                            score = response.averageScore,
                            xpEarned = response.xpEarned,
                            currentStreak = response.streak.currentStreak,
                            levelUnlocked = response.levelUnlocked,
                            unlockMessage = response.unlockMessage
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            state = ConversationState.SHOWING_TARGET,
                            isSaving = false,
                            error = error.localizedMessage ?: "Failed to save final completion status."
                        )
                    }
                }
        }
    }

    /**
     * Toggles slow pronunciation mode for targets.
     */
    fun toggleSlowMode() {
        _uiState.update { it.copy(slowMode = !it.slowMode) }
        speakCurrentTarget()
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Completes and saves conversation progress if the session has turns.
     */
    fun saveAndCompleteIfActive() {
        val state = _uiState.value
        if (state.sessionId.isNotEmpty() && state.history.isNotEmpty() && state.state != ConversationState.COMPLETED) {
            viewModelScope.launch {
                try {
                    repository.completeConversation(state.sessionId, 60)
                } catch (e: Exception) {
                    // Suppress error on exit
                }
            }
        }
    }

    override fun onCleared() {
        speechRecognizer.destroy()
        textToSpeech.shutdown()
        speakJob?.cancel()
        super.onCleared()
    }
}
