package com.example.newsapplication.model;

public enum UserRole {
    ADMIN(1, "admin", "Full control over content and settings"),
    AUTHOR(2, "author", "Can create and manage own articles"),
    READER(3, "reader", "Default role for registered users");

    private final int id;
    private final String name;
    private final String description;

    UserRole(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public int getRoleId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public static UserRole fromId(int id) {
        for (UserRole role : values()) {
            if (role.id == id) {
                return role;
            }
        }
        return READER; // Default
    }

    public static UserRole fromName(String name) {
        for (UserRole role : values()) {
            if (role.name.equalsIgnoreCase(name)) {
                return role;
            }
        }
        return READER; // Default
    }
}
