package com.example.domain.repository

import com.example.domain.model.RewardEntry

interface RewardPoolRepository {
    suspend fun createRewardEntry(entry: RewardEntry)
    suspend fun getEntriesForMonth(monthYear: String): List<RewardEntry>
    suspend fun markVerified(entryId: Int)
}
