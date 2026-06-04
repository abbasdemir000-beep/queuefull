package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO. Operates exclusively on persistence entities; mapping to domain
 * models happens in the repository implementation. Queries are byte-for-byte
 * identical to the previous DAO.
 */
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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueUpdate(update: QueueUpdateEntity): Long

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUserEntity)

    @Update
    suspend fun updateUser(user: AppUserEntity)
}
