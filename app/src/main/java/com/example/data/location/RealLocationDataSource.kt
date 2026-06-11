package com.example.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.example.domain.usecase.LocationPolicy
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Real device location via FusedLocationProvider.
 *
 * Best-effort: returns null when permission is missing, location services are
 * off, Play Services is unavailable, or no fix can be obtained — callers fall
 * back to the simulated position (see [LocationPolicy]). Kept out of the
 * domain layer so policies stay framework-free and unit-testable.
 */
class RealLocationDataSource(context: Context) {

    private val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)

    /**
     * Asks for a fresh fix first (balanced power), then falls back to the
     * last cached location. The caller is responsible for ensuring the
     * location permission was granted before invoking this.
     */
    @SuppressLint("MissingPermission")
    suspend fun currentFix(nowMs: Long = System.currentTimeMillis()): LocationPolicy.RealFix? {
        val location = try {
            requestCurrentLocation() ?: requestLastLocation()
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
        return location?.let { LocationPolicy.RealFix(it.latitude, it.longitude, nowMs) }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestCurrentLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            val cts = CancellationTokenSource()
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
                .addOnCanceledListener { cont.resume(null) }
            cont.invokeOnCancellation { cts.cancel() }
        }

    @SuppressLint("MissingPermission")
    private suspend fun requestLastLocation(): Location? =
        suspendCancellableCoroutine { cont ->
            client.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
                .addOnCanceledListener { cont.resume(null) }
        }
}
