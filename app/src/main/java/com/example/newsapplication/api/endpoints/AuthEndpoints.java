package com.example.newsapplication.api.endpoints;

import android.content.Context;

import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.model.request.UserLogin;
import com.example.newsapplication.model.request.UserRegister;
import org.json.JSONException;
import org.json.JSONObject;

public class AuthEndpoints {
    private final ApiClient apiClient;
    private Context context;

    public AuthEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public AuthEndpoints(Context context) {
        this.context = context;
        this.apiClient = new ApiClient(context);
    }

    public interface InviteUserCallback {
        void onSuccess(String userId, String email, String role);
        void onError(String error);
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
        logout(null, callback);
    }

    public void logout(String fcmToken, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/auth/logout";
        JSONObject requestBody = null;

        if (fcmToken != null && !fcmToken.isEmpty()) {
            requestBody = new JSONObject();
            try {
                requestBody.put("fcm_token", fcmToken);
            } catch (JSONException e) {
                callback.onError(ApiResponse.error(e.getMessage(), 0));
                return;
            }
        }

        apiClient.post(endpoint, requestBody, callback);
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
            callback.onError(ApiResponse.error(e.getMessage(), 0));
            return;
        }
        apiClient.put(ApiConfig.API_VERSION + "/auth/me", requestBody, callback);
    }

    public void inviteUser(String email, int roleId, Integer channelId, InviteUserCallback callback) {
        String invitedBy = "admin@gmail.com";
        if (context != null) {
            android.content.SharedPreferences prefs = context.getSharedPreferences("NewsAppSession", Context.MODE_PRIVATE);
            invitedBy = prefs.getString("userEmail", "admin@gmail.com");
        }
        
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("email", email);
            requestBody.put("role_id", roleId);
            requestBody.put("invited_by", invitedBy);
            if (channelId != null && channelId > 0) {
                requestBody.put("channel_id", channelId);
            }
        } catch (JSONException e) {
            callback.onError(e.getMessage());
            return;
        }
        
        apiClient.post(ApiConfig.API_VERSION + "/auth/admin/invite-user", requestBody, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                try {
                    JSONObject fullResponse = response.getData();
                    JSONObject data = fullResponse.getJSONObject("data");
                    String userId = data.optString("user_id", "");
                    String userEmail = data.optString("email", "");
                    String role = data.optString("role", "");
                    callback.onSuccess(userId, userEmail, role);
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                callback.onError(error.getMessage());
            }
        });
    }

    public void inviteUser(String email, int roleId, InviteUserCallback callback) {
        inviteUser(email, roleId, null, callback);
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
            callback.onError(ApiResponse.error(e.getMessage(), 0));
            return;
        }
        apiClient.post(ApiConfig.API_VERSION + "/auth/admin/invite-user", requestBody, callback);
    }

    public void setUserRole(String userId, String role, ApiClient.ApiCallback<JSONObject> callback) {
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