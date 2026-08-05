package ai.macao.app.onboarding.data.model

/**
 * Top-level Firestore document stored at users/{uid}.
 *
 * Fields map 1-to-1 with the Firestore document structure.
 * [createdAt] and [updatedAt] are stored as server timestamps via
 * FieldValue.serverTimestamp() — typed as Any? here to allow that.
 */
data class UserProfile(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val onboarding: OnboardingData = OnboardingData(),
    val createdAt: Any? = null,
    val updatedAt: Any? = null,
) {
    /**
     * Returns true when this user has already completed onboarding.
     * Used in MainActivity to skip the profile setup flow for returning users.
     */
    val hasCompletedOnboarding: Boolean
        get() = onboarding.completed
}
