package com.example.newsapplication.api.endpoints;

import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import org.json.JSONObject;

/**
 * API endpoints for Article management
 */
public class ArticleEndpoints {
    private final ApiClient apiClient;

    public ArticleEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    // ==================== Public Endpoints ====================

    /**
     * Get published articles with pagination
     */
    public void getArticles(ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/";
        android.util.Log.d("ArticleEndpoints", "Calling GET " + endpoint);
        apiClient.get(endpoint, callback);
    }

    /**
     * Get published articles with pagination and category filter
     */
    public void getArticles(int page, int limit, Integer categoryId, ApiClient.ApiCallback<JSONObject> callback) {
        StringBuilder endpoint = new StringBuilder(ApiConfig.API_VERSION + "/articles/?");
        endpoint.append("page=").append(page);
        endpoint.append("&limit=").append(limit);
        if (categoryId != null) {
            endpoint.append("&category=").append(categoryId);
        }
        
        android.util.Log.d("ArticleEndpoints", "Calling GET " + endpoint.toString());
        apiClient.get(endpoint.toString(), callback);
    }

    /**
     * Get a specific article by ID
     */
    public void getArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId;
        apiClient.get(endpoint, callback);
    }

    /**
     * Search articles
     */
    public void searchArticles(String query, int page, int limit, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/search?q=" + query + "&page=" + page + "&limit=" + limit;
        android.util.Log.d("ArticleEndpoints", "Calling GET " + endpoint);
        apiClient.get(endpoint, callback);
    }

    // ==================== Comments Endpoints ====================

    /**
     * Get comments for an article
     */
    public void getComments(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId + "/comments";
        apiClient.get(endpoint, callback);
    }

    /**
     * Add a comment to an article
     */
    public void addComment(String articleId, String content, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId + "/comments";
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("content", content);
        } catch (Exception e) {
            // Handle error
        }
        apiClient.post(endpoint, requestBody, callback);
    }

    // ==================== Bookmark Endpoints ====================

    /**
     * Bookmark an article
     */
    public void bookmarkArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId + "/bookmark";
        apiClient.post(endpoint, null, callback);
    }

    /**
     * Remove bookmark from an article
     */
    public void removeBookmark(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId + "/bookmark";
        apiClient.delete(endpoint, callback);
    }

    /**
     * Get user's bookmarked articles
     */
    public void getBookmarks(ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/users/me/bookmarks";
        apiClient.get(endpoint, callback);
    }

    // ==================== Author/Admin Endpoints ====================

    /**
     * Create a new article (requires auth)
     */
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
        } catch (Exception e) {
            // Handle error
        }
        apiClient.post(endpoint, requestBody, callback);
    }

    /**
     * Publish an article (requires auth)
     */
    public void publishArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId + "/publish";
        apiClient.put(endpoint, null, callback);
    }

    /**
     * Update article status (admin)
     */
    public void updateArticleStatus(String articleId, String status, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/" + articleId + "/status?status=" + status;
        apiClient.put(endpoint, null, callback);
    }

    // ==================== Admin Endpoints ====================

    /**
     * Get all articles including unpublished (admin only)
     */
    public void getAllArticlesAdmin(int page, int limit, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/admin/all?page=" + page + "&limit=" + limit;
        apiClient.get(endpoint, callback);
    }

    /**
     * Get pending articles (admin only)
     */
    public void getPendingArticles(ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/admin/pending";
        apiClient.get(endpoint, callback);
    }

    /**
     * Approve an article (admin only)
     */
    public void approveArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/admin/approve/" + articleId;
        apiClient.put(endpoint, null, callback);
    }

    /**
     * Reject an article (admin only)
     */
    public void rejectArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = ApiConfig.API_VERSION + "/articles/admin/reject/" + articleId;
        apiClient.put(endpoint, null, callback);
    }
}
