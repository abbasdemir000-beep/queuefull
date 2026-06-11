package com.example.domain.usecase

/**
 * Pure logic for the 5-hour competition cycle (timing + display formatting).
 *
 * Framework-agnostic. Extracted verbatim from `QueueFuelViewModel.updateCycleTimer`
 * /`loadOrCreateCycle`. The ViewModel retains all side effects (prefs, point
 * resets, notifications); this object owns only timing math and formatting.
 */
object CyclePolicy {

    /** Total length of one competition cycle: 5 hours. */
    const val CYCLE_DURATION_MS: Long = 5L * 60 * 60 * 1000

    /** Elapsed time after which the "cycle ending soon" notice fires: 4h 58m. */
    const val PRE_END_NOTICE_DELAY_MS: Long = (4L * 60 * 60 + 58 * 60) * 1000

    /** Milliseconds remaining in the cycle that started at [startTime] as of [now]. */
    fun remainingMs(startTime: Long, now: Long): Long = CYCLE_DURATION_MS - (now - startTime)

    /** True once the cycle that started at [startTime] has elapsed (remaining <= 0). */
    fun isCycleComplete(startTime: Long, now: Long): Boolean = remainingMs(startTime, now) <= 0

    /** True once enough time has elapsed to send the pre-end reminder. */
    fun shouldSendPreEndNotice(elapsedMs: Long): Boolean = elapsedMs >= PRE_END_NOTICE_DELAY_MS

    /**
     * Formats remaining milliseconds as "hh:mm:ss", clamping negatives to zero.
     *
     * Uses [java.util.Locale.ROOT] so the countdown always renders ASCII digits;
     * the default locale would render Eastern-Arabic numerals on Arabic devices
     * and break the fixed-width hh:mm:ss display.
     */
    fun formatRemaining(remainingMs: Long): String {
        val totalSecs = maxOf(0L, remainingMs / 1000L)
        val hrs = totalSecs / 3600
        val mins = (totalSecs % 3600) / 60
        val secs = totalSecs % 60
        return String.format(java.util.Locale.ROOT, "%02d:%02d:%02d", hrs, mins, secs)
    }
}
