package com.example.newsapplication.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.newsapplication.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
        private const val CHANNEL_ID = "news_notification_channel"
        private const val CHANNEL_NAME = "News Notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")

        // Save the token to preferences or send to your server
        saveTokenToPreferences(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
            showNotification(it.title ?: "News Update", it.body ?: "New article available")
        }

        // Check if message contains data payload
        remoteMessage.data.isNotEmpty().let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)

            // Extract data and handle accordingly
            val title = remoteMessage.data["title"] ?: "News Update"
            val body = remoteMessage.data["body"] ?: "New article available"
            val articleUrl = remoteMessage.data["article_url"]

            showNotification(title, body, articleUrl)
        }
    }

    private fun showNotification(title: String, body: String, articleUrl: String? = null) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android 8.0 (API 26) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for news notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Build notification
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Make sure this icon exists
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // You can add intent to open specific article if articleUrl is provided
        // For now, just show basic notification

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun saveTokenToPreferences(token: String) {
        val sharedPreferences = getSharedPreferences("FirebasePrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("fcm_token", token).apply()
        Log.d(TAG, "Token saved to preferences")
    }
}