package com.example.domain.usecase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureFlagsTest {

    @Test
    fun `ads disabled by default`() {
        assertFalse(FeatureFlags.ADS_ENABLED)
    }

    @Test
    fun `premium disabled by default`() {
        assertFalse(FeatureFlags.PREMIUM_ENABLED)
    }

    @Test
    fun `leaderboard enabled by default`() {
        assertTrue(FeatureFlags.LEADERBOARD_ENABLED)
    }

    @Test
    fun `reward pool disabled by default`() {
        assertFalse(FeatureFlags.REWARD_POOL_ENABLED)
    }

    @Test
    fun `one tap confirm enabled by default`() {
        assertTrue(FeatureFlags.ONE_TAP_CONFIRM_ENABLED)
    }
}
