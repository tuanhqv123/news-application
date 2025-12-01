package com.example.newsapplication.api.endpoints;

import com.example.newsapplication.api.ApiClient;
import org.json.JSONObject;

public class MediaEndpoints {
    private final ApiClient apiClient;

    public MediaEndpoints(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void uploadFile(String filePath, String description, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = "/api/v1/media/upload";
        
        // Create request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("file", filePath);
            requestBody.put("description", description);
        } catch (Exception e) {
            // Handle error
        }
        
        apiClient.post(endpoint, requestBody, callback);
    }

    public void uploadImage(String base64Image, ApiClient.ApiCallback<JSONObject> callback) {
        String endpoint = "/api/v1/media/upload";
        
        // Create request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("file", base64Image);
            requestBody.put("description", "Profile avatar upload");
        } catch (Exception e) {
            // Handle error
        }
        
        apiClient.post(endpoint, requestBody, callback);
    }
}
