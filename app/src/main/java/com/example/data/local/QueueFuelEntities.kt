package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room persistence entities.
 *
 * IMPORTANT: table names and column names are intentionally identical to the
 * previous schema (cities, stations, queue_updates, ad_banners, app_users).
 * Only the Kotlin class names changed (added the `Entity` suffix). Room keys its
 * schema off table/column names, so this rename requires NO migration and causes
 * NO data loss. These types never leave the data layer; the repository maps them
 * to/from pure domain models via [Mappers].
 */

@Entity(tableName = "cities")
data class CityEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameAr: String,
    val nameEn: String,
    val isApproved: Boolean = true
)

@Entity(tableName = "stations")
data class StationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cityId: Int,
    val cityName: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val type: String,
    val fuelTypes: String,
    val queueStatus: String = "EMPTY",
    val lastUpdated: Long = System.currentTimeMillis(),
    val confirmedCount: Int = 1,
    val hasFuel: Boolean = true,
    val isApproved: Boolean = true,
    val suggestedBy: String? = null
)

@Entity(tableName = "queue_updates")
data class QueueUpdateEntity(
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
data class AdBannerEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val city: String,
    val category: String,
    val ctaPhone: String? = null,
    val imageUrl: String? = null
)

@Entity(tableName = "app_users")
data class AppUserEntity(
    @PrimaryKey val phoneNumber: String,
    val name: String,
    val role: String = "USER",
    val points: Int = 0,
    val banned: Boolean = false
)
