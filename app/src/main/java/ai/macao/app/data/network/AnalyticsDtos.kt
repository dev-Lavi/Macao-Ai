package ai.macao.app.data.network

import com.google.gson.annotations.SerializedName

/**
 * Data Transfer Objects for the Analytics / Streaks & Learning Tracker endpoint:
 * GET /api/v1/users/analytics
 */

data class AnalyticsResponse(
    @SerializedName("dateStrip")
    val dateStrip: DateStripInfo = DateStripInfo(),

    @SerializedName("streak")
    val streak: StreakInfo = StreakInfo(),

    @SerializedName("hero")
    val hero: HeroMetrics = HeroMetrics(),

    @SerializedName("progress")
    val progress: ProgressTracker = ProgressTracker(),

    @SerializedName("fluency")
    val fluency: FluencyTracker = FluencyTracker(),
)

data class DateStripInfo(
    @SerializedName("todayLabel")
    val todayLabel: String = "Today",

    @SerializedName("todayNumber")
    val todayNumber: Int = 0,

    @SerializedName("dates")
    val dates: List<DateItem> = emptyList(),
)

data class DateItem(
    @SerializedName("dayNumber")
    val dayNumber: Int = 0,

    @SerializedName("dateStr")
    val dateStr: String = "",

    @SerializedName("isToday")
    val isToday: Boolean = false,

    @SerializedName("label")
    val label: String = "",
)

data class StreakInfo(
    @SerializedName("currentStreak")
    val currentStreak: Int = 0,

    @SerializedName("longestStreak")
    val longestStreak: Int = 0,

    @SerializedName("yearlyActiveDays")
    val yearlyActiveDays: Int = 0,

    @SerializedName("yearTotalDays")
    val yearTotalDays: Int = 365,

    @SerializedName("streakFreezeCount")
    val streakFreezeCount: Int = 2,

    @SerializedName("weekDays")
    val weekDays: List<WeekDayStatus> = emptyList(),

    @SerializedName("heatmap")
    val heatmap: List<List<Int>> = emptyList(),
)

data class WeekDayStatus(
    @SerializedName("dayLabel")
    val dayLabel: String = "",

    @SerializedName("date")
    val date: String = "",

    @SerializedName("dayNumber")
    val dayNumber: Int = 0,

    @SerializedName("isActive")
    val isActive: Boolean = false,

    @SerializedName("isToday")
    val isToday: Boolean = false,

    @SerializedName("xp")
    val xp: Int = 0,
)

data class HeroMetrics(
    @SerializedName("totalXp")
    val totalXp: Int = 0,

    @SerializedName("lessonsCount")
    val lessonsCount: Int = 0,

    @SerializedName("minutesStudied")
    val minutesStudied: Int = 0,

    @SerializedName("points")
    val points: Int = 0,
)

data class ProgressTracker(
    @SerializedName("status")
    val status: String = "Improving",

    @SerializedName("weeklyBars")
    val weeklyBars: List<WeeklyBarItem> = emptyList(),
)

data class WeeklyBarItem(
    @SerializedName("day")
    val day: String = "",

    @SerializedName("heightRatio")
    val heightRatio: Float = 0.5f,
)

data class FluencyTracker(
    @SerializedName("score")
    val score: Int = 0,

    @SerializedName("level")
    val level: String = "Beginner",

    @SerializedName("status")
    val status: String = "Improving",

    @SerializedName("breakdown")
    val breakdown: FluencyBreakdown = FluencyBreakdown(),
)

data class FluencyBreakdown(
    @SerializedName("accuracy")
    val accuracy: Int = 0,

    @SerializedName("speaking")
    val speaking: Int = 0,

    @SerializedName("vocabulary")
    val vocabulary: Int = 0,

    @SerializedName("consistency")
    val consistency: Int = 0,
)
