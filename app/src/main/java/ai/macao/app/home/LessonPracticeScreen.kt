package ai.macao.app.home

import ai.macao.app.R
import ai.macao.app.data.network.LessonItemResponse
import ai.macao.app.home.data.SpeechPracticeState
import ai.macao.app.theme.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Screen for practicing lesson items (vocabulary, pronunciations).
 * Reuses the friendly styling and interactive speech practice state machine.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonPracticeScreen(
    lessonTitle: String,
    currentItem: LessonItemResponse,
    currentIndex: Int,
    totalItems: Int,
    practiceState: SpeechPracticeState,
    onMicClick: () -> Unit,
    onNextClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            // ── Top Progress Header Bar ─────────────────────────────────────────
            LessonProgressHeader(
                lessonTitle = lessonTitle,
                currentIndex = currentIndex,
                totalItems = totalItems,
                onCancelClick = onCancelClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Vocabulary Word Card ────────────────────────────────────────────
            PracticeWordCard(item = currentItem)

            Spacer(modifier = Modifier.height(16.dp))

            // ── Animated Mascot Owl based on Practice state ───────────────────
            AnimatedPracticeOwl(practiceState = practiceState, modifier = Modifier.weight(1f))

            Spacer(modifier = Modifier.height(12.dp))

            // ── Status banner & Microphone practice button ──────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            ) {
                // Instruction/Feedback Prompt text
                PracticeStatusPrompt(practiceState = practiceState)

                Spacer(modifier = Modifier.height(16.dp))

                // Microphone round button with animations
                PracticeMicrophoneButton(
                    practiceState = practiceState,
                    onMicClick = onMicClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ── Advanced Continue Button ────────────────────────────────────
                Button(
                    onClick = onNextClick,
                    enabled = practiceState == SpeechPracticeState.CORRECT,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(4.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeAccent,
                        disabledContainerColor = MindfulBrown10,
                        contentColor = Color.White,
                        disabledContentColor = MindfulBrown40
                    )
                ) {
                    Text(
                        text = if (currentIndex == totalItems - 1) "FINISH LESSON" else "CONTINUE",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonProgressHeader(
    lessonTitle: String,
    currentIndex: Int,
    totalItems: Int,
    onCancelClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cancel/Back Button
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, MindfulBrown10, CircleShape)
                .clickable { onCancelClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back),
                contentDescription = "Cancel Lesson",
                tint = MindfulBrown,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Linear Progress bar with numerical progress indicator
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = lessonTitle,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MindfulBrown
                )
                Text(
                    text = "${currentIndex + 1} / $totalItems",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeAccent
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            val progressFraction = (currentIndex + 1).toFloat() / totalItems
            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = OrangeAccent,
                trackColor = MindfulBrown10
            )
        }
    }
}

@Composable
private fun PracticeWordCard(item: LessonItemResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(28.dp), spotColor = OrangeAccent.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFFFFD8CC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main foreign word text
            Text(
                text = item.word,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                color = MindfulBrown,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Romaji / Pronunciation
            Text(
                text = "/ ${item.pronunciation} /",
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium,
                color = MindfulBrown80,
                letterSpacing = 1.5.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Divider line
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(1.dp)
                    .background(MindfulBrown10)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // English Translation
            Text(
                text = item.translation,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent,
                textAlign = TextAlign.Center
            )

            // Example sentences if present
            if (item.exampleSentence.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "“${item.exampleSentence}”",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MindfulBrown60,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = item.exampleTranslation,
                    fontSize = 12.sp,
                    color = MindfulBrown40,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AnimatedPracticeOwl(
    practiceState: SpeechPracticeState,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "owlPracticeFloat")

    // Breathing float animation
    val floatY by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    // Owl assets mapped to educational state
    val owlRes = when (practiceState) {
        SpeechPracticeState.CORRECT -> R.drawable.owl_win
        SpeechPracticeState.LISTENING -> R.drawable.owl_headphone
        SpeechPracticeState.PROCESSING -> R.drawable.owl_detective
        SpeechPracticeState.INCORRECT -> R.drawable.owl_shy
        else -> R.drawable.owl_reading
    }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = owlRes),
            contentDescription = "Active Owl Mascot Helper",
            modifier = Modifier
                .size(200.dp)
                .offset(y = floatY.dp),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
private fun PracticeStatusPrompt(practiceState: SpeechPracticeState) {
    val promptText = when (practiceState) {
        SpeechPracticeState.IDLE -> "Tap the microphone to speak"
        SpeechPracticeState.LISTENING -> "Listening... Repeat the word! 🎙️"
        SpeechPracticeState.PROCESSING -> "Analyzing pronunciation... ⚡"
        SpeechPracticeState.CORRECT -> "Awesome! Perfect pronunciation! 🎉"
        SpeechPracticeState.INCORRECT -> "Almost! Tap the mic to try again."
    }

    val textColor = when (practiceState) {
        SpeechPracticeState.CORRECT -> Color(0xFF2A9D8F)
        SpeechPracticeState.LISTENING -> OrangeAccent
        SpeechPracticeState.PROCESSING -> Color(0xFFF4A261)
        SpeechPracticeState.INCORRECT -> Color(0xFFD90429)
        else -> MindfulBrown60
    }

    Text(
        text = promptText,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = textColor,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun PracticeMicrophoneButton(
    practiceState: SpeechPracticeState,
    onMicClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val buttonScale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.5f),
        label = "buttonScale"
    )

    // Pulsing circle when listening
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (practiceState == SpeechPracticeState.LISTENING) 1.4f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonColor = when (practiceState) {
        SpeechPracticeState.CORRECT -> Color(0xFF2A9D8F)
        SpeechPracticeState.LISTENING -> Color(0xFFE76F51)
        SpeechPracticeState.PROCESSING -> Color(0xFFF4A261)
        SpeechPracticeState.INCORRECT -> Color(0xFFD90429)
        else -> Color(0xFFEF233C) // Vivid red
    }

    Box(contentAlignment = Alignment.Center) {
        if (practiceState == SpeechPracticeState.LISTENING) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(buttonColor.copy(alpha = 0.25f))
            )
        }

        if (practiceState == SpeechPracticeState.PROCESSING) {
            CircularProgressIndicator(
                color = Color(0xFFF4A261),
                strokeWidth = 3.dp,
                modifier = Modifier.size(86.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(72.dp)
                .graphicsLayer {
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .shadow(8.dp, CircleShape, spotColor = buttonColor.copy(0.4f))
                .clip(CircleShape)
                .background(buttonColor)
                .pointerInput(practiceState) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onTap = { onMicClick() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val iconRes = if (practiceState == SpeechPracticeState.CORRECT) R.drawable.tick else R.drawable.mic
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = "Practice Speech",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
