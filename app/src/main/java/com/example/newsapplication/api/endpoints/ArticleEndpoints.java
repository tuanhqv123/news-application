package com.example.newsapplication.api.endpoints;

import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.utils.UrlBuilder;
import org.json.JSONException;
import org.json.JSONObject;

// API endpoints for Article management
public class ArticleEndpoints {
    private final ApiClient apiClient;

    public ArticleEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void getArticles(ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/";
        apiClient.get(endpoint, callback);
    }

    public void getArticles(int page, int limit, Integer categoryId, ApiClient.ApiCallback<JSONObject> callback) {
        String baseUrl = ApiConfig.API_VERSION + "/articles/";
        String endpoint = UrlBuilder.buildUrlWithOptionalParams(baseUrl,
            "page", page,
            "limit", limit,
            "category", categoryId
        );
        apiClient.get(endpoint, callback);
    }

    public void getArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId;
        apiClient.get(endpoint, callback);
    }

    public void searchArticles(String query, int page, int limit, ApiClient.ApiCallback<JSONObject> callback) {
        String baseUrl = ApiConfig.API_VERSION + "/articles/search";
        String endpoint = UrlBuilder.buildUrlWithOptionalParams(baseUrl,
            "q", query,
            "page", page,
            "limit", limit
        );
        apiClient.get(endpoint, callback);
    }

    public void getComments(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId + "/comments";
        apiClient.get(endpoint, callback);
    }

    public void addComment(String articleId, String content, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId + "/comments";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("content", content);
        } catch (JSONException e) {
            callback.onError(ApiResponse.error("Invalid request data", 0));
            return;
        }
        apiClient.post(endpoint, requestBody, callback);
    }

    public void bookmarkArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId + "/bookmark";
        apiClient.post(endpoint, null, callback);
    }

    public void removeBookmark(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId + "/bookmark";
        apiClient.delete(endpoint, callback);
    }

    public void getBookmarks(ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/users/me/bookmarks";
        apiClient.get(endpoint, callback);
    }

    public void createArticle(String title, String summary, String content, int categoryId, 
                              Integer channelId, String sourceUrl, String heroImageUrl, 
                              String language, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("title", title);
            requestBody.put("summary", summary);
            requestBody.put("content", content);
            requestBody.put("category_id", categoryId);
            if (channelId != null) requestBody.put("channel_id", channelId);
            if (sourceUrl != null) requestBody.put("source_url", sourceUrl);
            if (heroImageUrl != null) requestBody.put("hero_image_url", heroImageUrl);
            if (language != null) requestBody.put("language", language);
        } catch (JSONException e) {
            callback.onError(ApiResponse.error("Invalid request data", 0));
            return;
        }
        apiClient.post(endpoint, requestBody, callback);
    }

    public void publishArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId + "/publish";
        apiClient.put(endpoint, null, callback);
    }

    public void updateArticleStatus(String articleId, String status, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId + "/status?status=" + status;
        apiClient.put(endpoint, null, callback);
    }

    public void getAllArticlesAdmin(int page, int limit, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/admin/all?page=" + page + "&limit=" + limit;
        apiClient.get(endpoint, callback);
    }

    public void getPendingArticles(ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/admin/pending";
        apiClient.get(endpoint, callback);
    }

    public void approveArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/admin/approve/" + articleId;
        apiClient.put(endpoint, null, callback);
    }

    public void rejectArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/admin/reject/" + articleId;
        apiClient.put(endpoint, null, callback);
    }
}
