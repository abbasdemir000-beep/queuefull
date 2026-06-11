package com.example.data.remote

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM entry point: shows station-update pushes sent by the Cloud Functions
 * (city topics + direct token messages) and keeps the profile's token fresh.
 *
 * Only ever invoked by FCM itself, which requires a real Firebase project —
 * with the placeholder google-services.json this service stays dormant and
 * the in-app notifications log remains the offline demo's only channel.
 */
class QueueFuelMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        // VM start-up also registers the token; this covers mid-session rotation.
        val uid = runCatching { FirebaseAuth.getInstance().currentUser?.uid }.getOrNull() ?: return
        runCatching {
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .set(
                    mapOf("fcmToken" to token, "updatedAt" to FieldValue.serverTimestamp()),
                    SetOptions.merge()
                )
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body = message.notification?.body ?: message.data["body"] ?: ""
        showNotification(title, body)
    }

    private fun showNotification(title: String, body: String) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "تحديثات المحطات",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_qf_logo)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        try {
            manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS denied on API 33+ — drop silently.
        }
    }

    companion object {
        const val CHANNEL_ID = "queuefuel_updates"
    }
}
