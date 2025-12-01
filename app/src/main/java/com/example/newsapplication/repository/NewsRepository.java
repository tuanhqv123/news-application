package com.example.newsapplication.repository;

import android.content.Context;
import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.api.endpoints.*;
import org.json.JSONObject;

public class NewsRepository {
    private final ApiClient apiClient;
    private final AuthEndpoints authEndpoints;
    private final ArticleEndpoints articleEndpoints;
    private final UserEndpoints userEndpoints;
    private final CategoryEndpoints categoryEndpoints;
    private final MediaEndpoints mediaEndpoints;

    public NewsRepository(Context context) {
        this.apiClient = new ApiClient(context);
        this.authEndpoints = new AuthEndpoints(apiClient);
        this.articleEndpoints = new ArticleEndpoints(apiClient);
        this.userEndpoints = new UserEndpoints(apiClient);
        this.categoryEndpoints = new CategoryEndpoints(apiClient);
        this.mediaEndpoints = new MediaEndpoints(apiClient);
    }

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

    public void getArticles(int page, int limit, Integer categoryId, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.getArticles(page, limit, categoryId, wrapCallback(callback));
    }

    public void createArticle(String title, String summary, String content, int categoryId, RepositoryCallback<JSONObject> callback) {
        articleEndpoints.createArticle(title, summary, content, categoryId, wrapCallback(callback));
    }

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