package ai.macao.app.onboarding.ui

import ai.macao.app.onboarding.data.model.AgeGroup
import ai.macao.app.onboarding.data.model.LearningReason
import ai.macao.app.onboarding.data.model.ProficiencyLevel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ─── Generic single-choice screen ────────────────────────────────────────────

/**
 * Reusable single-choice screen for Steps 3, 4, and 5.
 *
 * Displays the question card and a vertical list of [OptionPill]s.
 * Tapping any pill immediately calls [onOptionSelected] — no Next button.
 */
@Composable
fun SingleChoiceScreen(
    currentStep      : Int,
    question         : String,
    options          : List<String>,
    selectedOption   : String = "",
    onOptionSelected : (String) -> Unit,
    onBack           : () -> Unit,
    modifier         : Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BgCream)
            .systemBarsPadding(),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── Top bar ──────────────────────────────────────────────────────────
        ProfileSetupTopBar(
            currentStep = currentStep,
            onBack      = onBack,
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Question card ─────────────────────────────────────────────────
        QuestionCard(
            question = question,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Options ───────────────────────────────────────────────────────
        LazyColumn(
            modifier            = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(options) { option ->
                OptionPill(
                    text     = option,
                    selected = option == selectedOption,
                    onClick  = { onOptionSelected(option) },
                )
            }
        }
    }
}

// ─── Step 3: Proficiency ──────────────────────────────────────────────────────

@Composable
fun ProficiencyScreen(
    currentStep      : Int = 3,
    learningLanguage : String,
    selectedLevel    : String = "",
    onLevelSelected  : (String) -> Unit,
    onBack           : () -> Unit,
) {
    SingleChoiceScreen(
        currentStep      = currentStep,
        question         = "How much $learningLanguage do you know?",
        options          = ProficiencyLevel.entries.map { it.displayName },
        selectedOption   = selectedLevel,
        onOptionSelected = onLevelSelected,
        onBack           = onBack,
    )
}

// ─── Step 4: Learning reason ──────────────────────────────────────────────────

@Composable
fun LearningReasonScreen(
    currentStep      : Int = 4,
    learningLanguage : String,
    selectedReason   : String = "",
    onReasonSelected : (String) -> Unit,
    onBack           : () -> Unit,
) {
    SingleChoiceScreen(
        currentStep      = currentStep,
        question         = "Why do you want to learn $learningLanguage?",
        options          = LearningReason.entries.map { it.displayName },
        selectedOption   = selectedReason,
        onOptionSelected = onReasonSelected,
        onBack           = onBack,
    )
}

// ─── Step 5: Age group ────────────────────────────────────────────────────────

@Composable
fun AgeGroupScreen(
    currentStep          : Int = 5,
    selectedAgeGroup     : String = "",
    onAgeGroupSelected   : (String) -> Unit,
    onBack               : () -> Unit,
) {
    SingleChoiceScreen(
        currentStep      = currentStep,
        question         = "How old are you?",
        options          = AgeGroup.entries.map { it.displayName },
        selectedOption   = selectedAgeGroup,
        onOptionSelected = onAgeGroupSelected,
        onBack           = onBack,
    )
}
