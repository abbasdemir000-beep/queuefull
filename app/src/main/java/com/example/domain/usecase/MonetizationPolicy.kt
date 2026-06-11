package com.example.domain.usecase

import com.example.domain.model.AccountType

/**
 * Premium/ads gating rules. The flags in [FeatureFlags] stay off during the
 * MVP; this policy is the single place UI and ads code must ask before
 * rendering monetized surfaces (see docs/MONETIZATION.md).
 */
object MonetizationPolicy {

    /** True while the user has an active PREMIUM subscription. */
    fun isPremium(accountType: String, premiumUntil: Long?, now: Long): Boolean =
        accountType == AccountType.PREMIUM.name && premiumUntil != null && premiumUntil > now

    /** AdMob/banner surfaces render only when ads are on and the user is not premium. */
    fun shouldShowAds(accountType: String, premiumUntil: Long?, now: Long): Boolean =
        FeatureFlags.ADS_ENABLED && !isPremium(accountType, premiumUntil, now)
}
