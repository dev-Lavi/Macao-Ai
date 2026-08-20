package ai.macao.app.data.repository

import ai.macao.app.data.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository handling learning data, levels, lessons content, and progress tracking.
 * Connects directly to the Render REST API via [MacaoApiClient].
 */
class LearningRepository {

    private val api = MacaoApiClient.apiService

    /**
     * Fetches the complete list of levels with units and lessons for a target language.
     */
    suspend fun getLevels(languageCode: String): Result<List<LevelResponse>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getLevels(languageCode)
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error?.message ?: "Failed to load levels.")
            }
        }
    }

    /**
     * Fetches the complete lesson content (items array) for a specific lesson.
     */
    suspend fun getLessonById(
        lessonId: String,
        languageCode: String,
        levelId: String
    ): Result<LessonContentResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getLessonById(lessonId, languageCode, levelId)
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error?.message ?: "Failed to load lesson content.")
            }
        }
    }

    /**
     * Saves user lesson progress (XP, score, accuracy, completion state).
     */
    suspend fun saveProgress(
        languageCode: String,
        levelId: String,
        lessonId: String,
        score: Int,
        accuracy: Double,
        completed: Boolean,
        timeSpentSeconds: Int
    ): Result<ProgressSaveResultResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = mapOf(
                "languageCode" to languageCode,
                "levelId" to levelId,
                "lessonId" to lessonId,
                "score" to score,
                "accuracy" to accuracy,
                "completed" to completed,
                "timeSpentSeconds" to timeSpentSeconds
            )
            val response = api.saveProgress(payload)
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error?.message ?: "Failed to save lesson progress.")
            }
        }
    }

    /**
     * Checks requirements and unlocks the subsequent level on the server.
     */
    suspend fun unlockLevel(languageCode: String, levelId: String): Result<UnlockLevelResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = mapOf(
                "languageCode" to languageCode,
                "levelId" to levelId
            )
            val response = api.unlockLevel(payload)
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error?.message ?: "Failed to unlock level.")
            }
        }
    }

    /**
     * Retrieves overall user progress statistics for a language.
     */
    suspend fun getProgress(languageCode: String): Result<ProgressResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getProgress(languageCode)
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error?.message ?: "Failed to fetch user progress.")
            }
        }
    }

    /**
     * Starts a conversation session on the server.
     */
    suspend fun startConversation(
        languageCode: String,
        levelId: String,
        lessonId: String,
        nativeLanguage: String
    ): Result<StartConversationResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.startConversation(
                StartConversationRequest(languageCode, levelId, lessonId, nativeLanguage)
            )
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error?.message ?: "Failed to start conversation.")
            }
        }
    }

    /**
     * Submits a conversation turn with transcript.
     */
    suspend fun submitTurn(
        sessionId: String,
        turnNumber: Int,
        transcript: String
    ): Result<ConversationTurnResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.submitTurn(
                sessionId,
                ConversationTurnRequest(turnNumber, transcript)
            )
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error?.message ?: "Failed to submit turn.")
            }
        }
    }

    /**
     * Completes the conversation session on the server and retrieves stats/level unlocks.
     */
    suspend fun completeConversation(
        sessionId: String,
        timeSpentSeconds: Int
    ): Result<CompleteConversationResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.completeConversation(
                sessionId,
                CompleteConversationRequest(timeSpentSeconds)
            )
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error?.message ?: "Failed to complete conversation.")
            }
        }
    }

    /**
     * Fetches situation missions for a language and level from backend API.
     */
    suspend fun getSituationMissions(
        languageCode: String,
        levelId: String = "level_1"
    ): Result<List<ai.macao.app.home.data.MissionData>> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getSituationMissions(languageCode, levelId)
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error?.message ?: "Failed to load situation missions.")
            }
        }
    }

    /**
     * Fetches full mission details & activities for a single mission from backend API.
     */
    suspend fun getSituationMissionById(
        missionId: String,
        languageCode: String
    ): Result<ai.macao.app.home.data.MissionData> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.getSituationMissionById(missionId, languageCode)
            if (response.success && response.data != null) {
                response.data
            } else {
                // Return local localized provider fallback if backend is unreachable or returning empty
                ai.macao.app.home.data.MissionRepository.getAirportMission(languageCode)
            }
        }.recover {
            ai.macao.app.home.data.MissionRepository.getAirportMission(languageCode)
        }
    }

    /**
     * Submits an activity answer attempt to the backend API for server-side validation.
     */
    suspend fun submitActivityAttempt(
        activityId: String,
        answer: String,
        languageCode: String
    ): Result<ActivityAttemptResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val response = api.submitActivityAttempt(activityId, ActivityAttemptRequest(answer, languageCode))
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error?.message ?: "Failed to submit activity attempt.")
            }
        }
    }

    /**
     * Records mission completion on the backend API and receives XP rewards & level unlocks.
     */
    suspend fun completeSituationMission(
        missionId: String,
        languageCode: String,
        score: Int = 100,
        accuracy: Double = 100.0,
        timeSpentSeconds: Int = 120
    ): Result<SituationMissionCompleteResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val payload = SituationMissionCompleteRequest(languageCode, score, accuracy, timeSpentSeconds)
            val response = api.completeSituationMission(missionId, payload)
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error?.message ?: "Failed to complete mission on backend.")
            }
        }
    }
}
