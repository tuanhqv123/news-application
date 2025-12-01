package com.example.newsapplication.api;

import android.content.Context;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.newsapplication.utils.NetworkUtils;
import com.example.newsapplication.utils.PreferenceUtils;
import com.example.newsapplication.utils.VolleySingleton;
import com.example.newsapplication.auth.UserSessionManager;
import com.example.newsapplication.auth.AuthService;
import com.google.gson.Gson;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class ApiClient {
    private final Context context;
    private final Gson gson;
    private final RequestQueue requestQueue;
    private String authToken;

    public ApiClient(Context context) {
        this.context = context;
        this.gson = new Gson();
        this.requestQueue = VolleySingleton.getInstance(context).getRequestQueue();
        loadAuthToken();
    }

    public void setAuthToken(String token) {
        this.authToken = token;
        // Store in both PreferenceUtils (for backwards compatibility) and UserSessionManager
        PreferenceUtils.putString(context, ApiConfig.PREFS_NAME, ApiConfig.TOKEN_KEY, token);
        new UserSessionManager(context).setAuthToken(token);
    }

    public void clearAuthToken() {
        this.authToken = null;
        // Clear from both storage locations
        PreferenceUtils.remove(context, ApiConfig.PREFS_NAME, ApiConfig.TOKEN_KEY);
        new UserSessionManager(context).logoutUser();
    }

    public void get(String endpoint, ApiCallback<JSONObject> callback) {
        makeRequest(Request.Method.GET, endpoint, null, callback);
    }

    public void post(String endpoint, Object body, ApiCallback<JSONObject> callback) {
        makeRequest(Request.Method.POST, endpoint, body, callback);
    }

    public void put(String endpoint, Object body, ApiCallback<JSONObject> callback) {
        makeRequest(Request.Method.PUT, endpoint, body, callback);
    }

    public void delete(String endpoint, ApiCallback<JSONObject> callback) {
        makeRequest(Request.Method.DELETE, endpoint, null, callback);
    }

    private void makeRequest(int method, String endpoint, Object body, ApiCallback<JSONObject> callback) {
        makeRequestWithRetry(method, endpoint, body, callback, false);
    }

    private void makeRequestWithRetry(int method, String endpoint, Object body, ApiCallback<JSONObject> callback, boolean isRetry) {
        String url = ApiConfig.BASE_URL + endpoint;
        String jsonBody = body != null ? 
            (body instanceof JSONObject) ? ((JSONObject) body).toString() : gson.toJson(body)
            : null;
        
        String methodStr = method == Request.Method.GET ? "GET" : method == Request.Method.POST ? "POST" : method == Request.Method.PUT ? "PUT" : method == Request.Method.DELETE ? "DELETE" : "UNKNOWN";
        android.util.Log.d("ApiClient", "=== Making " + methodStr + " request to " + endpoint + (isRetry ? " (RETRY)" : "") + " ===");
        android.util.Log.d("ApiClient", "URL: " + url);
        android.util.Log.d("ApiClient", "Body: " + (jsonBody != null ? jsonBody : "null"));

        JsonObjectRequest request = new JsonObjectRequest(method, url,
            jsonBody != null ? createJsonObject(jsonBody) : null,
            response -> callback.onSuccess(ApiResponse.success(response)),
            error -> {
                int statusCode = NetworkUtils.getStatusCode(error);
                android.util.Log.e("ApiClient", "Request failed with status: " + statusCode);
                
                // Handle 401 Unauthorized - try to refresh token if this is not already a retry
                if (statusCode == 401 && !isRetry) {
                    android.util.Log.d("ApiClient", "Got 401, attempting token refresh");
                    handleTokenRefresh(method, endpoint, body, callback);
                } else {
                    callback.onError(createErrorResponse(error));
                }
            }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                return getDefaultHeaders();
            }

            @Override
            public String getBodyContentType() {
                return ApiConfig.CONTENT_TYPE_JSON;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
            ApiConfig.TIMEOUT_MS,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
        android.util.Log.d("ApiClient", "Request added to queue");
    }

    private void handleTokenRefresh(int method, String endpoint, Object body, ApiCallback<JSONObject> originalCallback) {
        AuthService authService = new AuthService(context);
        authService.refreshToken(new AuthService.AuthResultCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                android.util.Log.d("ApiClient", "Token refreshed successfully, retrying original request");
                // Update auth token and retry the original request
                loadAuthToken();
                makeRequestWithRetry(method, endpoint, body, originalCallback, true);
            }

            @Override
            public void onError(String errorMessage) {
                android.util.Log.e("ApiClient", "Token refresh failed: " + errorMessage);
                // Return original 401 error
                originalCallback.onError(ApiResponse.error(errorMessage, 401));
            }
        });
    }

    private Map<String, String> getDefaultHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put(ApiConfig.HEADER_CONTENT_TYPE, ApiConfig.CONTENT_TYPE_JSON);
        if (authToken != null) {
            headers.put(ApiConfig.HEADER_AUTHORIZATION, ApiConfig.BEARER_PREFIX + authToken);
        }
        return headers;
    }

    private JSONObject createJsonObject(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            throw new RuntimeException("Invalid JSON: " + json, e);
        }
    }

    private ApiResponse<JSONObject> createErrorResponse(VolleyError error) {
        String message = NetworkUtils.getErrorMessage(context, error);
        int statusCode = NetworkUtils.getStatusCode(error);
        return ApiResponse.error(message, statusCode);
    }

    private void loadAuthToken() {
        // Try to load from UserSessionManager first, then fallback to PreferenceUtils
        UserSessionManager sessionManager = new UserSessionManager(context);
        authToken = sessionManager.getAuthToken();
        if (authToken == null) {
            authToken = PreferenceUtils.getString(context, ApiConfig.PREFS_NAME, ApiConfig.TOKEN_KEY, null);
        }
    }

    public interface ApiCallback<T> {
        void onSuccess(ApiResponse<T> response);
        void onError(ApiResponse<T> error);
    }
}