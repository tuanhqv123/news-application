package com.example.newsapplication.utils;

import android.net.Uri;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Utility class for building URLs with proper query parameter encoding
 */
public class UrlBuilder {
    private static final String TAG = "UrlBuilder";

    /**
     * Build URL with query parameters using proper encoding
     * @param baseUrl Base URL without query parameters
     * @param params Map of query parameters
     * @return Complete URL with encoded query parameters
     */
    public static String buildUrl(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }

        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        
        return builder.build().toString();
    }

    /**
     * URL encode a string value
     * @param value String to encode
     * @return URL-encoded string
     */
    public static String encode(String value) {
        if (value == null) {
            return "";
        }
        
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to encode URL parameter: " + value, e);
            return value;
        }
    }

    /**
     * Build URL with query parameters for optional values
     * Only adds parameters that are not null
     */
    public static String buildUrlWithOptionalParams(String baseUrl, Object... params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Parameters must be in key-value pairs");
        }

        Uri.Builder builder = Uri.parse(baseUrl).buildUpon();
        
        for (int i = 0; i < params.length; i += 2) {
            String key = (String) params[i];
            Object value = params[i + 1];
            
            if (value != null) {
                builder.appendQueryParameter(key, String.valueOf(value));
            }
        }
        
        return builder.build().toString();
    }
}
