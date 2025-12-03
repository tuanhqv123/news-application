package com.example.newsapplication.utils;

import android.content.Context;
import com.android.volley.VolleyError;
import org.json.JSONObject;

public class NetworkUtils {
    public static String getErrorMessage(Context context, VolleyError error) {
        if (error.networkResponse != null && error.networkResponse.data != null) {
            try {
                JSONObject errorData = new JSONObject(new String(error.networkResponse.data));
                if (errorData.has("detail")) {
                    return errorData.getString("detail");
                }
            } catch (Exception e) {
                // Failed to parse response body
            }
        }
        
        // If no detail field found, return generic message
        return "An error occurred";
    }

    public static int getStatusCode(VolleyError error) {
        return error.networkResponse != null ? error.networkResponse.statusCode : -1;
    }
}