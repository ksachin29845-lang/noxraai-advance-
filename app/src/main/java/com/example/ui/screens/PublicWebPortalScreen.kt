package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.PublicSeoConfig
import com.example.core.PublicWebLinkManager
import com.example.data.model.CustomCourseLesson
import com.example.data.model.LeaderboardEntry
import com.example.data.model.UserProfile
import com.example.ui.components.DEFAULT_DEV_AGE
import com.example.ui.components.DEFAULT_DEV_NAME
import com.example.ui.components.DEFAULT_DEV_ROLE
import com.example.ui.viewmodel.DataUiState

@Composable
fun PublicWebPortalScreen(
    profile: UserProfile,
    leaderboardState: DataUiState<List<LeaderboardEntry>>,
    customCourses: List<CustomCourseLesson>,
    developerName: String = DEFAULT_DEV_NAME,
    developerAge: String = DEFAULT_DEV_AGE,
    developerRole: String = DEFAULT_DEV_ROLE,
    initialSearchQuery: String = "",
    onTogglePublicVisibility: (Boolean) -> Unit,
    onOpenEnglishFiftyLevels: () -> Unit,
    onOpenMathFiftyLevels: () -> Unit,
    onOpenSpeakingLab: () -> Unit,
    onOpenAiTutor: () -> Unit,
    onOpenFriendChat: () -> Unit,
    onOpenDeveloperInfo: () -> Unit,
    onStartCustomCourse: (CustomCourseLesson) -> Unit,
    onShowMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    val context = LocalContext.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var searchQuery by rememberSaveable { mutableStateOf(initialSearchQuery) }
    var selectedCategoryFilter by rememberSaveable { mutableStateOf("All") }

    var activeWebUrl by remember { mutableStateOf(PublicWebLinkManager.getPublicWebUrl(context)) }
    var customDomainDraft by remember(activeWebUrl) { mutableStateOf(activeWebUrl) }

    var seoConfig by remember { mutableStateOf(PublicWebLinkManager.getSeoConfig(context)) }
    var seoTitleDraft by remember(seoConfig.title) { mutableStateOf(seoConfig.title) }
    var seoDescDraft by remember(seoConfig.description) { mutableStateOf(seoConfig.description) }
    var seoKeywordsDraft by remember(seoConfig.keywords) { mutableStateOf(seoConfig.keywords) }
    var seoAllowIndexing by remember(seoConfig.allowGoogleIndexing) { mutableStateOf(seoConfig.allowGoogleIndexing) }

    val personalProfileUrl = remember(activeWebUrl, profile.username) {
        PublicWebLinkManager.buildUserPublicProfileUrl(activeWebUrl, profile.username)
    }
    val isPubliclyDiscoverable = profile.leaderboardPrivacy != true

    val publicLearners = remember(leaderboardState) {
        (leaderboardState as? DataUiState.Success)?.data.orEmpty()
    }

    val allDirectoryEntries by remember(
        activeWebUrl,
        publicLearners,
        customCourses,
        developerName,
        developerAge,
        developerRole,
        profile
    ) {
        derivedStateOf {
            buildList {
                add(
                    PublicSearchEntry(
                        id = "page_home",
                        category = "Official Web",
                        breadcrumbUrl = "$activeWebUrl › home",
                        title = "Noxra AI — Official Smart Learning Web App",
                        snippet = "Practice English & Math Levels 1–50 across Basic, Medium & Hard difficulty tiers, chat with Noxra AI Tutor, and compete on Global Leaderboards.",
                        metaBadges = listOf("🌐 Public HTTPS", "⚡ Cloud Synced", "📱 Mobile + Web"),
                        shareUrl = activeWebUrl,
                        primaryActionLabel = "Open Web Link",
                        onPrimaryAction = { openExternalUrl(context, activeWebUrl, onShowMessage) }
                    )
                )
                add(
                    PublicSearchEntry(
                        id = "page_english_50",
                        category = "Courses",
                        breadcrumbUrl = "$activeWebUrl › learn › english-1-50",
                        title = "English Mastery Path — Levels 1 to 50 (Class 1–12)",
                        snippet = "Structured 50-level grammar, vocabulary, sentence building, and reading comprehension curriculum with instant AI explanations.",
                        metaBadges = listOf("🇬🇧 50 Levels", "Basic • Medium • Hard", "Lv.${profile.englishUnlockedLevel ?: 1L}/50 Unlocked"),
                        shareUrl = PublicWebLinkManager.buildPublicSearchQueryUrl(activeWebUrl, "English 1-50"),
                        primaryActionLabel = "Open English 1–50",
                        onPrimaryAction = onOpenEnglishFiftyLevels
                    )
                )
                add(
                    PublicSearchEntry(
                        id = "page_math_50",
                        category = "Courses",
                        breadcrumbUrl = "$activeWebUrl › learn › math-1-50",
                        title = "Math + AI Step Solver — Levels 1 to 50 (Class 1–12)",
                        snippet = "Master arithmetic, fractions, algebra, geometry, and word problems from Level 1 to Level 50 with step-by-step Noxra AI coaching.",
                        metaBadges = listOf("➗ 50 Levels", "AI Step Hints", "Lv.${profile.mathUnlockedLevel ?: 1L}/50 Unlocked"),
                        shareUrl = PublicWebLinkManager.buildPublicSearchQueryUrl(activeWebUrl, "Math 1-50"),
                        primaryActionLabel = "Open Math 1–50",
                        onPrimaryAction = onOpenMathFiftyLevels
                    )
                )
                add(
                    PublicSearchEntry(
                        id = "page_ai_speaking",
                        category = "AI & Founder",
                        breadcrumbUrl = "$activeWebUrl › labs › speaking-ai",
                        title = "Noxra AI Tutor & Real-Time Voice Speaking Lab",
                        snippet = "Ask homework questions in English or Hindi and record live voice pronunciation for instant acoustic & phonetic feedback.",
                        metaBadges = listOf("✨ Noxra AI", "🎙️ Voice Coaching"),
                        shareUrl = PublicWebLinkManager.buildPublicSearchQueryUrl(activeWebUrl, "Noxra AI Tutor"),
                        primaryActionLabel = "Open AI Tutor",
                        onPrimaryAction = onOpenAiTutor
                    )
                )
                add(
                    PublicSearchEntry(
                        id = "page_founder",
                        category = "AI & Founder",
                        breadcrumbUrl = "$activeWebUrl › founder › sachin-kumar",
                        title = "$developerName — $developerRole of Noxra AI",
                        snippet = "Official developer & founder profile ($developerAge). Building accessible AI-powered English and Mathematics education for students everywhere.",
                        metaBadges = listOf("👨‍💻 $developerName", "🎂 $developerAge", "✅ Verified Owner"),
                        shareUrl = PublicWebLinkManager.buildPublicSearchQueryUrl(activeWebUrl, developerName),
                        primaryActionLabel = "View Founder Profile",
                        onPrimaryAction = onOpenDeveloperInfo
                    )
                )

                customCourses.forEach { course ->
                    val courseKey = course.id.ifBlank { course.title.hashCode().toString() }
                    add(
                        PublicSearchEntry(
                            id = "course_$courseKey",
                            category = "Courses",
                            breadcrumbUrl = "$activeWebUrl › courses › ${course.subjectOrLanguage.lowercase()}",
                            title = "${course.title} (${course.subjectOrLanguage} • ${course.level})",
                            snippet = course.objective.ifBlank { course.questionPrompt },
                            metaBadges = listOfNotNull(
                                "🎓 ${course.subjectOrLanguage}",
                                course.studentClass,
                                "+${course.xpReward} XP"
                            ),
                            shareUrl = PublicWebLinkManager.buildPublicSearchQueryUrl(activeWebUrl, course.title),
                            primaryActionLabel = "Start Lesson",
                            onPrimaryAction = { onStartCustomCourse(course) }
                        )
                    )
                }

                publicLearners.forEach { learner ->
                    val handle = learner.username?.let { if (it.startsWith("@")) it else "@$it" }
                        ?: "@${learner.displayName.lowercase().replace(" ", "_")}"
                    val learnerShareUrl = PublicWebLinkManager.buildUserPublicProfileUrl(activeWebUrl, handle)
                    add(
                        PublicSearchEntry(
                            id = "learner_${learner.id.ifBlank { learner.userId }}",
                            category = "Learners",
                            breadcrumbUrl = "$activeWebUrl › u › ${handle.removePrefix("@")}",
                            title = "${learner.displayName} ($handle) — Noxra AI Learner",
                            snippet = "Public ranked student profile • Studying ${learner.learningLanguage} ${learner.studentClass?.let { "in $it" }.orEmpty()} • ${learner.league} League.",
                            metaBadges = listOfNotNull(
                                "⭐ ${learner.xp} XP",
                                "🔥 ${learner.streak}d Streak",
                                learner.studentClass,
                                "🏅 ${learner.league}"
                            ),
                            shareUrl = learnerShareUrl,
                            primaryActionLabel = "Chat with $handle",
                            onPrimaryAction = onOpenFriendChat
                        )
                    )
                }
            }
        }
    }

    val filteredEntries by remember(allDirectoryEntries, searchQuery, selectedCategoryFilter) {
        derivedStateOf {
            val q = searchQuery.trim().lowercase().removePrefix("@")
            allDirectoryEntries.filter { item ->
                val matchesCategory = selectedCategoryFilter == "All" || item.category == selectedCategoryFilter
                val matchesQuery = q.isEmpty() ||
                    item.title.lowercase().contains(q) ||
                    item.snippet.lowercase().contains(q) ||
                    item.breadcrumbUrl.lowercase().contains(q) ||
                    item.metaBadges.any { it.lowercase().contains(q) }
                matchesCategory && matchesQuery
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("public_web_portal_screen")
    ) {
        // Header Banner
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("public_portal_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.cd_public_back_button)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.public_portal_title),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.public_portal_subtitle),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "LIVE HTTPS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "🔍 Search Directory",
                        "🌐 Public Link & QR",
                        "📈 Google SEO & Index"
                    ).forEachIndexed { idx, label ->
                        FilterChip(
                            selected = selectedTab == idx,
                            onClick = { selectedTab = idx },
                            label = { Text(label, fontWeight = FontWeight.Bold) },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("portal_tab_$idx")
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("public_portal_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "🔍 Search Public Noxra AI Directory & Google",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text(stringResource(R.string.public_search_placeholder)) },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null)
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(
                                                onClick = { searchQuery = "" },
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear search query")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("public_google_style_search_input")
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    listOf("All", "Official Web", "Courses", "Learners", "AI & Founder").forEach { cat ->
                                        FilterChip(
                                            selected = selectedCategoryFilter == cat,
                                            onClick = { selectedCategoryFilter = cat },
                                            label = { Text(cat) },
                                            modifier = Modifier.heightIn(min = 48.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            val shareSearchUrl = PublicWebLinkManager.buildPublicSearchQueryUrl(
                                                activeWebUrl,
                                                searchQuery
                                            )
                                            copyToClipboard(context, "Noxra AI Search Link", shareSearchUrl)
                                            onShowMessage("📋 Copied public search link: $shareSearchUrl")
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("copy_search_query_link_button")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Copy Search Link")
                                    }
                                    Button(
                                        onClick = {
                                            val googleUrl = PublicWebLinkManager.buildGoogleSearchUrl(
                                                searchQuery.ifBlank { "Noxra AI $developerName" }
                                            )
                                            openExternalUrl(context, googleUrl, onShowMessage)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("launch_google_search_button")
                                    ) {
                                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Google.com")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "Showing ${filteredEntries.size} public web results ${if (searchQuery.isNotBlank()) "for \"${searchQuery.trim()}\"" else ""}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (filteredEntries.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "No matching public profile or page for \"${searchQuery.trim()}\"",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Try searching another @username, 'English 1-50', 'Math 1-50', or launch a live web search on Google.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Button(
                                        onClick = {
                                            openExternalUrl(
                                                context,
                                                PublicWebLinkManager.buildGoogleSearchUrl("Noxra AI ${searchQuery.trim()}"),
                                                onShowMessage
                                            )
                                        },
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Text("Search on Google.com ↗")
                                    }
                                }
                            }
                        }
                    } else {
                        items(filteredEntries, key = { it.id }) { entry ->
                            PublicDirectoryResultCard(
                                entry = entry,
                                onCopyUrl = { url ->
                                    copyToClipboard(context, entry.title, url)
                                    onShowMessage("📋 Copied public link: $url")
                                },
                                onShareUrl = { title, url ->
                                    shareExternalLink(context, "$title — Open on Noxra AI: $url")
                                }
                            )
                        }
                    }
                }

                1 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    ShareableLinkVisualMatrix(url = activeWebUrl)
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "🌐 Official Public App Web Link",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            text = activeWebUrl,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.testTag("portal_official_web_url_text")
                                        )
                                        Text(
                                            text = "Anyone on any phone, tablet, or computer can open this link without installing anything.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            copyToClipboard(context, "Noxra AI Public Web Link", activeWebUrl)
                                            onShowMessage("✅ Public Web Link copied! Share it anywhere.")
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("portal_copy_app_link_button")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Copy Link")
                                    }
                                    FilledTonalButton(
                                        onClick = {
                                            shareExternalLink(
                                                context,
                                                "🌐 Open Noxra AI (English & Math Levels 1–50 + AI Tutor) in your browser: $activeWebUrl"
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("portal_share_app_link_button")
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Share")
                                    }
                                    OutlinedButton(
                                        onClick = { openExternalUrl(context, activeWebUrl, onShowMessage) },
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("portal_open_app_link_button")
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.OpenInNew,
                                            contentDescription = stringResource(R.string.cd_public_open_browser)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.testTag("portal_personal_profile_link_card")
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "👤 Your Personal Public Student Link",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = personalProfileUrl,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledTonalButton(
                                        onClick = {
                                            copyToClipboard(context, "My Noxra AI Profile Link", personalProfileUrl)
                                            onShowMessage("📋 Copied your public profile link!")
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("portal_copy_personal_link_button")
                                    ) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Copy My Link")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            shareExternalLink(
                                                context,
                                                "Check out my Noxra AI learning profile (${profile.displayName} • ${profile.xp} XP): $personalProfileUrl"
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Share Profile")
                                    }
                                }

                                HorizontalDivider()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Appear in Public Search Directory",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isPubliclyDiscoverable) {
                                                "Visible: Anyone searching your @username or name can view your XP, streak & class."
                                            } else {
                                                "Private: Your @username is hidden from public directory searches."
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = isPubliclyDiscoverable,
                                        onCheckedChange = onTogglePublicVisibility,
                                        modifier = Modifier.testTag("public_visibility_switch")
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(shape = RoundedCornerShape(22.dp)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "🔗 Custom Public Domain / GitHub Pages URL",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "Hosted /docs on GitHub Pages, Netlify, or your own .com domain? Set it here so all Copy & Share buttons use your custom searchable domain:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedTextField(
                                    value = customDomainDraft,
                                    onValueChange = { customDomainDraft = it },
                                    label = { Text("Public https:// Domain") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("portal_custom_domain_input")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            activeWebUrl = PublicWebLinkManager.savePublicWebUrl(context, customDomainDraft)
                                            customDomainDraft = activeWebUrl
                                            onShowMessage("🌐 Active Public Web Link updated to $activeWebUrl")
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("portal_save_domain_button")
                                    ) {
                                        Text("Save Domain")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            activeWebUrl = PublicWebLinkManager.resetPublicWebUrl(context)
                                            customDomainDraft = activeWebUrl
                                            onShowMessage("🔄 Restored default AI Studio Shared App URL")
                                        },
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("portal_reset_domain_button")
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Reset default domain", modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Default")
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    item {
                        Text(
                            text = "Live Google Search Result Preview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    item {
                        GoogleSerpPreviewCard(
                            publicUrl = activeWebUrl,
                            seoConfig = PublicSeoConfig(
                                title = seoTitleDraft.ifBlank { PublicWebLinkManager.DEFAULT_SEO_TITLE },
                                description = seoDescDraft.ifBlank { PublicWebLinkManager.DEFAULT_SEO_DESCRIPTION },
                                keywords = seoKeywordsDraft,
                                allowGoogleIndexing = seoAllowIndexing
                            ),
                            developerName = developerName,
                            onOpenUrl = { openExternalUrl(context, activeWebUrl, onShowMessage) },
                            onTestOnGoogle = {
                                openExternalUrl(
                                    context,
                                    PublicWebLinkManager.buildGoogleSearchUrl(seoTitleDraft),
                                    onShowMessage
                                )
                            }
                        )
                    }

                    item {
                        Card(shape = RoundedCornerShape(22.dp)) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "⚙️ Customize Google Search Snippet & Keywords",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                OutlinedTextField(
                                    value = seoTitleDraft,
                                    onValueChange = { seoTitleDraft = it },
                                    label = { Text("Google Search Page Title") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("seo_title_input")
                                )
                                OutlinedTextField(
                                    value = seoDescDraft,
                                    onValueChange = { seoDescDraft = it },
                                    label = { Text("Google Meta Description") },
                                    minLines = 2,
                                    maxLines = 4,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("seo_description_input")
                                )
                                OutlinedTextField(
                                    value = seoKeywordsDraft,
                                    onValueChange = { seoKeywordsDraft = it },
                                    label = { Text("Search Keywords (comma separated)") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("seo_keywords_input")
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Allow Googlebot Indexing (robots: index, follow)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(
                                        checked = seoAllowIndexing,
                                        onCheckedChange = { seoAllowIndexing = it },
                                        modifier = Modifier.testTag("seo_allow_indexing_switch")
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            seoConfig = PublicWebLinkManager.saveSeoConfig(
                                                context,
                                                PublicSeoConfig(
                                                    title = seoTitleDraft,
                                                    description = seoDescDraft,
                                                    keywords = seoKeywordsDraft,
                                                    allowGoogleIndexing = seoAllowIndexing
                                                )
                                            )
                                            onShowMessage("✅ Saved Google SEO Title, Description & Schema config!")
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("save_seo_config_button")
                                    ) {
                                        Text("Save SEO")
                                    }
                                    FilledTonalButton(
                                        onClick = {
                                            val snippet = PublicWebLinkManager.generateHtmlSeoSnippet(
                                                publicUrl = activeWebUrl,
                                                seo = PublicSeoConfig(
                                                    title = seoTitleDraft,
                                                    description = seoDescDraft,
                                                    keywords = seoKeywordsDraft,
                                                    allowGoogleIndexing = seoAllowIndexing
                                                ),
                                                developerName = developerName
                                            )
                                            copyToClipboard(context, "Noxra AI SEO HTML", snippet)
                                            onShowMessage("📋 Copied Google SEO + Schema.org HTML tags!")
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("copy_seo_html_button")
                                    ) {
                                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Copy HTML")
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "🚀 How Anyone Finds Your Link on Google Search",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "1. Instant Direct Link: Your Shared App Link ($activeWebUrl) is live right now for anyone you share it with.\n" +
                                        "2. Pre-Built SEO Web Bundle (/docs): We bundled index.html (with Schema.org EducationalApplication JSON-LD), sitemap.xml, and robots.txt in /docs.\n" +
                                        "3. Worldwide Google Search: Push this project to GitHub (enable GitHub Pages on /docs) or Netlify, then submit your sitemap URL below in Google Search Console so Google indexes your site worldwide.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val sitemapUrl = PublicWebLinkManager.buildSitemapUrl(activeWebUrl)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            openExternalUrl(
                                                context,
                                                "https://search.google.com/search-console",
                                                onShowMessage
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("open_search_console_button")
                                    ) {
                                        Text("Search Console ↗")
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            openExternalUrl(
                                                context,
                                                PublicWebLinkManager.buildGoogleSiteIndexCheckUrl(activeWebUrl),
                                                onShowMessage
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("check_google_site_index_button")
                                    ) {
                                        Text("Check site: Index")
                                    }
                                }
                                FilledTonalButton(
                                    onClick = {
                                        copyToClipboard(context, "Noxra AI Sitemap URL", sitemapUrl)
                                        onShowMessage("📋 Copied Sitemap URL: $sitemapUrl")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("copy_sitemap_url_button")
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Copy Sitemap ($sitemapUrl)", maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
}

private fun shareExternalLink(context: Context, message: String) {
    runCatching {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Public Web Link"))
    }
}

private fun openExternalUrl(context: Context, url: String, onShowMessage: (String) -> Unit) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }.onFailure {
        onShowMessage("🌐 Link ready: $url")
    }
}
