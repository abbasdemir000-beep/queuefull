package com.example.domain.usecase

/**
 * Per-user trust score [0..100] driving the anti-spam escalation ladder.
 *
 * Maintained server-side (Cloud Functions adjust `users/{uid}.trustScore`);
 * mirrored in `backend/functions/src/policies.ts`. Below
 * [SUSPICIOUS_THRESHOLD] new reports are held PENDING for manual review
 * instead of auto-verifying; below [BAN_REVIEW_THRESHOLD] the account is
 * flagged for an admin ban decision.
 */
object TrustPolicy {

    const val MIN_SCORE: Int = 0
    const val MAX_SCORE: Int = 100
    const val INITIAL_SCORE: Int = 50

    const val VERIFIED_REPORT_DELTA: Int = 2
    const val CONFIRMED_BY_OTHERS_DELTA: Int = 1
    const val REJECTED_REPORT_DELTA: Int = -8
    const val DUPLICATE_PHOTO_DELTA: Int = -15

    const val SUSPICIOUS_THRESHOLD: Int = 25
    const val BAN_REVIEW_THRESHOLD: Int = 10

    /** Applies a delta and clamps the result into [MIN_SCORE]..[MAX_SCORE]. */
    fun adjust(score: Int, delta: Int): Int = (score + delta).coerceIn(MIN_SCORE, MAX_SCORE)

    /** Suspicious users lose auto-verification — their reports stay PENDING. */
    fun isSuspicious(score: Int): Boolean = score < SUSPICIOUS_THRESHOLD

    /** True when the account should be surfaced to an admin for a ban review. */
    fun needsBanReview(score: Int): Boolean = score < BAN_REVIEW_THRESHOLD
}
