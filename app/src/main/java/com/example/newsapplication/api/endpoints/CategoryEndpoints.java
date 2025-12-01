package com.example.newsapplication.api.endpoints;

import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import com.example.newsapplication.model.request.CategoryCreate;
import org.json.JSONObject;

public class CategoryEndpoints {
    private final ApiClient apiClient;

    public CategoryEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void getCategories(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/categories/", callback);
    }

    public void getCategoryArticles(int categoryId, int page, int limit, ApiClient.ApiCallback<JSONObject> callback) {
        String url = ApiConfig.API_VERSION + "/categories/" + categoryId + "?page=" + page + "&limit=" + limit;
        apiClient.get(url, callback);
    }

    public void getChannels(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/categories/channels", callback);
    }

    public void subscribeChannel(int channelId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.post(ApiConfig.API_VERSION + "/categories/channels/" + channelId + "/subscribe", null, callback);
    }

    public void unsubscribeChannel(int channelId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.delete(ApiConfig.API_VERSION + "/categories/channels/" + channelId + "/subscribe", callback);
    }

    public void createCategory(String name, String slug, ApiClient.ApiCallback<JSONObject> callback) {
        CategoryCreate category = new CategoryCreate(name, slug);
        apiClient.post(ApiConfig.API_VERSION + "/categories/admin/create", category, callback);
    }
}