package ai.macao.app

import ai.macao.app.data.network.*
import org.junit.Assert.*
import org.junit.Test

class AnalyticsUnitTest {

    @Test
    fun testAnalyticsResponseDefaults() {
        val response = AnalyticsResponse()
        assertEquals("Today", response.dateStrip.todayLabel)
        assertEquals(0, response.streak.currentStreak)
        assertEquals(365, response.streak.yearTotalDays)
        assertEquals(0, response.hero.totalXp)
        assertEquals("Improving", response.progress.status)
        assertEquals(0, response.fluency.score)
        assertEquals("Beginner", response.fluency.level)
    }

    @Test
    fun testAnalyticsResponsePopulated() {
        val response = AnalyticsResponse(
            dateStrip = DateStripInfo(
                todayLabel = "Today, 8 Jul",
                todayNumber = 8,
                dates = listOf(
                    DateItem(dayNumber = 6, dateStr = "2026-07-06", isToday = false, label = "6"),
                    DateItem(dayNumber = 7, dateStr = "2026-07-07", isToday = false, label = "7"),
                    DateItem(dayNumber = 8, dateStr = "2026-07-08", isToday = true, label = "Today, 8 Jul"),
                    DateItem(dayNumber = 9, dateStr = "2026-07-09", isToday = false, label = "9")
                )
            ),
            streak = StreakInfo(
                currentStreak = 12,
                longestStreak = 18,
                yearlyActiveDays = 31,
                yearTotalDays = 365,
                streakFreezeCount = 2,
                weekDays = listOf(
                    WeekDayStatus("Mo", "2026-07-06", 6, isActive = true, isToday = false),
                    WeekDayStatus("Tu", "2026-07-07", 7, isActive = true, isToday = false),
                    WeekDayStatus("We", "2026-07-08", 8, isActive = true, isToday = true),
                    WeekDayStatus("Th", "2026-07-09", 9, isActive = false, isToday = false)
                ),
                heatmap = listOf(
                    listOf(0, 1, 2, 2, 1, 0, 0),
                    listOf(1, 2, 3, 2, 1, 1, 0)
                )
            ),
            hero = HeroMetrics(
                totalXp = 1883,
                lessonsCount = 12,
                minutesStudied = 32,
                points = 1248
            ),
            progress = ProgressTracker(
                status = "Improving",
                weeklyBars = listOf(
                    WeeklyBarItem("Mo", 0.45f),
                    WeeklyBarItem("Tu", 0.90f),
                    WeeklyBarItem("We", 0.35f),
                    WeeklyBarItem("Th", 0.88f),
                    WeeklyBarItem("Fr", 0.50f),
                    WeeklyBarItem("Sa", 0.70f)
                )
            ),
            fluency = FluencyTracker(
                score = 82,
                level = "Intermediate",
                status = "Improving",
                breakdown = FluencyBreakdown(
                    accuracy = 85,
                    speaking = 80,
                    vocabulary = 78,
                    consistency = 90
                )
            )
        )

        assertEquals("Today, 8 Jul", response.dateStrip.todayLabel)
        assertEquals(12, response.streak.currentStreak)
        assertEquals(31, response.streak.yearlyActiveDays)
        assertEquals(4, response.streak.weekDays.size)
        assertTrue(response.streak.weekDays[0].isActive)
        assertFalse(response.streak.weekDays[3].isActive)
        assertEquals(1883, response.hero.totalXp)
        assertEquals(12, response.hero.lessonsCount)
        assertEquals(32, response.hero.minutesStudied)
        assertEquals(1248, response.hero.points)
        assertEquals("Improving", response.progress.status)
        assertEquals(6, response.progress.weeklyBars.size)
        assertEquals(82, response.fluency.score)
        assertEquals("Intermediate", response.fluency.level)
        assertEquals(85, response.fluency.breakdown.accuracy)
    }
}
