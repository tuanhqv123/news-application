package com.example.newsapplication.api.endpoints;

import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import org.json.JSONObject;

public class UserEndpoints {
    private final ApiClient apiClient;

    public UserEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void getUserBookmarks(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/users/me/bookmarks", callback);
    }

    // Admin endpoints
    public void getPendingAuthors(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/users/admin/pending-authors", callback);
    }

    public void approveAuthor(String userId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.put(ApiConfig.API_VERSION + "/users/admin/approve-author/" + userId, null, callback);
    }

    public void getAllUserProfiles(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/users/admin/all-profiles", callback);
    }

    public void banUser(String userId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.put(ApiConfig.API_VERSION + "/users/admin/ban/" + userId, null, callback);
    }

    public void unbanUser(String userId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.put(ApiConfig.API_VERSION + "/users/admin/unban/" + userId, null, callback);
    }

    public void changePassword(JSONObject requestBody, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.put(ApiConfig.API_VERSION + "/users/change-password", requestBody, callback);
    }

    public void updateProfile(JSONObject requestBody, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.put(ApiConfig.API_VERSION + "/users/profile", requestBody, callback);
    }

    public void updateAuthProfile(JSONObject requestBody, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.put(ApiConfig.API_VERSION + "/auth/me", requestBody, callback);
    }
}