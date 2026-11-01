package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private const val DEV_EMAIL = "ksachin29845@gmail.com"
const val DEFAULT_DEV_NAME = "Sachin Kumar"
const val DEFAULT_DEV_AGE = "12 y (12 Years)"
const val DEFAULT_DEV_ROLE = "CEO, Founder & Owner"
const val DEFAULT_DEV_BIO =
    "Hello! I am Sachin Kumar, a 12-year-old innovator and the CEO, Founder & Owner of Noxra AI — built to help students master English & Math Levels 1–50 with live AI coaching, ranked leagues, and friend chat."

@Composable
fun DeveloperFooterBanner(
    onOpenDeveloperModal: () -> Unit,
    developerName: String = DEFAULT_DEV_NAME,
    developerAge: String = DEFAULT_DEV_AGE,
    developerRole: String = DEFAULT_DEV_ROLE,
    canEditDeveloperInfo: Boolean = false,
    modifier: Modifier = Modifier
) {
    val resolvedName = developerName.ifBlank { DEFAULT_DEV_NAME }
    val resolvedAge = developerAge.ifBlank { DEFAULT_DEV_AGE }
    val resolvedRole = developerRole.ifBlank { DEFAULT_DEV_ROLE }
    val actionHint = if (canEditDeveloperInfo) "Tap to View / Owner Edit" else "Tap to View Official Profile"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("about_developer_footer_card")
            .clickable(onClick = onOpenDeveloperModal),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = resolvedName.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "About Developer • $resolvedName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "Verified CEO & Founder",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = "$resolvedRole of Noxra AI • Age: $resolvedAge ($actionHint)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.Code,
                contentDescription = "View developer details",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun DeveloperProfileCard(
    modifier: Modifier = Modifier,
    developerName: String = DEFAULT_DEV_NAME,
    developerAge: String = DEFAULT_DEV_AGE,
    developerRole: String = DEFAULT_DEV_ROLE,
    canEditDeveloperInfo: Boolean = false,
    onOpenFullStory: (() -> Unit)? = null
) {
    val resolvedName = developerName.ifBlank { DEFAULT_DEV_NAME }
    val resolvedAge = developerAge.ifBlank { DEFAULT_DEV_AGE }
    val resolvedRole = developerRole.ifBlank { DEFAULT_DEV_ROLE }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("developer_profile_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
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
                    Text(
                        text = "SK",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Developer Profile",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified Developer",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = resolvedName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$resolvedRole • Noxra AI",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            DeveloperDetailRow("Name", resolvedName)
            DeveloperDetailRow("Age", resolvedAge)
            DeveloperDetailRow("Position", resolvedRole)
            DeveloperDetailRow("Email", DEV_EMAIL)
            DeveloperDetailRow(
                label = "Interests",
                value = "Coding, AI, app development, business, and innovation",
                showDivider = false
            )

            if (onOpenFullStory != null) {
                OutlinedButton(
                    onClick = onOpenFullStory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("developer_card_open_story_button")
                ) {
                    Icon(
                        imageVector = if (canEditDeveloperInfo) Icons.Default.Edit else Icons.Default.Verified,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (canEditDeveloperInfo) {
                            "View Vision, Owner Edit & Contact CEO"
                        } else {
                            "View Official Vision & Contact CEO"
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun AboutDeveloperDialog(
    onDismiss: () -> Unit,
    onFeedbackMessage: (String) -> Unit,
    developerName: String = DEFAULT_DEV_NAME,
    developerAge: String = DEFAULT_DEV_AGE,
    developerRole: String = DEFAULT_DEV_ROLE,
    developerBio: String = DEFAULT_DEV_BIO,
    canEditDeveloperInfo: Boolean = false,
    onSaveDeveloperEdits: ((name: String, age: String, role: String, bio: String) -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler { onDismiss() }

        AboutDeveloperSurfaceContent(
            onDismiss = onDismiss,
            onFeedbackMessage = onFeedbackMessage,
            developerName = developerName,
            developerAge = developerAge,
            developerRole = developerRole,
            developerBio = developerBio,
            canEditDeveloperInfo = canEditDeveloperInfo,
            onSaveDeveloperEdits = onSaveDeveloperEdits
        )
    }
}

@Composable
fun AboutDeveloperSurfaceContent(
    onDismiss: () -> Unit,
    onFeedbackMessage: (String) -> Unit,
    developerName: String = DEFAULT_DEV_NAME,
    developerAge: String = DEFAULT_DEV_AGE,
    developerRole: String = DEFAULT_DEV_ROLE,
    developerBio: String = DEFAULT_DEV_BIO,
    canEditDeveloperInfo: Boolean = false,
    onSaveDeveloperEdits: ((name: String, age: String, role: String, bio: String) -> Unit)? = null
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val resolvedName = developerName.ifBlank { DEFAULT_DEV_NAME }
    val resolvedAge = developerAge.ifBlank { DEFAULT_DEV_AGE }
    val resolvedRole = developerRole.ifBlank { DEFAULT_DEV_ROLE }
    val resolvedBio = developerBio.ifBlank { DEFAULT_DEV_BIO }

    var editName by rememberSaveable(resolvedName) { mutableStateOf(resolvedName) }
    var editAge by rememberSaveable(resolvedAge) { mutableStateOf(resolvedAge) }
    var editRole by rememberSaveable(resolvedRole) { mutableStateOf(resolvedRole) }
    var editBio by rememberSaveable(resolvedBio) { mutableStateOf(resolvedBio) }

    val activeTab = if (!canEditDeveloperInfo && selectedTab == 2) 0 else selectedTab

    val clipboardSummary = remember(resolvedName, resolvedAge, resolvedRole, resolvedBio) {
        """
        About the Developer — Noxra AI
        Full Name: $resolvedName
        Position: $resolvedRole
        Age: $resolvedAge
        Email: $DEV_EMAIL
        Project Name: Noxra AI
        About: $resolvedBio
        """.trimIndent()
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .padding(vertical = 24.dp)
            .testTag("about_developer_dialog"),
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "👑",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "About Developer",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("close_developer_dialog_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close About the Developer"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
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
                        Text(
                            text = resolvedName.take(1).uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = resolvedName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$resolvedRole • Noxra AI",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "Age: $resolvedAge",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = "CEO • Founder • Owner",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("developer_owner_lock_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (canEditDeveloperInfo) Icons.Default.Verified else Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (canEditDeveloperInfo) {
                            "Owner Verified ($DEV_EMAIL) — Developer Info Editing Unlocked"
                        } else {
                            "Official Developer Profile — Editable strictly by Owner ($DEV_EMAIL)"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Vision") },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("dev_story_tab")
                )
                FilterChip(
                    selected = activeTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Profile") },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("dev_profile_tab")
                )
                if (canEditDeveloperInfo) {
                    FilterChip(
                        selected = activeTab == 2,
                        onClick = { selectedTab = 2 },
                        label = { Text("✏️ Edit") },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("dev_edit_tab")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (activeTab) {
                0 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = resolvedBio,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Noxra AI combines 50-level interactive English & Math mastery paths (Select Class + Basic, Medium, Hard), live Noxra AI coaching with saved history, global ranked leaderboards, Naruto Event ₹49 pass, and @username friend chat across every phone.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                1 -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Official Developer & CEO Profile",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            DeveloperDetailRow("Full Name", resolvedName)
                            DeveloperDetailRow("Position", resolvedRole)
                            DeveloperDetailRow("Age", resolvedAge)
                            DeveloperDetailRow("Project Name", "Noxra AI")
                            DeveloperDetailRow("Email", DEV_EMAIL)
                            DeveloperDetailRow("Field", "Artificial Intelligence, Education & Social Technology")
                            DeveloperDetailRow(
                                "Interests",
                                "Coding, AI, app development, business, and innovation",
                                showDivider = false
                            )
                        }
                    }
                }
                2 -> {
                    if (canEditDeveloperInfo) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "✏️ Owner Edit — Official Developer Info",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                OutlinedTextField(
                                    value = editName,
                                    onValueChange = { if (it.length <= 80) editName = it },
                                    label = { Text("Developer Name") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("edit_dev_name_input")
                                )
                                OutlinedTextField(
                                    value = editAge,
                                    onValueChange = { if (it.length <= 30) editAge = it },
                                    label = { Text("Age (e.g., 12 y)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("edit_dev_age_input")
                                )
                                OutlinedTextField(
                                    value = editRole,
                                    onValueChange = { if (it.length <= 100) editRole = it },
                                    label = { Text("Position (CEO, Founder & Owner)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("edit_dev_role_input")
                                )
                                OutlinedTextField(
                                    value = editBio,
                                    onValueChange = { if (it.length <= 400) editBio = it },
                                    label = { Text("About Developer Story") },
                                    maxLines = 4,
                                    modifier = Modifier.fillMaxWidth().testTag("edit_dev_bio_input")
                                )
                                Button(
                                    onClick = {
                                        if (onSaveDeveloperEdits != null) {
                                            onSaveDeveloperEdits(editName, editAge, editRole, editBio)
                                        }
                                        onFeedbackMessage("✅ Updated Official Developer Profile (${editName.ifBlank { DEFAULT_DEV_NAME }} • ${editAge.ifBlank { DEFAULT_DEV_AGE }} • ${editRole.ifBlank { DEFAULT_DEV_ROLE }})!")
                                        selectedTab = 1
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("save_developer_edits_button")
                                ) {
                                    Text("Save Official Developer Info")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Sachin Kumar CEO Info", clipboardSummary))
                        onFeedbackMessage("📋 Copied $resolvedName's CEO & Founder info ($DEV_EMAIL)!")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("copy_developer_info_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy developer details",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy")
                }

                Button(
                    onClick = {
                        try {
                            val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$DEV_EMAIL")
                                putExtra(Intent.EXTRA_SUBJECT, "Hello $resolvedName — Noxra AI Feedback")
                                putExtra(Intent.EXTRA_TEXT, "Hi $resolvedName,\n\nI love practicing Levels 1–50 on Noxra AI!")
                            }
                            context.startActivity(Intent.createChooser(mailIntent, "Send Email to $resolvedName"))
                        } catch (_: Exception) {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, clipboardSummary)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Developer Info"))
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("send_developer_email_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send email to developer",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Contact CEO")
                }
            }
        }
    }
}

@Composable
private fun DeveloperDetailRow(label: String, value: String, showDivider: Boolean = true) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(0.38f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(0.62f)
            )
        }
        if (showDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        }
    }
}
