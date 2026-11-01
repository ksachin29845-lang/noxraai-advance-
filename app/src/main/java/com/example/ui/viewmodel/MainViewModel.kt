package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.AdminRegistrationAiMailer
import com.example.core.AutoAdminEmailState
import com.example.core.NOXRA_ADMIN_EMAIL
import com.example.core.RegistrationUserSnapshot
import com.example.data.curriculum.ExerciseQuestion
import com.example.data.curriculum.ExerciseType
import com.example.data.curriculum.LessonNode
import com.example.data.model.AppHistoryItem
import com.example.data.model.CommunityPost
import com.example.data.model.CustomCourseLesson
import com.example.data.model.DeveloperConfig
import com.example.data.model.DirectChatMessage
import com.example.data.model.LeaderboardEntry
import com.example.data.model.RegistrationEvent
import com.example.data.model.SavedVocabulary
import com.example.data.model.SmartNotification
import com.example.data.model.UserProfile
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.example.data.repository.AppPreferencesState
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.SachinChatRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface DataUiState<out T> {
    data object Loading : DataUiState<Nothing>
    data class Success<T>(val data: T) : DataUiState<T>
    data class Error(val message: String) : DataUiState<Nothing>
}

class MainViewModel(
    private val repository: SachinChatRepository,
    private val preferencesRepository: PreferencesRepository,
    private val currentUserId: String,
    displayNameHint: String?,
    emailHint: String?,
    photoUrlHint: String?,
    usernameHint: String? = null,
    studentClassHint: String? = null,
    passcodeHint: String? = null,
    tierHint: String? = null,
    ageHint: Int? = null,
    nativeLanguageHint: String? = null,
    learningLanguageHint: String? = null
) : ViewModel() {

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _activeLesson = MutableStateFlow<LessonNode?>(null)
    val activeLesson: StateFlow<LessonNode?> = _activeLesson.asStateFlow()

    private val _adminRegistrations = MutableStateFlow<List<RegistrationEvent>>(emptyList())
    val adminRegistrations: StateFlow<List<RegistrationEvent>> = _adminRegistrations.asStateFlow()

    private val _reportedPosts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val reportedPosts: StateFlow<List<CommunityPost>> = _reportedPosts.asStateFlow()

    private val _newlyRegisteredBanner = MutableStateFlow(false)
    val newlyRegisteredBanner: StateFlow<Boolean> = _newlyRegisteredBanner.asStateFlow()

    private val _showRegistrationOnboarding = MutableStateFlow(false)
    val showRegistrationOnboarding: StateFlow<Boolean> = _showRegistrationOnboarding.asStateFlow()

    private val _showWelcomeSlideshow = MutableStateFlow(false)
    val showWelcomeSlideshow: StateFlow<Boolean> = _showWelcomeSlideshow.asStateFlow()

    private val _autoAdminEmailState = MutableStateFlow<AutoAdminEmailState>(AutoAdminEmailState.Idle)
    val autoAdminEmailState: StateFlow<AutoAdminEmailState> = _autoAdminEmailState.asStateFlow()

    private val _latestRegistrationSnapshot = MutableStateFlow<RegistrationUserSnapshot?>(null)
    val latestRegistrationSnapshot: StateFlow<RegistrationUserSnapshot?> = _latestRegistrationSnapshot.asStateFlow()

    private val _friendSearchResults = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val friendSearchResults: StateFlow<List<LeaderboardEntry>> = _friendSearchResults.asStateFlow()

    // Two-tier Flow error handling for all Firestore streams
    val userProfileState: StateFlow<DataUiState<UserProfile?>> =
        repository.observeUserProfile(currentUserId)
            .map<UserProfile?, DataUiState<UserProfile?>> { DataUiState.Success(it) }
            .catch { err ->
                Log.w(TAG, "Error observing profile", err)
                emit(DataUiState.Error(err.message ?: "Unable to load profile"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), DataUiState.Loading)

    val notificationsState: StateFlow<DataUiState<List<SmartNotification>>> =
        repository.observeNotifications(currentUserId)
            .map<List<SmartNotification>, DataUiState<List<SmartNotification>>> { DataUiState.Success(it) }
            .catch { err ->
                Log.w(TAG, "Error observing notifications", err)
                emit(DataUiState.Error(err.message ?: "Unable to load notifications"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), DataUiState.Loading)

    val vocabularyState: StateFlow<DataUiState<List<SavedVocabulary>>> =
        repository.observeVocabulary(currentUserId)
            .map<List<SavedVocabulary>, DataUiState<List<SavedVocabulary>>> { DataUiState.Success(it) }
            .catch { err ->
                Log.w(TAG, "Error observing vocabulary", err)
                emit(DataUiState.Error(err.message ?: "Unable to load vocabulary"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), DataUiState.Loading)

    val leaderboardState: StateFlow<DataUiState<List<LeaderboardEntry>>> =
        repository.observeLeaderboard()
            .map<List<LeaderboardEntry>, DataUiState<List<LeaderboardEntry>>> { DataUiState.Success(it) }
            .catch { err ->
                Log.w(TAG, "Error observing leaderboard", err)
                emit(DataUiState.Error(err.message ?: "Unable to load leaderboard"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), DataUiState.Loading)

    val communityPostsState: StateFlow<DataUiState<List<CommunityPost>>> =
        repository.observeCommunityPosts()
            .map<List<CommunityPost>, DataUiState<List<CommunityPost>>> { DataUiState.Success(it) }
            .catch { err ->
                Log.w(TAG, "Error observing community posts", err)
                emit(DataUiState.Error(err.message ?: "Unable to load community feed"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), DataUiState.Loading)

    val customCoursesState: StateFlow<DataUiState<List<CustomCourseLesson>>> =
        repository.observeCustomCourses()
            .map<List<CustomCourseLesson>, DataUiState<List<CustomCourseLesson>>> { DataUiState.Success(it) }
            .catch { err ->
                Log.w(TAG, "Error observing custom courses", err)
                emit(DataUiState.Error(err.message ?: "Unable to load custom lessons"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), DataUiState.Loading)

    val appHistoryState: StateFlow<DataUiState<List<AppHistoryItem>>> =
        repository.observeAppHistory(currentUserId)
            .map<List<AppHistoryItem>, DataUiState<List<AppHistoryItem>>> { DataUiState.Success(it) }
            .catch { err ->
                Log.w(TAG, "Error observing app history", err)
                emit(DataUiState.Error(err.message ?: "Unable to load saved app history"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), DataUiState.Loading)

    val directMessagesState: StateFlow<DataUiState<List<DirectChatMessage>>> =
        repository.observeDirectMessages(currentUserId)
            .map<List<DirectChatMessage>, DataUiState<List<DirectChatMessage>>> { DataUiState.Success(it) }
            .catch { err ->
                Log.w(TAG, "Error observing direct messages", err)
                emit(DataUiState.Error(err.message ?: "Unable to load friend chats"))
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), DataUiState.Loading)

    val ownRegistrationState: StateFlow<RegistrationEvent?> =
        repository.observeOwnRegistrationEvent(currentUserId)
            .catch { err ->
                Log.w(TAG, "Error observing registration record", err)
                emit(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), null)

    val developerConfigState: StateFlow<DeveloperConfig?> =
        repository.observeDeveloperConfig()
            .catch { err ->
                Log.w(TAG, "Error observing developer config", err)
                emit(null)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), null)

    val preferencesState: StateFlow<AppPreferencesState> =
        preferencesRepository.preferencesFlow
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT), AppPreferencesState())

    private val liveAuthUser = runCatching { Firebase.auth.currentUser }.getOrNull()

    val isCurrentUserAdmin: Boolean =
        (liveAuthUser?.let {
            it.email.equals(NOXRA_ADMIN_EMAIL, ignoreCase = true) && it.isEmailVerified
        } == true) || (emailHint?.equals(NOXRA_ADMIN_EMAIL, ignoreCase = true) == true &&
            liveAuthUser?.isEmailVerified != false)

    init {
        viewModelScope.launch {
            val result = repository.ensureUserBootstrapped(
                displayNameHint = displayNameHint,
                emailHint = emailHint,
                photoUrlHint = photoUrlHint,
                usernameHint = usernameHint,
                studentClassHint = studentClassHint,
                passcodeHint = passcodeHint,
                tierHint = tierHint,
                ageHint = ageHint,
                nativeLanguageHint = nativeLanguageHint,
                learningLanguageHint = learningLanguageHint
            )
            result.onSuccess { isNew ->
                if (isNew) {
                    _newlyRegisteredBanner.value = true
                    if (usernameHint.isNullOrBlank()) {
                        // Mandatory registration gate will hold the user until all info is completed
                        _showRegistrationOnboarding.value = true
                    } else {
                        val now = Date()
                        val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)
                        val timeFmt = SimpleDateFormat("hh:mm a", Locale.US).format(now)
                        val cleanHandle = if (usernameHint.startsWith("@")) usernameHint else "@$usernameHint"
                        val snapshot = RegistrationUserSnapshot(
                            userId = currentUserId,
                            fullName = displayNameHint?.ifBlank { "Student" } ?: "Student",
                            username = cleanHandle,
                            age = ageHint ?: 13,
                            studentClass = studentClassHint?.ifBlank { "Class 8" } ?: "Class 8",
                            selectedTier = tierHint?.ifBlank { "Basic" } ?: "Basic",
                            nativeLanguage = nativeLanguageHint?.ifBlank { "Hindi" } ?: "Hindi",
                            learningLanguage = learningLanguageHint?.ifBlank { "English" } ?: "English",
                            email = emailHint ?: "learner@noxra.ai",
                            hasPasscode = !passcodeHint.isNullOrBlank(),
                            registeredDate = dateFmt,
                            registeredTime = timeFmt
                        )
                        _showWelcomeSlideshow.value = true
                        triggerAutomaticAiAdminEmail(snapshot)
                        _statusBanner.value = "🎉 Welcome to Our Noxra AI App! Sending instant AI account info to Admin..."
                    }
                }
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Cloud sync warning"
            }

            if (isCurrentUserAdmin) {
                refreshAdminRegistrations()
                refreshAdminReportedPosts()
            }
        }
    }

    private fun triggerAutomaticAiAdminEmail(snapshot: RegistrationUserSnapshot) {
        _latestRegistrationSnapshot.value = snapshot
        viewModelScope.launch {
            val (aiSummary, statusLabel) = AdminRegistrationAiMailer.composeWithAiAndDispatchToAdmin(
                snapshot = snapshot,
                onProgress = { state -> _autoAdminEmailState.value = state }
            )
            repository.saveRegistrationAiEmailAudit(
                aiSummary = aiSummary,
                deliveryStatus = statusLabel
            )
            if (isCurrentUserAdmin) {
                refreshAdminRegistrations()
            }
        }
    }

    fun retryAutomaticAiAdminEmail(profile: UserProfile) {
        val now = Date()
        val snapshot = _latestRegistrationSnapshot.value ?: RegistrationUserSnapshot(
            userId = profile.userId.ifBlank { currentUserId },
            fullName = profile.displayName,
            username = profile.username ?: "@student",
            age = profile.age?.toInt() ?: 13,
            studentClass = profile.studentClass ?: "Class 8",
            selectedTier = profile.englishTier ?: "Basic",
            nativeLanguage = profile.nativeLanguage,
            learningLanguage = profile.learningLanguage,
            email = profile.email,
            hasPasscode = !profile.noxraPasscode.isNullOrBlank(),
            registeredDate = profile.registrationDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now),
            registeredTime = profile.registrationTime ?: SimpleDateFormat("hh:mm a", Locale.US).format(now)
        )
        triggerAutomaticAiAdminEmail(snapshot)
    }

    fun dismissRegistrationOnboarding() {
        _showRegistrationOnboarding.value = false
    }

    fun openRegistrationOnboarding() {
        _showRegistrationOnboarding.value = true
    }

    fun openWelcomeSlideshow() {
        _showWelcomeSlideshow.value = true
    }

    fun dismissWelcomeSlideshow() {
        _showWelcomeSlideshow.value = false
    }

    fun completeStudentRegistration(
        fullName: String,
        username: String,
        studentClass: String,
        age: Int = 13,
        noxraPasscode: String? = null,
        selectedTier: String? = "Basic",
        nativeLanguage: String = "Hindi",
        learningLanguage: String = "English",
        emailForReceipt: String = "",
        onCompleted: (String) -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.completeRegistrationOnboarding(
                fullName = fullName,
                username = username,
                studentClass = studentClass,
                noxraPasscode = noxraPasscode,
                selectedTier = selectedTier,
                age = age,
                nativeLanguage = nativeLanguage,
                learningLanguage = learningLanguage
            )
                .onSuccess { handle ->
                    _showRegistrationOnboarding.value = false
                    _newlyRegisteredBanner.value = true
                    _showWelcomeSlideshow.value = true
                    val now = Date()
                    val snapshot = RegistrationUserSnapshot(
                        userId = currentUserId,
                        fullName = fullName.ifBlank { "Student" },
                        username = handle,
                        age = age,
                        studentClass = studentClass.ifBlank { "Class 8" },
                        selectedTier = selectedTier ?: "Basic",
                        nativeLanguage = nativeLanguage,
                        learningLanguage = learningLanguage,
                        email = emailForReceipt.ifBlank { "Verified Google Account" },
                        hasPasscode = !noxraPasscode.isNullOrBlank(),
                        registeredDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now),
                        registeredTime = SimpleDateFormat("hh:mm a", Locale.US).format(now)
                    )
                    triggerAutomaticAiAdminEmail(snapshot)
                    _statusBanner.value = "🎉 Welcome to Our Noxra AI App, $fullName ($handle)! AI dispatched your account info to Admin."
                    onCompleted(handle)
                }
                .onFailure { err ->
                    _statusBanner.value = err.localizedMessage ?: "Could not save registration details"
                }
        }
    }

    fun dismissNewRegistrationBanner() {
        _newlyRegisteredBanner.value = false
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun showBanner(message: String) {
        _statusBanner.value = message
    }

    fun startLesson(lesson: LessonNode) {
        val currentProfile = (userProfileState.value as? DataUiState.Success)?.data
        if (currentProfile != null && currentProfile.relaxedMode != true && currentProfile.hearts <= 0L) {
            _statusBanner.value = "Out of ❤️ Hearts! Practice in the Practice tab to regain a heart or enable Relaxed Mode."
            return
        }
        _activeLesson.value = lesson
    }

    fun exitLesson() {
        _activeLesson.value = null
    }

    fun onLessonMistake(vocabWord: String, vocabMeaning: String, language: String) {
        val profile = (userProfileState.value as? DataUiState.Success)?.data
        viewModelScope.launch {
            if (profile != null) {
                repository.adjustHearts(profile, -1)
            }
            if (vocabWord.isNotBlank()) {
                repository.saveVocabularyItem(
                    word = vocabWord,
                    translation = vocabMeaning,
                    language = language,
                    isMistake = true,
                    isFavorite = false
                )
            }
        }
    }

    fun startCustomCourseLesson(course: CustomCourseLesson) {
        val playableNode = LessonNode(
            id = course.id.ifBlank { "custom_${course.title.hashCode()}" },
            index = 1,
            unitTitle = "Custom • ${course.subjectOrLanguage}",
            title = course.title,
            subtitle = course.objective,
            tier = course.level,
            category = "Custom",
            xpReward = course.xpReward.toInt(),
            gemReward = 15,
            iconEmoji = "🎓",
            questions = listOf(
                ExerciseQuestion(
                    id = "custom_q1",
                    type = ExerciseType.MULTIPLE_CHOICE,
                    prompt = course.questionPrompt,
                    audioPhrase = course.correctAnswer,
                    options = course.options,
                    correctAnswer = course.correctAnswer,
                    explanation = course.explanation,
                    keyVocabWord = course.subjectOrLanguage,
                    keyVocabMeaning = course.objective
                )
            )
        )
        startLesson(playableNode)
    }

    fun onLessonFinished(lesson: LessonNode, mistakesCount: Int) {
        val profile = (userProfileState.value as? DataUiState.Success)?.data ?: return
        val nextUnlock = if (lesson.category == "Custom") {
            (profile.unlockedLessonsCount ?: 2L).toInt()
        } else {
            lesson.index + 1
        }
        viewModelScope.launch {
            val result = repository.recordLessonCompleted(
                currentProfile = profile,
                earnedXp = lesson.xpReward,
                earnedGems = lesson.gemReward,
                perfectScore = (mistakesCount == 0),
                nextLessonUnlockIndex = nextUnlock
            )
            _activeLesson.value = null
            result.onSuccess {
                val bonus = if (mistakesCount == 0) " (Perfect +10 Bonus XP!)" else ""
                _statusBanner.value = "🎉 Completed '${lesson.title}'! +${lesson.xpReward} XP$bonus & +${lesson.gemReward} Gems!"
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Could not save lesson progress"
            }
        }
    }

    fun awardSubjectLabReward(subjectName: String, earnedXp: Int, earnedGems: Int) {
        val profile = (userProfileState.value as? DataUiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.awardSubjectLabXp(profile, subjectName, earnedXp, earnedGems).onSuccess {
                _statusBanner.value = "🌟 $subjectName Challenge Solved! +$earnedXp XP & +$earnedGems Gems!"
            }
        }
    }

    fun regainHeartViaPractice() {
        val profile = (userProfileState.value as? DataUiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.adjustHearts(profile, +1).onSuccess {
                _statusBanner.value = "❤️ +1 Heart earned through dedicated practice!"
            }
        }
    }

    fun selectLearningLanguage(newLanguage: String) {
        val profile = (userProfileState.value as? DataUiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.updateProfileDetails(
                displayName = profile.displayName,
                bio = profile.bio ?: "",
                age = profile.age,
                nativeLanguage = profile.nativeLanguage,
                learningLanguage = newLanguage,
                skillLevel = profile.skillLevel,
                dailyGoalMinutes = profile.dailyGoalMinutes,
                relaxedMode = profile.relaxedMode == true,
                username = profile.username,
                studentClass = profile.studentClass
            ).onSuccess {
                _statusBanner.value = "🌍 Switched active course to $newLanguage!"
            }
        }
    }

    fun saveProfileEdits(
        displayName: String,
        bio: String,
        age: Long?,
        nativeLanguage: String,
        learningLanguage: String,
        skillLevel: String,
        dailyGoalMinutes: Long,
        relaxedMode: Boolean,
        username: String? = null,
        studentClass: String? = null,
        leaderboardPrivacy: Boolean? = null
    ) {
        viewModelScope.launch {
            repository.updateProfileDetails(
                displayName = displayName,
                bio = bio,
                age = age,
                nativeLanguage = nativeLanguage,
                learningLanguage = learningLanguage,
                skillLevel = skillLevel,
                dailyGoalMinutes = dailyGoalMinutes,
                relaxedMode = relaxedMode,
                username = username,
                studentClass = studentClass,
                leaderboardPrivacy = leaderboardPrivacy
            ).onSuccess {
                _statusBanner.value = "✅ Profile, @username & learning goals synced to cloud!"
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to update profile"
            }
        }
    }

    fun setPublicSearchVisibility(visibleInPublicDirectory: Boolean) {
        val profile = (userProfileState.value as? DataUiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.updateProfileDetails(
                displayName = profile.displayName,
                bio = profile.bio ?: "",
                age = profile.age,
                nativeLanguage = profile.nativeLanguage,
                learningLanguage = profile.learningLanguage,
                skillLevel = profile.skillLevel,
                dailyGoalMinutes = profile.dailyGoalMinutes,
                relaxedMode = profile.relaxedMode == true,
                username = profile.username,
                studentClass = profile.studentClass,
                leaderboardPrivacy = !visibleInPublicDirectory
            ).onSuccess {
                _statusBanner.value = if (visibleInPublicDirectory) {
                    "🌐 Public Search Visibility ON — anyone can find your @username!"
                } else {
                    "🔒 Public Search Visibility OFF — hidden from public search directory."
                }
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to update public visibility"
            }
        }
    }

    fun buyReward(costGems: Long, rewardType: String, extraValue: String = "") {
        val profile = (userProfileState.value as? DataUiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.purchaseShopReward(profile, costGems, rewardType, extraValue)
                .onSuccess { msg -> _statusBanner.value = msg }
                .onFailure { err -> _statusBanner.value = err.localizedMessage ?: "Purchase failed" }
        }
    }

    fun claimDailyQuest(questId: String, questTitle: String, xpReward: Int, gemReward: Int) {
        val profile = (userProfileState.value as? DataUiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.claimQuestReward(profile, questId, questTitle, xpReward, gemReward)
                .onSuccess { msg -> _statusBanner.value = msg }
                .onFailure { err -> _statusBanner.value = err.localizedMessage ?: "Quest claim failed" }
        }
    }

    fun bookmarkDictionaryWord(
        word: String,
        translation: String,
        language: String,
        phonetic: String? = null,
        example: String? = null
    ) {
        viewModelScope.launch {
            repository.saveVocabularyItem(
                word = word,
                translation = translation,
                language = language,
                isMistake = false,
                isFavorite = true,
                phonetic = phonetic,
                exampleSentence = example
            ).onSuccess {
                _statusBanner.value = "📖 Saved '$word' to your Smart Vocabulary Notebook!"
            }
        }
    }

    fun reviewVocabularyCard(item: SavedVocabulary) {
        viewModelScope.launch {
            repository.promoteVocabularyMastery(item).onSuccess {
                _statusBanner.value = "🧠 Mastery increased for '${item.word}'!"
            }
        }
    }

    fun publishCommunityPost(content: String, tag: String) {
        val profile = (userProfileState.value as? DataUiState.Success)?.data ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            repository.createCommunityPost(
                authorName = profile.displayName,
                language = profile.learningLanguage,
                content = content,
                tag = tag
            ).onSuccess {
                _statusBanner.value = "💬 Shared with the Noxra AI learner community!"
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Could not publish post"
            }
        }
    }

    fun cheerCommunityPost(post: CommunityPost) {
        viewModelScope.launch {
            repository.sendCheerToPost(post).onSuccess {
                _statusBanner.value = "👏 Sent encouragement to ${post.authorName}!"
            }
        }
    }

    fun reportPost(postId: String) {
        viewModelScope.launch {
            repository.reportCommunityPost(postId).onSuccess {
                _statusBanner.value = "🛡️ Post reported and hidden for moderator review."
            }
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        val list = (notificationsState.value as? DataUiState.Success)?.data.orEmpty()
        viewModelScope.launch {
            repository.markAllNotificationsRead(list).onSuccess {
                _statusBanner.value = "✅ All notifications marked as read."
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch { repository.deleteNotification(id) }
    }

    fun triggerSmartStudyNotification(title: String, body: String, category: String, emoji: String) {
        viewModelScope.launch {
            repository.createNotification(title, body, category, emoji, "learn")
        }
    }

    fun refreshAdminRegistrations() {
        viewModelScope.launch {
            repository.fetchAllRegistrationsForAdmin().onSuccess {
                _adminRegistrations.value = it
            }
        }
    }

    fun refreshAdminReportedPosts() {
        viewModelScope.launch {
            repository.fetchReportedPostsForAdmin().onSuccess {
                _reportedPosts.value = it
            }
        }
    }

    fun moderatePost(postId: String, restoreActive: Boolean) {
        viewModelScope.launch {
            repository.moderateReportedPost(postId, restoreActive)
                .onSuccess {
                    _statusBanner.value = if (restoreActive) {
                        "✅ Post restored to community feed."
                    } else {
                        "🗑️ Reported post permanently removed."
                    }
                    refreshAdminReportedPosts()
                }
                .onFailure { err ->
                    _statusBanner.value = err.localizedMessage ?: "Moderation action failed"
                }
        }
    }

    fun publishCustomCourseLesson(
        title: String,
        subjectOrLanguage: String,
        level: String,
        studentClass: String?,
        objective: String,
        questionPrompt: String,
        options: List<String>,
        correctAnswer: String,
        explanation: String,
        xpReward: Long
    ) {
        viewModelScope.launch {
            repository.createCustomCourseLesson(
                title = title,
                subjectOrLanguage = subjectOrLanguage,
                level = level,
                studentClass = studentClass,
                objective = objective,
                questionPrompt = questionPrompt,
                options = options,
                correctAnswer = correctAnswer,
                explanation = explanation,
                xpReward = xpReward
            ).onSuccess {
                _statusBanner.value = "🎓 Published custom lesson '$title' for all Noxra AI students!"
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to publish custom lesson"
            }
        }
    }

    // --- Noxra AI 50-Level Paths, Naruto ₹49 Subscription, Developer Edits, History & Friend Chat ---

    fun selectFiftyLevelConfig(isMath: Boolean, studentClass: String, tier: String) {
        viewModelScope.launch {
            repository.updateFiftyLevelSelection(isMath, studentClass, tier).onSuccess {
                val subj = if (isMath) "Math" else "English"
                _statusBanner.value = "✅ Updated $subj 1–50 path: $studentClass • $tier Tier!"
            }
        }
    }

    fun completeFiftyLevel(isMath: Boolean, levelNumber: Int, studentClass: String, tier: String, mistakesCount: Int) {
        val profile = (userProfileState.value as? DataUiState.Success)?.data ?: return
        val baseXp = 20 + (levelNumber / 5) * 5 + if (mistakesCount == 0) 10 else 0
        val displayXp = if (profile.subscriptionActive == true) baseXp * 2 else baseXp
        val summary = if (mistakesCount == 0) "Flawless 3/3 mastery" else "Completed with $mistakesCount mistake(s)"
        viewModelScope.launch {
            repository.completeFiftyLevelNode(
                currentProfile = profile,
                isMath = isMath,
                studentClass = studentClass,
                tier = tier,
                completedLevel = levelNumber,
                earnedXp = baseXp,
                earnedGems = 12,
                scoreSummary = summary
            ).onSuccess { nextUnlocked ->
                val subj = if (isMath) "Math" else "English"
                val nextTag = if (levelNumber < 50) "Level $nextUnlocked Unlocked!" else "🏆 All 50 Levels Mastered!"
                _statusBanner.value = "🎉 $subj Level $levelNumber ($tier) Complete! +$displayXp XP • $nextTag"
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to save level progress"
            }
        }
    }

    fun activateNarutoSubscription() {
        val profile = (userProfileState.value as? DataUiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.activateNarutoEventSubscription(profile)
                .onSuccess { msg -> _statusBanner.value = msg }
                .onFailure { err -> _statusBanner.value = err.localizedMessage ?: "Subscription activation failed" }
        }
    }

    fun saveDeveloperOverrides(name: String, ageText: String, roleText: String, bioText: String) {
        if (!isCurrentUserAdmin) {
            _statusBanner.value = "🔒 Only the app owner (Sachin Kumar • $NOXRA_ADMIN_EMAIL) can edit Developer Info."
            return
        }
        viewModelScope.launch {
            repository.updateDeveloperProfileOverrides(name, ageText, roleText, bioText)
                .onSuccess { _statusBanner.value = "✅ Official Developer Profile updated & synced to Cloud!" }
                .onFailure { err -> _statusBanner.value = err.localizedMessage ?: "Could not save developer edits" }
        }
    }

    fun recordAiChatHistory(prompt: String, reply: String, mode: String) {
        viewModelScope.launch {
            val shortPrompt = prompt.replace('\n', ' ').take(90)
            val shortReply = reply.take(900)
            repository.recordAppHistory(
                category = "Noxra AI",
                title = "Noxra AI ($mode): $shortPrompt",
                summary = "Q: $shortPrompt\nA: $shortReply",
                subjectOrMode = mode,
                xpEarned = 5
            )
        }
    }

    fun deleteHistoryItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteAppHistoryItem(itemId)
        }
    }

    fun searchFriendsByUsername(query: String) {
        viewModelScope.launch {
            repository.searchFriendsByUsername(query).onSuccess { matches ->
                _friendSearchResults.value = matches.filter { it.userId != currentUserId }
            }
        }
    }

    fun sendDirectMessageToFriend(
        recipientId: String,
        recipientName: String,
        recipientUsername: String?,
        text: String
    ) {
        val profile = (userProfileState.value as? DataUiState.Success)?.data ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendDirectMessage(
                senderProfile = profile,
                recipientId = recipientId,
                recipientName = recipientName,
                recipientUsername = recipientUsername,
                text = text
            ).onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to send message"
            }
        }
    }

    // --- Local Preferences Actions ---

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setAccentPreset(preset: String) {
        viewModelScope.launch { preferencesRepository.setAccentPreset(preset) }
    }

    fun setTextScale(scale: Float) {
        viewModelScope.launch { preferencesRepository.setTextScale(scale) }
    }

    fun setSoundEffects(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setSoundEffects(enabled) }
    }

    fun toggleOfflineCourse(languageName: String) {
        viewModelScope.launch {
            preferencesRepository.toggleOfflineCourseDownload(languageName)
            _statusBanner.value = "📶 Updated offline lesson pack for $languageName!"
        }
    }

    fun recordDictionarySearch(term: String) {
        viewModelScope.launch { preferencesRepository.recordDictionarySearch(term) }
    }

    fun updateNotificationPrefs(
        enabled: Boolean? = null,
        pauseAll: Boolean? = null,
        reminderTime: String? = null,
        quietHours: String? = null,
        sound: Boolean? = null,
        vibration: Boolean? = null,
        email: Boolean? = null,
        dailyLimit: Int? = null,
        categories: Set<String>? = null
    ) {
        viewModelScope.launch {
            preferencesRepository.updateNotificationSettings(
                enabled, pauseAll, reminderTime, quietHours, sound, vibration, email, dailyLimit, categories
            )
        }
    }

    fun deleteUserCloudData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAccountData().onSuccess {
                onComplete()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to delete cloud account data"
            }
        }
    }

    private companion object {
        const val TAG = "MainViewModel"
        const val STOP_TIMEOUT = 5000L
    }
}
