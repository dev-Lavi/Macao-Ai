package ai.macao.app.onboarding.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// ─── Built-in language list ───────────────────────────────────────────────────

/**
 * A curated list of 50+ languages with emoji flag representations.
 *
 * This covers the vast majority of real-world users without requiring
 * a network call. Users can search for any of the 1,242 BCP-47 languages
 * by typing in the search bar (filtered against this list).
 *
 * To extend, simply append entries — the picker is data-driven.
 */
data class Language(val flag: String, val name: String)

val AllLanguages: List<Language> = listOf(
    Language("🇺🇸", "English"),
    Language("🇪🇸", "Spanish"),
    Language("🇫🇷", "French"),
    Language("🇩🇪", "German"),
    Language("🇧🇷", "Portuguese"),
    Language("🇮🇹", "Italian"),
    Language("🇯🇵", "Japanese"),
    Language("🇰🇷", "Korean"),
    Language("🇨🇳", "Chinese (Mandarin)"),
    Language("🇹🇼", "Chinese (Traditional)"),
    Language("🇷🇺", "Russian"),
    Language("🇸🇦", "Arabic"),
    Language("🇮🇳", "Hindi"),
    Language("🇮🇳", "Bengali"),
    Language("🇮🇳", "Tamil"),
    Language("🇮🇳", "Telugu"),
    Language("🇮🇳", "Marathi"),
    Language("🇮🇳", "Gujarati"),
    Language("🇮🇳", "Kannada"),
    Language("🇮🇳", "Malayalam"),
    Language("🇵🇰", "Urdu"),
    Language("🇳🇱", "Dutch"),
    Language("🇵🇱", "Polish"),
    Language("🇺🇦", "Ukrainian"),
    Language("🇸🇪", "Swedish"),
    Language("🇳🇴", "Norwegian"),
    Language("🇩🇰", "Danish"),
    Language("🇫🇮", "Finnish"),
    Language("🇬🇷", "Greek"),
    Language("🇨🇿", "Czech"),
    Language("🇸🇰", "Slovak"),
    Language("🇭🇺", "Hungarian"),
    Language("🇷🇴", "Romanian"),
    Language("🇧🇬", "Bulgarian"),
    Language("🇭🇷", "Croatian"),
    Language("🇷🇸", "Serbian"),
    Language("🇸🇮", "Slovenian"),
    Language("🇪🇪", "Estonian"),
    Language("🇱🇻", "Latvian"),
    Language("🇱🇹", "Lithuanian"),
    Language("🇮🇱", "Hebrew"),
    Language("🇹🇷", "Turkish"),
    Language("🇮🇩", "Indonesian"),
    Language("🇲🇾", "Malay"),
    Language("🇹🇭", "Thai"),
    Language("🇻🇳", "Vietnamese"),
    Language("🇵🇭", "Filipino"),
    Language("🇦🇿", "Azerbaijani"),
    Language("🇬🇪", "Georgian"),
    Language("🇦🇲", "Armenian"),
    Language("🇰🇿", "Kazakh"),
    Language("🇺🇿", "Uzbek"),
    Language("🇲🇳", "Mongolian"),
    Language("🇿🇦", "Zulu"),
    Language("🇳🇬", "Yoruba"),
    Language("🇳🇬", "Hausa"),
    Language("🇰🇪", "Swahili"),
    Language("🇪🇹", "Amharic"),
    Language("🇮🇷", "Persian"),
    Language("🇦🇫", "Pashto"),
    Language("🇱🇰", "Sinhala"),
    Language("🇳🇵", "Nepali"),
    Language("🏴󠁧󠁢󠁷󠁬󠁳󠁿", "Welsh"),
    Language("🇮🇪", "Irish"),
    Language("🏴󠁧󠁢󠁳󠁣󠁴󠁿", "Scottish Gaelic"),
    Language("🇲🇹", "Maltese"),
    Language("🇦🇱", "Albanian"),
    Language("🇲🇰", "Macedonian"),
    Language("🇧🇦", "Bosnian"),
    Language("🇲🇽", "Spanish (Mexico)"),
    Language("🇦🇷", "Spanish (Argentina)"),
    Language("🇨🇦", "French (Canada)"),
    Language("🇨🇭", "German (Swiss)"),
    Language("🇦🇹", "German (Austria)"),
    Language("🇵🇹", "Portuguese (Portugal)"),
    Language("🇪🇬", "Arabic (Egypt)"),
    Language("🇲🇦", "Arabic (Morocco)"),
    Language("🌐", "Sign Language"),
    Language("🌐", "Other"),
)

// ─── Screen ───────────────────────────────────────────────────────────────────

/**
 * Searchable language picker used for Steps 1 (native) and 2 (learning).
 *
 * - Shows all languages by default.
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
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredLanguages = remember(searchQuery) {
        if (searchQuery.isBlank()) AllLanguages
        else AllLanguages.filter {
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
