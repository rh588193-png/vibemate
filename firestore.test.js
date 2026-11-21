const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_creator_1";
const BOB_UID = "bob_creator_2";
const FOUNDER_UID = "raja_founder_1";
const FOUNDER_EMAIL = "rh588193@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastDate(offsetMs = 5000) {
  return new Date(Date.now() - offsetMs);
}

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

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("posts").where("visibility", "==", "public").get());
  await assertFails(unauthDb.collection("users").where("discoverable", "==", true).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      handle: "alice",
      displayName: "Alice",
      followersCount: 0,
      followingCount: 0,
      postsCount: 0,
      verified: false,
      founderBadge: false,
      discoverable: true,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );
});

test("User profile: owner create & partial update succeed; privilege escalation & ghost fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();

  // Valid create (matches Kotlin UserProfileRepository payload, no @DocumentId `id`)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      handle: "alice_vibes",
      displayName: "Alice Creator",
      bio: "Visual storyteller on VIBEMATE",
      avatarPreset: "prism_violet",
      followersCount: 0,
      followingCount: 0,
      postsCount: 0,
      verified: false,
      founderBadge: false,
      discoverable: true,
      createdAt: pastDate(10000),
      updatedAt: pastDate(10000),
    })
  );

  // Valid partial update
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      bio: "Updated bio",
      updatedAt: pastDate(1000),
    })
  );

  // Reject self-verification privilege escalation
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      verified: true,
      updatedAt: pastDate(500),
    })
  );

  // Reject founderBadge self-grant on create
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      handle: "fake_raja",
      displayName: "Fake",
      followersCount: 0,
      followingCount: 0,
      postsCount: 0,
      verified: true,
      founderBadge: true,
      discoverable: true,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  // Discoverable query succeeds, unfiltered users query fails
  await assertSucceeds(aliceDb.collection("users").where("discoverable", "==", true).get());
  await assertFails(aliceDb.collection("users").get());
});

test("Founder Admin role: server-verified email succeeds; unverified spoof and non-founder fail", async () => {
  const founderDb = testEnv.authenticatedContext(FOUNDER_UID, {
    email: FOUNDER_EMAIL,
    email_verified: true,
  }).firestore();

  const spoofDb = testEnv.authenticatedContext("spoof_uid", {
    email: FOUNDER_EMAIL,
    email_verified: false,
  }).firestore();

  const regularDb = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();

  // Spoof without email_verified must fail
  await assertFails(
    spoofDb.collection("admins").doc("spoof_uid").set({
      userId: "spoof_uid",
      role: "founder",
      grantedAt: pastDate(),
    })
  );

  // Non-founder must fail
  await assertFails(
    regularDb.collection("admins").doc(ALICE_UID).set({
      userId: ALICE_UID,
      role: "founder",
      grantedAt: pastDate(),
    })
  );

  // Verified founder succeeds
  await assertSucceeds(
    founderDb.collection("admins").doc(FOUNDER_UID).set({
      userId: FOUNDER_UID,
      role: "founder",
      grantedAt: pastDate(),
    })
  );

  // Regular user reading own non-existent admin doc succeeds with exists=false (no PERMISSION_DENIED)
  await assertSucceeds(regularDb.collection("admins").doc(ALICE_UID).get());
  // Regular user cannot list admins
  await assertFails(regularDb.collection("admins").get());
});

test("Posts, Reels, Likes, Comments, Bookmarks, and Moderation Reports enforce parity and isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const founderDb = testEnv.authenticatedContext(FOUNDER_UID, {
    email: FOUNDER_EMAIL,
    email_verified: true,
  }).firestore();

  // Create public post (no `id` key)
  await assertSucceeds(
    aliceDb.collection("posts").doc("post_1").set({
      userId: ALICE_UID,
      authorHandle: "alice_vibes",
      authorName: "Alice Creator",
      authorAvatarPreset: "prism_violet",
      authorVerified: false,
      postType: "post",
      mediaType: "photo",
      mediaKey: "studio",
      caption: "Late night lighting test in the studio",
      hashtags: ["vibemate", "studio"],
      visibility: "public",
      likesCount: 0,
      commentsCount: 0,
      sharesCount: 0,
      createdAt: pastDate(10000),
      updatedAt: pastDate(10000),
    })
  );

  // Reject payload containing @DocumentId `id`
  await assertFails(
    aliceDb.collection("posts").doc("post_bad").set({
      id: "post_bad",
      userId: ALICE_UID,
      authorHandle: "alice_vibes",
      authorName: "Alice Creator",
      postType: "post",
      mediaType: "photo",
      caption: "Should fail",
      visibility: "public",
      likesCount: 0,
      commentsCount: 0,
      sharesCount: 0,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  // Filtered public feed query succeeds; unfiltered query fails
  await assertSucceeds(aliceDb.collection("posts").where("visibility", "==", "public").get());
  await assertFails(bobDb.collection("posts").get());

  // Bob can like post_1 (+1) and write his own like doc
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").collection("likes").doc(BOB_UID).set({
      userId: BOB_UID,
      createdAt: pastDate(),
    })
  );
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").update({
      likesCount: 1,
      updatedAt: pastDate(1000),
    })
  );

  // Bob cannot hijack Alice's caption
  await assertFails(
    bobDb.collection("posts").doc("post_1").update({
      caption: "Hijacked",
      updatedAt: pastDate(500),
    })
  );

  // Bob adds a comment
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").collection("comments").doc("c1").set({
      userId: BOB_UID,
      authorHandle: "bob_motion",
      authorName: "Bob",
      postVisibility: "public",
      text: "Unreal lighting!",
      createdAt: pastDate(),
    })
  );
  await assertSucceeds(
    bobDb.collection("posts").doc("post_1").collection("comments").where("postVisibility", "==", "public").get()
  );

  // Bob bookmarks post_1 under his private user subcollection; Alice cannot read Bob's bookmarks
  await assertSucceeds(
    bobDb.collection("users").doc(BOB_UID).collection("bookmarks").doc("post_1").set({
      userId: BOB_UID,
      postId: "post_1",
      createdAt: pastDate(),
    })
  );
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("bookmarks").get());

  // Bob reports post_1; Bob cannot resolve his own report; Founder resolves and moderates post_1
  await assertSucceeds(
    bobDb.collection("moderation_reports").doc("rep_1").set({
      reporterId: BOB_UID,
      targetPostId: "post_1",
      reason: "Test moderation queue workflow",
      status: "open",
      createdAt: pastDate(2000),
      updatedAt: pastDate(2000),
    })
  );
  await assertFails(
    bobDb.collection("moderation_reports").doc("rep_1").update({
      status: "resolved",
      updatedAt: pastDate(500),
    })
  );
  await assertFails(bobDb.collection("moderation_reports").get());
  await assertSucceeds(founderDb.collection("moderation_reports").get());
  await assertSucceeds(
    founderDb.collection("moderation_reports").doc("rep_1").update({
      status: "resolved",
      resolutionNote: "Reviewed by Founder",
      updatedAt: pastDate(500),
    })
  );
});
