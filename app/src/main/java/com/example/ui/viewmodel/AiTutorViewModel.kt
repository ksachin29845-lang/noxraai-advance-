package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "AiTutorViewModel"

data class AiGeneratedQuizItem(
    val question: String,
    val options: List<String>,
    val correctAnswer: String,
    val explanation: String
)

data class AiGeneratedStory(
    val title: String,
    val storyParagraphs: List<String>,
    val keyVocabulary: List<String>,
    val comprehensionQuestion: String,
    val comprehensionAnswer: String
)

sealed interface AiTaskState<out T> {
    data object Idle : AiTaskState<Nothing>
    data object Loading : AiTaskState<Nothing>
    data class Success<T>(val data: T) : AiTaskState<T>
    data class Error(val message: String) : AiTaskState<Nothing>
}

/**
 * Handles structured one-shot AI learning tools (instant lesson explanations, custom JSON quizzes,
 * bilingual interactive stories, context translations, and registration alert summaries).
 * Multi-turn conversational AI Tutor chat state and session history are managed by [ChatViewModel].
 */
class AiTutorViewModel : ViewModel() {

    private val fastFlashLiteModel by lazy {
        Firebase.ai.generativeModel("gemini-3.5-flash-lite")
    }

    private val quizSchema = Schema.obj(
        properties = mapOf(
            "topicTitle" to Schema.string(description = "Title of the personalized quiz"),
            "questions" to Schema.array(
                description = "3 multiple-choice language questions",
                items = Schema.obj(
                    properties = mapOf(
                        "question" to Schema.string(description = "Question prompt"),
                        "options" to Schema.array(
                            items = Schema.string(),
                            description = "Exactly 4 answer choices"
                        ),
                        "correctAnswer" to Schema.string(description = "Exact matching string from options"),
                        "explanation" to Schema.string(description = "Friendly grammar or vocabulary explanation")
                    )
                )
            )
        )
    )

    private val storySchema = Schema.obj(
        properties = mapOf(
            "title" to Schema.string(description = "Engaging bilingual story title"),
            "paragraphs" to Schema.array(
                items = Schema.string(),
                description = "3 short story paragraphs in the target language with helpful hints"
            ),
            "keyVocabulary" to Schema.array(
                items = Schema.string(),
                description = "4 key vocabulary words with meanings"
            ),
            "comprehensionQuestion" to Schema.string(description = "A quick comprehension question about the story"),
            "comprehensionAnswer" to Schema.string(description = "The answer to the comprehension question")
        )
    )

    private val structuredQuizModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = quizSchema
            }
        )
    }

    private val structuredStoryModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = storySchema
            }
        )
    }

    private val _generatedQuizState = MutableStateFlow<AiTaskState<List<AiGeneratedQuizItem>>>(AiTaskState.Idle)
    val generatedQuizState: StateFlow<AiTaskState<List<AiGeneratedQuizItem>>> = _generatedQuizState.asStateFlow()

    private val _generatedStoryState = MutableStateFlow<AiTaskState<AiGeneratedStory>>(AiTaskState.Idle)
    val generatedStoryState: StateFlow<AiTaskState<AiGeneratedStory>> = _generatedStoryState.asStateFlow()

    private val _translationResult = MutableStateFlow<AiTaskState<String>>(AiTaskState.Idle)
    val translationResult: StateFlow<AiTaskState<String>> = _translationResult.asStateFlow()

    private val _explainAnswerState = MutableStateFlow<AiTaskState<String>>(AiTaskState.Idle)
    val explainAnswerState: StateFlow<AiTaskState<String>> = _explainAnswerState.asStateFlow()

    private val _aiRegistrationEmailState = MutableStateFlow<AiTaskState<String>>(AiTaskState.Idle)
    val aiRegistrationEmailState: StateFlow<AiTaskState<String>> = _aiRegistrationEmailState.asStateFlow()

    private val _fiftyLevelAiHintState = MutableStateFlow<AiTaskState<String>>(AiTaskState.Idle)
    val fiftyLevelAiHintState: StateFlow<AiTaskState<String>> = _fiftyLevelAiHintState.asStateFlow()

    private val _fiftyLevelBonusQuestionState = MutableStateFlow<AiTaskState<AiGeneratedQuizItem>>(AiTaskState.Idle)
    val fiftyLevelBonusQuestionState: StateFlow<AiTaskState<AiGeneratedQuizItem>> = _fiftyLevelBonusQuestionState.asStateFlow()

    private var activeExplainJob: Job? = null

    fun clearExplainAnswer() {
        activeExplainJob?.cancel()
        _explainAnswerState.value = AiTaskState.Idle
    }

    fun clearFiftyLevelAiStates() {
        _fiftyLevelAiHintState.value = AiTaskState.Idle
        _fiftyLevelBonusQuestionState.value = AiTaskState.Idle
    }

    fun explainLessonAnswer(
        questionPrompt: String,
        userAttempt: String,
        correctAnswer: String,
        wasCorrect: Boolean,
        learningLanguage: String,
        nativeLanguage: String
    ) {
        activeExplainJob?.cancel()
        activeExplainJob = viewModelScope.launch {
            _explainAnswerState.value = AiTaskState.Loading
            val accumulated = StringBuilder()
            try {
                val prompt = """
                    You are Noxra AI, the fast AI tutor inside Noxra AI.
                    Course: $learningLanguage | Native: $nativeLanguage.
                    Question: "$questionPrompt"
                    Learner answered: "$userAttempt" (${if (wasCorrect) "CORRECT" else "INCORRECT"})
                    Correct answer: "$correctAnswer"

                    Give a concise, friendly 3-bullet explanation:
                    • Rule/Reason why "$correctAnswer" is right.
                    • ${if (wasCorrect) "Quick bonus tip or natural variation." else "Why '$userAttempt' missed and an easy memory trick."}
                    • 1 short $learningLanguage example ($nativeLanguage meaning).
                """.trimIndent()

                withTimeout(60_000L) {
                    fastFlashLiteModel.generateContentStream(prompt).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        accumulated.append(piece)
                        if (accumulated.isNotEmpty()) {
                            _explainAnswerState.value = AiTaskState.Success(accumulated.toString())
                        }
                    }
                }
                val reply = accumulated.toString().trim()
                if (reply.isNotEmpty()) {
                    _explainAnswerState.value = AiTaskState.Success(reply)
                } else {
                    _explainAnswerState.value = AiTaskState.Error("Model returned an empty explanation.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Explain My Answer timed out", e)
                _explainAnswerState.value = AiTaskState.Error("Explanation timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Explain My Answer failed", e)
                _explainAnswerState.value = AiTaskState.Error(e.localizedMessage ?: "Could not generate explanation.")
            }
        }
    }

    fun generateAiRegistrationEmailReport(
        fullName: String,
        username: String,
        email: String,
        userId: String,
        studentClass: String,
        regDate: String,
        regTime: String
    ) {
        viewModelScope.launch {
            _aiRegistrationEmailState.value = AiTaskState.Loading
            val accumulated = StringBuilder()
            try {
                val prompt = """
                    Compose a concise, professional New Student Registration Alert email for Noxra AI Administrator (ksachin29845@gmail.com).
                    Include all verified account fields clearly:
                    - Full Name: $fullName
                    - Username: ${username.ifBlank { "Pending" }}
                    - Class / Grade: ${studentClass.ifBlank { "General Learner" }}
                    - Email: $email
                    - User ID: $userId
                    - Registered On: $regDate at $regTime via Google Sign-In
                    Add a 2-sentence AI onboarding summary welcoming the student and confirming Cloud Firestore sync.
                """.trimIndent()

                withTimeout(60_000L) {
                    fastFlashLiteModel.generateContentStream(prompt).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        accumulated.append(piece)
                        if (accumulated.isNotEmpty()) {
                            _aiRegistrationEmailState.value = AiTaskState.Success(accumulated.toString())
                        }
                    }
                }
                val text = accumulated.toString().trim()
                if (text.isNotEmpty()) {
                    _aiRegistrationEmailState.value = AiTaskState.Success(text)
                } else {
                    _aiRegistrationEmailState.value = AiTaskState.Error("AI returned an empty registration summary.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI registration report timed out", e)
                _aiRegistrationEmailState.value = AiTaskState.Error("AI report timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI registration report failed", e)
                _aiRegistrationEmailState.value = AiTaskState.Error(e.localizedMessage ?: "Failed to compose AI report.")
            }
        }
    }

    fun generatePersonalizedAiQuiz(
        learningLanguage: String,
        nativeLanguage: String,
        skillLevel: String,
        focusTopic: String
    ) {
        viewModelScope.launch {
            _generatedQuizState.value = AiTaskState.Loading
            try {
                val prompt = """
                    Create a 3-question multiple-choice quiz for a $skillLevel learner studying $learningLanguage (native language: $nativeLanguage).
                    Focus topic: $focusTopic.
                    Ensure each question has exactly 4 options and 'correctAnswer' matches one option verbatim.
                """.trimIndent()

                val response = withTimeout(60_000L) {
                    structuredQuizModel.generateContent(prompt)
                }
                val jsonText = response.text ?: throw IllegalStateException("Empty quiz JSON returned.")
                val root = JSONObject(jsonText)
                val arr = root.getJSONArray("questions")
                val parsed = (0 until arr.length()).map { idx ->
                    val obj = arr.getJSONObject(idx)
                    val optsJson = obj.getJSONArray("options")
                    val opts = (0 until optsJson.length()).map { optsJson.getString(it) }
                    AiGeneratedQuizItem(
                        question = obj.getString("question"),
                        options = opts,
                        correctAnswer = obj.getString("correctAnswer"),
                        explanation = obj.getString("explanation")
                    )
                }
                _generatedQuizState.value = AiTaskState.Success(parsed)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI quiz generation timed out", e)
                _generatedQuizState.value = AiTaskState.Error("Quiz generation timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI quiz generation failed", e)
                _generatedQuizState.value = AiTaskState.Error(e.localizedMessage ?: "Failed to generate AI quiz.")
            }
        }
    }

    fun generateInteractiveAiStory(
        learningLanguage: String,
        nativeLanguage: String,
        skillLevel: String,
        theme: String
    ) {
        viewModelScope.launch {
            _generatedStoryState.value = AiTaskState.Loading
            try {
                val prompt = """
                    Write a fun, short interactive reading story in $learningLanguage for a $skillLevel student whose native language is $nativeLanguage.
                    Story theme: $theme.
                    Include helpful $nativeLanguage meaning hints for tricky phrases.
                """.trimIndent()

                val response = withTimeout(60_000L) {
                    structuredStoryModel.generateContent(prompt)
                }
                val jsonText = response.text ?: throw IllegalStateException("Empty story JSON returned.")
                val root = JSONObject(jsonText)
                val pArr = root.getJSONArray("paragraphs")
                val vArr = root.getJSONArray("keyVocabulary")
                val story = AiGeneratedStory(
                    title = root.getString("title"),
                    storyParagraphs = (0 until pArr.length()).map { pArr.getString(it) },
                    keyVocabulary = (0 until vArr.length()).map { vArr.getString(it) },
                    comprehensionQuestion = root.getString("comprehensionQuestion"),
                    comprehensionAnswer = root.getString("comprehensionAnswer")
                )
                _generatedStoryState.value = AiTaskState.Success(story)
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "AI story generation timed out", e)
                _generatedStoryState.value = AiTaskState.Error("Story generation timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "AI story generation failed", e)
                _generatedStoryState.value = AiTaskState.Error(e.localizedMessage ?: "Failed to generate AI story.")
            }
        }
    }

    fun translateWithContext(
        inputPhrase: String,
        sourceOrTargetLang: String,
        nativeLanguage: String
    ) {
        if (inputPhrase.isBlank()) return
        viewModelScope.launch {
            _translationResult.value = AiTaskState.Loading
            val accumulated = StringBuilder()
            try {
                val prompt = """
                    Provide a fast, clear translation & usage guide for: "$inputPhrase"
                    Between $sourceOrTargetLang and $nativeLanguage:
                    1) Natural Translation
                    2) Pronunciation / Phonetic guide
                    3) Polite vs Everyday nuance
                    4) 1 short example sentence.
                """.trimIndent()

                withTimeout(60_000L) {
                    fastFlashLiteModel.generateContentStream(prompt).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        accumulated.append(piece)
                        if (accumulated.isNotEmpty()) {
                            _translationResult.value = AiTaskState.Success(accumulated.toString())
                        }
                    }
                }
                val text = accumulated.toString().trim()
                if (text.isNotBlank()) {
                    _translationResult.value = AiTaskState.Success(text)
                } else {
                    _translationResult.value = AiTaskState.Error("Model returned an empty translation.")
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Context translation timed out", e)
                _translationResult.value = AiTaskState.Error("Translation timed out after 60 seconds.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Context translation failed", e)
                _translationResult.value = AiTaskState.Error(e.localizedMessage ?: "Translation failed.")
            }
        }
    }

    fun explainFiftyLevelConcept(
        isMath: Boolean,
        studentClass: String,
        tier: String,
        levelNumber: Int,
        questionPrompt: String,
        correctAnswer: String
    ) {
        viewModelScope.launch {
            _fiftyLevelAiHintState.value = AiTaskState.Loading
            val accumulated = StringBuilder()
            try {
                val subjectLabel = if (isMath) "Mathematics" else "English"
                val prompt = """
                    You are Noxra AI coaching a $studentClass student on $subjectLabel ($tier Tier, Level $levelNumber of 50).
                    Question: "$questionPrompt"
                    Target Solution: "$correctAnswer"

                    Explain step-by-step in 3 super easy, friendly bullets how to solve or master this so any $studentClass student understands immediately!
                """.trimIndent()

                withTimeout(60_000L) {
                    fastFlashLiteModel.generateContentStream(prompt).collect { chunk ->
                        val piece = chunk.text.orEmpty()
                        accumulated.append(piece)
                        if (accumulated.isNotEmpty()) {
                            _fiftyLevelAiHintState.value = AiTaskState.Success(accumulated.toString())
                        }
                    }
                }
                val text = accumulated.toString().trim()
                if (text.isNotEmpty()) {
                    _fiftyLevelAiHintState.value = AiTaskState.Success(text)
                } else {
                    _fiftyLevelAiHintState.value = AiTaskState.Error("Noxra AI returned an empty explanation.")
                }
            } catch (e: TimeoutCancellationException) {
                _fiftyLevelAiHintState.value = AiTaskState.Error("Noxra AI explanation timed out.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _fiftyLevelAiHintState.value = AiTaskState.Error(e.localizedMessage ?: "Could not fetch Noxra AI explanation.")
            }
        }
    }

    fun generateFiftyLevelAiQuestion(
        isMath: Boolean,
        studentClass: String,
        tier: String,
        levelNumber: Int
    ) {
        viewModelScope.launch {
            _fiftyLevelBonusQuestionState.value = AiTaskState.Loading
            try {
                val subjectLabel = if (isMath) "Mathematics (equations, word problems, mental math, geometry, or algebra)" else "English (grammar, vocabulary, sentence building, tenses, or speaking comprehension)"
                val prompt = """
                    You are Noxra AI. Generate 1 fresh multiple-choice practice question for $studentClass at $tier difficulty (Level $levelNumber out of 50) in $subjectLabel.
                    Return JSON matching the schema (put at least 1 item in 'questions' with 4 options and verbatim 'correctAnswer').
                """.trimIndent()

                val response = withTimeout(60_000L) {
                    structuredQuizModel.generateContent(prompt)
                }
                val jsonText = response.text ?: throw IllegalStateException("Empty Noxra AI question JSON.")
                val root = JSONObject(jsonText)
                val arr = root.getJSONArray("questions")
                if (arr.length() == 0) throw IllegalStateException("No questions generated.")
                val obj = arr.getJSONObject(0)
                val optsJson = obj.getJSONArray("options")
                val opts = (0 until optsJson.length()).map { optsJson.getString(it) }
                val item = AiGeneratedQuizItem(
                    question = obj.getString("question"),
                    options = opts,
                    correctAnswer = obj.getString("correctAnswer"),
                    explanation = obj.getString("explanation")
                )
                _fiftyLevelBonusQuestionState.value = AiTaskState.Success(item)
            } catch (e: TimeoutCancellationException) {
                _fiftyLevelBonusQuestionState.value = AiTaskState.Error("Noxra AI bonus question timed out.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _fiftyLevelBonusQuestionState.value = AiTaskState.Error(e.localizedMessage ?: "Failed to generate Noxra AI question.")
            }
        }
    }
}
