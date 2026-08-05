package ai.macao.app.onboarding.ui

import ai.macao.app.onboarding.viewmodel.OnboardingViewModel
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Root composable for the full onboarding flow.
 *
 * Steps 1–5 : Profile setup questions
 * Step 6    : Compiling Data screen (auto-advances after ~2.8 s)
 * Step 7    : All Set screen (user taps CTA to finish)
 *
 * A [Snackbar] surfaces Firestore write errors (step 5 → 6 transition).
 * Navigation to MainApp happens only when the user taps "Let's Start Learning"
 * on step 7 — [isComplete] is set by [OnboardingViewModel.confirmComplete].
 */
@Composable
fun ProfileSetupFlow(
    onSetupComplete: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: OnboardingViewModel = viewModel(factory = OnboardingViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // ── Navigate away once user taps the CTA on All Set screen ───────────────
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) onSetupComplete()
    }

    // ── Firestore error → Snackbar ────────────────────────────────────────────
    LaunchedEffect(uiState.error) {
        val err = uiState.error
        if (err != null) {
            snackbarHostState.showSnackbar(
                message     = err,
                actionLabel = "Retry",
                duration    = SnackbarDuration.Long,
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier       = modifier,
        containerColor = BgCream,
        snackbarHost   = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            AnimatedContent(
                targetState   = uiState.currentStep,
                transitionSpec = {
                    val forward = targetState > initialState
                    slideInHorizontally(
                        animationSpec  = tween(350),
                        initialOffsetX = { if (forward) it else -it },
                    ) togetherWith slideOutHorizontally(
                        animationSpec  = tween(350),
                        targetOffsetX  = { if (forward) -it else it },
                    )
                },
                label    = "onboarding_step",
                modifier = Modifier.fillMaxSize(),
            ) { step ->
                when (step) {

                    // ── Step 1: Native language ───────────────────────────────
                    1 -> LanguagePickerScreen(
                        currentStep        = 1,
                        question           = "What is your native language?",
                        selectedLanguage   = uiState.nativeLanguage,
                        onLanguageSelected = viewModel::selectNativeLanguage,
                        onBack             = { /* Step 1 — no previous step */ },
                    )

                    // ── Step 2: Learning language ─────────────────────────────
                    2 -> LanguagePickerScreen(
                        currentStep        = 2,
                        question           = "What language would you like to learn?",
                        selectedLanguage   = uiState.learningLanguage,
                        onLanguageSelected = viewModel::selectLearningLanguage,
                        onBack             = viewModel::goBack,
                    )

                    // ── Step 3: Proficiency ───────────────────────────────────
                    3 -> ProficiencyScreen(
                        currentStep      = 3,
                        learningLanguage = uiState.learningLanguage.ifBlank { "the language" },
                        selectedLevel    = uiState.proficiencyLevel,
                        onLevelSelected  = viewModel::selectProficiencyLevel,
                        onBack           = viewModel::goBack,
                    )

                    // ── Step 4: Learning reason ───────────────────────────────
                    4 -> LearningReasonScreen(
                        currentStep      = 4,
                        learningLanguage = uiState.learningLanguage.ifBlank { "the language" },
                        selectedReason   = uiState.learningReason,
                        onReasonSelected = viewModel::selectLearningReason,
                        onBack           = viewModel::goBack,
                    )

                    // ── Step 5: Age group (triggers Firestore write) ──────────
                    5 -> AgeGroupScreen(
                        currentStep        = 5,
                        selectedAgeGroup   = uiState.ageGroup,
                        onAgeGroupSelected = viewModel::selectAgeGroup,
                        onBack             = viewModel::goBack,
                    )

                    // ── Step 6: Compiling data (post-save animation) ──────────
                    6 -> CompilingDataScreen(
                        onAnimationComplete = viewModel::advanceToAllSet,
                    )

                    // ── Step 7: All Set (user taps CTA → isComplete = true) ───
                    7 -> AllSetScreen(
                        onGetStarted = viewModel::confirmComplete,
                    )

                    // Fallback
                    else -> Box(
                        modifier         = Modifier.fillMaxSize().background(BgCream),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text       = "Something went wrong.",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color      = TextMedium,
                        )
                    }
                }
            }

            // ── Loading overlay (only during Firestore write, before step 6) ──
            if (uiState.isLoading) {
                LoadingOverlay()
            }
        }
    }
}
