package com.example.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AntiSpamPolicyTest {

    private val now = 1_000_000_000L

    // ---- Cooldown remaining ----

    @Test
    fun `no previous report means no cooldown`() {
        assertEquals(0L, AntiSpamPolicy.cooldownRemainingMs(null, now, AntiSpamPolicy.STATION_COOLDOWN_MS))
    }

    @Test
    fun `cooldown counts down from the last report`() {
        val last = now - 4 * 60 * 1000 // 4 minutes ago
        val remaining = AntiSpamPolicy.cooldownRemainingMs(last, now, AntiSpamPolicy.STATION_COOLDOWN_MS)
        assertEquals(6 * 60 * 1000L, remaining) // 10 min cooldown - 4 min elapsed
    }

    @Test
    fun `elapsed cooldown returns zero, never negative`() {
        val last = now - AntiSpamPolicy.STATION_COOLDOWN_MS - 1
        assertEquals(0L, AntiSpamPolicy.cooldownRemainingMs(last, now, AntiSpamPolicy.STATION_COOLDOWN_MS))
    }

    // ---- canReport ----

    @Test
    fun `first ever report is allowed`() {
        assertTrue(AntiSpamPolicy.canReport(null, null, now))
    }

    @Test
    fun `same station within 10 minutes is blocked`() {
        val last = now - 5 * 60 * 1000
        assertFalse(AntiSpamPolicy.canReport(last, last, now))
    }

    @Test
    fun `different station still respects the 2 minute global cooldown`() {
        val lastAny = now - 60 * 1000 // 1 minute ago, on another station
        assertFalse(AntiSpamPolicy.canReport(null, lastAny, now))
    }

    @Test
    fun `report allowed once both cooldowns elapsed`() {
        val lastStation = now - AntiSpamPolicy.STATION_COOLDOWN_MS
        val lastAny = now - AntiSpamPolicy.GLOBAL_COOLDOWN_MS
        assertTrue(AntiSpamPolicy.canReport(lastStation, lastAny, now))
    }

    // ---- Duplicate photo ----

    @Test
    fun `known hash is flagged as duplicate`() {
        assertTrue(AntiSpamPolicy.isDuplicatePhoto("abc123", setOf("abc123", "def456")))
    }

    @Test
    fun `new hash is not a duplicate`() {
        assertFalse(AntiSpamPolicy.isDuplicatePhoto("zzz999", setOf("abc123")))
    }

    @Test
    fun `missing hash is never a duplicate`() {
        assertFalse(AntiSpamPolicy.isDuplicatePhoto(null, setOf("abc123")))
        assertFalse(AntiSpamPolicy.isDuplicatePhoto("", setOf("abc123")))
    }

    // ---- Daily budget ----

    @Test
    fun `daily limit boundary`() {
        assertFalse(AntiSpamPolicy.exceedsDailyLimit(AntiSpamPolicy.MAX_REPORTS_PER_DAY - 1))
        assertTrue(AntiSpamPolicy.exceedsDailyLimit(AntiSpamPolicy.MAX_REPORTS_PER_DAY))
    }
}
