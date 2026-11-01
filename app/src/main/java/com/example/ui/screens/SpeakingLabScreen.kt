package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.core.RecordedSpeechClip
import com.example.core.SpeakingAudioRecorder
import com.example.core.SpeechAndAudioManager
import com.example.data.curriculum.LanguageCurriculum
import com.example.data.model.UserProfile
import com.example.ui.theme.HeartCrimsonRed
import com.example.ui.theme.StreakFlameOrange
import com.example.ui.theme.XpGoldYellow
import com.example.ui.viewmodel.LiveCoachState
import com.example.ui.viewmodel.PronunciationFeedbackReport
import com.example.ui.viewmodel.ScorecardState
import com.example.ui.viewmodel.SpeakingLabViewModel
import com.example.ui.viewmodel.WordPronunciationItem
import java.util.Locale
import kotlinx.coroutines.delay

private const val MAX_RECORD_SECONDS = 25L

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpeakingLabContent(
    profile: UserProfile,
    speakingLabViewModel: SpeakingLabViewModel,
    audioManager: SpeechAndAudioManager,
    onRegainHeart: () -> Unit,
    onBookmarkWord: (word: String, translation: String, language: String, phonetic: String?, example: String?) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val recorder = remember(context) { SpeakingAudioRecorder(context.applicationContext) }
    DisposableEffect(recorder) {
        onDispose { recorder.release() }
    }

    val liveCoachState by speakingLabViewModel.liveCoachState.collectAsStateWithLifecycle()
    val scorecardState by speakingLabViewModel.scorecardState.collectAsStateWithLifecycle()
    val attemptHistory by speakingLabViewModel.attemptHistory.collectAsStateWithLifecycle()

    val drills = remember(profile.learningLanguage) {
        LanguageCurriculum.speakingDrillsForLanguage(profile.learningLanguage)
    }
    var currentDrillIdx by rememberSaveable(profile.learningLanguage) { mutableIntStateOf(0) }
    var useCustomPhrase by rememberSaveable { mutableStateOf(false) }
    var customPhraseInput by rememberSaveable { mutableStateOf("") }

    val activeDrill = drills[currentDrillIdx % drills.size]
    val targetPhrase = if (useCustomPhrase && customPhraseInput.isNotBlank()) {
        customPhraseInput.trim()
    } else {
        activeDrill.phrase
    }

    var isRecording by remember { mutableStateOf(false) }
    var isPlayingBack by remember { mutableStateOf(false) }
    var elapsedDeciseconds by remember { mutableLongStateOf(0L) }
    var lastRecordedClip by remember { mutableStateOf<RecordedSpeechClip?>(null) }
    var recorderNotice by remember { mutableStateOf<String?>(null) }
    var selectedWordTip by remember { mutableStateOf<WordPronunciationItem?>(null) }

    val waveformSamples = remember {
        mutableStateListOf<Float>().apply {
            repeat(28) { add(0.08f) }
        }
    }

    val triggerGeminiAnalysis: (RecordedSpeechClip) -> Unit = { clip ->
        selectedWordTip = null
        speakingLabViewModel.analyzePronunciationAudio(
            audioBytes = clip.bytes,
            mimeType = clip.mimeType,
            targetPhrase = targetPhrase,
            learningLanguage = profile.learningLanguage,
            nativeLanguage = profile.nativeLanguage,
            skillLevel = profile.skillLevel,
            onHighAccuracyAchieved = { score ->
                onRegainHeart()
                onShowMessage("🌟 $score% Pronunciation Mastery! +1 ❤️ Heart earned!")
            }
        )
    }

    val stopAndAnalyze = {
        recorder.stopRecording()
            .onSuccess { clip ->
                isRecording = false
                lastRecordedClip = clip
                recorderNotice = null
                triggerGeminiAnalysis(clip)
            }
            .onFailure { err ->
                isRecording = false
                recorderNotice = err.localizedMessage ?: "Recording stopped."
            }
    }

    val beginMicRecording = {
        recorderNotice = null
        isPlayingBack = false
        recorder.startRecording()
            .onSuccess {
                elapsedDeciseconds = 0L
                isRecording = true
            }
            .onFailure { err ->
                isRecording = false
                recorderNotice = err.localizedMessage ?: "Could not start microphone."
            }
    }

    // Live amplitude & auto-stop coroutine while recording
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording) {
                val amp = recorder.pollNormalizedAmplitude()
                if (waveformSamples.size >= 28) {
                    waveformSamples.removeAt(0)
                }
                waveformSamples.add(amp)
                delay(100L)
                elapsedDeciseconds += 1L
                if (elapsedDeciseconds >= MAX_RECORD_SECONDS * 10L) {
                    stopAndAnalyze()
                    break
                }
            }
        } else {
            for (i in waveformSamples.indices) {
                waveformSamples[i] = 0.08f
            }
        }
    }

    val audioPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            beginMicRecording()
        } else {
            recorderNotice = "Microphone permission is required to record your voice for Gemini pronunciation analysis."
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("speaking_lab_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Header & Mode Switcher (Guided Scenarios vs Custom Phrase)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f)
                                )
                            )
                        )
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.GraphicEq,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = stringResource(R.string.speaking_lab_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "${profile.learningLanguage} • Live Gemini Multimodal Audio",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !useCustomPhrase,
                            onClick = {
                                useCustomPhrase = false
                                speakingLabViewModel.resetFeedbackForNewPhrase()
                            },
                            label = { Text("🎯 Guided Drills (${drills.size})") },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("speaking_lab_mode_guided")
                        )
                        FilterChip(
                            selected = useCustomPhrase,
                            onClick = {
                                useCustomPhrase = true
                                speakingLabViewModel.resetFeedbackForNewPhrase()
                            },
                            label = { Text("✍️ Custom Phrase") },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("speaking_lab_mode_custom")
                        )
                    }

                    if (useCustomPhrase) {
                        OutlinedTextField(
                            value = customPhraseInput,
                            onValueChange = { if (it.length <= 220) customPhraseInput = it },
                            label = { Text("Enter any ${profile.learningLanguage} sentence to practice") },
                            placeholder = { Text(activeDrill.phrase) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("speaking_lab_custom_phrase_input"),
                            maxLines = 3
                        )
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Scenario ${currentDrillIdx + 1}/${drills.size}: ${activeDrill.scenario}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.78f)
                            ) {
                                Text(
                                    text = activeDrill.difficulty,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "“$targetPhrase”",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (!useCustomPhrase) {
                                Text(
                                    text = "🔤 Phonetic: ${activeDrill.phoneticHint}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "💬 Meaning: ${activeDrill.translation}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Reference TTS Playback & Next Scenario Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { audioManager.speak(targetPhrase, profile.learningLanguage, false) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("speaking_lab_tts_normal")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen at normal speed")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Native")
                        }
                        OutlinedButton(
                            onClick = { audioManager.speak(targetPhrase, profile.learningLanguage, true) },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("speaking_lab_tts_slow")
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = "Listen at slow speed")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🐢 Slow")
                        }
                        if (!useCustomPhrase) {
                            FilledTonalButton(
                                onClick = {
                                    currentDrillIdx = (currentDrillIdx + 1) % drills.size
                                    lastRecordedClip = null
                                    selectedWordTip = null
                                    speakingLabViewModel.resetFeedbackForNewPhrase()
                                },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("speaking_lab_next_drill_button")
                            ) {
                                Text("Next →")
                            }
                        }
                    }
                }
            }
        }

        // 2. Live Microphone Recorder Studio & Waveform Console
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isRecording) "🔴 Recording Microphone..." else "🎙️ Voice Capture Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isRecording) HeartCrimsonRed else MaterialTheme.colorScheme.onSurface
                        )
                        val secs = elapsedDeciseconds / 10L
                        val tenths = elapsedDeciseconds % 10L
                        Text(
                            text = if (isRecording) {
                                String.format(Locale.US, "%02d.%ds / %02ds", secs, tenths, MAX_RECORD_SECONDS)
                            } else if (lastRecordedClip != null) {
                                String.format(Locale.US, "Last clip: %.1fs", lastRecordedClip!!.durationMs / 1000f)
                            } else {
                                "Ready (up to ${MAX_RECORD_SECONDS}s)"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Live Audio Waveform Visualizer (Canvas using DrawScope size)
                    val waveformCd = stringResource(R.string.cd_waveform_visualizer)
                    val activeBarColor = if (isRecording) HeartCrimsonRed else MaterialTheme.colorScheme.primary
                    val idleTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(idleTrackColor.copy(alpha = 0.65f))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                            .semantics { contentDescription = waveformCd }
                            .testTag("speaking_lab_waveform")
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val count = waveformSamples.size.coerceAtLeast(1)
                            val spacing = 4.dp.toPx()
                            val totalSpacing = spacing * (count - 1)
                            val barWidth = ((size.width - totalSpacing) / count).coerceAtLeast(3f)

                            waveformSamples.forEachIndexed { idx, normalized ->
                                val barHeight = (size.height * normalized).coerceIn(size.height * 0.12f, size.height)
                                val topY = (size.height - barHeight) / 2f
                                val startX = idx * (barWidth + spacing)
                                drawRoundRect(
                                    color = activeBarColor,
                                    topLeft = Offset(startX, topY),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                                )
                            }
                        }
                    }

                    // Record / Stop Primary Action Button
                    Button(
                        onClick = {
                            if (isRecording) {
                                stopAndAnalyze()
                            } else {
                                val granted = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (granted) {
                                    beginMicRecording()
                                } else {
                                    audioPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRecording) HeartCrimsonRed else MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .testTag("speaking_lab_record_button")
                    ) {
                        Icon(
                            imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = stringResource(R.string.cd_speaking_mic_button)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isRecording) {
                                stringResource(R.string.speaking_lab_stop)
                            } else {
                                stringResource(R.string.speaking_lab_record)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Self-Playback & Re-Analyze Row once a clip is captured
                    lastRecordedClip?.let { clip ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    if (isPlayingBack) {
                                        recorder.stopPlayback()
                                        isPlayingBack = false
                                    } else {
                                        isPlayingBack = true
                                        recorder.playRecordedClip(
                                            filePath = clip.filePath,
                                            onPlaybackCompleted = { isPlayingBack = false },
                                            onPlaybackError = { msg ->
                                                isPlayingBack = false
                                                recorderNotice = msg
                                            }
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("speaking_lab_playback_button")
                            ) {
                                Icon(
                                    imageVector = if (isPlayingBack) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = stringResource(R.string.cd_play_user_audio)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (isPlayingBack) {
                                        stringResource(R.string.speaking_lab_stop_playback)
                                    } else {
                                        stringResource(R.string.speaking_lab_play_recording)
                                    }
                                )
                            }

                            FilledTonalButton(
                                onClick = { triggerGeminiAnalysis(clip) },
                                enabled = !isRecording && liveCoachState !is LiveCoachState.Connecting,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("speaking_lab_reanalyze_button")
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Re-analyze audio with Gemini")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Re-Analyze")
                            }
                        }
                    }

                    recorderNotice?.let { msg ->
                        Text(
                            text = msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // 3. Real-Time Streaming Gemini Pronunciation Coach Card
        if (liveCoachState !is LiveCoachState.Idle) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("speaking_lab_live_coach_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Kavi Live Gemini Voice Coach",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            if (liveCoachState is LiveCoachState.Connecting || liveCoachState is LiveCoachState.Streaming) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp
                                )
                            }
                        }

                        when (val state = liveCoachState) {
                            is LiveCoachState.Connecting -> {
                                Text(
                                    text = "Listening to your audio recording and streaming phonetic feedback...",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            is LiveCoachState.Streaming -> {
                                Text(
                                    text = state.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            is LiveCoachState.Completed -> {
                                Text(
                                    text = state.text,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                OutlinedButton(
                                    onClick = { audioManager.speak(state.text, "English", false) },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Read coaching feedback aloud")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Read Coach Feedback Aloud")
                                }
                            }
                            is LiveCoachState.Error -> {
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            LiveCoachState.Idle -> Unit
                        }
                    }
                }
            }
        }

        // 4. Structured Acoustic & Word-by-Word Pronunciation Scorecard
        when (val scoreState = scorecardState) {
            ScorecardState.Idle -> Unit
            ScorecardState.Analyzing -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(28.dp))
                            Column {
                                Text(
                                    text = "Scoring Acoustic Accuracy, Rhythm & Intonation...",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Building word-by-word phonetic breakdown",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            is ScorecardState.Error -> {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = scoreState.message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            is ScorecardState.Success -> {
                val report = scoreState.report
                item {
                    PronunciationScorecardCard(
                        report = report,
                        learningLanguage = profile.learningLanguage,
                        selectedWordTip = selectedWordTip,
                        onSelectWord = { wordItem ->
                            selectedWordTip = wordItem
                            audioManager.speak(wordItem.word, profile.learningLanguage, true)
                        },
                        onSaveTrickyWords = {
                            val tricky = report.wordBreakdown.filter { it.status == "needs_practice" || it.status == "close" }
                            val toSave = tricky.ifEmpty { report.wordBreakdown.take(1) }
                            toSave.forEach { item ->
                                onBookmarkWord(
                                    item.word,
                                    item.phoneticTip.ifBlank { "Pronunciation focus" },
                                    profile.learningLanguage,
                                    report.practiceSyllables.ifBlank { null },
                                    targetPhrase
                                )
                            }
                            onShowMessage("📖 Saved ${toSave.size} pronunciation focus word(s) to Smart Notebook!")
                        }
                    )
                }
            }
        }

        // 5. Session Speaking Progress History
        if (attemptHistory.isNotEmpty()) {
            item {
                Text(
                    text = "📈 Session Pronunciation Progress",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            items(attemptHistory, key = { it.id }) { attempt ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "“${attempt.targetPhrase}”",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = "Heard: \"${attempt.heardTranscript.ifBlank { "—" }}\"",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Text(
                                text = "${attempt.timestampLabel} • Fluency ${attempt.fluencyScore}% • Intonation ${attempt.intonationScore}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${attempt.overallScore}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PronunciationScorecardCard(
    report: PronunciationFeedbackReport,
    learningLanguage: String,
    selectedWordTip: WordPronunciationItem?,
    onSelectWord: (WordPronunciationItem) -> Unit,
    onSaveTrickyWords: () -> Unit
) {
    val ringColor = when {
        report.overallScore >= 80 -> MaterialTheme.colorScheme.primary
        report.overallScore >= 55 -> XpGoldYellow
        else -> StreakFlameOrange
    }
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("speaking_lab_scorecard"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular Overall Score Gauge
                Box(
                    modifier = Modifier.size(92.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val strokeWidth = 10.dp.toPx()
                        drawArc(
                            color = trackColor,
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = ringColor,
                            startAngle = -90f,
                            sweepAngle = 360f * (report.overallScore / 100f),
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${report.overallScore}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Overall",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 3 Sub-Metrics
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubMetricBar("🎯 Articulation", report.accuracyScore)
                    SubMetricBar("🌊 Fluency & Pace", report.fluencyScore)
                    SubMetricBar("🎵 Intonation", report.intonationScore)
                }
            }

            if (report.heardTranscript.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "👂 What Gemini Heard ($learningLanguage):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "“${report.heardTranscript}”",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (report.wordBreakdown.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "🔍 Tap Any Word to Hear Slow Audio & Mouth Tip:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        report.wordBreakdown.forEachIndexed { idx, item ->
                            val (bgColor, badgeEmoji) = when (item.status) {
                                "native" -> MaterialTheme.colorScheme.primaryContainer to "✅"
                                "needs_practice" -> HeartCrimsonRed.copy(alpha = 0.16f) to "🎯"
                                else -> XpGoldYellow.copy(alpha = 0.22f) to "✨"
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = bgColor,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onSelectWord(item) }
                                    .testTag("word_chip_$idx")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$badgeEmoji ${item.word}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(visible = selectedWordTip != null) {
                        selectedWordTip?.let { tip ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "🗣️ \"${tip.word}\" (${tip.status.replace('_', ' ')}): ${tip.phoneticTip}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            if (report.strengths.isNotBlank()) {
                Text(
                    text = "💪 Strengths: ${report.strengths}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (report.actionableTip.isNotBlank()) {
                Text(
                    text = "💡 Coach Tip: ${report.actionableTip}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (report.practiceSyllables.isNotBlank()) {
                Text(
                    text = "🐢 Slow Syllable Drill: ${report.practiceSyllables}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (report.wordBreakdown.isNotEmpty()) {
                OutlinedButton(
                    onClick = onSaveTrickyWords,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("speaking_lab_save_tricky_words_button")
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Focus Words to Smart Revision Notebook")
                }
            }
        }
    }
}

@Composable
private fun SubMetricBar(label: String, score: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            Text(text = "$score%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { (score / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(7.dp)
                .clip(CircleShape)
        )
    }
}
