package com.example.newsapplication.api.endpoints;

import android.util.Log;
import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import com.example.newsapplication.model.request.CategoryCreate;
import com.example.newsapplication.utils.UrlBuilder;
import org.json.JSONObject;

public class CategoryEndpoints {
    private static final String TAG = "CategoryEndpoints";
    private final ApiClient apiClient;

    public CategoryEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void getCategories(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/categories/", callback);
    }

    public void getCategoryArticles(int categoryId, int page, int limit, ApiClient.ApiCallback<JSONObject> callback) {
        String baseUrl = ApiConfig.API_VERSION + "/categories/" + categoryId;
        String url = UrlBuilder.buildUrlWithOptionalParams(baseUrl,
            "page", page,
            "limit", limit
        );
        apiClient.get(url, callback);
    }

    public void getChannels(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/categories/channels", callback);
    }

    public void createCategory(String name, String slug, ApiClient.ApiCallback<JSONObject> callback) {
        CategoryCreate category = new CategoryCreate(name, slug);
        apiClient.post(ApiConfig.API_VERSION + "/categories/admin/create", category, callback);
    }
}