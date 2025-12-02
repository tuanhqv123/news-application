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
                        
                        apiClient.setAuthToken(token);
                        if (refreshToken != null) {
                            sessionManager.createLoginSession(userEmail, displayName, role, token, refreshToken);
                        } else {
                            sessionManager.createLoginSession(userEmail, displayName, role, token);
                        }
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            sessionManager.setAvatarUrl(avatarUrl);
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
                    callback.onError("Failed to parse login response: " + e.getMessage());
                }
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                String errorMessage = error.getErrorMessage();
                
                if (error.getData() != null) {
                    if (error.getData().has("success") && !error.getData().optBoolean("success", true)) {
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                            }
                        }
                    } else {
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                            }
                        }
                    }
                }
                
                callback.onError(errorMessage);
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
                String errorMessage = error.getErrorMessage();
                
                if (error.getData() != null) {
                    if (error.getData().has("success") && !error.getData().optBoolean("success", true)) {
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                            }
                        }
                    } else {
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                            }
                        }
                    }
                }
                
                callback.onError(errorMessage);
            }
        });
    }

    public void logout(AuthResultCallback callback) {
        authEndpoints.logout(new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                sessionManager.logoutUser();
                apiClient.clearAuthToken();
                callback.onSuccess(response.getData());
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                sessionManager.logoutUser();
                apiClient.clearAuthToken();
                callback.onSuccess(null);
            }
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
                    callback.onError("Failed to parse user data");
                }
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                String errorMessage = error.getErrorMessage();
                
                if (error.getData() != null) {
                    if (error.getData().has("success") && !error.getData().optBoolean("success", true)) {
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                            }
                        }
                    } else {
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                            }
                        }
                    }
                }
                
                callback.onError(errorMessage);
            }
        });
    }



    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public void refreshToken(AuthResultCallback callback) {
        String refreshToken = sessionManager.getRefreshToken();
        if (refreshToken == null) {
            callback.onError("No refresh token available");
            return;
        }

        authEndpoints.refreshToken(refreshToken, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                try {
                    JSONObject data = response.getData();
                    if (data != null && data.has("success") && data.getBoolean("success") && data.has("data")) {
                        JSONObject responseData = data.getJSONObject("data");
                        String newToken = responseData.getString("access_token");
                        String newRefreshToken = responseData.optString("refresh_token", refreshToken);

                        // Update stored tokens
                        apiClient.setAuthToken(newToken);
                        sessionManager.setAuthToken(newToken);
                        sessionManager.setRefreshToken(newRefreshToken);

                        callback.onSuccess(responseData);
                    } else {
                        callback.onError("Invalid refresh response");
                    }
                } catch (Exception e) {
                    callback.onError("Error parsing refresh response: " + e.getMessage());
                }
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                sessionManager.logoutUser();
                apiClient.clearAuthToken();
                callback.onError("Session expired. Please login again.");
            }
        });
    }
    
    public void validateToken(AuthResultCallback callback) {
        if (!sessionManager.isLoggedIn() || sessionManager.getAuthToken() == null) {
            callback.onError("Not logged in");
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
                String errorMessage = error.getErrorMessage();
                
                if (error.getData() != null) {
                    if (error.getData().has("success") && !error.getData().optBoolean("success", true)) {
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                            }
                        }
                    } else {
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                            }
                        }
                    }
                }
                
                callback.onError(errorMessage);
            }
        });
    }

    public UserSessionManager getSessionManager() {
        return sessionManager;
    }
}
