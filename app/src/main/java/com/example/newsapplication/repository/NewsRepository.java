package com.example.newsapplication.repository;

import android.content.Context;
import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.api.endpoints.*;
import org.json.JSONObject;

/**
 * Repository class that acts as a single source of truth for all data operations.
 * Provides a clean API for the UI layer to fetch/manipulate data.
 */
public class NewsRepository {
    private final ApiClient apiClient;
    private final AuthEndpoints authEndpoints;
    private final ArticleEndpoints articleEndpoints;
    private final UserEndpoints userEndpoints;
    private final CategoryEndpoints categoryEndpoints;
    private final ChannelEndpoints channelEndpoints;
    private final MediaEndpoints mediaEndpoints;

    public NewsRepository(Context context) {
        this.apiClient = new ApiClient(context);
        this.authEndpoints = new AuthEndpoints(apiClient);
        this.articleEndpoints = new ArticleEndpoints(apiClient);
        this.userEndpoints = new UserEndpoints(apiClient);
        this.categoryEndpoints = new CategoryEndpoints(apiClient);
        this.channelEndpoints = new ChannelEndpoints(apiClient);
        this.mediaEndpoints = new MediaEndpoints(apiClient);
    }

    // ==================== Auth ====================

    public void login(String email, String password, RepositoryCallback<JSONObject> callback) {
        authEndpoints.login(email, password, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    try {
                        String token = response.getData().optString("access_token");
                        if (!token.isEmpty()) {
                            apiClient.setAuthToken(token);
                        }
                    } catch (Exception e) {
                        // Handle token extraction error
                    }
                }
                callback.onResult(response);
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                callback.onResult(error);
            }
        });
    }

    // ==================== Articles ====================

    public void getArticles(RepositoryCallback<JSONObject> callback) {
        android.util.Log.d("NewsRepository", "Calling getArticles API");
        articleEndpoints.getArticles(wrapCallback(callback));
    }

    public void getArticles(int page, int limit, Integer categoryId, RepositoryCallback<JSONObject> callback) {
        android.util.Log.d("NewsRepository", "Calling getArticles with page=" + page + ", limit=" + limit + ", categoryId=" + categoryId);
        articleEndpoints.getArticles(page, limit, categoryId, wrapCallback(callback));
    }

    public void getArticle(String articleId, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.getArticle(articleId, wrapCallback(callback));
    }

    public void searchArticles(String query, int page, int limit, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.searchArticles(query, page, limit, wrapCallback(callback));
    }

    public void createArticle(String title, String summary, String content, int categoryId, 
                              Integer channelId, String sourceUrl, String heroImageUrl, 
                              String language, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.createArticle(title, summary, content, categoryId, channelId, 
                                        sourceUrl, heroImageUrl, language, wrapCallback(callback));
    }

    public void publishArticle(String articleId, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.publishArticle(articleId, wrapCallback(callback));
    }

    // ==================== Comments ====================

    public void getComments(String articleId, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.getComments(articleId, wrapCallback(callback));
    }

    public void addComment(String articleId, String content, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.addComment(articleId, content, wrapCallback(callback));
    }

    // ==================== Bookmarks ====================

    public void bookmarkArticle(String articleId, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.bookmarkArticle(articleId, wrapCallback(callback));
    }

    public void removeBookmark(String articleId, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.removeBookmark(articleId, wrapCallback(callback));
    }

    public void getBookmarks(RepositoryCallback<JSONObject> callback) {
        articleEndpoints.getBookmarks(wrapCallback(callback));
    }

    // ==================== Categories ====================

    public void getCategories(RepositoryCallback<JSONObject> callback) {
        android.util.Log.d("NewsRepository", "Calling getCategories API");
        categoryEndpoints.getCategories(wrapCallback(callback));
    }

    public void getCategoryArticles(int categoryId, int page, int limit, RepositoryCallback<JSONObject> callback) {
        categoryEndpoints.getCategoryArticles(categoryId, page, limit, wrapCallback(callback));
    }

    // ==================== Channels ====================

    public void getPublicChannels(RepositoryCallback<JSONObject> callback) {
        channelEndpoints.getPublicChannels(wrapCallback(callback));
    }

    public void getAllChannels(RepositoryCallback<JSONObject> callback) {
        channelEndpoints.getAllChannels(wrapCallback(callback));
    }

    public void getFollowedChannels(RepositoryCallback<JSONObject> callback) {
        channelEndpoints.getFollowedChannels(wrapCallback(callback));
    }

    public void followChannel(int channelId, RepositoryCallback<JSONObject> callback) {
        channelEndpoints.followChannel(channelId, wrapCallback(callback));
    }

    public void unfollowChannel(int channelId, RepositoryCallback<JSONObject> callback) {
        channelEndpoints.unfollowChannel(channelId, wrapCallback(callback));
    }

    public void getChannelArticles(int channelId, int page, int limit, RepositoryCallback<JSONObject> callback) {
        channelEndpoints.getChannelArticles(channelId, page, limit, wrapCallback(callback));
    }

    public void subscribeChannel(int channelId, RepositoryCallback<JSONObject> callback) {
        categoryEndpoints.subscribeChannel(channelId, wrapCallback(callback));
    }

    public void unsubscribeChannel(int channelId, RepositoryCallback<JSONObject> callback) {
        categoryEndpoints.unsubscribeChannel(channelId, wrapCallback(callback));
    }

    // ==================== User Profile ====================

    public void updateProfile(String displayName, String avatarUrl, RepositoryCallback<JSONObject> callback) {
        JSONObject requestBody = new JSONObject();
        try {
            if (displayName != null && !displayName.isEmpty()) {
                requestBody.put("display_name", displayName);
            }
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                requestBody.put("avatar_url", avatarUrl);
            }
        } catch (Exception e) {
            // Handle error
        }
        userEndpoints.updateAuthProfile(requestBody, wrapCallback(callback));
    }

    // ==================== Admin: Articles ====================

    public void getAllArticlesAdmin(int page, int limit, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.getAllArticlesAdmin(page, limit, wrapCallback(callback));
    }

    public void getPendingArticles(RepositoryCallback<JSONObject> callback) {
        articleEndpoints.getPendingArticles(wrapCallback(callback));
    }

    public void approveArticle(String articleId, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.approveArticle(articleId, wrapCallback(callback));
    }

    public void rejectArticle(String articleId, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.rejectArticle(articleId, wrapCallback(callback));
    }

    public void updateArticleStatus(String articleId, String status, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.updateArticleStatus(articleId, status, wrapCallback(callback));
    }

    // ==================== Admin: Users ====================

    public void getPendingAuthors(RepositoryCallback<JSONObject> callback) {
        userEndpoints.getPendingAuthors(wrapCallback(callback));
    }

    public void approveAuthor(String userId, RepositoryCallback<JSONObject> callback) {
        userEndpoints.approveAuthor(userId, wrapCallback(callback));
    }

    public void getAllUserProfiles(RepositoryCallback<JSONObject> callback) {
        userEndpoints.getAllUserProfiles(wrapCallback(callback));
    }

    public void banUser(String userId, RepositoryCallback<JSONObject> callback) {
        userEndpoints.banUser(userId, wrapCallback(callback));
    }

    public void unbanUser(String userId, RepositoryCallback<JSONObject> callback) {
        userEndpoints.unbanUser(userId, wrapCallback(callback));
    }

    // ==================== Admin: Channels ====================

    public void createChannel(String name, String slug, String description, String rssUrl, String logoUrl, RepositoryCallback<JSONObject> callback) {
        channelEndpoints.createChannel(name, slug, description, rssUrl, logoUrl, wrapCallback(callback));
    }

    public void updateChannel(int channelId, String name, String description, String rssUrl, String logoUrl, Boolean isActive, RepositoryCallback<JSONObject> callback) {
        channelEndpoints.updateChannel(channelId, name, description, rssUrl, logoUrl, isActive, wrapCallback(callback));
    }

    public void deleteChannel(int channelId, RepositoryCallback<JSONObject> callback) {
        channelEndpoints.deleteChannel(channelId, wrapCallback(callback));
    }

    // ==================== Media ====================

    public void uploadFile(String filePath, String description, RepositoryCallback<JSONObject> callback) {
        mediaEndpoints.uploadFile(filePath, description, wrapCallback(callback));
    }

    public void uploadImage(String base64Image, RepositoryCallback<JSONObject> callback) {
        mediaEndpoints.uploadImage(base64Image, wrapCallback(callback));
    }

    // ==================== Helper Methods ====================

    private ApiClient.ApiCallback<JSONObject> wrapCallback(RepositoryCallback<JSONObject> callback) {
        return new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                callback.onResult(response);
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                callback.onResult(error);
            }
        };
    }

    public interface RepositoryCallback<T> {
        void onResult(ApiResponse<T> response);
    }
}