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
                    
                    // Handle the actual API response format: {"success":true,"data":{"access_token":"...","user":{...}}}
                    if (data != null && data.has("success") && data.getBoolean("success") && data.has("data")) {
                        JSONObject responseData = data.getJSONObject("data");
                        
                        String token = responseData.getString("access_token");
                        String refreshToken = responseData.optString("refresh_token", null);
                        JSONObject userData = responseData.getJSONObject("user");
                        
                        String userEmail = userData.optString("email", email);
                        String displayName = userData.optString("display_name", email.split("@")[0]);
                        String role = userData.optString("role", "reader");
                        
                        // Store token and session data
                        apiClient.setAuthToken(token);
                        if (refreshToken != null) {
                            sessionManager.createLoginSession(userEmail, displayName, role, token, refreshToken);
                        } else {
                            sessionManager.createLoginSession(userEmail, displayName, role, token);
                        }
                        
                        // Also get the success message from response
                        String message = data.optString("message", "Login successful");
                        
                        // Create response object with user data and message
                        JSONObject responseWithMessage = new JSONObject();
                        try {
                            responseWithMessage.put("user", userData);
                            responseWithMessage.put("message", message);
                        } catch (JSONException e) {
                            // Use original response if this fails
                        }
                        
                        callback.onSuccess(responseWithMessage);
                    } else if (data != null && data.has("access_token")) {
                        // Fallback to simple format
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
                    // Check if response has success field first
                    if (error.getData().has("success") && !error.getData().optBoolean("success", true)) {
                        // This is an error response with success=false, try to get detail
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                                // Keep original error message if parsing fails
                            }
                        }
                    } else {
                        // Try to extract 'detail' key if it exists (for direct errors)
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                                // Keep original error message if parsing fails
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
                    // Check if response has success field first
                    if (error.getData().has("success") && !error.getData().optBoolean("success", true)) {
                        // This is an error response with success=false, try to get detail
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                                // Keep original error message if parsing fails
                            }
                        }
                    } else {
                        // Try to extract 'detail' key if it exists (for direct errors)
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                                // Keep original error message if parsing fails
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
                // Even if logout fails on server, clear local session
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
                    JSONObject userData = response.getData();
                    if (userData != null) {
                        String email = userData.optString("email", "");
                        String displayName = userData.optString("display_name", "");
                        String role = userData.optString("role", "reader");
                        
                        sessionManager.createLoginSession(email, displayName, role);
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
                    // Check if response has success field first
                    if (error.getData().has("success") && !error.getData().optBoolean("success", true)) {
                        // This is an error response with success=false, try to get detail
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                                // Keep original error message if parsing fails
                            }
                        }
                    } else {
                        // Try to extract 'detail' key if it exists (for direct errors)
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                                // Keep original error message if parsing fails
                            }
                        }
                    }
                }
                
                callback.onError(errorMessage);
            }
        });
    }

    // REMOVED - Use NewsRepository.updateProfile instead which has correct JSON format
    // public void updateProfile(String displayName, String avatarUrl, AuthResultCallback callback) {
    //     authEndpoints.updateProfile(displayName, avatarUrl, new ApiClient.ApiCallback<JSONObject>() {
    //         @Override
    //             public void onSuccess(ApiResponse<JSONObject> response) {
    //                 // Update local session data
    //                 String currentEmail = sessionManager.getUserEmail();
    //                 String currentRole = sessionManager.getUserRole();
    //                 sessionManager.createLoginSession(currentEmail, displayName, currentRole);
    //                 callback.onSuccess(response.getData());
    //             }

    //             @Override
    //             public void onError(ApiResponse<JSONObject> error) {
    //                 callback.onError(error.getErrorMessage());
    //             }
    //         });
    // }

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
                // Refresh token failed, user needs to login again
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
                // Token is valid if we can get user data
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                // Token is invalid
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
                // This could be extended to fetch actual bookmark data
                // For now, just return success
                callback.onSuccess(response.getData());
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                String errorMessage = error.getErrorMessage();
                
                if (error.getData() != null) {
                    // Check if response has success field first
                    if (error.getData().has("success") && !error.getData().optBoolean("success", true)) {
                        // This is an error response with success=false, try to get detail
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                                // Keep original error message if parsing fails
                            }
                        }
                    } else {
                        // Try to extract 'detail' key if it exists (for direct errors)
                        if (error.getData().has("detail")) {
                            try {
                                errorMessage = error.getData().getString("detail");
                            } catch (JSONException e) {
                                // Keep original error message if parsing fails
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
