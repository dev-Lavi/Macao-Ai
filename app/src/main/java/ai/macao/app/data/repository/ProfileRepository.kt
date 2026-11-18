package ai.macao.app.data.repository

import ai.macao.app.data.network.MacaoApiClient
import ai.macao.app.data.network.ProfileSummaryResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository providing aggregated user profile statistics, badges, and progress data.
 */
class ProfileRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val api = MacaoApiClient.apiService

    suspend fun getProfileSummary(): Result<ProfileSummaryResponse> = withContext(Dispatchers.IO) {
        runCatching {
            val user = firebaseAuth.currentUser
                ?: throw IllegalStateException("No authenticated user")

            val response = api.getProfileSummary()
            if (response.success && response.data != null) {
                response.data
            } else {
                throw Exception(response.error?.message ?: "Failed to load profile summary.")
            }
        }
    }
}
