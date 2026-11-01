package com.example.ui.screens

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.curriculum.FiftyLevelMasteryCurriculum
import com.example.ui.components.DEFAULT_DEV_AGE
import com.example.ui.components.DEFAULT_DEV_NAME
import com.example.ui.components.DEFAULT_DEV_ROLE

private val NATIVE_LANGUAGES = listOf("Hindi", "English", "Bengali", "Tamil", "Telugu", "Marathi")
private val LEARNING_LANGUAGES = listOf("English", "Spanish", "French", "German", "Japanese", "Hindi")

@Composable
fun CompleteRegistrationGateScreen(
    initialFullName: String,
    email: String,
    onCompleteAllInfo: (
        fullName: String,
        username: String,
        age: Int,
        studentClass: String,
        selectedTier: String,
        nativeLanguage: String,
        learningLanguage: String,
        passcode: String
    ) -> Unit,
    onOpenDeveloperInfo: () -> Unit,
    onSignOut: () -> Unit
) {
    var fullName by rememberSaveable {
        mutableStateOf(if (initialFullName == "Learner") "" else initialFullName)
    }
    var username by rememberSaveable { mutableStateOf("") }
    var ageText by rememberSaveable { mutableStateOf("") }
    var studentClass by rememberSaveable { mutableStateOf("") }
    var selectedTier by rememberSaveable { mutableStateOf("Basic") }
    var nativeLanguage by rememberSaveable { mutableStateOf("Hindi") }
    var learningLanguage by rememberSaveable { mutableStateOf("English") }
    var passcode by rememberSaveable { mutableStateOf("") }
    var validationError by rememberSaveable { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("mandatory_registration_gate_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 580.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                modifier = Modifier.size(80.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_noxra_logo),
                    contentDescription = "Noxra AI Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Complete Your Noxra AI Registration",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Signed in as $email • Please fill all required student details below to unlock Noxra AI, launch your Welcome Slideshow, and send your instant AI account summary.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = {
                            if (it.length <= 80) fullName = it
                            validationError = null
                        },
                        label = { Text("1. Student Full Name *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gate_full_name_input")
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            if (it.length <= 32) username = it
                            validationError = null
                        },
                        label = { Text("2. Unique @username ID * (e.g. sachin_ai)") },
                        leadingIcon = { Icon(Icons.Default.AlternateEmail, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gate_username_input")
                    )

                    OutlinedTextField(
                        value = ageText,
                        onValueChange = { raw ->
                            val digits = raw.filter { it.isDigit() }.take(3)
                            ageText = digits
                            validationError = null
                        },
                        label = { Text("3. Your Age (Years) *") },
                        leadingIcon = { Icon(Icons.Default.Cake, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gate_age_input")
                    )

                    Text(
                        text = "4. Select Your Class / Grade *:",
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
                                selected = studentClass == cls,
                                onClick = {
                                    studentClass = cls
                                    validationError = null
                                },
                                label = { Text(cls) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = studentClass,
                        onValueChange = {
                            if (it.length <= 36) studentClass = it
                            validationError = null
                        },
                        label = { Text("Selected Class / Grade *") },
                        leadingIcon = { Icon(Icons.Default.School, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gate_class_input")
                    )

                    Text(
                        text = "5. Select Starting Level Tier (English & Math 1–50) *:",
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
                                    .testTag("gate_tier_${tier.lowercase()}")
                            )
                        }
                    }

                    Text(
                        text = "6. I Speak (Native Language) *:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NATIVE_LANGUAGES.forEach { lang ->
                            FilterChip(
                                selected = nativeLanguage == lang,
                                onClick = { nativeLanguage = lang },
                                label = { Text(lang) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }

                    Text(
                        text = "7. Primary Language to Practice *:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LEARNING_LANGUAGES.forEach { lang ->
                            FilterChip(
                                selected = learningLanguage == lang,
                                onClick = { learningLanguage = lang },
                                label = { Text(lang) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = passcode,
                        onValueChange = {
                            if (it.length <= 24) passcode = it
                            validationError = null
                        },
                        label = { Text("8. Create Sync Passcode / PIN (min 4 chars) *") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gate_passcode_input")
                    )

                    validationError?.let { err ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("gate_validation_error")
                        ) {
                            Text(
                                text = err,
                                modifier = Modifier.padding(12.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val cleanName = fullName.trim()
                            val cleanUser = username.trim().removePrefix("@")
                                .replace(Regex("[^a-zA-Z0-9_.]"), "")
                            val parsedAge = ageText.toIntOrNull()
                            val cleanClass = studentClass.trim()
                            val cleanPasscode = passcode.trim()

                            validationError = when {
                                cleanName.length < 2 -> "Please enter your Full Name (at least 2 characters)."
                                cleanUser.length < 2 -> "Please choose a valid @username ID (at least 2 letters/numbers)."
                                parsedAge == null || parsedAge !in 5..100 -> "Please enter your valid Age (5 to 100 years)."
                                cleanClass.isBlank() -> "Please select or enter your Class / Grade."
                                cleanPasscode.length < 4 -> "Please set a Sync Passcode / PIN (at least 4 characters)."
                                else -> null
                            }

                            if (validationError == null && parsedAge != null) {
                                onCompleteAllInfo(
                                    cleanName,
                                    "@$cleanUser",
                                    parsedAge,
                                    cleanClass,
                                    selectedTier,
                                    nativeLanguage,
                                    learningLanguage,
                                    cleanPasscode
                                )
                            }
                        },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp)
                            .testTag("gate_complete_registration_button")
                    ) {
                        Text(
                            text = "Complete Registration & Open Noxra AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onOpenDeveloperInfo,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("gate_developer_info_button")
            ) {
                Text("👑 Developer: $DEFAULT_DEV_NAME ($DEFAULT_DEV_AGE • $DEFAULT_DEV_ROLE)")
            }

            TextButton(
                onClick = onSignOut,
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("gate_switch_account_button")
            ) {
                Text("Switch Google Account / Sign Out")
            }
        }
    }
}
