package ai.macao.app.home.data

/**
 * State of a stage island on the learning map.
 */
enum class StageState {
    COMPLETED,
    CURRENT,
    LOCKED,
}

/**
 * Decorative items that float or sit on stage platforms.
 */
enum class StageDecoration {
    NONE,
    LIGHTNING,
    COIN,
    CHEST,
    STAR,
}

/**
 * Lesson content associated with a stage.
 */
data class LessonData(
    val levelTitle: String,
    val japaneseText: String,
    val romajiText: String,
    val translationText: String,
    val promptText: String = "Tap the microphone to practice",
)

/**
 * Stage node representation along the winding path.
 */
data class StageModel(
    val id: Int,
    val levelNumber: Int,
    val title: String,
    val subtitle: String,
    val state: StageState,
    val decoration: StageDecoration = StageDecoration.NONE,
    val hasPlayerAvatar: Boolean = false,
    val xOffsetDp: Int = 0, // Horizontal offset in DP for S-curve path
    val lessonData: LessonData,
)

/**
 * Speech practice lesson states for Screen 2.
 */
enum class SpeechPracticeState {
    IDLE,
    LISTENING,
    PROCESSING,
    CORRECT,
    INCORRECT,
}

/**
 * Repository of sample stages for the learning map.
 */
object SampleStageData {
    val sampleStages = listOf(
        StageModel(
            id = 1,
            levelNumber = 1,
            title = "Greetings",
            subtitle = "Konnichiwa & Essentials",
            state = StageState.CURRENT,
            decoration = StageDecoration.LIGHTNING,
            hasPlayerAvatar = true,
            xOffsetDp = -75,
            lessonData = LessonData(
                levelTitle = "Level 1 - Intro",
                japaneseText = "こんにちは",
                romajiText = "ko - ni - chi - wa",
                translationText = "hello",
                promptText = "Tap the microphone to speak",
            ),
        ),
        StageModel(
            id = 2,
            levelNumber = 2,
            title = "Polite Thanks",
            subtitle = "Arigatou Gozaimasu",
            state = StageState.LOCKED,
            decoration = StageDecoration.COIN,
            hasPlayerAvatar = false,
            xOffsetDp = 65,
            lessonData = LessonData(
                levelTitle = "Level 2 - Expressions",
                japaneseText = "ありがとうございます",
                romajiText = "a - ri - ga - to - u",
                translationText = "thank you very much",
            ),
        ),
        StageModel(
            id = 3,
            levelNumber = 3,
            title = "Self Introduction",
            subtitle = "Hajimemashite",
            state = StageState.LOCKED,
            decoration = StageDecoration.STAR,
            hasPlayerAvatar = false,
            xOffsetDp = -55,
            lessonData = LessonData(
                levelTitle = "Level 3 - Meeting People",
                japaneseText = "はじめまして",
                romajiText = "ha - ji - me - ma - shi - te",
                translationText = "nice to meet you",
            ),
        ),
        StageModel(
            id = 4,
            levelNumber = 4,
            title = "Daily Routine",
            subtitle = "Good Morning & Night",
            state = StageState.LOCKED,
            decoration = StageDecoration.LIGHTNING,
            hasPlayerAvatar = false,
            xOffsetDp = 70,
            lessonData = LessonData(
                levelTitle = "Level 4 - Daily Life",
                japaneseText = "おはようございます",
                romajiText = "o - ha - yo - u",
                translationText = "good morning",
            ),
        ),
        StageModel(
            id = 5,
            levelNumber = 5,
            title = "Bonus Treasure",
            subtitle = "Review & Challenge",
            state = StageState.LOCKED,
            decoration = StageDecoration.CHEST,
            hasPlayerAvatar = false,
            xOffsetDp = -65,
            lessonData = LessonData(
                levelTitle = "Level 5 - Treasure Vault",
                japaneseText = "すみません",
                romajiText = "su - mi - ma - se - n",
                translationText = "excuse me / sorry",
            ),
        ),
        StageModel(
            id = 6,
            levelNumber = 6,
            title = "Food & Dining",
            subtitle = "Itadakimasu!",
            state = StageState.LOCKED,
            decoration = StageDecoration.COIN,
            hasPlayerAvatar = false,
            xOffsetDp = 50,
            lessonData = LessonData(
                levelTitle = "Level 6 - Dining Out",
                japaneseText = "いただきます",
                romajiText = "i - ta - da - ki - ma - su",
                translationText = "let's eat!",
            ),
        ),
        StageModel(
            id = 7,
            levelNumber = 7,
            title = "Mastery Challenge",
            subtitle = "Level 1 Graduation",
            state = StageState.LOCKED,
            decoration = StageDecoration.STAR,
            hasPlayerAvatar = false,
            xOffsetDp = -40,
            lessonData = LessonData(
                levelTitle = "Level 7 - Mastery",
                japaneseText = "さようなら",
                romajiText = "sa - yo - u - na - ra",
                translationText = "goodbye",
            ),
        ),
    )
}
