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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DirectChatMessage
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import com.example.ui.viewmodel.DataUiState

private data class ChatPartnerTarget(
    val userId: String,
    val displayName: String,
    val username: String?
)

@Composable
fun FriendChatScreen(
    profile: UserProfile,
    directMessagesState: DataUiState<List<DirectChatMessage>>,
    friendSearchResults: List<LeaderboardEntry>,
    publicLearnersState: DataUiState<List<LeaderboardEntry>>,
    onSearchUsername: (String) -> Unit,
    onSendDirectMessage: (recipientId: String, recipientName: String, recipientUsername: String?, text: String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var activePartner by remember { mutableStateOf<ChatPartnerTarget?>(null) }
    var draftMessage by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        onSearchUsername("")
    }

    BackHandler {
        if (activePartner != null) {
            activePartner = null
        } else {
            onBack()
        }
    }

    val allMessages = (directMessagesState as? DataUiState.Success)?.data ?: emptyList()
    val directoryLearners = ((publicLearnersState as? DataUiState.Success)?.data ?: emptyList())
        .filter { it.userId != profile.userId }

    val recentPartners = remember(allMessages, profile.userId) {
        val map = LinkedHashMap<String, ChatPartnerTarget>()
        allMessages.sortedByDescending { it.createdAt?.seconds ?: 0L }.forEach { msg ->
            val otherId = if (msg.senderId == profile.userId) msg.recipientId else msg.senderId
            if (otherId.isNotBlank() && otherId != profile.userId && !map.containsKey(otherId)) {
                val otherName = if (msg.senderId == profile.userId) {
                    msg.recipientName ?: "Friend"
                } else {
                    msg.senderName
                }
                val otherHandle = if (msg.senderId == profile.userId) {
                    msg.recipientUsername
                } else {
                    msg.senderUsername
                }
                map[otherId] = ChatPartnerTarget(
                    userId = otherId,
                    displayName = otherName,
                    username = otherHandle
                )
            }
        }
        map.values.toList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        val partner = activePartner
        if (partner != null) {
            val threadMessages = remember(allMessages, partner.userId, profile.userId) {
                allMessages
                    .filter {
                        (it.senderId == profile.userId && it.recipientId == partner.userId) ||
                            (it.senderId == partner.userId && it.recipientId == profile.userId)
                    }
                    .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { activePartner = null },
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("friend_thread_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Friend Search"
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = partner.displayName.take(1).uppercase(),
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = partner.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = partner.username ?: "@student",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("friend_chat_message_list"),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    if (threadMessages.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                )
                            ) {
                                Text(
                                    text = "👋 Say hello to ${partner.displayName} (${partner.username ?: "@friend"})! Messages sync instantly across phones.",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    items(threadMessages, key = { it.id.ifBlank { "${it.senderId}_${it.text.hashCode()}" } }) { msg ->
                        val isMine = msg.senderId == profile.userId
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 18.dp,
                                    topEnd = 18.dp,
                                    bottomStart = if (isMine) 18.dp else 4.dp,
                                    bottomEnd = if (isMine) 4.dp else 18.dp
                                ),
                                color = if (isMine) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier.widthIn(max = 300.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (isMine) {
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

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = draftMessage,
                        onValueChange = { draftMessage = it },
                        placeholder = { Text("Message ${partner.username ?: partner.displayName}...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("friend_chat_input_field"),
                        shape = RoundedCornerShape(18.dp),
                        singleLine = true
                    )
                    Button(
                        onClick = {
                            val clean = draftMessage.trim()
                            if (clean.isNotEmpty()) {
                                onSendDirectMessage(
                                    partner.userId,
                                    partner.displayName,
                                    partner.username,
                                    clean
                                )
                                draftMessage = ""
                            }
                        },
                        enabled = draftMessage.isNotBlank(),
                        modifier = Modifier
                            .heightIn(min = 52.dp)
                            .testTag("friend_chat_send_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Friend Message"
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("friend_chat_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Dashboard"
                            )
                        }
                        Column {
                            Text(
                                text = "💬 Find Friend & Direct Chat",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Your ID: ${profile.username ?: "@set_username_in_profile"} • Search any @username",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PersonSearch,
                                    contentDescription = "Search Username",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Search Friend by @username or Name",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        onSearchUsername(it)
                                    },
                                    placeholder = { Text("e.g. @sachin or @aarav") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("friend_username_search_input"),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                Button(
                                    onClick = { onSearchUsername(searchQuery) },
                                    modifier = Modifier
                                        .heightIn(min = 52.dp)
                                        .testTag("friend_username_search_button"),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Friend"
                                    )
                                }
                            }
                        }
                    }
                }

                if (recentPartners.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Direct Chats",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    items(recentPartners, key = { "recent_${it.userId}" }) { p ->
                        FriendRowCard(
                            displayName = p.displayName,
                            username = p.username,
                            subtitle = "Tap to open saved cloud chat",
                            onClick = { activePartner = p }
                        )
                    }
                }

                val candidateList = if (searchQuery.isNotBlank()) friendSearchResults else directoryLearners
                item {
                    Text(
                        text = if (searchQuery.isNotBlank()) {
                            "Matching Friends (${candidateList.size})"
                        } else {
                            "Registered Noxra AI Students (${candidateList.size})"
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                if (candidateList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            )
                        ) {
                            Text(
                                text = "No matching @username found yet. Ask your friend to register on Noxra AI and search their @username!",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(candidateList, key = { "dir_${it.userId}" }) { entry ->
                        FriendRowCard(
                            displayName = entry.displayName,
                            username = entry.username,
                            subtitle = "${entry.studentClass ?: "Learner"} • ${entry.xp} XP • ${entry.league} League",
                            onClick = {
                                activePartner = ChatPartnerTarget(
                                    userId = entry.userId,
                                    displayName = entry.displayName,
                                    username = entry.username
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendRowCard(
    displayName: String,
    username: String?,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("friend_row_${(username ?: displayName).lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.take(1).uppercase(),
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$displayName ${username?.let { "($it)" } ?: ""}".trim(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = "Open Chat",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
