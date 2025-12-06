package com.example.newsapplication.model;

import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("user_id")
    private String userId;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("role_id")
    private int roleId;

    @SerializedName("roles")
    private Role roles;

    @SerializedName("channel_id")
    private Integer channelId;

    @SerializedName("banned_until")
    private String bannedUntil;

    @SerializedName("is_super_admin")
    private boolean isSuperAdmin;

    @SerializedName("email")
    private String email;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("updated_at")
    private String updatedAt;

    // Nested Role class
    public static class Role {
        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    // Constructors
    public User() {
    }

    public User(String userId, String displayName, int roleId) {
        this.userId = userId;
        this.displayName = displayName;
        this.roleId = roleId;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public Role getRoles() {
        return roles;
    }

    public void setRoles(Role roles) {
        this.roles = roles;
    }

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }

    public String getBannedUntil() {
        return bannedUntil;
    }

    public void setBannedUntil(String bannedUntil) {
        this.bannedUntil = bannedUntil;
    }

    public boolean isSuperAdmin() {
        return isSuperAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        isSuperAdmin = superAdmin;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    // Helper methods
    public String getRole() {
        return getRoleName();
    }

    public String getRoleName() {
        if (roles != null && roles.getName() != null) {
            return roles.getName();
        }
        // Fallback based on role_id
        switch (roleId) {
            case 1:
                return "admin";
            case 2:
                return "author";
            case 3:
                return "reader";
            default:
                return "unknown";
        }
    }

    public String getRoleDescription() {
        if (roles != null && roles.getDescription() != null) {
            return roles.getDescription();
        }
        return "";
    }

    public boolean isBanned() {
        return bannedUntil != null && !bannedUntil.isEmpty();
    }

    public boolean isAdmin() {
        return roleId == 1;
    }

    public boolean isAuthor() {
        return roleId == 2;
    }

    public boolean isReader() {
        return roleId == 3;
    }
}
