package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.SpeechAndAudioManager
import com.example.data.curriculum.LanguageCurriculum
import com.example.data.model.AppHistoryItem
import com.example.data.model.UserProfile
import com.example.data.repository.AppPreferencesState
import com.example.ui.viewmodel.AiTaskState
import com.example.ui.viewmodel.AiTutorViewModel
import com.example.ui.viewmodel.ChatViewModel

@Composable
fun LearnScreen(
    profile: UserProfile,
    preferences: AppPreferencesState,
    chatViewModel: ChatViewModel,
    aiTutorViewModel: AiTutorViewModel,
    audioManager: SpeechAndAudioManager,
    weakVocabularyWords: List<String> = emptyList(),
    savedAiHistory: List<AppHistoryItem> = emptyList(),
    onDeleteSavedHistory: (String) -> Unit = {},
    learnFocusKey: Int = 0,
    initialSection: Int = 0,
    initialSubjectLabIndex: Int = 0,
    onEarnSubjectXp: (subjectName: String, xp: Int, gems: Int) -> Unit = { _, _, _ -> },
    onBookmarkWord: (word: String, translation: String, lang: String, phonetic: String?, example: String?) -> Unit,
    onRecordSearch: (String) -> Unit,
    onToggleOfflinePack: (String) -> Unit,
    onOpenEnglishFiftyLevels: () -> Unit = {},
    onOpenMathFiftyLevels: () -> Unit = {},
    onShowMessage: (String) -> Unit = {}
) {
    var selectedSection by rememberSaveable { mutableIntStateOf(initialSection) }
    LaunchedEffect(learnFocusKey) {
        if (learnFocusKey > 0) {
            selectedSection = initialSection
        }
    }

    val sections = listOf(
        "✨ Noxra AI",
        "🎯 Basic → Hard Paths",
        "🏫 Subjects • Maths • Music • Chess",
        "📚 Grammar & Script",
        "📖 Dictionary",
        "📶 Offline"
    )

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
                        .testTag("learn_tab_$idx")
                )
            }
        }

        when (selectedSection) {
            0 -> ChatScreen(
                chatViewModel = chatViewModel,
                profile = profile,
                weakVocabularyWords = weakVocabularyWords,
                savedAiHistory = savedAiHistory,
                onDeleteSavedHistory = onDeleteSavedHistory,
                aiTutorViewModel = aiTutorViewModel,
                audioManager = audioManager,
                onShowMessage = onShowMessage
            )
            1 -> BasicToHardProgressionHubPane(
                profile = profile,
                onOpenEnglishFiftyLevels = onOpenEnglishFiftyLevels,
                onOpenMathFiftyLevels = onOpenMathFiftyLevels
            )
            2 -> MultiSubjectLabsPane(
                initialSubjectIndex = initialSubjectLabIndex,
                studentClassHint = profile.studentClass,
                onEarnSubjectXp = onEarnSubjectXp,
                onOpenMathFiftyLevels = onOpenMathFiftyLevels
            )
            3 -> GrammarAndAlphabetPane(
                learningLanguage = profile.learningLanguage,
                audioManager = audioManager
            )
            4 -> DictionaryAndTranslatorPane(
                profile = profile,
                preferences = preferences,
                aiTutorViewModel = aiTutorViewModel,
                audioManager = audioManager,
                onBookmarkWord = onBookmarkWord,
                onRecordSearch = onRecordSearch
            )
            5 -> OfflineLearningPane(
                preferences = preferences,
                onToggleOfflinePack = onToggleOfflinePack
            )
        }
    }
}

@Composable
private fun BasicToHardProgressionHubPane(
    profile: UserProfile,
    onOpenEnglishFiftyLevels: () -> Unit,
    onOpenMathFiftyLevels: () -> Unit
) {
    val engTierLabel = when (profile.englishTier) {
        "Mid" -> "Medium"
        "Advance" -> "Hard"
        else -> "Basic"
    }
    val mathTierLabel = when (profile.mathTier) {
        "Mid" -> "Medium"
        "Advance" -> "Hard"
        else -> "Basic"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("learn_basic_to_hard_pane"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "🇬🇧 English Mastery Path (Basic → Medium → Hard)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Active: ${profile.englishClass ?: profile.studentClass ?: "Class 8"} • $engTierLabel Tier • Level ${profile.englishUnlockedLevel ?: 1L} / 50",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    LinearProgressIndicator(
                        progress = { ((profile.englishUnlockedLevel ?: 1L) / 50f).coerceIn(0.04f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(CircleShape)
                    )
                    Button(
                        onClick = onOpenEnglishFiftyLevels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("learn_open_english_50_button")
                    ) {
                        Text("▶ Practice English Levels 1–50", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "➗ Math Mastery Path (Basic → Medium → Hard + AI)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Active: ${profile.mathClass ?: profile.studentClass ?: "Class 8"} • $mathTierLabel Tier • Level ${profile.mathUnlockedLevel ?: 1L} / 50",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    LinearProgressIndicator(
                        progress = { ((profile.mathUnlockedLevel ?: 1L) / 50f).coerceIn(0.04f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(9.dp)
                            .clip(CircleShape)
                    )
                    Button(
                        onClick = onOpenMathFiftyLevels,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("learn_open_math_50_button")
                    ) {
                        Text("▶ Practice Math Levels 1–50 + AI", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun GrammarAndAlphabetPane(
    learningLanguage: String,
    audioManager: SpeechAndAudioManager
) {
    val alphabetCards = remember(learningLanguage) {
        LanguageCurriculum.alphabetCardsFor(learningLanguage)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "🔤 $learningLanguage Alphabet & Sound Guide",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                alphabetCards.forEach { (symbol, sound, example) ->
                    Card(
                        onClick = { audioManager.speak(example, learningLanguage, false) },
                        modifier = Modifier.width(156.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(symbol, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                            Text("[$sound]", style = MaterialTheme.typography.labelLarge)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(example, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "✍️ Grammar & Sentence Structure Tutorials",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        items(LanguageCurriculum.grammarTopics, key = { it.id }) { topic ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(topic.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                            Text(topic.level, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    Text(topic.summary, style = MaterialTheme.typography.bodyMedium)
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Text(
                            text = "📐 Formula: ${topic.formula}",
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    topic.examples.forEach { (eng, hin) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { audioManager.speak(eng, "English", false) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen example")
                            }
                            Column {
                                Text(eng, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(hin, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                    Text(
                        text = "💡 Pro Tip: ${topic.commonMistakeTip}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun DictionaryAndTranslatorPane(
    profile: UserProfile,
    preferences: AppPreferencesState,
    aiTutorViewModel: AiTutorViewModel,
    audioManager: SpeechAndAudioManager,
    onBookmarkWord: (String, String, String, String?, String?) -> Unit,
    onRecordSearch: (String) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    val translationState by aiTutorViewModel.translationResult.collectAsState()

    val filteredWords = remember(query) {
        if (query.isBlank()) {
            LanguageCurriculum.builtInDictionary
        } else {
            LanguageCurriculum.builtInDictionary.filter {
                it.word.contains(query, ignoreCase = true) ||
                    it.meaningGlobal.contains(query, ignoreCase = true) ||
                    it.meaningHindi.contains(query, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search Dictionary or enter any sentence to translate") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dictionary_search_input")
            )
        }

        // Recent Search Chips
        if (preferences.recentDictionarySearches.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    preferences.recentDictionarySearches.forEach { recent ->
                        FilterChip(
                            selected = query.equals(recent, ignoreCase = true),
                            onClick = { query = recent },
                            label = { Text("🕒 $recent") },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
            }
        }

        // AI Context-Aware Translator Action
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🌐 AI Context-Aware Translator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Button(
                            onClick = {
                                if (query.isNotBlank()) {
                                    onRecordSearch(query)
                                    aiTutorViewModel.translateWithContext(
                                        inputPhrase = query,
                                        sourceOrTargetLang = profile.learningLanguage,
                                        nativeLanguage = profile.nativeLanguage
                                    )
                                }
                            },
                            enabled = query.isNotBlank(),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("ai_translate_button")
                        ) {
                            Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Translate")
                        }
                    }
                    when (val tState = translationState) {
                        is AiTaskState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        is AiTaskState.Error -> Text(tState.message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        is AiTaskState.Success -> Text(tState.data, style = MaterialTheme.typography.bodyMedium)
                        AiTaskState.Idle -> Text(
                            "Type any word, idiom, or paragraph above and tap Translate for deep nuance & examples!",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        items(filteredWords, key = { it.word }) { entry ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${entry.word}  ${entry.phonetic}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("${entry.language} • ${entry.partOfSpeech}", style = MaterialTheme.typography.labelMedium)
                        }
                        Row {
                            IconButton(
                                onClick = { audioManager.speak(entry.word, entry.language, false) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Pronounce word")
                            }
                            IconButton(
                                onClick = { audioManager.speak(entry.word, entry.language, true) },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.Speed, contentDescription = "Slow pronunciation")
                            }
                            IconButton(
                                onClick = {
                                    onBookmarkWord(
                                        entry.word,
                                        "${entry.meaningHindi} — ${entry.meaningGlobal}",
                                        entry.language,
                                        entry.phonetic,
                                        entry.exampleSentence
                                    )
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = "Save to Smart Vocabulary Notebook")
                            }
                        }
                    }
                    Text("🇮🇳 ${entry.meaningHindi}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(entry.meaningGlobal, style = MaterialTheme.typography.bodyMedium)
                    Text("✨ Synonyms: ${entry.synonyms.joinToString(", ")}  |  ↔ Antonyms: ${entry.antonyms.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
                    Text("🔄 Forms: ${entry.conjugations.joinToString(" • ")}", style = MaterialTheme.typography.bodySmall)
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("“${entry.exampleSentence}”", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                            Text(entry.exampleTranslation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OfflineLearningPane(
    preferences: AppPreferencesState,
    onToggleOfflinePack: (String) -> Unit
) {
    val downloadedCount = preferences.downloadedOfflineCourses.size
    val estimatedMb = downloadedCount * 14

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("📶 Offline Course & Flashcard Manager", style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = "$downloadedCount course pack(s) cached on device (~$estimatedMb MB used). Offline lessons, flashcards, and grammar notes work without internet, and progress syncs automatically via Cloud Firestore when back online!",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        items(LanguageCurriculum.supportedLanguages, key = { it.name }) { lang ->
            val isDownloaded = preferences.downloadedOfflineCourses.contains(lang.name)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Text(lang.flagEmoji, style = MaterialTheme.typography.headlineMedium)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("${lang.name} Complete Pack", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Lessons, Flashcards & Grammar Notes • 14 MB", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    OutlinedButton(
                        onClick = { onToggleOfflinePack(lang.name) },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(
                            imageVector = if (isDownloaded) Icons.Default.CloudDone else Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isDownloaded) "Saved" else "Download")
                    }
                }
            }
        }
    }
}
