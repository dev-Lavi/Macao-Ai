package ai.macao.app.home

import ai.macao.app.R
import ai.macao.app.auth.ClashGroteskFontFamily
import ai.macao.app.data.network.*
import ai.macao.app.home.viewmodel.ProfileUiState
import ai.macao.app.home.viewmodel.ProfileViewModel
import ai.macao.app.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

// ─── Font Configuration ──────────────────────────────────────────────────────

private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private val ProfileInterFont = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.Bold),
)

// ─── Profile Screen Root ─────────────────────────────────────────────────────

@Composable
fun ProfileScreen(
    viewModel    : ProfileViewModel,
    selectedTab  : HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onBack       : () -> Unit = { onTabSelected(HomeTab.Home) },
) {
    LaunchedEffect(selectedTab) {
        if (selectedTab == HomeTab.Profile) {
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
            is ProfileUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }

            is ProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(R.drawable.owl_mad),
                            contentDescription = null,
                            modifier = Modifier.size(130.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = state.message,
                            fontFamily = ProfileInterFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MindfulBrown,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                        ) {
                            Text(text = "Retry", color = Color.White, fontFamily = ProfileInterFont, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            is ProfileUiState.Success -> {
                ProfileContent(
                    data = state.profile,
                    onBack = onBack
                )
            }
        }

        // Persistent Bottom Navigation Bar
        BottomNavBar(
            selectedTab   = selectedTab,
            onTabSelected = onTabSelected,
            modifier      = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ─── Profile Content Scroller ────────────────────────────────────────────────

@Composable
private fun ProfileContent(
    data  : ProfileSummaryResponse,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 110.dp)
    ) {
        // 1. Top Bar with back button and "Profile" title
        ProfileTopBar(onBack = onBack)

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Center User Profile Section
        UserProfileHeader(data = data)

        Spacer(modifier = Modifier.height(20.dp))

        // 3. User Statistics 2x2 Grid
        UserStatisticsGrid(data = data)

        Spacer(modifier = Modifier.height(24.dp))

        // Language Proficiency & Diagnostic Metrics
        ProficiencySection(proficiency = data.proficiency)

        Spacer(modifier = Modifier.height(24.dp))

        // 4. Strongest Topics
        StrongestTopicsSection(topics = data.strongestTopics)

        Spacer(modifier = Modifier.height(24.dp))

        // 5. Achievements / Level
        AchievementsLevelSection(level = data.level)

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Medals Horizontal Scroll
        MedalsSection(medals = data.medals)

        Spacer(modifier = Modifier.height(24.dp))

        // 7. Certifications Horizontal Scroll
        CertificationsSection(certifications = data.certifications)

        Spacer(modifier = Modifier.height(24.dp))

        // 8. XP Graph
        XpGraphSection(xpHistory = data.xpHistory)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ─── 1. Top Bar ──────────────────────────────────────────────────────────────

@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular back button matching design
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .border(1.dp, MindfulBrown10, CircleShape)
                .background(Color.White)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.back),
                contentDescription = "Back",
                tint = MindfulBrown,
                modifier = Modifier.size(16.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = "Profile",
            fontFamily = ProfileInterFont,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MindfulBrown
        )
    }
}

// ─── 2. User Profile Header ──────────────────────────────────────────────────

@Composable
private fun UserProfileHeader(data: ProfileSummaryResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile Image with circular clip
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(MindfulBrown10),
            contentAlignment = Alignment.Center
        ) {
            val imageUrl = data.user.profileImageUrl
            if (imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = "Profile Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text = data.user.displayName.take(1).uppercase().ifEmpty { "L" },
                    fontFamily = ProfileInterFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = MindfulBrown80
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Username / Display name
        Text(
            text = data.user.displayName.ifEmpty { data.user.username.ifEmpty { "Shinomiya" } },
            fontFamily = ProfileInterFont,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = MindfulBrown
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Membership and Language badges row
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pro Member badge (Blue)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE2F0FE))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.star),
                    contentDescription = null,
                    tint = Color(0xFF1E88E5),
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = data.user.membership.ifEmpty { "Pro Member" },
                    fontFamily = ProfileInterFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF1E88E5)
                )
            }

            // Language / Category badge (Warm Tan)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFE3D4C9))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = data.user.category.ifEmpty { "Japanese" },
                    fontFamily = ProfileInterFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = Color(0xFF8B6C5C)
                )
            }
        }
    }
}

// ─── 3. User Statistics 2x2 Grid ─────────────────────────────────────────────

@Composable
private fun UserStatisticsGrid(data: ProfileSummaryResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Row 1: Quizzes & Leaderboard
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatItemCard(
                iconRes = R.drawable.lightening,
                iconTint = Color(0xFFFF6433),
                iconBg = Color(0xFFFFECE5),
                value = data.stats.quizzes.toString(),
                label = "Quizzes",
                modifier = Modifier.weight(1f)
            )

            StatItemCard(
                iconRes = R.drawable.chart,
                iconTint = Color(0xFF5367F6),
                iconBg = Color(0xFFECEEFF),
                value = "#${data.stats.leaderboardRank}",
                label = "Leaderboard",
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Accuracy & Streak
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatItemCard(
                iconRes = R.drawable.tick,
                iconTint = Color(0xFF33C759),
                iconBg = Color(0xFFE8F7EC),
                value = "${data.stats.accuracy}%",
                label = "Accuracy",
                modifier = Modifier.weight(1f)
            )

            StatItemCard(
                iconRes = R.drawable.fire,
                iconTint = Color(0xFF9E5C32),
                iconBg = Color(0xFFF7EEE6),
                value = "${data.stats.streakDays} days",
                label = "Streak",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatItemCard(
    iconRes : Int,
    iconTint: Color,
    iconBg  : Color,
    value   : String,
    label   : String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MindfulBrown.copy(alpha = 0.05f),
                spotColor = MindfulBrown.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon circle
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = value,
                    fontFamily = ProfileInterFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MindfulBrown
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = label,
                    fontFamily = ProfileInterFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = MindfulBrown60
                )
            }
        }
    }
}

// ─── Language Proficiency Section ───────────────────────────────────────────

@Composable
private fun ProficiencySection(proficiency: ProfileProficiency) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        // Section Header
        Text(
            text = "LANGUAGE PROFICIENCY",
            fontFamily = ProfileInterFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            letterSpacing = 0.5.sp,
            color = MindfulBrown60
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Card Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = MindfulBrown.copy(alpha = 0.04f),
                    spotColor = MindfulBrown.copy(alpha = 0.08f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(18.dp)
        ) {
            Column {
                // 1. Learner Level Hero Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Level Code Badge (e.g. B1 or N4)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(OrangeAccent, Color(0xFFFF5A5F))
                                    )
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = proficiency.learnerLevel.code,
                                fontFamily = ClashGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = proficiency.learnerLevel.name,
                                fontFamily = ClashGroteskFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MindfulBrown
                            )
                            Text(
                                text = "Learner level",
                                fontFamily = ProfileInterFont,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                color = MindfulBrown40
                            )
                        }
                    }

                    // Check proficiency status tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F1EC))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Check proficiency",
                            fontFamily = ProfileInterFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MindfulBrown60
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = proficiency.learnerLevel.description,
                    fontFamily = ProfileInterFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = MindfulBrown60
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color(0xFFF0EBE6))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Speaking accuracy - ability to produce correct language
                ProficiencyMetricItem(
                    title = proficiency.speakingAccuracy.label.ifEmpty { "Speaking accuracy" },
                    description = proficiency.speakingAccuracy.description.ifEmpty { "Ability to produce correct language" },
                    percentage = proficiency.speakingAccuracy.percentage,
                    barBrush = Brush.horizontalGradient(listOf(Color(0xFFFF7B42), Color(0xFFFFA949))),
                    trackColor = Color(0xFFFDEAE8)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Pronunciation accuracy - quality of spoken pronunciation
                ProficiencyMetricItem(
                    title = proficiency.pronunciationAccuracy.label.ifEmpty { "Pronunciation accuracy" },
                    description = proficiency.pronunciationAccuracy.description.ifEmpty { "Quality of spoken pronunciation" },
                    percentage = proficiency.pronunciationAccuracy.percentage,
                    barBrush = Brush.horizontalGradient(listOf(Color(0xFF38B6FF), Color(0xFF4E9FDF))),
                    trackColor = Color(0xFFEBF4FB)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Vocabulary retention rate - effectiveness of revision
                ProficiencyMetricItem(
                    title = proficiency.vocabularyRetention.label.ifEmpty { "Vocabulary retention rate" },
                    description = proficiency.vocabularyRetention.description.ifEmpty { "Effectiveness of revision" },
                    percentage = proficiency.vocabularyRetention.percentage,
                    barBrush = Brush.horizontalGradient(listOf(Color(0xFF48BB78), Color(0xFF38A169))),
                    trackColor = Color(0xFFEBF8F2)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Error rate - no of errors/total attempt
                val errorDetails = if (proficiency.errorRate.totalAttempts > 0) {
                    " (${proficiency.errorRate.totalErrors}/${proficiency.errorRate.totalAttempts} attempts)"
                } else ""
                ProficiencyMetricItem(
                    title = proficiency.errorRate.label.ifEmpty { "Error rate" },
                    description = (proficiency.errorRate.description.ifEmpty { "No of errors / total attempt" }) + errorDetails,
                    percentage = proficiency.errorRate.percentage,
                    barBrush = Brush.horizontalGradient(listOf(Color(0xFFF56565), Color(0xFFE53E3E))),
                    trackColor = Color(0xFFFDE8E8)
                )
            }
        }
    }
}

@Composable
private fun ProficiencyMetricItem(
    title: String,
    description: String,
    percentage: Int,
    barBrush: Brush,
    trackColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontFamily = ProfileInterFont,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MindfulBrown
            )

            Text(
                text = "$percentage%",
                fontFamily = ProfileInterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MindfulBrown
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = description,
            fontFamily = ProfileInterFont,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = MindfulBrown40
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Progress track & fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(RoundedCornerShape(50))
                .background(trackColor)
        ) {
            val progressFraction = (percentage / 100f).coerceIn(0.04f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progressFraction)
                    .clip(RoundedCornerShape(50))
                    .background(barBrush)
            )
        }
    }
}

// ─── 4. Strongest Topics Section ─────────────────────────────────────────────

@Composable
private fun StrongestTopicsSection(topics: List<StrongestTopic>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "STRONGEST TOPICS",
            fontFamily = ProfileInterFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MindfulBrown60,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = MindfulBrown.copy(alpha = 0.05f),
                    spotColor = MindfulBrown.copy(alpha = 0.08f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            if (topics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No topic history yet. Practice lessons to reveal your strongest topics!",
                        fontFamily = ProfileInterFont,
                        fontSize = 13.sp,
                        color = MindfulBrown40,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    topics.forEach { topic ->
                        StrongestTopicRow(topic = topic)
                    }
                }
            }
        }
    }
}

@Composable
private fun StrongestTopicRow(topic: StrongestTopic) {
    val iconRes = when (topic.icon.lowercase()) {
        "food" -> R.drawable.food
        "cars" -> R.drawable.cars
        "social" -> R.drawable.social
        else -> R.drawable.food
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Topic asset image
        Image(
            painter = painterResource(iconRes),
            contentDescription = topic.name,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = topic.name,
                fontFamily = ProfileInterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MindfulBrown
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Gradient progress track
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFFDEAE8))
                ) {
                    val progressFraction = (topic.accuracy / 100f).coerceIn(0.04f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF7B42), Color(0xFFFF497A))
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Row {
                    Text(
                        text = "${topic.accuracy}%",
                        fontFamily = ProfileInterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MindfulBrown
                    )
                    Text(
                        text = " Correct",
                        fontFamily = ProfileInterFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = MindfulBrown60
                    )
                }
            }
        }
    }
}

// ─── 5. Achievements / Level Section ─────────────────────────────────────────

@Composable
private fun AchievementsLevelSection(level: ai.macao.app.data.network.ProfileLevelInfo) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "ACHIEVEMENTS",
            fontFamily = ProfileInterFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MindfulBrown60,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = MindfulBrown.copy(alpha = 0.05f),
                    spotColor = MindfulBrown.copy(alpha = 0.08f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column {
                // Level title row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1B18)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.currentLevel.toString(),
                            fontFamily = ProfileInterFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Level ${level.currentLevel}",
                            fontFamily = ProfileInterFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MindfulBrown
                        )
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = "${level.xpToNextLevel} Points to next level",
                            fontFamily = ProfileInterFont,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = MindfulBrown60
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Pill Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp)
                        .clip(RoundedCornerShape(19.dp))
                        .background(Color(0xFFFDF3DE)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left bubble (current level)
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5C24D)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = level.currentLevel.toString(),
                                fontFamily = ProfileInterFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MindfulBrown
                            )
                        }

                        // Center XP indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(R.drawable.star),
                                contentDescription = null,
                                tint = Color(0xFFB57D18),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${level.currentXp}/${level.nextLevelXp}",
                                fontFamily = ProfileInterFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFB57D18)
                            )
                        }

                        // Right bubble (next level)
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFCE5AA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (level.currentLevel + 1).toString(),
                                fontFamily = ProfileInterFont,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MindfulBrown
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── 6. Medals Section ───────────────────────────────────────────────────────

@Composable
private fun MedalsSection(medals: ai.macao.app.data.network.ProfileMedals) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "MEDALS",
                fontFamily = ProfileInterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MindfulBrown60,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = medals.total.toString(),
                fontFamily = ProfileInterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MindfulBrown60
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Gold Medal Card
            MedalCard(
                title = "Gold",
                count = medals.gold,
                gradient = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEE72), Color(0xFFEAA61E), Color(0xFFCE8700))
                ),
                countBg = Color(0xFFFFF7E7),
                countColor = Color(0xFFD38B00)
            )

            // Silver Medal Card
            MedalCard(
                title = "Silver",
                count = medals.silver,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFB8C5D3), Color(0xFF758599), Color(0xFF55657A))
                ),
                countBg = Color(0xFFF1F3F6),
                countColor = Color(0xFF677B94)
            )

            // Bronze Medal Card
            MedalCard(
                title = "Bronze",
                count = medals.bronze,
                gradient = Brush.linearGradient(
                    colors = listOf(Color(0xFFD6A784), Color(0xFFA67554), Color(0xFF7B4E32))
                ),
                countBg = Color(0xFFF9F3EE),
                countColor = Color(0xFF9E643E)
            )
        }
    }
}

@Composable
private fun MedalCard(
    title     : String,
    count     : Int,
    gradient  : Brush,
    countBg   : Color,
    countColor: Color
) {
    Box(
        modifier = Modifier
            .width(100.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MindfulBrown.copy(alpha = 0.05f),
                spotColor = MindfulBrown.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 3D Metallic Sphere Medal
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(gradient)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = title,
                fontFamily = ProfileInterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MindfulBrown
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(countBg)
                    .padding(horizontal = 10.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = count.toString(),
                    fontFamily = ProfileInterFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = countColor
                )
            }
        }
    }
}

// ─── 7. Certifications Section ───────────────────────────────────────────────

@Composable
private fun CertificationsSection(certifications: List<ProfileCertification>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "CERTIFICATIONS",
                fontFamily = ProfileInterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MindfulBrown60,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = certifications.size.toString(),
                fontFamily = ProfileInterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = MindfulBrown60
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (certifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 6.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = MindfulBrown.copy(alpha = 0.05f),
                        spotColor = MindfulBrown.copy(alpha = 0.08f)
                    )
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .padding(18.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No certifications earned yet. Complete units to earn certifications!",
                    fontFamily = ProfileInterFont,
                    fontSize = 13.sp,
                    color = MindfulBrown40,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                certifications.forEach { cert ->
                    CertificationCard(cert = cert)
                }
            }
        }
    }
}

@Composable
private fun CertificationCard(cert: ProfileCertification) {
    val isBronze = cert.level.uppercase() == "BRONZE"
    val badgeTint = if (isBronze) Color(0xFFB8967E) else Color(0xFF7A8EAC)
    val pillBg = if (isBronze) Color(0xFFF7F2EE) else Color(0xFFEEF2F8)
    val pillColor = if (isBronze) Color(0xFF9E7E67) else Color(0xFF6B7E98)
    val pillText = when (cert.level.uppercase()) {
        "SILVER" -> "Silver Certified"
        "GOLD" -> "Gold Certified"
        else -> "Bronze Certified"
    }

    Box(
        modifier = Modifier
            .width(155.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = MindfulBrown.copy(alpha = 0.05f),
                spotColor = MindfulBrown.copy(alpha = 0.08f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Rosette badge icon
            Icon(
                painter = painterResource(R.drawable.certificate),
                contentDescription = cert.name,
                tint = badgeTint,
                modifier = Modifier.size(38.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = cert.name,
                fontFamily = ProfileInterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MindfulBrown,
                textAlign = TextAlign.Center,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(pillBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = pillText,
                    fontFamily = ProfileInterFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    color = pillColor
                )
            }
        }
    }
}

// ─── 8. XP Graph Section ─────────────────────────────────────────────────────

@Composable
private fun XpGraphSection(xpHistory: List<XpHistoryPoint>) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "XP HISTORY",
            fontFamily = ProfileInterFont,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MindfulBrown60,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = MindfulBrown.copy(alpha = 0.05f),
                    spotColor = MindfulBrown.copy(alpha = 0.08f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(Color.White)
                .padding(16.dp)
        ) {
            val hasData = xpHistory.any { it.xp > 0 }

            if (xpHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No XP data available yet.",
                        fontFamily = ProfileInterFont,
                        fontSize = 13.sp,
                        color = MindfulBrown40
                    )
                }
            } else {
                Column {
                    val totalRecentXp = xpHistory.sumOf { it.xp }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Last 7 Days",
                            fontFamily = ProfileInterFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MindfulBrown60
                        )
                        Text(
                            text = "$totalRecentXp XP Total",
                            fontFamily = ProfileInterFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = OrangeAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Line chart canvas
                    DynamicXpLineChart(
                        points = xpHistory,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Day labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        xpHistory.forEach { pt ->
                            Text(
                                text = pt.dayLabel,
                                fontFamily = ProfileInterFont,
                                fontSize = 11.sp,
                                color = MindfulBrown40
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DynamicXpLineChart(
    points  : List<XpHistoryPoint>,
    modifier: Modifier = Modifier
) {
    val lineColor = OrangeAccent
    val maxVal = points.maxOfOrNull { it.xp.toFloat() }?.coerceAtLeast(40f) ?: 40f

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stepX = if (points.size > 1) w / (points.size - 1) else w

        fun xAt(i: Int) = i * stepX
        fun yAt(v: Float) = h - (v / maxVal) * (h * 0.75f) - 10.dp.toPx()

        // Background reference line
        drawLine(
            color = MindfulBrown10,
            start = Offset(0f, h - 5.dp.toPx()),
            end = Offset(w, h - 5.dp.toPx()),
            strokeWidth = 1.dp.toPx()
        )

        val path = Path()
        val fillPath = Path()

        points.forEachIndexed { i, pt ->
            val x = xAt(i)
            val y = yAt(pt.xp.toFloat())
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, h)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(w, h)
        fillPath.close()

        // Draw soft gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(OrangeAccent.copy(alpha = 0.25f), Color.Transparent),
                startY = 0f,
                endY = h
            )
        )

        // Draw line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        // Draw point dots
        points.forEachIndexed { i, pt ->
            val x = xAt(i)
            val y = yAt(pt.xp.toFloat())
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
            drawCircle(color = Color.White, radius = 2.dp.toPx(), center = Offset(x, y))
        }
    }
}
