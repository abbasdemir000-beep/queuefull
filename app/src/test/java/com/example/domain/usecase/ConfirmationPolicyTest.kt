package com.example.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfirmationPolicyTest {

    private val window = ConfirmationPolicy.VALIDITY_WINDOW_MS

    @Test
    fun `station with recent non-empty status has active report`() {
        val now = System.currentTimeMillis()
        val lastUpdated = now - (window / 2)
        assertTrue(ConfirmationPolicy.hasActiveReport(lastUpdated, "SHORT", now))
    }

    @Test
    fun `station with EMPTY status never has active report`() {
        val now = System.currentTimeMillis()
        assertFalse(ConfirmationPolicy.hasActiveReport(now, "EMPTY", now))
    }

    @Test
    fun `station whose report expired has no active report`() {
        val now = System.currentTimeMillis()
        val lastUpdated = now - window - 1
        assertFalse(ConfirmationPolicy.hasActiveReport(lastUpdated, "LONG", now))
    }

    @Test
    fun `exactly at window boundary is still valid`() {
        val now = System.currentTimeMillis()
        val lastUpdated = now - window
        assertTrue(ConfirmationPolicy.isInValidityWindow(lastUpdated, now))
    }

    @Test
    fun `one ms past window is expired`() {
        val now = System.currentTimeMillis()
        val lastUpdated = now - window - 1
        assertFalse(ConfirmationPolicy.isInValidityWindow(lastUpdated, now))
    }
}
