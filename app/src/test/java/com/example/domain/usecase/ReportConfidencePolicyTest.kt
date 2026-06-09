package com.example.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportConfidencePolicyTest {

    @Test
    fun `fresh report has initial score near 1`() {
        val score = ReportConfidencePolicy.calculateScore(0, 0)
        assertEquals(ReportConfidencePolicy.INITIAL_SCORE, score, 0.01f)
    }

    @Test
    fun `confirmations boost score`() {
        val score = ReportConfidencePolicy.calculateScore(5, 0)
        assertTrue(score > ReportConfidencePolicy.INITIAL_SCORE)
    }

    @Test
    fun `score at expiry boundary approaches zero`() {
        val score = ReportConfidencePolicy.calculateScore(0, ReportConfidencePolicy.EXPIRY_MS)
        assertEquals(0f, score, 0.01f)
    }

    @Test
    fun `score never exceeds max`() {
        val score = ReportConfidencePolicy.calculateScore(1000, 0)
        assertTrue(score <= ReportConfidencePolicy.MAX_SCORE)
    }

    @Test
    fun `not expired before expiresAt`() {
        val now = System.currentTimeMillis()
        assertFalse(ReportConfidencePolicy.isExpired(now + 1000, now))
    }

    @Test
    fun `expired after expiresAt`() {
        val now = System.currentTimeMillis()
        assertTrue(ReportConfidencePolicy.isExpired(now - 1, now))
    }

    @Test
    fun `computeExpiresAt adds expiry offset`() {
        val ts = 1_000_000L
        assertEquals(ts + ReportConfidencePolicy.EXPIRY_MS, ReportConfidencePolicy.computeExpiresAt(ts))
    }
}
