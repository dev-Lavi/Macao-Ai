package ai.macao.app.home.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

interface SpeechRecognizerManager {
    fun startListening(
        languageCode: String,
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onError: (String) -> Unit
    )
    fun stopListening()
    fun destroy()
}

class SpeechRecognizerManagerImpl(private val context: Context) : SpeechRecognizerManager {

    companion object {
        private const val TAG = "SpeechRecognizerMgr"
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListeningActive = false
    private var activeLanguageCode = "ja-JP"
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentOnPartial: ((String) -> Unit)? = null
    private var currentOnFinal: ((String) -> Unit)? = null
    private var currentOnError: ((String) -> Unit)? = null

    private fun initializeRecognizer() {
        if (speechRecognizer != null) return

        val isOnDeviceAvailable = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
        } else false

        Log.i(TAG, "Initializing SpeechRecognizer. On-Device ASR Available: $isOnDeviceAvailable")

        speechRecognizer = if (isOnDeviceAvailable && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } else {
            SpeechRecognizer.createSpeechRecognizer(context)
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.i(TAG, "ASR: Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.i(TAG, "ASR: Beginning of speech")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.i(TAG, "ASR: End of speech")
            }

            override fun onError(error: Int) {
                val errorMsg = getErrorText(error)
                Log.e(TAG, "ASR Error: $errorMsg ($error)")

                if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                    Log.w(TAG, "ASR Engine is busy. Recreating SpeechRecognizer...")
                    recreateRecognizer()
                    if (isListeningActive) {
                        mainHandler.postDelayed({
                            if (isListeningActive) startRecognitionSession()
                        }, 1000)
                    }
                } else {
                    currentOnError?.invoke(errorMsg)
                    isListeningActive = false
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val finalText = matches?.getOrNull(0) ?: ""
                Log.i(TAG, "ASR Final Result: $finalText")
                currentOnFinal?.invoke(finalText)
                isListeningActive = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val unstable = partialResults?.getStringArrayList("android.speech.extra.UNSTABLE_TEXT")
                
                val stableText = matches?.getOrNull(0) ?: ""
                val unstableText = unstable?.getOrNull(0) ?: ""

                val combinedText = when {
                    stableText.isNotEmpty() && unstableText.isNotEmpty() -> "$stableText $unstableText"
                    stableText.isNotEmpty() -> stableText
                    else -> unstableText
                }

                if (combinedText.isNotEmpty()) {
                    Log.v(TAG, "ASR Partial Result: $combinedText")
                    currentOnPartial?.invoke(combinedText)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun recreateRecognizer() {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up SpeechRecognizer: ${e.message}")
        }
        speechRecognizer = null
        initializeRecognizer()
    }

    private fun startRecognitionSession() {
        try {
            initializeRecognizer()

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, activeLanguageCode)
                putExtra("android.speech.extra.DICTATION_MODE", true)
                
                // Set silence timeouts
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
            }

            Log.i(TAG, "Starting recognition session with locale: $activeLanguageCode")
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start recognition session: ${e.message}")
            currentOnError?.invoke("Failed to start listening: ${e.message}")
            isListeningActive = false
        }
    }

    override fun startListening(
        languageCode: String,
        onPartialResult: (String) -> Unit,
        onFinalResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        currentOnPartial = onPartialResult
        currentOnFinal = onFinalResult
        currentOnError = onError
        isListeningActive = true

        // Form BCP-47 locale tag appropriately (e.g. ja -> ja-JP, es -> es-ES)
        activeLanguageCode = when (languageCode.lowercase()) {
            "ja" -> "ja-JP"
            "es" -> "es-ES"
            "fr" -> "fr-FR"
            "de" -> "de-DE"
            "ko" -> "ko-KR"
            "zh" -> "zh-CN"
            else -> "en-US"
        }

        mainHandler.post {
            startRecognitionSession()
        }
    }

    override fun stopListening() {
        isListeningActive = false
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(TAG, "Error calling stopListening: ${e.message}")
            }
        }
    }

    override fun destroy() {
        isListeningActive = false
        currentOnPartial = null
        currentOnFinal = null
        currentOnError = null
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.e(TAG, "Error destroying SpeechRecognizer: ${e.message}")
            }
            speechRecognizer = null
        }
    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Audio permission denied"
            SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
            else -> "Unknown speech error ($errorCode)"
        }
    }
}
