package com.example.newsapplication.api.endpoints;

import android.util.Log;
import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.utils.UrlBuilder;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * API endpoints for Channel management
 */
public class ChannelEndpoints {
    private static final String TAG = "ChannelEndpoints";
    private final ApiClient apiClient;

    public ChannelEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Get public channels list
     */
    public void getPublicChannels(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/channels/public/list", callback);
    }

    /**
     * Get all channels (requires auth)
     */
    public void getAllChannels(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/channels/list", callback);
    }

    /**
     * Get followed channels (requires auth)
     */
    public void getFollowedChannels(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/channels/followed", callback);
    }

    /**
     * Follow a channel
     */
    public void followChannel(int channelId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.post(ApiConfig.API_VERSION + "/channels/" + channelId + "/follow", null, callback);
    }

    /**
     * Unfollow a channel
     */
    public void unfollowChannel(int channelId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.delete(ApiConfig.API_VERSION + "/channels/" + channelId + "/follow", callback);
    }

    /**
     * Get articles from a specific channel
     */
    public void getChannelArticles(int channelId, int page, int limit, ApiClient.ApiCallback<JSONObject> callback) {
        String url = ApiConfig.API_VERSION + "/articles/?channel_id=" + channelId + "&page=" + page + "&limit=" + limit;
        apiClient.get(url, callback);
    }

    // Admin endpoints

    /**
     * Create a new channel (admin only)
     */
    public void createChannel(String name, String slug, String description, String rssUrl, String logoUrl, ApiClient.ApiCallback<JSONObject> callback) {
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("name", name);
            requestBody.put("slug", slug);
            if (description != null) requestBody.put("description", description);
            if (rssUrl != null) requestBody.put("rss_url", rssUrl);
            if (logoUrl != null) requestBody.put("logo_url", logoUrl);
        } catch (JSONException e) {
            Log.e(TAG, "Failed to build createChannel request body", e);
            callback.onError(ApiResponse.error("Invalid request data", 0));
            return;
        }
        apiClient.post(ApiConfig.API_VERSION + "/channels/admin/create", requestBody, callback);
    }

    /**
     * Update a channel (admin only)
     */
    public void updateChannel(int channelId, String name, String description, String rssUrl, String logoUrl, Boolean isActive, ApiClient.ApiCallback<JSONObject> callback) {
        String baseUrl = ApiConfig.API_VERSION + "/channels/admin/" + channelId;
        String url = UrlBuilder.buildUrlWithOptionalParams(baseUrl,
            "name", name,
            "description", description,
            "rss_url", rssUrl,
            "logo_url", logoUrl,
            "is_active", isActive
        );
        
        apiClient.put(url, null, callback);
    }

    /**
     * Delete a channel (admin only)
     */
    public void deleteChannel(int channelId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.delete(ApiConfig.API_VERSION + "/channels/admin/" + channelId, callback);
    }
}
