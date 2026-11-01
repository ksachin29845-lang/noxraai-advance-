package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.core.SpeechAndAudioManager
import androidx.compose.runtime.LaunchedEffect
import com.example.data.curriculum.LanguageCurriculum
import com.example.data.model.SavedVocabulary
import com.example.data.model.UserProfile
import com.example.ui.viewmodel.DataUiState
import com.example.ui.viewmodel.SpeakingLabViewModel

@Composable
fun PracticeScreen(
    profile: UserProfile,
    vocabularyState: DataUiState<List<SavedVocabulary>>,
    speakingLabViewModel: SpeakingLabViewModel,
    audioManager: SpeechAndAudioManager,
    speakingTabFocusKey: Int = 0,
    onRegainHeart: () -> Unit,
    onPromoteVocabulary: (SavedVocabulary) -> Unit,
    onBookmarkWord: (word: String, translation: String, lang: String, phonetic: String?, example: String?) -> Unit,
    onOpenEnglishFiftyLevels: () -> Unit = {},
    onOpenMathFiftyLevels: () -> Unit = {},
    onShowMessage: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(speakingTabFocusKey) {
        if (speakingTabFocusKey > 0) {
            selectedTab = 0
        }
    }
    val tabs = listOf("🗣️ Speaking Lab", "🎧 Listening Lab", "🧠 Smart Revision", "🧪 Tests & Certs")

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("practice_basic_to_hard_banner"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🎯 Basic → Hard Levels 1–50",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Select Class • Basic, Medium, Hard",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                FilledTonalButton(
                    onClick = onOpenEnglishFiftyLevels,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("practice_open_english_50_button")
                ) {
                    Text("🇬🇧 Eng 1–50")
                }
                FilledTonalButton(
                    onClick = onOpenMathFiftyLevels,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("practice_open_math_50_button")
                ) {
                    Text("➗ Math 1–50")
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { idx, label ->
                FilterChip(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    label = { Text(label) },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("practice_tab_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> SpeakingLabContent(
                profile = profile,
                speakingLabViewModel = speakingLabViewModel,
                audioManager = audioManager,
                onRegainHeart = onRegainHeart,
                onBookmarkWord = onBookmarkWord,
                onShowMessage = onShowMessage
            )
            1 -> ListeningDictationPane(
                learningLanguage = profile.learningLanguage,
                audioManager = audioManager,
                onRegainHeart = onRegainHeart
            )
            2 -> SmartRevisionPane(
                vocabularyState = vocabularyState,
                learningLanguage = profile.learningLanguage,
                audioManager = audioManager,
                onPromoteVocabulary = onPromoteVocabulary
            )
            3 -> PlacementTestsAndCertificatesPane(
                profile = profile,
                onShowMessage = onShowMessage
            )
        }
    }
}

@Composable
private fun ListeningDictationPane(
    learningLanguage: String,
    audioManager: SpeechAndAudioManager,
    onRegainHeart: () -> Unit
) {
    val dictationSentences = listOf(
        "Consistency turns small daily practice into fluency." to "नियमित अभ्यास छोटे कदमों को धाराप्रवाह भाषा में बदल देता है।",
        "Could you recommend a quiet place to study nearby?" to "क्या आप पास में पढ़ाई के लिए कोई शांत जगह सुझा सकते हैं?",
        "Learning a new language opens doors around the world." to "नई भाषा सीखना दुनिया भर में अवसरों के द्वार खोलता है।"
    )
    var index by rememberSaveable { mutableIntStateOf(0) }
    var typedText by rememberSaveable { mutableStateOf("") }
    var feedback by remember { mutableStateOf<String?>(null) }

    val (targetSentence, hindiHint) = dictationSentences[index % dictationSentences.size]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("🎧 Listen & Type Dictation", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Tap Play or Slow Turtle Audio, listen carefully, and type the exact sentence you hear!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { audioManager.speak(targetSentence, "English", false) },
                            modifier = Modifier.weight(1f).heightIn(min = 50.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Play Audio")
                        }
                        OutlinedButton(
                            onClick = { audioManager.speak(targetSentence, "English", true) },
                            modifier = Modifier.weight(1f).heightIn(min = 50.dp)
                        ) {
                            Text("🐢 Slow 0.55x")
                        }
                    }

                    Text("Meaning Hint: $hindiHint", style = MaterialTheme.typography.bodySmall)

                    OutlinedTextField(
                        value = typedText,
                        onValueChange = { typedText = it },
                        label = { Text("Type what you heard...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("listening_dictation_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val score = SpeechAndAudioManager.calculatePronunciationAccuracy(targetSentence, typedText)
                                if (score >= 85) {
                                    feedback = "✅ Excellent ear! ($score% match): \"$targetSentence\""
                                    onRegainHeart()
                                } else {
                                    feedback = "💡 $score% match — Expected: \"$targetSentence\""
                                }
                            },
                            enabled = typedText.isNotBlank(),
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) {
                            Text("Check Dictation")
                        }
                        OutlinedButton(
                            onClick = {
                                index = (index + 1) % dictationSentences.size
                                typedText = ""
                                feedback = null
                            },
                            modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                        ) {
                            Text("Next Audio →")
                        }
                    }

                    if (feedback != null) {
                        Text(feedback!!, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    FilledTonalButton(
                        onClick = onRegainHeart,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Practice Complete — Regain +1 ❤️ Heart")
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartRevisionPane(
    vocabularyState: DataUiState<List<SavedVocabulary>>,
    learningLanguage: String,
    audioManager: SpeechAndAudioManager,
    onPromoteVocabulary: (SavedVocabulary) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🧠 Spaced Repetition & Mistake Notebook", style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Missed quiz words and bookmarked dictionary words automatically appear here. Tap any flashcard to flip & boost mastery!",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        when (vocabularyState) {
            is DataUiState.Loading -> item { Text("Loading your Smart Revision deck...") }
            is DataUiState.Error -> item { Text(vocabularyState.message, color = MaterialTheme.colorScheme.error) }
            is DataUiState.Success -> {
                if (vocabularyState.data.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🗂️", style = MaterialTheme.typography.displayMedium)
                                Text("Your Mistake & Flashcard Deck is Clean!", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Bookmark words in Learn -> Dictionary or complete quizzes to build your smart deck.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(vocabularyState.data, key = { it.id.ifBlank { it.word } }) { item ->
                        var flipped by remember(item.id) { mutableStateOf(false) }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { flipped = !flipped },
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (item.isMistake) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = if (item.isMistake) "⚠️ Needs Revision" else "⭐ Saved Word",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    Text("Mastery: ${item.masteryLevel}/5", style = MaterialTheme.typography.labelMedium)
                                }
                                Text(item.word, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                                if (flipped) {
                                    Text("Meaning: ${item.translation}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    item.exampleSentence?.let {
                                        Text("Example: “$it”", style = MaterialTheme.typography.bodySmall)
                                    }
                                } else {
                                    Text("Tap flashcard to reveal translation →", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                LinearProgressIndicator(
                                    progress = { (item.masteryLevel.toFloat() / 5f).coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth().height(8.dp)
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(
                                        onClick = { audioManager.speak(item.word, item.language.ifBlank { learningLanguage }, false) },
                                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                    ) {
                                        Text("🔊 Pronounce")
                                    }
                                    Button(
                                        onClick = { onPromoteVocabulary(item) },
                                        enabled = item.masteryLevel < 5L,
                                        modifier = Modifier.weight(1f).heightIn(min = 48.dp)
                                    ) {
                                        Text(if (item.masteryLevel >= 5L) "Mastered ✅" else "I Remembered +1")
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
private fun PlacementTestsAndCertificatesPane(
    profile: UserProfile,
    onShowMessage: (String) -> Unit
) {
    var placementScore by rememberSaveable { mutableIntStateOf(-1) }
    var q1Choice by rememberSaveable { mutableStateOf("") }
    var q2Choice by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App-Issued Completion Certificate Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = "Certificate emblem",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text("Noxra AI Progress Certificate", style = MaterialTheme.typography.headlineSmall)
                    Text("Presented to ${profile.displayName}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(
                        text = "Course: ${profile.learningLanguage} (${profile.skillLevel}) • Fluency Score: ${profile.fluencyScore ?: 20}% • Total XP: ${profile.xp}",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Note: This is an in-app Noxra AI learning milestone award celebrating personal study progress, and is distinct from formal government or university language qualifications.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { onShowMessage("🎓 Progress Certificate summary ready to share from your Profile!") },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text("Save Milestone Certificate")
                    }
                }
            }
        }

        // Quick Placement Diagnostic Test
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("🧪 Quick Level Placement Assessment", style = MaterialTheme.typography.titleLarge)
                    Text("1. Choose the natural completion: 'If I ___ more free time, I would learn three languages.'", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("had", "have", "will have").forEach { opt ->
                            FilterChip(
                                selected = q1Choice == opt,
                                onClick = { q1Choice = opt },
                                label = { Text(opt) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }

                    Text("2. Choose the correct preposition: 'She is passionate ___ artificial intelligence and coding.'", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("about", "on", "with").forEach { opt ->
                            FilterChip(
                                selected = q2Choice == opt,
                                onClick = { q2Choice = opt },
                                label = { Text(opt) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            var s = 0
                            if (q1Choice == "had") s++
                            if (q2Choice == "about") s++
                            placementScore = s
                        },
                        enabled = q1Choice.isNotBlank() && q2Choice.isNotBlank(),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
                    ) {
                        Text("Check Placement Level")
                    }

                    if (placementScore >= 0) {
                        val rec = when (placementScore) {
                            2 -> "🔥 Recommended Tier: Hard / Advanced (2/2 correct!)"
                            1 -> "⚡ Recommended Tier: Medium / Intermediate (1/2 correct!)"
                            else -> "🌱 Recommended Tier: Basic Foundations (Start from Level 1!)"
                        }
                        Text(rec, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
