package com.example.domain.usecase

/**
 * Pure anti-spam rules for report submission.
 *
 * Enforced client-side for fast feedback and mirrored server-side in
 * `backend/functions/src/policies.ts` — the server values are authoritative,
 * the client checks only improve UX. Keep both files in sync.
 */
object AntiSpamPolicy {

    /** Minimum gap between two reports by the same user on the same station. */
    const val STATION_COOLDOWN_MS: Long = 10L * 60 * 1000

    /** Minimum gap between any two reports by the same user. */
    const val GLOBAL_COOLDOWN_MS: Long = 2L * 60 * 1000

    /** Reports per user per rolling 24 h before the account is flagged. */
    const val MAX_REPORTS_PER_DAY: Int = 30

    /**
     * Milliseconds the user must still wait before reporting again, or 0 when
     * the cooldown has elapsed (or no previous report exists).
     */
    fun cooldownRemainingMs(lastReportAt: Long?, now: Long, cooldownMs: Long): Long {
        if (lastReportAt == null) return 0L
        return (cooldownMs - (now - lastReportAt)).coerceAtLeast(0L)
    }

    /**
     * True when both the per-station and the global cooldown have elapsed.
     */
    fun canReport(lastStationReportAt: Long?, lastAnyReportAt: Long?, now: Long): Boolean =
        cooldownRemainingMs(lastStationReportAt, now, STATION_COOLDOWN_MS) == 0L &&
            cooldownRemainingMs(lastAnyReportAt, now, GLOBAL_COOLDOWN_MS) == 0L

    /**
     * True when the photo hash matches an already-seen report photo — a strong
     * signal the user is re-submitting the same picture to farm points.
     */
    fun isDuplicatePhoto(photoHash: String?, knownHashes: Collection<String>): Boolean =
        !photoHash.isNullOrBlank() && photoHash in knownHashes

    /** True when the user exceeded the rolling 24 h report budget. */
    fun exceedsDailyLimit(reportsLast24h: Int): Boolean = reportsLast24h >= MAX_REPORTS_PER_DAY
}
