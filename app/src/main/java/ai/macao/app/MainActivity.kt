package ai.macao.app

import ai.macao.app.home.*
import ai.macao.app.home.data.*
import ai.macao.app.home.viewmodel.*
import ai.macao.app.data.network.LevelResponse
import ai.macao.app.onboarding.OnboardingScreen
import ai.macao.app.onboarding.SplashScreen
import ai.macao.app.onboarding.data.model.UserProfile
import ai.macao.app.onboarding.data.repository.UserRepository
import ai.macao.app.onboarding.ui.ProfileSetupFlow
import ai.macao.app.theme.AppBackground
import ai.macao.app.theme.MacaoAITheme
import ai.macao.app.theme.OrangeAccent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import ai.macao.app.theme.MindfulBrown
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext

// ─── App screen enum ──────────────────────────────────────────────────────────

enum class AppScreen {
    Splash,
    Onboarding,   // intro slides + auth (sign in / sign up)
    ProfileSetup, // 5-step profile setup (new users only)
    MainApp,
}

// ─── MainActivity ──────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {

    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MacaoAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = Color.Transparent,
                ) {
                    val firebaseAuth          = remember { FirebaseAuth.getInstance() }
                    var currentUser           by remember { mutableStateOf(firebaseAuth.currentUser) }
                    var currentScreen         by remember { mutableStateOf(AppScreen.Splash) }
                    var isCheckingOnboarding  by remember { mutableStateOf(false) }
                    val coroutineScope        = rememberCoroutineScope()

                    // ── Auth state listener ─────────────────────────────────────
                    DisposableEffect(firebaseAuth) {
                        val listener = FirebaseAuth.AuthStateListener { auth ->
                            currentUser = auth.currentUser
                        }
                        firebaseAuth.addAuthStateListener(listener)
                        onDispose { firebaseAuth.removeAuthStateListener(listener) }
                    }

                    // ── Screen router ───────────────────────────────────────────
                    when {
                        isCheckingOnboarding -> OnboardingCheckScreen()

                        currentScreen == AppScreen.Splash -> {
                            SplashScreen(
                                onSplashComplete = {
                                    currentScreen = if (currentUser != null) {
                                        AppScreen.MainApp
                                    } else {
                                        AppScreen.Onboarding
                                    }
                                },
                            )
                        }

                        currentScreen == AppScreen.Onboarding -> {
                            OnboardingScreen(
                                onAuthSuccess = {
                                    isCheckingOnboarding = true
                                    coroutineScope.launch {
                                        val profile  = userRepository.getUserProfile()
                                        val completed = profile.getOrNull()?.hasCompletedOnboarding == true
                                        isCheckingOnboarding = false
                                        currentScreen = if (completed) {
                                            AppScreen.MainApp
                                        } else {
                                            userRepository.createUserIfNotExists()
                                            AppScreen.ProfileSetup
                                        }
                                    }
                                },
                            )
                        }

                        currentScreen == AppScreen.ProfileSetup -> {
                            ProfileSetupFlow(
                                onSetupComplete = {
                                    currentScreen = AppScreen.MainApp
                                },
                            )
                        }

                        currentScreen == AppScreen.MainApp -> {
                            MainAppShell(
                                userName  = currentUser?.displayName
                                    ?: currentUser?.email?.substringBefore('@')
                                    ?: "Learner",
                                userEmail = currentUser?.email ?: "",
                                userRepository = userRepository,
                                onSignOut = {
                                    firebaseAuth.signOut()
                                    currentScreen = AppScreen.Onboarding
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Main app shell with bottom-tab navigation ────────────────────────────────

@Composable
fun MainAppShell(
    userName : String,
    userEmail: String,
    userRepository: UserRepository,
    onSignOut: () -> Unit,
    learningViewModel: LearningViewModel = viewModel(factory = LearningViewModel.Factory)
) {
    var selectedTab by remember { mutableStateOf(HomeTab.Home) }
    var showSettings by remember { mutableStateOf(false) }

    // Fetch user profile to resolve active learning language code
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    LaunchedEffect(Unit) {
        val result = userRepository.getUserProfile()
        if (result.isSuccess) {
            userProfile = result.getOrNull()
        }
    }

    val langCode = remember(userProfile) {
        val langName = userProfile?.onboarding?.learningLanguage ?: "Japanese"
        when (langName.lowercase()) {
            "spanish" -> "es"
            "japanese" -> "ja"
            "english" -> "en"
            "french" -> "fr"
            "german" -> "de"
            "italian" -> "it"
            "portuguese" -> "pt"
            else -> "ja"
        }
    }

    LaunchedEffect(langCode) {
        learningViewModel.loadLevels(langCode)
    }

    val levelsState by learningViewModel.levelsState.collectAsState()
    val lessonState by learningViewModel.lessonState.collectAsState()

    // ── Route to Active Practice flow if started ─────────────────────────────
    when (val state = lessonState) {
        is LessonUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize().background(AppBackground),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangeAccent)
            }
        }
        is LessonUiState.Content -> {
            if (state.lesson.category == "Conversation Scenarios") {
                val context = LocalContext.current
                val conversationViewModel: ConversationViewModel = viewModel(
                    factory = ConversationViewModel.provideFactory(context)
                )

                LaunchedEffect(state.lesson.id) {
                    conversationViewModel.startConversation(
                        languageCode = state.lesson.languageCode,
                        levelId = state.lesson.levelId,
                        lessonId = state.lesson.id
                    )
                }

                ConversationScreen(
                    viewModel = conversationViewModel,
                    onCancelClick = {
                        learningViewModel.cancelLesson()
                        learningViewModel.loadLevels(langCode)
                    }
                )
            } else {
                val currentItem = state.lesson.items[state.currentIndex]
                LessonPracticeScreen(
                    lessonTitle = state.lesson.title,
                    currentItem = currentItem,
                    currentIndex = state.currentIndex,
                    totalItems = state.lesson.items.size,
                    practiceState = state.practiceState,
                    onMicClick = { learningViewModel.startSpeechPractice() },
                    onNextClick = { learningViewModel.advanceOrComplete() },
                    onCancelClick = { learningViewModel.cancelLesson() }
                )
            }
        }
        is LessonUiState.Completed -> {
            LessonCompleteScreen(
                xpEarned = state.xpEarned,
                accuracy = state.accuracy,
                isSaving = state.isSaving,
                saveError = state.error,
                onSaveAndContinue = {
                    learningViewModel.saveProgressAndUnlock(
                        onComplete = {
                            // Automatically resets and updates levels list
                        }
                    )
                }
            )
        }
        is LessonUiState.Error -> {
            Box(
                modifier = Modifier.fillMaxSize().background(AppBackground).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = painterResource(id = R.drawable.owl_shy),
                        contentDescription = "Mascot Error",
                        modifier = Modifier.size(140.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MindfulBrown,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { learningViewModel.cancelLesson() },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                    ) {
                        Text("Back to Map", color = Color.White)
                    }
                }
            }
        }
        is LessonUiState.Idle -> {
            // Render main tabs
            if (showSettings) {
                AccountSettingsScreen(
                    userRepository = userRepository,
                    onBack = { showSettings = false },
                    onSignOut = onSignOut,
                    selectedTab = selectedTab,
                    onTabSelected = { tab ->
                        selectedTab = tab
                        showSettings = false
                    }
                )
            } else {
                val stagesList = remember(levelsState) {
                    when (val levels = levelsState) {
                        is LevelsUiState.Success -> mapLevelsToStages(levels.levels)
                        else -> emptyList()
                    }
                }

                when (selectedTab) {
                    HomeTab.Home -> {
                        when (val levels = levelsState) {
                            is LevelsUiState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(AppBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = OrangeAccent)
                                }
                            }
                            is LevelsUiState.Error -> {
                                Box(
                                    modifier = Modifier.fillMaxSize().background(AppBackground).padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Image(
                                            painter = painterResource(id = R.drawable.owl_mad),
                                            contentDescription = "Error Mascot",
                                            modifier = Modifier.size(140.dp)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "Failed to load learning path:\n${levels.message}",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MindfulBrown,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(20.dp))
                                        Button(
                                            onClick = { learningViewModel.loadLevels(langCode) },
                                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                                        ) {
                                            Text("Retry", color = Color.White)
                                        }
                                    }
                                }
                            }
                            is LevelsUiState.Success -> {
                                if (stagesList.isEmpty()) {
                                    Box(
                                        modifier = Modifier.fillMaxSize().background(AppBackground).padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Image(
                                                painter = painterResource(id = R.drawable.owl_books),
                                                contentDescription = "Coming soon mascot",
                                                modifier = Modifier.size(140.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "Coming Soon!\nContent is being prepared for this language.",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MindfulBrown,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                } else {
                                    HomeScreen(
                                        userName      = userName,
                                        userRepository = userRepository,
                                        stages        = stagesList,
                                        learningLang  = userProfile?.onboarding?.learningLanguage ?: "Japanese",
                                        notifCount    = 5,
                                        selectedTab   = selectedTab,
                                        onTabSelected = { selectedTab = it },
                                        onStageClick  = { stage ->
                                            val encodedId = stage.lessonData.promptText
                                            val parts = encodedId.split("|")
                                            if (parts.size == 2) {
                                                learningViewModel.startLesson(parts[1], langCode, parts[0])
                                            }
                                        },
                                        onProfileClick = { showSettings = true },
                                    )
                                }
                            }
                        }
                    }

                    HomeTab.AiTalk -> {
                        val context = LocalContext.current
                        val conversationViewModel: ConversationViewModel = viewModel(
                            factory = ConversationViewModel.provideFactory(context)
                        )

                        val lessonId = when (langCode) {
                            "es" -> "es_intro_01"
                            "ja" -> "jp_n5_intro_01"
                            "de" -> "objects_de_01"
                            "fr" -> "objects_fr_01"
                            "en" -> "objects_en_01"
                            "it" -> "objects_it_01"
                            "pt" -> "objects_pt_01"
                            else -> "jp_n5_intro_01"
                        }

                        LaunchedEffect(langCode) {
                            conversationViewModel.startConversation(
                                languageCode = langCode,
                                levelId = "level_1",
                                lessonId = lessonId
                            )
                        }

                        ConversationScreen(
                            viewModel = conversationViewModel,
                            onCancelClick = {
                                selectedTab = HomeTab.Home
                                learningViewModel.loadLevels(langCode)
                            }
                        )
                    }

                    HomeTab.Profile -> AccountScreen(
                        data          = AccountData(displayName = userName),
                        selectedTab   = selectedTab,
                        onTabSelected = { selectedTab = it },
                    )

                    HomeTab.Notification,
                    HomeTab.Chart -> {
                        HomeScreen(
                            userName      = userName,
                            userRepository = userRepository,
                            stages        = stagesList,
                            learningLang  = userProfile?.onboarding?.learningLanguage ?: "Japanese",
                            selectedTab   = selectedTab,
                            onTabSelected = { selectedTab = it },
                            onStageClick  = { stage ->
                                val encodedId = stage.lessonData.promptText
                                val parts = encodedId.split("|")
                                if (parts.size == 2) {
                                    learningViewModel.startLesson(parts[1], langCode, parts[0])
                                }
                            },
                            onProfileClick = { showSettings = true },
                        )
                    }
                }
            }
        }
    }
}

// ─── Loading screen while checking onboarding status ─────────────────────────

@Composable
private fun OnboardingCheckScreen() {
    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            color       = OrangeAccent,
            modifier    = Modifier.size(48.dp),
            strokeWidth = 4.dp,
        )
    }
}

/**
 * Maps levels and completion metrics dynamically into the winding path stage list.
 */
private fun mapLevelsToStages(levels: List<LevelResponse>): List<StageModel> {
    val stages = mutableListOf<StageModel>()
    var stageId = 1
    
    val offsets = listOf(-75, 65, -55, 70, -65, 50, -40)
    
    levels.forEach { level ->
        level.units?.forEach { unit ->
            unit.lessons.forEach { lesson ->
                val offsetIndex = (stageId - 1) % offsets.size
                val offset = offsets[offsetIndex]
                
                val decoration = when (stageId % 5) {
                    1 -> StageDecoration.LIGHTNING
                    2 -> StageDecoration.COIN
                    3 -> StageDecoration.STAR
                    4 -> StageDecoration.CHEST
                    else -> StageDecoration.NONE
                }
                
                val isFirstUncompleted = !lesson.completed && stages.none { 
                    it.state == StageState.CURRENT && it.levelNumber == level.order 
                }
                
                val state = when {
                    !level.unlocked -> StageState.LOCKED
                    lesson.completed -> StageState.COMPLETED
                    isFirstUncompleted -> StageState.CURRENT
                    else -> StageState.LOCKED
                }

                stages.add(
                    StageModel(
                        id = stageId,
                        levelNumber = level.order,
                        title = lesson.title,
                        subtitle = lesson.description,
                        state = state,
                        decoration = decoration,
                        hasPlayerAvatar = state == StageState.CURRENT,
                        xOffsetDp = offset,
                        lessonData = LessonData(
                            levelTitle = level.title,
                            japaneseText = lesson.title,
                            romajiText = lesson.description,
                            translationText = "Lesson ${lesson.order}",
                            promptText = "${level.id}|${lesson.id}"
                        )
                    )
                )
                stageId++
            }
        }
    }

    if (stages.none { it.state == StageState.CURRENT }) {
        val firstUnlockedIndex = stages.indexOfFirst { it.state != StageState.LOCKED }
        if (firstUnlockedIndex != -1) {
            stages[firstUnlockedIndex] = stages[firstUnlockedIndex].copy(
                state = StageState.CURRENT,
                hasPlayerAvatar = true
            )
        }
    }
    
    return stages
}