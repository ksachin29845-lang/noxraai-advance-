package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.core.OperationType
import com.example.core.handleFirestoreError
import com.example.data.curriculum.LanguageCurriculum
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SachinChatRepository(
    private val db: FirebaseFirestore
) {
    // Convenience constructor strictly passing R.string.firestore_database_id
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth
        get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. Bootstrap User Profile, Leaderboard Entry & New Registration Notification ---

    suspend fun ensureUserBootstrapped(
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
    ): Result<Boolean> {
        val uid = requireUserId()
        val userDocRef = db.collection("users").document(uid)
        return try {
            val existingSnap = userDocRef.get().await()
            if (existingSnap.exists()) {
                // Ensure returning users always have a synced /leaderboard/{uid} entry so all ranked users appear
                val leaderboardRef = db.collection("leaderboard").document(uid)
                val lbSnap = leaderboardRef.get().await()
                if (!lbSnap.exists()) {
                    val existingXp = existingSnap.getLong("xp") ?: 50L
                    val existingStreak = existingSnap.getLong("streak") ?: 1L
                    val existingPrivacy = existingSnap.getBoolean("leaderboardPrivacy") == true
                    val backfilledEntry = LeaderboardEntry(
                        userId = uid,
                        displayName = existingSnap.getString("displayName") ?: displayNameHint ?: "Learner",
                        username = existingSnap.getString("username"),
                        studentClass = existingSnap.getString("studentClass"),
                        learningLanguage = existingSnap.getString("learningLanguage") ?: "English",
                        xp = existingXp,
                        streak = existingStreak,
                        league = LanguageCurriculum.leagueForXp(existingXp),
                        visibility = if (existingPrivacy) "private" else "public",
                        avatarEmoji = "⭐",
                        fluencyScore = existingSnap.getLong("fluencyScore") ?: 20L
                    )
                    leaderboardRef.set(backfilledEntry.toCreateMap()).await()
                }

                // Returning user: never force re-registration! Only merge hints if account had no username yet.
                val existingUsername = existingSnap.getString("username")
                val normalizedUsername = normalizeUsername(usernameHint)
                if (existingUsername.isNullOrBlank() && normalizedUsername != null) {
                    val safeName = displayNameHint?.trim()?.take(80)?.takeIf { it.isNotBlank() }
                        ?: existingSnap.getString("displayName") ?: "Learner"
                    completeRegistrationOnboarding(
                        fullName = safeName,
                        username = normalizedUsername,
                        studentClass = studentClassHint.orEmpty(),
                        noxraPasscode = passcodeHint,
                        selectedTier = tierHint,
                        age = ageHint,
                        nativeLanguage = nativeLanguageHint,
                        learningLanguage = learningLanguageHint
                    )
                }
                Result.success(false) // Returning user — instant login!
            } else {
                val now = Date()
                val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now)
                val timeFmt = SimpleDateFormat("hh:mm a", Locale.US).format(now)
                val safeName = displayNameHint?.trim()?.takeIf { it.isNotBlank() } ?: "Learner"
                val safeEmail = emailHint?.trim()?.takeIf { it.isNotBlank() } ?: "learner@noxra.ai"
                val normalizedUsername = normalizeUsername(usernameHint)
                val safeClass = studentClassHint?.trim()?.take(40)?.takeIf { it.isNotBlank() } ?: "Class 8"
                val safePasscode = passcodeHint?.trim()?.take(32)?.takeIf { it.isNotBlank() }
                val safeTier = tierHint?.takeIf { it in setOf("Basic", "Mid", "Advance") } ?: "Basic"
                val safeAge = ageHint?.coerceIn(5, 120)?.toLong()
                val safeNativeLang = nativeLanguageHint?.trim()?.take(40)?.takeIf { it.isNotBlank() } ?: "Hindi"
                val safeLearningLang = learningLanguageHint?.trim()?.take(40)?.takeIf { it.isNotBlank() } ?: "English"
                val hasCompletedOnboarding = normalizedUsername != null

                val newProfile = UserProfile(
                    userId = uid,
                    displayName = safeName,
                    username = normalizedUsername,
                    studentClass = if (hasCompletedOnboarding) safeClass else null,
                    noxraPasscode = safePasscode,
                    onboardingCompleted = hasCompletedOnboarding,
                    email = safeEmail,
                    nativeLanguage = safeNativeLang,
                    learningLanguage = safeLearningLang,
                    skillLevel = when (safeTier) {
                        "Advance" -> "Advanced"
                        "Mid" -> "Intermediate"
                        else -> "Beginner"
                    },
                    englishClass = safeClass,
                    englishTier = safeTier,
                    englishUnlockedLevel = 1L,
                    mathClass = safeClass,
                    mathTier = safeTier,
                    mathUnlockedLevel = 1L,
                    subscriptionPlan = "FREE",
                    subscriptionActive = false,
                    subscriptionPriceInr = 0L,
                    xp = 50L,
                    streak = 1L,
                    hearts = 5L,
                    gems = 150L,
                    lessonsCompleted = 0L,
                    dailyGoalMinutes = 15L,
                    bio = "Learning $safeLearningLang & Math Levels 1–50 on Noxra AI!",
                    age = safeAge,
                    photoUrl = photoUrlHint,
                    streakFreezeCount = 1L,
                    relaxedMode = false,
                    themePreference = "Emerald",
                    mascotName = "Kavi",
                    mascotLevel = 1L,
                    mascotOutfit = "Explorer Scarf",
                    unlockedLessonsCount = 2L,
                    fluencyScore = 20L,
                    registrationDate = dateFmt,
                    registrationTime = timeFmt,
                    registrationMethod = "Google"
                )
                userDocRef.set(newProfile.toCreateMap()).await()

                // Public PII-free leaderboard & @username friend directory entry
                val leaderboardRef = db.collection("leaderboard").document(uid)
                val leaderboardEntry = LeaderboardEntry(
                    userId = uid,
                    displayName = safeName,
                    username = normalizedUsername,
                    studentClass = if (hasCompletedOnboarding) safeClass else null,
                    learningLanguage = safeLearningLang,
                    xp = 50L,
                    streak = 1L,
                    league = "Bronze",
                    visibility = "public",
                    avatarEmoji = "🦉",
                    fluencyScore = 20L
                )
                leaderboardRef.set(leaderboardEntry.toCreateMap()).await()

                // Automatic New User Registration Event for Admin Dashboard & System Record
                val regRef = db.collection("registration_events").document(uid)
                val regEvent = RegistrationEvent(
                    userId = uid,
                    userName = safeName,
                    studentHandle = normalizedUsername,
                    studentClass = if (hasCompletedOnboarding) safeClass else null,
                    studentAge = safeAge,
                    selectedTier = if (hasCompletedOnboarding) safeTier else null,
                    nativeLanguage = safeNativeLang,
                    learningLanguage = safeLearningLang,
                    email = safeEmail,
                    registrationDate = dateFmt,
                    registrationTime = timeFmt,
                    registrationMethod = "Google",
                    autoEmailStatus = if (hasCompletedOnboarding) "Queued for automatic AI dispatch" else "Awaiting mandatory profile completion"
                )
                regRef.set(regEvent.toCreateMap()).await()

                val handleTag = normalizedUsername?.let { " ($it)" }.orEmpty()
                createNotification(
                    title = "🎉 Welcome to Our Noxra AI App!",
                    body = "Welcome $safeName$handleTag ($safeEmail)! Created by Developer Sachin Kumar (CEO, Founder & Owner). Class: $safeClass • Tier: $safeTier.",
                    category = "System",
                    iconEmoji = "🎉",
                    deepLinkRoute = "profile"
                )
                createNotification(
                    title = "✨ Noxra AI Tutor Ready!",
                    body = "Open Noxra AI anytime to practice English & Math Levels 1–50, solve step-by-step equations, or chat.",
                    category = "AI Tutor",
                    iconEmoji = "✨",
                    deepLinkRoute = "learn"
                )
                createNotification(
                    title = "🔥 Day 1 Streak & Levels 1–50 Unlocked!",
                    body = "Start Level 1 in English or Math, or explore the Naruto Event ₹49 pass!",
                    category = "Learning",
                    iconEmoji = "🔥",
                    deepLinkRoute = "home"
                )
                recordAppHistory(
                    category = "System",
                    title = "Account Created on Noxra AI",
                    summary = "Account created for $safeName$handleTag ($safeEmail) • $safeClass • $safeTier Tier. Cloud sync active.",
                    subjectOrMode = safeClass,
                    xpEarned = 50
                )
                Result.success(true) // Newly registered!
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userDocRef.path)
            Result.failure(e)
        }
    }

    private fun normalizeUsername(raw: String?): String? {
        val cleaned = raw?.trim()?.removePrefix("@")
            ?.replace(Regex("[^a-zA-Z0-9_.]"), "")
            ?.take(36)
            ?.takeIf { it.length >= 2 }
        return cleaned?.let { "@$it" }
    }

    suspend fun completeRegistrationOnboarding(
        fullName: String,
        username: String,
        studentClass: String,
        noxraPasscode: String? = null,
        selectedTier: String? = null,
        age: Int? = null,
        nativeLanguage: String? = null,
        learningLanguage: String? = null
    ): Result<String> {
        val uid = requireUserId()
        val safeName = fullName.trim().take(80).ifBlank { "Learner" }
        val normalizedUsername = normalizeUsername(username)
            ?: "@${safeName.lowercase(Locale.US).replace(Regex("[^a-z0-9]"), "").take(18).ifBlank { "student" }}"
        val safeClass = studentClass.trim().take(40).ifBlank { "Class 8" }
        val safeTier = selectedTier?.takeIf { it in setOf("Basic", "Mid", "Advance") } ?: "Basic"
        val safePasscode = noxraPasscode?.trim()?.take(32)?.takeIf { it.isNotBlank() }
        val safeAge = age?.coerceIn(5, 120)?.toLong()
        val safeNativeLang = nativeLanguage?.trim()?.take(40)?.takeIf { it.isNotBlank() }
        val safeLearningLang = learningLanguage?.trim()?.take(40)?.takeIf { it.isNotBlank() }
        val userRef = db.collection("users").document(uid)
        val leaderboardRef = db.collection("leaderboard").document(uid)
        val regRef = db.collection("registration_events").document(uid)

        return try {
            val userUpdates = mutableMapOf<String, Any>(
                "displayName" to safeName,
                "username" to normalizedUsername,
                "studentClass" to safeClass,
                "englishClass" to safeClass,
                "englishTier" to safeTier,
                "mathClass" to safeClass,
                "mathTier" to safeTier,
                "skillLevel" to when (safeTier) {
                    "Advance" -> "Advanced"
                    "Mid" -> "Intermediate"
                    else -> "Beginner"
                },
                "onboardingCompleted" to true,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (safePasscode != null) {
                userUpdates["noxraPasscode"] = safePasscode
            }
            if (safeAge != null) {
                userUpdates["age"] = safeAge
            }
            if (safeNativeLang != null) {
                userUpdates["nativeLanguage"] = safeNativeLang
            }
            if (safeLearningLang != null) {
                userUpdates["learningLanguage"] = safeLearningLang
            }
            userRef.update(userUpdates).await()

            val lbOnboardUpdates = mutableMapOf<String, Any>(
                "displayName" to safeName,
                "username" to normalizedUsername,
                "studentClass" to safeClass,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (safeLearningLang != null) {
                lbOnboardUpdates["learningLanguage"] = safeLearningLang
            }
            leaderboardRef.update(lbOnboardUpdates).await()

            val regSnap = regRef.get().await()
            if (regSnap.exists()) {
                val regUpdates = mutableMapOf<String, Any>(
                    "userName" to safeName,
                    "studentHandle" to normalizedUsername,
                    "studentClass" to safeClass,
                    "selectedTier" to safeTier
                )
                if (safeAge != null) {
                    regUpdates["studentAge"] = safeAge
                }
                if (safeNativeLang != null) {
                    regUpdates["nativeLanguage"] = safeNativeLang
                }
                if (safeLearningLang != null) {
                    regUpdates["learningLanguage"] = safeLearningLang
                }
                regRef.update(regUpdates).await()
            }

            createNotification(
                title = "✅ Welcome to Our Noxra AI App ($normalizedUsername)!",
                body = "Registered as $safeName ($normalizedUsername) • $safeClass • $safeTier. Developer: Sachin Kumar. Instant AI user report dispatched to admin!",
                category = "System",
                iconEmoji = "🆔",
                deepLinkRoute = "profile"
            )
            recordAppHistory(
                category = "System",
                title = "Completed Full Registration ($normalizedUsername)",
                summary = "Profile verified: $safeName • $normalizedUsername • Age ${safeAge ?: "-"} • $safeClass • $safeTier Tier.",
                subjectOrMode = "$safeClass • $safeTier"
            )
            Result.success(normalizedUsername)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveRegistrationAiEmailAudit(
        aiSummary: String,
        deliveryStatus: String
    ): Result<Unit> {
        val uid = requireUserId()
        val regRef = db.collection("registration_events").document(uid)
        return try {
            val safeSummary = aiSummary.trim().take(2400).ifBlank { "AI registration summary prepared." }
            val safeStatus = deliveryStatus.trim().take(120).ifBlank { "Sent via Noxra AI" }
            val regSnap = regRef.get().await()
            if (regSnap.exists()) {
                regRef.update(
                    mapOf(
                        "aiAdminEmailSummary" to safeSummary,
                        "autoEmailStatus" to safeStatus
                    )
                ).await()
            }
            recordAppHistory(
                category = "System",
                title = "Instant AI Admin Email Dispatched",
                summary = "Sent new account info to ksachin29845@gmail.com ($safeStatus).",
                subjectOrMode = "Admin AI Alert"
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, regRef.path)
            Result.failure(e)
        }
    }

    // --- 2. Realtime Observation Flows ---

    fun observeUserProfile(userId: String): Flow<UserProfile?> {
        val docRef = db.collection("users").document(userId)
        return docRef.snapshots()
            .map { snap ->
                snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { err ->
                if (err is Exception) handleFirestoreError(err, OperationType.GET, docRef.path)
                throw err
            }
    }

    fun observeNotifications(userId: String): Flow<List<SmartNotification>> {
        val path = "users/$userId/notifications"
        return db.collection("users").document(userId).collection("notifications")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(SmartNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { err ->
                if (err is Exception) handleFirestoreError(err, OperationType.LIST, path)
                throw err
            }
    }

    fun observeVocabulary(userId: String): Flow<List<SavedVocabulary>> {
        val path = "users/$userId/vocabulary"
        return db.collection("users").document(userId).collection("vocabulary")
            .whereEqualTo("userId", userId)
            .snapshots()
            .map { snap ->
                snap.toObjects(SavedVocabulary::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }
            }
            .catch { err ->
                if (err is Exception) handleFirestoreError(err, OperationType.LIST, path)
                throw err
            }
    }

    fun observeLeaderboard(): Flow<List<LeaderboardEntry>> {
        val path = "leaderboard"
        return db.collection("leaderboard")
            .whereEqualTo("visibility", "public")
            .limit(200)
            .snapshots()
            .map { snap ->
                snap.toObjects(LeaderboardEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.xp }
            }
            .catch { err ->
                if (err is Exception) handleFirestoreError(err, OperationType.LIST, path)
                throw err
            }
    }

    fun observeDeveloperConfig(): Flow<DeveloperConfig?> {
        val docRef = db.collection("app_config").document("developer_profile")
        return docRef.snapshots()
            .map { snap ->
                if (snap.exists()) {
                    snap.toObject(DeveloperConfig::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { err ->
                if (err is Exception) handleFirestoreError(err, OperationType.GET, docRef.path)
                throw err
            }
    }

    fun observeCommunityPosts(): Flow<List<CommunityPost>> {
        val path = "community_posts"
        return db.collection("community_posts")
            .whereEqualTo("visibility", "active")
            .limit(40)
            .snapshots()
            .map { snap ->
                snap.toObjects(CommunityPost::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { err ->
                if (err is Exception) handleFirestoreError(err, OperationType.LIST, path)
                throw err
            }
    }

    fun observeOwnRegistrationEvent(userId: String): Flow<RegistrationEvent?> {
        val docRef = db.collection("registration_events").document(userId)
        return docRef.snapshots()
            .map { snap ->
                snap.toObject(RegistrationEvent::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { err ->
                if (err is Exception) handleFirestoreError(err, OperationType.GET, docRef.path)
                throw err
            }
    }

    suspend fun fetchAllRegistrationsForAdmin(): Result<List<RegistrationEvent>> {
        val path = "registration_events"
        return try {
            val snap = db.collection("registration_events").limit(50).get().await()
            val items = snap.toObjects(RegistrationEvent::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedByDescending { it.createdAt?.seconds ?: 0L }
            Result.success(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    fun observeCustomCourses(): Flow<List<CustomCourseLesson>> {
        val path = "custom_courses"
        return db.collection("custom_courses")
            .whereEqualTo("status", "published")
            .limit(30)
            .snapshots()
            .map { snap ->
                snap.toObjects(CustomCourseLesson::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { err ->
                if (err is Exception) handleFirestoreError(err, OperationType.LIST, path)
                throw err
            }
    }

    suspend fun createCustomCourseLesson(
        title: String,
        subjectOrLanguage: String,
        level: String,
        studentClass: String?,
        objective: String,
        questionPrompt: String,
        options: List<String>,
        correctAnswer: String,
        explanation: String,
        xpReward: Long = 30L
    ): Result<String> {
        val uid = requireUserId()
        val courseId = "course_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("custom_courses").document(courseId)
        val safeOptions = options.map { it.trim() }.filter { it.isNotBlank() }.take(4)
        if (safeOptions.size < 2) {
            return Result.failure(IllegalArgumentException("Provide at least 2 answer options."))
        }
        val safeCorrect = if (safeOptions.contains(correctAnswer.trim())) correctAnswer.trim() else safeOptions.first()
        val lesson = CustomCourseLesson(
            authorId = uid,
            title = title,
            subjectOrLanguage = subjectOrLanguage,
            level = level.takeIf { it in listOf("Beginner", "Intermediate", "Advanced") } ?: "Beginner",
            studentClass = studentClass,
            objective = objective,
            questionPrompt = questionPrompt,
            options = safeOptions,
            correctAnswer = safeCorrect,
            explanation = explanation,
            xpReward = xpReward.coerceIn(5L, 200L),
            status = "published"
        )
        return try {
            docRef.set(lesson.toCreateMap()).await()
            Result.success(courseId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun fetchReportedPostsForAdmin(): Result<List<CommunityPost>> {
        val path = "community_posts"
        return try {
            val snap = db.collection("community_posts")
                .whereEqualTo("visibility", "reported")
                .limit(30)
                .get()
                .await()
            val items = snap.toObjects(CommunityPost::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .sortedByDescending { it.updatedAt?.seconds ?: it.createdAt?.seconds ?: 0L }
            Result.success(items)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun moderateReportedPost(postId: String, restoreActive: Boolean): Result<Unit> {
        requireUserId()
        val docRef = db.collection("community_posts").document(postId)
        return try {
            if (restoreActive) {
                docRef.update(
                    mapOf(
                        "visibility" to "active",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.delete().await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, if (restoreActive) OperationType.UPDATE else OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 3. Profile, XP, Streak, Hearts, Gems & Mascot Mutations ---

    suspend fun getUserProfileById(targetUserId: String): Result<UserProfile?> {
        val docRef = db.collection("users").document(targetUserId)
        return try {
            val snap = docRef.get().await()
            Result.success(snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateProfileDetails(
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
    ): Result<Unit> {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        val leaderboardRef = db.collection("leaderboard").document(uid)
        val normalizedUsername = normalizeUsername(username)
        val safeClass = studentClass?.trim()?.take(40)?.takeIf { it.isNotBlank() }
        return try {
            val updates = mutableMapOf<String, Any>(
                "displayName" to displayName.trim().take(80).ifBlank { "Learner" },
                "bio" to bio.trim().take(300),
                "nativeLanguage" to nativeLanguage.take(40),
                "learningLanguage" to learningLanguage.take(40),
                "skillLevel" to skillLevel,
                "dailyGoalMinutes" to dailyGoalMinutes.coerceIn(5L, 120L),
                "relaxedMode" to relaxedMode,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (age != null) {
                updates["age"] = age.coerceIn(5L, 120L)
            }
            if (normalizedUsername != null) {
                updates["username"] = normalizedUsername
            }
            if (safeClass != null) {
                updates["studentClass"] = safeClass
            }
            if (leaderboardPrivacy != null) {
                updates["leaderboardPrivacy"] = leaderboardPrivacy
            }
            userRef.update(updates).await()

            val lbUpdates = mutableMapOf<String, Any>(
                "displayName" to displayName.trim().take(80).ifBlank { "Learner" },
                "learningLanguage" to learningLanguage.take(40),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (normalizedUsername != null) {
                lbUpdates["username"] = normalizedUsername
            }
            if (safeClass != null) {
                lbUpdates["studentClass"] = safeClass
            }
            if (leaderboardPrivacy != null) {
                lbUpdates["visibility"] = if (leaderboardPrivacy) "private" else "public"
            }
            leaderboardRef.update(lbUpdates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            Result.failure(e)
        }
    }

    suspend fun awardSubjectLabXp(
        currentProfile: UserProfile,
        subjectName: String,
        earnedXp: Int,
        earnedGems: Int
    ): Result<Unit> {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        val leaderboardRef = db.collection("leaderboard").document(uid)
        return try {
            val newXp = (currentProfile.xp + earnedXp).coerceAtMost(10_000_000L)
            val newGems = (currentProfile.gems + earnedGems).coerceAtMost(10_000_000L)
            val newLessons = currentProfile.lessonsCompleted + 1L
            val newLeague = LanguageCurriculum.leagueForXp(newXp)
            userRef.update(
                mapOf(
                    "xp" to newXp,
                    "gems" to newGems,
                    "lessonsCompleted" to newLessons,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            leaderboardRef.update(
                mapOf(
                    "xp" to newXp,
                    "league" to newLeague,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            createNotification(
                title = "🌟 $subjectName Lab Complete! +$earnedXp XP",
                body = "Awesome work in Noxra AI $subjectName! You earned +$earnedXp XP and +$earnedGems Gems.",
                category = "Achievements",
                iconEmoji = "🧠",
                deepLinkRoute = "learn"
            )
            recordAppHistory(
                category = "Lesson",
                title = "$subjectName Lab Completed",
                summary = "Earned +$earnedXp XP and +$earnedGems Gems in $subjectName Lab.",
                subjectOrMode = subjectName,
                xpEarned = earnedXp
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            Result.failure(e)
        }
    }

    suspend fun recordLessonCompleted(
        currentProfile: UserProfile,
        earnedXp: Int,
        earnedGems: Int,
        perfectScore: Boolean,
        nextLessonUnlockIndex: Int
    ): Result<Unit> {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        val leaderboardRef = db.collection("leaderboard").document(uid)
        return try {
            val bonusXp = if (perfectScore) 10 else 0
            val newXp = (currentProfile.xp + earnedXp + bonusXp).coerceAtMost(10_000_000L)
            val newGems = (currentProfile.gems + earnedGems).coerceAtMost(10_000_000L)
            val newLessons = currentProfile.lessonsCompleted + 1L
            val newUnlocked = maxOf(currentProfile.unlockedLessonsCount ?: 2L, nextLessonUnlockIndex.toLong())
            val newFluency = ((currentProfile.fluencyScore ?: 20L) + 3L).coerceIn(0L, 100L)
            val newMascotLvl = (1L + (newXp / 120L)).coerceIn(1L, 100L)
            val newLongestStreak = maxOf(currentProfile.longestStreak ?: 1L, currentProfile.streak).coerceIn(0L, 10000L)
            val newLeague = LanguageCurriculum.leagueForXp(newXp)

            userRef.update(
                mapOf(
                    "xp" to newXp,
                    "gems" to newGems,
                    "lessonsCompleted" to newLessons,
                    "unlockedLessonsCount" to newUnlocked,
                    "fluencyScore" to newFluency,
                    "mascotLevel" to newMascotLvl,
                    "longestStreak" to newLongestStreak,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            leaderboardRef.update(
                mapOf(
                    "xp" to newXp,
                    "league" to newLeague,
                    "fluencyScore" to newFluency,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            createNotification(
                title = if (perfectScore) "⭐ Perfect Lesson! +${earnedXp + bonusXp} XP" else "⭐ Lesson Complete! +$earnedXp XP",
                body = "You earned +$earnedGems Gems and boosted your ${currentProfile.learningLanguage} Fluency Score to $newFluency%!",
                category = "Achievements",
                iconEmoji = "🏆",
                deepLinkRoute = "home"
            )
            recordAppHistory(
                category = "Lesson",
                title = "${currentProfile.learningLanguage} Lesson Completed",
                summary = "Earned +${earnedXp + bonusXp} XP & +$earnedGems Gems • Fluency $newFluency%.",
                subjectOrMode = currentProfile.learningLanguage,
                xpEarned = earnedXp + bonusXp
            )
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            Result.failure(e)
        }
    }

    suspend fun adjustHearts(currentProfile: UserProfile, delta: Int): Result<Unit> {
        if (currentProfile.relaxedMode == true && delta < 0) return Result.success(Unit)
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        val newHearts = (currentProfile.hearts + delta).coerceIn(0L, 5L)
        return try {
            userRef.update(
                mapOf(
                    "hearts" to newHearts,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            Result.failure(e)
        }
    }

    suspend fun purchaseShopReward(
        currentProfile: UserProfile,
        costGems: Long,
        rewardType: String,
        outfitOrThemeValue: String = ""
    ): Result<String> {
        val uid = requireUserId()
        if (currentProfile.gems < costGems) {
            return Result.failure(IllegalArgumentException("Need $costGems Gems (you have ${currentProfile.gems}). Complete a lesson to earn more!"))
        }
        val userRef = db.collection("users").document(uid)
        val remainingGems = currentProfile.gems - costGems
        val updates = mutableMapOf<String, Any>(
            "gems" to remainingGems,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        val summary = when (rewardType) {
            "HEART_REFILL" -> {
                updates["hearts"] = 5L
                "Full 5 ❤️ Hearts restored!"
            }
            "STREAK_FREEZE" -> {
                updates["streakFreezeCount"] = ((currentProfile.streakFreezeCount ?: 0L) + 1L).coerceAtMost(20L)
                "🧊 Streak Freeze shield equipped!"
            }
            "MASCOT_TREAT" -> {
                updates["mascotLevel"] = ((currentProfile.mascotLevel ?: 1L) + 1L).coerceAtMost(100L)
                "🍓 Kavi loved the Berry Energy Treat and leveled up!"
            }
            "MASCOT_OUTFIT" -> {
                updates["mascotOutfit"] = outfitOrThemeValue.take(40)
                "🎩 Kavi equipped the '$outfitOrThemeValue' outfit!"
            }
            else -> "Reward unlocked!"
        }
        return try {
            userRef.update(updates).await()
            createNotification(
                title = "💎 Reward Unlocked!",
                body = summary,
                category = "Rewards",
                iconEmoji = "💎",
                deepLinkRoute = "compete"
            )
            Result.success(summary)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            Result.failure(e)
        }
    }

    suspend fun claimQuestReward(
        currentProfile: UserProfile,
        questId: String,
        questTitle: String,
        xpReward: Int,
        gemReward: Int
    ): Result<String> {
        val uid = requireUserId()
        val existingClaims = currentProfile.claimedQuestIds.orEmpty()
        if (existingClaims.contains(questId)) {
            return Result.failure(IllegalArgumentException("Quest '$questTitle' reward already claimed!"))
        }
        val userRef = db.collection("users").document(uid)
        val leaderboardRef = db.collection("leaderboard").document(uid)
        val updatedClaims = (existingClaims + questId.take(60)).distinct().takeLast(50)
        val newXp = (currentProfile.xp + xpReward).coerceAtMost(10_000_000L)
        val newGems = (currentProfile.gems + gemReward).coerceAtMost(10_000_000L)
        val newLeague = LanguageCurriculum.leagueForXp(newXp)
        return try {
            userRef.update(
                mapOf(
                    "xp" to newXp,
                    "gems" to newGems,
                    "claimedQuestIds" to updatedClaims,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            leaderboardRef.update(
                mapOf(
                    "xp" to newXp,
                    "league" to newLeague,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val msg = "🎯 Claimed '$questTitle': +$xpReward XP & +$gemReward Gems!"
            createNotification(
                title = "🎯 Daily Quest Claimed!",
                body = msg,
                category = "Rewards",
                iconEmoji = "🎯",
                deepLinkRoute = "compete"
            )
            Result.success(msg)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            Result.failure(e)
        }
    }

    // --- 4. Vocabulary & Smart Revision Notebook ---

    suspend fun saveVocabularyItem(
        word: String,
        translation: String,
        language: String,
        isMistake: Boolean,
        isFavorite: Boolean,
        phonetic: String? = null,
        exampleSentence: String? = null
    ): Result<String> {
        val uid = requireUserId()
        val safeDocId = word.lowercase(Locale.US)
            .replace(Regex("[^a-z0-9_\\-]"), "_")
            .take(64)
            .ifBlank { "vocab_${UUID.randomUUID().toString().take(12)}" }
        val docRef = db.collection("users").document(uid).collection("vocabulary").document(safeDocId)
        return try {
            val existing = docRef.get().await()
            if (existing.exists()) {
                val prevMastery = existing.getLong("masteryLevel") ?: 1L
                val nextMastery = if (isMistake) maxOf(0L, prevMastery - 1L) else minOf(5L, prevMastery + 1L)
                docRef.update(
                    mapOf(
                        "translation" to translation.take(150),
                        "masteryLevel" to nextMastery,
                        "isMistake" to isMistake,
                        "isFavorite" to (isFavorite || (existing.getBoolean("isFavorite") == true)),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val vocab = SavedVocabulary(
                    userId = uid,
                    word = word,
                    translation = translation,
                    language = language,
                    masteryLevel = if (isMistake) 1L else 2L,
                    isMistake = isMistake,
                    isFavorite = isFavorite,
                    phonetic = phonetic,
                    exampleSentence = exampleSentence
                )
                docRef.set(vocab.toCreateMap()).await()
            }
            Result.success(safeDocId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun promoteVocabularyMastery(item: SavedVocabulary): Result<Unit> {
        val uid = requireUserId()
        if (item.id.isBlank()) return Result.failure(IllegalArgumentException("Blank vocabulary ID"))
        val docRef = db.collection("users").document(uid).collection("vocabulary").document(item.id)
        val nextLevel = (item.masteryLevel + 1L).coerceIn(0L, 5L)
        return try {
            docRef.update(
                mapOf(
                    "masteryLevel" to nextLevel,
                    "isMistake" to (nextLevel < 3L && item.isMistake),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 5. Smart Notifications CRUD ---

    suspend fun createNotification(
        title: String,
        body: String,
        category: String,
        iconEmoji: String? = null,
        deepLinkRoute: String? = null
    ): Result<String> {
        val uid = requireUserId()
        val notifId = "notif_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("notifications").document(notifId)
        val notif = SmartNotification(
            userId = uid,
            title = title,
            body = body,
            category = category,
            isRead = false,
            iconEmoji = iconEmoji,
            deepLinkRoute = deepLinkRoute
        )
        return try {
            docRef.set(notif.toCreateMap()).await()
            Result.success(notifId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun markNotificationRead(notificationId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notifications").document(notificationId)
        return try {
            docRef.update(
                mapOf(
                    "isRead" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun markAllNotificationsRead(notifications: List<SmartNotification>): Result<Unit> {
        val uid = requireUserId()
        return try {
            notifications.filter { !it.isRead && it.id.isNotBlank() }.forEach { item ->
                db.collection("users").document(uid)
                    .collection("notifications").document(item.id)
                    .update(mapOf("isRead" to true, "updatedAt" to FieldValue.serverTimestamp()))
                    .await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/$uid/notifications")
            Result.failure(e)
        }
    }

    suspend fun deleteNotification(notificationId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("notifications").document(notificationId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 6. Community Feed, Encouragement Cheers & Safety Moderation ---

    suspend fun createCommunityPost(
        authorName: String,
        language: String,
        content: String,
        tag: String = "Study Group"
    ): Result<String> {
        val uid = requireUserId()
        val postId = "post_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("community_posts").document(postId)
        val post = CommunityPost(
            authorId = uid,
            authorName = authorName,
            language = language,
            content = content.trim(),
            cheersCount = 0L,
            visibility = "active",
            tag = tag
        )
        return try {
            docRef.set(post.toCreateMap()).await()
            Result.success(postId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun sendCheerToPost(post: CommunityPost): Result<Unit> {
        requireUserId()
        if (post.id.isBlank()) return Result.failure(IllegalArgumentException("Invalid post ID"))
        val docRef = db.collection("community_posts").document(post.id)
        return try {
            docRef.update(
                mapOf(
                    "cheersCount" to (post.cheersCount + 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun reportCommunityPost(postId: String): Result<Unit> {
        requireUserId()
        val docRef = db.collection("community_posts").document(postId)
        return try {
            docRef.update(
                mapOf(
                    "visibility" to "reported",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 7. Noxra AI 50-Level English & Math Mastery (Basic, Medium & Hard) ---

    suspend fun updateFiftyLevelSelection(
        isMath: Boolean,
        studentClass: String,
        tier: String
    ): Result<Unit> {
        val uid = requireUserId()
        val safeClass = studentClass.trim().take(40).ifBlank { "Class 8" }
        val safeTier = tier.takeIf { it in setOf("Basic", "Mid", "Advance") } ?: "Basic"
        val userRef = db.collection("users").document(uid)
        val updates = if (isMath) {
            mapOf(
                "mathClass" to safeClass,
                "mathTier" to safeTier,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        } else {
            mapOf(
                "englishClass" to safeClass,
                "englishTier" to safeTier,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        }
        return try {
            userRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            Result.failure(e)
        }
    }

    suspend fun completeFiftyLevelNode(
        currentProfile: UserProfile,
        isMath: Boolean,
        studentClass: String,
        tier: String,
        completedLevel: Int,
        earnedXp: Int,
        earnedGems: Int,
        scoreSummary: String
    ): Result<Int> {
        val uid = requireUserId()
        val safeLevel = completedLevel.coerceIn(1, 50)
        val safeClass = studentClass.trim().take(40).ifBlank { "Class 8" }
        val safeTier = tier.takeIf { it in setOf("Basic", "Mid", "Advance") } ?: "Basic"
        val xpMultiplier = if (currentProfile.subscriptionActive == true) 2 else 1
        val finalXpGain = (earnedXp * xpMultiplier).coerceIn(5, 5000)
        val newXp = (currentProfile.xp + finalXpGain).coerceAtMost(10_000_000L)
        val newGems = (currentProfile.gems + earnedGems).coerceAtMost(10_000_000L)
        val newLessons = currentProfile.lessonsCompleted + 1L
        val newFluency = ((currentProfile.fluencyScore ?: 20L) + 2L).coerceIn(0L, 100L)
        val newLeague = LanguageCurriculum.leagueForXp(newXp)

        val currentUnlocked = if (isMath) {
            (currentProfile.mathUnlockedLevel ?: 1L).toInt()
        } else {
            (currentProfile.englishUnlockedLevel ?: 1L).toInt()
        }
        val nextUnlocked = maxOf(currentUnlocked, (safeLevel + 1).coerceAtMost(50))

        val userRef = db.collection("users").document(uid)
        val leaderboardRef = db.collection("leaderboard").document(uid)
        return try {
            val updates = mutableMapOf<String, Any>(
                "xp" to newXp,
                "gems" to newGems,
                "lessonsCompleted" to newLessons,
                "fluencyScore" to newFluency,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (isMath) {
                updates["mathClass"] = safeClass
                updates["mathTier"] = safeTier
                updates["mathUnlockedLevel"] = nextUnlocked.toLong()
            } else {
                updates["englishClass"] = safeClass
                updates["englishTier"] = safeTier
                updates["englishUnlockedLevel"] = nextUnlocked.toLong()
            }
            userRef.update(updates).await()

            leaderboardRef.update(
                mapOf(
                    "xp" to newXp,
                    "league" to newLeague,
                    "fluencyScore" to newFluency,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val subjectLabel = if (isMath) "Math" else "English"
            val boostTag = if (xpMultiplier > 1) " (2x Naruto Event Boost!)" else ""
            createNotification(
                title = "🏆 $subjectLabel Level $safeLevel Complete! +$finalXpGain XP",
                body = "$safeClass • $safeTier • Level $nextUnlocked/50 ready!$boostTag",
                category = "Achievements",
                iconEmoji = if (isMath) "➗" else "🇬🇧",
                deepLinkRoute = "home"
            )
            recordAppHistory(
                category = if (isMath) "Math 1-50" else "English 1-50",
                title = "$subjectLabel Level $safeLevel Completed ($safeClass • $safeTier)",
                summary = "$scoreSummary • Earned +$finalXpGain XP & +$earnedGems Gems$boostTag. Unlocked up to Level $nextUnlocked/50.",
                subjectOrMode = "$safeClass • $safeTier",
                levelNumber = safeLevel,
                xpEarned = finalXpGain
            )
            Result.success(nextUnlocked)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            Result.failure(e)
        }
    }

    // --- 8. Subscription Mode ("Naruto Event — ₹49") & Editable Developer Profile ---

    suspend fun activateNarutoEventSubscription(currentProfile: UserProfile): Result<String> {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        val leaderboardRef = db.collection("leaderboard").document(uid)
        val newXp = (currentProfile.xp + 100L).coerceAtMost(10_000_000L)
        val newGems = (currentProfile.gems + 100L).coerceAtMost(10_000_000L)
        val newLeague = LanguageCurriculum.leagueForXp(newXp)
        return try {
            userRef.update(
                mapOf(
                    "subscriptionPlan" to "NARUTO_EVENT_49",
                    "subscriptionActive" to true,
                    "subscriptionPriceInr" to 49L,
                    "relaxedMode" to true,
                    "hearts" to 5L,
                    "mascotOutfit" to "Naruto Hokage Cloak",
                    "xp" to newXp,
                    "gems" to newGems,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            leaderboardRef.update(
                mapOf(
                    "xp" to newXp,
                    "league" to newLeague,
                    "avatarEmoji" to "🦊",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            val summary = "🔥 Naruto Event Pass (₹49) Activated! 2x Level XP, Unlimited Hearts & Hokage Badge unlocked!"
            createNotification(
                title = "🔥 Naruto Event (₹49) Active!",
                body = summary,
                category = "Rewards",
                iconEmoji = "🦊",
                deepLinkRoute = "home"
            )
            recordAppHistory(
                category = "Subscription",
                title = "Activated Naruto Event — ₹49 Pass",
                summary = "Unlocked 2x XP across English & Math Levels 1–50, Unlimited Hearts, Hokage Cloak, and +100 Bonus XP!",
                subjectOrMode = "Naruto Event ₹49",
                xpEarned = 100
            )
            Result.success(summary)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, userRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateDeveloperProfileOverrides(
        name: String,
        age: String,
        role: String,
        bio: String
    ): Result<Unit> {
        requireUserId()
        val configRef = db.collection("app_config").document("developer_profile")
        val config = DeveloperConfig(
            name = name,
            age = age,
            role = role,
            bio = bio,
            email = "ksachin29845@gmail.com"
        )
        return try {
            configRef.set(config.toWriteMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, configRef.path)
            Result.failure(e)
        }
    }

    // --- 9. Cloud App History (Saves All App History) ---

    fun observeAppHistory(userId: String): Flow<List<AppHistoryItem>> {
        val path = "users/$userId/app_history"
        return db.collection("users").document(userId).collection("app_history")
            .whereEqualTo("userId", userId)
            .limit(80)
            .snapshots()
            .map { snap ->
                snap.toObjects(AppHistoryItem::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.createdAt?.seconds ?: 0L }
            }
            .catch { err ->
                if (err is Exception) handleFirestoreError(err, OperationType.LIST, path)
                throw err
            }
    }

    suspend fun recordAppHistory(
        category: String,
        title: String,
        summary: String,
        subjectOrMode: String? = null,
        levelNumber: Int? = null,
        xpEarned: Int? = null
    ): Result<String> {
        val uid = requireUserId()
        val histId = "hist_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val docRef = db.collection("users").document(uid).collection("app_history").document(histId)
        val item = AppHistoryItem(
            userId = uid,
            category = category,
            title = title,
            summary = summary,
            subjectOrMode = subjectOrMode,
            levelNumber = levelNumber?.toLong(),
            xpEarned = xpEarned?.toLong()
        )
        return try {
            docRef.set(item.toCreateMap()).await()
            Result.success(histId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteAppHistoryItem(historyId: String): Result<Unit> {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("app_history").document(historyId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    // --- 10. Search @username Friends & Real-Time Direct Chat ---

    suspend fun searchFriendsByUsername(query: String): Result<List<LeaderboardEntry>> {
        val uid = requireUserId()
        val path = "leaderboard"
        val cleanQuery = query.trim().lowercase(Locale.US).removePrefix("@")
        return try {
            val snap = db.collection("leaderboard")
                .whereEqualTo("visibility", "public")
                .limit(200)
                .get()
                .await()
            val allPublic = snap.toObjects(LeaderboardEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                .filter { it.userId != uid }
            val filtered = if (cleanQuery.isBlank()) {
                allPublic.sortedByDescending { it.xp }
            } else {
                allPublic.filter { entry ->
                    val handle = entry.username.orEmpty().lowercase(Locale.US).removePrefix("@")
                    val name = entry.displayName.lowercase(Locale.US)
                    handle.contains(cleanQuery) || name.contains(cleanQuery)
                }.sortedByDescending { it.xp }
            }
            Result.success(filtered)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    fun observeDirectMessages(currentUserId: String): Flow<List<DirectChatMessage>> {
        val path = "direct_messages"
        return db.collection("direct_messages")
            .whereArrayContains("participants", currentUserId)
            .limit(100)
            .snapshots()
            .map { snap ->
                snap.toObjects(DirectChatMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
            }
            .catch { err ->
                if (err is Exception) handleFirestoreError(err, OperationType.LIST, path)
                throw err
            }
    }

    suspend fun sendDirectMessage(
        senderProfile: UserProfile,
        recipientId: String,
        recipientName: String,
        recipientUsername: String?,
        text: String
    ): Result<String> {
        val uid = requireUserId()
        val trimmedText = text.trim().take(1000)
        if (trimmedText.isBlank()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty."))
        }
        if (recipientId.isBlank() || recipientId == uid) {
            return Result.failure(IllegalArgumentException("Choose another student @username to chat with."))
        }
        val msgId = "dm_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val sortedPair = listOf(uid, recipientId).sorted()
        val conversationKey = "${sortedPair[0]}_${sortedPair[1]}"
        val docRef = db.collection("direct_messages").document(msgId)
        val message = DirectChatMessage(
            conversationKey = conversationKey,
            participants = listOf(uid, recipientId),
            senderId = uid,
            senderName = senderProfile.displayName.ifBlank { "Learner" },
            senderUsername = senderProfile.username,
            recipientId = recipientId,
            recipientName = recipientName.ifBlank { "Friend" },
            recipientUsername = recipientUsername,
            text = trimmedText
        )
        return try {
            docRef.set(message.toCreateMap()).await()
            val peerTag = recipientUsername ?: recipientName
            recordAppHistory(
                category = "Friend Chat",
                title = "Messaged $peerTag",
                summary = "Sent direct message to $peerTag: \"${trimmedText.take(120)}\"",
                subjectOrMode = peerTag
            )
            Result.success(msgId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteAccountData(): Result<Unit> {
        val uid = requireUserId()
        return try {
            db.collection("leaderboard").document(uid).delete().await()
            db.collection("registration_events").document(uid).delete().await()
            db.collection("users").document(uid).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/$uid")
            Result.failure(e)
        }
    }
}
