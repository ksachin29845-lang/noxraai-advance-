package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Learner",
    val username: String? = null,
    val studentClass: String? = null,
    val noxraPasscode: String? = null,
    val onboardingCompleted: Boolean? = null,
    val email: String = "",
    val nativeLanguage: String = "Hindi",
    val learningLanguage: String = "English",
    val skillLevel: String = "Beginner",
    val englishClass: String? = "Class 8",
    val englishTier: String? = "Basic",
    val englishUnlockedLevel: Long? = 1L,
    val mathClass: String? = "Class 8",
    val mathTier: String? = "Basic",
    val mathUnlockedLevel: Long? = 1L,
    val subscriptionPlan: String? = "FREE",
    val subscriptionActive: Boolean? = false,
    val subscriptionPriceInr: Long? = 0L,
    val developerNameOverride: String? = null,
    val developerAgeOverride: String? = null,
    val developerRoleOverride: String? = null,
    val developerBioOverride: String? = null,
    val xp: Long = 0L,
    val streak: Long = 1L,
    val longestStreak: Long? = 1L,
    val hearts: Long = 5L,
    val gems: Long = 120L,
    val lessonsCompleted: Long = 0L,
    val dailyGoalMinutes: Long = 15L,
    val bio: String? = "Learning English & Math on Noxra AI!",
    val age: Long? = null,
    val photoUrl: String? = null,
    val streakFreezeCount: Long? = 1L,
    val relaxedMode: Boolean? = false,
    val themePreference: String? = "Emerald",
    val mascotName: String? = "Kavi",
    val mascotLevel: Long? = 1L,
    val mascotOutfit: String? = "Explorer Scarf",
    val unlockedLessonsCount: Long? = 2L,
    val fluencyScore: Long? = 18L,
    val claimedQuestIds: List<String>? = emptyList(),
    val leaderboardPrivacy: Boolean? = false,
    val registrationDate: String? = null,
    val registrationTime: String? = null,
    val registrationMethod: String? = "Google",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val validTiers = setOf("Basic", "Mid", "Advance")
        return mapOf<String, Any?>(
            "userId" to userId,
            "displayName" to displayName.take(80).ifBlank { "Learner" },
            "username" to username?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "studentClass" to studentClass?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "noxraPasscode" to noxraPasscode?.trim()?.take(32)?.takeIf { it.isNotBlank() },
            "onboardingCompleted" to onboardingCompleted,
            "email" to email.take(150).ifBlank { "learner@noxra.ai" },
            "nativeLanguage" to nativeLanguage.take(40),
            "learningLanguage" to learningLanguage.take(40),
            "skillLevel" to skillLevel,
            "englishClass" to englishClass?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "englishTier" to englishTier?.takeIf { it in validTiers },
            "englishUnlockedLevel" to englishUnlockedLevel?.coerceIn(1L, 50L),
            "mathClass" to mathClass?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "mathTier" to mathTier?.takeIf { it in validTiers },
            "mathUnlockedLevel" to mathUnlockedLevel?.coerceIn(1L, 50L),
            "subscriptionPlan" to subscriptionPlan?.take(40),
            "subscriptionActive" to subscriptionActive,
            "subscriptionPriceInr" to subscriptionPriceInr?.coerceIn(0L, 100_000L),
            "xp" to xp,
            "streak" to streak,
            "longestStreak" to (longestStreak ?: streak).coerceIn(0L, 10000L),
            "hearts" to hearts.coerceIn(0L, 5L),
            "gems" to gems.coerceAtLeast(0L),
            "lessonsCompleted" to lessonsCompleted.coerceAtLeast(0L),
            "dailyGoalMinutes" to dailyGoalMinutes.coerceIn(5L, 120L),
            "bio" to bio?.take(300),
            "age" to age?.coerceIn(5L, 120L),
            "photoUrl" to photoUrl?.take(500),
            "streakFreezeCount" to streakFreezeCount?.coerceIn(0L, 20L),
            "relaxedMode" to relaxedMode,
            "themePreference" to themePreference?.take(30),
            "mascotName" to mascotName?.take(40),
            "mascotLevel" to mascotLevel?.coerceIn(1L, 100L),
            "mascotOutfit" to mascotOutfit?.take(40),
            "unlockedLessonsCount" to unlockedLessonsCount?.coerceIn(1L, 500L),
            "fluencyScore" to fluencyScore?.coerceIn(0L, 100L),
            "claimedQuestIds" to (claimedQuestIds?.take(50) ?: emptyList<String>()),
            "leaderboardPrivacy" to (leaderboardPrivacy == true),
            "registrationDate" to registrationDate?.take(40),
            "registrationTime" to registrationTime?.take(40),
            "registrationMethod" to registrationMethod?.take(30),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }
}

@IgnoreExtraProperties
data class DeveloperConfig(
    @DocumentId val id: String = "",
    val name: String = "Sachin Kumar",
    val age: String = "12 years (12 y)",
    val role: String = "CEO, Founder & Owner",
    val bio: String = "At age 12, Sachin Kumar founded Noxra AI as CEO, Founder & Owner to make world-class 50-level English & Math mastery, simple AI tutoring, and global ranked competitions joyful and accessible for every learner.",
    val email: String = "ksachin29845@gmail.com",
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toWriteMap(): Map<String, Any> {
        return mapOf(
            "name" to name.trim().take(80).ifBlank { "Sachin Kumar" },
            "age" to age.trim().take(30).ifBlank { "12 years (12 y)" },
            "role" to role.trim().take(100).ifBlank { "CEO, Founder & Owner" },
            "bio" to bio.trim().take(400).ifBlank { "CEO, Founder & Owner of Noxra AI." },
            "email" to email.trim().take(150).ifBlank { "ksachin29845@gmail.com" },
            "updatedAt" to FieldValue.serverTimestamp()
        )
    }
}

@IgnoreExtraProperties
data class SmartNotification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val body: String = "",
    val category: String = "Learning", // Learning, Achievements, Friends, AI Tutor, Rewards, System
    val isRead: Boolean = false,
    val iconEmoji: String? = null,
    val deepLinkRoute: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        return mapOf<String, Any?>(
            "userId" to userId,
            "title" to title.take(120).ifBlank { "Learning Update" },
            "body" to body.take(500).ifBlank { "Keep your momentum going!" },
            "category" to category,
            "isRead" to isRead,
            "iconEmoji" to iconEmoji?.take(16),
            "deepLinkRoute" to deepLinkRoute?.take(60),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }
}

@IgnoreExtraProperties
data class SavedVocabulary(
    @DocumentId val id: String = "",
    val userId: String = "",
    val word: String = "",
    val translation: String = "",
    val language: String = "English",
    val masteryLevel: Long = 1L,
    val isMistake: Boolean = false,
    val isFavorite: Boolean = true,
    val phonetic: String? = null,
    val exampleSentence: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        return mapOf<String, Any?>(
            "userId" to userId,
            "word" to word.take(100),
            "translation" to translation.take(150),
            "language" to language.take(40),
            "masteryLevel" to masteryLevel.coerceIn(0L, 5L),
            "isMistake" to isMistake,
            "isFavorite" to isFavorite,
            "phonetic" to phonetic?.take(100),
            "exampleSentence" to exampleSentence?.take(300),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }
}

@IgnoreExtraProperties
data class AppHistoryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val category: String = "System", // Noxra AI, English 1-50, Math 1-50, Subscription, Friend Chat, System, Lesson
    val title: String = "",
    val summary: String = "",
    val subjectOrMode: String? = null,
    val levelNumber: Long? = null,
    val xpEarned: Long? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val allowedCategories = setOf(
            "Noxra AI", "English 1-50", "Math 1-50", "Subscription", "Friend Chat", "System", "Lesson"
        )
        return mapOf<String, Any?>(
            "userId" to userId,
            "category" to (category.takeIf { it in allowedCategories } ?: "System"),
            "title" to title.trim().take(140).ifBlank { "Noxra AI Activity" },
            "summary" to summary.trim().take(1200).ifBlank { "Saved to your Noxra AI cloud history." },
            "subjectOrMode" to subjectOrMode?.trim()?.take(60)?.takeIf { it.isNotBlank() },
            "levelNumber" to levelNumber?.coerceIn(1L, 50L),
            "xpEarned" to xpEarned?.coerceIn(0L, 5000L),
            "createdAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }
}

@IgnoreExtraProperties
data class DirectChatMessage(
    @DocumentId val id: String = "",
    val conversationKey: String = "",
    val participants: List<String> = emptyList(),
    val senderId: String = "",
    val senderName: String = "Learner",
    val senderUsername: String? = null,
    val recipientId: String = "",
    val recipientName: String? = null,
    val recipientUsername: String? = null,
    val text: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        return mapOf<String, Any?>(
            "conversationKey" to conversationKey.take(260),
            "participants" to participants.take(2),
            "senderId" to senderId,
            "senderName" to senderName.trim().take(80).ifBlank { "Learner" },
            "senderUsername" to senderUsername?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "recipientId" to recipientId,
            "recipientName" to recipientName?.trim()?.take(80)?.takeIf { it.isNotBlank() },
            "recipientUsername" to recipientUsername?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "text" to text.trim().take(1000).ifBlank { "👋" },
            "createdAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }
}

@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Learner",
    val username: String? = null,
    val studentClass: String? = null,
    val learningLanguage: String = "English",
    val xp: Long = 0L,
    val streak: Long = 1L,
    val league: String = "Bronze",
    val visibility: String = "public",
    val avatarEmoji: String? = "🦉",
    val fluencyScore: Long? = 18L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        return mapOf<String, Any?>(
            "userId" to userId,
            "displayName" to displayName.take(80).ifBlank { "Learner" },
            "username" to username?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "studentClass" to studentClass?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "learningLanguage" to learningLanguage.take(40),
            "xp" to xp.coerceAtLeast(0L),
            "streak" to streak.coerceAtLeast(0L),
            "league" to league,
            "visibility" to visibility,
            "avatarEmoji" to avatarEmoji?.take(16),
            "fluencyScore" to fluencyScore?.coerceIn(0L, 100L),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }
}

@IgnoreExtraProperties
data class CommunityPost(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "Learner",
    val language: String = "English",
    val content: String = "",
    val cheersCount: Long = 0L,
    val visibility: String = "active",
    val tag: String? = "Milestone",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        return mapOf<String, Any?>(
            "authorId" to authorId,
            "authorName" to authorName.take(80).ifBlank { "Learner" },
            "language" to language.take(40),
            "content" to content.take(400),
            "cheersCount" to 0L,
            "visibility" to "active",
            "tag" to tag?.take(40),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }
}

@IgnoreExtraProperties
data class RegistrationEvent(
    @DocumentId val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val studentHandle: String? = null,
    val studentClass: String? = null,
    val studentAge: Long? = null,
    val selectedTier: String? = null,
    val nativeLanguage: String? = null,
    val learningLanguage: String? = null,
    val email: String = "",
    val registrationDate: String = "",
    val registrationTime: String = "",
    val registrationMethod: String = "Google",
    val aiAdminEmailSummary: String? = null,
    val autoEmailStatus: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        val validTiers = setOf("Basic", "Mid", "Advance")
        return mapOf<String, Any?>(
            "userId" to userId,
            "userName" to userName.take(100).ifBlank { "New Learner" },
            "studentHandle" to studentHandle?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "studentClass" to studentClass?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "studentAge" to studentAge?.coerceIn(5L, 120L),
            "selectedTier" to selectedTier?.takeIf { it in validTiers },
            "nativeLanguage" to nativeLanguage?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "learningLanguage" to learningLanguage?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "email" to email.take(150).ifBlank { "learner@noxra.ai" },
            "registrationDate" to registrationDate.take(40),
            "registrationTime" to registrationTime.take(40),
            "registrationMethod" to "Google",
            "aiAdminEmailSummary" to aiAdminEmailSummary?.trim()?.take(2400)?.takeIf { it.isNotBlank() },
            "autoEmailStatus" to autoEmailStatus?.trim()?.take(120)?.takeIf { it.isNotBlank() },
            "createdAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }
}

@IgnoreExtraProperties
data class CustomCourseLesson(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val title: String = "",
    val subjectOrLanguage: String = "English",
    val level: String = "Beginner",
    val studentClass: String? = null,
    val objective: String = "",
    val questionPrompt: String = "",
    val options: List<String> = emptyList(),
    val correctAnswer: String = "",
    val explanation: String = "",
    val xpReward: Long = 25L,
    val status: String = "published",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> {
        return mapOf<String, Any?>(
            "authorId" to authorId,
            "title" to title.trim().take(100).ifBlank { "Custom Lesson" },
            "subjectOrLanguage" to subjectOrLanguage.trim().take(40).ifBlank { "English" },
            "level" to level,
            "studentClass" to studentClass?.trim()?.take(40)?.takeIf { it.isNotBlank() },
            "objective" to objective.trim().take(200).ifBlank { "Master core concepts" },
            "questionPrompt" to questionPrompt.trim().take(300),
            "options" to options.take(4).map { it.trim().take(120) },
            "correctAnswer" to correctAnswer.trim().take(120),
            "explanation" to explanation.trim().take(300).ifBlank { "Great job!" },
            "xpReward" to xpReward.coerceIn(5L, 200L),
            "status" to status,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        ).filterValues { it != null }.mapValues { it.value!! }
    }
}
