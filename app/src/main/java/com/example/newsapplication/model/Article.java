package com.example.newsapplication.model;

import java.io.Serializable;
import java.util.List;

public class Article implements Serializable {
    private String id;
    private String title;
    private String description;
    private String content;
    private String author;
    private String source;
    private String category;
    private String channelName;
    private String publishedAt;
    private String imageUrl;
    private int imageResId;
    private String date;
    private boolean isBookmarked;
    private boolean isVideo;
    private String ttsAudioUrl;
    private int ttsDurationSeconds;
    private String status;

    public Article() {}

    public Article(String id, String title, String description, String content,
                   String author, String source, String category, String imageUrl,
                   int imageResId, String date, boolean isVideo) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.content = content;
        this.author = author;
        this.source = source;
        this.category = category;
        this.imageUrl = imageUrl;
        this.imageResId = imageResId;
        this.date = date;
        this.isVideo = isVideo;
        this.isBookmarked = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Alias for description (API uses "summary")
    public String getSummary() { return description; }
    public void setSummary(String summary) { this.description = summary; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public boolean isBookmarked() { return isBookmarked; }
    public void setBookmarked(boolean bookmarked) { isBookmarked = bookmarked; }

    public boolean isVideo() { return isVideo; }
    public void setVideo(boolean video) { isVideo = video; }

    public String getTtsAudioUrl() { return ttsAudioUrl; }
    public void setTtsAudioUrl(String ttsAudioUrl) { this.ttsAudioUrl = ttsAudioUrl; }

    public int getTtsDurationSeconds() { return ttsDurationSeconds; }
    public void setTtsDurationSeconds(int ttsDurationSeconds) { this.ttsDurationSeconds = ttsDurationSeconds; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public String getPublishedAt() { return publishedAt; }
    public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}