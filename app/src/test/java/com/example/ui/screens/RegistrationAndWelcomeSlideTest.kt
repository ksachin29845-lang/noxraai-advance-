package com.example.ui.screens

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.core.AutoAdminEmailState
import com.example.ui.components.WelcomeSlideshowSurfaceContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1600dp")
class RegistrationAndWelcomeSlideTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun completeRegistrationGate_blocksIncompleteInfo_andSubmitsOnlyWhenAllFieldsFilled() {
        var submitted = false
        var capturedUsername = ""
        var capturedAge = 0
        var capturedClass = ""

        composeTestRule.setContent {
            MaterialTheme {
                CompleteRegistrationGateScreen(
                    initialFullName = "",
                    email = "student@noxra.ai",
                    onCompleteAllInfo = { _, username, age, studentClass, _, _, _, _ ->
                        submitted = true
                        capturedUsername = username
                        capturedAge = age
                        capturedClass = studentClass
                    },
                    onOpenDeveloperInfo = {},
                    onSignOut = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // 1. Clicking submit without filling all info blocks submission and displays validation error
        composeTestRule.onNodeWithTag("gate_complete_registration_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assertFalse(submitted)
        composeTestRule.onNodeWithTag("gate_validation_error")
            .performScrollTo()
            .assertIsDisplayed()

        // 2. Fill all required fields
        composeTestRule.onNodeWithTag("gate_full_name_input")
            .performScrollTo()
            .performTextInput("Aarav Sharma")
        composeTestRule.onNodeWithTag("gate_username_input")
            .performScrollTo()
            .performTextInput("aarav_ai")
        composeTestRule.onNodeWithTag("gate_age_input")
            .performScrollTo()
            .performTextInput("14")
        composeTestRule.onNodeWithTag("gate_class_input")
            .performScrollTo()
            .performTextInput("Class 9")
        composeTestRule.onNodeWithTag("gate_passcode_input")
            .performScrollTo()
            .performTextInput("4829")

        composeTestRule.onNodeWithTag("gate_complete_registration_button")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assertTrue(submitted)
        assertEquals("@aarav_ai", capturedUsername)
        assertEquals(14, capturedAge)
        assertEquals("Class 9", capturedClass)
    }

    @Test
    fun welcomeSlideshow_displaysWelcomeToOurNoxraAiApp_developerSachinKumar_andAutoAiEmailStatus() {
        composeTestRule.setContent {
            MaterialTheme {
                WelcomeSlideshowSurfaceContent(
                    studentName = "Aarav Sharma",
                    username = "@aarav_ai",
                    studentClass = "Class 9",
                    email = "aarav@noxra.ai",
                    developerName = "Sachin Kumar",
                    developerAge = "12 y",
                    developerRole = "CEO, Founder & Owner",
                    autoAdminEmailState = AutoAdminEmailState.SentAutomatically(
                        aiReport = "New user Aarav Sharma (@aarav_ai) registered on Noxra AI.",
                        channelNote = "Delivered"
                    ),
                    onOpenEmailClientWithReport = {},
                    onOpenDeveloperEditor = {},
                    onFinish = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Welcome to Our Noxra AI App!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Developer: Sachin Kumar").assertIsDisplayed()
        composeTestRule.onNodeWithTag("welcome_auto_ai_email_card")
            .performScrollTo()
            .assertIsDisplayed()
    }

    @Test
    fun adminEmailAndDeveloperProfileEditLock_onlyOwnerCanSeeEditTab() {
        assertEquals("ksachin29845@gmail.com", com.example.core.NOXRA_ADMIN_EMAIL)

        var ownerView by androidx.compose.runtime.mutableStateOf(false)
        composeTestRule.setContent {
            MaterialTheme {
                com.example.ui.components.AboutDeveloperSurfaceContent(
                    onDismiss = {},
                    onFeedbackMessage = {},
                    canEditDeveloperInfo = ownerView,
                    onSaveDeveloperEdits = { _, _, _, _ -> }
                )
            }
        }
        composeTestRule.waitForIdle()

        // Non-owner cannot see or tap dev_edit_tab
        composeTestRule.onNodeWithTag("dev_story_tab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dev_profile_tab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dev_edit_tab").assertDoesNotExist()

        // Switch to verified owner (ksachin29845@gmail.com) -> dev_edit_tab unlocks
        ownerView = true
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dev_edit_tab").assertIsDisplayed()
    }
}
