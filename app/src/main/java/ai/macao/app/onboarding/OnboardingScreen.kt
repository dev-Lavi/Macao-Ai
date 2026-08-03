package ai.macao.app.onboarding

import ai.macao.app.R
import ai.macao.app.auth.AuthEntry
import ai.macao.app.auth.AuthFlow
import ai.macao.app.auth.ClashGroteskFontFamily
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Google Font provider ───────────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage  = "com.google.android.gms",
    certificates     = R.array.com_google_android_gms_fonts_certs,
)

private val InterFontName = GoogleFont("Inter")

private val InterFontFamily = FontFamily(
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.ExtraBold),
)

// ─── Colors ─────────────────────────────────────────────────────────────────

val ColorBrown    = Color(0xFF926247)
val ColorDarkText = Color(0xFF7A5C44)
val ColorSubText  = Color(0xFFA08060)
val ColorDarkBtn  = Color(0xFF2D1A0E)

// ─── Data model ─────────────────────────────────────────────────────────────

data class OnboardingPageData(
    val imageRes  : Int,
    val title     : String,
    val subtitle  : String,
)

val onboardingPages = listOf(
    OnboardingPageData(
        imageRes = R.drawable.owl_1,
        title    = "Speak fluently without the stage fright",
        subtitle = "Master a new language naturally by chatting with your personal AI companion anytime, anywhere, completely judgment-free.",
    ),
    OnboardingPageData(
        imageRes = R.drawable.owl_2,
        title    = "Real conversations, real-time feedback",
        subtitle = "Practice real-world scenarios, perfect your pronunciation, and get instant corrections from an AI tutor that adapts to your pace.",
    ),
    OnboardingPageData(
        imageRes = R.drawable.owl_3,
        title    = "Ready to smash your daily goals?",
        subtitle = "Set your target, track your streaks, and let's unlock your confidence one quick habit at a time.",
    ),
)

// ─── Root onboarding composable ──────────────────────────────────────────────

@Composable
fun OnboardingScreen(
    onGetStarted: () -> Unit = {},
    onAlreadyHaveAccount: () -> Unit = {},
    onAuthSuccess: () -> Unit = {},
) {
    var authEntry by remember { mutableStateOf<AuthEntry?>(null) }

    if (authEntry != null) {
        AuthFlow(
            start = authEntry!!,
            onAuthSuccess = onAuthSuccess,
            onExit = { authEntry = null },
        )
        return
    }

    // Total pages = 3 onboarding + 1 get-started
    val totalPages = onboardingPages.size + 1
    val pagerState = rememberPagerState(pageCount = { totalPages })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream),
    ) {
        HorizontalPager(
            state    = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            if (page < onboardingPages.size) {
                OnboardingPageContent(page = onboardingPages[page])
            } else {
                GetStartedScreen(
                    onGetStarted = {
                        authEntry = AuthEntry.SignUp
                        onGetStarted()
                    },
                    onAlreadyHaveAccount = {
                        authEntry = AuthEntry.SignIn
                        onAlreadyHaveAccount()
                    },
                )
            }
        }

        // Page indicators – only show on onboarding pages (not on get started)
        if (pagerState.currentPage < onboardingPages.size) {
            PageIndicators(
                pageCount   = onboardingPages.size,
                currentPage = pagerState.currentPage,
                modifier    = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
            )
        } else {
            // Get-started screen also shows indicators at bottom (all dots active = last)
            PageIndicators(
                pageCount   = onboardingPages.size,
                currentPage = onboardingPages.size, // past last, all inactive – we show special state
                modifier    = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 32.dp),
            )
        }
    }
}

// ─── Single onboarding page ──────────────────────────────────────────────────

@Composable
fun OnboardingPageContent(page: OnboardingPageData) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream),
    ) {
        // ── Decorative circles ───────────────────────────────────────────────

        // Top-right circle (partially visible, cut off at top)
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = 60.dp, y = (-50).dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(ColorBrown),
        )

        // Left circle (partially visible, cut off at left)
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = (-70).dp, y = 80.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(ColorBrown),
        )

        // Bottom-right circle (partially visible, cut off at right)
        Box(
            modifier = Modifier
                .size(130.dp)
                .offset(x = 60.dp, y = 40.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(ColorBrown.copy(alpha = 0.6f)),
        )

        // Bottom-left circle (partially visible, cut off at left)
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-50).dp, y = 30.dp)
                .align(Alignment.BottomStart)
                .clip(CircleShape)
                .background(ColorBrown.copy(alpha = 0.5f)),
        )

        // ── Content ─────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Upper portion: owl image (right-aligned, ~45% of screen)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.48f),
            ) {
                Image(
                    painter            = painterResource(id = page.imageRes),
                    contentDescription = null,
                    modifier           = Modifier
                        .size(200.dp)
                        .align(Alignment.CenterEnd)
                        .padding(end = 24.dp),
                    contentScale       = ContentScale.Fit,
                )
            }

            // Lower portion: title + subtitle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(top = 16.dp),
            ) {
                Text(
                    text       = page.title,
                    fontSize   = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = ClashGroteskFontFamily,
                    color      = ColorDarkText,
                    lineHeight = 38.sp,
                    modifier   = Modifier.padding(bottom = 16.dp),
                )

                Text(
                    text       = page.subtitle,
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = InterFontFamily,
                    color      = ColorSubText,
                    lineHeight = 22.sp,
                )
            }
        }
    }
}

// ─── Get Started Screen ──────────────────────────────────────────────────────

@Composable
fun GetStartedScreen(
    onGetStarted: () -> Unit = {},
    onAlreadyHaveAccount: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCream),
    ) {
        // ── Decorative circles ───────────────────────────────────────────────

        // Top-right circle
        Box(
            modifier = Modifier
                .size(140.dp)
                .offset(x = 60.dp, y = (-50).dp)
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(ColorBrown),
        )

        // Top-left circle (partially cut off)
        Box(
            modifier = Modifier
                .size(130.dp)
                .offset(x = (-60).dp, y = (-40).dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(ColorBrown.copy(alpha = 0.7f)),
        )

        // Bottom-right circle
        Box(
            modifier = Modifier
                .size(130.dp)
                .offset(x = 60.dp, y = 40.dp)
                .align(Alignment.BottomEnd)
                .clip(CircleShape)
                .background(ColorBrown.copy(alpha = 0.6f)),
        )

        // Bottom-left circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-50).dp, y = 30.dp)
                .align(Alignment.BottomStart)
                .clip(CircleShape)
                .background(ColorBrown.copy(alpha = 0.5f)),
        )

        // ── Center content ───────────────────────────────────────────────────
        Column(
            modifier              = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment   = Alignment.CenterHorizontally,
            verticalArrangement   = Arrangement.Center,
        ) {
            // Logo image ("MACAO Ai" text logo)
            Image(
                painter            = painterResource(id = R.drawable.logo),
                contentDescription = "Macao AI logo",
                modifier           = Modifier
                    .width(230.dp)
                    .height(80.dp),
                contentScale       = ContentScale.Fit,
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Get Started button
            Button(
                onClick  = onGetStarted,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = RoundedCornerShape(28.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = ColorDarkBtn,
                    contentColor   = Color.White,
                ),
            ) {
                Text(
                    text       = "Get Started  →",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = InterFontFamily,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Already have an account button
            Button(
                onClick  = onAlreadyHaveAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape    = RoundedCornerShape(28.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = ColorDarkBtn,
                    contentColor   = Color.White,
                ),
            ) {
                Text(
                    text       = "I already have an account  →",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = InterFontFamily,
                )
            }
        }
    }
}

// ─── Pager indicators ────────────────────────────────────────────────────────

@Composable
fun PageIndicators(
    pageCount   : Int,
    currentPage : Int,
    modifier    : Modifier = Modifier,
) {
    Row(
        modifier              = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        repeat(pageCount) { index ->
            val isActive = index == currentPage
            val width by animateDpAsState(
                targetValue  = if (isActive) 40.dp else 10.dp,
                animationSpec = tween(durationMillis = 300),
                label        = "indicator_$index",
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(10.dp)
                    .width(width)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (isActive) ColorDarkBtn else ColorBrown.copy(alpha = 0.45f)
                    ),
            )
        }
    }
}
