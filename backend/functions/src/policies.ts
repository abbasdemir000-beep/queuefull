/**
 * Server-side mirrors of the pure policies in
 * app/src/main/java/com/example/domain/usecase/.
 *
 * THE VALUES HERE ARE AUTHORITATIVE — the Android client enforces the same
 * rules only for fast local feedback. When changing a constant, change it in
 * both places.
 */

export const PointsPolicy = {
  REPORT_POINTS: 15,
  CONFIRMATION_POINTS: 5,
  STATION_SUGGESTION_POINTS: 30,
  STATION_APPROVAL_POINTS: 50,
  WELCOME_POINTS: 20,
} as const;

export const AntiSpamPolicy = {
  /** Minimum gap between two reports by the same user on the same station. */
  STATION_COOLDOWN_MS: 10 * 60 * 1000,
  /** Minimum gap between any two reports by the same user. */
  GLOBAL_COOLDOWN_MS: 2 * 60 * 1000,
  /** Reports per user per rolling 24 h before the account is flagged. */
  MAX_REPORTS_PER_DAY: 30,
} as const;

export const TrustPolicy = {
  MIN_SCORE: 0,
  MAX_SCORE: 100,
  INITIAL_SCORE: 50,
  VERIFIED_REPORT_DELTA: 2,
  CONFIRMED_BY_OTHERS_DELTA: 1,
  REJECTED_REPORT_DELTA: -8,
  DUPLICATE_PHOTO_DELTA: -15,
  /** Below this, reports are held PENDING for manual review. */
  SUSPICIOUS_THRESHOLD: 25,
  /** Below this, the account is flagged for an admin ban decision. */
  BAN_REVIEW_THRESHOLD: 10,
} as const;

export const GeoProximity = {
  DEFAULT_RADIUS_METERS: 200,
} as const;

/** Congestion levels a user may claim in a report. */
export const REPORTABLE_STATUSES = new Set(["EMPTY", "MODERATE", "LONG"]);

export function adjustTrust(score: number, delta: number): number {
  return Math.min(TrustPolicy.MAX_SCORE, Math.max(TrustPolicy.MIN_SCORE, score + delta));
}

/** Haversine distance in meters — mirror of GeoProximity.distanceMeters. */
export function distanceMeters(
  lat1: number,
  lon1: number,
  lat2: number,
  lon2: number
): number {
  const R = 6371000;
  const toRad = (d: number) => (d * Math.PI) / 180;
  const dLat = toRad(lat2 - lat1);
  const dLon = toRad(lon2 - lon1);
  const a =
    Math.sin(dLat / 2) ** 2 +
    Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLon / 2) ** 2;
  return 2 * R * Math.asin(Math.sqrt(a));
}
