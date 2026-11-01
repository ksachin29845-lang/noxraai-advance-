package com.example.ui.viewmodel

import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.TextPart
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import android.os.Looper

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChatViewModelTest {

    @Test
    fun sendMessage_streamsChunks_updatesMessagesUiStateAndArchivesSession() {
        val capturedHistories = mutableListOf<List<Content>>()
        val gateway = object : TutorGeminiGateway {
            override fun streamTutorReply(contents: List<Content>, hasImage: Boolean): Flow<String> {
                capturedHistories.add(contents)
                return flowOf("Namaste! ", "Newton's First Law ", "explains inertia.")
            }
        }

        val viewModel = ChatViewModel(geminiGateway = gateway)
        viewModel.setTutorMode("Step-by-Step Teaching")
        viewModel.sendChatMessage(
            userPrompt = "Explain Newton's First Law",
            learningLanguage = "English",
            nativeLanguage = "Hindi",
            skillLevel = "Intermediate",
            studentClass = "Class 9",
            xp = 240L,
            lessonsCompleted = 8L
        )
        shadowOf(Looper.getMainLooper()).idle()

        val msgs = viewModel.messages.value
        assertEquals(2, msgs.size)
        assertTrue(msgs[0].isUser)
        assertEquals("Explain Newton's First Law", msgs[0].text)
        assertFalse(msgs[1].isUser)
        assertEquals("Namaste! Newton's First Law explains inertia.", msgs[1].text)
        assertFalse(msgs[1].isStreaming)
        assertEquals("Step-by-Step Teaching", msgs[1].modeBadge)

        val finalState = viewModel.uiState.value
        assertTrue("Expected ChatUiState.Success, got $finalState", finalState is ChatUiState.Success)
        assertEquals("Namaste! Newton's First Law explains inertia.", (finalState as ChatUiState.Success).text)
        assertNull(viewModel.chatError.value)
        assertEquals(1, viewModel.sessions.value.size)

        // Second turn preserves multi-turn conversation history
        viewModel.sendMessage("Give me a cricket example")
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(4, viewModel.messages.value.size)
        assertEquals(2, capturedHistories.size)
        val secondTurnTexts = capturedHistories[1].flatMap { c ->
            c.parts.filterIsInstance<TextPart>().map { it.text }
        }
        assertTrue(secondTurnTexts.any { it.contains("Explain Newton's First Law") })
        assertTrue(secondTurnTexts.any { it.contains("Namaste! Newton's First Law explains inertia.") })
        assertTrue(secondTurnTexts.any { it.contains("Give me a cricket example") })
    }

    @Test
    fun startNewChat_andSelectSession_preservesAndRestoresChatHistory() {
        var callIndex = 0
        val gateway = object : TutorGeminiGateway {
            override fun streamTutorReply(contents: List<Content>, hasImage: Boolean): Flow<String> {
                callIndex++
                return flowOf("Tutor reply #$callIndex")
            }
        }

        val viewModel = ChatViewModel(geminiGateway = gateway)
        val firstSessionId = viewModel.activeSessionId.value

        viewModel.sendMessage("Help me practice German greetings")
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(2, viewModel.messages.value.size)

        // Start a brand-new chat and verify previous session was archived
        viewModel.startNewChat("German")
        assertTrue(viewModel.messages.value.isEmpty())
        assertEquals(1, viewModel.sessions.value.size)
        assertEquals(firstSessionId, viewModel.sessions.value.first().sessionId)

        // Send a message in the second session
        viewModel.sendMessage("Teach me Spanish numbers")
        shadowOf(Looper.getMainLooper()).idle()
        assertEquals(2, viewModel.sessions.value.size)

        // Switch back to the first session and verify full message restoration
        viewModel.selectSession(firstSessionId)
        assertEquals(firstSessionId, viewModel.activeSessionId.value)
        assertEquals(2, viewModel.messages.value.size)
        assertEquals("Help me practice German greetings", viewModel.messages.value[0].text)
        assertEquals("Tutor reply #1", viewModel.messages.value[1].text)
    }

    @Test
    fun regenerateLastResponse_andErrorHandling_surfaceStateCleanly() {
        var shouldFail = true
        var attempt = 0
        val gateway = object : TutorGeminiGateway {
            override fun streamTutorReply(contents: List<Content>, hasImage: Boolean): Flow<String> = flow {
                attempt++
                if (shouldFail) {
                    throw IllegalStateException("Simulated App Check or network failure")
                }
                emit("Recovered explanation on attempt #$attempt")
            }
        }

        val viewModel = ChatViewModel(geminiGateway = gateway)
        viewModel.sendMessage("How do prepositions work in French?")
        shadowOf(Looper.getMainLooper()).idle()

        // Error is surfaced transparently to UI state
        assertTrue(viewModel.uiState.value is ChatUiState.Error)
        assertNotNull(viewModel.chatError.value)
        assertTrue(viewModel.chatError.value!!.contains("Simulated App Check"))
        // Empty streaming placeholder bubble was removed, leaving user's prompt intact for retry
        assertEquals(1, viewModel.messages.value.size)

        // Retry via regenerateLastResponse succeeds and clears error state
        shouldFail = false
        viewModel.regenerateLastResponse()
        shadowOf(Looper.getMainLooper()).idle()

        assertNull(viewModel.chatError.value)
        assertTrue(viewModel.uiState.value is ChatUiState.Success)
        assertEquals(2, viewModel.messages.value.size)
        assertEquals("Recovered explanation on attempt #2", viewModel.messages.value[1].text)
    }
}
