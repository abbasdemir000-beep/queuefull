package com.example.data.remote

import android.content.Context
import android.net.Uri
import com.example.domain.model.BackendProfile
import com.example.domain.model.QueueUpdate
import com.example.domain.model.Station
import com.example.domain.repository.BackendGateway
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firestore-backed [BackendGateway].
 *
 * Created only when a real `google-services.json` is present —
 * [createIfConfigured] returns null for the committed placeholder config, so
 * the offline demo never touches Firebase. Every method is defensive: a
 * backend failure degrades to local-only behaviour instead of crashing.
 */
class FirebaseBackendGateway private constructor(app: FirebaseApp) : BackendGateway {

    private val auth = FirebaseAuth.getInstance(app)
    private val firestore = FirebaseFirestore.getInstance(app)
    private val storage = FirebaseStorage.getInstance(app)
    private var subscribedCityTopic: String? = null

    companion object {
        private const val PLACEHOLDER_PROJECT_ID = "queuefuel-placeholder"

        /**
         * Null when Firebase is not configured (placeholder or missing
         * google-services.json) — callers then skip all backend work.
         */
        fun createIfConfigured(context: Context): BackendGateway? = try {
            val app = FirebaseApp.getApps(context).firstOrNull()
                ?: FirebaseApp.initializeApp(context)
            val projectId = app?.options?.projectId
            if (app == null || projectId.isNullOrBlank() || projectId == PLACEHOLDER_PROJECT_ID) {
                null
            } else {
                FirebaseBackendGateway(app)
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun signIn(): String? = try {
        auth.currentUser?.uid ?: auth.signInAnonymously().await().user?.uid
    } catch (e: Exception) {
        null
    }

    override fun observeStations(): Flow<List<Station>> = callbackFlow {
        val registration = firestore.collection("stations")
            .whereEqualTo("isApproved", true)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(snapshot.documents.mapNotNull { it.toStation() })
                }
            }
        awaitClose { registration.remove() }
    }

    override fun observeProfile(uid: String): Flow<BackendProfile?> = callbackFlow {
        val registration = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    trySend(if (snapshot.exists()) snapshot.toProfile(uid) else null)
                }
            }
        awaitClose { registration.remove() }
    }

    override suspend fun upsertProfile(
        uid: String,
        name: String,
        phone: String,
        city: String
    ): Boolean = try {
        val doc = firestore.collection("users").document(uid)
        val exists = doc.get().await().exists()
        val data = mutableMapOf<String, Any>(
            "name" to name,
            "phone" to phone,
            "city" to city,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (!exists) {
            // No OTP/SMS in this milestone: identities are anonymous and the
            // phone stays an unverified contact field (see firestore.rules).
            data["phoneVerified"] = false
            data["createdAt"] = FieldValue.serverTimestamp()
        }
        doc.set(data, SetOptions.merge()).await()
        true
    } catch (e: Exception) {
        false
    }

    override suspend fun submitReport(
        uid: String,
        update: QueueUpdate,
        photoSha256: String?
    ): Boolean = try {
        var storagePath: String? = null
        val localPath = update.photoPath
        if (!localPath.isNullOrBlank()) {
            val file = File(localPath)
            if (file.exists()) {
                val path = "reports/$uid/${System.currentTimeMillis()}.jpg"
                storage.reference.child(path).putFile(Uri.fromFile(file)).await()
                storagePath = path
            }
        }
        // Field set must stay in sync with the create rule in firestore.rules.
        firestore.collection("reports").add(
            mapOf(
                "uid" to uid,
                "userPhone" to update.userPhone,
                "stationId" to update.stationId,
                "queueStatus" to update.queueStatus,
                "hasFuel" to update.hasFuel,
                "fuelType" to update.fuelType,
                "latitude" to update.latitude,
                "longitude" to update.longitude,
                "photoPath" to storagePath,
                "photoSha256" to photoSha256,
                "createdAt" to FieldValue.serverTimestamp(),
                "verification" to "PENDING"
            )
        ).await()
        true
    } catch (e: Exception) {
        false
    }

    override suspend fun registerFcmToken(uid: String): Boolean = try {
        val token = FirebaseMessaging.getInstance().token.await()
        firestore.collection("users").document(uid)
            .set(
                mapOf("fcmToken" to token, "updatedAt" to FieldValue.serverTimestamp()),
                SetOptions.merge()
            )
            .await()
        true
    } catch (e: Exception) {
        false
    }

    override suspend fun subscribeToCityTopic(cityId: Int) {
        val topic = "city_$cityId"
        if (topic == subscribedCityTopic) return
        try {
            subscribedCityTopic?.let {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(it).await()
            }
            FirebaseMessaging.getInstance().subscribeToTopic(topic).await()
            subscribedCityTopic = topic
        } catch (e: Exception) {
            // Best-effort: retried on the next city selection.
        }
    }

    private fun DocumentSnapshot.toStation(): Station? = try {
        val stationId = (getLong("id") ?: id.toLongOrNull())?.toInt()
        val name = getString("name")
        val latitude = getDouble("latitude")
        val longitude = getDouble("longitude")
        val cityId = getLong("cityId")?.toInt()
        if (stationId == null || name == null || latitude == null || longitude == null || cityId == null) {
            null
        } else {
            Station(
                id = stationId,
                cityId = cityId,
                cityName = getString("cityName") ?: "",
                name = name,
                latitude = latitude,
                longitude = longitude,
                type = getString("type") ?: "حكومية",
                fuelTypes = getString("fuelTypes") ?: "",
                queueStatus = getString("queueStatus") ?: "EMPTY",
                lastUpdated = getLong("lastUpdated") ?: System.currentTimeMillis(),
                confirmedCount = (getLong("confirmedCount") ?: 1L).toInt(),
                hasFuel = getBoolean("hasFuel") ?: true,
                isApproved = getBoolean("isApproved") ?: true,
                suggestedBy = getString("suggestedBy")
            )
        }
    } catch (e: Exception) {
        null
    }

    private fun DocumentSnapshot.toProfile(uid: String): BackendProfile = BackendProfile(
        uid = uid,
        role = getString("role"),
        points = (getLong("points") ?: 0L).toInt(),
        lifetimePoints = (getLong("lifetimePoints") ?: 0L).toInt(),
        trustScore = (getLong("trustScore") ?: 50L).toInt(),
        banned = getBoolean("banned") ?: false,
        flagged = getBoolean("flagged") ?: false
    )
}
