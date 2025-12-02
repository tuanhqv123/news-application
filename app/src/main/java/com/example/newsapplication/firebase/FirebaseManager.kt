package com.example.newsapplication.firebase

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging

class FirebaseManager private constructor() {

    companion object {
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

        @Volatile
        private var INSTANCE: FirebaseManager? = null

        fun getInstance(): FirebaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseManager().also { INSTANCE = it }
            }
        }
    }

    // Get the current FCM token
    fun getCurrentToken(context: Context, callback: (String?) -> Unit) {
        val sharedPreferences = context.getSharedPreferences("FirebasePrefs", Context.MODE_PRIVATE)
        val savedToken = sharedPreferences.getString("fcm_token", null)

        if (savedToken != null) {
            callback(savedToken)
            return
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                callback(null)
                return@OnCompleteListener
            }

            val token = task.result
            sharedPreferences.edit().putString("fcm_token", token).apply()
            callback(token)
        })
    }

    // Check if notification permission is granted (for Android 13+)
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Request notification permission (for Android 13+)
    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    // Subscribe to a topic
    fun subscribeToTopic(topic: String, callback: (Boolean) -> Unit = {}) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    // Unsubscribe from a topic
    fun unsubscribeFromTopic(topic: String, callback: (Boolean) -> Unit = {}) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task ->
                callback(task.isSuccessful)
            }
    }

    // Get device information
    fun getDeviceInfo(context: Context): Map<String, String> {
        return mapOf(
            "device_model" to "${Build.MANUFACTURER} ${Build.MODEL}",
            "os_version" to "Android ${Build.VERSION.RELEASE}",
            "app_version" to getAppVersion(context)
        )
    }

    private fun getAppVersion(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val versionCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
            "${packageInfo.versionName} ($versionCode)"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}