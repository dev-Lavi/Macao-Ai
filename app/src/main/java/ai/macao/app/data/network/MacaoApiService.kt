package ai.macao.app.data.network

import ai.macao.app.onboarding.data.model.UserProfile
import okhttp3.MultipartBody
import retrofit2.http.*

interface MacaoApiService {

    @GET("users/me")
    suspend fun getProfile(): ApiResponse<UserProfile>

    @PATCH("users/me")
    suspend fun updateProfile(
        @Body fields: Map<String, @JvmSuppressWildcards Any>
    ): ApiResponse<UserProfile>

    @Multipart
    @POST("users/profile-image")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part
    ): ApiResponse<UserProfile>

    @DELETE("users/profile-image")
    suspend fun deleteProfileImage(): ApiResponse<UserProfile>

    @DELETE("users/me")
    suspend fun deleteProfile(): ApiResponse<Unit>

    // Learning endpoints
    @GET("languages")
    suspend fun getLanguages(): ApiResponse<List<String>>

    @GET("languages/{languageCode}/levels")
    suspend fun getLevels(
        @Path("languageCode") languageCode: String
    ): ApiResponse<List<LevelResponse>>

    @GET("languages/{languageCode}/levels/{levelId}")
    suspend fun getLevelById(
        @Path("languageCode") languageCode: String,
        @Path("levelId") levelId: String
    ): ApiResponse<LevelResponse>

    @GET("languages/{languageCode}/levels/{levelId}/lessons")
    suspend fun getLessons(
        @Path("languageCode") languageCode: String,
        @Path("levelId") levelId: String
    ): ApiResponse<List<UnitResponse>>

    @GET("lessons/{lessonId}")
    suspend fun getLessonById(
        @Path("lessonId") lessonId: String,
        @Query("languageCode") languageCode: String,
        @Query("levelId") levelId: String
    ): ApiResponse<LessonContentResponse>

    // Progress endpoints
    @GET("progress")
    suspend fun getProgress(
        @Query("languageCode") languageCode: String
    ): ApiResponse<ProgressResponse>

    @POST("progress")
    suspend fun saveProgress(
        @Body payload: Map<String, @JvmSuppressWildcards Any>
    ): ApiResponse<ProgressSaveResultResponse>

    @POST("progress/unlock")
    suspend fun unlockLevel(
        @Body payload: Map<String, @JvmSuppressWildcards Any>
    ): ApiResponse<UnlockLevelResponse>
}

// Data models representing backend response data structures
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val error: ApiError?
)

data class ApiError(
    val code: String,
    val message: String
)

data class LevelResponse(
    val id: String,
    val order: Int,
    val title: String,
    val description: String,
    val difficulty: String,
    val requiredScore: Int,
    val estimatedMinutes: Int,
    val unlocked: Boolean,
    val units: List<UnitResponse>?
)

data class UnitResponse(
    val id: String,
    val order: Int,
    val title: String,
    val description: String,
    val category: String,
    val totalLessons: Int,
    val completedLessonsCount: Int,
    val lessons: List<LessonMetadataResponse>
)

data class LessonMetadataResponse(
    val id: String,
    val order: Int,
    val title: String,
    val description: String,
    val estimatedMinutes: Int,
    val itemCount: Int,
    val completed: Boolean,
    val bestScore: Int
)

data class LessonContentResponse(
    val id: String,
    val levelId: String,
    val unitId: String,
    val languageCode: String,
    val title: String,
    val description: String,
    val order: Int,
    val category: String,
    val items: List<LessonItemResponse>
)

data class LessonItemResponse(
    val id: String,
    val targetLanguage: String,
    val nativeLanguage: String,
    val word: String,
    val translation: String,
    val pronunciation: String,
    val partOfSpeech: String,
    val category: String,
    val exampleSentence: String,
    val exampleTranslation: String,
    val difficulty: String,
    val order: Int,
    val published: Boolean
)

data class ProgressResponse(
    val languageCode: String,
    val currentLevel: String,
    val unlockedLevels: List<String>,
    val stats: ProgressStatsResponse,
    val streak: StreakResponse,
    val completedLessons: Map<String, CompletedLessonResponse>,
    val lastStudiedAt: String?
)

data class ProgressStatsResponse(
    val totalXp: Int,
    val lessonsCompletedCount: Int,
    val totalTimeSpentSeconds: Int,
    val averageAccuracy: Double
)

data class StreakResponse(
    val currentStreak: Int,
    val longestStreak: Int
)

data class CompletedLessonResponse(
    val score: Int,
    val accuracy: Double,
    val completed: Boolean,
    val completedAt: String,
    val attemptsCount: Int,
    val bestScore: Int
)

data class ProgressSaveResultResponse(
    val xpEarned: Int,
    val totalXp: Int,
    val streak: StreakResponse,
    val completedLessonsCount: Int,
    val bestScore: Int
)

data class UnlockLevelResponse(
    val unlocked: Boolean,
    val alreadyUnlocked: Boolean,
    val message: String,
    val unlockedLevels: List<String>,
    val currentLevel: String,
    val reason: String?,
    val requiredCompletedLessons: Int?,
    val actualCompletedLessons: Int?,
    val requiredScore: Int?,
    val actualAverageScore: Int?
)
