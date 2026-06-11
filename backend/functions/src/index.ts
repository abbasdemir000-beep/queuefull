/**
 * QueueFuel Cloud Functions — the authoritative side of the trust model.
 *
 * - onReportCreated: anti-spam checks → verification → transactional
 *   station update + points + raffle entry + trust adjustment → FCM fan-out.
 * - confirmStatus (callable): geofenced "I'm here and confirm" with
 *   server-side points.
 * - setUserRole (callable): ADMIN-only role management. Bootstrap the first
 *   admin by setting users/{uid}.role = "ADMIN" once in the Firebase console.
 *
 * See docs/BACKEND.md for the data model and deployment runbook.
 */
import { initializeApp } from "firebase-admin/app";
import {
  DocumentReference,
  FieldValue,
  getFirestore,
  Timestamp,
} from "firebase-admin/firestore";
import { getMessaging } from "firebase-admin/messaging";
import { logger } from "firebase-functions";
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { HttpsError, onCall } from "firebase-functions/v2/https";
import {
  adjustTrust,
  AntiSpamPolicy,
  GeoProximity,
  PointsPolicy,
  TrustPolicy,
  distanceMeters,
} from "./policies";
import { activeVerifier } from "./verifier";

initializeApp();
const db = getFirestore();

const STATUS_LABELS_AR: Record<string, string> = {
  EMPTY: "فارغة 🟢",
  SHORT: "قصيرة 🟢",
  MODERATE: "معتدلة 🟡",
  LONG: "طويلة 🔴",
  CLOSED: "مغلقة ⚫",
};

interface ReportDoc {
  uid: string;
  userPhone?: string;
  stationId: number;
  queueStatus: string;
  hasFuel: boolean;
  fuelType: string;
  latitude: number;
  longitude: number;
  photoPath: string | null;
  photoSha256?: string;
  createdAt: Timestamp;
  verification: string;
}

function currentMonthYear(): string {
  return new Date().toISOString().slice(0, 7); // "yyyy-MM"
}

/**
 * Applies the consequences of a VERIFIED report in one transaction:
 * verdict on the report, station status, reporter points + trust,
 * raffle entry, and the duplicate-photo ledger entry.
 */
async function applyVerifiedReport(
  reportRef: DocumentReference,
  reportId: string,
  report: ReportDoc,
  stationRef: DocumentReference,
  reason: string,
  confidence: number
): Promise<void> {
  const userRef = db.doc(`users/${report.uid}`);
  await db.runTransaction(async (tx) => {
    const userSnap = await tx.get(userRef);
    const trust =
      (userSnap.get("trustScore") as number | undefined) ??
      TrustPolicy.INITIAL_SCORE;

    tx.update(reportRef, {
      verification: "VERIFIED",
      verificationNote: reason,
      confidence,
      verifiedAt: FieldValue.serverTimestamp(),
    });
    tx.update(stationRef, {
      queueStatus: report.queueStatus,
      hasFuel: report.hasFuel,
      confirmedCount: 1,
      lastUpdated: Date.now(),
    });
    tx.set(
      userRef,
      {
        points: FieldValue.increment(PointsPolicy.REPORT_POINTS),
        lifetimePoints: FieldValue.increment(PointsPolicy.REPORT_POINTS),
        trustScore: adjustTrust(trust, TrustPolicy.VERIFIED_REPORT_DELTA),
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
    tx.create(db.collection("rewardEntries").doc(), {
      uid: report.uid,
      stationId: report.stationId,
      reportId,
      monthYear: currentMonthYear(),
      verified: true,
      createdAt: FieldValue.serverTimestamp(),
    });
    if (report.photoSha256) {
      tx.set(db.doc(`photoHashes/${report.photoSha256}`), {
        uid: report.uid,
        reportId,
        createdAt: FieldValue.serverTimestamp(),
      });
    }
  });
}

/** Best-effort FCM fan-out to the station's city topic. */
async function notifyCityTopic(
  cityId: unknown,
  stationName: unknown,
  stationId: number,
  queueStatus: string
): Promise<void> {
  if (cityId === undefined || cityId === null) return;
  const label = STATUS_LABELS_AR[queueStatus] ?? queueStatus;
  try {
    await getMessaging().send({
      topic: `city_${cityId}`,
      notification: {
        title: "تحديث حالة محطة ⛽",
        body: `محطة ${stationName ?? ""} أصبحت حالتها الآن: ${label}`,
      },
      data: {
        type: "station_update",
        stationId: String(stationId),
        queueStatus,
      },
    });
  } catch (e) {
    logger.warn("FCM topic send failed", { cityId, stationId, error: `${e}` });
  }
}

export const onReportCreated = onDocumentCreated(
  "reports/{reportId}",
  async (event) => {
    const snap = event.data;
    if (!snap) return;
    const reportId = event.params.reportId;
    const report = snap.data() as ReportDoc;
    const reportRef = snap.ref;
    const userRef = db.doc(`users/${report.uid}`);

    const reject = (reason: string, extra: Record<string, unknown> = {}) =>
      reportRef.update({
        verification: "REJECTED",
        verificationNote: reason,
        verifiedAt: FieldValue.serverTimestamp(),
        ...extra,
      });

    // 1. Account checks.
    const userSnap = await userRef.get();
    if (!userSnap.exists) {
      await reject("حساب غير معروف");
      return;
    }
    if (userSnap.get("banned") === true) {
      await reject("الحساب محظور");
      return;
    }
    const trust =
      (userSnap.get("trustScore") as number | undefined) ??
      TrustPolicy.INITIAL_SCORE;

    // 2. Cooldowns + daily budget (server-authoritative; the client enforces
    // the same AntiSpamPolicy values only for fast feedback).
    const nowMs = Date.now();
    const since = Timestamp.fromMillis(nowMs - 24 * 60 * 60 * 1000);
    const recent = await db
      .collection("reports")
      .where("uid", "==", report.uid)
      .where("createdAt", ">=", since)
      .orderBy("createdAt", "desc")
      .limit(AntiSpamPolicy.MAX_REPORTS_PER_DAY + 1)
      .get();
    const previous = recent.docs.filter((d) => d.id !== reportId);

    if (previous.length >= AntiSpamPolicy.MAX_REPORTS_PER_DAY) {
      await Promise.all([
        reject("تجاوزت الحد اليومي للتقارير"),
        userRef.set({ flagged: true }, { merge: true }),
      ]);
      return;
    }
    const lastAnyAt = (previous[0]?.get("createdAt") as Timestamp | undefined)
      ?.toMillis();
    const lastStationAt = (
      previous
        .find((d) => d.get("stationId") === report.stationId)
        ?.get("createdAt") as Timestamp | undefined
    )?.toMillis();
    if (
      lastAnyAt !== undefined &&
      nowMs - lastAnyAt < AntiSpamPolicy.GLOBAL_COOLDOWN_MS
    ) {
      await reject("الرجاء الانتظار قبل إرسال تقرير جديد (تهدئة عامة)");
      return;
    }
    if (
      lastStationAt !== undefined &&
      nowMs - lastStationAt < AntiSpamPolicy.STATION_COOLDOWN_MS
    ) {
      await reject("لقد أرسلت تقريراً عن هذه المحطة مؤخراً — حاول لاحقاً");
      return;
    }

    // 3. Duplicate photo (cross-device, durable ledger).
    if (report.photoSha256) {
      const hashSnap = await db.doc(`photoHashes/${report.photoSha256}`).get();
      if (hashSnap.exists) {
        const newTrust = adjustTrust(trust, TrustPolicy.DUPLICATE_PHOTO_DELTA);
        await Promise.all([
          reject("الصورة مكررة من تقرير سابق"),
          userRef.set(
            {
              trustScore: newTrust,
              flagged: newTrust < TrustPolicy.BAN_REVIEW_THRESHOLD,
            },
            { merge: true }
          ),
        ]);
        return;
      }
    }

    // 4. Station must exist.
    const stationRef = db.doc(`stations/${report.stationId}`);
    const stationSnap = await stationRef.get();
    if (!stationSnap.exists) {
      await reject("المحطة غير موجودة");
      return;
    }

    // 5. Verification (heuristics today, AI verifier later — see verifier.ts).
    const result = await activeVerifier.verify({
      stationLatitude: stationSnap.get("latitude") as number,
      stationLongitude: stationSnap.get("longitude") as number,
      userLatitude: report.latitude,
      userLongitude: report.longitude,
      claimedStatus: report.queueStatus,
      photoPath: report.photoPath,
    });

    if (result.verdict !== "VERIFIED") {
      const newTrust = adjustTrust(trust, TrustPolicy.REJECTED_REPORT_DELTA);
      await Promise.all([
        reject(result.reason, { confidence: result.confidence }),
        userRef.set(
          {
            trustScore: newTrust,
            flagged: newTrust < TrustPolicy.BAN_REVIEW_THRESHOLD,
          },
          { merge: true }
        ),
      ]);
      return;
    }

    // 6. Low-trust accounts lose auto-verification: hold for admin review.
    if (trust < TrustPolicy.SUSPICIOUS_THRESHOLD) {
      await reportRef.update({
        verification: "PENDING",
        verificationNote: "بانتظار مراجعة الأدمن (حساب قيد المتابعة)",
        confidence: result.confidence,
      });
      return;
    }

    // 7. Apply all consequences atomically, then notify the city.
    await applyVerifiedReport(
      reportRef,
      reportId,
      report,
      stationRef,
      result.reason,
      result.confidence
    );
    await notifyCityTopic(
      stationSnap.get("cityId"),
      stationSnap.get("name"),
      report.stationId,
      report.queueStatus
    );
  }
);

/**
 * Geofenced one-tap confirmation: +5 points, station confirmedCount + 1.
 * data: { stationId: number, latitude: number, longitude: number }
 */
export const confirmStatus = onCall(async (request) => {
  const uid = request.auth?.uid;
  if (!uid) {
    throw new HttpsError("unauthenticated", "Sign-in required");
  }
  const stationId = Number(request.data?.stationId);
  const latitude = Number(request.data?.latitude);
  const longitude = Number(request.data?.longitude);
  if (
    !Number.isFinite(stationId) ||
    !Number.isFinite(latitude) ||
    !Number.isFinite(longitude)
  ) {
    throw new HttpsError(
      "invalid-argument",
      "stationId, latitude and longitude are required"
    );
  }

  const userRef = db.doc(`users/${uid}`);
  const stationRef = db.doc(`stations/${stationId}`);

  await db.runTransaction(async (tx) => {
    const [userSnap, stationSnap] = await Promise.all([
      tx.get(userRef),
      tx.get(stationRef),
    ]);
    if (!userSnap.exists || userSnap.get("banned") === true) {
      throw new HttpsError("permission-denied", "Account unavailable");
    }
    if (!stationSnap.exists) {
      throw new HttpsError("not-found", "Unknown station");
    }
    const distance = distanceMeters(
      latitude,
      longitude,
      stationSnap.get("latitude") as number,
      stationSnap.get("longitude") as number
    );
    if (distance > GeoProximity.DEFAULT_RADIUS_METERS) {
      throw new HttpsError(
        "failed-precondition",
        "Outside the station geofence"
      );
    }
    const trust =
      (userSnap.get("trustScore") as number | undefined) ??
      TrustPolicy.INITIAL_SCORE;
    tx.update(stationRef, {
      confirmedCount: FieldValue.increment(1),
      lastUpdated: Date.now(),
    });
    tx.set(
      userRef,
      {
        points: FieldValue.increment(PointsPolicy.CONFIRMATION_POINTS),
        lifetimePoints: FieldValue.increment(PointsPolicy.CONFIRMATION_POINTS),
        trustScore: adjustTrust(trust, TrustPolicy.CONFIRMED_BY_OTHERS_DELTA),
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  });

  return { ok: true, points: PointsPolicy.CONFIRMATION_POINTS };
});

const VALID_ROLES = new Set(["USER", "REPORTER", "ADMIN"]);

/**
 * ADMIN-only role management. data: { targetUid: string, role: string }.
 * Bootstrap: there is intentionally no code path that creates the first
 * admin — Abbas sets users/{hisUid}.role = "ADMIN" once in the Firebase
 * console, after which this callable manages everyone else.
 */
export const setUserRole = onCall(async (request) => {
  const callerUid = request.auth?.uid;
  if (!callerUid) {
    throw new HttpsError("unauthenticated", "Sign-in required");
  }
  const callerSnap = await db.doc(`users/${callerUid}`).get();
  if (callerSnap.get("role") !== "ADMIN") {
    throw new HttpsError("permission-denied", "Admins only");
  }
  const targetUid = String(request.data?.targetUid ?? "");
  const role = String(request.data?.role ?? "");
  if (!targetUid || !VALID_ROLES.has(role)) {
    throw new HttpsError(
      "invalid-argument",
      "targetUid and a valid role (USER/REPORTER/ADMIN) are required"
    );
  }
  await db
    .doc(`users/${targetUid}`)
    .set({ role, updatedAt: FieldValue.serverTimestamp() }, { merge: true });
  logger.info("Role updated", { by: callerUid, targetUid, role });
  return { ok: true };
});

/**
 * ADMIN-only manual review of a PENDING report.
 * data: { reportId: string, approve: boolean }
 */
export const reviewReport = onCall(async (request) => {
  const callerUid = request.auth?.uid;
  if (!callerUid) {
    throw new HttpsError("unauthenticated", "Sign-in required");
  }
  const callerSnap = await db.doc(`users/${callerUid}`).get();
  if (callerSnap.get("role") !== "ADMIN") {
    throw new HttpsError("permission-denied", "Admins only");
  }
  const reportId = String(request.data?.reportId ?? "");
  const approve = request.data?.approve === true;
  if (!reportId) {
    throw new HttpsError("invalid-argument", "reportId is required");
  }

  const reportRef = db.doc(`reports/${reportId}`);
  const reportSnap = await reportRef.get();
  if (!reportSnap.exists) {
    throw new HttpsError("not-found", "Unknown report");
  }
  if (reportSnap.get("verification") !== "PENDING") {
    throw new HttpsError("failed-precondition", "Report already reviewed");
  }
  const report = reportSnap.data() as ReportDoc;

  if (!approve) {
    await reportRef.update({
      verification: "REJECTED",
      verificationNote: "رفض يدوي من الأدمن",
      verifiedAt: FieldValue.serverTimestamp(),
    });
    return { ok: true, verdict: "REJECTED" };
  }

  const stationRef = db.doc(`stations/${report.stationId}`);
  const stationSnap = await stationRef.get();
  if (!stationSnap.exists) {
    throw new HttpsError("failed-precondition", "Station no longer exists");
  }
  await applyVerifiedReport(
    reportRef,
    reportId,
    report,
    stationRef,
    "اعتماد يدوي من الأدمن",
    1
  );
  await notifyCityTopic(
    stationSnap.get("cityId"),
    stationSnap.get("name"),
    report.stationId,
    report.queueStatus
  );
  return { ok: true, verdict: "VERIFIED" };
});
