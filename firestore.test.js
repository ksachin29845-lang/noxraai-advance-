const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const CHARLIE_UID = "charlie_999";
const ADMIN_UID = "admin_789";
const OWNER_EMAIL = "ksachin29845@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function serverTimestamp() {
  return new Date(Date.now() - 1000);
}

function validUserPayload(uid) {
  const ts = serverTimestamp();
  return {
    userId: uid,
    displayName: "Alice Learner",
    email: "alice@example.com",
    nativeLanguage: "Hindi",
    learningLanguage: "English",
    skillLevel: "Beginner",
    xp: 50,
    streak: 1,
    hearts: 5,
    gems: 100,
    lessonsCompleted: 1,
    dailyGoalMinutes: 15,
    createdAt: ts,
    updatedAt: ts,
  };
}

test("1. Unauthenticated user cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("app_config").doc("developer_profile").get());
  await assertFails(unauthDb.collection("leaderboard").where("visibility", "==", "public").get());
  await assertFails(unauthDb.collection("direct_messages").where("participants", "array-contains", ALICE_UID).get());
  await assertFails(unauthDb.collection("community_posts").where("visibility", "==", "active").get());
});

test("2. Owner can create and read their own UserProfile, cross-user PII read is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("3. Shadow Update / Ghost Field injection is rejected on UserProfile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...validUserPayload(ALICE_UID),
      isVerifiedAdmin: true,
    })
  );
});

test("4. Email spoofing test: unverified admin email is rejected; verified ksachin29845@gmail.com succeeds", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = serverTimestamp();
  await assertSucceeds(
    aliceDb.collection("registration_events").doc(ALICE_UID).set({
      userId: ALICE_UID,
      userName: "Alice Learner",
      email: "alice@example.com",
      registrationDate: "2026-10-26",
      registrationTime: "10:00 AM",
      registrationMethod: "Google",
      createdAt: ts,
    })
  );

  const spoofAdminDb = testEnv.authenticatedContext("spoof_uid", {
    email: OWNER_EMAIL,
    email_verified: false,
  }).firestore();
  await assertFails(spoofAdminDb.collection("registration_events").doc(ALICE_UID).get());

  const verifiedAdminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: OWNER_EMAIL,
    email_verified: true,
  }).firestore();
  await assertSucceeds(verifiedAdminDb.collection("registration_events").doc(ALICE_UID).get());
});

test("5. Leaderboard query alignment: filtered public query succeeds, unfiltered query fails", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = serverTimestamp();
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      username: "@alice_k",
      learningLanguage: "English",
      xp: 250,
      streak: 4,
      league: "Silver",
      visibility: "public",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(bobDb.collection("leaderboard").where("visibility", "==", "public").get());
  await assertFails(bobDb.collection("leaderboard").get());
});

test("6. Smart notifications and vocabulary subcollections isolate per-user access", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = serverTimestamp();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("notifications").doc("notif_1").set({
      userId: ALICE_UID,
      title: "Streak Reminder",
      body: "Keep your daily learning streak going!",
      category: "Learning",
      isRead: false,
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("notifications")
      .where("userId", "==", ALICE_UID)
      .get()
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("notifications").doc("notif_1").get()
  );
});

test("7. Community posts enforce +1 cheer increment and terminal reported state lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = serverTimestamp();

  await assertSucceeds(
    aliceDb.collection("community_posts").doc("post_1").set({
      authorId: ALICE_UID,
      authorName: "Alice",
      language: "Spanish",
      content: "Completed Unit 1 today!",
      cheersCount: 0,
      visibility: "active",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertFails(
    bobDb.collection("community_posts").doc("post_1").update({
      cheersCount: 50,
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    bobDb.collection("community_posts").doc("post_1").update({
      cheersCount: 1,
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    bobDb.collection("community_posts").doc("post_1").update({
      visibility: "reported",
      updatedAt: serverTimestamp(),
    })
  );

  await assertFails(
    aliceDb.collection("community_posts").doc("post_1").update({
      content: "Trying to edit reported post",
      updatedAt: serverTimestamp(),
    })
  );
});

test("8. Student registration onboarding updates username & studentClass while protecting immutable registration fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const ts = serverTimestamp();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID)));
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Learner",
      learningLanguage: "English",
      xp: 50,
      streak: 1,
      league: "Bronze",
      visibility: "public",
      createdAt: ts,
      updatedAt: ts,
    })
  );
  await assertSucceeds(
    aliceDb.collection("registration_events").doc(ALICE_UID).set({
      userId: ALICE_UID,
      userName: "Alice Learner",
      email: "alice@example.com",
      registrationDate: "2026-10-26",
      registrationTime: "10:15 AM",
      registrationMethod: "Google",
      createdAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      displayName: "Alice Kumar",
      username: "@alice_k",
      studentClass: "Class 8",
      noxraPasscode: "4829",
      onboardingCompleted: true,
      updatedAt: serverTimestamp(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("registration_events").doc(ALICE_UID).update({
      userName: "Alice Kumar",
      studentHandle: "@alice_k",
      studentClass: "Class 8",
      studentAge: 13,
      selectedTier: "Mid",
      nativeLanguage: "Hindi",
      learningLanguage: "English",
      aiAdminEmailSummary: `AI-generated registration report for ${OWNER_EMAIL}`,
      autoEmailStatus: "Sent automatically via Noxra AI",
    })
  );

  await assertFails(
    aliceDb.collection("registration_events").doc(ALICE_UID).update({
      email: "hijack@example.com",
    })
  );
});

test("9. Quest claims, longestStreak, studentClass on leaderboard, and leaderboardPrivacy enforcement", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = serverTimestamp();

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID)));
  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      studentClass: "Class 9",
      learningLanguage: "English",
      xp: 150,
      streak: 3,
      league: "Silver",
      visibility: "public",
      createdAt: ts,
      updatedAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      xp: 175,
      gems: 110,
      longestStreak: 3,
      claimedQuestIds: ["quest_xp_150"],
      leaderboardPrivacy: true,
      updatedAt: serverTimestamp(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("leaderboard").doc(ALICE_UID).update({
      visibility: "private",
      updatedAt: serverTimestamp(),
    })
  );

  await assertFails(bobDb.collection("leaderboard").doc(ALICE_UID).get());
  await assertSucceeds(aliceDb.collection("leaderboard").doc(ALICE_UID).get());
});

test("10. Custom courses: strict verified-admin writes and student query-aligned published reads", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const verifiedAdminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: OWNER_EMAIL,
    email_verified: true,
  }).firestore();
  const ts = serverTimestamp();

  const coursePayload = {
    authorId: ADMIN_UID,
    title: "Quadratic Equations Quick Mastery",
    subjectOrLanguage: "Mathematics",
    level: "Intermediate",
    studentClass: "Class 10",
    objective: "Identify roots using the discriminant formula",
    questionPrompt: "If discriminant D = 0, how many real roots does ax² + bx + c = 0 have?",
    options: ["Two equal real roots", "No real roots", "Three roots"],
    correctAnswer: "Two equal real roots",
    explanation: "When b² - 4ac = 0, both roots equal -b / (2a).",
    xpReward: 30,
    status: "published",
    createdAt: ts,
    updatedAt: ts,
  };

  await assertFails(
    aliceDb.collection("custom_courses").doc("course_1").set({
      ...coursePayload,
      authorId: ALICE_UID,
    })
  );
  await assertSucceeds(verifiedAdminDb.collection("custom_courses").doc("course_1").set(coursePayload));
  await assertFails(unauthDb.collection("custom_courses").where("status", "==", "published").get());
  await assertSucceeds(aliceDb.collection("custom_courses").where("status", "==", "published").get());
  await assertFails(aliceDb.collection("custom_courses").get());
});

test("11. 50-Level English & Math progression, Naruto Event ₹49 subscription, and Owner-Only Developer Profile lock", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const verifiedAdminDb = testEnv.authenticatedContext(ADMIN_UID, {
    email: OWNER_EMAIL,
    email_verified: true,
  }).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      ...validUserPayload(ALICE_UID),
      username: "@alice_noxra",
      studentClass: "Class 8",
      noxraPasscode: "4829",
      onboardingCompleted: true,
      englishClass: "Class 8",
      englishTier: "Basic",
      englishUnlockedLevel: 1,
      mathClass: "Class 8",
      mathTier: "Basic",
      mathUnlockedLevel: 1,
      subscriptionPlan: "FREE",
      subscriptionActive: false,
      subscriptionPriceInr: 0,
      bio: "Learning on Noxra AI!",
      age: 14,
      photoUrl: "https://example.com/avatar.png",
      streakFreezeCount: 2,
      relaxedMode: false,
      themePreference: "Emerald",
      mascotName: "Kavi",
      mascotLevel: 2,
      mascotOutfit: "Ninja Headband",
      unlockedLessonsCount: 4,
      fluencyScore: 35,
      longestStreak: 5,
      claimedQuestIds: ["quest_1"],
      leaderboardPrivacy: false,
      registrationDate: "2026-10-28",
      registrationTime: "10:00 AM",
      registrationMethod: "Google",
    })
  );

  // Valid 50-level + Naruto Event ₹49 update succeeds for student
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      englishClass: "Class 8",
      englishTier: "Mid",
      englishUnlockedLevel: 14,
      mathClass: "Class 8",
      mathTier: "Advance",
      mathUnlockedLevel: 50,
      subscriptionPlan: "NARUTO_EVENT_49",
      subscriptionActive: true,
      subscriptionPriceInr: 49,
      updatedAt: serverTimestamp(),
    })
  );

  // Non-admin student CANNOT modify developerNameOverride or /app_config/developer_profile
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      developerNameOverride: "Spoofed Dev",
      updatedAt: serverTimestamp(),
    })
  );

  const devProfilePayload = {
    name: "Sachin Kumar",
    age: "12 years (12 y)",
    role: "CEO, Founder & Owner",
    bio: "Youngest CEO, Founder & Owner of Noxra AI.",
    email: OWNER_EMAIL,
    updatedAt: serverTimestamp(),
  };

  await assertFails(
    aliceDb.collection("app_config").doc("developer_profile").set(devProfilePayload)
  );

  // Signed-in learner CAN read /app_config/developer_profile even before and after owner writes it
  await assertSucceeds(
    aliceDb.collection("app_config").doc("developer_profile").get()
  );

  // Verified owner (ksachin29845@gmail.com) CAN create and update /app_config/developer_profile
  await assertSucceeds(
    verifiedAdminDb.collection("app_config").doc("developer_profile").set(devProfilePayload)
  );
  await assertSucceeds(
    verifiedAdminDb.collection("app_config").doc("developer_profile").update({
      bio: "Official Founder & CEO of Noxra AI — Building joyful education for all.",
      updatedAt: serverTimestamp(),
    })
  );
  await assertSucceeds(
    aliceDb.collection("app_config").doc("developer_profile").get()
  );

  // Out-of-bounds level > 50 MUST fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      englishUnlockedLevel: 51,
      updatedAt: serverTimestamp(),
    })
  );

  // Invalid tier enum MUST fail
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      mathTier: "UltraGodTier",
      updatedAt: serverTimestamp(),
    })
  );
});

test("12. Saved App History subcollection enforces owner isolation and append-only immutability", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const ts = serverTimestamp();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("app_history").doc("hist_1").set({
      userId: ALICE_UID,
      category: "English 1-50",
      title: "Completed English Level 3 (Class 8 • Basic)",
      summary: "Scored 3/3 with Noxra AI assistance!",
      subjectOrMode: "Class 8 • Basic",
      levelNumber: 3,
      xpEarned: 30,
      createdAt: ts,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("app_history").where("userId", "==", ALICE_UID).get()
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("app_history").doc("hist_1").get()
  );

  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("app_history").doc("hist_1").update({
      xpEarned: 5000,
    })
  );
});

test("13. Direct Friend Chat (@username DM) isolates reads/writes strictly to the 2 conversation participants", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const ts = serverTimestamp();

  const msgPayload = {
    conversationKey: `${ALICE_UID}_${BOB_UID}`,
    participants: [ALICE_UID, BOB_UID],
    senderId: ALICE_UID,
    senderName: "Alice",
    senderUsername: "@alice_k",
    recipientId: BOB_UID,
    recipientName: "Bob",
    recipientUsername: "@bob_m",
    text: "Hey Bob! Let's practice English Level 12 together on Noxra AI!",
    createdAt: ts,
  };

  await assertFails(
    bobDb.collection("direct_messages").doc("msg_spoof").set(msgPayload)
  );

  await assertSucceeds(
    aliceDb.collection("direct_messages").doc("msg_1").set(msgPayload)
  );

  await assertSucceeds(
    aliceDb.collection("direct_messages").where("participants", "array-contains", ALICE_UID).get()
  );
  await assertSucceeds(
    bobDb.collection("direct_messages").where("participants", "array-contains", BOB_UID).get()
  );

  await assertFails(
    charlieDb.collection("direct_messages").doc("msg_1").get()
  );

  await assertFails(aliceDb.collection("direct_messages").get());

  await assertFails(
    aliceDb.collection("direct_messages").doc("msg_1").update({
      text: "Edited secretly",
    })
  );
});
