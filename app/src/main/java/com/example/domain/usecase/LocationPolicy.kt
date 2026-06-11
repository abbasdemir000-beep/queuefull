package com.example.domain.usecase

/**
 * Decides which coordinates the app should treat as the user's position.
 *
 * Pure and framework-free. A fresh real GPS fix always wins; the map-screen
 * simulation remains the fallback for devices without permission, without a
 * fix, and for testing. The legacy "bypass the geofence when simulation is
 * off" shortcut is only legitimate while no real fix exists — once the device
 * reports a real position, proximity checks must be enforced with it.
 */
object LocationPolicy {

    /** A real fix older than this (5 minutes) is stale and falls back to simulation. */
    const val MAX_FIX_AGE_MS: Long = 5L * 60 * 1000

    /** A device-reported position with the time it was obtained. */
    data class RealFix(
        val latitude: Double,
        val longitude: Double,
        val timestampMs: Long
    )

    /** The position the app should use, flagged with its origin. */
    data class Coordinates(
        val latitude: Double,
        val longitude: Double,
        val isReal: Boolean
    )

    fun isFresh(fix: RealFix, nowMs: Long): Boolean =
        nowMs - fix.timestampMs <= MAX_FIX_AGE_MS

    fun effectiveCoordinates(
        realFix: RealFix?,
        simLatitude: Double,
        simLongitude: Double,
        nowMs: Long
    ): Coordinates =
        if (realFix != null && isFresh(realFix, nowMs)) {
            Coordinates(realFix.latitude, realFix.longitude, isReal = true)
        } else {
            Coordinates(simLatitude, simLongitude, isReal = false)
        }

    /**
     * Whether a proximity check may be skipped entirely. Only allowed while
     * the position is simulated AND the simulation toggle is off (the MVP's
     * developer shortcut); a real fix always enforces the geofence.
     */
    fun mayBypassGeofence(coordinates: Coordinates, simulationEnabled: Boolean): Boolean =
        !coordinates.isReal && !simulationEnabled
}
