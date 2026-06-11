/**
 * Server-side report verifier — mirror of the Android `ReportVerifier`
 * interface (domain/usecase/ReportVerification.kt).
 *
 * The default `HeuristicReportVerifier` replicates the app's
 * StubReportVerifier so behaviour is identical online and offline. To plug in
 * a real AI vision model later, implement `ReportVerifier` (download the
 * photo from Storage via the Admin SDK, send it to the vision API together
 * with the station context, map the answer to a verdict) and swap
 * `activeVerifier`. Store the API key as a Cloud Functions secret
 * (`firebase functions:secrets:set VISION_API_KEY`) — never in this repo.
 * Contract details: docs/BACKEND.md § AI photo verification.
 */
import { distanceMeters, GeoProximity, REPORTABLE_STATUSES } from "./policies";

export type Verdict = "PENDING" | "VERIFIED" | "REJECTED";

export interface VerificationRequest {
  stationLatitude: number;
  stationLongitude: number;
  userLatitude: number;
  userLongitude: number;
  claimedStatus: string;
  /** Storage path of the uploaded photo, e.g. reports/{uid}/1700000000.jpg */
  photoPath: string | null;
}

export interface VerificationResult {
  verdict: Verdict;
  reason: string;
  confidence: number;
}

export interface ReportVerifier {
  verify(request: VerificationRequest): Promise<VerificationResult>;
}

/** Deterministic heuristics: photo attached + inside geofence + valid status. */
export class HeuristicReportVerifier implements ReportVerifier {
  async verify(request: VerificationRequest): Promise<VerificationResult> {
    if (!request.photoPath) {
      return {
        verdict: "REJECTED",
        reason: "لا توجد صورة مرفقة بالتقرير",
        confidence: 1,
      };
    }
    if (!REPORTABLE_STATUSES.has(request.claimedStatus)) {
      return {
        verdict: "REJECTED",
        reason: "حالة الازدحام المحددة غير صالحة",
        confidence: 1,
      };
    }
    const distance = distanceMeters(
      request.userLatitude,
      request.userLongitude,
      request.stationLatitude,
      request.stationLongitude
    );
    if (distance > GeoProximity.DEFAULT_RADIUS_METERS) {
      return {
        verdict: "REJECTED",
        reason: `موقعك (${Math.round(distance)}م) خارج نطاق المحطة`,
        confidence: 1,
      };
    }
    return {
      verdict: "VERIFIED",
      reason: "صورة مرفقة وموقع مطابق للمحطة",
      confidence: 0.8,
    };
  }
}

/** The verifier used by onReportCreated. Swap for an AI implementation later. */
export const activeVerifier: ReportVerifier = new HeuristicReportVerifier();
