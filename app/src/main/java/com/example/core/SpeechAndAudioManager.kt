package com.example.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class SpeechAndAudioManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var speechRecognizer: SpeechRecognizer? = null

    init {
        try {
            tts = TextToSpeech(context.applicationContext) { status ->
                isTtsReady = (status == TextToSpeech.SUCCESS)
            }
        } catch (e: Exception) {
            Log.w("SpeechAudio", "TTS init unavailable: ${e.message}")
        }
    }

    fun resolveLocale(languageName: String): Locale {
        return when (languageName.lowercase()) {
            "hindi" -> Locale.forLanguageTag("hi-IN")
            "spanish" -> Locale.forLanguageTag("es-ES")
            "french" -> Locale.FRENCH
            "german" -> Locale.GERMAN
            "japanese" -> Locale.JAPANESE
            "korean" -> Locale.KOREAN
            "chinese" -> Locale.SIMPLIFIED_CHINESE
            "italian" -> Locale.ITALIAN
            "portuguese" -> Locale.forLanguageTag("pt-BR")
            "russian" -> Locale.forLanguageTag("ru-RU")
            "arabic" -> Locale.forLanguageTag("ar-SA")
            else -> Locale.US
        }
    }

    fun speak(text: String, languageName: String = "English", slowSpeed: Boolean = false) {
        val engine = tts ?: return
        if (!isTtsReady) return
        try {
            engine.language = resolveLocale(languageName)
            engine.setSpeechRate(if (slowSpeed) 0.55f else 1.0f)
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "sachinchat_tts_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.w("SpeechAudio", "TTS speak error: ${e.message}")
        }
    }

    fun startSpeechRecognition(
        languageName: String,
        onListeningChanged: (Boolean) -> Unit,
        onPartialOrFinalResult: (String) -> Unit,
        onErrorMessage: (String) -> Unit
    ) {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onListeningChanged(false)
            onErrorMessage("Live microphone recognition service is unavailable on this device. Use phonetic practice below!")
            return
        }

        try {
            speechRecognizer?.destroy()
            val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer = recognizer

            recognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    onListeningChanged(true)
                }

                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    onListeningChanged(false)
                }

                override fun onError(error: Int) {
                    onListeningChanged(false)
                    val msg = when (error) {
                        SpeechRecognizer.ERROR_NO_MATCH -> "Didn't catch that clearly. Tap the mic to try again!"
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech heard. Speak right after tapping the mic."
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required for speech recognition."
                        else -> "Speech recognition paused (code $error). Try again or use text practice."
                    }
                    onErrorMessage(msg)
                }

                override fun onResults(results: Bundle?) {
                    onListeningChanged(false)
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val spoken = matches?.firstOrNull().orEmpty()
                    if (spoken.isNotBlank()) {
                        onPartialOrFinalResult(spoken)
                    } else {
                        onErrorMessage("Could not recognize words. Please try speaking again.")
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
                    if (!partial.isNullOrBlank()) {
                        onPartialOrFinalResult(partial)
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            val localeTag = resolveLocale(languageName).toLanguageTag()
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }
            recognizer.startListening(intent)
        } catch (e: Exception) {
            onListeningChanged(false)
            onErrorMessage("Unable to start microphone: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    fun stopSpeechRecognition() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) {
        }
    }

    fun release() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {
        }
    }

    companion object {
        fun calculatePronunciationAccuracy(expected: String, spoken: String): Int {
            val cleanExpected = expected.lowercase().replace(Regex("[^\\p{L}\\p{N}\\s]"), "").trim()
            val cleanSpoken = spoken.lowercase().replace(Regex("[^\\p{L}\\p{N}\\s]"), "").trim()
            if (cleanExpected.isEmpty() || cleanSpoken.isEmpty()) return 0
            if (cleanExpected == cleanSpoken) return 100

            val expectedWords = cleanExpected.split(Regex("\\s+")).filter { it.isNotBlank() }
            val spokenWords = cleanSpoken.split(Regex("\\s+")).filter { it.isNotBlank() }
            if (expectedWords.isEmpty()) return 0

            val matched = expectedWords.count { word ->
                spokenWords.any { s -> s == word || levenshtein(s, word) <= 1 }
            }
            return ((matched.toFloat() / expectedWords.size.toFloat()) * 100f).toInt().coerceIn(10, 99)
        }

        private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
            val lhsLen = lhs.length
            val rhsLen = rhs.length
            var cost = IntArray(lhsLen + 1) { it }
            var newCost = IntArray(lhsLen + 1)
            for (i in 1..rhsLen) {
                newCost[0] = i
                for (j in 1..lhsLen) {
                    val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                    val costReplace = cost[j - 1] + match
                    val costInsert = cost[j] + 1
                    val costDelete = newCost[j - 1] + 1
                    newCost[j] = minOf(costInsert, costDelete, costReplace)
                }
                val swap = cost
                cost = newCost
                newCost = swap
            }
            return cost[lhsLen]
        }
    }
}
