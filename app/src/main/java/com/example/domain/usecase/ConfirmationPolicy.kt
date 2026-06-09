package com.example.domain.usecase

object ConfirmationPolicy {

    const val VALIDITY_WINDOW_MS: Long = 25L * 60 * 1000

    fun hasActiveReport(lastUpdated: Long, queueStatus: String, now: Long): Boolean =
        queueStatus != "EMPTY" && (now - lastUpdated) <= VALIDITY_WINDOW_MS

    fun isInValidityWindow(lastUpdated: Long, now: Long): Boolean =
        (now - lastUpdated) <= VALIDITY_WINDOW_MS
}
