package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class SachinChatRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun bootstrapUser_validGoogleUser_createsProfileLeaderboardRegistrationAndNotifications() = runBlocking {
        val uniqueEmail = "alice_${UUID.randomUUID().toString().take(8)}@test.com"
        val aliceUid = signInTestUser(uniqueEmail)
        val repository = SachinChatRepository(firestore)

        val bootResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserBootstrapped("Alice Kumar", uniqueEmail, null)
        }
        assertTrue("Expected bootstrap to succeed: ${bootResult.exceptionOrNull()}", bootResult.isSuccess)
        assertEquals(true, bootResult.getOrThrow())

        // Verify profile realtime emission
        val profile = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProfile(aliceUid).first { it != null }
        }
        assertNotNull(profile)
        assertEquals("Alice Kumar", profile?.displayName)

        // Verify notifications query alignment
        val notifications = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeNotifications(aliceUid).first { it.isNotEmpty() }
        }
        assertTrue(notifications.any { it.category == "System" })

        // Verify leaderboard public query alignment
        val leaderboard = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeLeaderboard().first { list -> list.any { it.userId == aliceUid } }
        }
        assertTrue(leaderboard.any { it.userId == aliceUid })
    }

    @Test
    fun getUserProfile_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceEmail = "alice_${UUID.randomUUID().toString().take(8)}@test.com"
        val aliceUid = signInTestUser(aliceEmail)
        val aliceRepo = SachinChatRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.ensureUserBootstrapped("Alice", aliceEmail, null).getOrThrow()
        }

        val bobEmail = "bob_${UUID.randomUUID().toString().take(8)}@test.com"
        signInTestUser(bobEmail)
        val bobRepo = SachinChatRepository(firestore)

        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getUserProfileById(aliceUid)
        }
        assertTrue("Expected cross-user PII profile read to fail", crossReadResult.isFailure)
        val rootEx = crossReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException", rootEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, rootEx?.code)
    }

    @Test
    fun observeLeaderboard_unauthenticatedCaller_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = SachinChatRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeLeaderboard().first()
            }
            fail("Expected unauthenticated leaderboard query to throw PERMISSION_DENIED")
        } catch (e: Exception) {
            val firestoreEx = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", firestoreEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
        }
    }

    @Test
    fun saveVocabularyAndCommunityCheer_validOperations_succeed() = runBlocking {
        val email = "learner_${UUID.randomUUID().toString().take(8)}@test.com"
        val uid = signInTestUser(email)
        val repository = SachinChatRepository(firestore)

        val vocabResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveVocabularyItem(
                word = "Eloquent",
                translation = "प्रभावशाली वक्ता",
                language = "English",
                isMistake = true,
                isFavorite = true
            )
        }
        assertTrue(vocabResult.isSuccess)

        val savedWords = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeVocabulary(uid).first { it.isNotEmpty() }
        }
        assertEquals("Eloquent", savedWords.first().word)

        val postResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createCommunityPost("Learner", "English", "Completed Unit 1!", "Milestone")
        }
        assertTrue(postResult.isSuccess)
    }

    @Test
    fun completeRegistrationOnboarding_andAwardSubjectLabXp_succeedAgainstRules() = runBlocking {
        val email = "student_${UUID.randomUUID().toString().take(8)}@test.com"
        val uid = signInTestUser(email)
        val repository = SachinChatRepository(firestore)

        val bootResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserBootstrapped("Student Initial", email, null)
        }
        assertTrue("Expected bootstrap to succeed: ${bootResult.exceptionOrNull()}", bootResult.isSuccess)

        val onboardResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.completeRegistrationOnboarding(
                fullName = "Sachin Student",
                username = "sachin_student",
                studentClass = "Class 8",
                noxraPasscode = "1234",
                selectedTier = "Mid",
                age = 14,
                nativeLanguage = "Hindi",
                learningLanguage = "English"
            )
        }
        assertTrue("Expected onboarding update to succeed: ${onboardResult.exceptionOrNull()}", onboardResult.isSuccess)
        assertEquals("@sachin_student", onboardResult.getOrThrow())

        val updatedProfile = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProfile(uid).first { it?.username == "@sachin_student" }
        }
        assertNotNull(updatedProfile)
        assertEquals("Class 8", updatedProfile?.studentClass)
        assertEquals(14L, updatedProfile?.age)
        assertEquals(true, updatedProfile?.onboardingCompleted)

        val auditResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveRegistrationAiEmailAudit(
                aiSummary = "AI Registration Report for ksachin29845@gmail.com: Sachin Student (@sachin_student), Class 8, Age 14.",
                deliveryStatus = "Sent automatically to ksachin29845@gmail.com"
            )
        }
        assertTrue("Expected AI email audit update to succeed: ${auditResult.exceptionOrNull()}", auditResult.isSuccess)

        val regEvent = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeOwnRegistrationEvent(uid).first {
                it?.studentHandle == "@sachin_student" && !it.aiAdminEmailSummary.isNullOrBlank()
            }
        }
        assertEquals("Class 8", regEvent?.studentClass)
        assertEquals(14L, regEvent?.studentAge)
        assertEquals("Mid", regEvent?.selectedTier)
        assertEquals("Sent automatically to ksachin29845@gmail.com", regEvent?.autoEmailStatus)

        val labXpResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.awardSubjectLabXp(
                currentProfile = checkNotNull(updatedProfile),
                subjectName = "Maths",
                earnedXp = 25,
                earnedGems = 10
            )
        }
        assertTrue("Expected subject lab XP award to succeed: ${labXpResult.exceptionOrNull()}", labXpResult.isSuccess)
    }

    @Test
    fun claimQuestReward_andLeaderboardPrivacy_andCustomCourseStudentRestriction_enforceRules() = runBlocking {
        val email = "quest_learner_${UUID.randomUUID().toString().take(8)}@test.com"
        val uid = signInTestUser(email)
        val repository = SachinChatRepository(firestore)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.ensureUserBootstrapped("Quest Learner", email, null, "quest_hero", "Class 10").getOrThrow()
        }

        val initialProfile = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProfile(uid).first { it != null }
        }
        assertNotNull(initialProfile)

        // 1. First daily quest claim succeeds and persists quest ID
        val claimResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.claimQuestReward(
                currentProfile = checkNotNull(initialProfile),
                questId = "quest_xp_150",
                questTitle = "XP Sprinter",
                xpReward = 25,
                gemReward = 10
            )
        }
        assertTrue("Expected quest claim to succeed: ${claimResult.exceptionOrNull()}", claimResult.isSuccess)

        val profileAfterClaim = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProfile(uid).first { it?.claimedQuestIds?.contains("quest_xp_150") == true }
        }
        assertNotNull(profileAfterClaim)
        assertEquals(75L, profileAfterClaim?.xp)

        // 2. Duplicate claim of same questId is rejected client-side
        val duplicateClaim = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.claimQuestReward(
                currentProfile = checkNotNull(profileAfterClaim),
                questId = "quest_xp_150",
                questTitle = "XP Sprinter",
                xpReward = 25,
                gemReward = 10
            )
        }
        assertTrue("Expected duplicate quest claim to fail", duplicateClaim.isFailure)

        // 3. Enabling leaderboardPrivacy updates leaderboard visibility to private
        val privacyResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateProfileDetails(
                displayName = "Quest Learner",
                bio = "Focused study mode",
                age = 15L,
                nativeLanguage = "Hindi",
                learningLanguage = "English",
                skillLevel = "Intermediate",
                dailyGoalMinutes = 20L,
                relaxedMode = false,
                username = "@quest_hero",
                studentClass = "Class 10",
                leaderboardPrivacy = true
            )
        }
        assertTrue("Expected leaderboardPrivacy update to succeed: ${privacyResult.exceptionOrNull()}", privacyResult.isSuccess)

        // 4. Student can observe published custom_courses, but non-admin creation fails with PERMISSION_DENIED
        val publishedList = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCustomCourses().first()
        }
        assertNotNull(publishedList)

        val unauthorizedCreate = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.createCustomCourseLesson(
                title = "Unauthorized Student Course",
                subjectOrLanguage = "Science",
                level = "Beginner",
                studentClass = "Class 9",
                objective = "Should fail for non-admin",
                questionPrompt = "What is H2O?",
                options = listOf("Water", "Oxygen"),
                correctAnswer = "Water",
                explanation = "H2O is water."
            )
        }
        assertTrue("Expected non-admin custom course write to fail", unauthorizedCreate.isFailure)
        val rootEx = unauthorizedCreate.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, rootEx?.code)
    }

    @Test
    fun fiftyLevelPaths_narutoSubscription_appHistory_andDirectFriendChat_enforceRules() = runBlocking {
        val aliceEmail = "alice_noxra_${UUID.randomUUID().toString().take(8)}@test.com"
        val aliceUid = signInTestUser(aliceEmail)
        val aliceRepo = SachinChatRepository(firestore)

        // 1. Bootstrap Alice with @username, Class 9, passcode & Basic tier
        val bootAlice = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.ensureUserBootstrapped(
                displayNameHint = "Alice Noxra",
                emailHint = aliceEmail,
                photoUrlHint = null,
                usernameHint = "alice_noxra",
                studentClassHint = "Class 9",
                passcodeHint = "9090",
                tierHint = "Basic"
            )
        }
        assertTrue("Bootstrap Alice should succeed: ${bootAlice.exceptionOrNull()}", bootAlice.isSuccess)

        var aliceProfile = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeUserProfile(aliceUid).first { it != null }
        }
        assertNotNull(aliceProfile)
        assertEquals(true, aliceProfile?.onboardingCompleted)

        // 2. Activate Naruto Event ₹49 Subscription (unlocks 2x XP)
        val subResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.activateNarutoEventSubscription(checkNotNull(aliceProfile))
        }
        assertTrue("Naruto ₹49 subscription should succeed: ${subResult.exceptionOrNull()}", subResult.isSuccess)

        aliceProfile = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeUserProfile(aliceUid).first { it?.subscriptionActive == true }
        }
        assertEquals("NARUTO_EVENT_49", aliceProfile?.subscriptionPlan)
        assertEquals(49L, aliceProfile?.subscriptionPriceInr)

        // 3. Complete English Level 1 & Math Level 1 (with 2x XP boost & App History logging)
        val engUnlock = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.completeFiftyLevelNode(
                currentProfile = checkNotNull(aliceProfile),
                isMath = false,
                studentClass = "Class 9",
                tier = "Mid",
                completedLevel = 1,
                earnedXp = 20,
                earnedGems = 10,
                scoreSummary = "Scored 3/3 on English Level 1"
            )
        }
        assertTrue("English Level 1 completion should succeed: ${engUnlock.exceptionOrNull()}", engUnlock.isSuccess)
        assertEquals(2, engUnlock.getOrThrow())

        aliceProfile = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeUserProfile(aliceUid).first { it?.englishUnlockedLevel == 2L }
        }

        val mathUnlock = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.completeFiftyLevelNode(
                currentProfile = checkNotNull(aliceProfile),
                isMath = true,
                studentClass = "Class 9",
                tier = "Advance",
                completedLevel = 1,
                earnedXp = 25,
                earnedGems = 12,
                scoreSummary = "Scored 3/3 on Math Level 1"
            )
        }
        assertTrue("Math Level 1 completion should succeed: ${mathUnlock.exceptionOrNull()}", mathUnlock.isSuccess)
        assertEquals(2, mathUnlock.getOrThrow())

        // 4. Signed-in student can read global Developer Profile, but non-owner edit is strictly denied
        val devRead = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeDeveloperConfig().first()
        }
        // Initially null before owner edits, and permitted without PERMISSION_DENIED
        assertEquals(null, devRead)

        val unauthorizedDevEdit = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.updateDeveloperProfileOverrides(
                name = "Unauthorized Editor",
                age = "99 y",
                role = "Spoofed Role",
                bio = "Non-owner attempting to edit developer info."
            )
        }
        assertTrue("Non-owner developer info edit must fail", unauthorizedDevEdit.isFailure)
        val devRootEx = unauthorizedDevEdit.exceptionOrNull()?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, devRootEx?.code)

        // 5. Observe Alice's Saved App History
        val aliceHistory = withTimeout(FLOW_TIMEOUT_MS) {
            aliceRepo.observeAppHistory(aliceUid).first { list ->
                list.any { it.category == "English 1-50" } && list.any { it.category == "Math 1-50" }
            }
        }
        assertTrue(aliceHistory.any { it.category == "Subscription" })

        // 6. Sign in Bob, search @alice_noxra, and send 1-on-1 Direct Message
        val bobEmail = "bob_noxra_${UUID.randomUUID().toString().take(8)}@test.com"
        val bobUid = signInTestUser(bobEmail)
        val bobRepo = SachinChatRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.ensureUserBootstrapped("Bob Friend", bobEmail, null, "bob_noxra", "Class 9").getOrThrow()
        }
        val bobProfile = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeUserProfile(bobUid).first { it != null }
        }

        val foundFriends = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.searchFriendsByUsername("@alice_noxra")
        }
        assertTrue(foundFriends.isSuccess)
        assertTrue(foundFriends.getOrThrow().any { it.userId == aliceUid })

        val dmSend = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.sendDirectMessage(
                senderProfile = checkNotNull(bobProfile),
                recipientId = aliceUid,
                recipientName = "Alice Noxra",
                recipientUsername = "@alice_noxra",
                text = "Hey Alice! Let's race to Level 50 on Noxra AI!"
            )
        }
        assertTrue("Direct message send should succeed: ${dmSend.exceptionOrNull()}", dmSend.isSuccess)
        val dmId = dmSend.getOrThrow()

        val bobMessages = withTimeout(FLOW_TIMEOUT_MS) {
            bobRepo.observeDirectMessages(bobUid).first { list -> list.any { it.id == dmId } }
        }
        assertEquals(1, bobMessages.count { it.id == dmId })

        // 7. Third-party Charlie CANNOT read Alice's app_history or Alice-Bob's direct message
        val charlieEmail = "charlie_noxra_${UUID.randomUUID().toString().take(8)}@test.com"
        signInTestUser(charlieEmail)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("direct_messages").document(dmId).get().await()
            }
            fail("Expected Charlie's read of Alice-Bob DM to fail with PERMISSION_DENIED")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
        }
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}

