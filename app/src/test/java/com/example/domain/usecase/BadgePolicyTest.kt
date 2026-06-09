package com.example.domain.usecase

import com.example.domain.model.Badge
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BadgePolicyTest {

    @Test
    fun `no badge before 5 contributions`() {
        assertNull(BadgePolicy.highestEarned(0))
        assertNull(BadgePolicy.highestEarned(4))
    }

    @Test
    fun `reporter badge at 5 contributions`() {
        assertEquals(Badge.REPORTER, BadgePolicy.highestEarned(5))
        assertEquals(Badge.REPORTER, BadgePolicy.highestEarned(24))
    }

    @Test
    fun `trusted reporter badge at 25 contributions`() {
        assertEquals(Badge.TRUSTED_REPORTER, BadgePolicy.highestEarned(25))
        assertEquals(Badge.TRUSTED_REPORTER, BadgePolicy.highestEarned(99))
    }

    @Test
    fun `elite reporter badge at 100 contributions`() {
        assertEquals(Badge.ELITE_REPORTER, BadgePolicy.highestEarned(100))
    }

    @Test
    fun `legend badge at 500 contributions`() {
        assertEquals(Badge.LEGEND, BadgePolicy.highestEarned(500))
        assertEquals(Badge.LEGEND, BadgePolicy.highestEarned(9999))
    }

    @Test
    fun `next target for zero contributions is reporter`() {
        assertEquals(Badge.REPORTER, BadgePolicy.nextTarget(0))
    }

    @Test
    fun `next target for legend is null`() {
        assertNull(BadgePolicy.nextTarget(500))
    }

    @Test
    fun `progress before first badge`() {
        val progress = BadgePolicy.progressToNext(3)
        assertEquals(3f / 5f, progress, 0.01f)
    }

    @Test
    fun `progress at max is 1`() {
        assertEquals(1f, BadgePolicy.progressToNext(600), 0.01f)
    }
}
