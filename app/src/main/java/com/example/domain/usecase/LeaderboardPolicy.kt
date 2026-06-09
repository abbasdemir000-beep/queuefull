package com.example.domain.usecase

import com.example.domain.model.AppUser
import com.example.domain.model.LeaderboardEntry

object LeaderboardPolicy {

    fun rank(users: List<AppUser>): List<LeaderboardEntry> =
        users.sortedByDescending { it.monthlyPoints }
            .mapIndexed { index, user ->
                LeaderboardEntry(
                    rank = index + 1,
                    userPhone = user.phoneNumber,
                    userName = user.name,
                    monthlyPoints = user.monthlyPoints,
                    badge = BadgePolicy.highestEarned(user.lifetimePoints)
                )
            }

    fun currentUserEntry(users: List<AppUser>, phone: String): LeaderboardEntry? =
        rank(users).firstOrNull { it.userPhone == phone }
}
