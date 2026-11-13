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

function validAdditionPayload(uid) {
  const now = new Date();
  return {
    userId: uid,
    firstNumber: 12.5,
    secondNumber: 30.5,
    total: 43.0,
    note: "Lunch split",
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated user: cannot list or create additions", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("additions").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("additions").doc("add_1").set(validAdditionPayload(ALICE_UID))
  );
});

test("Authenticated owner: can create, list, get, update, and delete own addition", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("additions").doc("add_1");

  await assertSucceeds(docRef.set(validAdditionPayload(ALICE_UID)));
  await assertSucceeds(docRef.get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("additions").get());
  await assertSucceeds(
    docRef.update({
      firstNumber: 20,
      secondNumber: 22,
      total: 42,
      note: "Updated note",
      updatedAt: new Date(),
    })
  );
  await assertSucceeds(docRef.delete());
});

test("Cross-user isolation: Bob cannot read, create, update, or delete Alice's additions", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("users")
      .doc(ALICE_UID)
      .collection("additions")
      .doc("alice_add_1")
      .set(validAdditionPayload(ALICE_UID));
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const aliceDocRef = bobDb.collection("users").doc(ALICE_UID).collection("additions").doc("alice_add_1");

  await assertFails(aliceDocRef.get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("additions").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("additions").doc("bob_inject").set(validAdditionPayload(BOB_UID))
  );
  await assertFails(aliceDocRef.update({ note: "Hacked", updatedAt: new Date() }));
  await assertFails(aliceDocRef.delete());
});

test("Schema validation: rejects ghost fields, spoofed userId, invalid types, and oversized note", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const colRef = aliceDb.collection("users").doc(ALICE_UID).collection("additions");

  // Ghost field
  await assertFails(
    colRef.doc("ghost_1").set({ ...validAdditionPayload(ALICE_UID), isAdmin: true })
  );

  // Spoofed userId
  await assertFails(
    colRef.doc("spoof_1").set(validAdditionPayload(BOB_UID))
  );

  // Invalid type for number
  await assertFails(
    colRef.doc("badtype_1").set({ ...validAdditionPayload(ALICE_UID), firstNumber: "12" })
  );

  // Oversized note (> 80 chars)
  await assertFails(
    colRef.doc("longnote_1").set({ ...validAdditionPayload(ALICE_UID), note: "x".repeat(81) })
  );

  // Invalid document ID
  await assertFails(
    colRef.doc("bad id!").set(validAdditionPayload(ALICE_UID))
  );
});

test("Update protection: rejects immutable createdAt/userId changes and ghost fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("additions").doc("add_immutable");
  await assertSucceeds(docRef.set(validAdditionPayload(ALICE_UID)));

  // Attempt to mutate createdAt
  await assertFails(
    docRef.update({ createdAt: new Date(0), updatedAt: new Date() })
  );

  // Attempt to mutate userId
  await assertFails(
    docRef.update({ userId: BOB_UID, updatedAt: new Date() })
  );

  // Attempt to inject ghost field on update
  await assertFails(
    docRef.update({ extra: "bad", updatedAt: new Date() })
  );
});
