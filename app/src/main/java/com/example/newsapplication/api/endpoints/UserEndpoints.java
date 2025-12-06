package com.example.newsapplication.api.endpoints;

import android.content.Context;

import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.model.User;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class UserEndpoints {
    private final ApiClient apiClient;
    private final Gson gson;

    public UserEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.gson = new Gson();
    }

    public UserEndpoints(Context context) {
        this.apiClient = new ApiClient(context);
        this.gson = new Gson();
    }

    // Callback interfaces
    public interface UserProfilesCallback {
        void onSuccess(List<User> users);
        void onError(String error);
    }

    public interface RoleChangeCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface BanCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public void getUserBookmarks(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/users/me/bookmarks", callback);
    }

    public void getAllUserProfiles(String role, UserProfilesCallback callback) {
        String endpoint = ApiConfig.API_VERSION + "/users/admin/all-profiles";
        if (role != null && !role.isEmpty()) {
            endpoint += "?role=" + role;
        }
        
        apiClient.get(endpoint, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                try {
                    JSONObject data = response.getData();
                    if (data.has("data")) {
                        data = data.getJSONObject("data");
                    }
                    org.json.JSONArray profilesArray = data.getJSONArray("profiles");
                    Type listType = new TypeToken<List<User>>(){}.getType();
                    List<User> users = gson.fromJson(profilesArray.toString(), listType);
                    callback.onSuccess(users);
                } catch (Exception e) {
                    callback.onError("Parse error: " + e.getMessage());
                }
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void getAllUserProfiles(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/users/admin/all-profiles", callback);
    }

    public void setUserRole(String userId, int roleId, RoleChangeCallback callback) {
        String role;
        switch (roleId) {
            case 1: role = "admin"; break;
            case 2: role = "author"; break;
            case 3: role = "reader"; break;
            default: 
                callback.onError("Invalid role ID");
                return;
        }
        
        String endpoint = ApiConfig.API_VERSION + "/users/admin/set-role/" + userId + "?role=" + role;
        apiClient.put(endpoint, null, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                callback.onSuccess("Role changed successfully");
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void banUser(String userId, BanCallback callback) {
        apiClient.put(ApiConfig.API_VERSION + "/users/admin/ban/" + userId, null, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                callback.onSuccess(response.getMessage());
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void unbanUser(String userId, BanCallback callback) {
        apiClient.put(ApiConfig.API_VERSION + "/users/admin/unban/" + userId, null, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                callback.onSuccess(response.getMessage());
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                callback.onError(error.getMessage());
            }
        });
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