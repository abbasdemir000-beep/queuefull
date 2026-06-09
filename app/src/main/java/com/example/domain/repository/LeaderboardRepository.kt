package com.example.domain.repository

import com.example.domain.model.LeaderboardEntry

interface LeaderboardRepository {
    suspend fun getLeaderboard(monthYear: String): List<LeaderboardEntry>
    suspend fun resetMonthlyPoints()
    suspend fun getCurrentUserRank(phone: String, monthYear: String): Int
}
