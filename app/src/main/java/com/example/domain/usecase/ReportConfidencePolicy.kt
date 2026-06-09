package com.example.domain.usecase

object ReportConfidencePolicy {

    const val INITIAL_SCORE: Float = 1.0f
    const val CONFIRMATION_BOOST: Float = 0.1f
    const val MAX_SCORE: Float = 3.0f
    const val EXPIRY_MS: Long = StatusExpiryPolicy.EXPIRY_THRESHOLD_MS

    fun computeExpiresAt(timestamp: Long): Long = timestamp + EXPIRY_MS

    fun isExpired(expiresAt: Long, now: Long): Boolean = now > expiresAt

    fun calculateScore(confirmationCount: Int, ageMs: Long): Float {
        val boosted = INITIAL_SCORE + (confirmationCount * CONFIRMATION_BOOST)
        val ageFactor = 1f - (ageMs.toFloat() / EXPIRY_MS.toFloat()).coerceIn(0f, 1f)
        return (boosted * ageFactor).coerceIn(0f, MAX_SCORE)
    }
}
