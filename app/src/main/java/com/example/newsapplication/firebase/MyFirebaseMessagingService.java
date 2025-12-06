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
    private static final String CHANNEL_ID = "article_notifications";

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

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());
            handleDataMessage(remoteMessage.getData());
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotificationMessage(remoteMessage.getNotification());
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
        String title = data.get("title");
        String message = data.get("message");
        String type = data.get("type");

        if (title != null && message != null) {
            showNotification(title, message, type, data);
        }
    }

    private void handleNotificationMessage(RemoteMessage.Notification notification) {
        String title = notification.getTitle();
        String message = notification.getBody();

        if (title != null && message != null) {
            showNotification(title, message, null, null);
        }
    }

    private void showNotification(String title, String message, String type, Map<String, String> data) {
        // Create notification channel for Android 8+
        createNotificationChannel();

        // Create intent to open app when notification is tapped
        Intent intent = new Intent(this, MainActivity.class);
        if (data != null && data.containsKey("article_id")) {
            intent.putExtra("article_id", data.get("article_id"));
            intent.putExtra("notification_type", type);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
                .setContentTitle(title)
                .setContentText(message)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent);

        // Show notification
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(getNotificationId(), builder.build());

        Log.d(TAG, "Notification shown: " + title);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Article Notifications";
            String description = "Notifications for article updates";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

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