package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cities")
data class City(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameAr: String,
    val nameEn: String,
    val isApproved: Boolean = true
)

@Entity(tableName = "stations")
data class Station(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityId: Int,
    val cityName: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String, // "حكومية" / "أهلية"
    val fuelTypes: String, // Comma separated: e.g. "عادي, محسن, سوبر"
    val queueStatus: String = "EMPTY", // EMPTY, SHORT, MODERATE, LONG, CLOSED
    val lastUpdated: Long = System.currentTimeMillis(),
    val confirmedCount: Int = 1,
    val hasFuel: Boolean = true,
    val isApproved: Boolean = true,
    val suggestedBy: String? = null
)

@Entity(tableName = "queue_updates")
data class QueueUpdate(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val stationId: Int,
    val queueStatus: String,
    val hasFuel: Boolean,
    val fuelType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userPhone: String,
    val latitude: Double,
    val longitude: Double
)

@Entity(tableName = "ad_banners")
data class AdBanner(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val city: String,
    val category: String, // "زيوت سيارات", "كراج صيانة", "مطاعم", "غسيل سيارات"
    val ctaPhone: String? = null,
    val imageUrl: String? = null
)

@Entity(tableName = "app_users")
data class AppUser(
    @PrimaryKey val phoneNumber: String,
    val name: String,
    val role: String = "USER", // "USER", "REPORTER", "ADMIN"
    val points: Int = 0,
    val banned: Boolean = false
)

@Dao
interface QueueFuelDao {
    // Cities
    @Query("SELECT * FROM cities WHERE isApproved = 1")
    fun getApprovedCities(): Flow<List<City>>

    @Query("SELECT * FROM cities")
    fun getAllCities(): Flow<List<City>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCity(city: City): Long

    @Update
    suspend fun updateCity(city: City)

    @Query("DELETE FROM cities WHERE id = :id")
    suspend fun deleteCityById(id: Int)

    // Stations
    @Query("SELECT * FROM stations WHERE isApproved = 1")
    fun getApprovedStations(): Flow<List<Station>>

    @Query("SELECT * FROM stations")
    fun getAllStations(): Flow<List<Station>>

    @Query("SELECT * FROM stations WHERE cityId = :cityId AND isApproved = 1")
    fun getStationsByCity(cityId: Int): Flow<List<Station>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStation(station: Station): Long

    @Update
    suspend fun updateStation(station: Station)

    @Query("DELETE FROM stations WHERE id = :id")
    suspend fun deleteStationById(id: Int)

    // Queue Updates
    @Query("SELECT * FROM queue_updates ORDER BY timestamp DESC")
    fun getAllQueueUpdates(): Flow<List<QueueUpdate>>

    @Query("SELECT * FROM queue_updates WHERE stationId = :stationId ORDER BY timestamp DESC LIMIT 5")
    fun getUpdatesForStation(stationId: Int): Flow<List<QueueUpdate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueueUpdate(update: QueueUpdate): Long

    // Ad Banners
    @Query("SELECT * FROM ad_banners")
    fun getAllBanners(): Flow<List<AdBanner>>

    @Query("SELECT * FROM ad_banners WHERE city = :city")
    fun getBannersByCity(city: String): Flow<List<AdBanner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: AdBanner): Long

    @Query("DELETE FROM ad_banners WHERE id = :id")
    suspend fun deleteBannerById(id: Int)

    // Users
    @Query("SELECT * FROM app_users WHERE phoneNumber = :phone")
    suspend fun getUserByPhone(phone: String): AppUser?

    @Query("SELECT * FROM app_users")
    fun getAllUsers(): Flow<List<AppUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUser)

    @Update
    suspend fun updateUser(user: AppUser)
}

@Database(entities = [City::class, Station::class, QueueUpdate::class, AdBanner::class, AppUser::class], version = 1, exportSchema = false)
abstract class QueueFuelDatabase : RoomDatabase() {
    abstract fun dao(): QueueFuelDao
}
