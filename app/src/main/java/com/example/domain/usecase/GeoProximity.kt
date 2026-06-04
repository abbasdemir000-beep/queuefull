package com.example.domain.usecase

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Pure geo-distance / proximity logic, extracted verbatim from the ViewModel.
 *
 * Framework-agnostic (no Android/Compose). Uses [Math.toRadians] (JVM stdlib, not
 * Android-specific) so output is bit-for-bit identical to the previous inline
 * implementation. A KMP-pure variant can replace `Math.toRadians` with
 * `deg / 180.0 * PI` later if/when we share this with iOS.
 */
object GeoProximity {

    /** Default radius (meters) within which a user may report/confirm a station. */
    const val DEFAULT_RADIUS_METERS: Double = 200.0

    /** Earth radius in meters used by the Haversine formula. */
    private const val EARTH_RADIUS_METERS: Double = 6371000.0

    /**
     * Great-circle distance in meters between two coordinates (Haversine).
     * Identical formula to the original `QueueFuelViewModel.calculateDistance`.
     */
    fun distanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    /**
     * True when the point (lat1, lon1) is within [radiusMeters] of (lat2, lon2).
     * Mirrors the original `dist <= 200.0` check (inclusive boundary).
     */
    fun isWithinRadius(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double,
        radiusMeters: Double = DEFAULT_RADIUS_METERS
    ): Boolean = distanceMeters(lat1, lon1, lat2, lon2) <= radiusMeters
}
