package com.example.newsapplication.utils;

import android.content.Context;
import com.android.volley.VolleyError;
import org.json.JSONObject;

public class NetworkUtils {
    public static String getErrorMessage(Context context, VolleyError error) {
        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            
            if (error.networkResponse.data != null) {
                try {
                    JSONObject errorData = new JSONObject(new String(error.networkResponse.data));
                    if (errorData.has("detail")) {
                        return errorData.getString("detail");
                    }
                } catch (Exception e) {
                }
            }
            
            switch (statusCode) {
                case 400: return "Bad request. Please check your input.";
                case 401: return "Authentication failed. Please check your credentials.";
                case 403: return "Access denied. You don't have permission.";
                case 404: return "Resource not found.";
                case 422: return "Validation error. Please check your input.";
                case 500: return "Server error. Please try again later.";
                default: return "Network error occurred.";
            }
        }
        return "Network connection failed. Please check your internet.";
    }

    public static int getStatusCode(VolleyError error) {
        return error.networkResponse != null ? error.networkResponse.statusCode : -1;
    }
}