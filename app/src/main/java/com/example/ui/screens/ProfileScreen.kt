package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.curriculum.LanguageCurriculum
import com.example.data.model.CommunityPost
import com.example.data.model.RegistrationEvent
import com.example.data.model.UserProfile
import com.example.data.repository.AppPreferencesState
import com.example.ui.components.DeveloperFooterBanner

import android.net.Uri
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import com.example.core.NOXRA_ADMIN_EMAIL
import com.example.ui.components.DEFAULT_DEV_AGE
import com.example.ui.components.DEFAULT_DEV_NAME
import com.example.ui.components.DEFAULT_DEV_ROLE
import com.example.ui.components.DeveloperProfileCard
import com.example.ui.viewmodel.AiTaskState

@Composable
fun ProfileScreen(
    profile: UserProfile,
    preferences: AppPreferencesState,
    registrationEvent: RegistrationEvent?,
    isAdmin: Boolean,
    adminRegistrations: List<RegistrationEvent>,
    reportedPosts: List<CommunityPost> = emptyList(),
    aiRegistrationEmailState: AiTaskState<String> = AiTaskState.Idle,
    onGenerateAiRegistrationEmail: () -> Unit = {},
    onSaveProfile: (
        displayName: String,
        bio: String,
        age: Long?,
        nativeLanguage: String,
        learningLanguage: String,
        skillLevel: String,
        dailyGoalMinutes: Long,
        relaxedMode: Boolean,
        username: String,
        studentClass: String,
        leaderboardPrivacy: Boolean
    ) -> Unit,
    onSetThemeMode: (String) -> Unit,
    onSetAccentPreset: (String) -> Unit,
    onSetTextScale: (Float) -> Unit,
    onSetSoundEffects: (Boolean) -> Unit,
    onOpenNotifications: () -> Unit,
    onRefreshAdminRegistrations: () -> Unit,
    onRefreshReportedPosts: () -> Unit = {},
    onModeratePost: (postId: String, restoreActive: Boolean) -> Unit = { _, _ -> },
    onPublishCustomCourse: (
        title: String,
        subjectOrLanguage: String,
        level: String,
        studentClass: String?,
        objective: String,
        questionPrompt: String,
        options: List<String>,
        correctAnswer: String,
        explanation: String,
        xpReward: Long
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onBroadcastSystemAlert: (title: String, body: String) -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccountData: () -> Unit,
    onOpenDeveloperInfo: () -> Unit,
    onOpenPublicWebPortal: () -> Unit = {},
    developerName: String = DEFAULT_DEV_NAME,
    developerAge: String = DEFAULT_DEV_AGE,
    developerRole: String = DEFAULT_DEV_ROLE,
    onShowMessage: (String) -> Unit
) {
    var selectedSection by rememberSaveable { mutableIntStateOf(0) }
    val sections = listOf("👤 Profile & Goals", "🎨 Customize & Security", "🎉 Registration & Admin")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sections.forEachIndexed { idx, title ->
                FilterChip(
                    selected = selectedSection == idx,
                    onClick = { selectedSection = idx },
                    label = { Text(title) },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("profile_tab_$idx")
                )
            }
        }

        when (selectedSection) {
            0 -> ProfileAndGoalsPane(
                profile = profile,
                isAdmin = isAdmin,
                developerName = developerName,
                developerAge = developerAge,
                developerRole = developerRole,
                onSaveProfile = onSaveProfile,
                onSignOut = onSignOut,
                onOpenDeveloperInfo = onOpenDeveloperInfo,
                onOpenPublicWebPortal = onOpenPublicWebPortal,
                onShowMessage = onShowMessage
            )
            1 -> CustomizeAndSecurityPane(
                profile = profile,
                isAdmin = isAdmin,
                developerName = developerName,
                developerAge = developerAge,
                developerRole = developerRole,
                preferences = preferences,
                onSetThemeMode = onSetThemeMode,
                onSetAccentPreset = onSetAccentPreset,
                onSetTextScale = onSetTextScale,
                onSetSoundEffects = onSetSoundEffects,
                onOpenNotifications = onOpenNotifications,
                onSignOut = onSignOut,
                onDeleteAccountData = onDeleteAccountData,
                onOpenDeveloperInfo = onOpenDeveloperInfo,
                onShowMessage = onShowMessage
            )
            2 -> RegistrationAndAdminPane(
                profile = profile,
                registrationEvent = registrationEvent,
                isAdmin = isAdmin,
                developerName = developerName,
                developerAge = developerAge,
                developerRole = developerRole,
                adminRegistrations = adminRegistrations,
                reportedPosts = reportedPosts,
                aiRegistrationEmailState = aiRegistrationEmailState,
                onGenerateAiRegistrationEmail = onGenerateAiRegistrationEmail,
                onRefreshAdminRegistrations = onRefreshAdminRegistrations,
                onRefreshReportedPosts = onRefreshReportedPosts,
                onModeratePost = onModeratePost,
                onPublishCustomCourse = onPublishCustomCourse,
                onBroadcastSystemAlert = onBroadcastSystemAlert,
                onOpenDeveloperInfo = onOpenDeveloperInfo,
                onShowMessage = onShowMessage
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ProfileAndGoalsPane(
    profile: UserProfile,
    isAdmin: Boolean,
    developerName: String,
    developerAge: String,
    developerRole: String,
    onSaveProfile: (
        displayName: String,
        bio: String,
        age: Long?,
        nativeLanguage: String,
        learningLanguage: String,
        skillLevel: String,
        dailyGoalMinutes: Long,
        relaxedMode: Boolean,
        username: String,
        studentClass: String,
        leaderboardPrivacy: Boolean
    ) -> Unit,
    onSignOut: () -> Unit,
    onOpenDeveloperInfo: () -> Unit,
    onOpenPublicWebPortal: () -> Unit = {},
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out of Noxra AI?") },
            text = {
                Text("Your XP, streaks, Levels 1–50 progress, and AI chat history are safely stored in Cloud Firestore.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("profile_confirm_logout_button")
                ) {
                    Text("Log Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    var displayName by remember(profile.displayName) { mutableStateOf(profile.displayName) }
    var usernameText by remember(profile.username) { mutableStateOf(profile.username ?: "") }
    var studentClassText by remember(profile.studentClass) { mutableStateOf(profile.studentClass ?: "") }
    var bio by remember(profile.bio) { mutableStateOf(profile.bio ?: "") }
    var ageText by remember(profile.age) { mutableStateOf(profile.age?.toString() ?: "") }
    var nativeLang by remember(profile.nativeLanguage) { mutableStateOf(profile.nativeLanguage) }
    var learningLang by remember(profile.learningLanguage) { mutableStateOf(profile.learningLanguage) }
    var skillLevel by remember(profile.skillLevel) { mutableStateOf(profile.skillLevel) }
    var dailyGoal by remember(profile.dailyGoalMinutes) { mutableStateOf(profile.dailyGoalMinutes) }
    var relaxedMode by remember(profile.relaxedMode) { mutableStateOf(profile.relaxedMode == true) }
    var leaderboardPrivacy by remember(profile.leaderboardPrivacy) { mutableStateOf(profile.leaderboardPrivacy == true) }

    val allLanguages = remember { LanguageCurriculum.supportedLanguages.map { it.name } }
    val skillLevels = listOf("Beginner", "Intermediate", "Advanced")
    val goalOptions = listOf(5L, 10L, 15L, 20L, 30L)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("profile_goals_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = profile.displayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = profile.displayName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Cloud Synced Account",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "${profile.username?.let { "$it • " }.orEmpty()}${profile.email}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "${profile.studentClass?.let { "$it • " }.orEmpty()}${profile.nativeLanguage} → ${profile.learningLanguage} (${profile.skillLevel})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(
                            onClick = {
                                val shareBody = "I'm learning ${profile.learningLanguage} & Math Levels 1–50 on Noxra AI! 🔥 ${profile.streak}-day streak • ⚡ ${profile.xp} XP • Fluency ${profile.fluencyScore ?: 20}%!"
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareBody)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Learning Progress"))
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("share_progress_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share learning progress"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    val fluency = (profile.fluencyScore ?: 20L).coerceIn(0L, 100L)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Estimated ${profile.learningLanguage} Fluency",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$fluency%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { fluency / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatBadgeChip("⚡ ${profile.xp} XP")
                        StatBadgeChip("🔥 ${profile.streak}d Streak")
                        StatBadgeChip("💎 ${profile.gems} Gems")
                        StatBadgeChip("📚 ${profile.lessonsCompleted} Done")
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onOpenPublicWebPortal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("profile_open_public_web_portal_button")
                    ) {
                        Text("🌐 Public Web Link & Google Search Hub", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { showLogoutDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("profile_top_logout_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Log Out of Noxra AI", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Achievement Badges + Academic Progress Analytics
        item {
            val bestStreak = maxOf(profile.longestStreak ?: 1L, profile.streak)
            val questsDoneCount = profile.claimedQuestIds?.size ?: 0
            Card(shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🏅 Achievement Badges",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AchievementBadgePill("🌟 First Step", unlocked = profile.lessonsCompleted >= 1)
                        AchievementBadgePill("🔥 3-Day Flame", unlocked = profile.streak >= 3)
                        AchievementBadgePill("⚡ 250 XP Scholar", unlocked = profile.xp >= 250)
                        AchievementBadgePill("🦉 Kavi Buddy Lv.2", unlocked = (profile.mascotLevel ?: 1) >= 2)
                        AchievementBadgePill("🎯 Quest Finisher", unlocked = questsDoneCount >= 1)
                        AchievementBadgePill("🏆 1000 XP Master", unlocked = profile.xp >= 1000)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(
                        modifier = Modifier.testTag("academic_analytics_card"),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "📈 Academic Analytics • Longest Streak: ${bestStreak}d • Quests Claimed: $questsDoneCount • League: ${LanguageCurriculum.leagueForXp(profile.xp)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Educational Note: Fluency percentages and XP scores are learning progress indicators designed for practice motivation, not official school examination grades.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Editable Profile & Learning Goals Form
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "✏️ Edit Profile & Learning Path",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { if (it.length <= 80) displayName = it },
                        label = { Text("Student Full Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_name_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = usernameText,
                        onValueChange = { if (it.length <= 32) usernameText = it },
                        label = { Text("Unique @username (for Login & Leaderboard)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_username_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = studentClassText,
                        onValueChange = { if (it.length <= 36) studentClassText = it },
                        label = { Text("Class / Grade (e.g., Class 8, Grade 10)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_class_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { if (it.length <= 280) bio = it },
                        label = { Text("Learner Bio") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_bio_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() } && input.length <= 3) ageText = input
                        },
                        label = { Text("Age (optional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_age_input")
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Native Language", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allLanguages.forEach { lang ->
                            FilterChip(
                                selected = nativeLang == lang,
                                onClick = { nativeLang = lang },
                                label = { Text(lang) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Learning Language", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allLanguages.forEach { lang ->
                            FilterChip(
                                selected = learningLang == lang,
                                onClick = { learningLang = lang },
                                label = { Text(lang) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Skill Level", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        skillLevels.forEach { lvl ->
                            FilterChip(
                                selected = skillLevel == lvl,
                                onClick = { skillLevel = lvl },
                                label = { Text(lvl) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Daily Study Goal (Minutes)", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        goalOptions.forEach { mins ->
                            FilterChip(
                                selected = dailyGoal == mins,
                                onClick = { dailyGoal = mins },
                                label = { Text("${mins}m / day") },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "♾️ Relaxed Learning Mode (Unlimited Hearts)",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Learn without losing hearts on mistakes.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = relaxedMode,
                            onCheckedChange = { relaxedMode = it },
                            modifier = Modifier.testTag("relaxed_mode_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "🕵️ Private Leaderboard Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hide my @username and XP from public league rankings.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = leaderboardPrivacy,
                            onCheckedChange = { leaderboardPrivacy = it },
                            modifier = Modifier.testTag("leaderboard_privacy_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            onSaveProfile(
                                displayName.ifBlank { "Learner" },
                                bio,
                                ageText.toLongOrNull(),
                                nativeLang,
                                learningLang,
                                skillLevel,
                                dailyGoal,
                                relaxedMode,
                                usernameText,
                                studentClassText,
                                leaderboardPrivacy
                            )
                            onShowMessage("Saving profile changes to Cloud Firestore...")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                            .testTag("save_profile_button")
                    ) {
                        Text("Save & Sync Profile across Devices")
                    }
                }
            }
        }

        // Dedicated Developer Profile Card (Sachin Kumar)
        item {
            DeveloperProfileCard(
                developerName = developerName,
                developerAge = developerAge,
                developerRole = developerRole,
                canEditDeveloperInfo = isAdmin,
                onOpenFullStory = onOpenDeveloperInfo
            )
        }

        item {
            DeveloperFooterBanner(
                onOpenDeveloperModal = onOpenDeveloperInfo,
                developerName = developerName,
                developerAge = developerAge,
                developerRole = developerRole,
                canEditDeveloperInfo = isAdmin
            )
        }
    }
}

@Composable
private fun StatBadgeChip(label: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.75f)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun AchievementBadgePill(title: String, unlocked: Boolean) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (unlocked) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        }
    ) {
        Text(
            text = if (unlocked) "$title ✓" else "🔒 $title",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (unlocked) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CustomizeAndSecurityPane(
    profile: UserProfile,
    isAdmin: Boolean,
    developerName: String,
    developerAge: String,
    developerRole: String,
    preferences: AppPreferencesState,
    onSetThemeMode: (String) -> Unit,
    onSetAccentPreset: (String) -> Unit,
    onSetTextScale: (Float) -> Unit,
    onSetSoundEffects: (Boolean) -> Unit,
    onOpenNotifications: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccountData: () -> Unit,
    onOpenDeveloperInfo: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Cloud Account Data?") },
            text = {
                Text("This permanently deletes your Noxra AI profile, vocabulary notebook, history, notifications, and leaderboard record, then signs you out.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteAccountData()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_account_button")
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("customize_security_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🎨 App Customization & Accessibility",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Display Theme", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("System", "Light", "Dark").forEach { mode ->
                            FilterChip(
                                selected = preferences.themeMode == mode,
                                onClick = { onSetThemeMode(mode) },
                                label = { Text(mode) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("theme_mode_$mode")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Color Accent Palette", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Emerald", "Ocean", "Royal", "Sunset").forEach { accent ->
                            FilterChip(
                                selected = preferences.accentPreset == accent,
                                onClick = { onSetAccentPreset(accent) },
                                label = { Text(accent) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("accent_preset_$accent")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Reading Font Size (${String.format("%.2f", preferences.textScale)}x)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = preferences.textScale,
                        onValueChange = onSetTextScale,
                        valueRange = 0.85f..1.25f,
                        modifier = Modifier.testTag("text_scale_slider")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("🔊 Interactive Sound Effects", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = preferences.soundEffectsEnabled,
                            onCheckedChange = onSetSoundEffects,
                            modifier = Modifier.testTag("sound_effects_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("open_notif_settings_from_profile")
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Smart Notification Preferences & Quiet Hours")
                    }
                }
            }
        }

        // Transparent Plan Info
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "✨ Noxra AI Access & Naruto Event ₹49",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (profile.subscriptionActive == true) {
                            "🔥 Naruto Event — ₹49 Subscription ACTIVE! Enjoy 2x XP across English & Math Levels 1–50, Unlimited Hearts, and Hokage Cloak."
                        } else {
                            "English & Math Levels 1–50, Noxra AI Tutor, Friend @username Chat, and Speaking Lab are unlocked! Optional Naruto Event ₹49 pass available on Dashboard."
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Security, Export & Sign Out
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🔒 Privacy, Data Export & Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            val exportData = """
                                Noxra AI Personal Data Export
                                Name: ${profile.displayName} (${profile.username ?: "@learner"})
                                Email: ${profile.email}
                                UID: ${profile.userId}
                                English Path: ${profile.englishClass ?: "Class 8"} (${profile.englishTier ?: "Basic"}) Lv.${profile.englishUnlockedLevel ?: 1}/50
                                Math Path: ${profile.mathClass ?: "Class 8"} (${profile.mathTier ?: "Basic"}) Lv.${profile.mathUnlockedLevel ?: 1}/50
                                Subscription: ${profile.subscriptionPlan ?: "FREE"}
                                XP: ${profile.xp} | Streak: ${profile.streak} days | Gems: ${profile.gems}
                            """.trimIndent()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Noxra AI Export", exportData))
                            onShowMessage("📋 Personal learning data summary copied to clipboard!")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("export_data_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Export Personal Learning Data")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onSignOut,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out of Google Account")
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("delete_account_button")
                    ) {
                        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Cloud Account Data")
                    }
                }
            }
        }

        item {
            DeveloperFooterBanner(
                onOpenDeveloperModal = onOpenDeveloperInfo,
                developerName = developerName,
                developerAge = developerAge,
                developerRole = developerRole,
                canEditDeveloperInfo = isAdmin
            )
        }
    }
}

@Composable
private fun RegistrationAndAdminPane(
    profile: UserProfile,
    registrationEvent: RegistrationEvent?,
    isAdmin: Boolean,
    developerName: String,
    developerAge: String,
    developerRole: String,
    adminRegistrations: List<RegistrationEvent>,
    reportedPosts: List<CommunityPost>,
    aiRegistrationEmailState: AiTaskState<String>,
    onGenerateAiRegistrationEmail: () -> Unit,
    onRefreshAdminRegistrations: () -> Unit,
    onRefreshReportedPosts: () -> Unit,
    onModeratePost: (postId: String, restoreActive: Boolean) -> Unit,
    onPublishCustomCourse: (
        title: String,
        subjectOrLanguage: String,
        level: String,
        studentClass: String?,
        objective: String,
        questionPrompt: String,
        options: List<String>,
        correctAnswer: String,
        explanation: String,
        xpReward: Long
    ) -> Unit,
    onBroadcastSystemAlert: (title: String, body: String) -> Unit,
    onOpenDeveloperInfo: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var alertTitle by rememberSaveable { mutableStateOf("") }
    var alertBody by rememberSaveable { mutableStateOf("") }

    var courseTitle by rememberSaveable { mutableStateOf("") }
    var courseSubject by rememberSaveable { mutableStateOf("English") }
    var courseLevel by rememberSaveable { mutableStateOf("Beginner") }
    var courseTargetClass by rememberSaveable { mutableStateOf("Class 8") }
    var courseObjective by rememberSaveable { mutableStateOf("") }
    var coursePrompt by rememberSaveable { mutableStateOf("") }
    var optA by rememberSaveable { mutableStateOf("") }
    var optB by rememberSaveable { mutableStateOf("") }
    var optC by rememberSaveable { mutableStateOf("") }
    var optD by rememberSaveable { mutableStateOf("") }
    var correctIdx by rememberSaveable { mutableIntStateOf(0) }
    var courseExplanation by rememberSaveable { mutableStateOf("") }

    val regName = registrationEvent?.userName?.ifBlank { null } ?: profile.displayName
    val regUsername = registrationEvent?.studentHandle?.ifBlank { null } ?: profile.username ?: "@learner"
    val regClass = registrationEvent?.studentClass?.ifBlank { null } ?: profile.studentClass ?: "General Learner"
    val regAge = (registrationEvent?.studentAge ?: profile.age)?.let { "$it years" } ?: "Verified Student"
    val regTier = registrationEvent?.selectedTier?.ifBlank { null } ?: profile.englishTier ?: "Basic"
    val regLanguages = "${registrationEvent?.nativeLanguage ?: profile.nativeLanguage} → ${registrationEvent?.learningLanguage ?: profile.learningLanguage}"
    val regEmail = registrationEvent?.email?.ifBlank { null } ?: profile.email
    val regUid = registrationEvent?.userId?.ifBlank { null } ?: profile.userId
    val regDate = registrationEvent?.registrationDate?.ifBlank { null } ?: profile.registrationDate ?: "Verified Cloud Account"
    val regTime = registrationEvent?.registrationTime?.ifBlank { null } ?: profile.registrationTime ?: "Active Session"
    val regMethod = registrationEvent?.registrationMethod?.ifBlank { null } ?: profile.registrationMethod ?: "Google"
    val regAutoEmailStatus = registrationEvent?.autoEmailStatus?.ifBlank { null } ?: "Sent automatically via Noxra AI"
    val savedAiSummary = registrationEvent?.aiAdminEmailSummary?.ifBlank { null }

    val defaultReceiptBody = savedAiSummary ?: """
        🎉 New Student Registered on Noxra AI!
        Full Name: $regName
        Username: $regUsername
        Age: $regAge
        Class / Grade: $regClass
        Starting Tier: $regTier
        Languages: $regLanguages
        Email Address: $regEmail
        User ID: $regUid
        Registration Date: $regDate
        Registration Time: $regTime
        Registration Method: $regMethod
    """.trimIndent()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("registration_admin_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Exact New User Registration Alert Card + AI Gmail Dispatch to ksachin29845@gmail.com
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🎉 New User Registered on Your App!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Automatic AI Dispatch → $NOXRA_ADMIN_EMAIL",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    RegistrationDetailRow("Full Name", regName)
                    RegistrationDetailRow("Username", regUsername)
                    RegistrationDetailRow("Age", regAge)
                    RegistrationDetailRow("Class / Grade", regClass)
                    RegistrationDetailRow("Starting Tier", regTier)
                    RegistrationDetailRow("Languages", regLanguages)
                    RegistrationDetailRow("Email Address", regEmail)
                    RegistrationDetailRow("User ID", regUid)
                    RegistrationDetailRow("Registration Date", regDate)
                    RegistrationDetailRow("Registration Time", regTime)
                    RegistrationDetailRow("Auto AI Email", regAutoEmailStatus)

                    if (savedAiSummary != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "🤖 AI Report Automatically Dispatched to Admin:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = savedAiSummary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = onGenerateAiRegistrationEmail,
                        enabled = aiRegistrationEmailState !is AiTaskState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("generate_ai_reg_email_button")
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("✨ Compose AI Registration Report")
                    }

                    when (aiRegistrationEmailState) {
                        is AiTaskState.Loading -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                        is AiTaskState.Error -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = aiRegistrationEmailState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        is AiTaskState.Success -> {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                            ) {
                                Text(
                                    text = aiRegistrationEmailState.data,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        AiTaskState.Idle -> {}
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val bodyToSend = (aiRegistrationEmailState as? AiTaskState.Success)?.data ?: defaultReceiptBody
                            try {
                                val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:$NOXRA_ADMIN_EMAIL")
                                    putExtra(Intent.EXTRA_SUBJECT, "Noxra AI New Student Registration: $regName ($regUsername)")
                                    putExtra(Intent.EXTRA_TEXT, bodyToSend)
                                }
                                context.startActivity(Intent.createChooser(mailIntent, "Send Registration Alert to Admin Gmail"))
                            } catch (_: Exception) {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, bodyToSend)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Registration Report"))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("send_gmail_admin_alert_button")
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Send Alert to $NOXRA_ADMIN_EMAIL")
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Registration Receipt", defaultReceiptBody))
                            onShowMessage("📋 Registration details copied to clipboard!")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("copy_registration_receipt_button")
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Registration Alert Details")
                    }
                }
            }
        }

        // Admin Panel Card
        item {
            Card(shape = RoundedCornerShape(22.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = "Admin Panel",
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "🛠️ Noxra AI Admin Dashboard",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (isAdmin) {
                                        "Admin Verified • ${adminRegistrations.size} Registered Users Loaded"
                                    } else {
                                        "Zero-Trust Role Protection Active"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (isAdmin) {
                            IconButton(
                                onClick = onRefreshAdminRegistrations,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("refresh_admin_registrations_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh registrations"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    if (isAdmin) {
                        OutlinedTextField(
                            value = alertTitle,
                            onValueChange = { if (it.length <= 100) alertTitle = it },
                            label = { Text("System Update Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = alertBody,
                            onValueChange = { if (it.length <= 300) alertBody = it },
                            label = { Text("Announcement Message") },
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (alertTitle.isNotBlank() && alertBody.isNotBlank()) {
                                    onBroadcastSystemAlert(alertTitle.trim(), alertBody.trim())
                                    alertTitle = ""
                                    alertBody = ""
                                    onShowMessage("📢 System announcement recorded to Notification Center!")
                                }
                            },
                            enabled = alertTitle.isNotBlank() && alertBody.isNotBlank(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                        ) {
                            Text("Publish System Notification")
                        }
                    } else {
                        Text(
                            text = "Global user registration logs are protected by Cloud Firestore Zero-Trust rules and visible exclusively to verified Noxra AI administrators. Your personal registration audit record is shown above.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Admin Custom Course Publisher + Reported Community Posts Moderation (Admin Only)
        if (isAdmin) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_custom_course_publisher_card"),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "🎓 Educator / Admin Custom Lesson Publisher",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Publish a custom interactive lesson to Cloud Firestore (`/custom_courses`) immediately visible on every student's Home dashboard.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = courseTitle,
                            onValueChange = { if (it.length <= 100) courseTitle = it },
                            label = { Text("Lesson Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = courseSubject,
                                onValueChange = { if (it.length <= 40) courseSubject = it },
                                label = { Text("Language / Subject") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = courseTargetClass,
                                onValueChange = { if (it.length <= 40) courseTargetClass = it },
                                label = { Text("Class / Grade") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Beginner", "Intermediate", "Advanced").forEach { lvl ->
                                FilterChip(
                                    selected = courseLevel == lvl,
                                    onClick = { courseLevel = lvl },
                                    label = { Text(lvl) },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = courseObjective,
                            onValueChange = { if (it.length <= 200) courseObjective = it },
                            label = { Text("Learning Objective") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = coursePrompt,
                            onValueChange = { if (it.length <= 300) coursePrompt = it },
                            label = { Text("Exercise Question Prompt") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = optA,
                                onValueChange = { if (it.length <= 120) optA = it },
                                label = { Text("Option 1") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = optB,
                                onValueChange = { if (it.length <= 120) optB = it },
                                label = { Text("Option 2") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = optC,
                                onValueChange = { if (it.length <= 120) optC = it },
                                label = { Text("Option 3 (optional)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = optD,
                                onValueChange = { if (it.length <= 120) optD = it },
                                label = { Text("Option 4 (optional)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Opt 1", "Opt 2", "Opt 3", "Opt 4").forEachIndexed { idx, label ->
                                FilterChip(
                                    selected = correctIdx == idx,
                                    onClick = { correctIdx = idx },
                                    label = { Text("✓ $label") },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = courseExplanation,
                            onValueChange = { if (it.length <= 300) courseExplanation = it },
                            label = { Text("Answer Explanation") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        val candidateOptions = listOf(optA, optB, optC, optD).map { it.trim() }.filter { it.isNotBlank() }
                        val canPublish = courseTitle.isNotBlank() && coursePrompt.isNotBlank() && candidateOptions.size >= 2
                        Button(
                            onClick = {
                                val chosenCorrect = listOf(optA, optB, optC, optD)
                                    .getOrNull(correctIdx)?.trim()?.takeIf { it.isNotBlank() }
                                    ?: candidateOptions.first()
                                onPublishCustomCourse(
                                    courseTitle.trim(),
                                    courseSubject.trim().ifBlank { "English" },
                                    courseLevel,
                                    courseTargetClass.trim().ifBlank { null },
                                    courseObjective.trim().ifBlank { "Master ${courseTitle.trim()}" },
                                    coursePrompt.trim(),
                                    candidateOptions,
                                    chosenCorrect,
                                    courseExplanation.trim().ifBlank { "Correct answer: $chosenCorrect" },
                                    30L
                                )
                                courseTitle = ""
                                courseObjective = ""
                                coursePrompt = ""
                                optA = ""
                                optB = ""
                                optC = ""
                                optD = ""
                                courseExplanation = ""
                            },
                            enabled = canPublish,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .testTag("publish_custom_course_button")
                        ) {
                            Text("Publish Custom Lesson (+30 XP)")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_moderation_card"),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🛡️ Reported Community Posts Queue",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${reportedPosts.size} reported post(s) awaiting review",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = onRefreshReportedPosts,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh reported posts")
                            }
                        }

                        if (reportedPosts.isEmpty()) {
                            Text(
                                text = "✅ Community feed is clean — zero reported posts awaiting moderation.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            reportedPosts.forEach { post ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "${post.authorName} • ${post.language}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = post.content,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedButton(
                                                onClick = { onModeratePost(post.id, true) },
                                                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                            ) {
                                                Text("Restore")
                                            }
                                            Button(
                                                onClick = { onModeratePost(post.id, false) },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                            ) {
                                                Text("Delete")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isAdmin && adminRegistrations.isNotEmpty()) {
            items(adminRegistrations, key = { it.id.ifBlank { it.userId } }) { reg ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "🎉 ${reg.userName} ${reg.studentHandle?.let { "($it)" }.orEmpty()} (${reg.registrationMethod})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Age: ${reg.studentAge ?: "-"} • Class: ${reg.studentClass ?: "General"} • Tier: ${reg.selectedTier ?: "Basic"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Email: ${reg.email} • UID: ${reg.userId}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (!reg.autoEmailStatus.isNullOrBlank()) {
                            Text(
                                text = "🤖 AI Email: ${reg.autoEmailStatus}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (!reg.aiAdminEmailSummary.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Text(
                                    text = reg.aiAdminEmailSummary,
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                        Text(
                            text = "Registered: ${reg.registrationDate} at ${reg.registrationTime}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            DeveloperFooterBanner(
                onOpenDeveloperModal = onOpenDeveloperInfo,
                developerName = developerName,
                developerAge = developerAge,
                developerRole = developerRole,
                canEditDeveloperInfo = isAdmin
            )
        }
    }
}

@Composable
private fun RegistrationDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(0.58f)
        )
    }
}
