package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.core.AutoAdminEmailState
import com.example.core.NOXRA_ADMIN_EMAIL

private data class WelcomeSlideSpec(
    val emoji: String,
    val badge: String,
    val headline: String,
    val description: String,
    val highlights: List<String>
)

@Composable
fun WelcomeSlideshowDialog(
    studentName: String,
    username: String,
    studentClass: String,
    email: String,
    developerName: String = DEFAULT_DEV_NAME,
    developerAge: String = DEFAULT_DEV_AGE,
    developerRole: String = DEFAULT_DEV_ROLE,
    canEditDeveloperInfo: Boolean = false,
    autoAdminEmailState: AutoAdminEmailState = AutoAdminEmailState.Idle,
    onRetryAutoAiEmail: () -> Unit = {},
    onOpenEmailClientWithReport: (String?) -> Unit,
    onOpenDeveloperEditor: () -> Unit,
    onFinish: () -> Unit
) {
    Dialog(
        onDismissRequest = onFinish,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        WelcomeSlideshowSurfaceContent(
            studentName = studentName,
            username = username,
            studentClass = studentClass,
            email = email,
            developerName = developerName,
            developerAge = developerAge,
            developerRole = developerRole,
            canEditDeveloperInfo = canEditDeveloperInfo,
            autoAdminEmailState = autoAdminEmailState,
            onRetryAutoAiEmail = onRetryAutoAiEmail,
            onOpenEmailClientWithReport = onOpenEmailClientWithReport,
            onOpenDeveloperEditor = onOpenDeveloperEditor,
            onFinish = onFinish
        )
    }
}

@Composable
fun WelcomeSlideshowSurfaceContent(
    studentName: String,
    username: String,
    studentClass: String,
    email: String,
    developerName: String = DEFAULT_DEV_NAME,
    developerAge: String = DEFAULT_DEV_AGE,
    developerRole: String = DEFAULT_DEV_ROLE,
    canEditDeveloperInfo: Boolean = false,
    autoAdminEmailState: AutoAdminEmailState = AutoAdminEmailState.Idle,
    onRetryAutoAiEmail: () -> Unit = {},
    onOpenEmailClientWithReport: (String?) -> Unit,
    onOpenDeveloperEditor: () -> Unit,
    onFinish: () -> Unit
) {
    var currentSlide by rememberSaveable { mutableIntStateOf(0) }
    var showAiReportPreview by rememberSaveable { mutableStateOf(false) }

    val resolvedDevName = developerName.ifBlank { DEFAULT_DEV_NAME }
    val resolvedDevAge = developerAge.ifBlank { DEFAULT_DEV_AGE }
    val resolvedDevRole = developerRole.ifBlank { DEFAULT_DEV_ROLE }

    val slides = listOf(
        WelcomeSlideSpec(
            emoji = "🚀",
            badge = "SLIDE 1 OF 4 • WELCOME TO NOXRA AI",
            headline = "Welcome to Our Noxra AI App!",
            description = "Congratulations $studentName (${username.ifBlank { "@student" }})! Your $studentClass account ($email) is now active on Noxra AI — created by Developer $resolvedDevName.",
            highlights = listOf(
                "👑 Developer: $resolvedDevName ($resolvedDevAge • $resolvedDevRole of Noxra AI)",
                "🤖 Instant AI Admin Email automatically dispatched to $NOXRA_ADMIN_EMAIL",
                "🔑 Log in on ANY phone anytime — never need to register again"
            )
        ),
        WelcomeSlideSpec(
            emoji = "🎯",
            badge = "SLIDE 2 OF 4 • 50-LEVEL MASTERY PATHS",
            headline = "English & Math Levels 1 to 50 + AI",
            description = "Select your Class and choose Basic, Medium, or Hard tier — then practice Levels 1, 2, 3 … 50 step by step!",
            highlights = listOf(
                "🇬🇧 English 1–50: Grammar, speaking, vocabulary & confidence made easy",
                "➗ Math 1–50: Mental math, equations, geometry & live Noxra AI solver",
                "✨ Noxra AI explains every tricky question in 3 simple bullets"
            )
        ),
        WelcomeSlideSpec(
            emoji = "🔥",
            badge = "SLIDE 3 OF 4 • EVENTS & SOCIAL CHAT",
            headline = "Naruto Event ₹49 & Friend @username Chat",
            description = "Unlock exclusive boosts and study together with friends across Noxra AI.",
            highlights = listOf(
                "🔥 Subscription Mode: Naruto Event — ₹49 (2x XP + Unlimited Hearts + Hokage Badge)",
                "💬 Search any friend's @username to chat 1-on-1 in real time",
                "📜 Every lesson, level, and Noxra AI chat saves to your Cloud History"
            )
        ),
        WelcomeSlideSpec(
            emoji = "👑",
            badge = "SLIDE 4 OF 4 • MEET THE DEVELOPER",
            headline = "$resolvedDevName • $resolvedDevRole",
            description = "Noxra AI App is built and led by Developer $resolvedDevName (Age: $resolvedDevAge) — $resolvedDevRole of Noxra AI.",
            highlights = listOf(
                "👨‍💻 Developer Name: $resolvedDevName",
                "🎂 Age: $resolvedDevAge",
                if (canEditDeveloperInfo) {
                    "🏆 Role: $resolvedDevRole of Noxra AI (Owner Edit Active)"
                } else {
                    "🏆 Role: $resolvedDevRole of Noxra AI (Official Verified Profile)"
                }
            )
        )
    )

    val slide = slides[currentSlide]

    val currentAiReportText: String? = when (autoAdminEmailState) {
        is AutoAdminEmailState.SendingAutomatically -> autoAdminEmailState.aiReport
        is AutoAdminEmailState.SentAutomatically -> autoAdminEmailState.aiReport
        is AutoAdminEmailState.FallbackReady -> autoAdminEmailState.aiReport
        else -> null
    }

    BackHandler {
        if (currentSlide > 0) currentSlide -= 1 else onFinish()
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 18.dp)
                .testTag("welcome_noxra_slideshow_dialog"),
            shape = RoundedCornerShape(30.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = slide.badge,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentSlide == 0) {
                        Image(
                            painter = painterResource(id = R.drawable.img_noxra_logo),
                            contentDescription = "Noxra AI Official Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(slide.emoji, style = MaterialTheme.typography.displaySmall)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = slide.headline,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("welcome_slide_headline")
                )

                // Prominent Developer Name Banner right on Slide 1 so every new user sees Developer Name immediately
                if (currentSlide == 0) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("welcome_slide_developer_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Developer",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Developer: $resolvedDevName",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "$resolvedDevRole • Age: $resolvedDevAge",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = slide.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        slide.highlights.forEach { bullet ->
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .padding(top = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = bullet,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Live Automatic AI Admin Email Status Card on Slide 1
                if (currentSlide == 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("welcome_auto_ai_email_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                when (autoAdminEmailState) {
                                    AutoAdminEmailState.ComposingWithAi,
                                    is AutoAdminEmailState.SendingAutomatically -> {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp
                                        )
                                    }
                                    else -> {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = when (autoAdminEmailState) {
                                        AutoAdminEmailState.Idle ->
                                            "AI Instant Admin Email Ready ($NOXRA_ADMIN_EMAIL)"
                                        AutoAdminEmailState.ComposingWithAi ->
                                            "Noxra AI is composing your user info report..."
                                        is AutoAdminEmailState.SendingAutomatically ->
                                            "Automatically sending AI report to $NOXRA_ADMIN_EMAIL..."
                                        is AutoAdminEmailState.SentAutomatically ->
                                            "✅ Sent automatically via AI to $NOXRA_ADMIN_EMAIL!"
                                        is AutoAdminEmailState.FallbackReady ->
                                            "✅ AI Report Saved to Admin Cloud ($NOXRA_ADMIN_EMAIL)"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            if (currentAiReportText != null) {
                                OutlinedButton(
                                    onClick = { showAiReportPreview = !showAiReportPreview },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("toggle_ai_email_preview_button")
                                ) {
                                    Text(if (showAiReportPreview) "Hide AI User Info Report" else "View AI User Info Sent to Admin")
                                }
                                if (showAiReportPreview) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface
                                    ) {
                                        Text(
                                            text = currentAiReportText,
                                            modifier = Modifier.padding(10.dp),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (currentSlide == 0 || currentSlide == slides.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onOpenEmailClientWithReport(currentAiReportText) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("slideshow_send_admin_gmail_button")
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Gmail Copy")
                        }
                        OutlinedButton(
                            onClick = onRetryAutoAiEmail,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("slideshow_retry_ai_email_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Re-Send AI")
                        }
                    }
                }

                if (currentSlide == slides.lastIndex) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            onFinish()
                            onOpenDeveloperEditor()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("slideshow_open_dev_info_button")
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (canEditDeveloperInfo) {
                                "About & Edit Developer ($resolvedDevName • $resolvedDevAge)"
                            } else {
                                "About Developer ($resolvedDevName • $resolvedDevAge)"
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Slide indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.indices.forEach { idx ->
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (idx == currentSlide) 26.dp else 8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (idx == currentSlide) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentSlide > 0) {
                        OutlinedButton(
                            onClick = { currentSlide -= 1 },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 50.dp)
                                .testTag("slideshow_back_button")
                        ) {
                            Text("Back")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onFinish,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 50.dp)
                                .testTag("slideshow_skip_button")
                        ) {
                            Text("Enter App")
                        }
                    }

                    Button(
                        onClick = {
                            if (currentSlide < slides.lastIndex) {
                                currentSlide += 1
                            } else {
                                onFinish()
                            }
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .heightIn(min = 50.dp)
                            .testTag("slideshow_next_or_start_button")
                    ) {
                        Text(
                            text = if (currentSlide < slides.lastIndex) "Next Slide" else "Open Noxra AI",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
