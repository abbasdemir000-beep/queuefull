package com.example.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StatusExpiryPolicyTest {

    private val now = 1_000_000_000_000L

    @Test
    fun `threshold is 40 minutes`() {
        assertEquals(2_400_000L, StatusExpiryPolicy.EXPIRY_THRESHOLD_MS)
    }

    @Test
    fun `expired status reverts to EMPTY`() {
        assertEquals("EMPTY", StatusExpiryPolicy.EXPIRED_STATUS)
    }

    @Test
    fun `EMPTY status never expires`() {
        val veryOld = now - 10_000_000L
        assertFalse(StatusExpiryPolicy.isExpired("EMPTY", veryOld, now))
    }

    @Test
    fun `recent non-empty status is not expired`() {
        assertFalse(StatusExpiryPolicy.isExpired("LONG", now - 1_000L, now))
    }

    @Test
    fun `old non-empty status is expired`() {
        assertTrue(StatusExpiryPolicy.isExpired("LONG", now - 2_400_001L, now))
    }

    @Test
    fun `status exactly at the threshold is not yet expired`() {
        // Condition is strict (> threshold), so the exact boundary is still valid.
        assertFalse(StatusExpiryPolicy.isExpired("MODERATE", now - 2_400_000L, now))
    }
}
