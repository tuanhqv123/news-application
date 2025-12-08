package com.example.newsapplication.auth;

import android.content.Context;
import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.api.endpoints.AuthEndpoints;
import org.json.JSONException;
import org.json.JSONObject;

public class AuthService {
    private final Context context;
    private final UserSessionManager sessionManager;
    private final ApiClient apiClient;
    private final AuthEndpoints authEndpoints;

    public interface AuthResultCallback {
        void onSuccess(JSONObject response);
        void onError(String errorMessage);
    }

    public AuthService(Context context) {
        this.context = context;
        this.sessionManager = new UserSessionManager(context);
        this.apiClient = new ApiClient(context);
        this.authEndpoints = new AuthEndpoints(apiClient);
    }

    public void login(String email, String password, AuthResultCallback callback) {
        authEndpoints.login(email, password, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                try {
                    JSONObject data = response.getData();
                    
                    if (data != null && data.has("success") && data.getBoolean("success") && data.has("data")) {
                        JSONObject responseData = data.getJSONObject("data");
                        
                        String token = responseData.getString("access_token");
                        String refreshToken = responseData.optString("refresh_token", null);
                        JSONObject userData = responseData.getJSONObject("user");
                        
                        String userEmail = userData.optString("email", email);
                        String displayName = userData.optString("display_name", email.split("@")[0]);
                        String role = userData.optString("role", "reader");
                        String avatarUrl = userData.optString("avatar_url", null);
                        String userId = userData.optString("user_id", userData.optString("id", null));

                        // Debug logging
                        android.util.Log.d("AuthService", "Login successful - User: " + userEmail);
                        android.util.Log.d("AuthService", "Avatar URL: " + avatarUrl);
                        android.util.Log.d("AuthService", "User ID: " + userId);

                        apiClient.setAuthToken(token);
                        if (refreshToken != null) {
                            sessionManager.createLoginSession(userEmail, displayName, role, token, refreshToken);
                        } else {
                            sessionManager.createLoginSession(userEmail, displayName, role, token);
                        }
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            android.util.Log.d("AuthService", "Saving avatar URL to session: " + avatarUrl);
                            sessionManager.setAvatarUrl(avatarUrl);
                        } else {
                            android.util.Log.w("AuthService", "No avatar URL found in response");
                        }
                        // Save user UUID for notifications
                        if (userId != null && !userId.isEmpty()) {
                            com.example.newsapplication.notifications.NotificationManager.getInstance(context).saveUserId(userId);
                        }
                        
                        String message = data.optString("message", "Login successful");
                        
                        JSONObject responseWithMessage = new JSONObject();
                        try {
                            responseWithMessage.put("user", userData);
                            responseWithMessage.put("message", message);
                        } catch (JSONException e) {
                        }
                        
                        callback.onSuccess(responseWithMessage);
                    } else if (data != null && data.has("access_token")) {
                        String token = data.getString("access_token");
                        apiClient.setAuthToken(token);
                        sessionManager.createLoginSession(email, email.split("@")[0], "reader", token);
                        
                        callback.onSuccess(data);
                    } else {
                        callback.onSuccess(data);
                    }
                } catch (JSONException e) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                callback.onError(error.getErrorMessage());
            }
        });
    }

    public void register(String email, String password, String displayName, AuthResultCallback callback) {
        authEndpoints.register(email, password, displayName, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                callback.onSuccess(response.getData());
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                callback.onError(error.getErrorMessage());
            }
        });
    }

    public void logout(AuthResultCallback callback) {
        android.util.Log.d("AuthService", "Starting logout process");

        // Get current FCM token before logout with timeout
        com.google.firebase.messaging.FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    final String fcmToken = task.isSuccessful() && task.getResult() != null ? task.getResult() : null;
                    android.util.Log.d("AuthService", "FCM token retrieved: " + (fcmToken != null ? "success" : "failed"));

                    // Logout API call with optional fcm_token
                    authEndpoints.logout(fcmToken, new ApiClient.ApiCallback<JSONObject>() {
                        @Override
                        public void onSuccess(ApiResponse<JSONObject> response) {
                            android.util.Log.d("AuthService", "Logout API call successful");
                            sessionManager.logoutUser();
                            apiClient.clearAuthToken();

                            // Reset notification token to guest mode
                            if (fcmToken != null) {
                                com.example.newsapplication.notifications.NotificationManager.getInstance(context)
                                    .resetTokenToGuest(fcmToken);
                            }

                            callback.onSuccess(response.getData());
                        }

                        @Override
                        public void onError(ApiResponse<JSONObject> error) {
                            android.util.Log.e("AuthService", "Logout API call failed: " + error.getErrorMessage());
                            // Force logout even if API fails
                            sessionManager.logoutUser();
                            apiClient.clearAuthToken();

                            // Still reset token to guest mode even if logout API fails
                            if (fcmToken != null) {
                                com.example.newsapplication.notifications.NotificationManager.getInstance(context)
                                    .resetTokenToGuest(fcmToken);
                            }

                            callback.onSuccess(null);
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AuthService", "Failed to get FCM token: " + e.getMessage());
                    // Proceed with logout without FCM token
                    authEndpoints.logout(null, new ApiClient.ApiCallback<JSONObject>() {
                        @Override
                        public void onSuccess(ApiResponse<JSONObject> response) {
                            android.util.Log.d("AuthService", "Logout API call successful (without FCM)");
                            sessionManager.logoutUser();
                            apiClient.clearAuthToken();
                            callback.onSuccess(response.getData());
                        }

                        @Override
                        public void onError(ApiResponse<JSONObject> error) {
                            android.util.Log.e("AuthService", "Logout API call failed: " + error.getErrorMessage());
                            // Force logout even if API fails
                            sessionManager.logoutUser();
                            apiClient.clearAuthToken();
                            callback.onSuccess(null);
                        }
                    });
                });
    }

    public void getCurrentUser(AuthResultCallback callback) {
        authEndpoints.getMe(new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                try {
                    JSONObject data = response.getData();
                    JSONObject userData = data;
                    
                    if (data != null && data.has("success") && data.optBoolean("success", false) && data.has("data")) {
                        JSONObject responseData = data.getJSONObject("data");
                        if (responseData.has("user")) {
                            userData = responseData.getJSONObject("user");
                        }
                    }
                    
                    if (userData != null) {
                        String email = userData.optString("email", "");
                        String displayName = userData.optString("display_name", "");
                        String role = userData.optString("role", "reader");
                        String avatarUrl = userData.optString("avatar_url", null);
                        
                        sessionManager.createLoginSession(email, displayName, role);
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            sessionManager.setAvatarUrl(avatarUrl);
                        }
                    }
                    callback.onSuccess(userData);
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                callback.onError(error.getErrorMessage());
            }
        });
    }



    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public void refreshToken(AuthResultCallback callback) {
        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken == null) {
            android.util.Log.d("AuthService", "No refresh token available");
            callback.onError("No refresh token");
            return;
        }

        android.util.Log.d("AuthService", "Attempting to refresh token");
        authEndpoints.refreshToken(refreshToken, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                try {
                    JSONObject data = response.getData();
                    if (data != null && data.has("success") && data.getBoolean("success") && data.has("data")) {
                        JSONObject responseData = data.getJSONObject("data");
                        String newToken = responseData.getString("access_token");
                        String newRefreshToken = responseData.optString("refresh_token", refreshToken);

                        android.util.Log.d("AuthService", "Token refresh successful");

                        // Update stored tokens
                        apiClient.setAuthToken(newToken);
                        sessionManager.setAuthToken(newToken);
                        sessionManager.setRefreshToken(newRefreshToken);

                        callback.onSuccess(responseData);
                    } else {
                        android.util.Log.e("AuthService", "Invalid response format in token refresh");
                        // Clear tokens on invalid response
                        sessionManager.logoutUser();
                        apiClient.clearAuthToken();
                        callback.onError(data != null ? data.toString() : "Invalid response");
                    }
                } catch (Exception e) {
                    android.util.Log.e("AuthService", "Error parsing refresh token response", e);
                    // Clear tokens on parse error
                    sessionManager.logoutUser();
                    apiClient.clearAuthToken();
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                android.util.Log.e("AuthService", "Refresh token API failed with status " + error.getStatusCode() + ": " + error.getErrorMessage());

                // If it's a 401, clear tokens and don't retry
                if (error.getStatusCode() == 401) {
                    android.util.Log.d("AuthService", "Refresh token is invalid/expired, clearing session");
                    sessionManager.logoutUser();
                    apiClient.clearAuthToken();
                    callback.onError("Session expired. Please log in again.");
                } else {
                    // For other errors, also clear tokens to prevent loops
                    sessionManager.logoutUser();
                    apiClient.clearAuthToken();
                    callback.onError(error.getErrorMessage());
                }
            }
        });
    }
    
    public void validateToken(AuthResultCallback callback) {
        if (!sessionManager.isLoggedIn() || sessionManager.getAuthToken() == null) {
            // Don't validate if not logged in
            return;
        }
        
        getCurrentUser(new AuthResultCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                sessionManager.logoutUser();
                apiClient.clearAuthToken();
                callback.onError(errorMessage);
            }
        });
    }

    public void getUserBookmarks(AuthResultCallback callback) {
        authEndpoints.getMe(new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                callback.onSuccess(response.getData());
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                callback.onError(error.getErrorMessage());
            }
        });
    }

    public UserSessionManager getSessionManager() {
        return sessionManager;
    }
}
