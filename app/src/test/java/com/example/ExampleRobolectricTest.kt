package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Noxra AI", appName)
  }

  @Test
  fun `speaking lab curriculum and gemini pronunciation parser work accurately`() {
    val spanishDrills = com.example.data.curriculum.LanguageCurriculum.speakingDrillsForLanguage("Spanish")
    org.junit.Assert.assertTrue(spanishDrills.isNotEmpty())
    org.junit.Assert.assertTrue(spanishDrills.first().phrase.contains("café", ignoreCase = true))

    val sampleJson = """
      {
        "heardTranscript": "Hola, me podría traer un café con leche",
        "overallScore": 88,
        "accuracyScore": 90,
        "fluencyScore": 84,
        "intonationScore": 89,
        "strengths": "Clear vowel sounds and natural pace.",
        "actionableTip": "Tap the tongue lightly on the roof of your mouth for 'traer'.",
        "practiceSyllables": "trah - EHR",
        "wordBreakdown": [
          {"word": "Hola", "status": "native", "phoneticTip": "Silent H"},
          {"word": "traer", "status": "close", "phoneticTip": "Soft single-tap r"}
        ]
      }
    """.trimIndent()

    val parsed = com.example.ui.viewmodel.SpeakingLabViewModel.parsePronunciationReportJson(sampleJson)
    assertEquals(88, parsed.overallScore)
    assertEquals(2, parsed.wordBreakdown.size)
    assertEquals("native", parsed.wordBreakdown[0].status)
    assertEquals("trah - EHR", parsed.practiceSyllables)
  }
}
