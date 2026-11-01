package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.core.AdminRegistrationAiMailer
import com.example.core.SpeechAndAudioManager
import com.example.core.authStateFlow
import com.example.core.configureAppCheck
import com.example.core.signOutUser
import com.example.data.model.UserProfile
import com.example.data.repository.AppPreferencesState
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.SachinChatRepository
import com.example.ui.components.AboutDeveloperDialog
import com.example.ui.components.DEFAULT_DEV_AGE
import com.example.ui.components.DEFAULT_DEV_BIO
import com.example.ui.components.DEFAULT_DEV_NAME
import com.example.ui.components.DEFAULT_DEV_ROLE
import com.example.ui.components.NarutoSubscriptionDialog
import com.example.ui.components.NotificationCenterDialog
import com.example.ui.components.WelcomeSlideshowDialog
import com.example.ui.screens.AppHistoryScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CompeteScreen
import com.example.ui.screens.CompleteRegistrationGateScreen
import com.example.ui.screens.FiftyLevelPathScreen
import com.example.ui.screens.FriendChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LearnScreen
import com.example.ui.screens.LessonGameOverlay
import com.example.ui.screens.PracticeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PublicWebPortalScreen
import com.example.ui.theme.SachinChatTheme
import com.example.ui.viewmodel.AiTutorViewModel
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.DataUiState
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.SpeakingLabViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

import com.example.core.NOXRA_ADMIN_EMAIL

private const val ADMIN_GMAIL_RECIPIENT = NOXRA_ADMIN_EMAIL

private enum class SubScreenOverlay {
    NONE,
    ENGLISH_50,
    MATH_50,
    FRIEND_CHAT,
    APP_HISTORY,
    PUBLIC_WEB_PORTAL
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // CRITICAL: Must run before setContent or any Firebase AI / Firestore SDK calls
        configureAppCheck(this, intent)
        enableEdgeToEdge()

        setContent {
            val appContext = applicationContext
            val prefsRepo = remember(appContext) { PreferencesRepository(appContext) }
            val prefsState by prefsRepo.preferencesFlow.collectAsState(initial = AppPreferencesState())

            SachinChatTheme(
                themeMode = prefsState.themeMode,
                accentPreset = prefsState.accentPreset,
                textScale = prefsState.textScale
            ) {
                SachinChatRootApp(preferencesRepository = prefsRepo)
            }
        }
    }
}

private data class MainNavTab(
    val id: String,
    val label: String,
    val icon: ImageVector
)

private val MAIN_NAV_TABS = listOf(
    MainNavTab("home", "Home", Icons.Default.Home),
    MainNavTab("learn", "Learn", Icons.Default.AutoAwesome),
    MainNavTab("practice", "Practice", Icons.Default.GraphicEq),
    MainNavTab("compete", "Compete", Icons.Default.EmojiEvents),
    MainNavTab("profile", "Profile", Icons.Default.Person)
)

@Composable
private fun SachinChatRootApp(
    preferencesRepository: PreferencesRepository
) {
    val currentUser by Firebase.auth.authStateFlow().collectAsState(initial = Firebase.auth.currentUser)
    var showUnauthDeveloperModal by rememberSaveable { mutableStateOf(false) }
    var globalFeedback by remember { mutableStateOf<String?>(null) }

    var pendingFullName by rememberSaveable { mutableStateOf("") }
    var pendingUsername by rememberSaveable { mutableStateOf("") }
    var pendingAge by rememberSaveable { mutableIntStateOf(0) }
    var pendingStudentClass by rememberSaveable { mutableStateOf("") }
    var pendingPasscode by rememberSaveable { mutableStateOf("") }
    var pendingSelectedTier by rememberSaveable { mutableStateOf("Basic") }
    var pendingNativeLanguage by rememberSaveable { mutableStateOf("Hindi") }
    var pendingLearningLanguage by rememberSaveable { mutableStateOf("English") }

    val user = currentUser
    if (user == null) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                AuthScreen(
                    onAuthSuccess = { fullName, username, age, studentClass, passcode, tier, nativeLang, learningLang ->
                        pendingFullName = fullName
                        pendingUsername = username
                        pendingAge = age ?: 0
                        pendingStudentClass = studentClass
                        pendingPasscode = passcode
                        pendingSelectedTier = tier
                        pendingNativeLanguage = nativeLang
                        pendingLearningLanguage = learningLang
                    },
                    onOpenDeveloperInfo = { showUnauthDeveloperModal = true }
                )
            }
        }

        if (showUnauthDeveloperModal) {
            AboutDeveloperDialog(
                onDismiss = { showUnauthDeveloperModal = false },
                onFeedbackMessage = { msg -> globalFeedback = msg },
                canEditDeveloperInfo = false
            )
        }
    } else {
        AuthenticatedSachinChatShell(
            user = user,
            pendingFullName = pendingFullName,
            pendingUsername = pendingUsername,
            pendingAge = pendingAge.takeIf { it > 0 },
            pendingStudentClass = pendingStudentClass,
            pendingPasscode = pendingPasscode,
            pendingSelectedTier = pendingSelectedTier,
            pendingNativeLanguage = pendingNativeLanguage,
            pendingLearningLanguage = pendingLearningLanguage,
            preferencesRepository = preferencesRepository,
            onGlobalFeedback = { msg -> globalFeedback = msg }
        )
    }

    globalFeedback?.let { message ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    IconButton(onClick = { globalFeedback = null }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss message",
                            tint = MaterialTheme.colorScheme.inverseOnSurface
                        )
                    }
                }
            }
        }
    }
}

private fun launchAdminRegistrationGmailIntent(
    context: Context,
    fullName: String,
    username: String,
    studentClass: String,
    selectedTier: String,
    email: String,
    userId: String,
    registeredAt: String
) {
    val body = """
        🎉 Instant New User Registration Notification — Noxra AI
        =====================================================
        Full Name: $fullName
        Username ID: $username
        Class / Grade: $studentClass
        Starting Practice Tier: $selectedTier (Levels 1–50 Unlocked)
        Email Address: $email
        Cloud User ID: $userId
        Registered At: $registeredAt
        Account Persistence: Cloud Firestore Synced (Login on any phone without re-registering)
        App: Noxra AI (Founder & CEO: Sachin Kumar, Age 12 y)
    """.trimIndent()
    try {
        val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$ADMIN_GMAIL_RECIPIENT")
            putExtra(Intent.EXTRA_SUBJECT, "Noxra AI Registration Alert: $fullName ($username)")
            putExtra(Intent.EXTRA_TEXT, body)
        }
        context.startActivity(Intent.createChooser(mailIntent, "Send Instant Registration Alert to Admin Gmail"))
    } catch (_: Exception) {
        // Gracefully ignore if no email app is installed on headless test environments
    }
}

@Composable
private fun AuthenticatedSachinChatShell(
    user: FirebaseUser,
    pendingFullName: String,
    pendingUsername: String,
    pendingAge: Int?,
    pendingStudentClass: String,
    pendingPasscode: String,
    pendingSelectedTier: String,
    pendingNativeLanguage: String,
    pendingLearningLanguage: String,
    preferencesRepository: PreferencesRepository,
    onGlobalFeedback: (String) -> Unit
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val audioManager = remember(appContext) { SpeechAndAudioManager(appContext) }
    DisposableEffect(audioManager) {
        onDispose { audioManager.release() }
    }

    val mainViewModel: MainViewModel = viewModel(
        key = "main_vm_${user.uid}",
        factory = viewModelFactory {
            initializer {
                val dbId = appContext.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(dbId)
                val repo = SachinChatRepository(db = db)
                MainViewModel(
                    repository = repo,
                    preferencesRepository = preferencesRepository,
                    currentUserId = user.uid,
                    displayNameHint = pendingFullName.ifBlank { user.displayName },
                    emailHint = user.email,
                    photoUrlHint = user.photoUrl?.toString(),
                    usernameHint = pendingUsername.ifBlank { null },
                    studentClassHint = pendingStudentClass.ifBlank { null },
                    passcodeHint = pendingPasscode.ifBlank { null },
                    tierHint = pendingSelectedTier.ifBlank { "Basic" },
                    ageHint = pendingAge,
                    nativeLanguageHint = pendingNativeLanguage.ifBlank { "Hindi" },
                    learningLanguageHint = pendingLearningLanguage.ifBlank { "English" }
                )
            }
        }
    )

    val chatViewModel: ChatViewModel = viewModel(
        key = "chat_vm_${user.uid}",
        factory = viewModelFactory {
            initializer { ChatViewModel() }
        }
    )

    LaunchedEffect(chatViewModel, mainViewModel) {
        chatViewModel.onExchangeCompleted = { prompt, reply, mode ->
            mainViewModel.recordAiChatHistory(prompt, reply, mode)
        }
    }

    val aiTutorViewModel: AiTutorViewModel = viewModel(
        key = "ai_vm_${user.uid}",
        factory = viewModelFactory {
            initializer { AiTutorViewModel() }
        }
    )

    val speakingLabViewModel: SpeakingLabViewModel = viewModel(
        key = "speaking_lab_vm_${user.uid}",
        factory = viewModelFactory {
            initializer { SpeakingLabViewModel() }
        }
    )

    val profileState by mainViewModel.userProfileState.collectAsStateWithLifecycle()
    val notificationsState by mainViewModel.notificationsState.collectAsStateWithLifecycle()
    val vocabularyState by mainViewModel.vocabularyState.collectAsStateWithLifecycle()
    val leaderboardState by mainViewModel.leaderboardState.collectAsStateWithLifecycle()
    val communityPostsState by mainViewModel.communityPostsState.collectAsStateWithLifecycle()
    val customCoursesState by mainViewModel.customCoursesState.collectAsStateWithLifecycle()
    val appHistoryState by mainViewModel.appHistoryState.collectAsStateWithLifecycle()
    val directMessagesState by mainViewModel.directMessagesState.collectAsStateWithLifecycle()
    val friendSearchResults by mainViewModel.friendSearchResults.collectAsStateWithLifecycle()
    val preferences by mainViewModel.preferencesState.collectAsStateWithLifecycle()
    val activeLesson by mainViewModel.activeLesson.collectAsStateWithLifecycle()
    val statusBanner by mainViewModel.statusBanner.collectAsStateWithLifecycle()
    val newlyRegistered by mainViewModel.newlyRegisteredBanner.collectAsStateWithLifecycle()
    val showRegistrationOnboarding by mainViewModel.showRegistrationOnboarding.collectAsStateWithLifecycle()
    val showWelcomeSlideshow by mainViewModel.showWelcomeSlideshow.collectAsStateWithLifecycle()
    val autoAdminEmailState by mainViewModel.autoAdminEmailState.collectAsStateWithLifecycle()
    val ownRegistration by mainViewModel.ownRegistrationState.collectAsStateWithLifecycle()
    val developerConfig by mainViewModel.developerConfigState.collectAsStateWithLifecycle()
    val adminRegistrations by mainViewModel.adminRegistrations.collectAsStateWithLifecycle()
    val reportedPosts by mainViewModel.reportedPosts.collectAsStateWithLifecycle()
    val explainAnswerState by aiTutorViewModel.explainAnswerState.collectAsStateWithLifecycle()
    val aiRegistrationEmailState by aiTutorViewModel.aiRegistrationEmailState.collectAsStateWithLifecycle()

    val initialDeepLinkSearchQuery = remember {
        val uri = (context as? ComponentActivity)?.intent?.data
        uri?.getQueryParameter("u") ?: uri?.getQueryParameter("q") ?: ""
    }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var activeSubScreen by rememberSaveable {
        mutableStateOf(
            if (initialDeepLinkSearchQuery.isNotBlank()) SubScreenOverlay.PUBLIC_WEB_PORTAL
            else SubScreenOverlay.NONE
        )
    }
    var portalInitialQuery by rememberSaveable { mutableStateOf(initialDeepLinkSearchQuery) }
    var speakingTabFocusKey by rememberSaveable { mutableIntStateOf(0) }
    var learnTabFocusKey by rememberSaveable { mutableIntStateOf(0) }
    var targetLearnSection by rememberSaveable { mutableIntStateOf(0) }
    var targetSubjectLabIndex by rememberSaveable { mutableIntStateOf(0) }
    var showNotificationsModal by rememberSaveable { mutableStateOf(false) }
    var showDeveloperModal by rememberSaveable { mutableStateOf(false) }
    var showNarutoSubscriptionModal by rememberSaveable { mutableStateOf(false) }

    BackHandler(
        enabled = activeSubScreen == SubScreenOverlay.NONE &&
            selectedTab != 0 &&
            activeLesson == null &&
            !showNotificationsModal &&
            !showRegistrationOnboarding &&
            !showWelcomeSlideshow &&
            !showDeveloperModal &&
            !showNarutoSubscriptionModal
    ) {
        selectedTab = 0
    }

    val resolvedProfile = remember(profileState, user) {
        when (val s = profileState) {
            is DataUiState.Success -> s.data ?: UserProfile(
                id = user.uid,
                userId = user.uid,
                displayName = user.displayName ?: "Learner",
                email = user.email ?: "learner@noxra.ai"
            )
            else -> UserProfile(
                id = user.uid,
                userId = user.uid,
                displayName = user.displayName ?: "Learner",
                email = user.email ?: "learner@noxra.ai"
            )
        }
    }

    val canEditDev = mainViewModel.isCurrentUserAdmin
    val devName = developerConfig?.name?.ifBlank { null }
        ?: (if (canEditDev) resolvedProfile.developerNameOverride?.ifBlank { null } else null)
        ?: DEFAULT_DEV_NAME
    val devAge = developerConfig?.age?.ifBlank { null }
        ?: (if (canEditDev) resolvedProfile.developerAgeOverride?.ifBlank { null } else null)
        ?: DEFAULT_DEV_AGE
    val devRole = developerConfig?.role?.ifBlank { null }
        ?: (if (canEditDev) resolvedProfile.developerRoleOverride?.ifBlank { null } else null)
        ?: DEFAULT_DEV_ROLE
    val devBio = developerConfig?.bio?.ifBlank { null }
        ?: (if (canEditDev) resolvedProfile.developerBioOverride?.ifBlank { null } else null)
        ?: DEFAULT_DEV_BIO

    val needsMandatoryRegistration = remember(profileState, resolvedProfile) {
        val successData = (profileState as? DataUiState.Success)?.data
        successData != null &&
            successData.onboardingCompleted != true &&
            successData.username.isNullOrBlank()
    }

    // Mandatory Full-Info Registration Gate: user MUST fill all required info before Noxra AI opens
    if (needsMandatoryRegistration) {
        Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                CompleteRegistrationGateScreen(
                    initialFullName = resolvedProfile.displayName,
                    email = resolvedProfile.email,
                    onCompleteAllInfo = { fullName, username, age, studentClass, tier, nativeLang, learningLang, passcode ->
                        mainViewModel.completeStudentRegistration(
                            fullName = fullName,
                            username = username,
                            studentClass = studentClass,
                            age = age,
                            noxraPasscode = passcode,
                            selectedTier = tier,
                            nativeLanguage = nativeLang,
                            learningLanguage = learningLang,
                            emailForReceipt = resolvedProfile.email
                        )
                    },
                    onOpenDeveloperInfo = { showDeveloperModal = true },
                    onSignOut = {
                        signOutUser(
                            credentialManager = credentialManager,
                            onSignOutComplete = {},
                            scope = scope
                        )
                    }
                )
            }
        }
        if (showDeveloperModal) {
            AboutDeveloperDialog(
                onDismiss = { showDeveloperModal = false },
                onFeedbackMessage = { msg -> onGlobalFeedback(msg) },
                developerName = devName,
                developerAge = devAge,
                developerRole = devRole,
                developerBio = devBio,
                canEditDeveloperInfo = canEditDev,
                onSaveDeveloperEdits = if (canEditDev) {
                    { name, age, role, bio ->
                        mainViewModel.saveDeveloperOverrides(name, age, role, bio)
                    }
                } else null
            )
        }
        return
    }

    val unreadCount = remember(notificationsState) {
        (notificationsState as? DataUiState.Success)?.data?.count { !it.isRead } ?: 0
    }

    val publishedCustomCourses = remember(customCoursesState) {
        (customCoursesState as? DataUiState.Success)?.data.orEmpty()
    }

    val weakVocabWords = remember(vocabularyState) {
        (vocabularyState as? DataUiState.Success)?.data
            ?.filter { it.isMistake || it.masteryLevel <= 2L }
            ?.map { "${it.word} (${it.translation})" }
            ?.take(8)
            .orEmpty()
    }

    // Full-screen interactive Lesson Game Overlay when a lesson is active
    val currentLesson = activeLesson
    if (currentLesson != null) {
        Scaffold(contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LessonGameOverlay(
                    lesson = currentLesson,
                    learningLanguage = resolvedProfile.learningLanguage,
                    hearts = resolvedProfile.hearts,
                    relaxedMode = resolvedProfile.relaxedMode == true,
                    audioManager = audioManager,
                    explainAnswerState = explainAnswerState,
                    onExplainAnswer = { prompt, attempt, correct, wasCorrect ->
                        aiTutorViewModel.explainLessonAnswer(
                            questionPrompt = prompt,
                            userAttempt = attempt,
                            correctAnswer = correct,
                            wasCorrect = wasCorrect,
                            learningLanguage = resolvedProfile.learningLanguage,
                            nativeLanguage = resolvedProfile.nativeLanguage
                        )
                    },
                    onClearExplainAnswer = { aiTutorViewModel.clearExplainAnswer() },
                    onMistake = { word, meaning ->
                        mainViewModel.onLessonMistake(word, meaning, resolvedProfile.learningLanguage)
                    },
                    onLessonComplete = { mistakes ->
                        aiTutorViewModel.clearExplainAnswer()
                        mainViewModel.onLessonFinished(currentLesson, mistakes)
                    },
                    onExit = {
                        aiTutorViewModel.clearExplainAnswer()
                        mainViewModel.exitLesson()
                    }
                )
            }
        }
        return
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!isWideScreen) {
                    NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                        MAIN_NAV_TABS.forEachIndexed { idx, tab ->
                            NavigationBarItem(
                                selected = activeSubScreen == SubScreenOverlay.NONE && selectedTab == idx,
                                onClick = {
                                    activeSubScreen = SubScreenOverlay.NONE
                                    selectedTab = idx
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("nav_tab_${tab.id}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWideScreen) {
                    NavigationRail(
                        modifier = Modifier
                            .fillMaxHeight()
                            .testTag("side_nav_rail")
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        MAIN_NAV_TABS.forEachIndexed { idx, tab ->
                            NavigationRailItem(
                                selected = activeSubScreen == SubScreenOverlay.NONE && selectedTab == idx,
                                onClick = {
                                    activeSubScreen = SubScreenOverlay.NONE
                                    selectedTab = idx
                                },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                modifier = Modifier.testTag("rail_tab_${tab.id}")
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 900.dp)
                    ) {
                        AnimatedVisibility(visible = statusBanner != null) {
                            statusBanner?.let { msg ->
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .testTag("status_banner")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = msg,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(onClick = { mainViewModel.clearStatusBanner() }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss notification banner"
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (profileState is DataUiState.Loading) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f)) {
                                when (activeSubScreen) {
                                    SubScreenOverlay.ENGLISH_50 -> FiftyLevelPathScreen(
                                        isMath = false,
                                        profile = resolvedProfile,
                                        aiTutorViewModel = aiTutorViewModel,
                                        audioManager = audioManager,
                                        onSelectClassAndTier = { cls, tier ->
                                            mainViewModel.selectFiftyLevelConfig(isMath = false, studentClass = cls, tier = tier)
                                        },
                                        onCompleteLevel = { lvl, cls, tier, mistakes ->
                                            mainViewModel.completeFiftyLevel(
                                                isMath = false,
                                                levelNumber = lvl,
                                                studentClass = cls,
                                                tier = tier,
                                                mistakesCount = mistakes
                                            )
                                        },
                                        onOpenNoxraAiChat = {
                                            activeSubScreen = SubScreenOverlay.NONE
                                            targetLearnSection = 0
                                            learnTabFocusKey += 1
                                            selectedTab = 1
                                        },
                                        onBack = { activeSubScreen = SubScreenOverlay.NONE }
                                    )

                                    SubScreenOverlay.MATH_50 -> FiftyLevelPathScreen(
                                        isMath = true,
                                        profile = resolvedProfile,
                                        aiTutorViewModel = aiTutorViewModel,
                                        audioManager = audioManager,
                                        onSelectClassAndTier = { cls, tier ->
                                            mainViewModel.selectFiftyLevelConfig(isMath = true, studentClass = cls, tier = tier)
                                        },
                                        onCompleteLevel = { lvl, cls, tier, mistakes ->
                                            mainViewModel.completeFiftyLevel(
                                                isMath = true,
                                                levelNumber = lvl,
                                                studentClass = cls,
                                                tier = tier,
                                                mistakesCount = mistakes
                                            )
                                        },
                                        onOpenNoxraAiChat = {
                                            activeSubScreen = SubScreenOverlay.NONE
                                            targetLearnSection = 0
                                            learnTabFocusKey += 1
                                            selectedTab = 1
                                        },
                                        onBack = { activeSubScreen = SubScreenOverlay.NONE }
                                    )

                                    SubScreenOverlay.FRIEND_CHAT -> FriendChatScreen(
                                        profile = resolvedProfile,
                                        directMessagesState = directMessagesState,
                                        friendSearchResults = friendSearchResults,
                                        publicLearnersState = leaderboardState,
                                        onSearchUsername = { query -> mainViewModel.searchFriendsByUsername(query) },
                                        onSendDirectMessage = { recipientId, recipientName, recipientUsername, text ->
                                            mainViewModel.sendDirectMessageToFriend(
                                                recipientId = recipientId,
                                                recipientName = recipientName,
                                                recipientUsername = recipientUsername,
                                                text = text
                                            )
                                        },
                                        onBack = { activeSubScreen = SubScreenOverlay.NONE }
                                    )

                                    SubScreenOverlay.APP_HISTORY -> AppHistoryScreen(
                                        historyState = appHistoryState,
                                        onDeleteHistoryItem = { itemId -> mainViewModel.deleteHistoryItem(itemId) },
                                        onBack = { activeSubScreen = SubScreenOverlay.NONE }
                                    )

                                    SubScreenOverlay.PUBLIC_WEB_PORTAL -> PublicWebPortalScreen(
                                        profile = resolvedProfile,
                                        leaderboardState = leaderboardState,
                                        customCourses = publishedCustomCourses,
                                        developerName = devName,
                                        developerAge = devAge,
                                        developerRole = devRole,
                                        initialSearchQuery = portalInitialQuery,
                                        onTogglePublicVisibility = { visible ->
                                            mainViewModel.setPublicSearchVisibility(visible)
                                        },
                                        onOpenEnglishFiftyLevels = { activeSubScreen = SubScreenOverlay.ENGLISH_50 },
                                        onOpenMathFiftyLevels = { activeSubScreen = SubScreenOverlay.MATH_50 },
                                        onOpenSpeakingLab = {
                                            activeSubScreen = SubScreenOverlay.NONE
                                            speakingTabFocusKey += 1
                                            selectedTab = 2
                                        },
                                        onOpenAiTutor = {
                                            activeSubScreen = SubScreenOverlay.NONE
                                            targetLearnSection = 0
                                            learnTabFocusKey += 1
                                            selectedTab = 1
                                        },
                                        onOpenFriendChat = { activeSubScreen = SubScreenOverlay.FRIEND_CHAT },
                                        onOpenDeveloperInfo = { showDeveloperModal = true },
                                        onStartCustomCourse = { course ->
                                            activeSubScreen = SubScreenOverlay.NONE
                                            mainViewModel.startCustomCourseLesson(course)
                                        },
                                        onShowMessage = { msg -> mainViewModel.showBanner(msg) },
                                        onBack = {
                                            portalInitialQuery = ""
                                            activeSubScreen = SubScreenOverlay.NONE
                                        }
                                    )

                                    SubScreenOverlay.NONE -> when (selectedTab) {
                                        0 -> HomeScreen(
                                            profile = resolvedProfile,
                                            unreadNotificationsCount = unreadCount,
                                            newlyRegistered = newlyRegistered,
                                            registrationRecord = ownRegistration,
                                            audioManager = audioManager,
                                            customCourses = publishedCustomCourses,
                                            onStartCustomCourse = { course ->
                                                mainViewModel.startCustomCourseLesson(course)
                                            },
                                            onClaimQuest = { questId, title, xp, gems ->
                                                mainViewModel.claimDailyQuest(questId, title, xp, gems)
                                            },
                                            onDismissNewRegistration = { mainViewModel.dismissNewRegistrationBanner() },
                                            onSelectLanguage = { lang -> mainViewModel.selectLearningLanguage(lang) },
                                            onStartLesson = { lesson -> mainViewModel.startLesson(lesson) },
                                            onOpenNotifications = { showNotificationsModal = true },
                                            onOpenAiTutorTab = {
                                                targetLearnSection = 0
                                                learnTabFocusKey += 1
                                                selectedTab = 1
                                            },
                                            onOpenSpeakingLab = {
                                                speakingTabFocusKey += 1
                                                selectedTab = 2
                                            },
                                            onOpenSubjectLab = { subjectIdx ->
                                                targetLearnSection = 2
                                                targetSubjectLabIndex = subjectIdx
                                                learnTabFocusKey += 1
                                                selectedTab = 1
                                            },
                                            onOpenEnglishFiftyLevels = { activeSubScreen = SubScreenOverlay.ENGLISH_50 },
                                            onOpenMathFiftyLevels = { activeSubScreen = SubScreenOverlay.MATH_50 },
                                            onOpenFriendChat = { activeSubScreen = SubScreenOverlay.FRIEND_CHAT },
                                            onOpenNarutoSubscription = { showNarutoSubscriptionModal = true },
                                            onOpenAppHistory = { activeSubScreen = SubScreenOverlay.APP_HISTORY },
                                            onOpenPublicWebPortal = {
                                                portalInitialQuery = ""
                                                activeSubScreen = SubScreenOverlay.PUBLIC_WEB_PORTAL
                                            },
                                            onOpenWelcomeSlideshow = { mainViewModel.openWelcomeSlideshow() },
                                            onFeedMascot = { mainViewModel.buyReward(25L, "MASCOT_TREAT", "") },
                                            onOpenDeveloperInfo = { showDeveloperModal = true },
                                            developerName = devName,
                                            developerAge = devAge,
                                            developerRole = devRole,
                                            canEditDeveloperInfo = canEditDev,
                                            onSignOut = {
                                                signOutUser(
                                                    credentialManager = credentialManager,
                                                    onSignOutComplete = {},
                                                    scope = scope
                                                )
                                            }
                                        )
                                        1 -> LearnScreen(
                                            profile = resolvedProfile,
                                            preferences = preferences,
                                            chatViewModel = chatViewModel,
                                            aiTutorViewModel = aiTutorViewModel,
                                            audioManager = audioManager,
                                            weakVocabularyWords = weakVocabWords,
                                            savedAiHistory = (appHistoryState as? DataUiState.Success)?.data.orEmpty(),
                                            onDeleteSavedHistory = { itemId -> mainViewModel.deleteHistoryItem(itemId) },
                                            learnFocusKey = learnTabFocusKey,
                                            initialSection = targetLearnSection,
                                            initialSubjectLabIndex = targetSubjectLabIndex,
                                            onEarnSubjectXp = { subject, xp, gems ->
                                                mainViewModel.awardSubjectLabReward(subject, xp, gems)
                                            },
                                            onBookmarkWord = { word, trans, lang, phonetic, ex ->
                                                mainViewModel.bookmarkDictionaryWord(word, trans, lang, phonetic, ex)
                                            },
                                            onRecordSearch = { term -> mainViewModel.recordDictionarySearch(term) },
                                            onToggleOfflinePack = { lang -> mainViewModel.toggleOfflineCourse(lang) },
                                            onOpenEnglishFiftyLevels = { activeSubScreen = SubScreenOverlay.ENGLISH_50 },
                                            onOpenMathFiftyLevels = { activeSubScreen = SubScreenOverlay.MATH_50 },
                                            onShowMessage = { msg -> mainViewModel.showBanner(msg) }
                                        )
                                        2 -> PracticeScreen(
                                            profile = resolvedProfile,
                                            vocabularyState = vocabularyState,
                                            speakingLabViewModel = speakingLabViewModel,
                                            audioManager = audioManager,
                                            speakingTabFocusKey = speakingTabFocusKey,
                                            onRegainHeart = { mainViewModel.regainHeartViaPractice() },
                                            onPromoteVocabulary = { item -> mainViewModel.reviewVocabularyCard(item) },
                                            onBookmarkWord = { word, trans, lang, phonetic, ex ->
                                                mainViewModel.bookmarkDictionaryWord(word, trans, lang, phonetic, ex)
                                            },
                                            onOpenEnglishFiftyLevels = { activeSubScreen = SubScreenOverlay.ENGLISH_50 },
                                            onOpenMathFiftyLevels = { activeSubScreen = SubScreenOverlay.MATH_50 },
                                            onShowMessage = { msg -> mainViewModel.showBanner(msg) }
                                        )
                                        3 -> CompeteScreen(
                                            profile = resolvedProfile,
                                            leaderboardState = leaderboardState,
                                            communityPostsState = communityPostsState,
                                            onBuyReward = { cost, type, extra ->
                                                mainViewModel.buyReward(cost, type, extra)
                                            },
                                            onClaimQuest = { questId, title, xp, gems ->
                                                mainViewModel.claimDailyQuest(questId, title, xp, gems)
                                            },
                                            onPublishPost = { content, tag ->
                                                mainViewModel.publishCommunityPost(content, tag)
                                            },
                                            onCheerPost = { post -> mainViewModel.cheerCommunityPost(post) },
                                            onReportPost = { id -> mainViewModel.reportPost(id) }
                                        )
                                        4 -> ProfileScreen(
                                            profile = resolvedProfile,
                                            preferences = preferences,
                                            registrationEvent = ownRegistration,
                                            isAdmin = mainViewModel.isCurrentUserAdmin,
                                            adminRegistrations = adminRegistrations,
                                            reportedPosts = reportedPosts,
                                            aiRegistrationEmailState = aiRegistrationEmailState,
                                            onGenerateAiRegistrationEmail = {
                                                aiTutorViewModel.generateAiRegistrationEmailReport(
                                                    fullName = resolvedProfile.displayName,
                                                    username = resolvedProfile.username.orEmpty(),
                                                    email = resolvedProfile.email,
                                                    userId = resolvedProfile.userId,
                                                    studentClass = resolvedProfile.studentClass.orEmpty(),
                                                    regDate = ownRegistration?.registrationDate ?: resolvedProfile.registrationDate ?: "Today",
                                                    regTime = ownRegistration?.registrationTime ?: resolvedProfile.registrationTime ?: "Now"
                                                )
                                            },
                                            onSaveProfile = { name, bio, age, nativeL, learnL, lvl, goal, relaxed, handle, studentCls, lbPrivacy ->
                                                mainViewModel.saveProfileEdits(
                                                    name, bio, age, nativeL, learnL, lvl, goal, relaxed, handle, studentCls, lbPrivacy
                                                )
                                            },
                                            onSetThemeMode = { mainViewModel.setThemeMode(it) },
                                            onSetAccentPreset = { mainViewModel.setAccentPreset(it) },
                                            onSetTextScale = { mainViewModel.setTextScale(it) },
                                            onSetSoundEffects = { mainViewModel.setSoundEffects(it) },
                                            onOpenNotifications = { showNotificationsModal = true },
                                            onRefreshAdminRegistrations = { mainViewModel.refreshAdminRegistrations() },
                                            onRefreshReportedPosts = { mainViewModel.refreshAdminReportedPosts() },
                                            onModeratePost = { postId, restoreActive ->
                                                mainViewModel.moderatePost(postId, restoreActive)
                                            },
                                            onPublishCustomCourse = { title, subject, lvl, cls, obj, prompt, opts, correct, expl, xp ->
                                                mainViewModel.publishCustomCourseLesson(
                                                    title, subject, lvl, cls, obj, prompt, opts, correct, expl, xp
                                                )
                                            },
                                            onBroadcastSystemAlert = { title, body ->
                                                mainViewModel.triggerSmartStudyNotification(title, body, "System", "📢")
                                            },
                                            onSignOut = {
                                                signOutUser(
                                                    credentialManager = credentialManager,
                                                    onSignOutComplete = {},
                                                    scope = scope
                                                )
                                            },
                                            onDeleteAccountData = {
                                                mainViewModel.deleteUserCloudData {
                                                    signOutUser(
                                                        credentialManager = credentialManager,
                                                        onSignOutComplete = {},
                                                        scope = scope
                                                    )
                                                }
                                            },
                                            onOpenDeveloperInfo = { showDeveloperModal = true },
                                            onOpenPublicWebPortal = {
                                                portalInitialQuery = ""
                                                activeSubScreen = SubScreenOverlay.PUBLIC_WEB_PORTAL
                                            },
                                            developerName = devName,
                                            developerAge = devAge,
                                            developerRole = devRole,
                                            onShowMessage = { mainViewModel.showBanner(it) }
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

    if (showRegistrationOnboarding) {
        Dialog(
            onDismissRequest = { mainViewModel.dismissRegistrationOnboarding() }
        ) {
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                CompleteRegistrationGateScreen(
                    initialFullName = resolvedProfile.displayName,
                    email = resolvedProfile.email,
                    onCompleteAllInfo = { fullName, username, age, studentClass, tier, nativeLang, learningLang, passcode ->
                        mainViewModel.completeStudentRegistration(
                            fullName = fullName,
                            username = username,
                            studentClass = studentClass,
                            age = age,
                            noxraPasscode = passcode,
                            selectedTier = tier,
                            nativeLanguage = nativeLang,
                            learningLanguage = learningLang,
                            emailForReceipt = resolvedProfile.email
                        )
                    },
                    onOpenDeveloperInfo = {
                        mainViewModel.dismissRegistrationOnboarding()
                        showDeveloperModal = true
                    },
                    onSignOut = {
                        mainViewModel.dismissRegistrationOnboarding()
                    }
                )
            }
        }
    }

    if (showWelcomeSlideshow) {
        WelcomeSlideshowDialog(
            studentName = resolvedProfile.displayName,
            username = resolvedProfile.username.orEmpty(),
            studentClass = resolvedProfile.studentClass ?: "Class 8",
            email = resolvedProfile.email,
            developerName = devName,
            developerAge = devAge,
            developerRole = devRole,
            canEditDeveloperInfo = canEditDev,
            autoAdminEmailState = autoAdminEmailState,
            onRetryAutoAiEmail = {
                mainViewModel.retryAutomaticAiAdminEmail(resolvedProfile)
            },
            onOpenEmailClientWithReport = { aiReport ->
                if (!aiReport.isNullOrBlank()) {
                    AdminRegistrationAiMailer.openEmailClientWithReport(
                        context = context,
                        subjectName = resolvedProfile.displayName,
                        username = resolvedProfile.username ?: "@student",
                        body = aiReport
                    )
                } else {
                    launchAdminRegistrationGmailIntent(
                        context = context,
                        fullName = resolvedProfile.displayName,
                        username = resolvedProfile.username ?: "@student",
                        studentClass = resolvedProfile.studentClass ?: "Class 8",
                        selectedTier = resolvedProfile.englishTier ?: "Basic",
                        email = resolvedProfile.email,
                        userId = resolvedProfile.userId,
                        registeredAt = ownRegistration?.let { "${it.registrationDate} ${it.registrationTime}" } ?: "Just Now"
                    )
                }
            },
            onOpenDeveloperEditor = {
                mainViewModel.dismissWelcomeSlideshow()
                showDeveloperModal = true
            },
            onFinish = { mainViewModel.dismissWelcomeSlideshow() }
        )
    }

    if (showNarutoSubscriptionModal) {
        NarutoSubscriptionDialog(
            profile = resolvedProfile,
            onActivateNarutoEvent = {
                mainViewModel.activateNarutoSubscription()
                showNarutoSubscriptionModal = false
            },
            onDismiss = { showNarutoSubscriptionModal = false }
        )
    }

    if (showDeveloperModal) {
        AboutDeveloperDialog(
            onDismiss = { showDeveloperModal = false },
            onFeedbackMessage = { msg -> onGlobalFeedback(msg) },
            developerName = devName,
            developerAge = devAge,
            developerRole = devRole,
            developerBio = devBio,
            canEditDeveloperInfo = canEditDev,
            onSaveDeveloperEdits = if (canEditDev) {
                { name, age, role, bio ->
                    mainViewModel.saveDeveloperOverrides(name, age, role, bio)
                }
            } else null
        )
    }

    if (showNotificationsModal) {
        NotificationCenterDialog(
            notificationsState = notificationsState,
            preferences = preferences,
            learningLanguage = resolvedProfile.learningLanguage,
            onDismiss = { showNotificationsModal = false },
            onMarkRead = { id -> mainViewModel.markNotificationRead(id) },
            onMarkAllRead = { mainViewModel.markAllNotificationsRead() },
            onDeleteNotification = { id -> mainViewModel.deleteNotification(id) },
            onTriggerSmartReminder = { title, body, cat, emoji ->
                mainViewModel.triggerSmartStudyNotification(title, body, cat, emoji)
            },
            onUpdatePrefs = { enabled, pauseAll, remTime, quiet, sound, vib, email, limit, cats ->
                mainViewModel.updateNotificationPrefs(
                    enabled, pauseAll, remTime, quiet, sound, vib, email, limit, cats
                )
            },
            onFeedbackMessage = { msg -> mainViewModel.showBanner(msg) }
        )
    }
}
