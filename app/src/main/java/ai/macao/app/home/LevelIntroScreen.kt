package ai.macao.app.home

import ai.macao.app.R
import ai.macao.app.home.data.LessonData
import ai.macao.app.home.data.SpeechPracticeState
import ai.macao.app.theme.AppBackground
import ai.macao.app.theme.MindfulBrown
import ai.macao.app.theme.MindfulBrown10
import ai.macao.app.theme.MindfulBrown40
import ai.macao.app.theme.MindfulBrown60
import ai.macao.app.theme.MindfulBrown80
import ai.macao.app.theme.OrangeAccent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import ai.macao.app.auth.ClashGroteskFontFamily
import androidx.compose.runtime.Composable

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Level Introduction Screen (Screen 2).
 * Inspired by Reference 2:
 * - Back button + Level Title header
 * - Prominent Japanese Word/Phrase Card (Japanese, Romaji, Translation)
 * - Animated Owl Mascot (owl_2.png)
 * - Large Microphone Action Button with full interactive state machine (IDLE, LISTENING, PROCESSING, CORRECT)
 */
@Composable
fun LevelIntroScreen(
    lessonData: LessonData = LessonData(
        levelTitle = "Level 1 - Intro",
        japaneseText = "こんにちは",
        romajiText = "ko - ni - chi - wa",
        translationText = "hello",
    ),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var practiceState by remember { mutableStateOf(SpeechPracticeState.IDLE) }

    // Simulated speech interaction loop when mic is tapped
    LaunchedEffect(practiceState) {
        when (practiceState) {
            SpeechPracticeState.LISTENING -> {
                delay(2200)
                practiceState = SpeechPracticeState.PROCESSING
            }
            SpeechPracticeState.PROCESSING -> {
                delay(1200)
                practiceState = SpeechPracticeState.CORRECT
            }
            else -> {}
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // ── Top Header Bar ─────────────────────────────────────────────
            LevelIntroHeader(
                levelTitle = lessonData.levelTitle,
                onBackClick = onBackClick,
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Japanese Word / Phrase Card ────────────────────────────────
            WordCard(lessonData = lessonData)

            Spacer(modifier = Modifier.height(24.dp))

            // ── Animated Mascot Owl ────────────────────────────────────────
            AnimatedOwlMascot(
                practiceState = practiceState,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Status Banner & Microphone Action Area ─────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
            ) {
                // Interactive Status Prompt Text
                StatusPromptText(practiceState = practiceState)

                Spacer(modifier = Modifier.height(20.dp))

                // Large Microphone Action Button
                MicrophoneActionButton(
                    practiceState = practiceState,
                    onMicClick = {
                        practiceState = when (practiceState) {
                            SpeechPracticeState.IDLE -> SpeechPracticeState.LISTENING
                            SpeechPracticeState.CORRECT -> SpeechPracticeState.IDLE
                            else -> SpeechPracticeState.IDLE
                        }
                    },
                )
            }
        }
    }
}

/**
 * Top Header containing Back Button and Level Title.
 */
@Composable
private fun LevelIntroHeader(
    levelTitle: String,
    onBackClick: () -> Unit,
) {
    var isBackPressed by remember { mutableStateOf(false) }
    val backScale by animateFloatAsState(
        targetValue = if (isBackPressed) 0.88f else 1f,
        animationSpec = spring(stiffness = 400f),
        label = "backScale",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Round Back Button
        Box(
            modifier = Modifier
                .size(46.dp)
                .graphicsLayer {
                    scaleX = backScale
                    scaleY = backScale
                }
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, MindfulBrown10, CircleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isBackPressed = true
                            tryAwaitRelease()
                            isBackPressed = false
                        },
                        onTap = { onBackClick() },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Back to map",
                tint = MindfulBrown,
                modifier = Modifier.size(20.dp),
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = levelTitle,
            fontFamily = ClashGroteskFontFamily,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MindfulBrown,
        )
    }
}

/**
 * Word / Phrase Card displaying Japanese text, Romaji pronunciation, and English translation.
 */
@Composable
private fun WordCard(lessonData: LessonData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(32.dp),
                spotColor = Color(0xFFE88D67).copy(alpha = 0.25f),
                ambientColor = MindfulBrown.copy(alpha = 0.1f),
            )
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .border(
                width = 2.dp,
                color = Color(0xFFFFD8CC),
                shape = RoundedCornerShape(32.dp),
            )
            .padding(horizontal = 28.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Japanese Characters (Largest & Bold)
            Text(
                text = lessonData.japaneseText,
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = MindfulBrown,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Romaji Pronunciation Guide (Medium)
            Text(
                text = lessonData.romajiText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MindfulBrown80,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
            )

            Spacer(modifier = Modifier.height(10.dp))

            // English Translation (Smaller Accent)
            Text(
                text = lessonData.translationText,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = OrangeAccent,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Animated Mascot Owl (owl_2.png) with gentle breathing/floating idle animation.
 */
@Composable
private fun AnimatedOwlMascot(
    practiceState: SpeechPracticeState,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "owlFloat")

    // Gentle floating motion
    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "owlY",
    )

    // Subtle scale breathing
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "breathScale",
    )

    val owlDrawable = when (practiceState) {
        SpeechPracticeState.CORRECT -> R.drawable.owl_jump
        SpeechPracticeState.LISTENING -> R.drawable.owl_1
        else -> R.drawable.owl_2
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = owlDrawable),
            contentDescription = "Language Learning Owl Mascot",
            modifier = Modifier
                .size(240.dp)
                .offset(y = floatOffsetY.dp)
                .scale(breathScale),
            contentScale = ContentScale.Fit,
        )
    }
}

/**
 * Interactive Status Prompt text under owl mascot.
 */
@Composable
private fun StatusPromptText(practiceState: SpeechPracticeState) {
    val promptText = when (practiceState) {
        SpeechPracticeState.IDLE -> "Tap the microphone to speak"
        SpeechPracticeState.LISTENING -> "Listening... Say \"Konnichiwa\" 🎙️"
        SpeechPracticeState.PROCESSING -> "Analyzing pronunciation... ⚡"
        SpeechPracticeState.CORRECT -> "Subarashii! Perfect pronunciation! 🎉"
        SpeechPracticeState.INCORRECT -> "Almost! Tap to try again."
    }

    val textColor = when (practiceState) {
        SpeechPracticeState.CORRECT -> Color(0xFF2A9D8F)
        SpeechPracticeState.LISTENING -> OrangeAccent
        SpeechPracticeState.PROCESSING -> Color(0xFFE76F51)
        else -> MindfulBrown60
    }

    Text(
        text = promptText,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
        textAlign = TextAlign.Center,
    )
}

/**
 * Large 76dp Microphone Action Button with glowing wave rings during listening state.
 */
@Composable
private fun MicrophoneActionButton(
    practiceState: SpeechPracticeState,
    onMicClick: () -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }

    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.5f),
        label = "micScale",
    )

    // Pulsing wave animation when LISTENING
    val infiniteTransition = rememberInfiniteTransition(label = "wavePulse")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (practiceState == SpeechPracticeState.LISTENING) 1.45f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "waveScale",
    )

    val buttonColor = when (practiceState) {
        SpeechPracticeState.CORRECT -> Color(0xFF2A9D8F)
        SpeechPracticeState.LISTENING -> Color(0xFFE76F51)
        SpeechPracticeState.PROCESSING -> Color(0xFFF4A261)
        else -> Color(0xFFEF233C) // Vivid red microphone button from reference 2
    }

    Box(
        contentAlignment = Alignment.Center,
    ) {
        // Listening pulse wave ring
        if (practiceState == SpeechPracticeState.LISTENING) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(waveScale)
                    .clip(CircleShape)
                    .background(Color(0xFFEF233C).copy(alpha = 0.25f)),
            )
        }

        // Processing progress indicator ring
        if (practiceState == SpeechPracticeState.PROCESSING) {
            CircularProgressIndicator(
                color = Color(0xFFF4A261),
                strokeWidth = 4.dp,
                modifier = Modifier.size(92.dp),
            )
        }

        // Main Microphone Button Container
        Box(
            modifier = Modifier
                .size(76.dp)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .shadow(
                    elevation = 14.dp,
                    shape = CircleShape,
                    spotColor = buttonColor.copy(alpha = 0.5f),
                    ambientColor = buttonColor.copy(alpha = 0.3f),
                )
                .clip(CircleShape)
                .background(buttonColor)
                .pointerInput(practiceState) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onMicClick() },
                    )
                },
            contentAlignment = Alignment.Center,
        ) {
            val iconRes = if (practiceState == SpeechPracticeState.CORRECT) R.drawable.tick else R.drawable.mic

            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Microphone Practice Action",
                tint = Color.White,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}
