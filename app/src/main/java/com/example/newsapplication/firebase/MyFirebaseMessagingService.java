package com.example.newsapplication.firebase;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.newsapplication.MainActivity;
import com.example.newsapplication.R;
import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.endpoints.NotificationEndpoints;
import com.example.newsapplication.auth.UserSessionManager;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "news_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM Token received: " + token);

        // Register token with your API
        registerTokenWithServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        String title = null;
        String message = null;

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            // Extract title and message from data if available
            title = remoteMessage.getData().get("title");
            message = remoteMessage.getData().get("body");

            handleDataMessage(remoteMessage.getData());
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            // Use notification payload if data doesn't have title/message
            if (title == null) title = remoteMessage.getNotification().getTitle();
            if (message == null) message = remoteMessage.getNotification().getBody();

            // Show notification with data from remoteMessage.getData()
            if (title != null && message != null) {
                showNotification(title, message, remoteMessage.getData().get("type"), remoteMessage.getData());
            }
        }
    }

    private void registerTokenWithServer(String token) {
        // Get user ID (if logged in)
        String userId = getUserId(); // Implement this method to get current user ID

        NotificationEndpoints notificationEndpoints = new NotificationEndpoints(
                new ApiClient(this)
        );

        notificationEndpoints.registerToken(token, "android", new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                Log.d(TAG, "Token registered successfully");
            }

            @Override
            public void onError(com.example.newsapplication.api.ApiResponse<JSONObject> error) {
                Log.e(TAG, "Failed to register token: " + error.getErrorMessage());
            }
        });
    }

    private void handleDataMessage(Map<String, String> data) {
        Log.d(TAG, "handleDataMessage called with: " + data);

        String title = data.get("title");
        String message = data.get("body"); // Changed from "message" to "body"
        String type = data.get("type");

        Log.d(TAG, "Extracted - title: " + title + ", message: " + message + ", type: " + type);

        // Show notification even if we don't have all data
        if (title != null && message != null) {
            showNotification(title, message, type, data);
        } else if (title != null) {
            // Fallback: show notification with just title
            showNotification(title, "Tap to open article", type, data);
        } else {
            Log.w(TAG, "No title found in notification data");
        }
    }

  private void showNotification(String title, String message, String type, Map<String, String> data) {
        // Create notification channel for Android 8+
        createNotificationChannel();

        Log.d(TAG, "Creating notification with data: " + data);

        // Create intent to open app when notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        if (data != null) {
            // Add all data fields to intent
            for (Map.Entry<String, String> entry : data.entrySet()) {
                intent.putExtra(entry.getKey(), entry.getValue());
                Log.d(TAG, "Adding extra: " + entry.getKey() + " = " + entry.getValue());
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

        // Use unique request code to avoid issues
        int requestCode = (int) System.currentTimeMillis();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Log.d(TAG, "Created PendingIntent with requestCode: " + requestCode);

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Using default Android icon
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Higher priority for better visibility
                .setDefaults(NotificationCompat.DEFAULT_ALL) // Use default sound, vibration, lights
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)); // Support long text

        // Show notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(getNotificationId(), builder.build());

        Log.d(TAG, "Notification shown: " + title);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Article Notifications";
            String description = "Notifications for article updates";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private int getNotificationId() {
        return (int) System.currentTimeMillis();
    }

    private String getUserId() {
        // Get user ID from session manager
        UserSessionManager sessionManager = new UserSessionManager(this);
        if (sessionManager.isLoggedIn()) {
            // For guest users, API accepts user_id as null
            // For logged in users, you might want to get the actual user ID from your API
            // For now, we'll return email as a placeholder
            return sessionManager.getUserEmail();
        }
        return null; // Guest user
    }
}