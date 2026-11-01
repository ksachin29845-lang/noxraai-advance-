package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.SpeechAndAudioManager
import com.example.data.curriculum.FiftyLevelMasteryCurriculum
import com.example.data.curriculum.FiftyLevelNode
import com.example.data.model.UserProfile
import com.example.ui.theme.StreakFlameOrange
import com.example.ui.theme.XpGoldYellow
import com.example.ui.viewmodel.AiTaskState
import com.example.ui.viewmodel.AiTutorViewModel

@Composable
fun FiftyLevelPathScreen(
    isMath: Boolean,
    profile: UserProfile,
    aiTutorViewModel: AiTutorViewModel,
    audioManager: SpeechAndAudioManager,
    onSelectClassAndTier: (studentClass: String, tier: String) -> Unit,
    onCompleteLevel: (levelNumber: Int, studentClass: String, tier: String, mistakesCount: Int) -> Unit,
    onOpenNoxraAiChat: () -> Unit,
    onBack: () -> Unit
) {
    val initialClass = if (isMath) {
        profile.mathClass ?: profile.studentClass ?: "Class 8"
    } else {
        profile.englishClass ?: profile.studentClass ?: "Class 8"
    }
    val initialTier = if (isMath) {
        profile.mathTier ?: "Basic"
    } else {
        profile.englishTier ?: "Basic"
    }

    var selectedClass by rememberSaveable(isMath, initialClass) {
        mutableStateOf(
            if (initialClass in FiftyLevelMasteryCurriculum.SUPPORTED_CLASSES) initialClass else "Class 8"
        )
    }
    var selectedTier by rememberSaveable(isMath, initialTier) {
        mutableStateOf(
            if (initialTier in FiftyLevelMasteryCurriculum.SUPPORTED_TIERS) initialTier else "Basic"
        )
    }

    val unlockedLevel = (if (isMath) profile.mathUnlockedLevel ?: 1L else profile.englishUnlockedLevel ?: 1L)
        .toInt()
        .coerceIn(1, 50)

    val levels = remember(isMath, selectedClass, selectedTier) {
        if (isMath) {
            FiftyLevelMasteryCurriculum.generateMathFiftyLevels(selectedClass, selectedTier)
        } else {
            FiftyLevelMasteryCurriculum.generateEnglishFiftyLevels(selectedClass, selectedTier)
        }
    }

    var activeLevelNumber by rememberSaveable(isMath) { mutableStateOf<Int?>(null) }
    val activeNode = remember(activeLevelNumber, levels) {
        activeLevelNumber?.let { num -> levels.firstOrNull { it.levelNumber == num } }
    }

    BackHandler {
        if (activeLevelNumber != null) {
            aiTutorViewModel.clearFiftyLevelAiStates()
            activeLevelNumber = null
        } else {
            onBack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        if (activeNode != null) {
            FiftyLevelSessionPlayer(
                isMath = isMath,
                node = activeNode,
                studentClass = selectedClass,
                tier = selectedTier,
                isSubscribed = profile.subscriptionActive == true,
                aiTutorViewModel = aiTutorViewModel,
                audioManager = audioManager,
                onExitNode = {
                    aiTutorViewModel.clearFiftyLevelAiStates()
                    activeLevelNumber = null
                },
                onCompleteNode = { mistakes ->
                    onCompleteLevel(activeNode.levelNumber, selectedClass, selectedTier, mistakes)
                    aiTutorViewModel.clearFiftyLevelAiStates()
                    activeLevelNumber = null
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 700.dp)
                    .testTag("fifty_level_nodes_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("fifty_level_back_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back to Dashboard"
                                )
                            }
                            Column {
                                Text(
                                    text = if (isMath) "➗ Math Practice (Levels 1–50)" else "🇬🇧 English Practice (Levels 1–50)",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Select Class • Basic / Medium / Hard • Noxra AI Powered",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = onOpenNoxraAiChat,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("fifty_level_open_noxra_ai_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("✨ Noxra AI", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fifty_level_selector_card"),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "1️⃣ Select Your Class",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(FiftyLevelMasteryCurriculum.SUPPORTED_CLASSES) { cls ->
                                    FilterChip(
                                        selected = selectedClass == cls,
                                        onClick = {
                                            selectedClass = cls
                                            onSelectClassAndTier(cls, selectedTier)
                                        },
                                        label = { Text(cls, fontWeight = FontWeight.SemiBold) },
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("class_chip_${cls.lowercase().replace(" ", "_")}")
                                    )
                                }
                            }

                            Text(
                                text = "2️⃣ Select Difficulty Level (Basic • Medium • Hard)",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FiftyLevelMasteryCurriculum.SUPPORTED_TIERS.forEach { tier ->
                                    val emoji = when (tier) {
                                        "Basic" -> "🌱"
                                        "Mid" -> "⚡"
                                        else -> "🔥"
                                    }
                                    val displayTier = when (tier) {
                                        "Basic" -> "Basic"
                                        "Mid" -> "Medium"
                                        else -> "Hard"
                                    }
                                    FilterChip(
                                        selected = selectedTier == tier,
                                        onClick = {
                                            selectedTier = tier
                                            onSelectClassAndTier(selectedClass, tier)
                                        },
                                        label = { Text("$emoji $displayTier", fontWeight = FontWeight.ExtraBold) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("tier_chip_${tier.lowercase()}")
                                    )
                                }
                            }

                            val currentTierLabel = when (selectedTier) {
                                "Basic" -> "Basic"
                                "Mid" -> "Medium"
                                else -> "Hard"
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Unlocked Level: $unlockedLevel / 50 ($selectedClass • $currentTierLabel)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                if (profile.subscriptionActive == true) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = StreakFlameOrange.copy(alpha = 0.18f)
                                    ) {
                                        Text(
                                            text = "🔥 Naruto ₹49 • 2x XP",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = StreakFlameOrange
                                        )
                                    }
                                }
                            }
                            LinearProgressIndicator(
                                progress = { (unlockedLevel / 50f).coerceIn(0.02f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                        }
                    }
                }

                items(levels, key = { it.levelNumber }) { node ->
                    val showUnitBanner = (node.levelNumber - 1) % 10 == 0
                    if (showUnitBanner) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            shape = RoundedCornerShape(18.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = node.unitTitle,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    val isCompleted = node.levelNumber < unlockedLevel
                    val isCurrent = node.levelNumber == unlockedLevel
                    val isUnlocked = node.levelNumber <= unlockedLevel
                    val waveOffsets = listOf(0.dp, 22.dp, 0.dp, (-22).dp)
                    val horizontalOffset = waveOffsets[(node.levelNumber - 1) % waveOffsets.size]

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .offset(x = horizontalOffset)
                            .testTag("level_node_${node.levelNumber}")
                            .clickable(enabled = isUnlocked) {
                                aiTutorViewModel.clearFiftyLevelAiStates()
                                activeLevelNumber = node.levelNumber
                            },
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                isCurrent -> MaterialTheme.colorScheme.primaryContainer
                                isCompleted -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            }
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 5.dp else 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isCurrent -> MaterialTheme.colorScheme.primary
                                            isCompleted -> XpGoldYellow
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked Level ${node.levelNumber}",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(
                                        text = "${node.levelNumber}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${node.iconEmoji} ${node.title}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (isUnlocked) node.subtitle else "Complete Level ${node.levelNumber - 1} to unlock",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            when {
                                isCompleted -> Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Level Completed",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                isCurrent -> Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Start Level",
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "START",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimary
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

@Composable
private fun FiftyLevelSessionPlayer(
    isMath: Boolean,
    node: FiftyLevelNode,
    studentClass: String,
    tier: String,
    isSubscribed: Boolean,
    aiTutorViewModel: AiTutorViewModel,
    audioManager: SpeechAndAudioManager,
    onExitNode: () -> Unit,
    onCompleteNode: (mistakesCount: Int) -> Unit
) {
    var questionIdx by rememberSaveable(node.levelNumber) { mutableIntStateOf(0) }
    var selectedOption by rememberSaveable(node.levelNumber, questionIdx) { mutableStateOf<String?>(null) }
    var checked by rememberSaveable(node.levelNumber, questionIdx) { mutableStateOf(false) }
    var mistakes by rememberSaveable(node.levelNumber) { mutableIntStateOf(0) }
    var bonusChoice by remember { mutableStateOf<String?>(null) }

    val aiHintState by aiTutorViewModel.fiftyLevelAiHintState.collectAsState()
    val aiBonusState by aiTutorViewModel.fiftyLevelBonusQuestionState.collectAsState()

    val isFinished = questionIdx >= node.questions.size
    val currentQuestion = node.questions.getOrNull(questionIdx)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onExitNode,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("exit_fifty_level_session_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Exit Level Practice"
                )
            }
            Text(
                text = "${node.title} • $studentClass",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.weight(1f)
            )
            if (!isFinished && currentQuestion != null) {
                IconButton(
                    onClick = { audioManager.speak(currentQuestion.prompt, "English") },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Listen to Question"
                    )
                }
            }
        }

        if (isFinished) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = "Level Completed Trophy",
                        tint = XpGoldYellow,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Level ${node.levelNumber} Mastered!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "$studentClass • $tier Tier • Mistakes: $mistakes" +
                            if (isSubscribed) " • 🔥 2x Naruto Event ₹49 XP Applied!" else "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(
                        onClick = { onCompleteNode(mistakes) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                            .testTag("fifty_level_complete_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = if (node.levelNumber < 50) {
                                "🏆 Claim XP & Unlock Level ${node.levelNumber + 1}"
                            } else {
                                "👑 Claim Level 50 Grand Mastery Reward"
                            },
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        } else if (currentQuestion != null) {
            LinearProgressIndicator(
                progress = { ((questionIdx + 1).toFloat() / node.questions.size.toFloat()).coerceIn(0.1f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Question ${questionIdx + 1} of ${node.questions.size} • $tier Tier",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = currentQuestion.prompt,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (currentQuestion.audioHint.isNotBlank()) {
                        Text(
                            text = "💡 Hint: ${currentQuestion.audioHint}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            currentQuestion.options.forEachIndexed { idx, opt ->
                val isPicked = selectedOption == opt
                val isCorrectOpt = opt == currentQuestion.correctAnswer
                val bg = when {
                    checked && isCorrectOpt -> MaterialTheme.colorScheme.primaryContainer
                    checked && isPicked && !isCorrectOpt -> MaterialTheme.colorScheme.errorContainer
                    isPicked -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("fifty_level_option_$idx")
                        .clickable(enabled = !checked) { selectedOption = opt },
                    shape = RoundedCornerShape(16.dp),
                    color = bg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${('A' + idx)}. $opt",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isPicked) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                }
            }

            if (checked) {
                val gotRight = selectedOption == currentQuestion.correctAnswer
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (gotRight) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (gotRight) "✅ Correct! Great work!" else "💡 Correct Answer: ${currentQuestion.correctAnswer}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentQuestion.explanation,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Button(
                onClick = {
                    if (!checked) {
                        checked = true
                        if (selectedOption != currentQuestion.correctAnswer) {
                            mistakes += 1
                        }
                    } else {
                        aiTutorViewModel.clearFiftyLevelAiStates()
                        bonusChoice = null
                        questionIdx += 1
                    }
                },
                enabled = selectedOption != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .testTag("fifty_level_check_or_next_button"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (!checked) "Check Answer" else "Next Step →",
                    fontWeight = FontWeight.ExtraBold
                )
            }

            // Noxra AI Tutor Tools embedded inside every English & Math Level 1..50!
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                                )
                            )
                        )
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Noxra AI Coach",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "✨ Noxra AI Level ${node.levelNumber} Coach",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                aiTutorViewModel.explainFiftyLevelConcept(
                                    isMath = isMath,
                                    studentClass = studentClass,
                                    tier = tier,
                                    levelNumber = node.levelNumber,
                                    questionPrompt = currentQuestion.prompt,
                                    correctAnswer = currentQuestion.correctAnswer
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("fifty_level_ai_explain_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("✨ AI Step-by-Step", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                bonusChoice = null
                                aiTutorViewModel.generateFiftyLevelAiQuestion(
                                    isMath = isMath,
                                    studentClass = studentClass,
                                    tier = tier,
                                    levelNumber = node.levelNumber
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("fifty_level_ai_bonus_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("🤖 AI Bonus Q", fontWeight = FontWeight.Bold)
                        }
                    }

                    when (val hint = aiHintState) {
                        is AiTaskState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Noxra AI is writing easy steps...", style = MaterialTheme.typography.bodySmall)
                        }
                        is AiTaskState.Success -> Text(
                            text = hint.data,
                            style = MaterialTheme.typography.bodySmall
                        )
                        is AiTaskState.Error -> Text(
                            text = hint.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        AiTaskState.Idle -> Unit
                    }

                    when (val bonus = aiBonusState) {
                        is AiTaskState.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Noxra AI is crafting a fresh Level ${node.levelNumber} question...", style = MaterialTheme.typography.bodySmall)
                        }
                        is AiTaskState.Success -> {
                            val item = bonus.data
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "🤖 Noxra AI Bonus: ${item.question}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    item.options.forEach { opt ->
                                        OutlinedButton(
                                            onClick = { bonusChoice = opt },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 48.dp)
                                        ) {
                                            Text(opt)
                                        }
                                    }
                                    if (bonusChoice != null) {
                                        val ok = bonusChoice == item.correctAnswer
                                        Text(
                                            text = (if (ok) "✅ Spot on! " else "💡 Answer: ${item.correctAnswer}. ") + item.explanation,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                        is AiTaskState.Error -> Text(
                            text = bonus.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        AiTaskState.Idle -> Unit
                    }
                }
            }
        }
    }
}
