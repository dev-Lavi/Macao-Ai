package ai.macao.app

import ai.macao.app.data.network.*
import org.junit.Assert.*
import org.junit.Test

class ProfileUnitTest {

    @Test
    fun testProfileSummaryResponseDefaults() {
        val defaultProfile = ProfileSummaryResponse()
        assertEquals("", defaultProfile.user.id)
        assertEquals("Pro Member", defaultProfile.user.membership)
        assertEquals("Japanese", defaultProfile.user.category)
        assertEquals(0, defaultProfile.stats.quizzes)
        assertEquals(0, defaultProfile.stats.accuracy)
        assertEquals(1, defaultProfile.level.currentLevel)
        assertEquals(0, defaultProfile.medals.total)
        assertTrue(defaultProfile.strongestTopics.isEmpty())
        assertTrue(defaultProfile.certifications.isEmpty())
        assertTrue(defaultProfile.xpHistory.isEmpty())
        assertEquals("A1", defaultProfile.proficiency.learnerLevel.code)
        assertEquals(84, defaultProfile.proficiency.speakingAccuracy.percentage)
        assertEquals(89, defaultProfile.proficiency.pronunciationAccuracy.percentage)
        assertEquals(92, defaultProfile.proficiency.vocabularyRetention.percentage)
        assertEquals(16, defaultProfile.proficiency.errorRate.percentage)
    }

    @Test
    fun testProfileSummaryResponsePopulated() {
        val profile = ProfileSummaryResponse(
            user = ProfileUserInfo(
                id = "user_123",
                username = "Shinomiya",
                displayName = "Shinomiya",
                profileImageUrl = "https://example.com/avatar.jpg",
                membership = "Pro Member",
                category = "Japanese"
            ),
            stats = ProfileStats(
                quizzes = 55,
                leaderboardRank = 2,
                accuracy = 83,
                streakDays = 12
            ),
            proficiency = ProfileProficiency(
                learnerLevel = LearnerLevelInfo("B1", "Intermediate", "Check proficiency: Everyday communication"),
                speakingAccuracy = ProficiencyMetric(85, "Speaking accuracy", "Ability to produce correct language"),
                pronunciationAccuracy = ProficiencyMetric(90, "Pronunciation accuracy", "Quality of spoken pronunciation"),
                vocabularyRetention = ProficiencyMetric(88, "Vocabulary retention rate", "Effectiveness of revision"),
                errorRate = ErrorRateMetric(15, 18, 120, "Error rate", "No of errors / total attempt")
            ),
            strongestTopics = listOf(
                StrongestTopic(id = "topic_1", name = "Food Life style", icon = "food", accuracy = 28),
                StrongestTopic(id = "topic_2", name = "Cars Brand", icon = "cars", accuracy = 35),
                StrongestTopic(id = "topic_3", name = "Social Media", icon = "social", accuracy = 40)
            ),
            level = ProfileLevelInfo(
                currentLevel = 2,
                currentXp = 5200,
                nextLevelXp = 6000,
                xpToNextLevel = 800,
                progressPercentage = 84.0
            ),
            medals = ProfileMedals(
                total = 53,
                gold = 24,
                silver = 18,
                bronze = 11
            ),
            certifications = listOf(
                ProfileCertification(id = "c1", name = "Food Safety Protocols", level = "BRONZE", icon = "certificate"),
                ProfileCertification(id = "c2", name = "Facilities & Maintenance", level = "SILVER", icon = "certificate")
            ),
            xpHistory = listOf(
                XpHistoryPoint(date = "2026-08-27", dayLabel = "Sa", xp = 320),
                XpHistoryPoint(date = "2026-08-28", dayLabel = "Su", xp = 450)
            )
        )

        assertEquals("Shinomiya", profile.user.displayName)
        assertEquals(55, profile.stats.quizzes)
        assertEquals(2, profile.stats.leaderboardRank)
        assertEquals(83, profile.stats.accuracy)
        assertEquals(12, profile.stats.streakDays)
        assertEquals("B1", profile.proficiency.learnerLevel.code)
        assertEquals("Intermediate", profile.proficiency.learnerLevel.name)
        assertEquals(85, profile.proficiency.speakingAccuracy.percentage)
        assertEquals(90, profile.proficiency.pronunciationAccuracy.percentage)
        assertEquals(88, profile.proficiency.vocabularyRetention.percentage)
        assertEquals(15, profile.proficiency.errorRate.percentage)
        assertEquals(18, profile.proficiency.errorRate.totalErrors)
        assertEquals(120, profile.proficiency.errorRate.totalAttempts)
        assertEquals(3, profile.strongestTopics.size)
        assertEquals("food", profile.strongestTopics[0].icon)
        assertEquals(2, profile.level.currentLevel)
        assertEquals(53, profile.medals.total)
        assertEquals(24, profile.medals.gold)
        assertEquals(2, profile.certifications.size)
        assertEquals(2, profile.xpHistory.size)
    }
}
