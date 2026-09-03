package ai.macao.app.home

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.macao.app.R
import ai.macao.app.auth.ClashGroteskFontFamily
import ai.macao.app.data.network.*
import ai.macao.app.home.viewmodel.AnalyticsUiState
import ai.macao.app.home.viewmodel.AnalyticsViewModel
import ai.macao.app.theme.*

// ─── Color Palette for Analytics ─────────────────────────────────────────────

private val CaramelCardBg     = Color(0xFFA5775B)
private val PeachPillBg       = Color(0xFFFCD5CB)
private val PeachActiveTrack  = Color(0xFFFEEAE5)
private val FireBrownTint     = Color(0xFF926247)
private val StreakRingColor   = Color(0xFF8E6349)
private val InactiveDayCircle = Color(0xFF2C3E50)

// ─── Font Configuration ──────────────────────────────────────────────────────

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private val AnalyticsInterFont = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.Bold),
)

// ─── Analytics Screen Root ───────────────────────────────────────────────────

@Composable
fun AnalyticsScreen(
    viewModel    : AnalyticsViewModel,
    selectedTab  : HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onBack       : () -> Unit = { onTabSelected(HomeTab.Home) },
) {
    LaunchedEffect(selectedTab) {
        if (selectedTab == HomeTab.Chart) {
            viewModel.refresh()
        }
    }

    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        when (val state = uiState) {
            is AnalyticsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }
            is AnalyticsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Couldn't load analytics",
                            fontFamily = ClashGroteskFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MindfulBrown
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = state.message,
                            fontFamily = AnalyticsInterFont,
                            fontSize = 14.sp,
                            color = MindfulBrown60,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { viewModel.loadAnalytics() },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry", color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            is AnalyticsUiState.Success -> {
                val data = state.data

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .padding(bottom = 110.dp)
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // 1. Top Bar (< Analytics)
                    AnalyticsTopBar(onBack = onBack)

                    Spacer(modifier = Modifier.height(16.dp))

                    // 2. Date Selector Strip
                    DateSelectorStrip(
                        dateStrip = data.dateStrip,
                        selectedDateStr = state.selectedDateStr,
                        onSelectDate = { viewModel.selectDate(it) }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // 3. Weekly Streak Card
                    WeeklyStreakCard(
                        streak = data.streak,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // 4. Hero Stats: Total Experience & 3 metrics
                    HeroStatsSection(hero = data.hero)

                    Spacer(modifier = Modifier.height(28.dp))

                    // 5. Section Heading: "Learning Tracker"
                    Text(
                        text = "Learning Tracker",
                        fontFamily = ClashGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = MindfulBrown,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 6. Learning Tracker 2-Column Cards (Daily Streak + Progress)
                    LearningTrackerRow(
                        streak = data.streak,
                        progress = data.progress,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 7. Fluency Score Card
                    FluencyScoreCard(
                        fluency = data.fluency,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        // Persistent Bottom Navigation Bar
        BottomNavBar(
            selectedTab   = selectedTab,
            onTabSelected = onTabSelected,
            modifier      = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─── 1. Top Bar ──────────────────────────────────────────────────────────────

@Composable
private fun AnalyticsTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular back button
        Box(
            modifier = Modifier
                .size(46.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = CircleShape,
                    ambientColor = MindfulBrown.copy(alpha = 0.05f),
                    spotColor = MindfulBrown.copy(alpha = 0.08f)
                )
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, MindfulBrown10, CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.back),
                contentDescription = "Back",
                tint = MindfulBrown,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = "Analytics",
            fontFamily = ClashGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MindfulBrown
        )
    }
}

// ─── 2. Date Selector Strip ──────────────────────────────────────────────────

@Composable
private fun DateSelectorStrip(
    dateStrip: DateStripInfo,
    selectedDateStr: String,
    onSelectDate: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        dateStrip.dates.forEach { item ->
            if (item.isToday) {
                // Highlighted "Today, 8 Jul" peach pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(PeachPillBg)
                        .clickable { onSelectDate(item.dateStr) }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.label,
                        fontFamily = AnalyticsInterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MindfulBrown
                    )
                }
            } else {
                // Surrounding simple date numbers
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { onSelectDate(item.dateStr) }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.dayNumber.toString(),
                        fontFamily = AnalyticsInterFont,
                        fontWeight = if (item.dateStr == selectedDateStr) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 14.sp,
                        color = if (item.dateStr == selectedDateStr) MindfulBrown else MindfulBrown60
                    )
                }
            }
        }
    }
}

// ─── 3. Weekly Streak Card ───────────────────────────────────────────────────

@Composable
private fun WeeklyStreakCard(
    streak: StreakInfo,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = MindfulBrown.copy(alpha = 0.04f),
                spotColor = MindfulBrown.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(vertical = 18.dp, horizontal = 16.dp)
    ) {
        Column {
            // Days of the week header + status row
            val weekDays = streak.weekDays.ifEmpty {
                listOf(
                    WeekDayStatus("Mo", isActive = true),
                    WeekDayStatus("Tu", isActive = true),
                    WeekDayStatus("We", isActive = true),
                    WeekDayStatus("Th", isActive = true, isToday = true),
                    WeekDayStatus("Fr", isActive = false),
                    WeekDayStatus("Sa", isActive = false),
                    WeekDayStatus("Su", isActive = false),
                )
            }

            // Headers: Mo Tu We Th Fr Sa Su
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                weekDays.forEach { day ->
                    Box(
                        modifier = Modifier.width(38.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.dayLabel,
                            fontFamily = AnalyticsInterFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MindfulBrown
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Day Status row with connected peach pill for active days
            Box(modifier = Modifier.fillMaxWidth()) {
                // Find active contiguous span
                val activeCount = weekDays.takeWhile { it.isActive }.size.coerceAtLeast(1)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    weekDays.forEachIndexed { index, day ->
                        val isInsidePeachTrack = index < activeCount

                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(46.dp)
                                .clip(
                                    when {
                                        !isInsidePeachTrack -> RoundedCornerShape(0.dp)
                                        index == 0 && activeCount == 1 -> RoundedCornerShape(24.dp)
                                        index == 0 -> RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                                        index == activeCount - 1 -> RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                                        else -> RoundedCornerShape(0.dp)
                                    }
                                )
                                .background(if (isInsidePeachTrack) PeachActiveTrack else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day.isActive) {
                                // Active day: Circle with brown outline and fire icon
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.5.dp, StreakRingColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.fire),
                                        contentDescription = "Active streak",
                                        tint = FireBrownTint,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else {
                                // Inactive / future day: Hollow circle with dark/slate outline
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, InactiveDayCircle, CircleShape)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bottom row: Fire icon + "12 days" on left, milestone badges on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Fire + count
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.fire),
                        contentDescription = "Current streak",
                        tint = FireBrownTint,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${streak.currentStreak} days",
                        fontFamily = AnalyticsInterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MindfulBrown
                    )
                }

                // Right: Milestone / Streak Freeze badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(streak.streakFreezeCount.coerceAtLeast(2)) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF7ECE4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.fire),
                                contentDescription = "Freeze Badge",
                                tint = FireBrownTint,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── 4. Hero Stats Section ───────────────────────────────────────────────────

@Composable
private fun HeroStatsSection(hero: HeroMetrics) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Big bold XP number
        Text(
            text = "${hero.totalXp} XP",
            fontFamily = ClashGroteskFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 38.sp,
            color = MindfulBrown,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Total Experience",
            fontFamily = AnalyticsInterFont,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = MindfulBrown60,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(22.dp))

        // 3-column stats row: Lessons | Minutes Studies | Points
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Metric 1: Lessons
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = hero.lessonsCount.toString(),
                    fontFamily = ClashGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MindfulBrown
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Lessons",
                    fontFamily = AnalyticsInterFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MindfulBrown60
                )
            }

            // Metric 2: Minutes Studies
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = hero.minutesStudied.toString(),
                    fontFamily = ClashGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MindfulBrown
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Minutes Studies",
                    fontFamily = AnalyticsInterFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MindfulBrown60
                )
            }

            // Metric 3: Points
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = hero.points.toString(),
                    fontFamily = ClashGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MindfulBrown
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Points",
                    fontFamily = AnalyticsInterFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MindfulBrown60
                )
            }
        }
    }
}

// ─── 5. Learning Tracker 2-Column Row ────────────────────────────────────────

@Composable
private fun LearningTrackerRow(
    streak: StreakInfo,
    progress: ProgressTracker,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Card 1: Daily Streak (Heatmap)
        DailyStreakCard(
            streak = streak,
            modifier = Modifier.weight(1f)
        )

        // Card 2: Progress (Vertical Bars)
        ProgressCard(
            progress = progress,
            modifier = Modifier.weight(1f)
        )
    }
}

// ─── Daily Streak Card (Caramel) ─────────────────────────────────────────────

@Composable
private fun DailyStreakCard(
    streak: StreakInfo,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = CaramelCardBg.copy(alpha = 0.2f),
                spotColor = CaramelCardBg.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(CaramelCardBg)
            .padding(16.dp)
            .height(210.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Header with fire icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.fire),
                        contentDescription = "Daily Streak",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Daily Streak",
                        fontFamily = AnalyticsInterFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Big ratio metric: 31/365
                Text(
                    text = "${streak.yearlyActiveDays}/${streak.yearTotalDays}",
                    fontFamily = ClashGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }

            // 5x7 Activity Dot Matrix
            val heatmap = streak.heatmap.ifEmpty {
                listOf(
                    listOf(0, 1, 2, 2, 1, 0, 0),
                    listOf(1, 2, 3, 2, 1, 1, 0),
                    listOf(2, 2, 2, 3, 1, 0, 0),
                    listOf(1, 1, 2, 2, 2, 1, 0),
                    listOf(2, 3, 3, 2, 1, 0, 0)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                heatmap.take(5).forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        row.take(7).forEach { intensity ->
                            val dotColor = when (intensity) {
                                3 -> Color.White
                                2 -> Color(0xFFF6EDE6)
                                1 -> Color(0xFF875B42)
                                else -> Color(0xFF7A4E36)
                            }
                            Box(
                                modifier = Modifier
                                    .size(13.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(dotColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Progress Card (Caramel) ─────────────────────────────────────────────────

@Composable
private fun ProgressCard(
    progress: ProgressTracker,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = CaramelCardBg.copy(alpha = 0.2f),
                spotColor = CaramelCardBg.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(CaramelCardBg)
            .padding(16.dp)
            .height(210.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Progress",
                    fontFamily = AnalyticsInterFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = progress.status,
                    fontFamily = ClashGroteskFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.White
                )
            }

            // 6 Vertical Rounded Bars in pure white
            val bars = progress.weeklyBars.ifEmpty {
                listOf(
                    WeeklyBarItem("Mo", 0.45f),
                    WeeklyBarItem("Tu", 0.90f),
                    WeeklyBarItem("We", 0.35f),
                    WeeklyBarItem("Th", 0.88f),
                    WeeklyBarItem("Fr", 0.50f),
                    WeeklyBarItem("Sa", 0.70f),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                bars.take(6).forEach { bar ->
                    val barHeightDp = (80 * bar.heightRatio.coerceIn(0.2f, 1.0f)).dp

                    Box(
                        modifier = Modifier
                            .width(13.dp)
                            .height(barHeightDp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

// ─── 7. Fluency Score Card (Caramel) ─────────────────────────────────────────

@Composable
private fun FluencyScoreCard(
    fluency: FluencyTracker,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = CaramelCardBg.copy(alpha = 0.2f),
                spotColor = CaramelCardBg.copy(alpha = 0.3f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(CaramelCardBg)
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Fluency Score",
                        fontFamily = AnalyticsInterFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${fluency.score}%",
                        fontFamily = ClashGroteskFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.White
                    )
                }

                // Level Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.22f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = fluency.level,
                        fontFamily = AnalyticsInterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Fluency competencies breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CompetencyItem("Accuracy", "${fluency.breakdown.accuracy}%")
                CompetencyItem("Speaking", "${fluency.breakdown.speaking}%")
                CompetencyItem("Vocabulary", "${fluency.breakdown.vocabulary}%")
                CompetencyItem("Consistency", "${fluency.breakdown.consistency}%")
            }
        }
    }
}

@Composable
private fun CompetencyItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = AnalyticsInterFont,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontFamily = AnalyticsInterFont,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}
