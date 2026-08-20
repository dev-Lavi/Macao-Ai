package ai.macao.app.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ─── Supported language list ─────────────────────────────────────────────────

/**
 * Single source of truth for Supported Languages in MacaoAI.
 *
 * Contains only languages fully backed by lessons, AI services,
 * speech recognition, and TTS engines.
 */
data class Language(val code: String, val flag: String, val name: String)

val SupportedLanguages: List<Language> = listOf(
    Language("en", "🇺🇸", "English"),
    Language("es", "🇪🇸", "Spanish"),
    Language("fr", "🇫🇷", "French"),
    Language("de", "🇩🇪", "German"),
    Language("ja", "🇯🇵", "Japanese"),
    Language("it", "🇮🇹", "Italian"),
    Language("pt", "🇧🇷", "Portuguese"),
    Language("hi", "🇮🇳", "Hindi"),
)

// Legacy alias for backwards compatibility
val AllLanguages: List<Language> = SupportedLanguages

// ─── Screen ───────────────────────────────────────────────────────────────────

/**
 * Searchable language picker used for Steps 1 (native) and 2 (learning).
 *
 * - Shows supported languages by default.
 * - Filters in real-time as the user types in the search bar.
 * - Tapping a language calls [onLanguageSelected] and auto-advances.
 * - No "Next" button — selection immediately proceeds to the next step.
 */
@Composable
fun LanguagePickerScreen(
    currentStep        : Int,
    question           : String,
    selectedLanguage   : String = "",
    onLanguageSelected : (String) -> Unit,
    onBack             : () -> Unit,
    modifier           : Modifier = Modifier,
    availableLanguages : List<Language> = SupportedLanguages,
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredLanguages = remember(searchQuery, availableLanguages) {
        if (searchQuery.isBlank()) availableLanguages
        else availableLanguages.filter {
            it.name.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

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

        // ── Language list + search ────────────────────────────────────────
        LazyColumn(
            modifier            = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding      = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(filteredLanguages, key = { it.name }) { lang ->
                LanguageRow(
                    flag     = lang.flag,
                    name     = lang.name,
                    selected = lang.name == selectedLanguage,
                    onClick  = { onLanguageSelected(lang.name) },
                )
            }
        }

        // ── Search bar (pinned to bottom) ─────────────────────────────────
        LanguageSearchBar(
            query         = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier      = Modifier
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp, top = 8.dp),
        )
    }
}
