package com.example.domain.usecase

import com.example.domain.model.AccountType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MonetizationPolicyTest {

    private val now = 1_000_000_000L

    @Test
    fun `active premium subscription is recognized`() {
        assertTrue(MonetizationPolicy.isPremium(AccountType.PREMIUM.name, now + 1, now))
    }

    @Test
    fun `expired premium is not premium`() {
        assertFalse(MonetizationPolicy.isPremium(AccountType.PREMIUM.name, now, now))
        assertFalse(MonetizationPolicy.isPremium(AccountType.PREMIUM.name, now - 1, now))
    }

    @Test
    fun `premium without an expiry date is not premium`() {
        assertFalse(MonetizationPolicy.isPremium(AccountType.PREMIUM.name, null, now))
    }

    @Test
    fun `free account is never premium`() {
        assertFalse(MonetizationPolicy.isPremium(AccountType.FREE.name, now + 1, now))
    }

    @Test
    fun `ads stay off while the feature flag is disabled`() {
        // FeatureFlags.ADS_ENABLED is false during the MVP, so no surface may show ads.
        assertFalse(MonetizationPolicy.shouldShowAds(AccountType.FREE.name, null, now))
        assertFalse(MonetizationPolicy.shouldShowAds(AccountType.PREMIUM.name, now + 1, now))
    }
}
