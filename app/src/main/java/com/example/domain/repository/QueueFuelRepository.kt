package com.example.domain.repository

import com.example.domain.model.AdBanner
import com.example.domain.model.AppUser
import com.example.domain.model.City
import com.example.domain.model.LeaderboardEntry
import com.example.domain.model.QueueUpdate
import com.example.domain.model.ReportConfidence
import com.example.domain.model.RewardEntry
import com.example.domain.model.Station
import com.example.domain.model.UserBadge
import kotlinx.coroutines.flow.Flow

interface QueueFuelRepository {

    // ---- Observable streams ----
    val approvedCities: Flow<List<City>>
    val allCities: Flow<List<City>>
    val approvedStations: Flow<List<Station>>
    val allStations: Flow<List<Station>>
    val allBanners: Flow<List<AdBanner>>
    val allUsers: Flow<List<AppUser>>
    val allQueueUpdates: Flow<List<QueueUpdate>>

    fun getStationsByCity(cityId: Int): Flow<List<Station>>
    fun getBannersByCity(city: String): Flow<List<AdBanner>>
    fun getUpdatesForStation(stationId: Int): Flow<List<QueueUpdate>>

    // ---- Cities ----
    suspend fun insertCity(city: City)
    suspend fun approveCity(city: City)
    suspend fun deleteCity(id: Int)

    // ---- Stations ----
    suspend fun insertStation(station: Station)
    suspend fun updateStation(station: Station)
    suspend fun deleteStation(id: Int)

    // ---- Queue updates ----
    suspend fun insertQueueUpdate(update: QueueUpdate)
    suspend fun confirmQueueStatus(stationId: Int, userPhone: String)
    suspend fun getLatestUpdateForStation(stationId: Int): QueueUpdate?

    /** Admin override: mark a report verified and grant the reporter points + a raffle entry. */
    suspend fun approveReport(reportId: Int)

    /** Admin override: mark a report rejected (kept for audit, no rewards). */
    suspend fun rejectReport(reportId: Int)

    // ---- Users / rewards ----
    suspend fun rewardUserPoints(phone: String, pointsToAdd: Int)
    suspend fun getUserByPhone(phone: String): AppUser?
    suspend fun insertUser(user: AppUser)
    suspend fun updateUser(user: AppUser)

    // ---- Banners ----
    suspend fun insertBanner(banner: AdBanner)
    suspend fun deleteBanner(id: Int)

    // ---- Leaderboard ----
    suspend fun getLeaderboard(): List<LeaderboardEntry>
    suspend fun resetMonthlyPoints()

    // ---- Badges ----
    suspend fun getBadgesForUser(phone: String): List<UserBadge>
    suspend fun awardBadge(badge: UserBadge)

    // ---- Reward pool ----
    suspend fun createRewardEntry(entry: RewardEntry)
    suspend fun getRewardEntriesForMonth(monthYear: String): List<RewardEntry>
    suspend fun markRewardEntryVerified(entryId: Int)

    // ---- Report confidence ----
    suspend fun upsertReportConfidence(confidence: ReportConfidence)
    suspend fun getConfidenceForStation(stationId: Int): ReportConfidence?
    suspend fun confirmReportConfidence(stationId: Int, newScore: Float)
    suspend fun expireOldConfidences(now: Long)
}
