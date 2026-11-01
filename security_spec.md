# Security Specification — Noxra AI Firestore Rules

## 1. Data Invariants
1. **Strict Authentication**: Unauthenticated callers (`request.auth == null`) cannot read or write any document across any collection.
2. **PII Split-Collection Isolation**: User PII (`email`, `age`, `noxraPasscode`) lives only in `/users/{userId}` and `/registration_events/{userId}` and is strictly isolated to the owning `userId` (`request.auth.uid == userId`) or verified admins (`isAdmin()`). Public `/leaderboard/{userId}` entries contain zero PII.
3. **Immutable Ownership & Temporal Fields**: `userId`, `authorId`, `senderId`, and `createdAt` can never be mutated on `update`. All timestamps must be Firestore `Timestamp` values `<= request.time`.
4. **Query Enforcer Alignment**: Every `allow list` rule evaluates `resource.data` (`userId == request.auth.uid`, `request.auth.uid in resource.data.participants`, `visibility == 'public'`, `visibility == 'active'`, or `status == 'published'`). Unfiltered collection scrapes are rejected.
5. **Verified Owner/Admin Gate & Locked Developer Info**: Admin checks require either an explicit document in `/admins/{uid}` (immutable to clients) or `request.auth.token.email == 'ksachin29845@gmail.com' && request.auth.token.email_verified == true`. Only verified admins can create or update `/app_config/developer_profile` or modify developer override fields.
6. **Terminal State Locking & Append-Only Logs**: Community posts marked `visibility == 'reported'` are locked against further non-admin updates. Direct messages (`/direct_messages/{messageId}`) and per-user history entries (`/users/{userId}/app_history/{historyId}`) forbid arbitrary updates (`allow update: if false`).
7. **Direct Friend Chat Isolation**: `/direct_messages/{messageId}` strictly restricts reads to the two authenticated UIDs in `participants` (`size() == 2`), and restricts creation to `senderId == request.auth.uid && request.auth.uid in participants`.

## 2. The Dirty Dozen Adversarial Payloads
1. **Unauthenticated Scrape**: `GET /users/alice_123`, `GET /app_config/developer_profile`, or `GET /direct_messages` without auth -> Denied.
2. **Cross-User PII Read**: Bob (`bob_456`) reads `/users/alice_123` or `/users/alice_123/app_history/h1` -> Denied.
3. **Unfiltered List Scrape**: Alice queries `/leaderboard` without `where('visibility', '==', 'public')` or `/direct_messages` without `where('participants', 'array-contains', uid)` -> Denied.
4. **Identity Spoofing on Create**: Alice creates `/direct_messages/m1` with `{ senderId: 'bob_456' }` -> Denied.
5. **Shadow / Ghost Field Injection**: Alice creates `/users/alice_123` with extra `{ isAdmin: true }` -> Denied by `keys().hasOnly(...)`.
6. **Email Spoofing Attack**: Attacker authenticates with `{ email: 'ksachin29845@gmail.com', email_verified: false }` and reads `/registration_events/alice_123` or writes `/app_config/developer_profile` -> Denied.
7. **Unauthorized Developer Info Tampering**: Non-admin Alice attempts to write `/app_config/developer_profile` or update `developerNameOverride` on `/users/alice_123` -> Denied.
8. **Value Poisoning on Update**: Alice updates `englishUnlockedLevel` on `/users/alice_123` with `999` (> 50) -> Denied by `isValidUserProfile`.
9. **String Overflow / Denial of Wallet**: Alice posts 5,000-char string to `/direct_messages` -> Denied by `.size() <= 1000`.
10. **Immortal Field Tampering**: Alice updates `createdAt` or `userId` on `/users/alice_123` -> Denied.
11. **Third-Party Chat Eavesdropping**: Charlie (`charlie_999`) attempts to read Alice-Bob message `/direct_messages/m1` -> Denied (`!(charlie_999 in participants)`).
12. **Terminal State Bypass**: Author attempts to edit a post already in `visibility == 'reported'` state -> Denied.
