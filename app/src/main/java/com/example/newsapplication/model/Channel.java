package com.example.newsapplication.model;

import org.json.JSONObject;

/**
 * Model class representing a news channel.
 */
public class Channel {
    private int id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private String rssUrl;
    private boolean isActive;
    private boolean isFollowing;
    private String createdAt;
    private String updatedAt;

    public Channel() {}

    public Channel(int id, String name, String slug, String description, 
                   String logoUrl, boolean isFollowing) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.logoUrl = logoUrl;
        this.isFollowing = isFollowing;
    }

    /**
     * Create a Channel from JSON response
     */
    public static Channel fromJson(JSONObject json) {
        Channel channel = new Channel();
        try {
            channel.id = json.optInt("id", 0);
            channel.name = json.optString("name", "Unknown Channel");
            channel.slug = json.optString("slug", "");
            channel.description = json.optString("description", "");
            channel.logoUrl = json.optString("logo_url", "");
            channel.rssUrl = json.optString("rss_url", "");
            channel.isActive = json.optBoolean("is_active", true);
            channel.createdAt = json.optString("created_at", "");
            channel.updatedAt = json.optString("updated_at", "");
            // Check if user is following
            channel.isFollowing = json.optBoolean("is_following", false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return channel;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getRssUrl() {
        return rssUrl;
    }

    public void setRssUrl(String rssUrl) {
        this.rssUrl = rssUrl;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
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
}
