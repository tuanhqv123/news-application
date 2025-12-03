package com.example.newsapplication.model;

import org.json.JSONObject;

public class Category {
    private int id;
    private String name;
    private String slug;

    public Category() {}

    public Category(int id, String name, String slug) {
        this.id = id;
        this.name = name;
        this.slug = slug;
    }

    public static Category fromJson(JSONObject json) {
        Category category = new Category();
        category.id = json.optInt("id", 0);
        category.name = json.optString("name", "");
        category.slug = json.optString("slug", "");
        return category;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }
}
