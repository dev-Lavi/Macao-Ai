package ai.macao.app.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import ai.macao.app.R
import ai.macao.app.auth.ClashGroteskFontFamily
import ai.macao.app.home.audio.SpeechRecognizerManager
import ai.macao.app.home.audio.SpeechRecognizerManagerImpl
import ai.macao.app.home.audio.TextToSpeechManager
import ai.macao.app.home.audio.TextToSpeechManagerImpl
import ai.macao.app.home.data.*
import ai.macao.app.theme.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Situation-Based Language Learning Screen for Missions (e.g. Mission 1: The Airport).
 * Reuses the existing design language, TTS, and Speech Recognition engines.
 */
@Composable
fun SituationMissionScreen(
    missionData: MissionData,
    languageCode: String,
    onMissionComplete: (xpEarned: Int, accuracy: Double) -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsManager = remember { TextToSpeechManagerImpl(context) }
    val speechManager = remember { SpeechRecognizerManagerImpl(context) }

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var pendingMicAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            pendingMicAction?.invoke()
        }
        pendingMicAction = null
    }

    val requestAudioPermissionAndRun: (() -> Unit) -> Unit = { action ->
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            hasAudioPermission = true
            action()
        } else {
            pendingMicAction = action
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            ttsManager.shutdown()
            speechManager.destroy()
        }
    }

    var currentStepType by remember { mutableStateOf(SituationStepType.INTRO) }

    // State variables for individual steps
    var selectedPackingIds by remember { mutableStateOf(setOf<String>()) }
    var currentPlaceIndex by remember { mutableIntStateOf(0) }
    var selectedGateOption by remember { mutableStateOf<String?>(null) }
    var selectedSeatOption by remember { mutableStateOf<String?>(null) }
    var speakingState by remember { mutableStateOf(SpeechPracticeState.IDLE) }
    var spokenTranscript by remember { mutableStateOf("") }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        when (currentStepType) {
            SituationStepType.INTRO -> {
                MissionIntroStep(
                    mission = missionData,
                    onStartMission = { currentStepType = SituationStepType.PACKING },
                    onCancelClick = onCancelClick
                )
            }

            SituationStepType.PACKING -> {
                EssentialPackingStep(
                    missionNumber = missionData.missionNumber,
                    items = missionData.packingItems,
                    selectedIds = selectedPackingIds,
                    onItemClick = { item ->
                        val newSet = if (selectedPackingIds.contains(item.id)) {
                            selectedPackingIds - item.id
                        } else {
                            selectedPackingIds + item.id
                        }
                        selectedPackingIds = newSet
                        ttsManager.speak(item.targetName, languageCode)
                    },
                    onSpeakerClick = { item -> ttsManager.speak(item.targetName, languageCode) },
                    onReadyToFly = { currentStepType = SituationStepType.LEARN_PLACES },
                    onBackClick = { currentStepType = SituationStepType.INTRO }
                )
            }

            SituationStepType.LEARN_PLACES -> {
                LearnPlacesStep(
                    places = missionData.places,
                    currentIndex = currentPlaceIndex,
                    languageCode = languageCode,
                    ttsManager = ttsManager,
                    speechManager = speechManager,
                    onRequestAudioPermission = requestAudioPermissionAndRun,
                    onNextPlace = {
                        if (currentPlaceIndex < missionData.places.lastIndex) {
                            currentPlaceIndex++
                        } else {
                            currentStepType = SituationStepType.LISTENING_ANNOUNCEMENT
                        }
                    },
                    onBackClick = { currentStepType = SituationStepType.PACKING }
                )
            }

            SituationStepType.LISTENING_ANNOUNCEMENT -> {
                AirportAnnouncementStep(
                    question = missionData.announcement,
                    languageCode = languageCode,
                    selectedOption = selectedGateOption,
                    ttsManager = ttsManager,
                    onSelectOption = { selectedGateOption = it },
                    onContinue = { currentStepType = SituationStepType.FIND_SEAT },
                    onBackClick = { currentStepType = SituationStepType.LEARN_PLACES }
                )
            }

            SituationStepType.FIND_SEAT -> {
                FindSeatStep(
                    seatChallenge = missionData.seatChallenge,
                    selectedOption = selectedSeatOption,
                    onSelectOption = { selectedSeatOption = it },
                    onContinue = { currentStepType = SituationStepType.SPEAKING_CHALLENGE },
                    onBackClick = { currentStepType = SituationStepType.LISTENING_ANNOUNCEMENT }
                )
            }

            SituationStepType.SPEAKING_CHALLENGE -> {
                CoffeeSpeakingStep(
                    challenge = missionData.speakingChallenge,
                    practiceState = speakingState,
                    spokenTranscript = spokenTranscript,
                    languageCode = languageCode,
                    ttsManager = ttsManager,
                    speechManager = speechManager,
                    onMicClick = {
                        if (speakingState == SpeechPracticeState.IDLE || speakingState == SpeechPracticeState.INCORRECT) {
                            requestAudioPermissionAndRun {
                                speakingState = SpeechPracticeState.LISTENING
                                speechManager.startListening(
                                    languageCode = languageCode,
                                    onPartialResult = { partial ->
                                        spokenTranscript = partial
                                    },
                                    onFinalResult = { finalResult ->
                                        spokenTranscript = finalResult
                                        speakingState = SpeechPracticeState.PROCESSING
                                        val isMatch = validateBeginnerSpeech(finalResult, missionData.speakingChallenge.phrase)
                                        speakingState = if (isMatch) SpeechPracticeState.CORRECT else SpeechPracticeState.INCORRECT
                                    },
                                    onError = { errorMsg ->
                                        speakingState = SpeechPracticeState.INCORRECT
                                    }
                                )
                            }
                        } else if (speakingState == SpeechPracticeState.CORRECT) {
                            currentStepType = SituationStepType.MISSION_COMPLETE
                        }
                    },
                    onSkipSpeaking = {
                        currentStepType = SituationStepType.MISSION_COMPLETE
                    },
                    onBackClick = { currentStepType = SituationStepType.FIND_SEAT }
                )
            }

            SituationStepType.MISSION_COMPLETE -> {
                MissionCompleteStep(
                    summary = missionData.summary,
                    onReturnHome = {
                        onMissionComplete(120, 100.0)
                    }
                )
            }
        }
    }
}

/**
 * Accurate speech validator that compares spoken audio against primary target and parenthetical romaji.
 */
private fun validateBeginnerSpeech(spoken: String, target: String): Boolean {
    if (spoken.isBlank() || target.isBlank()) return false

    val primaryTarget = target.substringBefore("(").trim()
    val romajiTarget = if (target.contains("(")) {
        target.substringAfter("(").substringBefore(")").trim()
    } else ""

    val cleanSpoken = spoken.lowercase().replace(Regex("[^\\p{L}\\p{N}\\s]"), "").trim()
    val cleanPrimary = primaryTarget.lowercase().replace(Regex("[^\\p{L}\\p{N}\\s]"), "").trim()
    val cleanRomaji = romajiTarget.lowercase().replace(Regex("[^\\p{L}\\p{N}\\s]"), "").trim()

    if (cleanSpoken.isBlank()) return false

    // Check direct equality or substring inclusion with primary target or romaji guide
    if (cleanPrimary.isNotEmpty() && (cleanSpoken == cleanPrimary || cleanSpoken.contains(cleanPrimary) || cleanPrimary.contains(cleanSpoken))) {
        return true
    }
    if (cleanRomaji.isNotEmpty() && (cleanSpoken == cleanRomaji || cleanSpoken.contains(cleanRomaji) || cleanRomaji.contains(cleanSpoken))) {
        return true
    }

    val spokenTokens = cleanSpoken.split("\\s+".toRegex()).filter { it.isNotEmpty() }
    val targetTokens = (cleanPrimary + " " + cleanRomaji).split("\\s+".toRegex()).filter { it.isNotEmpty() }

    if (spokenTokens.isEmpty() || targetTokens.isEmpty()) return false

    val matches = spokenTokens.count { token ->
        targetTokens.any { t -> t == token || t.contains(token) || token.contains(t) }
    }
    return matches > 0 && matches >= (targetTokens.size * 0.5).toInt().coerceAtLeast(1)
}

// ─── STEP 1: MISSION INTRO ───────────────────────────────────────────────────

@Composable
private fun MissionIntroStep(
    mission: MissionData,
    onStartMission: () -> Unit,
    onCancelClick: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, MindfulBrown10, CircleShape)
                    .clickable { onCancelClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Close",
                    tint = MindfulBrown,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = "Voyage",
                fontFamily = ClashGroteskFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MindfulBrown
            )

            Spacer(modifier = Modifier.width(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Image Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shadow(6.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Image(
                painter = painterResource(id = mission.resolveHeroImageRes()),
                contentDescription = "Airport Voyage Hero",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats Row (Arrival & Reward)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Arrival Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.takeoff),
                        contentDescription = "Arrival",
                        tint = MindfulBrown,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ARRIVAL",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MindfulBrown60,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mission.arrivalCode,
                        fontFamily = ClashGroteskFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MindfulBrown
                    )
                }
            }

            // Reward Card
            Card(
                modifier = Modifier
                    .weight(1f)
                    .shadow(4.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.coffee),
                        contentDescription = "Reward",
                        tint = MindfulBrown,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "REWARD",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MindfulBrown60,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mission.rewardTitle,
                        fontFamily = ClashGroteskFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MindfulBrown,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mission Header Text
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFEAD9))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.compass),
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = mission.missionNumber,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = mission.title,
                fontFamily = ClashGroteskFontFamily,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MindfulBrown
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = mission.subtitle,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MindfulBrown80
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Mission Goal Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.star),
                        contentDescription = null,
                        tint = MindfulBrown,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mission Goal",
                        fontFamily = ClashGroteskFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MindfulBrown
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = mission.goalDescription,
                    fontSize = 14.sp,
                    color = MindfulBrown80,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "PROGRESS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MindfulBrown60,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "0%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MindfulBrown
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = OrangeAccent,
                    trackColor = MindfulBrown10
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bottom CTA
        Button(
            onClick = onStartMission,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(6.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MindfulBrown)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Start Mission",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── STEP 2: ESSENTIAL PACKING ───────────────────────────────────────────────

@Composable
private fun EssentialPackingStep(
    missionNumber: String,
    items: List<PackingItem>,
    selectedIds: Set<String>,
    onItemClick: (PackingItem) -> Unit,
    onSpeakerClick: (PackingItem) -> Unit,
    onReadyToFly: () -> Unit,
    onBackClick: () -> Unit,
) {
    val lastSelected = remember(selectedIds) {
        items.find { selectedIds.contains(it.id) }
    }
    val requiredSelectedCount = remember(selectedIds) {
        items.filter { it.isRequired }.count { selectedIds.contains(it.id) }
    }
    val isReady = requiredSelectedCount >= 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Header
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, MindfulBrown10, CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Back",
                        tint = MindfulBrown,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = "Mission 1: The Airport",
                    fontFamily = ClashGroteskFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MindfulBrown
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Essential Packing",
                fontFamily = ClashGroteskFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MindfulBrown
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Select 3 items needed for your flight.",
                fontSize = 15.sp,
                color = MindfulBrown80
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selected Word Card Preview
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = lastSelected?.targetName ?: "Tap an item below",
                        fontFamily = ClashGroteskFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MindfulBrown
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = lastSelected?.nativeTranslation ?: "Discover essential travel items",
                        fontSize = 14.sp,
                        color = MindfulBrown60
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MindfulBrown)
                        .clickable {
                            if (lastSelected != null) {
                                onSpeakerClick(lastSelected)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.speaker),
                        contentDescription = "Listen",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2-Column Grid of Objects
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(items) { item ->
                val isSelected = selectedIds.contains(item.id)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .shadow(if (isSelected) 8.dp else 2.dp, RoundedCornerShape(28.dp))
                        .clip(RoundedCornerShape(28.dp))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) MindfulBrown else MindfulBrown10,
                            shape = RoundedCornerShape(28.dp)
                        )
                        .clickable { onItemClick(item) },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFFFFF6EE) else Color.White
                    )
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Image(
                                painter = painterResource(id = item.getResolvedImageRes()),
                                contentDescription = item.targetName,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.targetName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MindfulBrown,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Selected checkmark badge
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(26.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-8).dp, y = 8.dp)
                                    .clip(CircleShape)
                                    .background(MindfulBrown),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.tick),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Ready to fly bottom CTA
        Button(
            onClick = onReadyToFly,
            enabled = isReady,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(6.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MindfulBrown,
                disabledContainerColor = MindfulBrown10,
                contentColor = Color.White,
                disabledContentColor = MindfulBrown40
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Ready to Fly",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.takeoff),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ─── STEP 3: LEARN PLACES ────────────────────────────────────────────────────

@Composable
private fun LearnPlacesStep(
    places: List<PlaceItem>,
    currentIndex: Int,
    languageCode: String,
    ttsManager: TextToSpeechManager,
    speechManager: SpeechRecognizerManager,
    onRequestAudioPermission: (() -> Unit) -> Unit,
    onNextPlace: () -> Unit,
    onBackClick: () -> Unit,
) {
    val place = places.getOrNull(currentIndex) ?: PlaceItem()
    var practiceState by remember(currentIndex) { mutableStateOf(SpeechPracticeState.IDLE) }
    var spokenTranscript by remember(currentIndex) { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Top Header with Progress Bar
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, MindfulBrown10, CircleShape)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Close",
                        tint = MindfulBrown,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${currentIndex + 1} of ${places.size} places learned",
                        fontFamily = ClashGroteskFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MindfulBrown
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / places.size.coerceAtLeast(1) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = MindfulBrown,
                        trackColor = MindfulBrown10
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Learn Local Places",
                fontFamily = ClashGroteskFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MindfulBrown,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Listen, practice, and explore key travel locations.",
                fontSize = 15.sp,
                color = MindfulBrown80,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Place Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Illustration Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFFFF6EE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = place.iconRes),
                        contentDescription = place.targetName ?: "",
                        tint = MindfulBrown,
                        modifier = Modifier.size(64.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = place.targetName ?: "",
                    fontFamily = ClashGroteskFontFamily,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MindfulBrown,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = place.nativeTranslation ?: "",
                    fontSize = 17.sp,
                    color = MindfulBrown60,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Speaker and Mic buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(MindfulBrown)
                            .clickable {
                                ttsManager.speak(place.targetName ?: "", languageCode)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.speaker),
                            contentDescription = "Speak Place",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(58.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(
                                if (practiceState == SpeechPracticeState.CORRECT) Color(0xFF2A9D8F)
                                else if (practiceState == SpeechPracticeState.LISTENING) Color(0xFFE76F51)
                                else Color(0xFFFFEAD9)
                            )
                            .clickable {
                                if (practiceState == SpeechPracticeState.IDLE || practiceState == SpeechPracticeState.INCORRECT) {
                                    onRequestAudioPermission {
                                        practiceState = SpeechPracticeState.LISTENING
                                        spokenTranscript = ""
                                        speechManager.startListening(
                                            languageCode = languageCode,
                                            onPartialResult = { partial -> spokenTranscript = partial },
                                            onFinalResult = { finalResult ->
                                                spokenTranscript = finalResult
                                                practiceState = SpeechPracticeState.PROCESSING
                                                val isMatch = validateBeginnerSpeech(finalResult, place.targetName ?: "")
                                                practiceState = if (isMatch) SpeechPracticeState.CORRECT else SpeechPracticeState.INCORRECT
                                            },
                                            onError = {
                                                practiceState = SpeechPracticeState.INCORRECT
                                            }
                                        )
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.mic),
                            contentDescription = "Practice Mic",
                            tint = if (practiceState == SpeechPracticeState.CORRECT || practiceState == SpeechPracticeState.LISTENING) Color.White else MindfulBrown,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Listening Feedback Status
                val statusText = when (practiceState) {
                    SpeechPracticeState.IDLE -> "Tap mic to practice pronunciation"
                    SpeechPracticeState.LISTENING -> "Listening ... $spokenTranscript"
                    SpeechPracticeState.PROCESSING -> "Evaluating pronunciation ..."
                    SpeechPracticeState.CORRECT -> "Great pronunciation! 🎉"
                    SpeechPracticeState.INCORRECT -> "Try again: ${place.targetName ?: ""}"
                }
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (practiceState == SpeechPracticeState.CORRECT) Color(0xFF2A9D8F) else MindfulBrown80,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Continue Learning CTA (Disabled until pronunciation is CORRECT)
        val isPronunciationCorrect = practiceState == SpeechPracticeState.CORRECT
        Button(
            onClick = {
                practiceState = SpeechPracticeState.IDLE
                onNextPlace()
            },
            enabled = isPronunciationCorrect,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(6.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MindfulBrown,
                disabledContainerColor = MindfulBrown10,
                contentColor = Color.White,
                disabledContentColor = MindfulBrown40
            )
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Continue Learning",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = R.drawable.arrow_right),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ─── STEP 4: AIRPORT ANNOUNCEMENT LISTENING ──────────────────────────────────

@Composable
private fun AirportAnnouncementStep(
    question: ListeningQuestion,
    languageCode: String,
    selectedOption: String?,
    ttsManager: TextToSpeechManager,
    onSelectOption: (String) -> Unit,
    onContinue: () -> Unit,
    onBackClick: () -> Unit,
) {
    val isCorrect = selectedOption != null && selectedOption == question.correctAnswer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, MindfulBrown10, CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    tint = MindfulBrown,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Airport Announcement",
                fontFamily = ClashGroteskFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MindfulBrown
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Announcement Banner Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📢 Listen Carefully",
                    fontFamily = ClashGroteskFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MindfulBrown
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your destination country is being announced over the airport PA.",
                    fontSize = 14.sp,
                    color = MindfulBrown60,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        ttsManager.speak(question.audioText ?: "", languageCode)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.speaker),
                            contentDescription = "Play Audio",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Play Announcement",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = question.prompt ?: "Listen carefully. Which destination country was announced?",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MindfulBrown,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 4 Destination Country Option Cards
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            (question.options ?: emptyList()).forEach { opt ->
                val isSelected = selectedOption == opt
                val isThisOptionCorrect = opt == question.correctAnswer

                val cardBg = if (isSelected) {
                    if (isThisOptionCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                } else Color.White

                val borderColor = if (isSelected) {
                    if (isThisOptionCorrect) Color(0xFF2A9D8F) else Color(0xFFE76F51)
                } else MindfulBrown10

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (isSelected) 6.dp else 1.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { onSelectOption(opt ?: "") },
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = opt ?: "",
                            fontFamily = ClashGroteskFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MindfulBrown
                        )
                        if (isSelected) {
                            Icon(
                                painter = painterResource(id = if (isThisOptionCorrect) R.drawable.tick else R.drawable.alert),
                                contentDescription = null,
                                tint = if (isThisOptionCorrect) Color(0xFF2A9D8F) else Color(0xFFE76F51),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedOption != null) {
            Text(
                text = if (isCorrect) "Correct! Destination country identified! 🎉" else "Incorrect country. Listen again and select the correct option.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect) Color(0xFF2A9D8F) else Color(0xFFE76F51),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onContinue,
            enabled = isCorrect,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(6.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MindfulBrown,
                disabledContainerColor = MindfulBrown10,
                contentColor = Color.White,
                disabledContentColor = MindfulBrown40
            )
        ) {
            Text(
                text = "Continue",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── STEP 5: FIND COFFEE SHOP ────────────────────────────────────────────────

@Composable
private fun FindSeatStep(
    seatChallenge: SeatChallenge,
    selectedOption: String?,
    onSelectOption: (String) -> Unit,
    onContinue: () -> Unit,
    onBackClick: () -> Unit,
) {
    val isCorrect = selectedOption != null && selectedOption == seatChallenge.correctAnswer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, MindfulBrown10, CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    tint = MindfulBrown,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Find Coffee Shop",
                fontFamily = ClashGroteskFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MindfulBrown
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Coffee Shop Map Graphic
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AIRPORT CAFÉS & SHOPS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MindfulBrown60,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = seatChallenge.boardingPassSeat ?: "Café",
                    fontFamily = ClashGroteskFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OrangeAccent
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Vocabulary tags
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    (seatChallenge.vocabulary ?: emptyList()).forEach { (target, native) ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = target ?: "",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MindfulBrown
                            )
                            Text(
                                text = native ?: "",
                                fontSize = 11.sp,
                                color = MindfulBrown60
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = seatChallenge.prompt ?: "You want to grab a coffee. Select the authentic Coffee Shop / Café.",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MindfulBrown,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Coffee Shop Choice Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            (seatChallenge.options ?: emptyList()).forEach { opt ->
                val isSelected = selectedOption == opt
                val isThisOptionCorrect = opt == seatChallenge.correctAnswer

                val cardBg = if (isSelected) {
                    if (isThisOptionCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                } else Color.White

                val borderColor = if (isSelected) {
                    if (isThisOptionCorrect) Color(0xFF2A9D8F) else Color(0xFFE76F51)
                } else MindfulBrown10

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(if (isSelected) 6.dp else 1.dp, RoundedCornerShape(18.dp))
                        .clip(RoundedCornerShape(18.dp))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { onSelectOption(opt ?: "") },
                    colors = CardDefaults.cardColors(containerColor = cardBg)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = opt ?: "",
                            fontFamily = ClashGroteskFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MindfulBrown
                        )
                        if (isSelected) {
                            Icon(
                                painter = painterResource(id = if (isThisOptionCorrect) R.drawable.tick else R.drawable.alert),
                                contentDescription = null,
                                tint = if (isThisOptionCorrect) Color(0xFF2A9D8F) else Color(0xFFE76F51),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedOption != null) {
            Text(
                text = if (isCorrect) "Great job! You found the coffee shop! ☕" else "That's not a coffee shop! Try again.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCorrect) Color(0xFF2A9D8F) else Color(0xFFE76F51),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onContinue,
            enabled = isCorrect,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(6.dp, RoundedCornerShape(18.dp)),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MindfulBrown,
                disabledContainerColor = MindfulBrown10,
                contentColor = Color.White,
                disabledContentColor = MindfulBrown40
            )
        ) {
            Text(
                text = "Continue",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ─── STEP 6: SPEAKING CHALLENGE ─────────────────────────────────────────────

@Composable
private fun CoffeeSpeakingStep(
    challenge: SpeakingChallenge,
    practiceState: SpeechPracticeState,
    spokenTranscript: String,
    languageCode: String,
    ttsManager: TextToSpeechManager,
    speechManager: SpeechRecognizerManager,
    onMicClick: () -> Unit,
    onSkipSpeaking: () -> Unit,
    onBackClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, MindfulBrown10, CircleShape)
                    .clickable { onBackClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    tint = MindfulBrown,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = "Speaking Challenge",
                fontFamily = ClashGroteskFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MindfulBrown
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Coffee Phrase Card (Matching Image 4)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFFFF6EE)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = challenge.imageRes),
                        contentDescription = "Coffee",
                        modifier = Modifier.size(140.dp),
                        contentScale = ContentScale.Fit
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .size(42.dp)
                            .shadow(4.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { ttsManager.speak(challenge.phrase, languageCode) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.speaker),
                            contentDescription = "Play Audio",
                            tint = MindfulBrown,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "TARGET PHRASE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MindfulBrown60,
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = challenge.phrase ?: "",
                    fontFamily = ClashGroteskFontFamily,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MindfulBrown,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = challenge.translation ?: "",
                    fontSize = 16.sp,
                    color = MindfulBrown80,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Listening Status Prompt Text
        val statusText = when (practiceState) {
            SpeechPracticeState.IDLE -> "Tap the microphone to speak"
            SpeechPracticeState.LISTENING -> "Listening ... $spokenTranscript"
            SpeechPracticeState.PROCESSING -> "Analyzing pronunciation ..."
            SpeechPracticeState.CORRECT -> "Nice! You ordered your coffee! 🎉"
            SpeechPracticeState.INCORRECT -> "Almost! Tap to try again."
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = statusText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (practiceState == SpeechPracticeState.CORRECT) Color(0xFF2A9D8F) else MindfulBrown80,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = challenge.phoneticHint,
                fontSize = 13.sp,
                color = MindfulBrown60,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Microphone Action Button
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(if (practiceState == SpeechPracticeState.CORRECT) Color(0xFF2A9D8F) else MindfulBrown)
                .clickable { onMicClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = if (practiceState == SpeechPracticeState.CORRECT) R.drawable.tick else R.drawable.mic),
                contentDescription = "Mic",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Can't speak right now",
            fontSize = 14.sp,
            color = MindfulBrown60,
            modifier = Modifier.clickable { onSkipSpeaking() }
        )
    }
}

// ─── STEP 7: MISSION COMPLETE ────────────────────────────────────────────────

@Composable
private fun MissionCompleteStep(
    summary: MissionSummary,
    onReturnHome: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .shadow(12.dp, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Big Checkmark Circle
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF6EE))
                        .border(2.dp, MindfulBrown, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.tick),
                        contentDescription = null,
                        tint = MindfulBrown,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = summary.title ?: "",
                    fontFamily = ClashGroteskFontFamily,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MindfulBrown,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = summary.subtitle ?: "",
                    fontSize = 15.sp,
                    color = MindfulBrown80,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats Pills Row (12 WORDS, 3 PHRASES)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF6EE))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${summary.wordsCount}",
                                fontFamily = ClashGroteskFontFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MindfulBrown
                            )
                            Text(
                                text = "WORDS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MindfulBrown60,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF6EE))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "${summary.phrasesCount}",
                                fontFamily = ClashGroteskFontFamily,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MindfulBrown
                            )
                            Text(
                                text = "PHRASES",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MindfulBrown60,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Cultural Tip Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF6EE))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.idea),
                            contentDescription = "Tip",
                            tint = MindfulBrown,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Tip",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MindfulBrown
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = summary.tip ?: "",
                                fontSize = 13.sp,
                                color = MindfulBrown80,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Return Home CTA Button
                Button(
                    onClick = onReturnHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .shadow(6.dp, RoundedCornerShape(18.dp)),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MindfulBrown)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Return Home",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            painter = painterResource(id = R.drawable.takeoff),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
