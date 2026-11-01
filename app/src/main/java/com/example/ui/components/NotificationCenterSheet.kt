package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.SmartNotificationHelper
import com.example.data.model.SmartNotification
import com.example.data.repository.AppPreferencesState
import com.example.ui.viewmodel.DataUiState

private val NOTIFICATION_CATEGORIES = listOf(
    "All", "Learning", "Achievements", "Friends", "AI Tutor", "Rewards", "System"
)

@Composable
fun NotificationCenterDialog(
    notificationsState: DataUiState<List<SmartNotification>>,
    preferences: AppPreferencesState,
    learningLanguage: String,
    onDismiss: () -> Unit,
    onMarkRead: (String) -> Unit,
    onMarkAllRead: () -> Unit,
    onDeleteNotification: (String) -> Unit,
    onTriggerSmartReminder: (String, String, String, String) -> Unit,
    onUpdatePrefs: (
        enabled: Boolean?,
        pauseAll: Boolean?,
        reminderTime: String?,
        quietHours: String?,
        sound: Boolean?,
        vibration: Boolean?,
        email: Boolean?,
        dailyLimit: Int?,
        categories: Set<String>?
    ) -> Unit,
    onFeedbackMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var showSettingsView by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf("All") }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val sent = SmartNotificationHelper.sendLocalNotification(
                context = context,
                title = "🔥 Your $learningLanguage streak is waiting!",
                body = "Practice 5 new words with Kavi to hit today's learning goal.",
                enableSound = preferences.notificationSound,
                enableVibration = preferences.notificationVibration
            )
            onFeedbackMessage(if (sent) "🔔 Smart Android notification dispatched!" else "Saved in Notification Center!")
        } else {
            onFeedbackMessage("In-app smart notification added (system permission declined).")
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler {
            if (showSettingsView) showSettingsView = false else onDismiss()
        }

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 28.dp)
                .testTag("notification_center_dialog"),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Notifications icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (showSettingsView) "Smart Notification Settings" else "Notification Center",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    Row {
                        IconButton(
                            onClick = { showSettingsView = !showSettingsView },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("toggle_notif_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Notification settings"
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_notifications_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close notifications"
                            )
                        }
                    }
                }

                if (showSettingsView) {
                    NotificationSettingsPane(
                        preferences = preferences,
                        onUpdatePrefs = onUpdatePrefs
                    )
                } else {
                    // Category Filter Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NOTIFICATION_CATEGORIES.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("notif_cat_${cat.lowercase().replace(" ", "_")}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Action Bar: Mark All Read + Test Smart Engine
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onMarkAllRead,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("mark_all_read_button")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Mark All Read")
                        }
                        Button(
                            onClick = {
                                if (!preferences.notificationsEnabled || preferences.pauseAllNotifications) {
                                    onFeedbackMessage("Notifications are currently paused in settings.")
                                    return@Button
                                }
                                val title = "🤖 Smart Coach: 3-Min $learningLanguage Boost!"
                                val body = "Based on your recent progress, now is your peak study time. Earn +25 XP!"
                                onTriggerSmartReminder(title, body, "AI Tutor", "🤖")
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    SmartNotificationHelper.sendLocalNotification(
                                        context = context,
                                        title = title,
                                        body = body,
                                        enableSound = preferences.notificationSound,
                                        enableVibration = preferences.notificationVibration
                                    )
                                    onFeedbackMessage("🔔 Smart reminder sent!")
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("send_smart_reminder_button")
                        ) {
                            Text("Smart Reminder")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    when (notificationsState) {
                        is DataUiState.Loading -> {
                            Text("Loading smart notifications...", style = MaterialTheme.typography.bodyMedium)
                        }
                        is DataUiState.Error -> {
                            Text(
                                text = notificationsState.message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        is DataUiState.Success -> {
                            val filtered = notificationsState.data.filter { item ->
                                selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
                            }
                            if (filtered.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("🔔", style = MaterialTheme.typography.displayMedium)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "No notifications in '$selectedCategory' yet!",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "Tap 'Smart Reminder' above or complete a lesson to receive updates.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    items(filtered, key = { it.id.ifBlank { it.title + it.body } }) { notif ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    if (!notif.isRead && notif.id.isNotBlank()) {
                                                        onMarkRead(notif.id)
                                                    }
                                                },
                                            shape = RoundedCornerShape(18.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (notif.isRead) {
                                                    MaterialTheme.colorScheme.surface
                                                } else {
                                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                                }
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(42.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = notif.iconEmoji ?: "🔔",
                                                        style = MaterialTheme.typography.titleLarge
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Surface(
                                                            shape = RoundedCornerShape(8.dp),
                                                            color = MaterialTheme.colorScheme.secondaryContainer
                                                        ) {
                                                            Text(
                                                                text = notif.category,
                                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                                style = MaterialTheme.typography.labelSmall
                                                            )
                                                        }
                                                        if (!notif.isRead) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .size(8.dp)
                                                                    .clip(CircleShape)
                                                                    .background(MaterialTheme.colorScheme.primary)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = notif.title,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = notif.body,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                if (notif.id.isNotBlank()) {
                                                    IconButton(
                                                        onClick = { onDeleteNotification(notif.id) },
                                                        modifier = Modifier.size(48.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.DeleteOutline,
                                                            contentDescription = "Delete notification"
                                                        )
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
            }
        }
    }
}

@Composable
private fun NotificationSettingsPane(
    preferences: AppPreferencesState,
    onUpdatePrefs: (
        enabled: Boolean?,
        pauseAll: Boolean?,
        reminderTime: String?,
        quietHours: String?,
        sound: Boolean?,
        vibration: Boolean?,
        email: Boolean?,
        dailyLimit: Int?,
        categories: Set<String>?
    ) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    ToggleSettingRow(
                        title = "Enable Smart Notifications",
                        subtitle = "Master switch for reminders, streaks & AI tutor updates",
                        checked = preferences.notificationsEnabled,
                        onCheckedChange = { onUpdatePrefs(it, null, null, null, null, null, null, null, null) }
                    )
                    HorizontalDivider()
                    ToggleSettingRow(
                        title = "Pause All Notifications Temporarily",
                        subtitle = "Snooze alerts during exams or vacations without losing settings",
                        checked = preferences.pauseAllNotifications,
                        onCheckedChange = { onUpdatePrefs(null, it, null, null, null, null, null, null, null) }
                    )
                    HorizontalDivider()
                    ToggleSettingRow(
                        title = "Notification Sound",
                        subtitle = "Play cheerful chime on streak & XP milestones",
                        checked = preferences.notificationSound,
                        onCheckedChange = { onUpdatePrefs(null, null, null, null, it, null, null, null, null) }
                    )
                    HorizontalDivider()
                    ToggleSettingRow(
                        title = "Haptic Vibration",
                        subtitle = "Gentle vibration for daily practice reminders",
                        checked = preferences.notificationVibration,
                        onCheckedChange = { onUpdatePrefs(null, null, null, null, null, it, null, null, null) }
                    )
                    HorizontalDivider()
                    ToggleSettingRow(
                        title = "Weekly Email Summary",
                        subtitle = "Optional weekly learning report & milestone digest",
                        checked = preferences.emailNotifications,
                        onCheckedChange = { onUpdatePrefs(null, null, null, null, null, null, it, null, null) }
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Preferred Study Reminder Time", style = MaterialTheme.typography.titleMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("08:00 AM", "02:00 PM", "07:30 PM", "09:00 PM").forEach { slot ->
                            FilterChip(
                                selected = preferences.reminderTime == slot,
                                onClick = { onUpdatePrefs(null, null, slot, null, null, null, null, null, null) },
                                label = { Text(slot) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }
                    Text(
                        text = "Quiet Hours (Do Not Disturb): ${preferences.quietHoursRange}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Daily Notification Cap: ${preferences.dailyNotificationLimit} / day (prevents alert fatigue)",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Slider(
                        value = preferences.dailyNotificationLimit.toFloat(),
                        onValueChange = {
                            onUpdatePrefs(null, null, null, null, null, null, null, it.toInt(), null)
                        },
                        valueRange = 1f..6f,
                        steps = 4
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Notification Categories", style = MaterialTheme.typography.titleMedium)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NOTIFICATION_CATEGORIES.drop(1).forEach { cat ->
                            val enabled = preferences.enabledNotificationCategories.contains(cat)
                            FilterChip(
                                selected = enabled,
                                onClick = {
                                    val updated = preferences.enabledNotificationCategories.toMutableSet()
                                    if (enabled) updated.remove(cat) else updated.add(cat)
                                    onUpdatePrefs(null, null, null, null, null, null, null, null, updated)
                                },
                                label = { Text(cat) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }
                    Text(
                        text = "🔒 Privacy Guarantee: Notifications encourage positive habits without shaming or exposing private learning content on locked screens.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
