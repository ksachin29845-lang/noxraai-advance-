package com.example.ui.screens

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.core.PublicSeoConfig
import com.example.core.PublicWebLinkManager
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import com.example.ui.viewmodel.DataUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w480dp-h1600dp")
class PublicWebPortalScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun publicWebLinkManager_returnsLiveSharedAppUrl_andGeneratesGoogleSeoSnippet() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        PublicWebLinkManager.resetPublicWebUrl(context)

        val defaultUrl = PublicWebLinkManager.getPublicWebUrl(context)
        assertEquals(
            "https://ais-pre-ebyzwvrftdrahqkgejpaka-597285151758.asia-southeast1.run.app",
            defaultUrl
        )

        val profileLink = PublicWebLinkManager.buildUserPublicProfileUrl(defaultUrl, "@vishal_ai")
        assertEquals(
            "https://ais-pre-ebyzwvrftdrahqkgejpaka-597285151758.asia-southeast1.run.app/?u=vishal_ai",
            profileLink
        )

        val htmlSnippet = PublicWebLinkManager.generateHtmlSeoSnippet(
            publicUrl = defaultUrl,
            seo = PublicSeoConfig(),
            developerName = "Sachin Kumar"
        )
        assertTrue(htmlSnippet.contains("application/ld+json"))
        assertTrue(htmlSnippet.contains("EducationalApplication"))
        assertTrue(htmlSnippet.contains("index, follow"))
    }

    @Test
    fun publicWebPortalScreen_searchesPublicDirectory_andSwitchesLinkAndGoogleSeoTabs() {
        val sampleProfile = UserProfile(
            id = "u1",
            userId = "u1",
            displayName = "Vishal Kumar",
            username = "@vishal_2025",
            studentClass = "Class 9",
            xp = 850L,
            streak = 7L,
            leaderboardPrivacy = false
        )
        val sampleLeaderboard = listOf(
            LeaderboardEntry(
                id = "u2",
                userId = "u2",
                displayName = "Diya Verma",
                username = "@diya_math",
                studentClass = "Class 8",
                xp = 1290L,
                streak = 14L,
                league = "Gold"
            )
        )

        var visibilityCallbackValue: Boolean? = null

        composeTestRule.setContent {
            MaterialTheme {
                PublicWebPortalScreen(
                    profile = sampleProfile,
                    leaderboardState = DataUiState.Success(sampleLeaderboard),
                    customCourses = emptyList(),
                    onTogglePublicVisibility = { visibilityCallbackValue = it },
                    onOpenEnglishFiftyLevels = {},
                    onOpenMathFiftyLevels = {},
                    onOpenSpeakingLab = {},
                    onOpenAiTutor = {},
                    onOpenFriendChat = {},
                    onOpenDeveloperInfo = {},
                    onStartCustomCourse = {},
                    onShowMessage = {},
                    onBack = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // 1. Search Directory filters public learner by @username
        composeTestRule.onNodeWithTag("public_google_style_search_input")
            .performTextInput("diya_math")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("public_search_result_learner_u2")
            .assertIsDisplayed()

        // 2. Switch to Public Link & QR tab and verify live URL + visibility toggle
        composeTestRule.onNodeWithTag("portal_tab_1").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("portal_official_web_url_text").assertIsDisplayed()
        composeTestRule.onNodeWithTag("public_visibility_switch")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertEquals(false, visibilityCallbackValue)

        // 3. Switch to Google SEO & Index tab and verify Google SERP preview card
        composeTestRule.onNodeWithTag("portal_tab_2").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("google_serp_preview_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Live Google Search Result Preview").assertIsDisplayed()
    }
}
