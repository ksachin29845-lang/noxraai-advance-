package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.core.SpeechAndAudioManager
import com.example.data.curriculum.LanguageCurriculum
import com.example.data.curriculum.LessonNode
import com.example.data.model.CustomCourseLesson
import com.example.data.model.RegistrationEvent
import com.example.data.model.UserProfile
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.ui.components.DEFAULT_DEV_AGE
import com.example.ui.components.DEFAULT_DEV_NAME
import com.example.ui.components.DEFAULT_DEV_ROLE
import com.example.ui.components.DeveloperFooterBanner
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import com.example.core.PublicWebLinkManager
import com.example.ui.theme.GemCyanBlue
import com.example.ui.theme.HeartCrimsonRed
import com.example.ui.theme.StreakFlameOrange
import com.example.ui.theme.XpGoldYellow
import java.util.Calendar

const val NOXRA_SHARED_WEB_URL = PublicWebLinkManager.DEFAULT_PUBLIC_WEB_URL

@Composable
fun HomeScreen(
    profile: UserProfile,
    unreadNotificationsCount: Int,
    newlyRegistered: Boolean,
    registrationRecord: RegistrationEvent?,
    audioManager: SpeechAndAudioManager,
    customCourses: List<CustomCourseLesson> = emptyList(),
    onStartCustomCourse: (CustomCourseLesson) -> Unit = {},
    onClaimQuest: (questId: String, title: String, xp: Int, gems: Int) -> Unit = { _, _, _, _ -> },
    onDismissNewRegistration: () -> Unit,
    onSelectLanguage: (String) -> Unit,
    onStartLesson: (LessonNode) -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenAiTutorTab: () -> Unit,
    onOpenSpeakingLab: () -> Unit = {},
    onOpenSubjectLab: (Int) -> Unit = {},
    onOpenEnglishFiftyLevels: () -> Unit = {},
    onOpenMathFiftyLevels: () -> Unit = {},
    onOpenFriendChat: () -> Unit = {},
    onOpenNarutoSubscription: () -> Unit = {},
    onOpenAppHistory: () -> Unit = {},
    onOpenPublicWebPortal: () -> Unit = {},
    onOpenWelcomeSlideshow: () -> Unit = {},
    onFeedMascot: () -> Unit,
    onOpenDeveloperInfo: () -> Unit,
    developerName: String = DEFAULT_DEV_NAME,
    developerAge: String = DEFAULT_DEV_AGE,
    developerRole: String = DEFAULT_DEV_ROLE,
    canEditDeveloperInfo: Boolean = false,
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    var showLanguagePicker by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }
    var webLinkCopied by remember { mutableStateOf(false) }
    var activeWebUrl by remember { mutableStateOf(PublicWebLinkManager.getPublicWebUrl(context)) }
    var showEditWebLinkDialog by remember { mutableStateOf(false) }
    var draftWebUrl by remember { mutableStateOf(activeWebUrl) }
    var activeGuidebookUnit by remember { mutableStateOf<String?>(null) }
    var selectedPathTierFilter by rememberSaveable { mutableStateOf("All") }

    val activeLang = remember(profile.learningLanguage) {
        LanguageCurriculum.supportedLanguages.find { it.name == profile.learningLanguage }
            ?: LanguageCurriculum.supportedLanguages.first()
    }
    val lessons = remember(profile.learningLanguage) {
        LanguageCurriculum.lessonsForLanguage(profile.learningLanguage)
    }
    val (wordTitle, wordMeaning) = remember(profile.learningLanguage) {
        LanguageCurriculum.wordOfTheDay(profile.learningLanguage)
    }
    val (phraseText, phraseTranslation) = remember(profile.learningLanguage) {
        LanguageCurriculum.phraseOfTheDay(profile.learningLanguage)
    }
    val unlockedCount = (profile.unlockedLessonsCount ?: 2L).toInt()

    if (showLanguagePicker) {
        LanguagePickerDialog(
            currentLanguage = profile.learningLanguage,
            onSelect = { chosen ->
                showLanguagePicker = false
                onSelectLanguage(chosen)
            },
            onDismiss = { showLanguagePicker = false }
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Log Out of Noxra AI?") },
            text = {
                Text("Your XP, streaks, Levels 1–50 progress, and AI chat history are safely synced to the cloud. You can sign back in anytime.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutConfirm = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("home_confirm_logout_button")
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) {
                    Text("Stay Signed In")
                }
            }
        )
    }

    if (showEditWebLinkDialog && canEditDeveloperInfo) {
        AlertDialog(
            onDismissRequest = { showEditWebLinkDialog = false },
            title = { Text("Set Public Web App Link") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Paste your live public web link (for example from GitHub Pages or Netlify) so anyone can copy and open Noxra AI:"
                    )
                    OutlinedTextField(
                        value = draftWebUrl,
                        onValueChange = { draftWebUrl = it },
                        singleLine = true,
                        label = { Text("Public https:// Link") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeWebUrl = PublicWebLinkManager.savePublicWebUrl(context, draftWebUrl)
                        showEditWebLinkDialog = false
                    }
                ) {
                    Text("Save Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditWebLinkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    activeGuidebookUnit?.let { unitName ->
        val unitLessons = lessons.filter { it.unitTitle == unitName }
        UnitGuidebookDialog(
            unitTitle = unitName,
            languageName = activeLang.name,
            unitLessons = unitLessons,
            audioManager = audioManager,
            onDismiss = { activeGuidebookUnit = null }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Top Noxra AI Gamification & Account HUD
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_noxra_logo),
                                contentDescription = "Noxra AI Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                            )
                            // Language Course Selector Pill
                            Surface(
                                onClick = { showLanguagePicker = true },
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("language_selector_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(activeLang.flagEmoji, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = activeLang.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Quick AI Chat + Notification Bell + Log Out
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onOpenAiTutorTab,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("quick_ai_tutor_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = stringResource(R.string.cd_ai_chat_button),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = onOpenNotifications,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("open_notifications_bell_button")
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotificationsCount > 0) {
                                            Badge { Text("$unreadNotificationsCount") }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = stringResource(R.string.cd_notifications_button)
                                    )
                                }
                            }
                            IconButton(
                                onClick = { showLogoutConfirm = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("home_logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Logout,
                                    contentDescription = "Log out of Noxra AI",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

                    // Streak, XP, Gems, Hearts Gamification Stats Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HudMiniStat(Icons.Default.LocalFireDepartment, "${profile.streak}d Streak", StreakFlameOrange)
                        HudMiniStat(Icons.Default.Star, "${profile.xp} XP", XpGoldYellow)
                        Text(
                            text = "💎 ${profile.gems}",
                            style = MaterialTheme.typography.labelLarge,
                            color = GemCyanBlue,
                            fontWeight = FontWeight.ExtraBold
                        )
                        HudMiniStat(
                            Icons.Default.Favorite,
                            if (profile.relaxedMode == true) "∞" else "${profile.hearts}",
                            HeartCrimsonRed
                        )
                    }
                }
            }
        }

        // 1b. Unique Noxra AI Command Dashboard + Separate Feature Buttons Hub
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("noxra_feature_buttons_grid"),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ Noxra AI Command Hub",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Tap any separate feature button below • Cloud Synced",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenWelcomeSlideshow,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("open_welcome_slideshow_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("🎬 Welcome Tour", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Instant Copy, Open, Share & Google Search Public Web Link Bar
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_web_link_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            runCatching {
                                                context.startActivity(
                                                    Intent(Intent.ACTION_VIEW, Uri.parse(activeWebUrl))
                                                )
                                            }
                                        }
                                ) {
                                    Text(
                                        text = "🌐 Public Web Link (Google Indexable)",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = activeWebUrl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                if (canEditDeveloperInfo) {
                                    IconButton(
                                        onClick = {
                                            draftWebUrl = activeWebUrl
                                            showEditWebLinkDialog = true
                                        },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("edit_web_link_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit public web link",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                FilledTonalButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Noxra AI Web Link", activeWebUrl))
                                        webLinkCopied = true
                                    },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("copy_web_link_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Noxra AI web link",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (webLinkCopied) "Copied ✓" else "Copy")
                                }
                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Open & run Noxra AI in your browser: $activeWebUrl"
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Noxra AI Web Link"))
                                    },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("share_web_link_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Noxra AI web link",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Button(
                                onClick = onOpenPublicWebPortal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("open_public_web_portal_button"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "🔍 Public Search Directory & Google SEO Hub",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val engTierLabel = when (profile.englishTier) {
                            "Mid" -> "Medium"
                            "Advance" -> "Hard"
                            else -> profile.englishTier ?: "Basic"
                        }
                        val mathTierLabel = when (profile.mathTier) {
                            "Mid" -> "Medium"
                            "Advance" -> "Hard"
                            else -> profile.mathTier ?: "Basic"
                        }
                        NoxraFeatureActionTile(
                            emoji = "🇬🇧",
                            title = "English 1–50",
                            subtitle = "${profile.englishClass ?: "Class 8"} • $engTierLabel • Lv.${profile.englishUnlockedLevel ?: 1L}/50",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            testTag = "feature_btn_english_50",
                            onClick = onOpenEnglishFiftyLevels,
                            modifier = Modifier.weight(1f)
                        )
                        NoxraFeatureActionTile(
                            emoji = "➗",
                            title = "Math 1–50 + AI",
                            subtitle = "${profile.mathClass ?: "Class 8"} • $mathTierLabel • Lv.${profile.mathUnlockedLevel ?: 1L}/50",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            testTag = "feature_btn_math_50",
                            onClick = onOpenMathFiftyLevels,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NoxraFeatureActionTile(
                            emoji = "✨",
                            title = "Noxra AI Tutor",
                            subtitle = "Smart AI Chat & Homework",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            testTag = "feature_btn_noxra_ai",
                            onClick = onOpenAiTutorTab,
                            modifier = Modifier.weight(1f)
                        )
                        NoxraFeatureActionTile(
                            emoji = "💬",
                            title = "Friend Chat",
                            subtitle = "Search @username & Chat",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            testTag = "feature_btn_friend_chat",
                            onClick = onOpenFriendChat,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NoxraFeatureActionTile(
                            emoji = "🔥",
                            title = "Naruto Event ₹49",
                            subtitle = if (profile.subscriptionActive == true) "Active ✓ • 2x XP Boost" else "49 Rupees Subscription",
                            containerColor = StreakFlameOrange.copy(alpha = 0.20f),
                            testTag = "feature_btn_naruto_49",
                            onClick = onOpenNarutoSubscription,
                            modifier = Modifier.weight(1f)
                        )
                        NoxraFeatureActionTile(
                            emoji = "📜",
                            title = "Saved History",
                            subtitle = "All Chats & Levels Saved",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            testTag = "feature_btn_app_history",
                            onClick = onOpenAppHistory,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        NoxraFeatureActionTile(
                            emoji = "🎙️",
                            title = "Speaking Lab",
                            subtitle = "Voice Pronunciation AI",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            testTag = "feature_btn_speaking_lab",
                            onClick = onOpenSpeakingLab,
                            modifier = Modifier.weight(1f)
                        )
                        NoxraFeatureActionTile(
                            emoji = "👑",
                            title = "About Developer",
                            subtitle = "${developerName.ifBlank { DEFAULT_DEV_NAME }} • ${developerAge.ifBlank { DEFAULT_DEV_AGE }}",
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f),
                            testTag = "feature_btn_about_developer",
                            onClick = onOpenDeveloperInfo,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Optional New User Registration Celebration Card
        if (newlyRegistered && registrationRecord != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_user_registration_banner"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🎉 New User Registered on Your App!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            IconButton(onClick = onDismissNewRegistration, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss welcome notice")
                            }
                        }
                        Text(
                            text = "👤 User Name: ${registrationRecord.userName} ${registrationRecord.studentHandle?.let { "($it)" }.orEmpty()}\n" +
                                "🎓 Class / Grade: ${registrationRecord.studentClass ?: profile.studentClass ?: "General Learner"}\n" +
                                "📧 Email Address: ${registrationRecord.email}\n" +
                                "🆔 User ID: ${registrationRecord.userId}\n" +
                                "📅 Registration Date: ${registrationRecord.registrationDate} • 🕒 ${registrationRecord.registrationTime}\n" +
                                "🌐 Registration Method: ${registrationRecord.registrationMethod}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // 2. Adventure World Map Hero Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(176.dp)) {
                    Image(
                        painter = painterResource(id = R.drawable.img_world_adventure_banner),
                        contentDescription = stringResource(R.string.cd_adventure_banner),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color(0xCC06281E)
                                    )
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activeLang.flagEmoji} ${activeLang.name} Adventure World",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "Fluency Score: ${profile.fluencyScore ?: 20}% • ${profile.lessonsCompleted} Lessons Completed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            FilledTonalButton(
                                onClick = onOpenAiTutorTab,
                                modifier = Modifier.heightIn(min = 48.dp)
                            ) {
                                Text("✨ Noxra AI")
                            }
                        }
                    }
                }
            }
        }

        // 2b. Student Progress Dashboard, Weekly Activity Chart & Next Lesson Hub
        item {
            val nextLesson = lessons.firstOrNull { it.index == unlockedCount } ?: lessons.last()
            val completedNodesInPath = (unlockedCount - 1).coerceIn(0, lessons.size)
            val completionPct = ((completedNodesInPath * 100f) / lessons.size.coerceAtLeast(1)).toInt().coerceIn(0, 100)
            val currentLeague = LanguageCurriculum.leagueForXp(profile.xp)
            val bestStreak = maxOf(profile.longestStreak ?: 1L, profile.streak)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_progress_dashboard_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "👋 Welcome, ${profile.displayName} • ${profile.username ?: "@learner"}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${profile.studentClass ?: "All Courses"} • ${profile.skillLevel} • Goal: ${profile.dailyGoalMinutes}m/day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = { onStartLesson(nextLesson) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("dashboard_next_lesson_button")
                        ) {
                            Text("▶ Continue")
                        }
                    }

                    // Course Completion Bar + League & Longest Streak
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeLang.name} Course Progress: $completionPct%",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "🏆 $currentLeague League • Best 🔥 ${bestStreak}d",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { (completionPct / 100f).coerceIn(0.04f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(9.dp)
                                .clip(CircleShape)
                        )
                    }

                    if (completedNodesInPath >= lessons.size && lessons.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = "🎓 Certificate Unlocked: ${activeLang.name} Path Completed by ${profile.displayName}!",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("💪 Strength", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                Text("${activeLang.name} Core Vocab", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("🔁 Recommended Next", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                                Text(nextLesson.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, maxLines = 1)
                            }
                        }
                    }

                    // 7-Day Weekly Learning Activity Bar Chart
                    WeeklyActivityMiniChart(
                        streak = profile.streak.toInt(),
                        lessonsCompleted = profile.lessonsCompleted.toInt(),
                        xp = profile.xp.toInt()
                    )
                }
            }
        }

        // 2c. Daily Quests Quick-Claim Card
        item {
            val claimed = profile.claimedQuestIds.orEmpty()
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_daily_quests_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "🎯 Daily Learning Quests",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    HomeQuestClaimRow(
                        questId = "quest_xp_150",
                        emoji = "⚡",
                        title = "Reach 150 Total XP",
                        current = profile.xp.toInt().coerceAtMost(150),
                        target = 150,
                        xpReward = 25,
                        gemReward = 10,
                        isClaimed = claimed.contains("quest_xp_150"),
                        onClaim = { onClaimQuest("quest_xp_150", "XP Sprinter", 25, 10) }
                    )
                    HomeQuestClaimRow(
                        questId = "quest_lessons_3",
                        emoji = "📚",
                        title = "Complete 3 Lessons",
                        current = profile.lessonsCompleted.toInt().coerceAtMost(3),
                        target = 3,
                        xpReward = 35,
                        gemReward = 15,
                        isClaimed = claimed.contains("quest_lessons_3"),
                        onClaim = { onClaimQuest("quest_lessons_3", "Path Explorer", 35, 15) }
                    )
                    HomeQuestClaimRow(
                        questId = "quest_streak_7",
                        emoji = "🔥",
                        title = "7-Day Study Streak",
                        current = profile.streak.toInt().coerceAtMost(7),
                        target = 7,
                        xpReward = 50,
                        gemReward = 25,
                        isClaimed = claimed.contains("quest_streak_7"),
                        onClaim = { onClaimQuest("quest_streak_7", "Consistency Champion", 50, 25) }
                    )
                }
            }
        }

        // 2d. Multi-Subject Expansion: School Subjects, Maths, Music & Chess Quick Launch
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("beyond_languages_hub_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "🚀 Beyond Languages: School Subjects, Maths, Music & Chess",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onOpenSubjectLab(0) },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("home_open_maths_button")
                        ) {
                            Text("🧮 Maths")
                        }
                        FilledTonalButton(
                            onClick = { onOpenSubjectLab(1) },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("home_open_music_button")
                        ) {
                            Text("🎵 Music")
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { onOpenSubjectLab(2) },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("home_open_chess_button")
                        ) {
                            Text("♟️ Chess")
                        }
                        FilledTonalButton(
                            onClick = { onOpenSubjectLab(3) },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("home_open_subjects_button")
                        ) {
                            Text("🏫 Subjects")
                        }
                    }
                }
            }
        }

        // 2e. Educator & Admin Custom Lessons (When Published in Cloud Firestore)
        if (customCourses.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_custom_courses_section"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "🎓 Educator & Custom Lessons",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        customCourses.take(5).forEach { course ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onStartCustomCourse(course) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${course.title} (${course.subjectOrLanguage})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${course.level}${course.studentClass?.let { " • $it" }.orEmpty()} • +${course.xpReward} XP",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = course.objective,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    OutlinedButton(
                                        onClick = { onStartCustomCourse(course) },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Text("Start")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Virtual Learning Companion "Kavi" Interactive Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_kavi_mascot),
                        contentDescription = stringResource(R.string.cd_mascot_image),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(74.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🐣 ${profile.mascotName ?: "Kavi"} • Lv.${profile.mascotLevel ?: 1} (${profile.mascotOutfit ?: "Explorer Scarf"})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "\"${activeLang.greeting}\" — Complete today's path node to keep your ${profile.streak}-day streak blazing!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        FilledTonalButton(
                            onClick = onFeedMascot,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("feed_mascot_button")
                        ) {
                            Text("🍓 Feed Berry Treat (25 💎)")
                        }
                    }
                }
            }
        }

        // 3b. AI Speaking Lab Quick-Launch Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenSpeakingLab() }
                    .testTag("open_speaking_lab_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = "Open AI Speaking Lab",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "🎙️ AI Speaking Lab • Live Gemini Coach",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                text = "Record your ${activeLang.name} pronunciation for real-time acoustic & word-by-word feedback.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenSpeakingLab,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("launch_speaking_lab_button")
                    ) {
                        Text("Speak")
                    }
                }
            }
        }

        // 4. Daily Coach: Word & Phrase of the Day
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 Daily ${activeLang.name} Coach",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { audioManager.speak("$wordTitle. $phraseText", activeLang.name, false) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen to Word of the Day")
                        }
                    }
                    Text("🌟 Word of the Day: $wordTitle", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(wordMeaning, style = MaterialTheme.typography.bodySmall)
                    Text("💬 Phrase: \"$phraseText\"", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text(phraseTranslation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // 5. Interactive Stepping-Stone Learning Path (Basic → Medium → Hard)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "🗺️ Interactive Learning Path",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "All" to "🌍 All Units",
                        "Basic" to "🌱 Basic",
                        "Medium" to "⚡ Medium",
                        "Hard" to "🔥 Hard"
                    ).forEach { (filterKey, label) ->
                        FilterChip(
                            selected = selectedPathTierFilter == filterKey,
                            onClick = { selectedPathTierFilter = filterKey },
                            label = { Text(label) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("home_path_filter_${filterKey.lowercase()}")
                        )
                    }
                }
            }
        }

        val filteredLessons = lessons.filter { node ->
            when (selectedPathTierFilter) {
                "Basic" -> node.tier.equals("Beginner", ignoreCase = true) || node.tier.equals("Basic", ignoreCase = true)
                "Medium" -> node.tier.equals("Intermediate", ignoreCase = true) || node.tier.equals("Mid", ignoreCase = true)
                "Hard" -> node.tier.equals("Advanced", ignoreCase = true) || node.tier.equals("Advance", ignoreCase = true)
                else -> true
            }
        }

        filteredLessons.forEachIndexed { idx, node ->
            val isUnlocked = node.index <= unlockedCount
            val isCompleted = node.index < unlockedCount
            val horizontalOffset = when (idx % 4) {
                1 -> 26.dp
                3 -> (-26).dp
                else -> 0.dp
            }
            val friendlyTierLabel = when {
                node.tier.equals("Beginner", ignoreCase = true) || node.tier.equals("Basic", ignoreCase = true) -> "🌱 Basic Level"
                node.tier.equals("Intermediate", ignoreCase = true) || node.tier.equals("Mid", ignoreCase = true) -> "⚡ Medium Level"
                node.tier.equals("Advanced", ignoreCase = true) || node.tier.equals("Advance", ignoreCase = true) -> "🔥 Hard Level"
                else -> "${node.tier} Level"
            }

            item(key = node.id) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (idx == 0 || filteredLessons[idx - 1].unitTitle != node.unitTitle) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = node.unitTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = friendlyTierLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                                FilledTonalButton(
                                    onClick = { activeGuidebookUnit = node.unitTitle },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("unit_guidebook_button_$idx")
                                ) {
                                    Text("📘 Guidebook")
                                }
                            }
                        }
                    }

                    Card(
                        onClick = { if (isUnlocked) onStartLesson(node) },
                        enabled = isUnlocked,
                        modifier = Modifier
                            .fillMaxWidth(0.90f)
                            .offset(x = horizontalOffset)
                            .testTag("lesson_node_${node.id}"),
                        shape = RoundedCornerShape(24.dp),
                        border = if (isUnlocked && !isCompleted) {
                            BorderStroke(2.5.dp, MaterialTheme.colorScheme.primary)
                        } else null,
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isCompleted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                isUnlocked -> MaterialTheme.colorScheme.surface
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 4.dp else 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isUnlocked) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = node.iconEmoji,
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            text = node.category,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Text(
                                        text = "+${node.xpReward} XP • +${node.gemReward} 💎",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = node.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = node.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = when {
                                    isCompleted -> Icons.Default.CheckCircle
                                    isUnlocked -> Icons.Default.PlayArrow
                                    else -> Icons.Default.Lock
                                },
                                contentDescription = if (isUnlocked) "Start lesson" else "Locked lesson",
                                tint = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }

        // 6. Prominent Developer Info Card at Bottom of Home
        item {
            Spacer(modifier = Modifier.height(8.dp))
            DeveloperFooterBanner(
                onOpenDeveloperModal = onOpenDeveloperInfo,
                developerName = developerName,
                developerAge = developerAge,
                developerRole = developerRole,
                canEditDeveloperInfo = canEditDeveloperInfo
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun NoxraFeatureActionTile(
    emoji: String,
    title: String,
    subtitle: String,
    containerColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = modifier
            .heightIn(min = 68.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun WeeklyActivityMiniChart(
    streak: Int,
    lessonsCompleted: Int,
    xp: Int
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")
    val todayIdx = remember {
        val dow = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) // Sun=1..Sat=7
        if (dow == Calendar.SUNDAY) 6 else dow - 2
    }
    val activeDaysCount = streak.coerceIn(1, 7)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📈 Weekly Learning Activity",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$lessonsCompleted lessons • $xp XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dayLabels.forEachIndexed { idx, day ->
                    val distanceFromToday = (todayIdx - idx + 7) % 7
                    val isActive = distanceFromToday < activeDaysCount
                    val isToday = idx == todayIdx
                    val barFraction = when {
                        isToday -> (0.45f + (xp % 60) / 100f).coerceIn(0.45f, 1f)
                        isActive -> (0.35f + ((idx * 13 + lessonsCompleted * 7) % 50) / 100f).coerceIn(0.35f, 0.9f)
                        else -> 0.14f
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((34 * barFraction).dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    when {
                                        isToday -> MaterialTheme.colorScheme.primary
                                        isActive -> StreakFlameOrange.copy(alpha = 0.8f)
                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isToday) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeQuestClaimRow(
    questId: String,
    emoji: String,
    title: String,
    current: Int,
    target: Int,
    xpReward: Int,
    gemReward: Int,
    isClaimed: Boolean,
    onClaim: () -> Unit
) {
    val done = current >= target
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "$emoji $title ($current/$target)",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (current.toFloat() / target.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(6.dp)
                    .clip(CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        when {
            isClaimed -> {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = "Claimed ✓",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            done -> {
                FilledTonalButton(
                    onClick = onClaim,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("home_claim_$questId")
                ) {
                    Text("Claim +$xpReward XP")
                }
            }
            else -> {
                Text(
                    text = "+$xpReward XP • +$gemReward💎",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun HudMiniStat(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    tint: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = tint,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun LanguagePickerDialog(
    currentLanguage: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        BackHandler { onDismiss() }
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("🌍 Choose Course Language", style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier.heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(LanguageCurriculum.supportedLanguages.size) { index ->
                        val lang = LanguageCurriculum.supportedLanguages[index]
                        val selected = lang.name == currentLanguage
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(lang.name) }
                                .testTag("pick_lang_${lang.name.lowercase()}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(lang.flagEmoji, style = MaterialTheme.typography.headlineSmall)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(lang.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("${lang.nativeName} • ${lang.scriptSample}", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                if (selected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Selected language",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
private fun UnitGuidebookDialog(
    unitTitle: String,
    languageName: String,
    unitLessons: List<LessonNode>,
    audioManager: SpeechAndAudioManager,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        BackHandler { onDismiss() }
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "📘 $unitTitle",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Unit Guidebook • Key $languageName Phrases & Grammar Tips",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                LazyColumn(
                    modifier = Modifier.heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sampleQuestions = unitLessons.flatMap { it.questions }.take(5)
                    items(sampleQuestions.size) { i ->
                        val q = sampleQuestions[i]
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${q.keyVocabWord} → ${q.keyVocabMeaning}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = q.explanation,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(
                                    onClick = { audioManager.speak(q.audioPhrase, languageName, false) },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Pronounce key phrase")
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                ) {
                    Text("Got It!")
                }
            }
        }
    }
}
