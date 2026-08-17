package ai.macao.app.home.data

enum class ConversationState {
    INITIALIZING,
    SHOWING_TARGET,
    LISTENING,
    PROCESSING,
    OWL_SPEAKING,
    FEEDBACK,
    RETRY,
    COMPLETED,
    ERROR
}
