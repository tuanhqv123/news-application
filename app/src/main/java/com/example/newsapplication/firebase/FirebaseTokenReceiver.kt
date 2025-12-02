package com.example.newsapplication.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.firebase.iid.FirebaseInstanceId

class FirebaseTokenReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return

        when (intent.action) {
            "com.google.firebase.INSTANCE_ID_EVENT" -> {
                FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener { instanceIdResult ->
                    val token = instanceIdResult.token
                    val sharedPreferences = context.getSharedPreferences("FirebasePrefs", Context.MODE_PRIVATE)
                    sharedPreferences.edit().putString("fcm_token", token).apply()
                }
            }
        }
    }
}