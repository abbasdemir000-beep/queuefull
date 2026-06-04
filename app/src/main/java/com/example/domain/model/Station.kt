package com.example.domain.model

/**
 * Pure domain model for a fuel station.
 *
 * Framework-agnostic by design. Field names/types mirror the persistence entity
 * exactly so existing presentation code is unaffected by the layer split.
 */
data class Station(
    val id: Int = 0,
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
