package ai.macao.app.onboarding.data.model

/**
 * Holds all five onboarding answers.
 *
 * This object lives in the ViewModel until the user completes the final
 * step, at which point it is written to Firestore in a single set() call.
 */
data class OnboardingData(
    val nativeLanguage: String = "",
    val learningLanguage: String = "",
    val proficiencyLevel: String = "",
    val learningReason: String = "",
    val ageGroup: String = "",
    val completed: Boolean = false,
) {
    /**
     * Converts this model to the nested map that Firestore expects under
     * the "onboarding" key of the users document.
     */
    fun toMap(): Map<String, Any> = mapOf(
        "nativeLanguage"   to nativeLanguage,
        "learningLanguage" to learningLanguage,
        "proficiencyLevel" to proficiencyLevel,
        "learningReason"   to learningReason,
        "ageGroup"         to ageGroup,
        "completed"        to completed,
    )
}

// ─── Proficiency options (Step 3) ────────────────────────────────────────────

enum class ProficiencyLevel(val displayName: String) {
    TOTAL_BEGINNER("Total Beginner"),
    BEGINNER("Beginner"),
    INTERMEDIATE("Intermediate"),
    ADVANCED("Advanced"),
}

// ─── Reason options (Step 4) ─────────────────────────────────────────────────

enum class LearningReason(val displayName: String) {
    CAREER("Career"),
    EDUCATION("Education"),
    FUN_AND_CULTURE("Fun & Culture"),
    DAILY_LIFE("Daily Life"),
    TRAVEL("Travel"),
    FRIENDS_AND_FAMILY("Friends & Family"),
}

// ─── Age group options (Step 5) ──────────────────────────────────────────────

enum class AgeGroup(val displayName: String) {
    AGE_13_17("13–17"),
    AGE_18_24("18–24"),
    AGE_25_34("25–34"),
    AGE_35_44("35–44"),
    AGE_45_54("45–54"),
    AGE_55_PLUS("55+"),
}
