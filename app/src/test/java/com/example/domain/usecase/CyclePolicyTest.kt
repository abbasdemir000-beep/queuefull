package com.example.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CyclePolicyTest {

    @Test
    fun `cycle duration is five hours`() {
        assertEquals(5L * 60 * 60 * 1000, CyclePolicy.CYCLE_DURATION_MS)
        assertEquals(18_000_000L, CyclePolicy.CYCLE_DURATION_MS)
    }

    @Test
    fun `pre-end notice fires at four hours fifty-eight minutes`() {
        assertEquals(17_880_000L, CyclePolicy.PRE_END_NOTICE_DELAY_MS)
    }

    @Test
    fun `remaining is full duration at the start of a cycle`() {
        assertEquals(CyclePolicy.CYCLE_DURATION_MS, CyclePolicy.remainingMs(5_000L, 5_000L))
    }

    @Test
    fun `remaining decreases as time elapses`() {
        assertEquals(CyclePolicy.CYCLE_DURATION_MS - 1_000_000L, CyclePolicy.remainingMs(0L, 1_000_000L))
    }

    @Test
    fun `cycle is complete exactly at the duration boundary`() {
        assertTrue(CyclePolicy.isCycleComplete(0L, CyclePolicy.CYCLE_DURATION_MS))
        assertTrue(CyclePolicy.isCycleComplete(0L, CyclePolicy.CYCLE_DURATION_MS + 1))
    }

    @Test
    fun `cycle is not complete just before the boundary`() {
        assertFalse(CyclePolicy.isCycleComplete(0L, CyclePolicy.CYCLE_DURATION_MS - 1))
    }

    @Test
    fun `pre-end notice boundary is inclusive`() {
        assertTrue(CyclePolicy.shouldSendPreEndNotice(CyclePolicy.PRE_END_NOTICE_DELAY_MS))
        assertFalse(CyclePolicy.shouldSendPreEndNotice(CyclePolicy.PRE_END_NOTICE_DELAY_MS - 1))
    }

    @Test
    fun `formatRemaining renders hh-mm-ss`() {
        assertEquals("00:00:00", CyclePolicy.formatRemaining(0L))
        assertEquals("05:00:00", CyclePolicy.formatRemaining(CyclePolicy.CYCLE_DURATION_MS))
        // 1h 1m 1s = 3661 s
        assertEquals("01:01:01", CyclePolicy.formatRemaining(3_661_000L))
    }

    @Test
    fun `formatRemaining clamps negative values to zero`() {
        assertEquals("00:00:00", CyclePolicy.formatRemaining(-5_000L))
    }
}
