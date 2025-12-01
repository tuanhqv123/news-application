package com.example.newsapplication.api.endpoints;

import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiConfig;
import org.json.JSONObject;
import java.io.File;

public class MediaEndpoints {
    private final ApiClient apiClient;

    public MediaEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void uploadFile(File file, ApiClient.ApiCallback<JSONObject> callback) {
        // Note: For multipart upload, Volley needs extension or custom implementation
        // This is a placeholder - implement proper multipart request
        apiClient.post(ApiConfig.API_VERSION + "/media/upload", null, callback);
    }
}