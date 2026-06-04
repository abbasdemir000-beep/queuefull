package com.example.domain.repository

import com.example.domain.model.AdBanner
import com.example.domain.model.AppUser
import com.example.domain.model.City
import com.example.domain.model.QueueUpdate
import com.example.domain.model.Station
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over the QueueFuel data layer.
 *
 * The presentation layer (ViewModels) depends on this interface only, never on a
 * concrete implementation. Today the single implementation is Room-backed
 * ([com.example.data.repository.QueueFuelRepositoryImpl]); tomorrow a remote- or
 * cache-backed implementation can be swapped in without touching callers. All
 * types crossing this boundary are pure domain models.
 *
 * Note: this PR is a structural extraction only. The method surface mirrors the
 * previous concrete repository exactly to guarantee identical behavior.
 */
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

    // ---- Users / rewards ----
    suspend fun rewardUserPoints(phone: String, pointsToAdd: Int)
    suspend fun getUserByPhone(phone: String): AppUser?
    suspend fun insertUser(user: AppUser)
    suspend fun updateUser(user: AppUser)

    // ---- Banners ----
    suspend fun insertBanner(banner: AdBanner)
    suspend fun deleteBanner(id: Int)
}
