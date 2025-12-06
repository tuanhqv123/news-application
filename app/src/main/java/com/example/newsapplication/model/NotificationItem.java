package com.example.newsapplication.model;

public class NotificationItem {
    private String fcmToken;
    private String deviceType;
    private String lastUsedAt;
    private String createdAt;

    public NotificationItem() {}

    public NotificationItem(String fcmToken, String deviceType, String lastUsedAt, String createdAt) {
        this.fcmToken = fcmToken;
        this.deviceType = deviceType;
        this.lastUsedAt = lastUsedAt;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(String lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}