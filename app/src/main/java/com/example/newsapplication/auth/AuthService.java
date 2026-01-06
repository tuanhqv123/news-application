package com.example.newsapplication.auth;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.newsapplication.api.ApiClient;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthService {
    private static final String TAG = "AuthService";
    private static final String API_BASE_URL = "http://10.0.2.2:8000/api/v1";

    private final Context context;
    private final RequestQueue requestQueue;
    private final UserSessionManager sessionManager;

    public interface AuthResultCallback {
        void onSuccess(JSONObject response);
        void onError(String errorMessage);
    }

    public AuthService(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.sessionManager = new UserSessionManager(context);
    }

    public void loginWithGoogle(String idToken, String nonce, AuthResultCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("id_token", idToken);
            if (nonce != null) {
                requestBody.put("nonce", nonce);
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    API_BASE_URL + "/auth/google",
                    requestBody,
                    response -> {
                        try {
                            handleAuthResponse(response);
                            callback.onSuccess(response);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing Google login response", e);
                            callback.onError("Invalid response format");
                        }
                    },
                    error -> callback.onError(parseVolleyError(error))
            );

            request.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 1.0f));
            requestQueue.add(request);

        } catch (JSONException e) {
            Log.e(TAG, "Error creating Google login request", e);
            callback.onError("Failed to create request");
        }
    }

    private void handleAuthResponse(JSONObject response) throws JSONException {
        // Check if response has "data" field (standard response format)
        if (response.has("data")) {
            JSONObject data = response.getJSONObject("data");
            JSONObject userData = data.getJSONObject("user");

            String accessToken = data.getString("access_token");
            String refreshToken = data.getString("refresh_token");

            sessionManager.saveUserInfo(
                    userData.getString("id"),
                    userData.getString("email"),
                    userData.optString("display_name", ""),
                    userData.optString("avatar_url", ""),
                    userData.optString("role", "reader")
            );

            sessionManager.setAuthToken(accessToken);
            sessionManager.setRefreshToken(refreshToken);

            ApiClient.initialize(context, accessToken);

            Log.d(TAG, "Auth successful - User: " + userData.getString("email"));
        }
        // Handle Google Sign-In response format (different structure)
        else if (response.has("user") && response.has("session")) {
            JSONObject userData = response.getJSONObject("user");
            JSONObject sessionData = response.getJSONObject("session");

            sessionManager.saveUserInfo(
                    userData.getString("id"),
                    userData.getString("email"),
                    userData.optString("full_name", ""),
                    userData.optString("avatar_url", ""),
                    userData.optString("role", "reader")
            );

            sessionManager.setAuthToken(sessionData.getString("access_token"));
            sessionManager.setRefreshToken(sessionData.getString("refresh_token"));

            ApiClient.initialize(context, sessionData.getString("access_token"));

            Log.d(TAG, "Auth successful - User: " + userData.getString("email"));
        }
        else {
            throw new JSONException("Invalid response format");
        }
    }


    private String parseVolleyError(com.android.volley.VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                String responseBody = new String(error.networkResponse.data, "utf-8");
                JSONObject jsonError = new JSONObject(responseBody);

                if (jsonError.has("detail")) {
                    return jsonError.getString("detail");
                } else if (jsonError.has("message")) {
                    return jsonError.getString("message");
                }
            } catch (Exception e) {
                // Ignore parse error
            }
        }
        return error.getMessage() != null ? error.getMessage() : "Network error occurred";
    }

    public void login(String email, String password, AuthResultCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            requestBody.put("password", password);

            // ✅ THÊM LOG NÀY
            Log.d(TAG, "Login request body: " + requestBody.toString());
            Log.d(TAG, "Login URL: " + API_BASE_URL + "/auth/login");

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    API_BASE_URL + "/auth/login",
                    requestBody,
                    response -> {
                        try {
                            handleAuthResponse(response);
                            callback.onSuccess(response);
                        } catch (JSONException e) {
                            callback.onError("Invalid response format");
                        }
                    },
                    error -> {
                        // ✅ THÊM LOG NÀY
                        Log.e(TAG, "Login error status code: " + error.networkResponse.statusCode);
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Login error body: " + errorBody);
                        }
                        callback.onError(parseVolleyError(error));
                    }
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            callback.onError("Failed to create request");
        }
    }


    public void register(String email, String password, String fullName, AuthResultCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            requestBody.put("password", password);
            requestBody.put("display_name", fullName);

            Log.d(TAG, "Register request: " + requestBody.toString());

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    API_BASE_URL + "/auth/register",
                    requestBody,
                    response -> {
                        Log.d(TAG, "Registration successful");
                        callback.onSuccess(response);
                    },
                    error -> {
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String errorBody = new String(error.networkResponse.data);
                            Log.e(TAG, "Register error: " + errorBody);
                        }
                        callback.onError(parseVolleyError(error));
                    }
            );

            requestQueue.add(request);
        } catch (JSONException e) {
            Log.e(TAG, "Error creating register request", e);
            callback.onError("Failed to create request");
        }
    }


    public void refreshToken(String refreshToken, AuthResultCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("refresh_token", refreshToken);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    API_BASE_URL + "/auth/refresh",
                    requestBody,
                    response -> {
                        try {
                            handleAuthResponse(response);
                            callback.onSuccess(response);
                        } catch (JSONException e) {
                            Log.e(TAG, "Error parsing refresh response", e);
                            callback.onError("Invalid response format");
                        }
                    },
                    error -> callback.onError(parseVolleyError(error))
            );

            request.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 1.0f));
            requestQueue.add(request);

        } catch (JSONException e) {
            callback.onError("Failed to refresh token");
        }
    }

    public void logout(AuthResultCallback callback) {
        String authToken = sessionManager.getAuthToken();

        try {
            JSONObject requestBody = new JSONObject();

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    API_BASE_URL + "/auth/logout",
                    requestBody,
                    response -> {
                        sessionManager.clearSession();
                        callback.onSuccess(response);
                    },
                    error -> {
                        sessionManager.clearSession();
                        try {
                            callback.onSuccess(new JSONObject());
                        } catch (Exception e) {
                            callback.onError("Logout completed");
                        }
                    }
            ) {
                @Override
                public java.util.Map<String, String> getHeaders() {
                    java.util.Map<String, String> headers = new java.util.HashMap<>();
                    headers.put("Content-Type", "application/json");
                    if (authToken != null && !authToken.isEmpty()) {
                        headers.put("Authorization", "Bearer " + authToken);
                    }
                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(15000, 0, 1.0f));
            requestQueue.add(request);

        } catch (Exception e) {
            sessionManager.clearSession();
            try {
                callback.onSuccess(new JSONObject());
            } catch (Exception ex) {
                callback.onError("Logout completed");
            }
        }
    }
}
