package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueFuelDao {
    // Cities
    @Query("SELECT * FROM cities WHERE isApproved = 1")
    fun getApprovedCities(): Flow<List<CityEntity>>

    @Query("SELECT * FROM cities")
    fun getAllCities(): Flow<List<CityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: CityEntity): Long

    @Update
    suspend fun updateCity(city: CityEntity)

    @Query("DELETE FROM cities WHERE id = :id")
    suspend fun deleteCityById(id: Int)

    // Stations
    @Query("SELECT * FROM stations WHERE isApproved = 1")
    fun getApprovedStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations")
    fun getAllStations(): Flow<List<StationEntity>>

    @Query("SELECT * FROM stations WHERE cityId = :cityId AND isApproved = 1")
    fun getStationsByCity(cityId: Int): Flow<List<StationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: StationEntity): Long

    @Update
    suspend fun updateStation(station: StationEntity)

    @Query("DELETE FROM stations WHERE id = :id")
    suspend fun deleteStationById(id: Int)

    // Queue Updates
    @Query("SELECT * FROM queue_updates ORDER BY timestamp DESC")
    fun getAllQueueUpdates(): Flow<List<QueueUpdateEntity>>

    @Query("SELECT * FROM queue_updates WHERE stationId = :stationId ORDER BY timestamp DESC LIMIT 5")
    fun getUpdatesForStation(stationId: Int): Flow<List<QueueUpdateEntity>>

    @Query("SELECT * FROM queue_updates WHERE stationId = :stationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestUpdateForStation(stationId: Int): QueueUpdateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueUpdate(update: QueueUpdateEntity): Long

    @Query("SELECT * FROM queue_updates WHERE id = :id")
    suspend fun getQueueUpdateById(id: Int): QueueUpdateEntity?

    @Query("UPDATE queue_updates SET verification = :status, verificationNote = :note WHERE id = :id")
    suspend fun setReportVerification(id: Int, status: String, note: String)

    // Ad Banners
    @Query("SELECT * FROM ad_banners")
    fun getAllBanners(): Flow<List<AdBannerEntity>>

    @Query("SELECT * FROM ad_banners WHERE city = :city")
    fun getBannersByCity(city: String): Flow<List<AdBannerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: AdBannerEntity): Long

    @Query("DELETE FROM ad_banners WHERE id = :id")
    suspend fun deleteBannerById(id: Int)

    // Users
    @Query("SELECT * FROM app_users WHERE phoneNumber = :phone")
    suspend fun getUserByPhone(phone: String): AppUserEntity?

    @Query("SELECT * FROM app_users")
    fun getAllUsers(): Flow<List<AppUserEntity>>

    @Query("SELECT * FROM app_users ORDER BY monthlyPoints DESC")
    suspend fun getUsersRankedByMonthlyPoints(): List<AppUserEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUserEntity)

    @Update
    suspend fun updateUser(user: AppUserEntity)

    @Query("UPDATE app_users SET monthlyPoints = 0")
    suspend fun resetAllMonthlyPoints()

    // Advertisements
    @Query("SELECT * FROM advertisements WHERE isActive = 1")
    fun getActiveAds(): Flow<List<AdvertisementEntity>>

    @Query("SELECT * FROM advertisements WHERE isActive = 1 AND targetCity = :city")
    fun getAdsByCity(city: String): Flow<List<AdvertisementEntity>>

    @Query("SELECT * FROM advertisements WHERE isActive = 1 AND category = :category")
    fun getAdsByCategory(category: String): Flow<List<AdvertisementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: AdvertisementEntity): Long

    @Query("DELETE FROM advertisements WHERE id = :id")
    suspend fun deleteAdById(id: Int)

    @Query("UPDATE advertisements SET viewCount = viewCount + 1 WHERE id = :id")
    suspend fun incrementAdViewCount(id: Int)

    @Query("UPDATE advertisements SET clickCount = clickCount + 1 WHERE id = :id")
    suspend fun incrementAdClickCount(id: Int)

    // User Badges
    @Query("SELECT * FROM user_badges WHERE userPhone = :phone")
    suspend fun getBadgesForUser(phone: String): List<UserBadgeEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUserBadge(badge: UserBadgeEntity)

    // Reward Entries
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewardEntry(entry: RewardEntryEntity): Long

    @Query("SELECT * FROM reward_entries WHERE monthYear = :monthYear")
    suspend fun getRewardEntriesForMonth(monthYear: String): List<RewardEntryEntity>

    @Query("UPDATE reward_entries SET isVerified = 1 WHERE id = :id")
    suspend fun markRewardEntryVerified(id: Int)

    // Report Confidence
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReportConfidence(confidence: ReportConfidenceEntity): Long

    @Query("SELECT * FROM report_confidence WHERE stationId = :stationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getConfidenceForStation(stationId: Int): ReportConfidenceEntity?

    @Query("UPDATE report_confidence SET confirmationCount = confirmationCount + 1, confidenceScore = :score WHERE stationId = :stationId AND isExpired = 0")
    suspend fun updateConfidence(stationId: Int, score: Float)

    @Query("UPDATE report_confidence SET isExpired = 1 WHERE expiresAt < :now AND isExpired = 0")
    suspend fun expireOldConfidence(now: Long)
}
