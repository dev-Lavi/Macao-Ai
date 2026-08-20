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
    private var useOnDeviceMode = true
    private var isCurrentRecognizerOnDevice = false
    private val mainHandler = Handler(Looper.getMainLooper())

    private var currentOnPartial: ((String) -> Unit)? = null
    private var currentOnFinal: ((String) -> Unit)? = null
    private var currentOnError: ((String) -> Unit)? = null

    private fun initializeRecognizer(forceStandard: Boolean = false) {
        if (speechRecognizer != null) return

        val canTryOnDevice = !forceStandard && useOnDeviceMode &&
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU &&
                SpeechRecognizer.isOnDeviceRecognitionAvailable(context)

        Log.i(TAG, "Initializing SpeechRecognizer. On-Device Mode: $canTryOnDevice")

        isCurrentRecognizerOnDevice = canTryOnDevice
        speechRecognizer = if (canTryOnDevice && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
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

                // Fallback from on-device to standard recognition if requested language pack is missing or unsupported offline
                if (isCurrentRecognizerOnDevice && (error == 12 || error == 13 || error == 14 || error == SpeechRecognizer.ERROR_CLIENT)) {
                    Log.w(TAG, "On-device ASR failed for $activeLanguageCode (error $error). Falling back to standard SpeechRecognizer...")
                    useOnDeviceMode = false
                    recreateRecognizer(forceStandard = true)
                    if (isListeningActive) {
                        mainHandler.post {
                            startRecognitionSession()
                        }
                    }
                    return
                }

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

    private fun recreateRecognizer(forceStandard: Boolean = false) {
        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error cleaning up SpeechRecognizer: ${e.message}")
        }
        speechRecognizer = null
        initializeRecognizer(forceStandard = forceStandard)
    }

    private fun startRecognitionSession() {
        try {
            initializeRecognizer()

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, activeLanguageCode)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, activeLanguageCode)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, activeLanguageCode)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra("android.speech.extra.DICTATION_MODE", true)
                
                // Set generous silence timeouts to prevent premature speech recognition timeout
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 8000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 4000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L)
            }

            Log.i(TAG, "Starting recognition session with locale: $activeLanguageCode (OnDevice: $isCurrentRecognizerOnDevice)")
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

        val normalizedLang = languageCode.lowercase().replace("_", "-")
        val langPrefix = normalizedLang.split("-").firstOrNull() ?: normalizedLang

        activeLanguageCode = when {
            normalizedLang.startsWith("ja") || langPrefix == "ja" -> "ja-JP"
            normalizedLang.startsWith("es") || langPrefix == "es" -> "es-ES"
            normalizedLang.startsWith("fr") || langPrefix == "fr" -> "fr-FR"
            normalizedLang.startsWith("de") || langPrefix == "de" -> "de-DE"
            normalizedLang.startsWith("ko") || langPrefix == "ko" -> "ko-KR"
            normalizedLang.startsWith("zh") || langPrefix == "zh" -> "zh-CN"
            normalizedLang.startsWith("en") || langPrefix == "en" -> "en-US"
            normalizedLang.contains("-") -> languageCode
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
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Please try again."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service is busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap mic to retry."
            SpeechRecognizer.ERROR_TOO_MANY_REQUESTS -> "Too many requests"
            SpeechRecognizer.ERROR_SERVER_DISCONNECTED -> "Server disconnected"
            12 -> "Language not supported offline"
            13 -> "Language model not available offline"
            14 -> "Cannot check offline language support"
            else -> "Speech error ($errorCode)"
        }
    }
}
