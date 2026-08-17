package ai.macao.app.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import ai.macao.app.R
import ai.macao.app.auth.ClashGroteskFontFamily
import ai.macao.app.home.audio.TextToSpeechManager
import ai.macao.app.home.data.ConversationState
import ai.macao.app.home.viewmodel.ChatMessage
import ai.macao.app.home.viewmodel.ConversationViewModel
import ai.macao.app.theme.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import ai.macao.app.data.network.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    viewModel: ConversationViewModel,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val chatListState = rememberLazyListState()
    val context = LocalContext.current

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.startListening()
        }
    }

    val onMicAction = {
        if (hasAudioPermission) {
            if (state.state == ConversationState.SHOWING_TARGET || state.state == ConversationState.RETRY) {
                viewModel.startListening()
            }
        } else {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Auto-scroll chat history to the bottom when new message arrives
    LaunchedEffect(state.history.size) {
        if (state.history.isNotEmpty()) {
            chatListState.animateScrollToItem(state.history.size - 1)
        }
    }

    if (state.state == ConversationState.INITIALIZING) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = OrangeAccent)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Initializing conversation...",
                    fontFamily = ClashGroteskFontFamily,
                    color = MindfulBrown80,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    if (state.state == ConversationState.COMPLETED) {
        ConversationCompleteOverlay(
            score = state.score,
            xpEarned = state.xpEarned,
            streak = state.currentStreak,
            isSaving = state.isSaving,
            levelUnlocked = state.levelUnlocked,
            unlockMessage = state.unlockMessage,
            onFinish = onCancelClick
        )
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Top Header ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel/Back Icon Button
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
                        contentDescription = "Exit Conversation",
                        tint = MindfulBrown,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Progress Info
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Owl Conversation",
                            fontFamily = ClashGroteskFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MindfulBrown
                        )
                        Text(
                            text = "${state.turnNumber} / ${state.totalTurns}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = OrangeAccent
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val progressFraction = state.turnNumber.toFloat() / state.totalTurns.coerceAtLeast(1)
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

            // ── Chat & Mascot Area ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                if (state.history.isEmpty()) {
                    // Show centered Mascot Owl welcoming the student
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(bottom = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedMascotOwl(state = state.state)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Welcome to scenario practice!\nTap the microphone to speak.",
                            color = MindfulBrown60,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // List of Chat Bubbles
                    LazyColumn(
                        state = chatListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.history) { msg ->
                            ChatBubble(message = msg)
                        }
                    }
                }
            }

            // ── Target Phrase Card ──────────────────────────────────────────
            AnimatedVisibility(
                visible = state.currentTarget != null && state.state != ConversationState.COMPLETED
            ) {
                state.currentTarget?.let { target ->
                    TargetSentenceCard(
                        target = target,
                        slowMode = state.slowMode,
                        onSlowToggle = { viewModel.toggleSlowMode() },
                        onReplayTarget = { viewModel.speakCurrentTarget() }
                    )
                }
            }

            // ── Action Feedback / Mic Button Bar ────────────────────────────
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 16.dp, bottom = 24.dp)
                    .shadow(12.dp, spotColor = Color.Black.copy(alpha = 0.05f))
            ) {
                // Feedback Correction / Status
                if (state.state == ConversationState.FEEDBACK && state.correctionMessage != null) {
                    FeedbackCorrectionCard(
                        correction = state.correctionMessage!!,
                        onRetry = { viewModel.retryTurn() }
                    )
                } else {
                    Text(
                        text = when {
                            !hasAudioPermission -> "Microphone access is needed to practice speaking 🎙️"
                            state.state == ConversationState.LISTENING -> "Listening... Read the sentence! 🎙️"
                            state.state == ConversationState.PROCESSING -> "Analyzing speech transcript... ⚡"
                            state.state == ConversationState.OWL_SPEAKING -> "Listen to the Owl tutor..."
                            state.state == ConversationState.RETRY -> "Let's try again!"
                            else -> "Tap the microphone when you are ready to speak"
                        },
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            !hasAudioPermission -> Color(0xFFD90429)
                            state.state == ConversationState.LISTENING -> OrangeAccent
                            state.state == ConversationState.PROCESSING -> Color(0xFFF4A261)
                            state.state == ConversationState.RETRY -> Color(0xFFD90429)
                            else -> MindfulBrown60
                        },
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 48.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        MicButton(
                            state = state.state,
                            onMicClick = onMicAction
                        )
                    }
                }

                // Error Notification Panel
                state.error?.let { errMsg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Snackbar(
                        action = {
                            Text(
                                "Dismiss",
                                modifier = Modifier.clickable { viewModel.dismissError() },
                                color = OrangeAccent,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Text(errMsg)
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val bubbleShape = if (message.isOwl) {
        RoundedCornerShape(topStart = 0.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 0.dp, bottomEnd = 20.dp, bottomStart = 20.dp)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isOwl) Arrangement.Start else Arrangement.End
    ) {
        if (message.isOwl) {
            // Owl Mini Icon
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, MindfulBrown10, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.owl_win),
                    contentDescription = "Owl Tutor",
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = bubbleShape,
            color = if (message.isOwl) Color.White else OrangeAccent,
            border = if (message.isOwl) BorderStroke(1.dp, MindfulBrown10) else null,
            shadowElevation = 2.dp,
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (message.isOwl) MindfulBrown else Color.White
                )

                if (message.isOwl && message.romanization.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.romanization,
                        fontSize = 13.sp,
                        fontStyle = FontStyle.Italic,
                        color = MindfulBrown80
                    )
                }

                if (message.isOwl && message.meaning.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.height(1.dp).fillMaxWidth().background(MindfulBrown10))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message.meaning,
                        fontSize = 13.sp,
                        color = MindfulBrown60
                    )
                }
            }
        }
    }
}

@Composable
private fun TargetSentenceCard(
    target: ConversationTarget,
    slowMode: Boolean,
    onSlowToggle: () -> Unit,
    onReplayTarget: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.5.dp, Color(0xFFFFD8CC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Say this:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = OrangeAccent,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = target.targetText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MindfulBrown,
                textAlign = TextAlign.Center
            )

            if (target.romanization.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "/ ${target.romanization} /",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MindfulBrown80,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(1.dp)
                    .background(MindfulBrown10)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "\"${target.meaning}\"",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MindfulBrown60,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Play / Speak Target Button
                TextButton(
                    onClick = onReplayTarget,
                    colors = ButtonDefaults.textButtonColors(contentColor = OrangeAccent)
                ) {
                    Text("🔊 Hear Owl", fontWeight = FontWeight.Bold)
                }

                // Slow Speed Toggle
                OutlinedButton(
                    onClick = onSlowToggle,
                    border = BorderStroke(1.dp, if (slowMode) OrangeAccent else MindfulBrown10),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = if (slowMode) OrangeAccent else MindfulBrown60,
                        containerColor = if (slowMode) OrangeAccent.copy(alpha = 0.08f) else Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (slowMode) "🐢 Slow On" else "🐢 Slow Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedbackCorrectionCard(
    correction: ConversationMessageDetail,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
        border = BorderStroke(1.5.dp, Color(0xFFFFCCCC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Almost! Feedback:",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD90429)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = correction.text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MindfulBrown,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = correction.meaning,
                fontSize = 14.sp,
                color = MindfulBrown60,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD90429)),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("Tap to Retry", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun AnimatedMascotOwl(state: ConversationState) {
    val infiniteTransition = rememberInfiniteTransition(label = "owlFloat")
    val floatY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatY"
    )

    val owlRes = when (state) {
        ConversationState.LISTENING -> R.drawable.owl_headphone
        ConversationState.PROCESSING -> R.drawable.owl_detective
        ConversationState.OWL_SPEAKING -> R.drawable.owl_win
        ConversationState.FEEDBACK, ConversationState.RETRY -> R.drawable.owl_shy
        else -> R.drawable.owl_reading
    }

    Image(
        painter = painterResource(id = owlRes),
        contentDescription = "Tutor Owl",
        modifier = Modifier
            .size(160.dp)
            .offset(y = floatY.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun MicButton(
    state: ConversationState,
    onMicClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.5f),
        label = "scale"
    )

    // Pulsing circle when listening
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (state == ConversationState.LISTENING) 1.40f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonColor = when (state) {
        ConversationState.LISTENING -> Color(0xFFE76F51)
        ConversationState.PROCESSING -> Color(0xFFF4A261)
        else -> Color(0xFFEF233C)
    }

    Box(contentAlignment = Alignment.Center) {
        if (state == ConversationState.LISTENING) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(buttonColor.copy(alpha = 0.25f))
            )
        }

        if (state == ConversationState.PROCESSING) {
            CircularProgressIndicator(
                color = Color(0xFFF4A261),
                strokeWidth = 3.dp,
                modifier = Modifier.size(90.dp)
            )
        }

        Box(
            modifier = Modifier
                .size(76.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(8.dp, CircleShape, spotColor = buttonColor.copy(0.4f))
                .clip(CircleShape)
                .background(buttonColor)
                .pointerInput(state) {
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
            Icon(
                painter = painterResource(id = R.drawable.mic),
                contentDescription = "Talk to Owl",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

@Composable
private fun ConversationCompleteOverlay(
    score: Int,
    xpEarned: Int,
    streak: Int,
    isSaving: Boolean,
    levelUnlocked: Boolean,
    unlockMessage: String,
    onFinish: () -> Unit
) {
    Box(
        modifier = Modifier
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

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Yatta! 🎉",
                    fontFamily = ClashGroteskFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangeAccent,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Conversation Completed Successfully",
                    fontFamily = ClashGroteskFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MindfulBrown80,
                    textAlign = TextAlign.Center
                )
            }

            Image(
                painter = painterResource(id = R.drawable.owl_celebrate),
                contentDescription = "Celebrate Owl",
                modifier = Modifier.size(240.dp),
                contentScale = ContentScale.Fit
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OrangeAccent
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(36.dp)
                                .background(MindfulBrown10)
                        )

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
                                text = "$score%",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2A9D8F)
                            )
                        }
                    }

                    if (levelUnlocked) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MindfulBrown10)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "🔓 Level Unlocked: $unlockMessage",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF4A261),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Button(
                onClick = onFinish,
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
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "FINISH CONVERSATION",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
