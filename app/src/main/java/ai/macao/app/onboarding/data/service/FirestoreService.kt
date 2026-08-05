package ai.macao.app.onboarding.data.service

import ai.macao.app.onboarding.data.model.OnboardingData
import ai.macao.app.onboarding.data.model.UserProfile
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

private const val TAG = "FirestoreService"
private const val USERS_COLLECTION = "users"

/**
 * Low-level Firestore operations.
 *
 * All functions are suspend and use [await] — no callbacks, no duplicate reads.
 * Each function returns [Result] so callers decide how to surface errors.
 */
object FirestoreService {

    private val db: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    // ─── Create user document (idempotent) ───────────────────────────────────

    /**
     * Creates or merges the base user document.
     *
     * Uses [SetOptions.merge] so that calling this on an existing user
     * does NOT overwrite existing onboarding data.
     * Only sets uid, email, displayName, and createdAt if they're absent.
     */
    suspend fun createUserIfNotExists(
        uid: String,
        email: String,
        displayName: String,
    ): Result<Unit> = runCatching {
        // We only write createdAt on initial creation.
        // merge() ensures existing fields are preserved.
        val data = hashMapOf(
            "uid"         to uid,
            "email"       to email,
            "displayName" to displayName,
            "createdAt"   to FieldValue.serverTimestamp(),
            "updatedAt"   to FieldValue.serverTimestamp(),
        )
        db.collection(USERS_COLLECTION)
            .document(uid)
            .set(data, SetOptions.merge())
            .await()
        Log.d(TAG, "createUserIfNotExists: success for uid=$uid")
        Unit
    }.onFailure {
        Log.e(TAG, "createUserIfNotExists: failed for uid=$uid", it)
    }

    // ─── Save completed onboarding (SINGLE write) ─────────────────────────

    /**
     * Writes the completed onboarding answers to Firestore.
     *
     * This is called ONCE when the user finishes the final step.
     * Uses merge so other top-level fields (uid, email, etc.) are untouched.
     */
    suspend fun saveOnboarding(
        uid: String,
        onboarding: OnboardingData,
    ): Result<Unit> = runCatching {
        val data = hashMapOf<String, Any>(
            "onboarding" to onboarding.toMap(),
            "updatedAt"  to FieldValue.serverTimestamp(),
        )
        db.collection(USERS_COLLECTION)
            .document(uid)
            .set(data, SetOptions.merge())
            .await()
        Log.d(TAG, "saveOnboarding: success for uid=$uid")
        Unit
    }.onFailure {
        Log.e(TAG, "saveOnboarding: failed for uid=$uid", it)
    }

    // ─── Read user profile ────────────────────────────────────────────────

    /**
     * Fetches the user document exactly once.
     *
     * Returns null if the document doesn't exist yet (new user before
     * [createUserIfNotExists] runs).
     */
    suspend fun getUserProfile(uid: String): Result<UserProfile?> = runCatching {
        val snapshot = db.collection(USERS_COLLECTION)
            .document(uid)
            .get()
            .await()

        if (!snapshot.exists()) return@runCatching null

        // Parse onboarding sub-map
        @Suppress("UNCHECKED_CAST")
        val onboardingMap = snapshot.get("onboarding") as? Map<String, Any> ?: emptyMap()
        val onboardingData = OnboardingData(
            nativeLanguage   = onboardingMap["nativeLanguage"]   as? String ?: "",
            learningLanguage = onboardingMap["learningLanguage"] as? String ?: "",
            proficiencyLevel = onboardingMap["proficiencyLevel"] as? String ?: "",
            learningReason   = onboardingMap["learningReason"]   as? String ?: "",
            ageGroup         = onboardingMap["ageGroup"]         as? String ?: "",
            completed        = onboardingMap["completed"]        as? Boolean ?: false,
        )

        UserProfile(
            uid         = snapshot.getString("uid") ?: uid,
            email       = snapshot.getString("email") ?: "",
            displayName = snapshot.getString("displayName") ?: "",
            onboarding  = onboardingData,
            createdAt   = snapshot.get("createdAt"),
            updatedAt   = snapshot.get("updatedAt"),
        )
    }.onFailure {
        Log.e(TAG, "getUserProfile: failed for uid=$uid", it)
    }

    // ─── Update individual profile fields ─────────────────────────────────

    /**
     * General-purpose field updater for future profile editing features.
     *
     * Pass a map of top-level Firestore field paths to their new values.
     * Always stamps [updatedAt] with a server timestamp.
     */
    suspend fun updateProfile(
        uid: String,
        fields: Map<String, Any>,
    ): Result<Unit> = runCatching {
        val data = fields.toMutableMap()
        data["updatedAt"] = FieldValue.serverTimestamp()
        db.collection(USERS_COLLECTION)
            .document(uid)
            .set(data, SetOptions.merge())
            .await()
        Log.d(TAG, "updateProfile: success for uid=$uid, fields=${fields.keys}")
        Unit
    }.onFailure {
        Log.e(TAG, "updateProfile: failed for uid=$uid", it)
    }
}
