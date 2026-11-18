package ai.macao.app.data.repository

import ai.macao.app.data.network.AnalyticsResponse
import ai.macao.app.data.network.MacaoApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository providing Daily Streak, Progress, and Fluency Score analytics.
 */
class AnalyticsRepository {

    suspend fun getAnalyticsSummary(languageCode: String? = null): Result<AnalyticsResponse> =
        withContext(Dispatchers.IO) {
            try {
                val response = MacaoApiClient.apiService.getAnalyticsSummary(languageCode)
                if (response.success && response.data != null) {
                    Result.success(response.data)
                } else {
                    Result.failure(Exception(response.error?.message ?: "Failed to fetch analytics summary."))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
