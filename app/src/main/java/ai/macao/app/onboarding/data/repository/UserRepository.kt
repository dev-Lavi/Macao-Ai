package ai.macao.app.onboarding.data.repository

import ai.macao.app.onboarding.data.model.OnboardingData
import ai.macao.app.onboarding.data.model.UserProfile
import ai.macao.app.onboarding.data.service.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * Single source of truth for user data.
 *
 * The ViewModel talks only to this class. Direct Firestore calls are
 * isolated in [FirestoreService] so this class stays testable.
 */
class UserRepository(
    private val firestoreService: FirestoreService = FirestoreService,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) {

    // ─── Auth helpers ────────────────────────────────────────────────────────

    /**
     * Returns the currently authenticated [FirebaseUser] or null.
     * Does NOT perform a Firestore read.
     */
    fun getCurrentUser(): FirebaseUser? = firebaseAuth.currentUser

    // ─── Firestore operations ────────────────────────────────────────────────

    /**
     * Ensures a users/{uid} document exists with baseline fields.
     *
     * Safe to call on every sign-up/sign-in — merge semantics mean
     * existing onboarding data is never overwritten.
     */
    suspend fun createUserIfNotExists(): Result<Unit> {
        val user = firebaseAuth.currentUser
            ?: return Result.failure(IllegalStateException("No authenticated user"))

        return firestoreService.createUserIfNotExists(
            uid         = user.uid,
            email       = user.email ?: "",
            displayName = user.displayName ?: user.email?.substringBefore('@') ?: "",
        )
    }

    /**
     * Performs the single Firestore write at the end of onboarding.
     *
     * Called ONLY once — when the user taps the last step's option.
     * Marks [OnboardingData.completed] = true before writing.
     */
    suspend fun saveOnboarding(onboarding: OnboardingData): Result<Unit> {
        val uid = firebaseAuth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("No authenticated user"))

        val completedOnboarding = onboarding.copy(completed = true)
        return firestoreService.saveOnboarding(uid, completedOnboarding)
    }

    /**
     * Fetches the full user profile from Firestore exactly once.
     *
     * Used by [MainActivity] at startup to decide whether to show
     * the profile setup flow or skip straight to the main app.
     */
    suspend fun getUserProfile(): Result<UserProfile?> {
        val uid = firebaseAuth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("No authenticated user"))

        return firestoreService.getUserProfile(uid)
    }

    /**
     * Updates arbitrary top-level profile fields.
     *
     * Provided for future profile-editing screens. Not called during onboarding.
     */
    suspend fun updateProfile(fields: Map<String, Any>): Result<Unit> {
        val uid = firebaseAuth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("No authenticated user"))

        return firestoreService.updateProfile(uid, fields)
    }
}
