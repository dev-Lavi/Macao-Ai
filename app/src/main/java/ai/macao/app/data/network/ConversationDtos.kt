package ai.macao.app.data.network

data class StartConversationRequest(
    val languageCode: String,
    val levelId: String,
    val lessonId: String,
    val nativeLanguage: String = "en"
)

data class StartConversationResponse(
    val sessionId: String,
    val languageCode: String,
    val levelId: String,
    val lessonId: String,
    val turnNumber: Int,
    val totalTurns: Int,
    val target: ConversationTarget
)

data class ConversationTarget(
    val id: String,
    val targetText: String,
    val romanization: String,
    val meaning: String
)

data class ConversationTurnRequest(
    val turnNumber: Int,
    val transcript: String
)

data class ConversationTurnResponse(
    val sessionId: String,
    val turnNumber: Int,
    val understood: Boolean,
    val score: Int,
    val shouldRetry: Boolean,
    val correction: ConversationMessageDetail?,
    val owlReply: ConversationMessageDetail?,
    val nextTurn: Boolean,
    val completed: Boolean,
    val nextTarget: ConversationTarget?
)

data class ConversationMessageDetail(
    val text: String,
    val romanization: String,
    val meaning: String
)

data class CompleteConversationRequest(
    val timeSpentSeconds: Int
)

data class CompleteConversationResponse(
    val sessionId: String,
    val averageScore: Int,
    val xpEarned: Int,
    val totalXp: Int,
    val streak: StreakResponse,
    val completedLessonsCount: Int,
    val levelUnlocked: Boolean,
    val unlockMessage: String
)
