package com.example.domain.model

/**
 * Pure domain model for a city-targeted advertisement banner.
 *
 * Framework-agnostic by design. Field names/types mirror the persistence entity
 * exactly so existing presentation code is unaffected by the layer split.
 */
data class AdBanner(
    val id: Int = 0,
    val title: String,
    val description: String,
    val city: String,
    val category: String, // "زيوت سيارات", "كراج صيانة", "مطاعم", "غسيل سيارات"
    val ctaPhone: String? = null,
    val imageUrl: String? = null
)
