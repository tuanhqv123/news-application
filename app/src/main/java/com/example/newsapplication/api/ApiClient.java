package com.example.newsapplication.api;

import android.content.Context;
import com.android.volley.*;
import com.android.volley.toolbox.JsonObjectRequest;
import com.example.newsapplication.utils.NetworkUtils;
import com.example.newsapplication.utils.PreferenceUtils;
import com.example.newsapplication.utils.VolleySingleton;
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
        PreferenceUtils.putString(context, ApiConfig.PREFS_NAME, ApiConfig.TOKEN_KEY, token);
    }

    public void clearAuthToken() {
        this.authToken = null;
        PreferenceUtils.remove(context, ApiConfig.PREFS_NAME, ApiConfig.TOKEN_KEY);
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
        String url = ApiConfig.BASE_URL + endpoint;
        String jsonBody = body != null ? gson.toJson(body) : null;

        JsonObjectRequest request = new JsonObjectRequest(method, url,
            jsonBody != null ? createJsonObject(jsonBody) : null,
            response -> callback.onSuccess(ApiResponse.success(response)),
            error -> callback.onError(createErrorResponse(error))
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
        authToken = PreferenceUtils.getString(context, ApiConfig.PREFS_NAME, ApiConfig.TOKEN_KEY, null);
    }

    public interface ApiCallback<T> {
        void onSuccess(ApiResponse<T> response);
        void onError(ApiResponse<T> error);
    }
}