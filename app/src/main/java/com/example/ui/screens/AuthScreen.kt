package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.R
import com.example.core.attemptAutoSignIn
import com.example.core.onGoogleSignInClicked
import com.example.data.curriculum.FiftyLevelMasteryCurriculum
import com.example.data.curriculum.LanguageCurriculum
import com.example.ui.components.DeveloperFooterBanner

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import com.example.core.PublicWebLinkManager

private val AUTH_NATIVE_LANGUAGES = listOf("Hindi", "English", "Bengali", "Tamil", "Telugu", "Marathi")
private val AUTH_LEARNING_LANGUAGES = listOf("English", "Spanish", "French", "German", "Japanese", "Hindi")

@Composable
fun AuthScreen(
    onAuthSuccess: (
        fullName: String,
        username: String,
        age: Int?,
        studentClass: String,
        passcode: String,
        selectedTier: String,
        nativeLanguage: String,
        learningLanguage: String
    ) -> Unit,
    onOpenDeveloperInfo: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var isNewStudentMode by rememberSaveable { mutableStateOf(true) }
    var fullNameInput by rememberSaveable { mutableStateOf("") }
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var ageInput by rememberSaveable { mutableStateOf("") }
    var studentClassInput by rememberSaveable { mutableStateOf("") }
    var passcodeInput by rememberSaveable { mutableStateOf("") }
    var selectedTier by rememberSaveable { mutableStateOf("Basic") }
    var nativeLanguageInput by rememberSaveable { mutableStateOf("Hindi") }
    var learningLanguageInput by rememberSaveable { mutableStateOf("English") }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var authWebLinkCopied by remember { mutableStateOf(false) }
    val activePublicWebUrl = remember(context) { PublicWebLinkManager.getPublicWebUrl(context) }

    // Silent auto sign-in check on startup so signed-in users never need to register/login again
    LaunchedEffect(Unit) {
        attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {
                onAuthSuccess("", "", null, "", "", "Basic", "Hindi", "English")
            },
            onUnauthenticated = {},
            scope = scope
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                tonalElevation = 6.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.size(120.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_noxra_logo),
                    contentDescription = stringResource(R.string.cd_mascot_image),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    text = "✨ English 1–50 • Math 1–50 • Noxra AI • Friend Chat",
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Noxra AI",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = stringResource(R.string.welcome_headline),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Fill your student registration details once — Noxra AI opens with your Welcome Slideshow (Developer: Sachin Kumar) & sends your account info via AI automatically!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Scrollable Language Flag Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LanguageCurriculum.supportedLanguages.forEach { lang ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = lang.flagEmoji, style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = lang.name, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Student Registration vs Returning Cross-Phone Login Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = isNewStudentMode,
                            onClick = {
                                isNewStudentMode = true
                                errorMessage = null
                            },
                            label = { Text("🆕 New Registration") },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("auth_mode_register_chip")
                        )
                        FilterChip(
                            selected = !isNewStudentMode,
                            onClick = {
                                isNewStudentMode = false
                                errorMessage = null
                            },
                            label = { Text("🔑 Any-Phone Login") },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("auth_mode_login_chip")
                        )
                    }

                    if (isNewStudentMode) {
                        Text(
                            text = "All fields (*) are required for new registration. Once completed, Noxra AI opens immediately, shows the Welcome to Our Noxra AI App slide & Developer Name, and AI sends your user info to Admin automatically!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = fullNameInput,
                            onValueChange = {
                                if (it.length <= 80) fullNameInput = it
                                errorMessage = null
                            },
                            label = { Text("1. Student Full Name *") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_full_name_input")
                        )
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = {
                                if (it.length <= 32) usernameInput = it
                                errorMessage = null
                            },
                            label = { Text("2. Unique @username ID * (e.g. sachin_ai)") },
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_username_input")
                        )
                        OutlinedTextField(
                            value = ageInput,
                            onValueChange = { raw ->
                                ageInput = raw.filter { it.isDigit() }.take(3)
                                errorMessage = null
                            },
                            label = { Text("3. Your Age (Years) *") },
                            leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_age_input")
                        )

                        Text(
                            text = "4. Select Class / Grade *:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FiftyLevelMasteryCurriculum.SUPPORTED_CLASSES.forEach { cls ->
                                FilterChip(
                                    selected = studentClassInput == cls,
                                    onClick = {
                                        studentClassInput = cls
                                        errorMessage = null
                                    },
                                    label = { Text(cls) },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                )
                            }
                        }
                        OutlinedTextField(
                            value = studentClassInput,
                            onValueChange = {
                                if (it.length <= 36) studentClassInput = it
                                errorMessage = null
                            },
                            label = { Text("Selected Class / Grade * (e.g. Class 8)") },
                            leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_class_input")
                        )

                        Text(
                            text = "5. Starting Practice Tier (English & Math Levels 1–50) *:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FiftyLevelMasteryCurriculum.SUPPORTED_TIERS.forEach { tier ->
                                val tierDisplay = when (tier) {
                                    "Basic" -> "🌱 Basic"
                                    "Mid" -> "⚡ Medium"
                                    "Advance" -> "🔥 Hard"
                                    else -> tier
                                }
                                FilterChip(
                                    selected = selectedTier == tier,
                                    onClick = { selectedTier = tier },
                                    label = { Text(tierDisplay) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("register_tier_${tier.lowercase()}")
                                )
                            }
                        }

                        Text(
                            text = "6. Native Language & Learning Language *:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AUTH_NATIVE_LANGUAGES.forEach { lang ->
                                FilterChip(
                                    selected = nativeLanguageInput == lang,
                                    onClick = { nativeLanguageInput = lang },
                                    label = { Text("Speak: $lang") },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                )
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            AUTH_LEARNING_LANGUAGES.forEach { lang ->
                                FilterChip(
                                    selected = learningLanguageInput == lang,
                                    onClick = { learningLanguageInput = lang },
                                    label = { Text("Learn: $lang") },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = passcodeInput,
                            onValueChange = {
                                if (it.length <= 24) passcodeInput = it
                                errorMessage = null
                            },
                            label = { Text("7. Account Sync Passcode / PIN (min 4 chars) *") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("register_passcode_input")
                        )
                    } else {
                        Text(
                            text = "Already created an account? Enter your @username ID & Passcode (optional) and tap Sign In with Google to restore Levels 1–50, Naruto ₹49 Pass, Friend Chats, and History on any phone — you will stay signed in permanently!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = usernameInput,
                            onValueChange = { if (it.length <= 32) usernameInput = it },
                            label = { Text("Your @username ID (optional)") },
                            leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_username_input")
                        )
                        OutlinedTextField(
                            value = passcodeInput,
                            onValueChange = { if (it.length <= 24) passcodeInput = it },
                            label = { Text("Sync Passcode / PIN (optional)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_passcode_input")
                        )
                    }

                    if (errorMessage != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("registration_validation_error"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                        ) {
                            Text(
                                text = errorMessage!!,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Button(
                        onClick = {
                            errorMessage = null
                            if (isNewStudentMode) {
                                val cleanName = fullNameInput.trim()
                                val cleanUser = usernameInput.trim().removePrefix("@")
                                    .replace(Regex("[^a-zA-Z0-9_.]"), "")
                                val parsedAge = ageInput.toIntOrNull()
                                val cleanClass = studentClassInput.trim()
                                val cleanPin = passcodeInput.trim()

                                val validationProblem = when {
                                    cleanName.length < 2 -> "Please fill your Full Name before opening Noxra AI."
                                    cleanUser.length < 2 -> "Please choose a unique @username ID (at least 2 letters/numbers)."
                                    parsedAge == null || parsedAge !in 5..100 -> "Please enter your valid Age (5 to 100 years)."
                                    cleanClass.isBlank() -> "Please select or enter your Class / Grade."
                                    cleanPin.length < 4 -> "Please create a Sync Passcode / PIN (at least 4 characters)."
                                    else -> null
                                }
                                if (validationProblem != null) {
                                    errorMessage = validationProblem
                                    return@Button
                                }
                            }

                            isLoading = true
                            onGoogleSignInClicked(
                                context = context,
                                credentialManager = credentialManager,
                                onAuthSuccess = {
                                    isLoading = false
                                    if (isNewStudentMode) {
                                        onAuthSuccess(
                                            fullNameInput.trim(),
                                            usernameInput.trim(),
                                            ageInput.toIntOrNull(),
                                            studentClassInput.trim(),
                                            passcodeInput.trim(),
                                            selectedTier,
                                            nativeLanguageInput,
                                            learningLanguageInput
                                        )
                                    } else {
                                        onAuthSuccess(
                                            "",
                                            "",
                                            null,
                                            "",
                                            passcodeInput.trim(),
                                            "Basic",
                                            "Hindi",
                                            "English"
                                        )
                                    }
                                },
                                onAuthError = { msg ->
                                    isLoading = false
                                    errorMessage = msg
                                },
                                scope = scope,
                                onAuthCancelled = {
                                    isLoading = false
                                }
                            )
                        },
                        enabled = !isLoading,
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .testTag("google_sign_in_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (isNewStudentMode) {
                                    "Complete Registration & Open Noxra AI"
                                } else {
                                    "Login & Sync Account via Google"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Core Highlights Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FeatureHighlightRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Noxra AI Tutor + English & Math Levels 1–50",
                        subtitle = "Select Class • Basic, Medium, Hard • Practice Levels 1 to 50 with live AI coaching"
                    )
                    FeatureHighlightRow(
                        icon = Icons.Default.RecordVoiceOver,
                        title = "Friend @username Chat + AI Speaking Lab",
                        subtitle = "Search any friend's @username to chat directly + real-time voice coaching"
                    )
                    FeatureHighlightRow(
                        icon = Icons.Default.EmojiEvents,
                        title = "Naruto Event ₹49 Pass & Full Cloud History",
                        subtitle = "2x XP boost, permanent cloud history & login once across any phone"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1-Tap Public Web Link Copy, Share & Google Search Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_share_web_link_card")
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_VIEW, Uri.parse(activePublicWebUrl))
                                        )
                                    }
                                }
                        ) {
                            Text(
                                text = "🌐 Public Searchable Web Link (Tap to Open)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = activePublicWebUrl,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Noxra AI Web Link", activePublicWebUrl)
                                )
                                authWebLinkCopied = true
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("auth_copy_web_link_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Noxra AI public web link",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (authWebLinkCopied) "Copied ✓" else "Copy")
                        }
                        IconButton(
                            onClick = {
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Open & run Noxra AI through this web link: $activePublicWebUrl"
                                    )
                                }
                                context.startActivity(
                                    Intent.createChooser(shareIntent, "Share Noxra AI Web Link")
                                )
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("auth_share_web_link_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Noxra AI public web link",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                context.startActivity(
                                    Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(PublicWebLinkManager.buildGoogleSearchUrl("Noxra AI Sachin Kumar"))
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("auth_search_google_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("🔍 Search Noxra AI on Google.com ↗", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            DeveloperFooterBanner(onOpenDeveloperModal = onOpenDeveloperInfo)
        }
    }
}

@Composable
private fun FeatureHighlightRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
