package com.example.newsapplication.api.endpoints;

import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.utils.JsonParsingUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationEndpoints {
    private final ApiClient apiClient;

    public NotificationEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Register device token for push notifications
     * Note: Your backend should support both creating and updating tokens
     */
    public void registerToken(String fcmToken, String deviceType, ApiClient.ApiCallback<JSONObject> callback) {
        setToken(fcmToken, deviceType, null, callback);
    }

    /**
     * Register or update a device token for push notifications with optional user_id
     * This method can handle both guest users (user_id = null) and logged-in users
     */
    public void setToken(String fcmToken, String deviceType, String userId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/notifications/set-token";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("fcm_token", fcmToken);
            if (deviceType != null) {
                requestBody.put("device_type", deviceType);
            }
            // Include user_id (can be null for guests)
            requestBody.put("user_id", userId);
        } catch (JSONException e) {
            callback.onError(ApiResponse.error(e.getMessage(), 0));
            return;
        }
        apiClient.post(endpoint, requestBody, callback);
    }

    /**
     * Get all registered devices for the current user
     */
    public void getMyDevices(ApiClient.ApiCallback<JSONObject> callback) {
        getMyDevices(null, callback);
    }

    /**
     * Get devices for a specific user or guest devices
     * @param userId Optional user UUID to filter devices (null for guest devices)
     */
    public void getMyDevices(String userId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/notifications/my-devices";
        if (userId != null && !userId.isEmpty()) {
            endpoint += "?user_id=" + userId;
        }
        apiClient.get(endpoint, callback);
    }
}