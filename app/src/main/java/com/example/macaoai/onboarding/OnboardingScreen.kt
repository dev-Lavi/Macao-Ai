package com.example.macaoai.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.macaoai.R

// ─── Google Font provider ───────────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage  = "com.google.android.gms",
    certificates     = R.array.com_google_android_gms_fonts_certs,
)

private val InterFontName = GoogleFont("Inter")

private val InterFontFamily = androidx.compose.ui.text.font.FontFamily(
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.ExtraBold),
    Font(googleFont = InterFontName, fontProvider = provider, weight = FontWeight.Black),
)

// ─── Colors ─────────────────────────────────────────────────────────────────

val ColorBlue   = Color(0xFF4271FF)
val ColorOrange = Color(0xFFFFA500)
val ColorPink   = Color(0xFFFF698E)
val ColorBlack  = Color(0xFF111111)

// ─── Data model ─────────────────────────────────────────────────────────────

data class TitlePart(
    val text: String,
    val color: Color,
)

data class OnboardingPageData(
    val backgroundDrawable: Int,
    val titleParts: List<TitlePart>,
    val subtitle: String,
)

val onboardingPages = listOf(
    OnboardingPageData(
        backgroundDrawable = R.drawable.bg_shape,
        titleParts = listOf(
            TitlePart("Speak fluently\n", ColorBlue),
            TitlePart("without the\n", ColorBlack),
            TitlePart("stage fright", ColorOrange),
        ),
        subtitle = "Master a new language naturally by chatting with your personal AI companion anytime, anywhere, completely judgment-free.",
    ),
    OnboardingPageData(
        backgroundDrawable = R.drawable.bg_shape_orange,
        titleParts = listOf(
            TitlePart("Real conversations", ColorBlue),
            TitlePart(",\n", ColorBlack),
            TitlePart("real-time ", ColorBlack),
            TitlePart("feedback", ColorOrange),
        ),
        subtitle = "Practice real-world scenarios, perfect your pronunciation, and get instant corrections from an AI tutor that adapts to your pace.",
    ),
    OnboardingPageData(
        backgroundDrawable = R.drawable.pink_shape,
        titleParts = listOf(
            TitlePart("Ready to ", ColorBlue),
            TitlePart("smash\n", ColorBlack),
            TitlePart("your ", ColorBlack),
            TitlePart("daily goals?", ColorOrange),
        ),
        subtitle = "Set your target, track your streaks, and let's unlock your confidence one quick habit at a time.",
    ),
)

// ─── Root onboarding composable ──────────────────────────────────────────────

@Composable
fun OnboardingScreen() {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            OnboardingPageContent(page = onboardingPages[page])
        }

        // Page indicators
        PageIndicators(
            pageCount  = onboardingPages.size,
            currentPage = pagerState.currentPage,
            modifier   = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
        )
    }
}

// ─── Single page ─────────────────────────────────────────────────────────────

@Composable
fun OnboardingPageContent(page: OnboardingPageData) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        // Upper section: clipped background shape
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.1f),
        ) {
            Image(
                painter        = painterResource(id = page.backgroundDrawable),
                contentDescription = null,
                modifier       = Modifier.fillMaxSize(),
                contentScale   = ContentScale.Crop,
            )
        }

        // Lower section: title + subtitle
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp),
        ) {
            // Multi-colour title
            val annotated = buildAnnotatedString {
                page.titleParts.forEach { part ->
                    withStyle(
                        SpanStyle(
                            color      = part.color,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize   = 32.sp,
                            fontFamily = InterFontFamily,
                        )
                    ) {
                        append(part.text)
                    }
                }
            }

            Text(
                text       = annotated,
                lineHeight = 40.sp,
                modifier   = Modifier.padding(bottom = 16.dp),
            )

            // Subtitle
            Text(
                text       = page.subtitle,
                fontSize   = 15.sp,
                color      = Color(0xFF555555),
                fontWeight = FontWeight.Normal,
                fontFamily = InterFontFamily,
                lineHeight = 23.sp,
            )
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
                        if (isActive) ColorBlack else ColorPink.copy(alpha = 0.55f)
                    ),
            )
        }
    }
}
