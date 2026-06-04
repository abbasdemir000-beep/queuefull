package com.example.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class PointsPolicyTest {

    @Test
    fun `reward constants match the established economy`() {
        assertEquals(15, PointsPolicy.REPORT_POINTS)
        assertEquals(5, PointsPolicy.CONFIRMATION_POINTS)
        assertEquals(30, PointsPolicy.STATION_SUGGESTION_POINTS)
        assertEquals(50, PointsPolicy.STATION_APPROVAL_POINTS)
        assertEquals(20, PointsPolicy.WELCOME_POINTS)
        assertEquals(20, PointsPolicy.CYCLE_RESET_POINTS)
        assertEquals(250, PointsPolicy.ADMIN_INITIAL_POINTS)
    }

    @Test
    fun `award adds points to the current balance`() {
        assertEquals(115, PointsPolicy.award(100, PointsPolicy.REPORT_POINTS))
        assertEquals(20, PointsPolicy.award(0, PointsPolicy.WELCOME_POINTS))
    }

    @Test
    fun `award with zero is a no-op`() {
        assertEquals(42, PointsPolicy.award(42, 0))
    }
}
