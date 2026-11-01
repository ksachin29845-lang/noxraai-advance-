package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppHistoryItem
import com.example.ui.viewmodel.DataUiState
import java.text.SimpleDateFormat
import java.util.Locale

private val HISTORY_CATEGORIES = listOf(
    "All", "Noxra AI", "English 1-50", "Math 1-50", "Subscription", "Friend Chat", "Lesson", "System"
)

@Composable
fun AppHistoryScreen(
    historyState: DataUiState<List<AppHistoryItem>>,
    onDeleteHistoryItem: (String) -> Unit,
    onBack: () -> Unit
) {
    var selectedCategory by rememberSaveable { mutableStateOf("All") }
    var searchQuery by rememberSaveable { mutableStateOf("") }

    BackHandler { onBack() }

    val dateFormatter = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp)
                .testTag("saved_app_history_list"),
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
                            .testTag("history_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard"
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "Saved Cloud History",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "📜 Saved Cloud App History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Every Noxra AI chat, Level 1–50 win, friend chat & ₹49 pass is saved forever",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search your saved history...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("history_search_input"),
                    shape = RoundedCornerShape(16.dp)
                )
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(HISTORY_CATEGORIES) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("history_filter_${cat.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }

            when (historyState) {
                is DataUiState.Loading -> item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is DataUiState.Error -> item {
                    Text(
                        text = historyState.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                is DataUiState.Success -> {
                    val filtered = historyState.data.filter { item ->
                        val catOk = selectedCategory == "All" || item.category.equals(selectedCategory, ignoreCase = true)
                        val q = searchQuery.trim()
                        val searchOk = q.isEmpty() ||
                            item.title.contains(q, ignoreCase = true) ||
                            item.summary.contains(q, ignoreCase = true) ||
                            item.category.contains(q, ignoreCase = true)
                        catOk && searchOk
                    }

                    if (filtered.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    text = "No history entries match this filter yet. Complete English/Math Levels 1–50 or chat with Noxra AI — everything saves automatically!",
                                    modifier = Modifier.padding(18.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        items(filtered, key = { it.id.ifBlank { "${it.title}_${it.summary.hashCode()}" } }) { entry ->
                            val emoji = when (entry.category) {
                                "Noxra AI" -> "✨"
                                "English 1-50" -> "🇬🇧"
                                "Math 1-50" -> "➗"
                                "Subscription" -> "🔥"
                                "Friend Chat" -> "💬"
                                "Lesson" -> "📚"
                                else -> "☁️"
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("history_item_card_${entry.id}"),
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = "$emoji ${entry.category}",
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                            entry.xpEarned?.takeIf { it > 0 }?.let { xp ->
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "+$xp XP",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        if (entry.id.isNotBlank()) {
                                            IconButton(
                                                onClick = { onDeleteHistoryItem(entry.id) },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete history entry"
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = entry.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = entry.summary,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    entry.createdAt?.toDate()?.let { dt ->
                                        Text(
                                            text = dateFormatter.format(dt),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
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
