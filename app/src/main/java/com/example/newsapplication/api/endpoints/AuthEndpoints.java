package com.example.newsapplication.api.endpoints;

import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import com.example.newsapplication.model.request.UserLogin;
import com.example.newsapplication.model.request.UserRegister;
import com.example.newsapplication.model.request.UserProfile;
import org.json.JSONObject;

public class AuthEndpoints {
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
        UserProfile profile = new UserProfile(displayName, avatarUrl);
        apiClient.put(ApiConfig.API_VERSION + "/auth/me", profile, callback);
    }

    public void inviteUser(String email, int roleId, Integer channelId, String invitedBy, ApiClient.ApiCallback<JSONObject> callback) {
        // Implement UserInvite
        apiClient.post(ApiConfig.API_VERSION + "/auth/admin/invite-user", null, callback); // Placeholder
    }

    public void setUserRole(String userId, String role, ApiClient.ApiCallback<JSONObject> callback) {
        // Query params
        apiClient.post(ApiConfig.API_VERSION + "/auth/admin/set-role?user_id=" + userId + "&role=" + role, null, callback);
    }

    public void getRoles(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/auth/roles", callback);
    }
}