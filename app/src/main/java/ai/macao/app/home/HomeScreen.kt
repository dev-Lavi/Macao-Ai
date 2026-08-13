package ai.macao.app.home

import ai.macao.app.R
import ai.macao.app.home.data.SampleStageData
import ai.macao.app.home.data.StageModel
import ai.macao.app.theme.AppBackground
import ai.macao.app.theme.MindfulBrown
import ai.macao.app.theme.MindfulBrown10
import ai.macao.app.theme.MindfulBrown60
import ai.macao.app.theme.MindfulBrown80
import ai.macao.app.theme.OrangeAccent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import ai.macao.app.auth.ClashGroteskFontFamily
import androidx.compose.runtime.Composable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import ai.macao.app.onboarding.data.repository.UserRepository

// ─── Font ────────────────────────────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private val HomeInterFont = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Inter"), fontProvider = provider, weight = FontWeight.Bold),
)

// ─── HomeScreen ───────────────────────────────────────────────────────────────

/**
 * Main dashboard / learning path screen.
 *
 * Layout:
 *  ┌─────────────────────────────────────┐
 *  │  Profile   Hi, {name}!  [🔔]        │  ← TopBar
 *  │            Pro Member · Japanese    │
 *  ├─────────────────────────────────────┤
 *  │  (Scrollable Learning Map)          │  ← LearningMapScreen
 *  ├─────────────────────────────────────┤
 *  │  [🏠]   [🔔]   [📊]   [👤]         │  ← BottomNavBar
 *  └─────────────────────────────────────┘
 */
@Composable
fun HomeScreen(
    userName       : String,
    userRepository : UserRepository,
    stages         : List<StageModel>,
    learningLang   : String = "Japanese",
    notifCount     : Int    = 5,
    selectedTab    : HomeTab,
    onTabSelected  : (HomeTab) -> Unit,
    onStageClick   : (StageModel) -> Unit = {},
    onNotifClick   : () -> Unit = {},
    onProfileClick : () -> Unit = {},
) {
    var profileImageUrl by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        val result = userRepository.getUserProfile()
        if (result.isSuccess) {
            profileImageUrl = result.getOrNull()?.profileImageUrl ?: ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top bar ─────────────────────────────────────────────────────
            HomeTopBar(
                userName       = userName,
                learningLang   = learningLang,
                notifCount     = notifCount,
                profileImageUrl = profileImageUrl,
                onNotifClick   = onNotifClick,
                onProfileClick = onProfileClick,
            )

            // ── Learning Map (Scrollable vertical map) ──────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) {
                LearningMapScreen(
                    userName = userName,
                    stages = stages,
                    onStageClick = onStageClick,
                )
            }
        }

        // ── Bottom nav pinned at bottom ──────────────────────────────────────
        BottomNavBar(
            selectedTab   = selectedTab,
            onTabSelected = onTabSelected,
            modifier      = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ─── Top bar ─────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar(
    userName      : String,
    learningLang  : String,
    notifCount    : Int,
    profileImageUrl: String,
    onNotifClick  : () -> Unit,
    onProfileClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(top = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // ── Profile avatar ───────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MindfulBrown60, MindfulBrown),
                    ),
                )
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center,
        ) {
            if (profileImageUrl.isNotEmpty()) {
                AsyncImage(
                    model = profileImageUrl,
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(
                    text       = userName.take(1).uppercase(),
                    fontFamily = HomeInterFont,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 22.sp,
                    color      = Color.White,
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // ── Name + badges ────────────────────────────────────────────────────
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = "Hi, $userName!",
                fontFamily = ClashGroteskFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize   = 20.sp,
                color      = MindfulBrown,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pro Member badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFFEEF2FF))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.star),
                        contentDescription = null,
                        tint               = Color(0xFF4361EE),
                        modifier           = Modifier.size(12.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text       = "Pro Member",
                        fontFamily = HomeInterFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize   = 11.sp,
                        color      = Color(0xFF4361EE),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Language badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MindfulBrown10)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text       = learningLang,
                        fontFamily = HomeInterFont,
                        fontWeight = FontWeight.Medium,
                        fontSize   = 11.sp,
                        color      = MindfulBrown80,
                    )
                }
            }
        }

        // ── Notification button ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .size(44.dp)
                .shadow(6.dp, CircleShape, ambientColor = MindfulBrown.copy(0.12f))
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onNotifClick),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter            = painterResource(R.drawable.notification),
                contentDescription = "Notifications",
                tint               = MindfulBrown,
                modifier           = Modifier.size(22.dp),
            )
            // Badge dot
            if (notifCount > 0) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp),
                )
            }
        }
    }
}
