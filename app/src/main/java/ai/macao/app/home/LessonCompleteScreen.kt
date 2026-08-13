package ai.macao.app.home

import ai.macao.app.R
import ai.macao.app.theme.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import ai.macao.app.auth.ClashGroteskFontFamily

import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Celebratory Lesson Completion Screen.
 * Shows stats (XP, accuracy) and saving status with the Render backend.
 */
@Composable
fun LessonCompleteScreen(
    xpEarned: Int,
    accuracy: Double,
    isSaving: Boolean,
    saveError: String?,
    onSaveAndContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "completeBob")
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounceScale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Celebratory Header ──────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Subarashii! 🎉",
                    fontFamily = ClashGroteskFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangeAccent,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lesson Completed Successfully",
                    fontFamily = ClashGroteskFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MindfulBrown80,
                    textAlign = TextAlign.Center
                )
            }

            // ── Mascot Celebrate Owl ────────────────────────────────────────
            Image(
                painter = painterResource(id = R.drawable.owl_celebrate),
                contentDescription = "Celebrating Mascot Owl",
                modifier = Modifier
                    .size(240.dp)
                    .scale(bounceScale),
                contentScale = ContentScale.Fit
            )

            // ── Score & Stats Panel ─────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // XP Section
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "POINTS EARNED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MindfulBrown60,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+$xpEarned XP",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OrangeAccent
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(40.dp)
                            .background(MindfulBrown10)
                    )

                    // Accuracy Section
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "ACCURACY",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MindfulBrown60,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${accuracy.toInt()}%",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2A9D8F)
                        )
                    }
                }
            }

            // ── Error Messaging ─────────────────────────────────────────────
            if (saveError != null) {
                Text(
                    text = saveError,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD90429),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // ── Action Save button ──────────────────────────────────────────
            Button(
                onClick = onSaveAndContinue,
                enabled = !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .shadow(6.dp, RoundedCornerShape(18.dp)),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeAccent,
                    disabledContainerColor = MindfulBrown10,
                    contentColor = Color.White,
                    disabledContentColor = MindfulBrown40
                )
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = if (saveError != null) "RETRY SAVING" else "SAVE & CONTINUE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
