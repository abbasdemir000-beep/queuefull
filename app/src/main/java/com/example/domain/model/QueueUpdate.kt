package com.example.domain.model

/**
 * Pure domain model for a user-submitted queue status update.
 *
 * Framework-agnostic by design. Field names/types mirror the persistence entity
 * exactly so existing presentation code is unaffected by the layer split.
 */
data class QueueUpdate(
    val id: Int = 0,
    val stationId: Int,
    val queueStatus: String,
    val hasFuel: Boolean,
    val fuelType: String,
    val timestamp: Long = System.currentTimeMillis(),
    val userPhone: String,
    val latitude: Double,
    val longitude: Double,
    val photoPath: String? = null,
    val verification: String = "PENDING",
    val verificationNote: String = ""
)
