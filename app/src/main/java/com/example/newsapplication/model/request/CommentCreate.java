package com.example.newsapplication.model.request;

import com.google.gson.annotations.SerializedName;

public class CommentCreate {
    @SerializedName("content")
    private String content;

    public CommentCreate(String content) {
        this.content = content;
    }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}