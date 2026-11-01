package com.example.ui.screens

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.core.SpeechAndAudioManager
import com.example.data.model.AppHistoryItem
import com.example.data.model.UserProfile
import com.example.ui.viewmodel.AiChatMessage
import com.example.ui.viewmodel.AiTutorViewModel
import com.example.ui.viewmodel.ChatUiState
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.TutorLearnerContext

private val SIMPLE_TUTOR_MODES = listOf(
    "Simple Explanation",
    "Step-by-Step Teaching",
    "Writing Feedback",
    "Practice Questions"
)

@Composable
fun ChatScreen(
    chatViewModel: ChatViewModel = viewModel(),
    profile: UserProfile = UserProfile(),
    weakVocabularyWords: List<String> = emptyList(),
    savedAiHistory: List<AppHistoryItem> = emptyList(),
    onDeleteSavedHistory: (String) -> Unit = {},
    aiTutorViewModel: AiTutorViewModel? = null,
    audioManager: SpeechAndAudioManager? = null,
    onShowMessage: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by chatViewModel.uiState.collectAsStateWithLifecycle()
    val messages by chatViewModel.messages.collectAsStateWithLifecycle()
    val sessions by chatViewModel.sessions.collectAsStateWithLifecycle()
    val activeSessionId by chatViewModel.activeSessionId.collectAsStateWithLifecycle()
    val activeMode by chatViewModel.activeTutorMode.collectAsStateWithLifecycle()
    val isGenerating by chatViewModel.isGenerating.collectAsStateWithLifecycle()
    val chatError by chatViewModel.chatError.collectAsStateWithLifecycle()
    val attachedImage by chatViewModel.attachedImage.collectAsStateWithLifecycle()
    val followUpSuggestions by chatViewModel.followUpSuggestions.collectAsStateWithLifecycle()

    var promptInput by rememberSaveable { mutableStateOf("") }
    var showHistoryPanel by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    val weakWordsSummary = remember(weakVocabularyWords) {
        weakVocabularyWords.take(6).joinToString(", ")
    }

    val learnerContext = remember(
        profile.learningLanguage,
        profile.nativeLanguage,
        profile.skillLevel,
        profile.studentClass,
        profile.xp,
        profile.lessonsCompleted,
        weakWordsSummary
    ) {
        TutorLearnerContext(
            learningLanguage = profile.learningLanguage,
            nativeLanguage = profile.nativeLanguage,
            skillLevel = profile.skillLevel,
            studentClass = profile.studentClass.orEmpty(),
            xp = profile.xp,
            lessonsCompleted = profile.lessonsCompleted,
            weakWordsSummary = weakWordsSummary
        )
    }

    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                promptInput = spoken
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val decoded = decodeSampledBitmapFromUri(context, uri, maxDimension = 1280)
            if (decoded != null) {
                chatViewModel.attachImage(decoded)
            } else {
                onShowMessage("Could not load selected image. Please try another photo.")
            }
        }
    }

    val starterCards = remember(profile.learningLanguage, profile.studentClass, profile.skillLevel) {
        val gradeTag = profile.studentClass?.takeIf { it.isNotBlank() } ?: "students"
        listOf(
            "⚡ Explain Simply" to "Explain fractions and percentages simply for $gradeTag with real-life examples",
            "✍️ Fix My Sentence" to "Check and explain my sentence: 'I am learn ${profile.learningLanguage} since 2 month'",
            "➗ Solve Step-by-Step" to "Teach me how to solve 3x + 7 = 22 step by step",
            "🧠 Quick Practice" to "Give me 3 quick practice questions for ${profile.learningLanguage} (${profile.skillLevel})"
        )
    }

    val statusSubtitle = when (uiState) {
        is ChatUiState.Loading, is ChatUiState.Streaming ->
            "Streaming answer • ${profile.learningLanguage}"
        else ->
            "Simple AI Assistant • ${profile.learningLanguage}"
    }

    val lastModelMsgId = remember(messages) {
        messages.lastOrNull { !it.isUser && !it.isStreaming && it.text.isNotBlank() }?.id
    }

    val cloudAiHistory = remember(savedAiHistory) {
        savedAiHistory.filter { it.category.equals("Noxra AI", ignoreCase = true) || it.title.contains("Noxra AI", ignoreCase = true) }
    }
    val totalHistoryCount = sessions.size + cloudAiHistory.size

    val dispatchCurrentPrompt = {
        if (!isGenerating && (promptInput.isNotBlank() || attachedImage != null)) {
            val toSend = promptInput
            promptInput = ""
            chatViewModel.sendMessage(
                userPrompt = toSend,
                learnerContext = learnerContext
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("chat_screen_root")
    ) {
        // Clean, Simple Noxra AI Header with History & New Chat
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_noxra_logo),
                            contentDescription = "Noxra AI Emblem",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = stringResource(R.string.chat_header_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = statusSubtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { showHistoryPanel = !showHistoryPanel },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("open_ai_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Open Noxra AI History",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (totalHistoryCount > 0) "History ($totalHistoryCount)" else "History",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        FilledTonalButton(
                            onClick = {
                                showHistoryPanel = false
                                chatViewModel.startNewChat(profile.learningLanguage)
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("new_ai_chat_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.chat_new_button),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(R.string.chat_new_button), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SIMPLE_TUTOR_MODES.forEach { mode ->
                        FilterChip(
                            selected = activeMode == mode,
                            onClick = { chatViewModel.setTutorMode(mode) },
                            label = { Text(mode, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        )
                    }
                }
            }
        }

        // Collapsible Noxra AI Chat History Panel (Sessions + Cloud Saved History)
        AnimatedVisibility(visible = showHistoryPanel) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("ai_history_panel"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Noxra AI Saved History",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        IconButton(
                            onClick = { showHistoryPanel = false },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_ai_history_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close history panel")
                        }
                    }

                    if (sessions.isEmpty() && cloudAiHistory.isEmpty()) {
                        Text(
                            text = "No past chats yet. Ask Noxra AI any English, Math, or school question below — your history saves automatically!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (sessions.isNotEmpty()) {
                                item(key = "active_sessions_header") {
                                    Text(
                                        text = "Recent Conversations",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                items(sessions, key = { "sess_${it.sessionId}" }) { session ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (session.sessionId == activeSessionId && messages.isNotEmpty()) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                chatViewModel.selectSession(session.sessionId)
                                                showHistoryPanel = false
                                            }
                                            .testTag("ai_chat_session_${session.sessionId}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = session.title,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${session.tutorMode} • ${session.messages.size} messages",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(
                                                onClick = { chatViewModel.deleteSession(session.sessionId) },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete chat session ${session.title}",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            if (cloudAiHistory.isNotEmpty()) {
                                item(key = "cloud_history_header") {
                                    if (sessions.isNotEmpty()) {
                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                                    }
                                    Text(
                                        text = "Cloud Saved Q&A (${cloudAiHistory.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                items(cloudAiHistory.take(25), key = { "cloud_${it.id.ifBlank { it.title }}" }) { item ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val cleanQ = item.summary
                                                    .substringAfter("Q:", item.title)
                                                    .substringBefore("\nA:")
                                                    .trim()
                                                promptInput = cleanQ
                                                showHistoryPanel = false
                                            }
                                            .testTag("reuse_saved_ai_history_${item.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = item.summary.replace('\n', ' '),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            if (item.id.isNotBlank()) {
                                                IconButton(
                                                    onClick = { onDeleteSavedHistory(item.id) },
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .testTag("delete_saved_ai_history_${item.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteOutline,
                                                        contentDescription = "Delete saved AI history item",
                                                        modifier = Modifier.size(18.dp)
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

        // LazyColumn displaying User vs. AI Message Bubbles
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("chat_message_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.isEmpty()) {
                item(key = "chat_empty_state") {
                    ChatEmptyWelcomeState(
                        nativeLanguage = profile.nativeLanguage,
                        learningLanguage = profile.learningLanguage,
                        starterCards = starterCards,
                        enabled = !isGenerating,
                        onSelectPrompt = { selectedPrompt ->
                            chatViewModel.sendMessage(
                                userPrompt = selectedPrompt,
                                learnerContext = learnerContext
                            )
                        }
                    )
                }
            }

            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    isLatestCompletedAiReply = !msg.isUser && msg.id == lastModelMsgId && !isGenerating,
                    onListen = if (!msg.isUser && audioManager != null && msg.text.isNotBlank()) {
                        { audioManager.speak(msg.text, profile.learningLanguage, false) }
                    } else {
                        null
                    },
                    onCopy = if (!msg.isUser && msg.text.isNotBlank()) {
                        {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("Noxra AI", msg.text))
                            onShowMessage("📋 Copied Noxra AI answer to clipboard!")
                        }
                    } else {
                        null
                    },
                    onRegenerate = if (!msg.isUser && msg.id == lastModelMsgId && !isGenerating) {
                        { chatViewModel.regenerateLastResponse(learnerContext) }
                    } else {
                        null
                    }
                )
            }

            if (messages.isNotEmpty() && !isGenerating && chatError == null && followUpSuggestions.isNotEmpty()) {
                item(key = "chat_follow_ups") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        followUpSuggestions.forEach { suggestion ->
                            AssistChip(
                                onClick = {
                                    chatViewModel.sendMessage(
                                        userPrompt = suggestion,
                                        learnerContext = learnerContext
                                    )
                                },
                                label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }
                }
            }

            if (chatError != null) {
                item(key = "chat_error_banner") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("chat_error_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = chatError!!,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { chatViewModel.regenerateLastResponse(learnerContext) },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("ai_chat_retry_button")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Retry")
                                }
                                OutlinedButton(
                                    onClick = { chatViewModel.dismissError() },
                                    modifier = Modifier.heightIn(min = 48.dp)
                                ) {
                                    Text("Dismiss")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Input Area with Send / Stop Button
        ChatBottomInputBar(
            inputText = promptInput,
            onInputChange = { promptInput = it },
            isGenerating = isGenerating,
            attachedImage = attachedImage,
            onClearAttachedImage = { chatViewModel.clearAttachedImage() },
            onAttachPhotoClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onVoiceInputClick = {
                try {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Noxra AI...")
                    }
                    voiceLauncher.launch(intent)
                } catch (_: Exception) {
                    onShowMessage("🎙️ Voice dictation is not available on this device.")
                }
            },
            onSendClick = dispatchCurrentPrompt,
            onStopClick = { chatViewModel.stopGenerating() }
        )
    }
}

@Composable
fun ChatMessageBubble(
    message: AiChatMessage,
    isLatestCompletedAiReply: Boolean = false,
    onListen: (() -> Unit)? = null,
    onCopy: (() -> Unit)? = null,
    onRegenerate: (() -> Unit)? = null
) {
    val isUser = message.isUser
    val bubbleTag = if (isUser) "chat_bubble_user_${message.id}" else "chat_bubble_ai_${message.id}"
    val userLabel = stringResource(R.string.chat_user_sender_label)
    val aiPrefix = stringResource(R.string.chat_ai_sender_prefix)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(bubbleTag),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        if (!isUser) {
            Image(
                painter = painterResource(id = R.drawable.img_noxra_logo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .padding(end = 8.dp, bottom = 2.dp)
                    .size(30.dp)
                    .clip(CircleShape)
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(if (isUser) 0.84f else 0.92f),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (isUser) 20.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceContainerLow
                }
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isUser) 1.dp else 2.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildString {
                            if (isUser) {
                                append(userLabel)
                                if (message.hasAttachedImage) append(" • 📷 Photo")
                            } else {
                                append("✨ $aiPrefix • ${message.modeBadge}")
                            }
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isUser) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                    if (!isUser && message.text.isNotBlank()) {
                        Row {
                            if (onListen != null) {
                                IconButton(
                                    onClick = onListen,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = stringResource(R.string.chat_listen_response_cd),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            if (onCopy != null) {
                                IconButton(
                                    onClick = onCopy,
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContentCopy,
                                        contentDescription = stringResource(R.string.chat_copy_response_cd),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                            if (isLatestCompletedAiReply && onRegenerate != null) {
                                IconButton(
                                    onClick = onRegenerate,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("ai_chat_regenerate_button")
                                ) {
                                    Icon(
                                        Icons.Default.Refresh,
                                        contentDescription = stringResource(R.string.chat_regenerate_response_cd),
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                if (message.isStreaming && message.text.isBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.chat_thinking_status),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUser) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBottomInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    isGenerating: Boolean,
    attachedImage: Bitmap? = null,
    onClearAttachedImage: () -> Unit = {},
    onAttachPhotoClick: () -> Unit = {},
    onVoiceInputClick: () -> Unit = {},
    onSendClick: () -> Unit,
    onStopClick: () -> Unit = {}
) {
    Surface(
        tonalElevation = 4.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_bottom_input_bar")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            attachedImage?.let { bmp ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Attached study image preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Text(
                        text = "📷 Study photo attached (Vision enabled)",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onClearAttachedImage,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove attached photo")
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onAttachPhotoClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("ai_chat_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = stringResource(R.string.chat_attach_photo_cd),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onVoiceInputClick,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("ai_chat_voice_mic_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = stringResource(R.string.chat_voice_input_cd),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    placeholder = { Text(stringResource(R.string.chat_input_placeholder)) },
                    shape = RoundedCornerShape(26.dp),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field")
                )

                Spacer(modifier = Modifier.width(8.dp))

                if (isGenerating) {
                    FilledIconButton(
                        onClick = onStopClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_stop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = stringResource(R.string.chat_stop_button_cd)
                        )
                    }
                } else {
                    FilledIconButton(
                        onClick = onSendClick,
                        enabled = inputText.isNotBlank() || attachedImage != null,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = stringResource(R.string.chat_send_button_cd)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatEmptyWelcomeState(
    nativeLanguage: String,
    learningLanguage: String,
    starterCards: List<Pair<String, String>>,
    enabled: Boolean,
    onSelectPrompt: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            tonalElevation = 4.dp,
            shadowElevation = 4.dp,
            modifier = Modifier.size(72.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_noxra_logo),
                contentDescription = "Noxra AI Assistant Logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.chat_empty_headline),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Ask any question in $nativeLanguage or $learningLanguage — tap a quick card below or type your own.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            starterCards.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    pair.forEach { (badge, fullPrompt) ->
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = enabled) { onSelectPrompt(fullPrompt) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = fullPrompt,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun decodeSampledBitmapFromUri(
    context: Context,
    uri: Uri,
    maxDimension: Int
): Bitmap? {
    return try {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, boundsOptions)
        }
        var sampleSize = 1
        val largestSide = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
        while (largestSide / sampleSize > maxDimension * 2) {
            sampleSize *= 2
        }
        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        }
    } catch (_: Exception) {
        null
    }
}
