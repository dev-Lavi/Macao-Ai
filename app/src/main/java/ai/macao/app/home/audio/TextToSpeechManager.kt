package ai.macao.app.home.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

interface TextToSpeechManager {
    fun speak(text: String, languageCode: String, slowMode: Boolean = false)
    fun stop()
    fun shutdown()
}

class TextToSpeechManagerImpl(private val context: Context) : TextToSpeechManager, TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "TextToSpeechManager"
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeech: Pair<String, String>? = null // text, langCode
    private var pendingSlowMode = false

    init {
        Log.i(TAG, "Initializing TextToSpeech engine...")
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            Log.i(TAG, "TTS Engine successfully initialized.")
            isInitialized = true
            
            // Speak any pending speech queued during initialization
            pendingSpeech?.let { (text, langCode) ->
                speak(text, langCode, pendingSlowMode)
                pendingSpeech = null
            }
        } else {
            Log.e(TAG, "TTS Initialization failed with status: $status")
        }
    }

    override fun speak(text: String, languageCode: String, slowMode: Boolean) {
        if (text.isBlank()) return

        if (!isInitialized) {
            Log.w(TAG, "TTS engine not initialized yet. Queueing speech: \"$text\"")
            pendingSpeech = Pair(text, languageCode)
            pendingSlowMode = slowMode
            return
        }

        val locale = when (languageCode.lowercase()) {
            "ja" -> Locale.JAPANESE
            "es" -> Locale("es", "ES")
            "fr" -> Locale.FRANCE
            "de" -> Locale.GERMANY
            "ko" -> Locale.KOREAN
            "zh" -> Locale.CHINESE
            else -> Locale.US
        }

        try {
            val availability = tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (availability == TextToSpeech.LANG_NOT_SUPPORTED || availability == TextToSpeech.LANG_MISSING_DATA) {
                Log.w(TAG, "Language $locale is not supported or missing voice data. Falling back to default.")
                tts?.language = Locale.getDefault()
            } else {
                tts?.language = locale
            }

            // Control speech rate
            val speechRate = if (slowMode) 0.75f else 1.0f
            tts?.setSpeechRate(speechRate)

            Log.i(TAG, "Speaking: \"$text\" [Language: $locale, SlowMode: $slowMode]")
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MACAOAI_TTS_UTTERANCE")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to speak text: ${e.message}")
        }
    }

    override fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error calling TTS stop: ${e.message}")
        }
    }

    override fun shutdown() {
        isInitialized = false
        pendingSpeech = null
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS: ${e.message}")
        }
        tts = null
    }
}
