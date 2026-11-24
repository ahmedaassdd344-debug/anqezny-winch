const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const CUSTOMER_UID = "customer_uid_1";
const DRIVER_UID = "driver_uid_1";
const OTHER_UID = "other_uid_2";
const ADMIN_EMAIL = "ahmedaassdd344@gmail.com";

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

function pastDate(msOffset = 2000) {
  return new Date(Date.now() - msOffset);
}

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("trips").get());
  await assertFails(unauthDb.collection("drivers").get());
  await assertFails(
    unauthDb.collection("users").doc(CUSTOMER_UID).set({
      userId: CUSTOMER_UID,
      displayName: "Hacker",
      role: "customer",
      acceptedTerms: true,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );
});

test("User profile: cannot self-assign admin role or inject ghost fields", async () => {
  const custDb = testEnv.authenticatedContext(CUSTOMER_UID).firestore();
  await assertFails(
    custDb.collection("users").doc(CUSTOMER_UID).set({
      userId: CUSTOMER_UID,
      displayName: "Fake Admin",
      role: "admin",
      acceptedTerms: true,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );
  await assertFails(
    custDb.collection("users").doc(CUSTOMER_UID).set({
      userId: CUSTOMER_UID,
      displayName: "Ghost",
      role: "customer",
      acceptedTerms: true,
      isSuperUser: true,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );
  await assertSucceeds(
    custDb.collection("users").doc(CUSTOMER_UID).set({
      userId: CUSTOMER_UID,
      displayName: "أحمد محمود",
      role: "customer",
      phone: "01012345678",
      acceptedTerms: true,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );
});

test("Driver registration & PII isolation: driver cannot self-approve; customer cannot read private docs", async () => {
  const driverDb = testEnv.authenticatedContext(DRIVER_UID).firestore();
  const custDb = testEnv.authenticatedContext(CUSTOMER_UID).firestore();

  // Cannot self-approve on create
  await assertFails(
    driverDb.collection("drivers").doc(DRIVER_UID).set({
      userId: DRIVER_UID,
      name: "كريم حسن",
      winchType: "ونش هيدروليك مسطح",
      plateNumber: "ج ط 1234",
      area: "المهندسين",
      status: "approved",
      online: false,
      walletBalance: 500,
      withdrawableBalance: 0,
      totalTrips: 0,
      ratingSum: 0,
      ratingCount: 0,
      ratingAvg: 0,
      docsSubmitted: true,
      acceptedTerms: true,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  // Valid pending driver creation
  await assertSucceeds(
    driverDb.collection("drivers").doc(DRIVER_UID).set({
      userId: DRIVER_UID,
      name: "كريم حسن",
      winchType: "ونش هيدروليك مسطح",
      plateNumber: "ج ط 1234",
      area: "المهندسين",
      status: "pending",
      online: false,
      walletBalance: 0,
      withdrawableBalance: 0,
      totalTrips: 0,
      ratingSum: 0,
      ratingCount: 0,
      ratingAvg: 0,
      docsSubmitted: true,
      acceptedTerms: true,
      createdAt: pastDate(),
      updatedAt: pastDate(),
    })
  );

  // Driver private docs
  await assertSucceeds(
    driverDb.collection("drivers").doc(DRIVER_UID).collection("private").doc("documents").set({
      userId: DRIVER_UID,
      phone: "01099887766",
      nationalId: "29501012101234",
      licenseNumber: "GIZA-9921",
      licenseExpiry: "2028-10",
      updatedAt: pastDate(),
    })
  );

  // Customer MUST NOT read driver's private PII docs
  await assertFails(
    custDb.collection("drivers").doc(DRIVER_UID).collection("private").doc("documents").get()
  );
});

test("Bootstrapped Admin email verification guard: unverified email spoof fails; verified admin succeeds", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("drivers").doc(DRIVER_UID).set({
      userId: DRIVER_UID,
      name: "كريم حسن",
      winchType: "ونش مسطح",
      plateNumber: "ج ط 1234",
      area: "الدقي",
      status: "pending",
      online: false,
      walletBalance: 0,
      withdrawableBalance: 0,
      totalTrips: 0,
      ratingSum: 0,
      ratingCount: 0,
      ratingAvg: 0,
      docsSubmitted: true,
      acceptedTerms: true,
      createdAt: pastDate(5000),
      updatedAt: pastDate(5000),
    });
  });

  // Spoofed email without email_verified == true
  const spoofAdminDb = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();
  await assertFails(
    spoofAdminDb.collection("drivers").doc(DRIVER_UID).update({
      status: "approved",
      updatedAt: pastDate(),
    })
  );

  // Real verified admin
  const realAdminDb = testEnv.authenticatedContext("admin_uid", {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  await assertSucceeds(
    realAdminDb.collection("drivers").doc(DRIVER_UID).update({
      status: "approved",
      walletBalance: 300,
      updatedAt: pastDate(),
    })
  );
});

test("Trip lifecycle & Detailed Driver Rating + Review enforcement", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("drivers").doc(DRIVER_UID).set({
      userId: DRIVER_UID,
      name: "سعيد المنشاوي",
      winchType: "ونش VIP",
      plateNumber: "ج ي 5588",
      area: "الشيخ زايد",
      status: "approved",
      online: true,
      walletBalance: 300,
      withdrawableBalance: 0,
      totalTrips: 0,
      ratingSum: 0,
      ratingCount: 0,
      ratingAvg: 0,
      docsSubmitted: true,
      acceptedTerms: true,
      createdAt: pastDate(10000),
      updatedAt: pastDate(10000),
    });
  });

  const custDb = testEnv.authenticatedContext(CUSTOMER_UID).firestore();
  const driverDb = testEnv.authenticatedContext(DRIVER_UID).firestore();
  const otherDb = testEnv.authenticatedContext(OTHER_UID).firestore();

  // Customer creates pending trip (without "id" field)
  await assertSucceeds(
    custDb.collection("trips").doc("trip_101").set({
      customerId: CUSTOMER_UID,
      customerName: "طارق نبيل",
      customerPhone: "01122334455",
      pickupArea: "المهندسين",
      pickupLat: 30.0561,
      pickupLng: 31.2001,
      dropoffArea: "الشيخ زايد",
      dropoffLat: 30.0218,
      dropoffLng: 30.9854,
      distanceKm: 22.5,
      vehicleType: "سيارة فاخرة VIP",
      serviceType: "إنقاذ فوري",
      paymentMethod: "كاش",
      gatewayLive: false,
      fareEgp: 720,
      commissionEgp: 108,
      driverNetEgp: 612,
      pin: "4821",
      status: "pending",
      photosCount: 0,
      pinVerified: false,
      signaturePointsCount: 0,
      signedByCustomer: false,
      cancelFeeEgp: 0,
      rated: false,
      createdAt: pastDate(5000),
      updatedAt: pastDate(5000),
    })
  );

  // Driver accepts trip
  await assertSucceeds(
    driverDb.collection("trips").doc("trip_101").update({
      driverId: DRIVER_UID,
      driverName: "سعيد المنشاوي",
      driverPlate: "ج ي 5588",
      status: "accepted",
      updatedAt: pastDate(4000),
    })
  );

  // Random other user cannot modify accepted trip
  await assertFails(
    otherDb.collection("trips").doc("trip_101").update({
      status: "cancelled",
      cancelFeeEgp: 0,
      updatedAt: pastDate(3500),
    })
  );

  // Driver arrives, loads, completes
  await assertSucceeds(
    driverDb.collection("trips").doc("trip_101").update({
      status: "arrived",
      waitingStartedAt: pastDate(3000),
      updatedAt: pastDate(3000),
    })
  );
  await assertSucceeds(
    driverDb.collection("trips").doc("trip_101").update({
      status: "loaded",
      photosCount: 4,
      pinVerified: true,
      updatedAt: pastDate(2000),
    })
  );
  await assertSucceeds(
    driverDb.collection("trips").doc("trip_101").update({
      status: "completed",
      signaturePointsCount: 18,
      signedByCustomer: true,
      updatedAt: pastDate(1500),
    })
  );

  // Terminal state lock: driver cannot reopen completed trip
  await assertFails(
    driverDb.collection("trips").doc("trip_101").update({
      status: "accepted",
      updatedAt: pastDate(1000),
    })
  );

  // Customer submits review and updates driver aggregate rating
  await assertSucceeds(
    custDb.collection("trips").doc("trip_101").update({
      rating: 5,
      reviewComment: "سائق محترم وسريع جداً في التحميل",
      rated: true,
      updatedAt: pastDate(1000),
    })
  );
  await assertSucceeds(
    custDb.collection("reviews").doc("rev_trip_101").set({
      tripId: "trip_101",
      driverId: DRIVER_UID,
      customerId: CUSTOMER_UID,
      customerName: "طارق نبيل",
      stars: 5,
      comment: "سائق محترم وسريع جداً في التحميل",
      tags: ["ممتاز في المواعيد", "أمان في التحميل"],
      createdAt: pastDate(1000),
    })
  );
  await assertSucceeds(
    custDb.collection("drivers").doc(DRIVER_UID).update({
      ratingSum: 5,
      ratingCount: 1,
      ratingAvg: 5,
      updatedAt: pastDate(1000),
    })
  );

  // Customer cannot re-rate same trip or tamper with driver wallet
  await assertFails(
    custDb.collection("trips").doc("trip_101").update({
      rating: 1,
      rated: true,
      updatedAt: pastDate(500),
    })
  );
  await assertFails(
    custDb.collection("drivers").doc(DRIVER_UID).update({
      walletBalance: 99999,
      updatedAt: pastDate(500),
    })
  );

  // Client query alignment tests
  await assertSucceeds(custDb.collection("trips").where("customerId", "==", CUSTOMER_UID).get());
  await assertSucceeds(driverDb.collection("trips").where("status", "==", "pending").get());
  await assertSucceeds(custDb.collection("reviews").where("driverId", "==", DRIVER_UID).get());
  await assertFails(custDb.collection("trips").get()); // unfiltered query fails!
});
