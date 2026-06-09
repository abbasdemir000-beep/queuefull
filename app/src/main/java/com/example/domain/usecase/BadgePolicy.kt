package com.example.domain.usecase

import com.example.domain.model.Badge

object BadgePolicy {

    fun highestEarned(verifiedContributions: Int): Badge? =
        Badge.values().reversed().firstOrNull { verifiedContributions >= it.minVerifiedContributions }

    fun nextTarget(verifiedContributions: Int): Badge? =
        Badge.values().firstOrNull { verifiedContributions < it.minVerifiedContributions }

    fun progressToNext(verifiedContributions: Int): Float {
        val next = nextTarget(verifiedContributions) ?: return 1f
        val prev = Badge.values().lastOrNull { it.ordinal < next.ordinal }
        val floor = prev?.minVerifiedContributions ?: 0
        val range = next.minVerifiedContributions - floor
        return if (range == 0) 1f else (verifiedContributions - floor).toFloat() / range
    }
}
