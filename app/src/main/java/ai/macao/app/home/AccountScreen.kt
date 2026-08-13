package ai.macao.app.home

import ai.macao.app.R
import ai.macao.app.theme.AppBackground
import ai.macao.app.theme.GoldAccent
import ai.macao.app.theme.MindfulBrown
import ai.macao.app.theme.MindfulBrown10
import ai.macao.app.theme.MindfulBrown40
import ai.macao.app.theme.MindfulBrown60
import ai.macao.app.theme.MindfulBrown80
import ai.macao.app.theme.OrangeAccent
import ai.macao.app.theme.PurpleLevel
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabIndicatorScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Font ────────────────────────────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private val AccInterFont = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Bold),
)

// ─── Account data ─────────────────────────────────────────────────────────────

data class AccountData(
    val displayName   : String  = "Shinomiya",
    val handle        : String  = "sh.t5Ovc9",
    val joinedDate    : String  = "May 2023",
    val friendsCount  : Int     = 0,
    val countryFlag   : String  = "🇯🇵",
    val dayStreak     : Int     = 1,
    val totalXp       : Int     = 1771,
    val league        : String  = "Gold",
    val top3Finishes  : Int     = 1,
    val weeklyXpSelf  : List<Float> = listOf(0f, 0f, 0f, 0f, 0f, 0f, 10f),
    val weeklyXpOther : List<Float> = listOf(0f, 20f, 40f, 60f, 80f, 130f, 139f),
    val otherName     : String  = "Asmi Ace",
    val otherXp       : Int     = 139,
)

// ─── AccountScreen ────────────────────────────────────────────────────────────

@Composable
fun AccountScreen(
    data         : AccountData  = AccountData(),
    selectedTab  : HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    onFollow     : () -> Unit   = {},
    onShare      : () -> Unit   = {},
    onBlockUser  : () -> Unit   = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 100.dp),
            ) {
                ProfileHeader(data, onFollow, onShare)
                Spacer(modifier = Modifier.height(20.dp))
                StatisticsSection(data)
                Spacer(modifier = Modifier.height(20.dp))
                XpChartSection(data)
                Spacer(modifier = Modifier.height(20.dp))
                FriendsSection()
                Spacer(modifier = Modifier.height(20.dp))
                AchievementsSection()
                Spacer(modifier = Modifier.height(24.dp))
                BlockUserButton(onBlockUser)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        BottomNavBar(
            selectedTab   = selectedTab,
            onTabSelected = onTabSelected,
            modifier      = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ─── Profile header ───────────────────────────────────────────────────────────

@Composable
private fun ProfileHeader(
    data    : AccountData,
    onFollow: () -> Unit,
    onShare : () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 44.dp),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            // Info column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = data.displayName,
                    fontFamily = AccInterFont,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 22.sp,
                    color      = MindfulBrown,
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text       = data.handle,
                    fontFamily = AccInterFont,
                    fontWeight = FontWeight.Normal,
                    fontSize   = 13.sp,
                    color      = MindfulBrown60,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter            = painterResource(R.drawable.notification),
                        contentDescription = null,
                        tint               = MindfulBrown40,
                        modifier           = Modifier.size(13.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text       = "Joined ${data.joinedDate}",
                        fontFamily = AccInterFont,
                        fontSize   = 12.sp,
                        color      = MindfulBrown60,
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter            = painterResource(R.drawable.profile),
                        contentDescription = null,
                        tint               = MindfulBrown40,
                        modifier           = Modifier.size(13.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text       = "${data.friendsCount} Friends",
                        fontFamily = AccInterFont,
                        fontSize   = 12.sp,
                        color      = MindfulBrown60,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = data.countryFlag, fontSize = 22.sp)
            }

            // Avatar circle
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PurpleLevel, Color(0xFF6C3483)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = data.displayName.take(1).uppercase(),
                    fontFamily = AccInterFont,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 28.sp,
                    color      = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Follow + Share row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(PurpleLevel, Color(0xFF4361EE)),
                        ),
                    )
                    .clickable(onClick = onFollow),
                contentAlignment = Alignment.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter            = painterResource(R.drawable.profile),
                        contentDescription = null,
                        tint               = Color.White,
                        modifier           = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text       = "FOLLOW",
                        fontFamily = AccInterFont,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 13.sp,
                        color      = Color.White,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, MindfulBrown10, RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .clickable(onClick = onShare),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter            = painterResource(R.drawable.chart),
                    contentDescription = "Share",
                    tint               = MindfulBrown60,
                    modifier           = Modifier.size(18.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MindfulBrown10, thickness = 1.dp)
    }
}

// ─── Statistics section ───────────────────────────────────────────────────────

@Composable
private fun StatisticsSection(data: AccountData) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionTitle("Statistics")
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard(
                emoji = "🔥",
                value = data.dayStreak.toString(),
                label = "Day streak",
                modifier = Modifier.weight(1f),
            )
            StatCard(
                emoji = "⚡",
                value = data.totalXp.toString(),
                label = "Total XP",
                modifier = Modifier.weight(1f),
                accentColor = GoldAccent,
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            StatCard(
                emoji = "🥇",
                value = data.league,
                label = "League",
                badge = "WEEK 2",
                modifier = Modifier.weight(1f),
                accentColor = GoldAccent,
            )
            StatCard(
                emoji = "🏆",
                value = data.top3Finishes.toString(),
                label = "Top 3 finishes",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatCard(
    emoji      : String,
    value      : String,
    label      : String,
    badge      : String?    = null,
    accentColor: Color      = OrangeAccent,
    modifier   : Modifier   = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(14.dp),
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 20.sp)
                if (badge != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(GoldAccent)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text       = badge,
                            fontFamily = AccInterFont,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 9.sp,
                            color      = Color.White,
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text       = value,
                fontFamily = AccInterFont,
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp,
                color      = MindfulBrown,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text       = label,
                fontFamily = AccInterFont,
                fontSize   = 12.sp,
                color      = MindfulBrown60,
            )
        }
    }
}

// ─── XP chart section ─────────────────────────────────────────────────────────

@Composable
private fun XpChartSection(data: AccountData) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionTitle("XP this week")
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp),
        ) {
            Column {
                // Legend
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF4361EE)),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text       = data.otherName,
                                fontFamily = AccInterFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 13.sp,
                                color      = MindfulBrown,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(MindfulBrown40),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text       = "You",
                                fontFamily = AccInterFont,
                                fontSize   = 13.sp,
                                color      = MindfulBrown60,
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text       = "${data.otherXp} XP",
                            fontFamily = AccInterFont,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 14.sp,
                            color      = Color(0xFF4361EE),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text       = "${data.weeklyXpSelf.last().toInt()} XP",
                            fontFamily = AccInterFont,
                            fontSize   = 13.sp,
                            color      = MindfulBrown60,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chart
                XpLineChart(
                    seriesBlue = data.weeklyXpOther,
                    seriesGray = data.weeklyXpSelf,
                    modifier   = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Day labels
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    listOf("Sa", "Su", "M", "Tu", "W", "Th", "F").forEach { day ->
                        Text(
                            text       = day,
                            fontFamily = AccInterFont,
                            fontSize   = 11.sp,
                            color      = MindfulBrown40,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun XpLineChart(
    seriesBlue: List<Float>,
    seriesGray: List<Float>,
    modifier  : Modifier = Modifier,
) {
    val blueColor = Color(0xFF4361EE)
    val grayColor = MindfulBrown40

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val maxVal = (seriesBlue + seriesGray).maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val stepX = w / (seriesBlue.size - 1).toFloat()

        fun xAt(i: Int) = i * stepX
        fun yAt(v: Float) = h - (v / maxVal) * h * 0.9f

        fun drawSeries(series: List<Float>, color: Color) {
            val path = Path()
            series.forEachIndexed { i, v ->
                if (i == 0) path.moveTo(xAt(i), yAt(v))
                else path.lineTo(xAt(i), yAt(v))
            }
            drawPath(
                path  = path,
                color = color,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round),
            )
            // dots
            series.forEachIndexed { i, v ->
                drawCircle(
                    color  = color,
                    radius = 4.dp.toPx(),
                    center = Offset(xAt(i), yAt(v)),
                )
                drawCircle(
                    color  = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(xAt(i), yAt(v)),
                )
            }
        }

        // Y-axis reference lines
        listOf(0f, 40f, 80f, 120f, 160f).forEach { yVal ->
            val y = yAt(yVal)
            if (y in 0f..h) {
                drawLine(
                    color       = grayColor.copy(alpha = 0.15f),
                    start       = Offset(0f, y),
                    end         = Offset(w, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }
        }

        drawSeries(seriesBlue, blueColor)
        drawSeries(seriesGray, grayColor)
    }
}

// ─── Friends section ──────────────────────────────────────────────────────────

@Composable
private fun FriendsSection() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("FOLLOWING", "FOLLOWERS")

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionTitle("Friends")
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White),
        ) {
            Column {
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor   = Color.White,
                    contentColor     = MindfulBrown,
                    indicator        = @Composable {
                        Box(
                            modifier = Modifier
                                .tabIndicatorOffset(selectedTab, matchContentSize = false)
                                .height(2.dp)
                                .background(OrangeAccent),
                        )
                    },
                    divider          = {},
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick  = { selectedTab = index },
                            selectedContentColor   = OrangeAccent,
                            unselectedContentColor = MindfulBrown40,
                        ) {
                            Text(
                                text       = title,
                                fontFamily = AccInterFont,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 13.sp,
                                modifier   = Modifier.padding(vertical = 14.dp),
                            )
                        }
                    }
                }

                Box(
                    modifier         = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text       = if (selectedTab == 0) "Not following anyone yet" else "No followers yet",
                        fontFamily = AccInterFont,
                        fontSize   = 14.sp,
                        color      = MindfulBrown40,
                        textAlign  = TextAlign.Center,
                    )
                }
            }
        }
    }
}

// ─── Achievements section ─────────────────────────────────────────────────────

private data class Achievement(
    val emoji: String,
    val level: Int,
    val bg   : Color,
)

private val achievements = listOf(
    Achievement("🔥", 3, Color(0xFFE74C3C)),
    Achievement("🧙", 5, Color(0xFF27AE60)),
    Achievement("📜", 3, Color(0xFFE67E22)),
)

@Composable
private fun AchievementsSection() {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionTitle("Achievements")
        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(16.dp),
        ) {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    achievements.forEach { ach ->
                        AchievementCard(ach, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text       = "View 7 more",
                    fontFamily = AccInterFont,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 13.sp,
                    color      = MindfulBrown60,
                )
            }
        }
    }
}

@Composable
private fun AchievementCard(ach: Achievement, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(90.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ach.bg),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = ach.emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.25f))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(
                    text       = "LEVEL ${ach.level}",
                    fontFamily = AccInterFont,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 9.sp,
                    color      = Color.White,
                )
            }
        }
    }
}

// ─── Block user button ────────────────────────────────────────────────────────

@Composable
private fun BlockUserButton(onBlockUser: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                onClick           = onBlockUser,
            ),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Icon(
            painter            = painterResource(R.drawable.notification),
            contentDescription = null,
            tint               = MindfulBrown40,
            modifier           = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text       = "BLOCK USER",
            fontFamily = AccInterFont,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 12.sp,
            color      = MindfulBrown40,
        )
    }
}

// ─── Shared section title ─────────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String) {
    Text(
        text       = title,
        fontFamily = AccInterFont,
        fontWeight = FontWeight.Bold,
        fontSize   = 18.sp,
        color      = MindfulBrown,
    )
}
