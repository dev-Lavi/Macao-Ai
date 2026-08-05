package ai.macao.app.onboarding.ui

import ai.macao.app.R
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Shared font ─────────────────────────────────────────────────────────────

private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage   = "com.google.android.gms",
    certificates      = R.array.com_google_android_gms_fonts_certs,
)

private val InterName = GoogleFont("Inter")

val OnboardingFontFamily = FontFamily(
    Font(googleFont = InterName, fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = InterName, fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = InterName, fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = InterName, fontProvider = provider, weight = FontWeight.Bold),
)

// ─── Color palette ───────────────────────────────────────────────────────────

val BgCream        = Color(0xFFF5F0E8)
val OrangeBorder   = Color(0xFFE8572A)
val PillBackground = Color(0xFFEDE8DF)
val PillSelected   = Color(0xFF2D1A0E)
val TextDark       = Color(0xFF1A0E05)
val TextMedium     = Color(0xFF6B5B4E)
val TextLight      = Color(0xFFA08060)
val SearchBarBg    = Color(0xFFEAE4DA)

// ─── Top bar with back arrow + step progress ─────────────────────────────────

/**
 * Renders the "Profile setup" header with a back arrow and a
 * 5-segment step progress bar, matching the reference design.
 */
@Composable
fun ProfileSetupTopBar(
    currentStep: Int,
    totalSteps: Int = 5,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Back arrow button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PillBackground)
                    .clickable(onClick = onBack),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = "←",
                    color      = TextDark,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text       = "Profile setup",
                fontFamily = OnboardingFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize   = 17.sp,
                color      = TextDark,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        StepProgressBar(currentStep = currentStep, totalSteps = totalSteps)
    }
}

// ─── Segmented progress bar ──────────────────────────────────────────────────

@Composable
fun StepProgressBar(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        repeat(totalSteps) { index ->
            val isCompleted = index < currentStep
            val fraction by animateFloatAsState(
                targetValue   = if (isCompleted) 1f else 0f,
                animationSpec = tween(durationMillis = 350),
                label         = "step_$index",
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50)),
            ) {
                // Track (unfilled)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(PillBackground),
                )
                // Fill (animated)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .background(OrangeBorder),
                )
            }
        }
    }
}

// ─── Question card ───────────────────────────────────────────────────────────

/**
 * Orange-bordered rounded card displaying the step's question text.
 * Matches the reference design exactly.
 */
@Composable
fun QuestionCard(
    question: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 2.dp,
                color = OrangeBorder,
                shape = RoundedCornerShape(20.dp),
            )
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = question,
            fontFamily = OnboardingFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize   = 20.sp,
            color      = TextDark,
            lineHeight = 28.sp,
        )
    }
}

// ─── Option pill (single-choice steps 3–5) ───────────────────────────────────

/**
 * Pill-shaped tappable option used in proficiency, reason, and age steps.
 *
 * Tapping immediately calls [onClick]; there is no separate "Next" button,
 * matching the auto-advance UX shown in the reference design.
 */
@Composable
fun OptionPill(
    text: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor   = if (selected) PillSelected else PillBackground
    val textColor = if (selected) Color.White  else TextDark

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(bgColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text       = text,
            fontFamily = OnboardingFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 15.sp,
            color      = textColor,
        )
    }
}

// ─── Language row (steps 1–2 language picker) ────────────────────────────────

/**
 * A single language row with emoji flag + name pill.
 * Used inside the scrollable language list.
 */
@Composable
fun LanguageRow(
    flag: String,
    name: String,
    selected: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor   = if (selected) PillSelected else PillBackground
    val textColor = if (selected) Color.White  else TextDark

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = flag, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text       = name,
            fontFamily = OnboardingFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 15.sp,
            color      = textColor,
        )
    }
}

// ─── Language search bar ─────────────────────────────────────────────────────

@Composable
fun LanguageSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value         = query,
        onValueChange = onQueryChange,
        singleLine    = true,
        textStyle     = TextStyle(
            fontFamily = OnboardingFontFamily,
            fontSize   = 14.sp,
            color      = TextDark,
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(25.dp))
                    .background(SearchBarBg),
                contentAlignment = Alignment.CenterStart,
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text       = "Search from 1242 languages…",
                                fontFamily = OnboardingFontFamily,
                                fontSize   = 14.sp,
                                color      = TextLight,
                            )
                        }
                        innerTextField()
                    }
                    Text(text = "🔍", fontSize = 16.sp)
                }
            }
        },
        modifier = modifier,
    )
}

// ─── Loading overlay ─────────────────────────────────────────────────────────

/** Full-screen semi-transparent overlay with a centered spinner. */
@Composable
fun LoadingOverlay() {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f)),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color       = OrangeBorder,
            modifier    = Modifier.size(52.dp),
            strokeWidth = 4.dp,
        )
    }
}
