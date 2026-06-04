package com.example.domain.model

/**
 * Pure domain model for an application user.
 *
 * Framework-agnostic by design. Field names/types mirror the persistence entity
 * exactly so existing presentation code is unaffected by the layer split.
 */
data class AppUser(
    val phoneNumber: String,
    val name: String,
    val role: String = "USER", // "USER", "REPORTER", "ADMIN"
    val points: Int = 0,
    val banned: Boolean = false
)
