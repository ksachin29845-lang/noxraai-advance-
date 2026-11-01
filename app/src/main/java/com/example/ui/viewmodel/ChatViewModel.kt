package com.example.ui.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.content
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

private const val TAG = "ChatViewModel"
private const val INFERENCE_TIMEOUT_MS = 60_000L
private const val MAX_HISTORY_MESSAGES = 12
private const val MAX_INLINE_IMAGE_DIMENSION_PX = 1280

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val modeBadge: String = "Simple Explanation",
    val isStreaming: Boolean = false,
    val hasAttachedImage: Boolean = false,
    val timestampMillis: Long = System.currentTimeMillis()
)

data class TutorLearnerContext(
    val learningLanguage: String = "English",
    val nativeLanguage: String = "Hindi",
    val skillLevel: String = "Beginner",
    val studentClass: String = "",
    val xp: Long = 0L,
    val lessonsCompleted: Long = 0L,
    val weakWordsSummary: String = ""
)

data class ChatConversationSession(
    val sessionId: String,
    val title: String,
    val tutorMode: String,
    val learningLanguage: String,
    val messages: List<AiChatMessage>,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

sealed interface ChatUiState {
    data object Idle : ChatUiState
    data object Loading : ChatUiState
    data class Streaming(val partialText: String) : ChatUiState
    data class Success(val text: String) : ChatUiState
    data class Error(val message: String) : ChatUiState
}

interface TutorGeminiGateway {
    fun streamTutorReply(contents: List<Content>, hasImage: Boolean): Flow<String>
}

class FirebaseTutorGeminiGateway : TutorGeminiGateway {
    // Lazy initialization per Firebase AI Logic best practices to avoid eager startup evaluation
    private val fastConversationalModel by lazy {
        Firebase.ai.generativeModel("gemini-3.5-flash-lite")
    }

    private val multimodalTutorModel by lazy {
        Firebase.ai.generativeModel("gemini-3.8-flash")
    }

    override fun streamTutorReply(contents: List<Content>, hasImage: Boolean): Flow<String> {
        val activeModel = if (hasImage) multimodalTutorModel else fastConversationalModel
        return activeModel.generateContentStream(contents).map { chunk ->
            chunk.text.orEmpty()
        }
    }
}

class ChatViewModel(
    private val geminiGateway: TutorGeminiGateway = FirebaseTutorGeminiGateway()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Idle)
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private val _messages = MutableStateFlow<List<AiChatMessage>>(emptyList())
    val messages: StateFlow<List<AiChatMessage>> = _messages.asStateFlow()

    private val _sessions = MutableStateFlow<List<ChatConversationSession>>(emptyList())
    val sessions: StateFlow<List<ChatConversationSession>> = _sessions.asStateFlow()

    private val _activeSessionId = MutableStateFlow(newSessionId())
    val activeSessionId: StateFlow<String> = _activeSessionId.asStateFlow()

    private val _activeTutorMode = MutableStateFlow(DEFAULT_TUTOR_MODE)
    val activeTutorMode: StateFlow<String> = _activeTutorMode.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _chatError = MutableStateFlow<String?>(null)
    val chatError: StateFlow<String?> = _chatError.asStateFlow()

    private val _attachedImage = MutableStateFlow<Bitmap?>(null)
    val attachedImage: StateFlow<Bitmap?> = _attachedImage.asStateFlow()

    private val _followUpSuggestions = MutableStateFlow(defaultFollowUpsForMode(DEFAULT_TUTOR_MODE))
    val followUpSuggestions: StateFlow<List<String>> = _followUpSuggestions.asStateFlow()

    private var activeChatJob: Job? = null
    private var lastLearnerContext: TutorLearnerContext = TutorLearnerContext()

    /** Optional hook invoked when a streaming Noxra AI reply finishes so MainActivity can persist history to Firestore. */
    var onExchangeCompleted: ((prompt: String, reply: String, mode: String) -> Unit)? = null

    fun setTutorMode(mode: String) {
        if (mode.isBlank()) return
        _activeTutorMode.value = mode
        _followUpSuggestions.value = defaultFollowUpsForMode(mode)
    }

    fun attachImage(bitmap: Bitmap?) {
        _attachedImage.value = bitmap?.let { scaleBitmapSafely(it) }
    }

    fun clearAttachedImage() {
        _attachedImage.value = null
    }

    fun dismissError() {
        _chatError.value = null
        if (_uiState.value is ChatUiState.Error) {
            _uiState.value = ChatUiState.Idle
        }
    }

    fun stopGenerating() {
        activeChatJob?.cancel()
        activeChatJob = null
        _isGenerating.value = false
        _messages.value = _messages.value.map { msg ->
            if (msg.isStreaming) {
                if (msg.text.isBlank()) msg.copy(text = "(Stopped)", isStreaming = false)
                else msg.copy(isStreaming = false)
            } else {
                msg
            }
        }
        _uiState.value = ChatUiState.Idle
        syncActiveSessionSnapshot(lastLearnerContext.learningLanguage)
    }

    fun startNewChat(learningLanguage: String = lastLearnerContext.learningLanguage) {
        if (_isGenerating.value) {
            stopGenerating()
        }
        syncActiveSessionSnapshot(learningLanguage)
        _activeSessionId.value = newSessionId()
        _messages.value = emptyList()
        _attachedImage.value = null
        _chatError.value = null
        _uiState.value = ChatUiState.Idle
        _followUpSuggestions.value = defaultFollowUpsForMode(_activeTutorMode.value)
    }

    fun selectSession(sessionId: String) {
        if (sessionId == _activeSessionId.value) return
        if (_isGenerating.value) {
            stopGenerating()
        }
        syncActiveSessionSnapshot(lastLearnerContext.learningLanguage)
        val target = _sessions.value.firstOrNull { it.sessionId == sessionId } ?: return
        _activeSessionId.value = target.sessionId
        _messages.value = target.messages
        _activeTutorMode.value = target.tutorMode
        _attachedImage.value = null
        _chatError.value = null
        _uiState.value = ChatUiState.Idle
        _followUpSuggestions.value = defaultFollowUpsForMode(target.tutorMode)
    }

    fun deleteSession(sessionId: String) {
        val wasActive = sessionId == _activeSessionId.value
        if (wasActive && _isGenerating.value) {
            stopGenerating()
        }
        _sessions.value = _sessions.value.filterNot { it.sessionId == sessionId }
        if (wasActive) {
            val fallback = _sessions.value.firstOrNull()
            if (fallback != null) {
                _activeSessionId.value = fallback.sessionId
                _messages.value = fallback.messages
                _activeTutorMode.value = fallback.tutorMode
            } else {
                _activeSessionId.value = newSessionId()
                _messages.value = emptyList()
            }
            _attachedImage.value = null
            _chatError.value = null
            _uiState.value = ChatUiState.Idle
        }
    }

    fun regenerateLastResponse(learnerContext: TutorLearnerContext = lastLearnerContext) {
        if (_isGenerating.value) return
        val currentList = _messages.value
        val lastUserIdx = currentList.indexOfLast { it.isUser }
        if (lastUserIdx < 0) return

        val lastUserMsg = currentList[lastUserIdx]
        _messages.value = currentList.subList(0, lastUserIdx)
        _activeTutorMode.value = lastUserMsg.modeBadge
        sendMessage(
            userPrompt = lastUserMsg.text,
            learnerContext = learnerContext
        )
    }

    fun sendChatMessage(
        userPrompt: String,
        learningLanguage: String,
        nativeLanguage: String,
        skillLevel: String,
        tutorMode: String = _activeTutorMode.value,
        studentClass: String = "",
        xp: Long = 0L,
        lessonsCompleted: Long = 0L,
        weakWordsSummary: String = ""
    ) {
        if (tutorMode.isNotBlank()) {
            _activeTutorMode.value = tutorMode
        }
        sendMessage(
            userPrompt = userPrompt,
            learnerContext = TutorLearnerContext(
                learningLanguage = learningLanguage,
                nativeLanguage = nativeLanguage,
                skillLevel = skillLevel,
                studentClass = studentClass,
                xp = xp,
                lessonsCompleted = lessonsCompleted,
                weakWordsSummary = weakWordsSummary
            )
        )
    }

    fun sendMessage(
        userPrompt: String,
        learnerContext: TutorLearnerContext = lastLearnerContext,
        imageOverride: Bitmap? = null
    ) {
        val bitmapToSend = (imageOverride ?: _attachedImage.value)?.let { scaleBitmapSafely(it) }
        val trimmed = userPrompt.trim().ifBlank {
            if (bitmapToSend != null) "Please explain and solve what is shown in this study image step by step."
            else ""
        }
        if (trimmed.isEmpty() || _isGenerating.value) return

        lastLearnerContext = learnerContext
        val mode = _activeTutorMode.value
        _chatError.value = null
        _attachedImage.value = null
        _uiState.value = ChatUiState.Loading

        val userMsgId = "u_${UUID.randomUUID().toString().take(10)}"
        val aiMsgId = "a_${UUID.randomUUID().toString().take(10)}"

        val priorHistory = _messages.value.takeLast(MAX_HISTORY_MESSAGES)
        _messages.value = _messages.value + AiChatMessage(
            id = userMsgId,
            isUser = true,
            text = trimmed,
            modeBadge = mode,
            hasAttachedImage = bitmapToSend != null
        ) + AiChatMessage(
            id = aiMsgId,
            isUser = false,
            text = "",
            modeBadge = mode,
            isStreaming = true
        )

        activeChatJob = viewModelScope.launch {
            _isGenerating.value = true
            val accumulated = StringBuilder()
            try {
                val historyContents = buildConversationContents(
                    userPrompt = trimmed,
                    tutorMode = mode,
                    learnerContext = learnerContext,
                    priorHistory = priorHistory,
                    attachedBitmap = bitmapToSend
                )

                withTimeout(INFERENCE_TIMEOUT_MS) {
                    geminiGateway.streamTutorReply(
                        contents = historyContents,
                        hasImage = bitmapToSend != null
                    ).collect { chunkText ->
                        if (chunkText.isNotEmpty()) {
                            accumulated.append(chunkText)
                            val partial = accumulated.toString()
                            _uiState.value = ChatUiState.Streaming(partial)
                            _messages.value = _messages.value.map { m ->
                                if (m.id == aiMsgId) m.copy(text = partial, isStreaming = true) else m
                            }
                        }
                    }
                }

                val finalReply = accumulated.toString().trim()
                if (finalReply.isNotEmpty()) {
                    _messages.value = _messages.value.map { m ->
                        if (m.id == aiMsgId) m.copy(text = finalReply, isStreaming = false) else m
                    }
                    _uiState.value = ChatUiState.Success(finalReply)
                    _followUpSuggestions.value = contextualFollowUps(
                        tutorMode = mode,
                        learningLanguage = learnerContext.learningLanguage
                    )
                    syncActiveSessionSnapshot(learnerContext.learningLanguage)
                    onExchangeCompleted?.invoke(trimmed, finalReply, mode)
                } else {
                    _messages.value = _messages.value.filterNot { it.id == aiMsgId }
                    val emptyMsg = "Noxra AI returned an empty response. Please try again."
                    _chatError.value = emptyMsg
                    _uiState.value = ChatUiState.Error(emptyMsg)
                }
            } catch (e: TimeoutCancellationException) {
                Log.e(TAG, "Noxra AI chat timed out after ${INFERENCE_TIMEOUT_MS}ms", e)
                finalizeBubbleOnFailure(aiMsgId)
                val timeoutMsg = "Noxra AI timed out after 60 seconds. Check your connection and tap Retry."
                _chatError.value = timeoutMsg
                _uiState.value = ChatUiState.Error(timeoutMsg)
            } catch (e: CancellationException) {
                // Always rethrow CancellationException to preserve cooperative coroutine cancellation
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Firebase Gemini AI chat failed", e)
                finalizeBubbleOnFailure(aiMsgId)
                val errMsg = e.localizedMessage ?: "Noxra AI request failed."
                _chatError.value = errMsg
                _uiState.value = ChatUiState.Error(errMsg)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun finalizeBubbleOnFailure(aiMsgId: String) {
        _messages.value = _messages.value.mapNotNull { m ->
            if (m.id != aiMsgId) m
            else if (m.text.isBlank()) null
            else m.copy(isStreaming = false)
        }
        syncActiveSessionSnapshot(lastLearnerContext.learningLanguage)
    }

    private fun syncActiveSessionSnapshot(learningLanguage: String) {
        val currentMessages = _messages.value.filter { it.text.isNotBlank() }
        if (currentMessages.isEmpty()) return

        val currentId = _activeSessionId.value
        val firstUserPrompt = currentMessages.firstOrNull { it.isUser }?.text ?: "AI Tutor Session"
        val cleanTitle = firstUserPrompt
            .replace('\n', ' ')
            .trim()
            .let { if (it.length > 36) "${it.take(36).trimEnd()}…" else it }

        val updatedSession = ChatConversationSession(
            sessionId = currentId,
            title = cleanTitle,
            tutorMode = _activeTutorMode.value,
            learningLanguage = learningLanguage,
            messages = currentMessages,
            updatedAtMillis = System.currentTimeMillis()
        )

        val others = _sessions.value.filterNot { it.sessionId == currentId }
        _sessions.value = (listOf(updatedSession) + others).take(15)
    }

    private fun buildConversationContents(
        userPrompt: String,
        tutorMode: String,
        learnerContext: TutorLearnerContext,
        priorHistory: List<AiChatMessage>,
        attachedBitmap: Bitmap?
    ): List<Content> {
        val styleInstruction = when (tutorMode) {
            "Simple Explanation" ->
                "Explain in super simple, everyday language suitable for ${learnerContext.studentClass.ifBlank { "a student" }}. Keep it crisp and clear."
            "Step-by-Step Teaching" ->
                "Break the concept into numbered steps (Step 1, Step 2, Step 3) and check understanding at the end."
            "Examples First" ->
                "Start immediately with 2-3 relatable real-life examples before stating the underlying rule."
            "Practice Questions" ->
                "Briefly explain, then challenge the student with 2 interactive practice questions without revealing answers until they try."
            "Revision Mode" ->
                "Focus on quick active-recall revision, memory tricks, and reviewing weak spots (${learnerContext.weakWordsSummary.ifBlank { "core vocabulary & grammar" }})."
            "Conversation Practice", "Roleplay: Café", "Roleplay: Interview", "Roleplay: Travel" ->
                "Hold a natural, encouraging conversation in ${learnerContext.learningLanguage} (2-3 short sentences), include a brief ${learnerContext.nativeLanguage} hint for tricky words if ${learnerContext.skillLevel} == Beginner, and ask a follow-up question."
            "Writing Feedback" ->
                "Kindly spot grammar or spelling slips, show the corrected natural sentence, and explain why in 2 short bullets."
            else ->
                "Answer directly, accurately, and concisely. Guide the student to understand rather than giving dry textbook definitions."
        }

        val systemContext = """
            You are Noxra AI, the fast, encouraging AI Tutor inside Noxra AI.
            Student context: Class=${learnerContext.studentClass.ifBlank { "General" }}, Native=${learnerContext.nativeLanguage}, Learning=${learnerContext.learningLanguage} (${learnerContext.skillLevel}), Completed=${learnerContext.lessonsCompleted} lessons, XP=${learnerContext.xp}.
            ${if (learnerContext.weakWordsSummary.isNotBlank()) "Words needing revision: ${learnerContext.weakWordsSummary}." else ""}
            Mode ($tutorMode): $styleInstruction
            Safety: Never claim guaranteed exam scores; encourage steady understanding.
        """.trimIndent()

        return buildList {
            add(content("user") { text(systemContext) })
            add(content("model") { text("Ready to help!") })
            priorHistory.forEach { msg ->
                if (msg.text.isNotBlank() && msg.text != "(Stopped)") {
                    add(content(if (msg.isUser) "user" else "model") { text(msg.text) })
                }
            }
            add(
                content("user") {
                    if (attachedBitmap != null) {
                        image(attachedBitmap)
                    }
                    text(userPrompt)
                }
            )
        }
    }

    private fun scaleBitmapSafely(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        if (width <= MAX_INLINE_IMAGE_DIMENSION_PX && height <= MAX_INLINE_IMAGE_DIMENSION_PX) {
            return source
        }
        val ratio = MAX_INLINE_IMAGE_DIMENSION_PX.toFloat() / maxOf(width, height).toFloat()
        val targetW = (width * ratio).toInt().coerceAtLeast(1)
        val targetH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(source, targetW, targetH, true)
    }

    private fun newSessionId(): String = "chat_${UUID.randomUUID().toString().take(8)}"

    companion object {
        const val DEFAULT_TUTOR_MODE = "Simple Explanation"

        val SUPPORTED_TUTOR_MODES = listOf(
            "Simple Explanation",
            "Step-by-Step Teaching",
            "Examples First",
            "Practice Questions",
            "Revision Mode",
            "Conversation Practice",
            "Roleplay: Café",
            "Writing Feedback"
        )

        fun defaultFollowUpsForMode(mode: String): List<String> = when (mode) {
            "Practice Questions" -> listOf(
                "Give me 2 harder practice questions",
                "Check my answer and explain why",
                "Give me a quick hint first"
            )
            "Conversation Practice", "Roleplay: Café" -> listOf(
                "How do I reply more politely?",
                "Translate your last message",
                "Switch roleplay to travel check-in"
            )
            "Writing Feedback" -> listOf(
                "Make my sentence sound more natural",
                "Explain the grammar rule behind that",
                "Give me a similar sentence to fix"
            )
            else -> listOf(
                "Explain with an even simpler example",
                "Quiz me with 2 quick questions on this",
                "Summarize key takeaways in 3 bullets"
            )
        }

        private fun contextualFollowUps(tutorMode: String, learningLanguage: String): List<String> = when (tutorMode) {
            "Conversation Practice", "Roleplay: Café" -> listOf(
                "Keep the $learningLanguage conversation going!",
                "Explain any new $learningLanguage words you used",
                "Correct my grammar if I made a slip"
            )
            else -> defaultFollowUpsForMode(tutorMode)
        }
    }
}
