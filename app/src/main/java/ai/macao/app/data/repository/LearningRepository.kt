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
}
