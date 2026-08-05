package ai.macao.app.onboarding.ui

import ai.macao.app.R
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ─── Shared brown palette for these two screens ───────────────────────────────

private val BrownBg     = Color(0xFFB08060)   // main background
private val BrownLight  = Color(0xFFCBAA8A)   // circle/pill base
private val BrownCircle = Color(0xFFD4B89A)   // 4-dot circles (brown/20 equivalent)

// ─── Screen 1: Compiling data ─────────────────────────────────────────────────

/**
 * Shown immediately after the Firestore write succeeds.
 *
 * Four animated pulsing circles arranged in a 2×2 cross pattern.
 * Each circle pulses with a staggered scale animation to create a
 * "breathing" group effect.
 *
 * Auto-advances to the All Set screen after [durationMs] milliseconds.
 */
@Composable
fun CompilingDataScreen(
    onAnimationComplete: () -> Unit,
    durationMs: Long = 2800L,
) {
    // Auto-advance after the duration
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(durationMs)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrownBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ── Four pulsing circles in a 2×2 cross ───────────────────────────
            FourDotLoader()

            Spacer(modifier = Modifier.height(40.dp))

            // ── "Compiling Data..." ───────────────────────────────────────────
            Text(
                text       = "Compiling Data...",
                fontFamily = OnboardingFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 26.sp,
                color      = Color.White,
                textAlign  = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── Subtitle ──────────────────────────────────────────────────────
            Text(
                text       = "Please wait… We're calculating the\ndata based on your assessment inputs.",
                fontFamily = OnboardingFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize   = 15.sp,
                color      = Color.White.copy(alpha = 0.85f),
                textAlign  = TextAlign.Center,
                lineHeight = 22.sp,
                modifier   = Modifier.padding(horizontal = 40.dp),
            )
        }
    }
}

// ─── Four-dot pulsing loader ──────────────────────────────────────────────────

@Composable
private fun FourDotLoader() {
    // Each circle gets a staggered infinite scale animation
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    val scales = List(4) { index ->
        infiniteTransition.animateFloat(
            initialValue  = 0.85f,
            targetValue   = 1.15f,
            animationSpec = infiniteRepeatable(
                animation  = tween(
                    durationMillis = 700,
                    delayMillis    = index * 160,
                    easing         = FastOutSlowInEasing,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "scale_$index",
        )
    }

    val circleSize = 54.dp

    // 2×2 cross arrangement:
    //      [top]
    //  [left][right]
    //      [bottom]
    Box(
        modifier         = Modifier.size(140.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Top
        Box(
            modifier = Modifier
                .offset(y = (-46).dp)
                .size(circleSize)
                .scale(scales[0].value)
                .clip(CircleShape)
                .background(BrownCircle),
        )
        // Left
        Box(
            modifier = Modifier
                .offset(x = (-46).dp)
                .size(circleSize)
                .scale(scales[1].value)
                .clip(CircleShape)
                .background(BrownCircle),
        )
        // Right
        Box(
            modifier = Modifier
                .offset(x = 46.dp)
                .size(circleSize)
                .scale(scales[2].value)
                .clip(CircleShape)
                .background(BrownCircle),
        )
        // Bottom
        Box(
            modifier = Modifier
                .offset(y = 46.dp)
                .size(circleSize)
                .scale(scales[3].value)
                .clip(CircleShape)
                .background(BrownCircle),
        )
    }
}

// ─── Screen 2: All Set ────────────────────────────────────────────────────────

/**
 * "You're all Set Up." screen shown after the compiling animation.
 *
 * Features:
 * - tick.xml icon at the top
 * - Circular owl image ([owl_white_lines_bg])
 * - Paragraph with motivational copy
 * - "Let's Start Learning →" capsule button using [arrow_right] drawable
 */
@Composable
fun AllSetScreen(
    onGetStarted: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrownBg),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            // ── Tick icon ─────────────────────────────────────────────────────
            Image(
                painter            = painterResource(R.drawable.tick),
                contentDescription = "Completed",
                colorFilter        = ColorFilter.tint(Color.White),
                modifier           = Modifier.size(36.dp),
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Headline ──────────────────────────────────────────────────────
            Text(
                text       = "You're all Set Up.",
                fontFamily = OnboardingFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize   = 28.sp,
                color      = Color.White,
                textAlign  = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Circular owl image ────────────────────────────────────────────
            Box(
                modifier         = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(BrownLight),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter            = painterResource(R.drawable.owl_white_lines_bg),
                    contentDescription = "Owl mascot",
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize(),
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Body text ─────────────────────────────────────────────────────
            Text(
                text = "Your language journey starts now! Every word you " +
                    "learn brings you closer to real conversations. " +
                    "Let's take the first step together.",
                fontFamily  = OnboardingFontFamily,
                fontWeight  = FontWeight.Normal,
                fontSize    = 15.sp,
                color       = Color.White.copy(alpha = 0.90f),
                textAlign   = TextAlign.Center,
                lineHeight  = 22.sp,
                modifier    = Modifier.padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── "Let's Start Learning" capsule button ─────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(29.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .clickable(onClick = onGetStarted),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text       = "Let's Start Learning",
                        fontFamily = OnboardingFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = Color.White,
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Image(
                        painter            = painterResource(R.drawable.arrow_right),
                        contentDescription = "Arrow right",
                        colorFilter        = ColorFilter.tint(Color.White),
                        modifier           = Modifier.size(22.dp),
                    )
                }
            }
        }
    }
}
