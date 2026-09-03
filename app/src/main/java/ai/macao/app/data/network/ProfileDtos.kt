package ai.macao.app.data.network

/**
 * Top-level response returned by GET /api/v1/users/profile-summary.
 */
data class ProfileSummaryResponse(
    val user: ProfileUserInfo = ProfileUserInfo(),
    val stats: ProfileStats = ProfileStats(),
    val proficiency: ProfileProficiency = ProfileProficiency(),
    val strongestTopics: List<StrongestTopic> = emptyList(),
    val level: ProfileLevelInfo = ProfileLevelInfo(),
    val medals: ProfileMedals = ProfileMedals(),
    val certifications: List<ProfileCertification> = emptyList(),
    val xpHistory: List<XpHistoryPoint> = emptyList()
)

data class ProfileProficiency(
    val learnerLevel: LearnerLevelInfo = LearnerLevelInfo(),
    val speakingAccuracy: ProficiencyMetric = ProficiencyMetric(84, "Speaking accuracy", "Ability to produce correct language"),
    val pronunciationAccuracy: ProficiencyMetric = ProficiencyMetric(89, "Pronunciation accuracy", "Quality of spoken pronunciation"),
    val vocabularyRetention: ProficiencyMetric = ProficiencyMetric(92, "Vocabulary retention rate", "Effectiveness of revision"),
    val errorRate: ErrorRateMetric = ErrorRateMetric(16, 18, 112, "Error rate", "No of errors / total attempt"),
)

data class LearnerLevelInfo(
    val code: String = "A1",
    val name: String = "Beginner",
    val description: String = "Check proficiency: Basic vocabulary and elementary phrases",
)

data class ProficiencyMetric(
    val percentage: Int = 0,
    val label: String = "",
    val description: String = "",
)

data class ErrorRateMetric(
    val percentage: Int = 0,
    val totalErrors: Int = 0,
    val totalAttempts: Int = 0,
    val label: String = "Error rate",
    val description: String = "No of errors / total attempt",
)

data class ProfileUserInfo(
    val id: String = "",
    val username: String = "",
    val displayName: String = "",
    val profileImageUrl: String = "",
    val membership: String = "Pro Member",
    val category: String = "Japanese"
)

data class ProfileStats(
    val quizzes: Int = 0,
    val leaderboardRank: Int = 1,
    val accuracy: Int = 0,
    val streakDays: Int = 0
)

data class StrongestTopic(
    val id: String = "",
    val name: String = "",
    val icon: String = "food",
    val accuracy: Int = 0
)

data class ProfileLevelInfo(
    val currentLevel: Int = 1,
    val currentXp: Int = 0,
    val nextLevelXp: Int = 1000,
    val xpToNextLevel: Int = 1000,
    val progressPercentage: Double = 0.0
)

data class ProfileMedals(
    val total: Int = 0,
    val gold: Int = 0,
    val silver: Int = 0,
    val bronze: Int = 0
)

data class ProfileCertification(
    val id: String = "",
    val name: String = "",
    val level: String = "BRONZE",
    val icon: String = "certificate"
)

data class XpHistoryPoint(
    val date: String = "",
    val dayLabel: String = "",
    val xp: Int = 0
)
