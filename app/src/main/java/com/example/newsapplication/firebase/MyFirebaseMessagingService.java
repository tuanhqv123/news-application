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
import com.example.newsapplication.database.NotificationHistoryHelper;
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

        // Get notification payload first (if available)
        String notificationTitle = null;
        String notificationMessage = null;
        if (remoteMessage.getNotification() != null) {
            notificationTitle = remoteMessage.getNotification().getTitle();
            notificationMessage = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification payload - Title: " + notificationTitle + ", Message: " + notificationMessage);
        }

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData(), notificationTitle, notificationMessage);
        } else if (notificationTitle != null && notificationMessage != null) {
            // Only notification payload, no data - still show it
            showNotification(notificationTitle, notificationMessage, null, null);
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

    private void handleDataMessage(Map<String, String> data, String notificationTitle, String notificationMessage) {
        Log.d(TAG, "handleDataMessage called with: " + data);

        // Use notification title/message as primary, fallback to data
        String title = notificationTitle != null ? notificationTitle : data.get("title");
        String message = notificationMessage != null ? notificationMessage : data.get("body");
        String type = data.get("type");
        String articleId = data.get("article_id");
        String channelId = data.get("channel_id");
        String screen = data.get("screen");

        Log.d(TAG, "Extracted - title: " + title + ", message: " + message + ", type: " + type);

        // Save notification to local database with the actual notification title/message
        saveNotificationToHistory(articleId, title, message, type, channelId, screen, data.toString());

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

    private void saveNotificationToHistory(String articleId, String title, String message,
                                       String type, String channelId, String screen, String data) {
        try {
            NotificationHistoryHelper dbHelper = NotificationHistoryHelper.getInstance(this);
            dbHelper.saveNotification(articleId, title, message, type, channelId, screen, data);
            Log.d(TAG, "Notification saved to history: " + title);
        } catch (Exception e) {
            Log.e(TAG, "Error saving notification to history", e);
        }
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