package com.example.domain.model

/**
 * Pure domain model for a city.
 *
 * Intentionally free of any framework concerns (no Room, Compose, or Android
 * imports) so it can be shared across Android, iOS (KMP), and future web/backend
 * layers. Field names/types mirror the persistence entity exactly so existing
 * presentation code is unaffected.
 */
data class City(
    val id: Int = 0,
    val nameAr: String,
    val nameEn: String,
    val isApproved: Boolean = true
)
