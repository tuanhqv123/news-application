package com.example.newsapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.newsapplication.firebase.FirebaseManager

class FirebaseSetupActivity : AppCompatActivity() {

    private lateinit var firebaseManager: FirebaseManager
    private lateinit var tokenTextView: TextView
    private lateinit var getNewTokenButton: Button
    private lateinit var subscribeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firebase_setup)

        firebaseManager = FirebaseManager.getInstance()

        initViews()
        checkNotificationPermission()
        getCurrentToken()
    }

    private fun initViews() {
        tokenTextView = findViewById(R.id.tokenTextView)
        getNewTokenButton = findViewById(R.id.getNewTokenButton)
        subscribeButton = findViewById(R.id.subscribeButton)

        getNewTokenButton.setOnClickListener {
            getCurrentToken()
        }

        subscribeButton.setOnClickListener {
            subscribeToNewsTopic()
        }
    }

    private fun checkNotificationPermission() {
        if (!firebaseManager.hasNotificationPermission(this)) {
            firebaseManager.requestNotificationPermission(this)
        }
    }

    private fun getCurrentToken() {
        tokenTextView.text = "Getting token..."

        firebaseManager.getCurrentToken(this) { token ->
            runOnUiThread {
                if (token != null) {
                    tokenTextView.text = "FCM Token:\n$token"

                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    val clip = android.content.ClipData.newPlainText("FCM Token", token)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(this, "Token copied to clipboard!", Toast.LENGTH_SHORT).show()
                } else {
                    tokenTextView.text = "Failed to get FCM token"
                    Toast.makeText(this, "Failed to get token", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun subscribeToNewsTopic() {
        firebaseManager.subscribeToTopic("news_updates") { success ->
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "Subscribed to news updates", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to subscribe", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            FirebaseManager.NOTIFICATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}