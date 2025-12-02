package com.example.newsapplication.model;

import org.json.JSONObject;

/**
 * Model class representing a comment on an article.
 */
public class Comment {
    private int id;
    private String articleId;
    private String userId;
    private String userName;
    private String userAvatar;
    private String content;
    private String createdAt;
    private String updatedAt;

    public Comment() {}

    public Comment(int id, String articleId, String userId, String userName, 
                   String userAvatar, String content, String createdAt) {
        this.id = id;
        this.articleId = articleId;
        this.userId = userId;
        this.userName = userName;
        this.userAvatar = userAvatar;
        this.content = content;
        this.createdAt = createdAt;
    }

    /**
     * Create a Comment from JSON response
     * API Response: {"id": 9, "article_id": "...", "user_id": "...", "body": "haha", 
     *                "created_at": "...", "profile": {"display_name": "...", "avatar_url": "..."}}
     */
    public static Comment fromJson(JSONObject json) {
        Comment comment = new Comment();
        try {
            comment.id = json.optInt("id", 0);
            comment.articleId = json.optString("article_id", "");
            comment.userId = json.optString("user_id", "");
            comment.content = json.optString("body", "");
            comment.createdAt = json.optString("created_at", "");
            comment.updatedAt = json.optString("updated_at", "");
            
            // Parse profile object
            JSONObject profileObj = json.optJSONObject("profile");
            if (profileObj != null) {
                comment.userName = profileObj.optString("display_name", "Anonymous");
                comment.userAvatar = profileObj.optString("avatar_url", "");
            } else {
                comment.userName = "Anonymous";
                comment.userAvatar = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return comment;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    /**
     * Get a formatted time ago string
     */
    public String getTimeAgo() {
        if (createdAt == null || createdAt.isEmpty()) {
            return "";
        }
        try {
            // Parse ISO 8601 date
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US);
            java.util.Date date = sdf.parse(createdAt.replace("Z", "").split("\\.")[0]);
            if (date == null) return createdAt;
            
            long diff = System.currentTimeMillis() - date.getTime();
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            
            if (days > 0) {
                return days + (days == 1 ? " day ago" : " days ago");
            } else if (hours > 0) {
                return hours + (hours == 1 ? " hour ago" : " hours ago");
            } else if (minutes > 0) {
                return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
            } else {
                return "Just now";
            }
        } catch (Exception e) {
            return createdAt;
        }
    }
}
