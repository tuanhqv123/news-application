package com.example.newsapplication.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

class FirebaseTokenReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "FirebaseTokenReceiver"
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            "com.google.firebase.MESSAGING_EVENT" -> {
                // Handle Firebase messaging events
                Log.d(TAG, "Firebase messaging event received")
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    Log.d(TAG, "Firebase token: $token")

                    // Save token to preferences
                    val sharedPreferences = context.getSharedPreferences("FirebasePrefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putString("fcm_token", token).apply()
                }
            }
        }
    }
}