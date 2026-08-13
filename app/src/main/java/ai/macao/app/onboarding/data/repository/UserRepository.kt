package ai.macao.app.onboarding.data.repository

import ai.macao.app.data.network.MacaoApiClient
import ai.macao.app.onboarding.data.model.OnboardingData
import ai.macao.app.onboarding.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

/**
 * Single source of truth for user data.
 * Redirects all data operations to the Render REST API instead of direct Firestore writes/reads.
 */
class UserRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) {

    // ─── Auth helpers ────────────────────────────────────────────────────────

    /**
     * Returns the currently authenticated [FirebaseUser] or null.
     */
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    // ─── REST API operations ────────────────────────────────────────────────

    /**
     * Ensures a users/{uid} document exists with baseline fields.
     */
    suspend fun createUserIfNotExists(): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No authenticated user")

        val response = MacaoApiClient.apiService.getProfile()
        if (!response.success || response.data == null) {
            throw Exception(response.error?.message ?: "Failed to get or create profile")
        }
        Unit
    }

    /**
     * Performs the single API write at the end of onboarding.
     */
    suspend fun saveOnboarding(onboarding: OnboardingData): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No authenticated user")

        val completedOnboarding = onboarding.copy(completed = true)
        val fields = mapOf("onboarding" to completedOnboarding.toMap())
        val response = MacaoApiClient.apiService.updateProfile(fields)
        if (!response.success || response.data == null) {
            throw Exception(response.error?.message ?: "Failed to save onboarding")
        }
        Unit
    }

    /**
     * Fetches the full user profile from REST API.
     */
    suspend fun getUserProfile(): Result<UserProfile?> = runCatching {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No authenticated user")

        val response = MacaoApiClient.apiService.getProfile()
        if (response.success) {
            response.data
        } else {
            throw Exception(response.error?.message ?: "Failed to get profile")
        }
    }

    /**
     * Updates arbitrary top-level profile fields.
     */
    suspend fun updateProfile(fields: Map<String, Any>): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No authenticated user")

        val response = MacaoApiClient.apiService.updateProfile(fields)
        if (!response.success) {
            throw Exception(response.error?.message ?: "Failed to update profile")
        }
        Unit
    }

    /**
     * Updates both Firebase Auth display name and the REST user document.
     */
    suspend fun updateUserProfile(
        name: String,
        username: String,
        settings: Map<String, Any>
    ): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No authenticated user")

        // Update Firebase Auth Display Name
        val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()
        user.updateProfile(profileUpdates).await()

        // Update REST API Document
        val fields = hashMapOf<String, Any>(
            "displayName" to name,
            "username" to username
        )
        fields.putAll(settings)
        val response = MacaoApiClient.apiService.updateProfile(fields)
        if (!response.success) {
            throw Exception(response.error?.message ?: "REST API update failed")
        }
        Unit
    }

    /**
     * Deletes user document from REST API and then deletes authenticated user account.
     */
    suspend fun deleteAccount(): Result<Unit> = runCatching {
        val user = firebaseAuth.currentUser
            ?: throw IllegalStateException("No authenticated user")

        // 1. Delete user profile doc from Firestore via REST API
        val response = MacaoApiClient.apiService.deleteProfile()
        if (!response.success) {
            throw Exception(response.error?.message ?: "Profile deletion failed")
        }

        // 2. Delete Auth Account
        user.delete().await()
        Unit
    }
}

