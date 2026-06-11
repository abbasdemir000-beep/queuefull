package com.example.domain.model

/**
 * Server-side view of the current device's user (`users/{uid}` in Firestore).
 *
 * All fields except the profile basics are written exclusively by Cloud
 * Functions — when a backend is configured, this is the authoritative source
 * for role, points, and trust (see AuthPolicy.effectiveRole and
 * docs/BACKEND.md).
 */
data class BackendProfile(
    val uid: String,
    val role: String? = null,
    val points: Int = 0,
    val lifetimePoints: Int = 0,
    val trustScore: Int = 50,
    val banned: Boolean = false,
    val flagged: Boolean = false
)
