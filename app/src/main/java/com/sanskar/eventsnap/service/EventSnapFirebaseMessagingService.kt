package com.sanskar.eventsnap.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sanskar.eventsnap.MainActivity
import com.sanskar.eventsnap.R

class EventSnapFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "New token: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "EventSnap"
        val message = remoteMessage.notification?.body
            ?: remoteMessage.data["message"]
            ?: "You have a new notification"

        // If there is nothing meaningful to show, bail out.
        if (message.isBlank()) return

        runCatching { sendNotification(title, message) }
            .onFailure { Log.e("FCM", "Failed to show notification", it) }
    }

    private fun sendNotification(title: String, message: String) {
        val channelId = DEFAULT_CHANNEL_ID

        // Create notification channel (required for 26+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "EventSnap Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for EventSnap app"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        // Android 13+ requires POST_NOTIFICATIONS runtime permission.
        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {
                Log.w("FCM", "POST_NOTIFICATIONS not granted; skipping notification")
                return
            }
        }

        postNotification(notification)
    }

    @SuppressLint("MissingPermission")
    private fun postNotification(notification: android.app.Notification) {
        // Still guard with try/catch for edge cases.
        runCatching {
            NotificationManagerCompat.from(this)
                .notify((System.currentTimeMillis() and 0x7FFFFFFF).toInt(), notification)
        }.onFailure {
            Log.w("FCM", "Notification not posted", it)
        }
    }

    private companion object {
        const val DEFAULT_CHANNEL_ID = "eventsnap_channel"
    }
}
