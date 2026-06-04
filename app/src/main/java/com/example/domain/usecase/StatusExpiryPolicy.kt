package com.example.domain.usecase

/**
 * Rules governing when a station's reported queue status is considered stale.
 *
 * Pure and framework-agnostic. Extracted verbatim from
 * `QueueFuelViewModel.expireOldUpdates`. The ViewModel keeps the orchestration
 * (reading stations, writing the reset, emitting the notification); this object
 * owns only the decision.
 */
object StatusExpiryPolicy {

    /** A non-EMPTY status older than this (40 minutes) is considered expired. */
    const val EXPIRY_THRESHOLD_MS: Long = 2_400_000L

    /** The status value a station reverts to once its report expires. */
    const val EXPIRED_STATUS: String = "EMPTY"

    /**
     * True when a station's status should expire back to EMPTY.
     *
     * Mirrors the original condition exactly:
     * `queueStatus != "EMPTY" && (now - lastUpdated) > 2_400_000`.
     * The threshold is strict (`>`), so a status exactly at the boundary is NOT
     * yet expired.
     */
    fun isExpired(queueStatus: String, lastUpdated: Long, now: Long): Boolean =
        queueStatus != EXPIRED_STATUS && (now - lastUpdated) > EXPIRY_THRESHOLD_MS
}
