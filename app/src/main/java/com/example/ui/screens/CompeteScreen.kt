package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CommunityPost
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import com.example.ui.theme.GemCyanBlue
import com.example.ui.theme.HeartCrimsonRed
import com.example.ui.theme.StreakFlameOrange
import com.example.ui.theme.XpGoldYellow
import com.example.ui.viewmodel.DataUiState

@Composable
fun CompeteScreen(
    profile: UserProfile,
    leaderboardState: DataUiState<List<LeaderboardEntry>>,
    communityPostsState: DataUiState<List<CommunityPost>>,
    onBuyReward: (costGems: Long, rewardType: String, extraValue: String) -> Unit,
    onClaimQuest: (questId: String, title: String, xp: Int, gems: Int) -> Unit = { _, _, _, _ -> },
    onPublishPost: (content: String, tag: String) -> Unit,
    onCheerPost: (CommunityPost) -> Unit,
    onReportPost: (String) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = listOf("🏆 All Ranked Users", "🎯 Quests & Shop", "👥 Community")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEachIndexed { idx, label ->
                FilterChip(
                    selected = selectedTab == idx,
                    onClick = { selectedTab = idx },
                    label = { Text(label) },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("compete_tab_$idx")
                )
            }
        }

        when (selectedTab) {
            0 -> LeaderboardLeaguePane(
                profile = profile,
                leaderboardState = leaderboardState
            )
            1 -> QuestsAndRewardShopPane(
                profile = profile,
                onBuyReward = onBuyReward,
                onClaimQuest = onClaimQuest
            )
            2 -> CommunityFeedPane(
                profile = profile,
                communityPostsState = communityPostsState,
                onPublishPost = onPublishPost,
                onCheerPost = onCheerPost,
                onReportPost = onReportPost
            )
        }
    }
}

@Composable
private fun LeaderboardLeaguePane(
    profile: UserProfile,
    leaderboardState: DataUiState<List<LeaderboardEntry>>
) {
    var selectedScope by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val scopeLabels = listOf("🌍 All Ranked Users", "🎓 My Class", "🗣️ Same Language")

    val leagueInfo = remember(profile.xp) {
        when {
            profile.xp >= 1500L -> Triple("Emerald League", "💚", 2500L)
            profile.xp >= 800L -> Triple("Sapphire League", "💠", 1500L)
            profile.xp >= 400L -> Triple("Gold League", "🥇", 800L)
            profile.xp >= 150L -> Triple("Silver League", "🥈", 400L)
            else -> Triple("Bronze League", "🥉", 150L)
        }
    }

    val rankedEntries = remember(
        leaderboardState,
        selectedScope,
        searchQuery,
        profile.userId,
        profile.displayName,
        profile.username,
        profile.xp,
        profile.streak,
        profile.studentClass,
        profile.learningLanguage,
        profile.leaderboardPrivacy
    ) {
        val rawList = (leaderboardState as? DataUiState.Success)?.data ?: emptyList()
        val combinedList = if (
            profile.userId.isNotBlank() &&
            profile.leaderboardPrivacy != true &&
            rawList.none { it.userId == profile.userId }
        ) {
            rawList + LeaderboardEntry(
                id = profile.userId,
                userId = profile.userId,
                displayName = profile.displayName,
                username = profile.username,
                studentClass = profile.studentClass,
                learningLanguage = profile.learningLanguage,
                xp = profile.xp,
                streak = profile.streak,
                fluencyScore = profile.fluencyScore ?: 20L,
                visibility = "public",
                avatarEmoji = "🦉"
            )
        } else {
            rawList
        }

        val sortedAll = combinedList.sortedWith(
            compareByDescending<LeaderboardEntry> { it.xp }
                .thenByDescending { it.streak }
                .thenBy { it.displayName.lowercase() }
        )

        val filtered = sortedAll
            .filter { entry ->
                when (selectedScope) {
                    1 -> {
                        val targetCls = profile.studentClass?.trim().orEmpty()
                        if (targetCls.isBlank()) entry.userId == profile.userId || !entry.studentClass.isNullOrBlank()
                        else entry.studentClass.equals(targetCls, ignoreCase = true)
                    }
                    2 -> entry.learningLanguage.equals(profile.learningLanguage, ignoreCase = true)
                    else -> true
                }
            }
            .filter { entry ->
                val q = searchQuery.trim()
                q.isBlank() ||
                    entry.displayName.contains(q, ignoreCase = true) ||
                    (entry.username?.contains(q, ignoreCase = true) == true)
            }
        var lastXp = Long.MIN_VALUE
        var currentRank = 0
        filtered.mapIndexed { idx, entry ->
            if (entry.xp != lastXp) {
                currentRank = idx + 1
                lastXp = entry.xp
            }
            currentRank to entry
        }
    }

    val myGlobalRank = remember(rankedEntries, profile.userId) {
        rankedEntries.firstOrNull { it.second.userId == profile.userId }?.first
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("leaderboard_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer,
                                    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = leagueInfo.second,
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = leagueInfo.first,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "All Ranked Learners (${rankedEntries.size}) • Live Cloud XP",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "League Trophy",
                            tint = XpGoldYellow,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    val progress = (profile.xp.toFloat() / leagueInfo.third.toFloat()).coerceIn(0.05f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your XP: ${profile.xp} / ${leagueInfo.third} next tier",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = "Fair play verified",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Fair-Play Protected",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        // Scope Filters + @username Learner Search
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    scopeLabels.forEachIndexed { idx, label ->
                        FilterChip(
                            selected = selectedScope == idx,
                            onClick = { selectedScope = idx },
                            label = { Text(label) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("leaderboard_scope_$idx")
                        )
                    }
                }
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { if (it.length <= 40) searchQuery = it },
                    placeholder = { Text("Search learner by name or @username...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("leaderboard_search_input")
                )
            }
        }

        when (leaderboardState) {
            is DataUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is DataUiState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = leaderboardState.message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            is DataUiState.Success -> {
                if (rankedEntries.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = if (searchQuery.isNotBlank() || selectedScope != 0) {
                                    "No public learners match this filter yet. Try clearing the search or switching back to Weekly League."
                                } else {
                                    "Complete your first lesson to claim the #1 spot in this week's league!"
                                },
                                modifier = Modifier.padding(20.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    item(key = "compete_top_podium_summary") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("compete_top_podium_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f))
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "🏅 Top Ranked Learners (${rankedEntries.size} Total)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    if (myGlobalRank != null) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text(
                                                text = "Your Rank: #$myGlobalRank",
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rankedEntries.take(3).forEach { (podiumRank, podiumEntry) ->
                                        val medal = when (podiumRank) {
                                            1 -> "🥇"
                                            2 -> "🥈"
                                            else -> "🥉"
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text(medal, style = MaterialTheme.typography.titleLarge)
                                                Text(
                                                    text = podiumEntry.displayName,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = "${podiumEntry.xp} XP",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    itemsIndexed(rankedEntries, key = { _, pair -> pair.second.id.ifBlank { pair.second.userId } }) { index, (rank, entry) ->
                        val isCurrentUser = entry.userId == profile.userId
                        val rankBadge = when (rank) {
                            1 -> "🥇"
                            2 -> "🥈"
                            3 -> "🥉"
                            else -> "#$rank"
                        }
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("leaderboard_row_$index"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrentUser) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrentUser) 3.dp else 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rankBadge,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.width(38.dp)
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = entry.avatarEmoji ?: "🦉",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    val handleTag = entry.username?.takeIf { it.isNotBlank() }?.let { " ($it)" }.orEmpty()
                                    Text(
                                        text = if (isCurrentUser) "${entry.displayName}$handleTag (You)" else "${entry.displayName}$handleTag",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val classLabel = entry.studentClass?.takeIf { it.isNotBlank() }?.let { " • $it" }.orEmpty()
                                    Text(
                                        text = "${entry.learningLanguage}$classLabel • 🔥 ${entry.streak}d • Fluency ${entry.fluencyScore ?: 20}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = XpGoldYellow.copy(alpha = 0.18f)
                                ) {
                                    Text(
                                        text = "⚡ ${entry.xp} XP",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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

private data class ShopRewardItem(
    val id: String,
    val emoji: String,
    val title: String,
    val description: String,
    val costGems: Long,
    val rewardType: String,
    val extraValue: String = ""
)

@Composable
private fun QuestsAndRewardShopPane(
    profile: UserProfile,
    onBuyReward: (costGems: Long, rewardType: String, extraValue: String) -> Unit,
    onClaimQuest: (questId: String, title: String, xp: Int, gems: Int) -> Unit
) {
    val shopItems = remember {
        listOf(
            ShopRewardItem(
                id = "heart_refill",
                emoji = "❤️",
                title = "Full 5 Hearts Refill",
                description = "Instantly restore all 5 lives for uninterrupted lessons.",
                costGems = 30L,
                rewardType = "HEART_REFILL"
            ),
            ShopRewardItem(
                id = "streak_freeze",
                emoji = "🧊",
                title = "Streak Freeze Shield",
                description = "Protects your daily streak if you miss a busy study day.",
                costGems = 45L,
                rewardType = "STREAK_FREEZE"
            ),
            ShopRewardItem(
                id = "mascot_treat",
                emoji = "🍓",
                title = "Berry Energy Treat for Kavi",
                description = "Feed mascot Kavi to level up your study companion!",
                costGems = 25L,
                rewardType = "MASCOT_TREAT"
            ),
            ShopRewardItem(
                id = "outfit_scholar",
                emoji = "🎓",
                title = "Scholar Graduation Cap",
                description = "Equip Kavi with the academic polyglot cap.",
                costGems = 60L,
                rewardType = "MASCOT_OUTFIT",
                extraValue = "Scholar Graduation Cap"
            ),
            ShopRewardItem(
                id = "outfit_saffron",
                emoji = "🧣",
                title = "Royal Saffron Explorer Cape",
                description = "Legendary cape celebrating daily language mastery.",
                costGems = 80L,
                rewardType = "MASCOT_OUTFIT",
                extraValue = "Royal Saffron Cape"
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quests_shop_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            val claimed = profile.claimedQuestIds.orEmpty()
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🎯 Daily Missions & Verified Reward Claims",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    QuestProgressRow(
                        emoji = "⚡",
                        title = "XP Sprinter: Reach 150 Total XP",
                        current = profile.xp.toInt().coerceAtMost(150),
                        target = 150,
                        questId = "quest_xp_150",
                        xpReward = 25,
                        gemReward = 10,
                        isClaimed = claimed.contains("quest_xp_150"),
                        onClaim = { onClaimQuest("quest_xp_150", "XP Sprinter", 25, 10) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    QuestProgressRow(
                        emoji = "📚",
                        title = "Path Explorer: Complete 3 Lessons",
                        current = profile.lessonsCompleted.toInt().coerceAtMost(3),
                        target = 3,
                        questId = "quest_lessons_3",
                        xpReward = 35,
                        gemReward = 15,
                        isClaimed = claimed.contains("quest_lessons_3"),
                        onClaim = { onClaimQuest("quest_lessons_3", "Path Explorer", 35, 15) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    QuestProgressRow(
                        emoji = "🔥",
                        title = "Consistency Champion: 7-Day Streak",
                        current = profile.streak.toInt().coerceAtMost(7),
                        target = 7,
                        questId = "quest_streak_7",
                        xpReward = 50,
                        gemReward = 25,
                        isClaimed = claimed.contains("quest_streak_7"),
                        onClaim = { onClaimQuest("quest_streak_7", "Consistency Champion", 50, 25) }
                    )
                }
            }
        }

        // Cooperative Friends Quest & Friend Streaks Card
        item {
            var nudgedPartner by rememberSaveable { mutableStateOf(false) }
            val squadLessons = (profile.lessonsCompleted.toInt() + 4).coerceAtMost(10)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("friends_quest_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🤝 Weekly Friends Quest & Friend Streaks",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = StreakFlameOrange.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "🔥 ${(profile.streak + 2)}d Squad",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = StreakFlameOrange
                            )
                        }
                    }
                    Text(
                        text = "Co-op Goal with Kavi Study Squad: Complete 10 lessons together this week for a 2x XP Chest!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    QuestProgressRow(
                        emoji = "🎉",
                        title = "Squad Lessons Completed",
                        current = squadLessons,
                        target = 10
                    )
                    Button(
                        onClick = { nudgedPartner = true },
                        enabled = !nudgedPartner,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("friends_quest_high_five_button")
                    ) {
                        Text(if (nudgedPartner) "🙌 High-Five & Study Boost Sent!" else "🙌 Send High-Five to Study Squad")
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Reward Shop",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Gem Reward Shop",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Hearts: ${profile.hearts}/5 • Freezes: ${profile.streakFreezeCount ?: 0} • Kavi Lv.${profile.mascotLevel ?: 1}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GemCyanBlue.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = "💎 ${profile.gems}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        items(shopItems, key = { it.id }) { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(52.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = item.emoji, style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onBuyReward(item.costGems, item.rewardType, item.extraValue) },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("buy_${item.id}_button")
                    ) {
                        Text("💎 ${item.costGems}")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestProgressRow(
    emoji: String,
    title: String,
    current: Int,
    target: Int,
    questId: String? = null,
    xpReward: Int = 0,
    gemReward: Int = 0,
    isClaimed: Boolean = false,
    onClaim: (() -> Unit)? = null
) {
    val ratio = (current.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    val isDone = current >= target
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$emoji $title",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            when {
                isClaimed -> {
                    Text(
                        text = "Claimed ✓",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                isDone && onClaim != null && questId != null -> {
                    Button(
                        onClick = onClaim,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("claim_${questId}_button")
                    ) {
                        Text("Claim +$xpReward XP • +$gemReward💎")
                    }
                }
                isDone -> {
                    Text(
                        text = "✅ Done",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                else -> {
                    Text(
                        text = if (xpReward > 0) "$current/$target (+$xpReward XP)" else "$current / $target",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
        )
    }
}

@Composable
private fun CommunityFeedPane(
    profile: UserProfile,
    communityPostsState: DataUiState<List<CommunityPost>>,
    onPublishPost: (content: String, tag: String) -> Unit,
    onCheerPost: (CommunityPost) -> Unit,
    onReportPost: (String) -> Unit
) {
    var postDraft by rememberSaveable { mutableStateOf("") }
    var selectedTag by rememberSaveable { mutableStateOf("Milestone") }
    val postTags = listOf("Milestone", "Study Tip", "Question", "Challenge")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("community_feed_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "👥 Share Progress or Study Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Encourage fellow ${profile.learningLanguage} learners in a moderated, positive space.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        postTags.forEach { tag ->
                            FilterChip(
                                selected = selectedTag == tag,
                                onClick = { selectedTag = tag },
                                label = { Text(tag) },
                                modifier = Modifier.heightIn(min = 48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = postDraft,
                        onValueChange = { if (it.length <= 400) postDraft = it },
                        placeholder = {
                            Text("Share a phrase you learned in ${profile.learningLanguage} or celebrate your streak...")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("community_post_input"),
                        minLines = 2,
                        maxLines = 4
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${postDraft.length}/400",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = {
                                if (postDraft.isNotBlank()) {
                                    onPublishPost(postDraft.trim(), selectedTag)
                                    postDraft = ""
                                }
                            },
                            enabled = postDraft.isNotBlank(),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("publish_community_post_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Publish post",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Post")
                        }
                    }
                }
            }
        }

        when (communityPostsState) {
            is DataUiState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
            is DataUiState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = communityPostsState.message,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            is DataUiState.Success -> {
                val posts = communityPostsState.data
                if (posts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(
                                text = "Be the first to share a milestone or study tip with the Noxra AI learner community!",
                                modifier = Modifier.padding(20.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(posts, key = { it.id }) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("community_post_card_${post.id}"),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = post.authorName.take(1).uppercase(),
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = post.authorName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Learning ${post.language} • ${post.tag ?: "Update"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { onReportPost(post.id) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("report_post_${post.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Flag,
                                            contentDescription = "Report post for moderation",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = post.content,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Streak & Study Community",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(
                                        onClick = { onCheerPost(post) },
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("cheer_post_${post.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ThumbUp,
                                            contentDescription = "Send encouragement",
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Encourage (${post.cheersCount})")
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
