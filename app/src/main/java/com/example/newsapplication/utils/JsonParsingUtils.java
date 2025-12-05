package com.example.newsapplication.utils;

import com.example.newsapplication.R;
import com.example.newsapplication.model.Article;
import com.example.newsapplication.model.Category;
import com.example.newsapplication.model.Channel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Centralized JSON parsing utility to eliminate duplicate parsing logic
public class JsonParsingUtils {

    // Parse articles from various API response formats
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
            }
        } catch (Exception e) {
        }
        
        return articles;
    }

    // Parse a single article from JSON
    public static Article parseArticle(JSONObject articleJson) {
        try {
            String id = articleJson.optString("id", "");
            String title = articleJson.optString("title", "Unknown Title");
            String summary = articleJson.optString("summary", "");
            String content = articleJson.optString("content", "");
            
            String source = articleJson.optString("source", "");
            if (source.isEmpty()) {
                source = articleJson.optString("source_url", "Unknown Source");
                if (source.startsWith("http")) {
                    try {
                        java.net.URL url = new java.net.URL(source);
                        source = url.getHost();
                    } catch (Exception e) {
                    }
                }
            }
            
            String category = articleJson.optString("category", "");
            if (category.isEmpty()) {
                int channelId = articleJson.optInt("channel_id", 0);
                if (channelId > 0) {
                    category = "Channel " + channelId;
                } else {
                    category = "General";
                }
            }
            
            String channelName = "";
            if (articleJson.has("channels") && !articleJson.isNull("channels")) {
                JSONObject channelObj = articleJson.optJSONObject("channels");
                if (channelObj != null) {
                    channelName = channelObj.optString("name", "");
                }
            }
            
            String author = articleJson.optString("author", "Unknown Author");
            String imageUrl = articleJson.optString("hero_image_url", "");
            String createdAt = articleJson.optString("created_at", "");
            String publishedAt = articleJson.optString("published_at", createdAt);
            String status = articleJson.optString("status", "");
            
            int imageResId = imageUrl.isEmpty() ? R.drawable.placeholder_image : R.drawable.ic_launcher_foreground;
            
            Article article = new Article(id, title, summary, content, author, source, category, imageUrl, imageResId, createdAt, false);
            article.setChannelName(channelName);
            article.setPublishedAt(publishedAt);
            return article;
        } catch (Exception e) {
            return null;
        }
    }

    // Parse channels from various API response formats
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
            }
        } catch (Exception e) {
        }
        
        return channels;
    }

    // Parse bookmarked article IDs from API response
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
            }
        } catch (Exception e) {
        }
        
        return bookmarkedIds;
    }

    // Extract articles array from various response formats
    private static JSONArray extractArticlesArray(JSONObject response) {
        try {
            if (response.has("data") && response.opt("data") instanceof JSONObject) {
                JSONObject dataObj = response.getJSONObject("data");
                if (dataObj.has("articles")) {
                    return dataObj.getJSONArray("articles");
                }
                if (dataObj.has("results")) {
                    return dataObj.getJSONArray("results");
                }
            }
            
            if (response.has("articles")) {
                return response.getJSONArray("articles");
            }
            
            if (response.has("results")) {
                return response.getJSONArray("results");
            }
            
            if (response.has("data") && response.opt("data") instanceof JSONArray) {
                return response.getJSONArray("data");
            }
        } catch (Exception e) {
        }
        
        return null;
    }

    // Extract channels array from various response formats
    private static JSONArray extractChannelsArray(JSONObject response) {
        try {
            if (response.has("data") && response.opt("data") instanceof JSONObject) {
                JSONObject dataObj = response.getJSONObject("data");
                if (dataObj.has("channels")) {
                    return dataObj.getJSONArray("channels");
                }
            }
            
            if (response.has("channels")) {
                return response.getJSONArray("channels");
            }
            
            if (response.has("results")) {
                return response.getJSONArray("results");
            }
            
            if (response.has("data") && response.opt("data") instanceof JSONArray) {
                return response.getJSONArray("data");
            }
        } catch (Exception e) {
        }
        
        return null;
    }

    // Parse categories from API response
    public static List<Category> parseCategories(JSONObject response) {
        List<Category> categories = new ArrayList<>();
        
        try {
            JSONArray categoriesArray = extractCategoriesArray(response);
            
            if (categoriesArray != null) {
                for (int i = 0; i < categoriesArray.length(); i++) {
                    Category category = Category.fromJson(categoriesArray.getJSONObject(i));
                    if (category != null) {
                        categories.add(category);
                    }
                }
            }
        } catch (Exception e) {
        }
        
        return categories;
    }

    // Extract categories array from various response formats
    private static JSONArray extractCategoriesArray(JSONObject response) {
        try {
            if (response.has("data") && response.opt("data") instanceof JSONObject) {
                JSONObject dataObj = response.getJSONObject("data");
                if (dataObj.has("categories")) {
                    return dataObj.getJSONArray("categories");
                }
            }
            
            if (response.has("categories")) {
                return response.getJSONArray("categories");
            }
            
            if (response.has("results")) {
                return response.getJSONArray("results");
            }
            
            if (response.has("data") && response.opt("data") instanceof JSONArray) {
                return response.getJSONArray("data");
            }
        } catch (Exception e) {
        }
        
        return null;
    }

    // Extract bookmarks array from various response formats
    private static JSONArray extractBookmarksArray(JSONObject response) {
        try {
            if (response.has("data") && response.opt("data") instanceof JSONObject) {
                JSONObject dataObj = response.getJSONObject("data");
                if (dataObj.has("bookmarks")) {
                    return dataObj.getJSONArray("bookmarks");
                }
            }
            
            if (response.has("bookmarks")) {
                return response.getJSONArray("bookmarks");
            }
            
            if (response.has("results")) {
                return response.getJSONArray("results");
            }
        } catch (Exception e) {
        }
        
        return null;
    }
}
