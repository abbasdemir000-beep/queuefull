package com.example.domain.repository

import com.example.domain.model.BackendProfile
import com.example.domain.model.QueueUpdate
import com.example.domain.model.Station
import kotlinx.coroutines.flow.Flow

/**
 * Boundary to the real backend (Firestore + Cloud Functions + FCM).
 *
 * Identity is anonymous/device-based (Firebase Anonymous Auth) — no OTP, no
 * SMS, no phone verification. The backend owns everything valuable: it
 * re-verifies every report server-side and is the only writer of roles,
 * points, trust scores, and raffle entries. The local Room database remains
 * the app's working store; remote stations are mirrored into it so every
 * screen keeps working unchanged (and fully offline when no backend is
 * configured). See docs/BACKEND.md.
 */
interface BackendGateway {

    /** Signs in with the device-scoped anonymous identity; uid or null on failure. */
    suspend fun signIn(): String?

    /** Live snapshots of the approved stations collection (server wins). */
    fun observeStations(): Flow<List<Station>>

    /** Live snapshots of this device's server profile (role/points/trust/ban). */
    fun observeProfile(uid: String): Flow<BackendProfile?>

    /** Creates or updates the client-writable profile fields for this uid. */
    suspend fun upsertProfile(uid: String, name: String, phone: String, city: String): Boolean

    /**
     * Uploads the report photo and appends the report as PENDING; the
     * onReportCreated Cloud Function issues the authoritative verdict.
     */
    suspend fun submitReport(uid: String, update: QueueUpdate, photoSha256: String?): Boolean

    /** Stores the current FCM token on the profile for direct pushes. */
    suspend fun registerFcmToken(uid: String): Boolean

    /** Moves the FCM topic subscription to the given city. */
    suspend fun subscribeToCityTopic(cityId: Int)
}
