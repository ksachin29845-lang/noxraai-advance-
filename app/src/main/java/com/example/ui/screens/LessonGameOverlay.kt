package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.SpeechAndAudioManager
import com.example.data.curriculum.ExerciseType
import com.example.data.curriculum.LessonNode
import com.example.ui.theme.HeartCrimsonRed

import androidx.compose.material.icons.filled.AutoAwesome
import com.example.ui.viewmodel.AiTaskState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LessonGameOverlay(
    lesson: LessonNode,
    learningLanguage: String,
    hearts: Long,
    relaxedMode: Boolean,
    audioManager: SpeechAndAudioManager,
    explainAnswerState: AiTaskState<String> = AiTaskState.Idle,
    onExplainAnswer: (prompt: String, userAttempt: String, correctAnswer: String, wasCorrect: Boolean) -> Unit = { _, _, _, _ -> },
    onClearExplainAnswer: () -> Unit = {},
    onMistake: (vocabWord: String, vocabMeaning: String) -> Unit,
    onLessonComplete: (mistakesCount: Int) -> Unit,
    onExit: () -> Unit
) {
    BackHandler { onExit() }

    val totalQuestions = lesson.questions.size
    var currentIndex by remember(lesson.id) { mutableIntStateOf(0) }
    var mistakesCount by remember(lesson.id) { mutableIntStateOf(0) }
    var showCelebration by remember(lesson.id) { mutableStateOf(false) }

    // Per-question answer states
    val question = lesson.questions.getOrNull(currentIndex)
    var selectedChoice by remember(currentIndex) { mutableStateOf<String?>(null) }
    var typedInput by remember(currentIndex) { mutableStateOf("") }
    var showAudioHintFallback by remember(currentIndex) { mutableStateOf(false) }
    var useKeyboardForSpeech by remember(currentIndex) { mutableStateOf(false) }
    val arrangedWords = remember(currentIndex) { mutableStateListOf<String>() }
    val matchedLeftKeys = remember(currentIndex) { mutableStateListOf<String>() }
    var pendingLeftMatch by remember(currentIndex) { mutableStateOf<String?>(null) }
    var checkedState by remember(currentIndex) { mutableStateOf<Boolean?>(null) } // null = not checked, true = correct, false = wrong

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                typedInput = spoken
            }
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (showCelebration) 1f else (currentIndex.toFloat() / totalQuestions.coerceAtLeast(1).toFloat()),
        label = "lesson_progress"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("lesson_game_overlay"),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .widthIn(max = 720.dp)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Progress & Hearts Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onExit,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("exit_lesson_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Exit lesson")
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = HeartCrimsonRed.copy(alpha = 0.14f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Hearts remaining",
                            tint = HeartCrimsonRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (relaxedMode) "∞" else "$hearts",
                            style = MaterialTheme.typography.labelLarge,
                            color = HeartCrimsonRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (showCelebration || question == null) {
                // Lesson Celebration Summary
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Surface(
                        shape = CircleShape,
                        tonalElevation = 6.dp,
                        modifier = Modifier.size(132.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_kavi_mascot),
                            contentDescription = "Kavi celebrating lesson completion",
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (mistakesCount == 0) "🌟 Perfect Lesson!" else "🎉 Lesson Complete!",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = lesson.title,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(18.dp))

                    val accuracy = (((totalQuestions - mistakesCount).coerceAtLeast(1) * 100) / totalQuestions.coerceAtLeast(1))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatBadgeCard("⭐ XP Earned", "+${lesson.xpReward + if (mistakesCount == 0) 10 else 0}", Modifier.weight(1f))
                        StatBadgeCard("💎 Gems", "+${lesson.gemReward}", Modifier.weight(1f))
                        StatBadgeCard("🎯 Accuracy", "$accuracy%", Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Button(
                        onClick = { onLessonComplete(mistakesCount) },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 54.dp)
                            .testTag("claim_lesson_rewards_button")
                    ) {
                        Text("Claim XP & Continue", style = MaterialTheme.typography.titleMedium)
                    }
                }
            } else {
                // Active Interactive Question Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = question.type.label,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }

                    Text(
                        text = question.prompt,
                        style = MaterialTheme.typography.headlineSmall
                    )

                    // Audio Playback Strip (Normal & Turtle Slow Speed)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { audioManager.speak(question.audioPhrase, learningLanguage, slowSpeed = false) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("lesson_tts_normal_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen normal speed")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Listen")
                        }
                        OutlinedButton(
                            onClick = { audioManager.speak(question.audioPhrase, learningLanguage, slowSpeed = true) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("lesson_tts_slow_button")
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = "Listen slow turtle speed")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🐢 Slow")
                        }
                    }

                    when (question.type) {
                        ExerciseType.ARRANGE_WORDS -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = if (arrangedWords.isEmpty()) "Tap words below to build your sentence..."
                                        else arrangedWords.joinToString(" "),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (arrangedWords.isNotEmpty() && checkedState == null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            arrangedWords.forEachIndexed { idx, w ->
                                                ElevatedFilterChip(
                                                    selected = true,
                                                    onClick = { arrangedWords.removeAt(idx) },
                                                    label = { Text("$w ✕") },
                                                    modifier = Modifier.heightIn(min = 48.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                question.wordBank.forEach { word ->
                                    val alreadyUsed = arrangedWords.contains(word)
                                    ElevatedFilterChip(
                                        selected = alreadyUsed,
                                        enabled = !alreadyUsed && checkedState == null,
                                        onClick = { arrangedWords.add(word) },
                                        label = { Text(word) },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    )
                                }
                            }
                        }

                        ExerciseType.WORD_MATCH -> {
                            Text(
                                text = "Tap a word on the left, then tap its matching meaning on the right:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            val rightMeanings = remember(question.id) {
                                question.matchPairs.map { it.second }.shuffled()
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    question.matchPairs.forEach { (leftWord, _) ->
                                        val done = matchedLeftKeys.contains(leftWord)
                                        val active = pendingLeftMatch == leftWord
                                        OutlinedButton(
                                            onClick = { pendingLeftMatch = leftWord },
                                            enabled = !done && checkedState == null,
                                            border = BorderStroke(
                                                width = if (active) 2.dp else 1.dp,
                                                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                        ) {
                                            Text(if (done) "✅ $leftWord" else leftWord)
                                        }
                                    }
                                }
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rightMeanings.forEach { meaning ->
                                        val ownerLeft = question.matchPairs.firstOrNull { it.second == meaning }?.first
                                        val done = ownerLeft != null && matchedLeftKeys.contains(ownerLeft)
                                        OutlinedButton(
                                            onClick = {
                                                val chosenLeft = pendingLeftMatch
                                                if (chosenLeft != null) {
                                                    val expectedMeaning = question.matchPairs.firstOrNull { it.first == chosenLeft }?.second
                                                    if (expectedMeaning == meaning) {
                                                        matchedLeftKeys.add(chosenLeft)
                                                        if (matchedLeftKeys.size == question.matchPairs.size) {
                                                            selectedChoice = "ALL_MATCHED"
                                                        }
                                                    } else {
                                                        mistakesCount++
                                                        onMistake(question.keyVocabWord, question.keyVocabMeaning)
                                                    }
                                                    pendingLeftMatch = null
                                                }
                                            },
                                            enabled = !done && checkedState == null,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                        ) {
                                            Text(if (done) "✅ $meaning" else meaning)
                                        }
                                    }
                                }
                            }
                        }

                        ExerciseType.TYPE_TRANSLATION, ExerciseType.FIX_GRAMMAR -> {
                            OutlinedTextField(
                                value = typedInput,
                                onValueChange = { if (checkedState == null) typedInput = it },
                                label = {
                                    Text(
                                        if (question.type == ExerciseType.FIX_GRAMMAR) "Type the corrected sentence"
                                        else "Type your translation"
                                    )
                                },
                                enabled = checkedState == null,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("lesson_typed_answer_input")
                            )
                        }

                        ExerciseType.LISTEN_AND_TYPE -> {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "🎧 Tap Listen above, then type what you hear:",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (showAudioHintFallback) {
                                        Text(
                                            text = "Quiet-mode hint: \"${question.correctAnswer}\"",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        OutlinedButton(
                                            onClick = { showAudioHintFallback = true },
                                            modifier = Modifier.heightIn(min = 48.dp)
                                        ) {
                                            Text("Can't listen right now? Show text hint")
                                        }
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = typedInput,
                                onValueChange = { if (checkedState == null) typedInput = it },
                                label = { Text("Type what you heard") },
                                enabled = checkedState == null,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("lesson_listen_type_input")
                            )
                        }

                        ExerciseType.SPEAK_SENTENCE -> {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "🗣️ \"${question.correctAnswer}\"",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Button(
                                            onClick = {
                                                try {
                                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                        putExtra(
                                                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                                        )
                                                        putExtra(RecognizerIntent.EXTRA_PROMPT, question.correctAnswer)
                                                    }
                                                    speechLauncher.launch(intent)
                                                } catch (_: Exception) {
                                                    useKeyboardForSpeech = true
                                                }
                                            },
                                            enabled = checkedState == null,
                                            modifier = Modifier
                                                .weight(1f)
                                                .heightIn(min = 48.dp)
                                                .testTag("lesson_speak_mic_button")
                                        ) {
                                            Icon(Icons.Default.Mic, contentDescription = "Speak sentence")
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Tap to Speak")
                                        }
                                        OutlinedButton(
                                            onClick = { useKeyboardForSpeech = !useKeyboardForSpeech },
                                            enabled = checkedState == null,
                                            modifier = Modifier.heightIn(min = 48.dp)
                                        ) {
                                            Text(if (useKeyboardForSpeech) "Hide Keyboard" else "Can't speak?")
                                        }
                                    }
                                    if (typedInput.isNotBlank() && !useKeyboardForSpeech) {
                                        Text(
                                            text = "Heard: \"$typedInput\"",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    if (useKeyboardForSpeech) {
                                        OutlinedTextField(
                                            value = typedInput,
                                            onValueChange = { if (checkedState == null) typedInput = it },
                                            label = { Text("Type the phrase instead") },
                                            enabled = checkedState == null,
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("lesson_speak_fallback_input")
                                        )
                                    }
                                }
                            }
                        }

                        else -> {
                            question.options.forEachIndexed { idx, opt ->
                                val isSelected = selectedChoice == opt
                                Card(
                                    onClick = {
                                        if (checkedState == null) {
                                            selectedChoice = opt
                                            audioManager.speak(opt, learningLanguage, slowSpeed = false)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 56.dp)
                                        .testTag("option_card_$idx"),
                                    shape = RoundedCornerShape(18.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = opt,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Instant Feedback Explanation Card + Streaming "Explain My Answer" AI
                    if (checkedState != null) {
                        val isCorrect = checkedState == true
                        val currentAttemptText = when (question.type) {
                            ExerciseType.ARRANGE_WORDS -> arrangedWords.joinToString(" ")
                            ExerciseType.WORD_MATCH -> "Matched pairs"
                            ExerciseType.TYPE_TRANSLATION,
                            ExerciseType.LISTEN_AND_TYPE,
                            ExerciseType.SPEAK_SENTENCE,
                            ExerciseType.FIX_GRAMMAR -> typedInput
                            else -> selectedChoice.orEmpty()
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCorrect) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.errorContainer
                                }
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (isCorrect) "✅ Spot on! Awesome work!" else "💡 Almost! Correct answer: ${question.correctAnswer}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = question.explanation,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                OutlinedButton(
                                    onClick = {
                                        onExplainAnswer(
                                            question.prompt,
                                            currentAttemptText,
                                            question.correctAnswer,
                                            isCorrect
                                        )
                                    },
                                    enabled = explainAnswerState !is AiTaskState.Loading,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("explain_my_answer_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("✨ Explain My Answer with AI")
                                }

                                when (explainAnswerState) {
                                    is AiTaskState.Loading -> {
                                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                                    }
                                    is AiTaskState.Error -> {
                                        Text(
                                            text = explainAnswerState.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                    is AiTaskState.Success -> {
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                                        ) {
                                            Text(
                                                text = explainAnswerState.data,
                                                modifier = Modifier.padding(12.dp),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    AiTaskState.Idle -> {}
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bottom Check / Continue Action Button
                val userCandidate = when (question.type) {
                    ExerciseType.ARRANGE_WORDS -> arrangedWords.joinToString(" ")
                    ExerciseType.WORD_MATCH -> if (matchedLeftKeys.size == question.matchPairs.size) "ALL_MATCHED" else ""
                    ExerciseType.TYPE_TRANSLATION,
                    ExerciseType.LISTEN_AND_TYPE,
                    ExerciseType.SPEAK_SENTENCE,
                    ExerciseType.FIX_GRAMMAR -> typedInput
                    else -> selectedChoice.orEmpty()
                }

                Button(
                    onClick = {
                        if (checkedState == null) {
                            val ok = normalizeAnswerForCheck(userCandidate) == normalizeAnswerForCheck(question.correctAnswer)
                            checkedState = ok
                            if (!ok) {
                                mistakesCount++
                                onMistake(question.keyVocabWord, question.keyVocabMeaning)
                            }
                        } else {
                            onClearExplainAnswer()
                            if (currentIndex + 1 < totalQuestions) {
                                currentIndex++
                            } else {
                                showCelebration = true
                            }
                        }
                    },
                    enabled = userCandidate.isNotBlank() || checkedState != null,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp)
                        .testTag("lesson_check_continue_button")
                ) {
                    Text(
                        text = if (checkedState == null) "Check Answer" else "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StatBadgeCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun normalizeAnswerForCheck(raw: String): String {
    return raw.trim()
        .lowercase()
        .replace(Regex("[.?!,;:\"']"), "")
        .replace(Regex("\\s+"), " ")
}
