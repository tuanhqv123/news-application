package com.example.newsapplication.model.request;

import com.google.gson.annotations.SerializedName;

public class UserRegister {
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    @SerializedName("display_name")
    private String displayName;

    public UserRegister(String email, String password, String displayName) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}