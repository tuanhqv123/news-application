package com.example.newsapplication.model.request;

import com.google.gson.annotations.SerializedName;

public class ArticleCreate {
    @SerializedName("title")
    private String title;

    @SerializedName("summary")
    private String summary;

    @SerializedName("content")
    private String content;

    @SerializedName("category_id")
    private int categoryId;

    @SerializedName("channel_id")
    private Integer channelId;

    @SerializedName("source_url")
    private String sourceUrl;

    @SerializedName("hero_image_url")
    private String heroImageUrl;

    @SerializedName("language")
    private String language;

    public ArticleCreate(String title, String summary, String content, int categoryId) {
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.categoryId = categoryId;
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }

    public Integer getChannelId() { return channelId; }
    public void setChannelId(Integer channelId) { this.channelId = channelId; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getHeroImageUrl() { return heroImageUrl; }
    public void setHeroImageUrl(String heroImageUrl) { this.heroImageUrl = heroImageUrl; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}