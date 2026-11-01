package com.example.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.data.model.AppHistoryItem
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.TutorGeminiGateway
import com.google.firebase.ai.type.Content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun chatScreen_displaysMessageBubblesInLazyColumn_andSendsFromBottomInputBar() {
        val fakeGateway = object : TutorGeminiGateway {
            override fun streamTutorReply(contents: List<Content>, hasImage: Boolean): Flow<String> {
                return flowOf("Bonjour! ", "Here is how French greetings work.")
            }
        }
        val chatViewModel = ChatViewModel(geminiGateway = fakeGateway)

        composeTestRule.setContent {
            MaterialTheme {
                ChatScreen(chatViewModel = chatViewModel)
            }
        }
        composeTestRule.waitForIdle()

        // Verify LazyColumn, bottom input bar, and initially disabled Send button
        composeTestRule.onNodeWithTag("chat_message_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_bottom_input_bar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_send_button").assertIsNotEnabled()

        // Type user prompt into bottom input field and click Send
        composeTestRule.onNodeWithTag("chat_input_field")
            .performTextInput("Teach me French greetings")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("chat_send_button")
            .assertIsEnabled()
            .performClick()
        composeTestRule.waitForIdle()

        // Verify both User bubble and AI Tutor bubble render inside LazyColumn
        assertEquals(2, chatViewModel.messages.value.size)
        val userMsgId = chatViewModel.messages.value[0].id
        val aiMsgId = chatViewModel.messages.value[1].id

        composeTestRule.onNodeWithTag("chat_bubble_user_$userMsgId").assertIsDisplayed()
        composeTestRule.onNodeWithText("Teach me French greetings").assertIsDisplayed()

        composeTestRule.onNodeWithTag("chat_bubble_ai_$aiMsgId").assertIsDisplayed()
        composeTestRule.onNodeWithText("Bonjour! Here is how French greetings work.").assertIsDisplayed()
    }

    @Test
    fun chatScreen_historyButton_opensSavedHistoryPanel() {
        val fakeGateway = object : TutorGeminiGateway {
            override fun streamTutorReply(contents: List<Content>, hasImage: Boolean): Flow<String> {
                return flowOf("Step 1: Factor the quadratic.")
            }
        }
        val chatViewModel = ChatViewModel(geminiGateway = fakeGateway)
        val sampleHistory = listOf(
            AppHistoryItem(
                id = "hist_1",
                userId = "u1",
                category = "Noxra AI",
                title = "Solve 2x + 6 = 14",
                summary = "Subtract 6 from both sides, then divide by 2 to get x = 4."
            )
        )

        composeTestRule.setContent {
            MaterialTheme {
                ChatScreen(
                    chatViewModel = chatViewModel,
                    savedAiHistory = sampleHistory
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("open_ai_history_button")
            .assertIsDisplayed()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("ai_history_panel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Solve 2x + 6 = 14").assertIsDisplayed()
    }
}
