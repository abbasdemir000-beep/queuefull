package com.example.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrustPolicyTest {

    @Test
    fun `verified report raises the score`() {
        assertEquals(52, TrustPolicy.adjust(TrustPolicy.INITIAL_SCORE, TrustPolicy.VERIFIED_REPORT_DELTA))
    }

    @Test
    fun `rejected report lowers the score`() {
        assertEquals(42, TrustPolicy.adjust(TrustPolicy.INITIAL_SCORE, TrustPolicy.REJECTED_REPORT_DELTA))
    }

    @Test
    fun `score is clamped at the maximum`() {
        assertEquals(TrustPolicy.MAX_SCORE, TrustPolicy.adjust(99, 50))
    }

    @Test
    fun `score is clamped at the minimum`() {
        assertEquals(TrustPolicy.MIN_SCORE, TrustPolicy.adjust(5, TrustPolicy.DUPLICATE_PHOTO_DELTA))
    }

    @Test
    fun `suspicious threshold gates auto-verification`() {
        assertFalse(TrustPolicy.isSuspicious(TrustPolicy.SUSPICIOUS_THRESHOLD))
        assertTrue(TrustPolicy.isSuspicious(TrustPolicy.SUSPICIOUS_THRESHOLD - 1))
    }

    @Test
    fun `ban review threshold flags the account`() {
        assertFalse(TrustPolicy.needsBanReview(TrustPolicy.BAN_REVIEW_THRESHOLD))
        assertTrue(TrustPolicy.needsBanReview(TrustPolicy.BAN_REVIEW_THRESHOLD - 1))
    }

    @Test
    fun `new users start neither suspicious nor flagged`() {
        assertFalse(TrustPolicy.isSuspicious(TrustPolicy.INITIAL_SCORE))
        assertFalse(TrustPolicy.needsBanReview(TrustPolicy.INITIAL_SCORE))
    }
}
