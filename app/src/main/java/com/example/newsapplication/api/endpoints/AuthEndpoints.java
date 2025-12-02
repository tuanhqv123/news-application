package com.example.newsapplication.api.endpoints;

import android.util.Log;
import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.model.request.UserLogin;
import com.example.newsapplication.model.request.UserRegister;
import com.example.newsapplication.model.request.UserProfile;
import org.json.JSONException;
import org.json.JSONObject;

public class AuthEndpoints {
    private static final String TAG = "AuthEndpoints";
    private final ApiClient apiClient;

    public AuthEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void login(String email, String password, ApiClient.ApiCallback<JSONObject> callback) {
        UserLogin loginRequest = new UserLogin(email, password);
        apiClient.post(ApiConfig.API_VERSION + "/auth/login", loginRequest, callback);
    }

    public void register(String email, String password, String displayName, ApiClient.ApiCallback<JSONObject> callback) {
        UserRegister registerRequest = new UserRegister(email, password, displayName);
        apiClient.post(ApiConfig.API_VERSION + "/auth/register", registerRequest, callback);
    }

    public void logout(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.post(ApiConfig.API_VERSION + "/auth/logout", null, callback);
    }

    public void getMe(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/auth/me", callback);
    }

    public void updateProfile(String displayName, String avatarUrl, ApiClient.ApiCallback<JSONObject> callback) {
        JSONObject requestBody = new JSONObject();
        try {
            if (displayName != null && !displayName.isEmpty()) {
                requestBody.put("display_name", displayName);
            }
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                requestBody.put("avatar_url", avatarUrl);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to build updateProfile request body", e);
            callback.onError(ApiResponse.error("Invalid request data", 0));
            return;
        }
        apiClient.put(ApiConfig.API_VERSION + "/auth/me", requestBody, callback);
    }

    public void inviteUser(String email, int roleId, Integer channelId, String invitedBy, ApiClient.ApiCallback<JSONObject> callback) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("role_id", roleId);
            requestBody.put("invited_by", invitedBy);
            if (channelId != null) {
                requestBody.put("channel_id", channelId);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Failed to build inviteUser request body", e);
            callback.onError(ApiResponse.error("Invalid request data", 0));
            return;
        }
        apiClient.post(ApiConfig.API_VERSION + "/auth/admin/invite-user", requestBody, callback);
    }

    public void setUserRole(String userId, String role, ApiClient.ApiCallback<JSONObject> callback) {
        // Query params
        apiClient.post(ApiConfig.API_VERSION + "/auth/admin/set-role?user_id=" + userId + "&role=" + role, null, callback);
    }

    public void getRoles(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/auth/roles", callback);
    }

    public void refreshToken(String refreshToken, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/auth/refresh?refresh_token=" + refreshToken;
        apiClient.post(endpoint, null, callback);
    }
}