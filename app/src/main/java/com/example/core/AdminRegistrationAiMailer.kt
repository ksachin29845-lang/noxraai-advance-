package com.example.core

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

const val NOXRA_ADMIN_EMAIL = "ksachin29845@gmail.com"
private const val TAG = "AdminRegAiMailer"
private const val FORMSUBMIT_ENDPOINT = "https://formsubmit.co/ajax/ksachin29845@gmail.com"

data class RegistrationUserSnapshot(
    val userId: String,
    val fullName: String,
    val username: String,
    val age: Int,
    val studentClass: String,
    val selectedTier: String,
    val nativeLanguage: String,
    val learningLanguage: String,
    val email: String,
    val hasPasscode: Boolean,
    val registeredDate: String,
    val registeredTime: String
) {
    fun toStructuredReceipt(): String = """
        🚀 NOXRA AI — NEW ACCOUNT REGISTRATION REPORT
        ==============================================
        Admin Recipient: $NOXRA_ADMIN_EMAIL
        App Name: Noxra AI
        Developer: Sachin Kumar (Age: 12 y • CEO, Founder & Owner)
        ----------------------------------------------
        Student Full Name: $fullName
        Username ID: $username
        Age: $age years
        Selected Class: $studentClass
        Starting Tier: $selectedTier (English & Math Levels 1–50)
        Native Language: $nativeLanguage
        Learning Language: $learningLanguage
        Email Address: $email
        Cloud User UID: $userId
        Sync Passcode Set: ${if (hasPasscode) "Yes (Any-Phone Login Active)" else "Google Cloud Sync"}
        Registration Date: $registeredDate
        Registration Time: $registeredTime
    """.trimIndent()
}

sealed interface AutoAdminEmailState {
    data object Idle : AutoAdminEmailState
    data object ComposingWithAi : AutoAdminEmailState
    data class SendingAutomatically(val aiReport: String) : AutoAdminEmailState
    data class SentAutomatically(val aiReport: String, val channelNote: String) : AutoAdminEmailState
    data class FallbackReady(val aiReport: String, val reason: String) : AutoAdminEmailState
}

object AdminRegistrationAiMailer {

    private val aiModel by lazy {
        Firebase.ai.generativeModel("gemini-3.5-flash-lite")
    }

    /**
     * Automatically composes a complete user registration report via Gemini (Firebase AI Logic)
     * and dispatches it in the background to ksachin29845@gmail.com.
     *
     * Returns Pair(finalReportText, statusLabel) for Cloud Firestore audit persistence.
     */
    suspend fun composeWithAiAndDispatchToAdmin(
        snapshot: RegistrationUserSnapshot,
        onProgress: (AutoAdminEmailState) -> Unit
    ): Pair<String, String> {
        onProgress(AutoAdminEmailState.ComposingWithAi)

        val baseReceipt = snapshot.toStructuredReceipt()
        val aiGeneratedReport = composeReportWithGemini(snapshot, baseReceipt)

        onProgress(AutoAdminEmailState.SendingAutomatically(aiGeneratedReport))

        val deliveryResult = dispatchViaHttpJson(snapshot, aiGeneratedReport)
        return deliveryResult.fold(
            onSuccess = { note ->
                val statusLabel = "Sent automatically via AI ($note)"
                onProgress(AutoAdminEmailState.SentAutomatically(aiGeneratedReport, statusLabel))
                aiGeneratedReport to statusLabel
            },
            onFailure = { err ->
                Log.w(TAG, "Direct HTTP email relay unavailable; Cloud Admin Audit saved", err)
                val statusLabel = "AI report saved to Admin Cloud Console (${err.message?.take(48) ?: "Relay queued"})"
                onProgress(
                    AutoAdminEmailState.FallbackReady(
                        aiReport = aiGeneratedReport,
                        reason = statusLabel
                    )
                )
                aiGeneratedReport to statusLabel
            }
        )
    }

    private suspend fun composeReportWithGemini(
        snapshot: RegistrationUserSnapshot,
        fallbackReceipt: String
    ): String {
        val prompt = """
            You are Noxra AI's automated registration assistant created by Developer Sachin Kumar (Age: 12 y, CEO, Founder & Owner).
            A brand-new student just completed full registration on the Noxra AI app.
            Compose a concise, professional plain-text instant registration email addressed to Admin ($NOXRA_ADMIN_EMAIL).

            Include EVERY single user detail clearly:
            - Full Name: ${snapshot.fullName}
            - Username ID: ${snapshot.username}
            - Age: ${snapshot.age} years
            - Class / Grade: ${snapshot.studentClass}
            - Starting Level Tier: ${snapshot.selectedTier} (English 1–50 & Math 1–50)
            - Native Language: ${snapshot.nativeLanguage}
            - Learning Language: ${snapshot.learningLanguage}
            - Email Address: ${snapshot.email}
            - Cloud User ID: ${snapshot.userId}
            - Cross-Phone Sync Passcode: ${if (snapshot.hasPasscode) "Configured" else "Google Auth"}
            - Registered At: ${snapshot.registeredDate} at ${snapshot.registeredTime}

            End with a 2-sentence AI Learning Profile Recommendation for how Noxra AI will guide ${snapshot.fullName} through ${snapshot.studentClass} (${snapshot.selectedTier} Tier).
            Keep under 220 words. Plain text only, no markdown code fences.
        """.trimIndent()

        return try {
            val response = withTimeout(35_000L) {
                aiModel.generateContent(prompt)
            }
            val text = response.text?.trim()
            if (!text.isNullOrBlank()) {
                text
            } else {
                fallbackReceipt
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI email composition timed out; using complete structured report", e)
            "$fallbackReceipt\n\n[AI Note: Composed via instant structured template after timeout]"
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "AI email composition error; using complete structured report", e)
            "$fallbackReceipt\n\n[AI Note: Instant structured report generated]"
        }
    }

    private suspend fun dispatchViaHttpJson(
        snapshot: RegistrationUserSnapshot,
        aiReport: String
    ): Result<String> = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            val payload = JSONObject().apply {
                put("_subject", "🚀 Noxra AI New Account: ${snapshot.fullName} (${snapshot.username})")
                put("_template", "table")
                put("_captcha", "false")
                put("app_name", "Noxra AI — Created by Sachin Kumar (12 y, CEO, Founder & Owner)")
                put("admin_email", NOXRA_ADMIN_EMAIL)
                put("student_full_name", snapshot.fullName)
                put("username_id", snapshot.username)
                put("student_age", "${snapshot.age} years")
                put("student_class", snapshot.studentClass)
                put("selected_tier", "${snapshot.selectedTier} (Levels 1–50)")
                put("native_language", snapshot.nativeLanguage)
                put("learning_language", snapshot.learningLanguage)
                put("student_email", snapshot.email)
                put("cloud_uid", snapshot.userId)
                put("passcode_sync", if (snapshot.hasPasscode) "Configured" else "Google Auth")
                put("registered_at", "${snapshot.registeredDate} ${snapshot.registeredTime}")
                put("ai_generated_summary", aiReport)
            }

            val url = URL(FORMSUBMIT_ENDPOINT)
            connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                connectTimeout = 15_000
                readTimeout = 15_000
                doInput = true
                doOutput = true
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Origin", "https://noxra.ai")
                setRequestProperty("Referer", "https://noxra.ai/")
                setRequestProperty("User-Agent", "NoxraAI-Android/1.0")
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            val responseBody = try {
                val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
                stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            } catch (_: Exception) {
                ""
            }

            if (responseCode in 200..299) {
                Log.d(TAG, "Automatic AI registration email dispatched ($responseCode): $responseBody")
                Result.success("Delivered to $NOXRA_ADMIN_EMAIL")
            } else {
                Log.w(TAG, "Email relay returned HTTP $responseCode: $responseBody")
                Result.failure(IllegalStateException("HTTP $responseCode"))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed automatic HTTP email dispatch", e)
            Result.failure(e)
        } finally {
            connection?.disconnect()
        }
    }

    fun openEmailClientWithReport(
        context: Context,
        subjectName: String,
        username: String,
        body: String
    ) {
        try {
            val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$NOXRA_ADMIN_EMAIL")
                putExtra(Intent.EXTRA_SUBJECT, "Noxra AI New Account Info: $subjectName ($username)")
                putExtra(Intent.EXTRA_TEXT, body)
            }
            context.startActivity(Intent.createChooser(mailIntent, "Send User Info to $NOXRA_ADMIN_EMAIL"))
        } catch (e: Exception) {
            Log.w(TAG, "No email client available on device", e)
        }
    }
}
