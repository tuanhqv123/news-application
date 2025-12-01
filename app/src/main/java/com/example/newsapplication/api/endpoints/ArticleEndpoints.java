package com.example.newsapplication.api.endpoints;

import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import com.example.newsapplication.model.request.ArticleCreate;
import com.example.newsapplication.model.request.CommentCreate;
import org.json.JSONObject;

public class ArticleEndpoints {
    private final ApiClient apiClient;

    public ArticleEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void getArticles(int page, int limit, Integer category, ApiClient.ApiCallback<JSONObject> callback) {
        String url = ApiConfig.API_VERSION + "/articles/?page=" + page + "&limit=" + limit;
        if (category != null) {
            url += "&category=" + category;
        }
        apiClient.get(url, callback);
    }

    public void createArticle(String title, String summary, String content, int categoryId, ApiClient.ApiCallback<JSONObject> callback) {
        ArticleCreate article = new ArticleCreate(title, summary, content, categoryId);
        apiClient.post(ApiConfig.API_VERSION + "/articles/", article, callback);
    }

    public void getArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/articles/" + articleId, callback);
    }

    public void getComments(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/articles/" + articleId + "/comments", callback);
    }

    public void addComment(String articleId, String content, ApiClient.ApiCallback<JSONObject> callback) {
        CommentCreate comment = new CommentCreate(content);
        apiClient.post(ApiConfig.API_VERSION + "/articles/" + articleId + "/comments", comment, callback);
    }

    public void bookmarkArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.post(ApiConfig.API_VERSION + "/articles/" + articleId + "/bookmark", null, callback);
    }

    public void removeBookmark(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.delete(ApiConfig.API_VERSION + "/articles/" + articleId + "/bookmark", callback);
    }

    public void publishArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.put(ApiConfig.API_VERSION + "/articles/" + articleId + "/publish", null, callback);
    }

    public void searchArticles(String query, int page, int limit, ApiClient.ApiCallback<JSONObject> callback) {
        String url = ApiConfig.API_VERSION + "/articles/search?q=" + query + "&page=" + page + "&limit=" + limit;
        apiClient.get(url, callback);
    }

    // Admin endpoints
    public void getAllArticlesAdmin(int page, int limit, ApiClient.ApiCallback<JSONObject> callback) {
        String url = ApiConfig.API_VERSION + "/articles/admin/all?page=" + page + "&limit=" + limit;
        apiClient.get(url, callback);
    }

    public void updateArticleStatus(String articleId, String status, ApiClient.ApiCallback<JSONObject> callback) {
        String url = ApiConfig.API_VERSION + "/articles/" + articleId + "/status?status=" + status;
        apiClient.put(url, null, callback);
    }

    public void getPendingArticles(ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.get(ApiConfig.API_VERSION + "/articles/admin/pending", callback);
    }

    public void approveArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.put(ApiConfig.API_VERSION + "/articles/admin/approve/" + articleId, null, callback);
    }

    public void rejectArticle(String articleId, ApiClient.ApiCallback<JSONObject> callback) {
        apiClient.put(ApiConfig.API_VERSION + "/articles/admin/reject/" + articleId, null, callback);
    }
}