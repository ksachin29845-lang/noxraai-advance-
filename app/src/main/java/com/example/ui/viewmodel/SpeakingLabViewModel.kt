package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "SpeakingLabVM"

data class WordPronunciationItem(
    val word: String,
    val status: String, // "native", "close", "needs_practice"
    val phoneticTip: String
)

data class PronunciationFeedbackReport(
    val heardTranscript: String,
    val overallScore: Int,
    val accuracyScore: Int,
    val fluencyScore: Int,
    val intonationScore: Int,
    val strengths: String,
    val actionableTip: String,
    val practiceSyllables: String,
    val wordBreakdown: List<WordPronunciationItem>
)

data class SpeakingAttemptRecord(
    val id: String,
    val timestampLabel: String,
    val language: String,
    val targetPhrase: String,
    val heardTranscript: String,
    val overallScore: Int,
    val fluencyScore: Int,
    val intonationScore: Int
)

sealed interface LiveCoachState {
    data object Idle : LiveCoachState
    data object Connecting : LiveCoachState
    data class Streaming(val text: String) : LiveCoachState
    data class Completed(val text: String) : LiveCoachState
    data class Error(val message: String) : LiveCoachState
}

sealed interface ScorecardState {
    data object Idle : ScorecardState
    data object Analyzing : ScorecardState
    data class Success(val report: PronunciationFeedbackReport) : ScorecardState
    data class Error(val message: String) : ScorecardState
}

class SpeakingLabViewModel : ViewModel() {

    private val pronunciationReportSchema = Schema.obj(
        properties = mapOf(
            "heardTranscript" to Schema.string(
                description = "Exact transcription of what the speaker said in the audio recording"
            ),
            "overallScore" to Schema.integer(
                description = "Overall pronunciation score from 0 to 100"
            ),
            "accuracyScore" to Schema.integer(
                description = "Phoneme and consonant/vowel articulation accuracy from 0 to 100"
            ),
            "fluencyScore" to Schema.integer(
                description = "Speaking pace, smoothness, and rhythm score from 0 to 100"
            ),
            "intonationScore" to Schema.integer(
                description = "Natural pitch contour, tone, and syllable stress score from 0 to 100"
            ),
            "strengths" to Schema.string(
                description = "Warm summary of words or sounds the learner pronounced clearly"
            ),
            "actionableTip" to Schema.string(
                description = "Specific mouth, tongue, breath, or syllable-stress tip to sound more natural"
            ),
            "practiceSyllables" to Schema.string(
                description = "Slow syllable-by-syllable phonetic guide for the most challenging word in the phrase"
            ),
            "wordBreakdown" to Schema.array(
                description = "Word-by-word pronunciation assessment for each word in the target phrase",
                items = Schema.obj(
                    properties = mapOf(
                        "word" to Schema.string(description = "Word from the target phrase"),
                        "status" to Schema.enumeration(
                            values = listOf("native", "close", "needs_practice"),
                            description = "Pronunciation quality rating for this word"
                        ),
                        "phoneticTip" to Schema.string(
                            description = "Short pronunciation guide or mouth-placement cue for this word"
                        )
                    )
                )
            )
        )
    )

    // Low-latency streaming voice coach using gemini-3.5-flash-lite for fast TTFT
    private val streamingCoachModel by lazy {
        Firebase.ai.generativeModel("gemini-3.5-flash-lite")
    }

    private val structuredScorecardModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = pronunciationReportSchema
            }
        )
    }

    private val _liveCoachState = MutableStateFlow<LiveCoachState>(LiveCoachState.Idle)
    val liveCoachState: StateFlow<LiveCoachState> = _liveCoachState.asStateFlow()

    private val _scorecardState = MutableStateFlow<ScorecardState>(ScorecardState.Idle)
    val scorecardState: StateFlow<ScorecardState> = _scorecardState.asStateFlow()

    private val _attemptHistory = MutableStateFlow<List<SpeakingAttemptRecord>>(emptyList())
    val attemptHistory: StateFlow<List<SpeakingAttemptRecord>> = _attemptHistory.asStateFlow()

    private var activeCoachJob: Job? = null
    private var activeScorecardJob: Job? = null

    fun resetFeedbackForNewPhrase() {
        activeCoachJob?.cancel()
        activeScorecardJob?.cancel()
        _liveCoachState.value = LiveCoachState.Idle
        _scorecardState.value = ScorecardState.Idle
    }

    fun analyzePronunciationAudio(
        audioBytes: ByteArray,
        mimeType: String,
        targetPhrase: String,
        learningLanguage: String,
        nativeLanguage: String,
        skillLevel: String,
        onHighAccuracyAchieved: (Int) -> Unit
    ) {
        if (audioBytes.isEmpty()) {
            _liveCoachState.value = LiveCoachState.Error("Recorded audio buffer is empty. Please record again.")
            return
        }
        if (audioBytes.size > 19 * 1024 * 1024) {
            _liveCoachState.value = LiveCoachState.Error("Audio clip exceeds maximum size. Please record a shorter clip.")
            return
        }

        activeCoachJob?.cancel()
        activeScorecardJob?.cancel()

        // 1. Real-Time Streaming Voice Coaching (Low-Latency TTFT via generateContentStream)
        activeCoachJob = viewModelScope.launch {
            _liveCoachState.value = LiveCoachState.Connecting
            val accumulated = StringBuilder()
            try {
                val streamPrompt = content {
                    inlineData(audioBytes, mimeType)
                    text(
                        """
                        You are Noxra AI, an expert, encouraging $learningLanguage pronunciation coach inside Noxra AI.
                        Learner profile: Native language = $nativeLanguage, Level = $skillLevel.
                        Target phrase the learner tried to pronounce: "$targetPhrase"
                        
                        Listen carefully to the attached microphone audio recording and stream concise, real-time coaching (3-5 short bullet points):
                        1. What you heard them say vs the target phrase.
                        2. Which vowels, consonants, or tones sounded crisp and natural.
                        3. Exact mouth/tongue placement & syllable stress fix for any mispronounced or skipped words (include a brief $nativeLanguage hint if helpful).
                        """.trimIndent()
                    )
                }

                withTimeout(60_000L) {
                    streamingCoachModel.generateContentStream(streamPrompt).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        accumulated.append(piece)
                        if (accumulated.isNotEmpty()) {
                            _liveCoachState.value = LiveCoachState.Streaming(accumulated.toString())
                        }
                    }
                }

                val finalText = accumulated.toString().trim()
                if (finalText.isNotEmpty()) {
                    _liveCoachState.value = LiveCoachState.Completed(finalText)
                } else {
                    _liveCoachState.value = LiveCoachState.Error("Gemini returned an empty coaching stream. Please try speaking again.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Real-time pronunciation streaming timed out", e)
                _liveCoachState.value = LiveCoachState.Error("Real-time coaching timed out after 60 seconds. Check your connection and retry.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Real-time pronunciation streaming failed", e)
                _liveCoachState.value = LiveCoachState.Error(
                    e.localizedMessage ?: "Gemini real-time coaching failed."
                )
            }
        }

        // 2. Structured Word-by-Word Acoustic & Phonetic Scorecard (JSON via Schema.obj)
        activeScorecardJob = viewModelScope.launch {
            _scorecardState.value = ScorecardState.Analyzing
            try {
                val structuredPrompt = content {
                    inlineData(audioBytes, mimeType)
                    text(
                        """
                        Analyze the speaker's pronunciation in this audio recording against the target $learningLanguage phrase: "$targetPhrase".
                        The learner's native language is $nativeLanguage ($skillLevel level).
                        Be honest and constructive:
                        - If the audio is silent, unintelligible, or completely unrelated to "$targetPhrase", assign low scores (0-25) and explain clearly.
                        - Evaluate every word of "$targetPhrase" in wordBreakdown with status 'native', 'close', or 'needs_practice' and a concise phoneticTip.
                        """.trimIndent()
                    )
                }

                val response = withTimeout(60_000L) {
                    structuredScorecardModel.generateContent(structuredPrompt)
                }
                val rawJson = response.text ?: throw IllegalStateException("Gemini returned an empty pronunciation scorecard.")
                val report = parsePronunciationReportJson(rawJson)
                _scorecardState.value = ScorecardState.Success(report)

                val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date())
                val record = SpeakingAttemptRecord(
                    id = "att_${System.currentTimeMillis()}",
                    timestampLabel = timeStr,
                    language = learningLanguage,
                    targetPhrase = targetPhrase,
                    heardTranscript = report.heardTranscript,
                    overallScore = report.overallScore,
                    fluencyScore = report.fluencyScore,
                    intonationScore = report.intonationScore
                )
                _attemptHistory.value = (listOf(record) + _attemptHistory.value).take(12)

                if (report.overallScore >= 75) {
                    onHighAccuracyAchieved(report.overallScore)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Structured pronunciation scorecard timed out", e)
                _scorecardState.value = ScorecardState.Error("Pronunciation scorecard timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Structured pronunciation scorecard failed", e)
                _scorecardState.value = ScorecardState.Error(
                    e.localizedMessage ?: "Failed to generate pronunciation scorecard."
                )
            }
        }
    }

    companion object {
        fun parsePronunciationReportJson(rawJson: String): PronunciationFeedbackReport {
            val root = JSONObject(rawJson)
            val wordsArr = root.optJSONArray("wordBreakdown")
            val breakdown = if (wordsArr != null) {
                (0 until wordsArr.length()).map { idx ->
                    val wObj = wordsArr.getJSONObject(idx)
                    WordPronunciationItem(
                        word = wObj.optString("word", ""),
                        status = wObj.optString("status", "close"),
                        phoneticTip = wObj.optString("phoneticTip", "")
                    )
                }
            } else {
                emptyList()
            }

            return PronunciationFeedbackReport(
                heardTranscript = root.optString("heardTranscript", ""),
                overallScore = root.optInt("overallScore", 0).coerceIn(0, 100),
                accuracyScore = root.optInt("accuracyScore", 0).coerceIn(0, 100),
                fluencyScore = root.optInt("fluencyScore", 0).coerceIn(0, 100),
                intonationScore = root.optInt("intonationScore", 0).coerceIn(0, 100),
                strengths = root.optString("strengths", ""),
                actionableTip = root.optString("actionableTip", ""),
                practiceSyllables = root.optString("practiceSyllables", ""),
                wordBreakdown = breakdown
            )
        }
    }
}
