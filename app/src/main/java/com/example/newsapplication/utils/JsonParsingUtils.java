package com.example.newsapplication.utils;

import android.util.Log;
import com.example.newsapplication.R;
import com.example.newsapplication.model.Article;
import com.example.newsapplication.model.Channel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Centralized JSON parsing utility to eliminate duplicate parsing logic
 */
public class JsonParsingUtils {
    private static final String TAG = "JsonParsingUtils";

    /**
     * Parse articles from various API response formats
     * Handles: {"data": {"articles": [...]}}, {"results": [...]}, {"articles": [...]}, {"data": [...]}
     */
    public static List<Article> parseArticles(JSONObject response) {
        List<Article> articles = new ArrayList<>();
        
        try {
            JSONArray articlesArray = extractArticlesArray(response);
            
            if (articlesArray != null) {
                for (int i = 0; i < articlesArray.length(); i++) {
                    Article article = parseArticle(articlesArray.getJSONObject(i));
                    if (article != null) {
                        articles.add(article);
                    }
                }
                Log.d(TAG, "Successfully parsed " + articles.size() + " articles");
            } else {
                Log.w(TAG, "No articles array found in response");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing articles from response", e);
        }
        
        return articles;
    }

    /**
     * Parse a single article from JSON
     */
    public static Article parseArticle(JSONObject articleJson) {
        try {
            String id = articleJson.optString("id", "");
            String title = articleJson.optString("title", "Unknown Title");
            String summary = articleJson.optString("summary", "");
            String content = articleJson.optString("content", "");
            
            // Handle different field names for source
            String source = articleJson.optString("source", "");
            if (source.isEmpty()) {
                source = articleJson.optString("source_url", "Unknown Source");
                // Extract domain from URL if it's a URL
                if (source.startsWith("http")) {
                    try {
                        java.net.URL url = new java.net.URL(source);
                        source = url.getHost();
                    } catch (Exception e) {
                        // Keep original if parsing fails
                    }
                }
            }
            
            // Handle channel_id as category if category not available
            String category = articleJson.optString("category", "");
            if (category.isEmpty()) {
                int channelId = articleJson.optInt("channel_id", 0);
                if (channelId > 0) {
                    category = "Channel " + channelId;
                } else {
                    category = "General";
                }
            }
            
            String author = articleJson.optString("author", "Unknown Author");
            String imageUrl = articleJson.optString("hero_image_url", "");
            String createdAt = articleJson.optString("created_at", "");
            
            // Use placeholder image if no URL
            int imageResId = imageUrl.isEmpty() ? R.drawable.placeholder_image : R.drawable.ic_launcher_foreground;
            
            return new Article(id, title, summary, content, source, category, author, imageUrl, imageResId, createdAt, false);
        } catch (Exception e) {
            Log.e(TAG, "Error parsing single article", e);
            return null;
        }
    }

    /**
     * Parse channels from various API response formats
     */
    public static List<Channel> parseChannels(JSONObject response) {
        List<Channel> channels = new ArrayList<>();
        
        try {
            JSONArray channelsArray = extractChannelsArray(response);
            
            if (channelsArray != null) {
                for (int i = 0; i < channelsArray.length(); i++) {
                    Channel channel = Channel.fromJson(channelsArray.getJSONObject(i));
                    if (channel != null) {
                        channels.add(channel);
                    }
                }
                Log.d(TAG, "Successfully parsed " + channels.size() + " channels");
            } else {
                Log.w(TAG, "No channels array found in response");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing channels from response", e);
        }
        
        return channels;
    }

    /**
     * Parse bookmarked article IDs from API response
     */
    public static Set<String> parseBookmarkedIds(JSONObject response) {
        Set<String> bookmarkedIds = new HashSet<>();
        
        try {
            JSONArray bookmarksArray = extractBookmarksArray(response);
            
            if (bookmarksArray != null) {
                for (int i = 0; i < bookmarksArray.length(); i++) {
                    JSONObject bookmark = bookmarksArray.getJSONObject(i);
                    String articleId = bookmark.optString("article_id", "");
                    if (!articleId.isEmpty()) {
                        bookmarkedIds.add(articleId);
                    }
                }
                Log.d(TAG, "Successfully parsed " + bookmarkedIds.size() + " bookmarked IDs");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error parsing bookmarked IDs", e);
        }
        
        return bookmarkedIds;
    }

    /**
     * Extract articles array from various response formats
     */
    private static JSONArray extractArticlesArray(JSONObject response) {
        try {
            // Try: {"data": {"articles": [...]}}
            if (response.has("data") && response.opt("data") instanceof JSONObject) {
                JSONObject dataObj = response.getJSONObject("data");
                if (dataObj.has("articles")) {
                    return dataObj.getJSONArray("articles");
                }
                if (dataObj.has("results")) {
                    return dataObj.getJSONArray("results");
                }
            }
            
            // Try: {"articles": [...]}
            if (response.has("articles")) {
                return response.getJSONArray("articles");
            }
            
            // Try: {"results": [...]}
            if (response.has("results")) {
                return response.getJSONArray("results");
            }
            
            // Try: {"data": [...]}
            if (response.has("data") && response.opt("data") instanceof JSONArray) {
                return response.getJSONArray("data");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting articles array", e);
        }
        
        return null;
    }

    /**
     * Extract channels array from various response formats
     */
    private static JSONArray extractChannelsArray(JSONObject response) {
        try {
            // Try: {"data": {"channels": [...]}}
            if (response.has("data") && response.opt("data") instanceof JSONObject) {
                JSONObject dataObj = response.getJSONObject("data");
                if (dataObj.has("channels")) {
                    return dataObj.getJSONArray("channels");
                }
            }
            
            // Try: {"channels": [...]}
            if (response.has("channels")) {
                return response.getJSONArray("channels");
            }
            
            // Try: {"results": [...]}
            if (response.has("results")) {
                return response.getJSONArray("results");
            }
            
            // Try: {"data": [...]}
            if (response.has("data") && response.opt("data") instanceof JSONArray) {
                return response.getJSONArray("data");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting channels array", e);
        }
        
        return null;
    }

    /**
     * Extract bookmarks array from various response formats
     */
    private static JSONArray extractBookmarksArray(JSONObject response) {
        try {
            // Try: {"data": {"bookmarks": [...]}}
            if (response.has("data") && response.opt("data") instanceof JSONObject) {
                JSONObject dataObj = response.getJSONObject("data");
                if (dataObj.has("bookmarks")) {
                    return dataObj.getJSONArray("bookmarks");
                }
            }
            
            // Try: {"bookmarks": [...]}
            if (response.has("bookmarks")) {
                return response.getJSONArray("bookmarks");
            }
            
            // Try: {"results": [...]}
            if (response.has("results")) {
                return response.getJSONArray("results");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting bookmarks array", e);
        }
        
        return null;
    }
}
