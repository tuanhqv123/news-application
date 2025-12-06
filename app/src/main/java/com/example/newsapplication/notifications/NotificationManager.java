package com.example.newsapplication.notifications;

import android.content.Context;
import android.util.Log;

import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.endpoints.NotificationEndpoints;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

public class NotificationManager {
    private static final String TAG = "NotificationManager";
    private static NotificationManager instance;
    private final Context context;
    private final NotificationEndpoints notificationEndpoints;

    private NotificationManager(Context context) {
        this.context = context.getApplicationContext();
        this.notificationEndpoints = new NotificationEndpoints(new ApiClient(this.context));
    }

    public static synchronized NotificationManager getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationManager(context);
        }
        return instance;
    }

    /**
     * Initialize notification system - call this when app starts
     */
    public void initialize() {
        Log.d(TAG, "Initializing notification system");

        // Get FCM token and register with server
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String token = task.getResult();
                    Log.d(TAG, "FCM Token: " + token);

                    // Only register if token is new or user has changed
                    if (!isTokenAlreadyRegistered(token) || hasUserChanged()) {
                        registerTokenWithServer(token);
                    } else {
                        Log.d(TAG, "Token already registered, skipping");
                    }
                });
    }

    /**
     * Call this after user logs in to update token with user ID
     */
    public void onUserLoggedIn() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String token = task.getResult();
                        Log.d(TAG, "Updating token after login");
                        updateTokenWithUser(token);
                    }
                });
    }

    /**
     * Check if user has changed (guest -> logged in)
     */
    private boolean hasUserChanged() {
        android.content.SharedPreferences prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        boolean wasLoggedIn = prefs.getBoolean("was_logged_in", false);
        boolean isLoggedIn = new com.example.newsapplication.auth.UserSessionManager(context).isLoggedIn();

        if (wasLoggedIn != isLoggedIn) {
            prefs.edit().putBoolean("was_logged_in", isLoggedIn).apply();
            return true;
        }
        return false;
    }

    /**
     * Register device token with your API
     */
    private void registerTokenWithServer(String token) {
        // Get current user ID (null for guest users)
        String userId = getCurrentUserId();

        notificationEndpoints.setToken(token, "android", userId, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                Log.d(TAG, "Device token set successfully" + (userId != null ? " with user ID: " + userId : " as guest"));
                // Save token locally to avoid re-registering
                saveTokenLocally(token);
            }

            @Override
            public void onError(com.example.newsapplication.api.ApiResponse<JSONObject> error) {
                Log.e(TAG, "Failed to set device token: " + error.getErrorMessage());
                // Retry after delay if failed
                retryTokenRegistration(token);
            }
        });
    }

    /**
     * Retry token registration with delay
     */
    private void retryTokenRegistration(String token) {
        // Retry after 5 seconds
        new android.os.Handler().postDelayed(() -> {
            Log.d(TAG, "Retrying token registration...");
            registerTokenWithServer(token);
        }, 5000);
    }

    /**
     * Get all registered devices for current user
     */
    public void getMyDevices(ApiClient.ApiCallback<JSONObject> callback) {
        notificationEndpoints.getMyDevices(callback);
    }

    /**
     * Update token with user ID after login
     */
    public void updateTokenWithUser(String token) {
        String userId = getCurrentUserId();

        notificationEndpoints.setToken(token, "android", userId, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                Log.d(TAG, "Token updated with user ID: " + userId);
                saveTokenLocally(token);
            }

            @Override
            public void onError(com.example.newsapplication.api.ApiResponse<JSONObject> error) {
                Log.e(TAG, "Failed to update token with user ID: " + error.getErrorMessage());
            }
        });
    }

    /**
     * Reset token to guest mode (user_id = null) after logout
     */
    public void resetTokenToGuest(String token) {
        notificationEndpoints.setToken(token, "android", null, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                Log.d(TAG, "Token reset to guest mode successfully");
            }

            @Override
            public void onError(com.example.newsapplication.api.ApiResponse<JSONObject> error) {
                Log.e(TAG, "Failed to reset token to guest mode: " + error.getErrorMessage());
            }
        });
    }

    /**
     * Get current user ID (returns null if not logged in)
     */
    private String getCurrentUserId() {
        com.example.newsapplication.auth.UserSessionManager sessionManager =
            new com.example.newsapplication.auth.UserSessionManager(context);

        if (sessionManager.isLoggedIn()) {
            // TODO: Return actual user UUID from your API
            // For now, we can use email as a placeholder
            // You should modify UserSessionManager to store the actual user_id from your API
            String userEmail = sessionManager.getUserEmail();

            // Check if we have a UUID format stored
            android.content.SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String userId = prefs.getString("user_uuid", null);

            if (userId != null) {
                return userId;
            }

            // Fallback to email (but this won't work with your API which expects UUID)
            Log.w(TAG, "User UUID not found, using email as fallback. Consider storing user_id from API response.");
            return userEmail;
        }
        return null; // Guest user
    }

    /**
     * Save user UUID from API response
     */
    public void saveUserId(String userId) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("user_uuid", userId).apply();
        Log.d(TAG, "Saved user ID: " + userId);
    }

    /**
     * Save token locally to avoid re-registering
     */
    private void saveTokenLocally(String token) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("last_registered_token", token).apply();
    }

    /**
     * Check if token is already registered
     */
    private boolean isTokenAlreadyRegistered(String token) {
        android.content.SharedPreferences prefs = context.getSharedPreferences("NotificationPrefs", Context.MODE_PRIVATE);
        String lastToken = prefs.getString("last_registered_token", "");
        return token.equals(lastToken);
    }
}