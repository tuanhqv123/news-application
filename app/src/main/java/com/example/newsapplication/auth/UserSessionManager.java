package com.example.newsapplication.auth;

import android.content.Context;
import android.content.SharedPreferences;

public class UserSessionManager {

    private static final String PREF_NAME = "NewsAppSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_AUTH_TOKEN = "authToken";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";

    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private Context context;

    public UserSessionManager(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void createLoginSession(String email, String name, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_ROLE, role);
        editor.apply();
    }

    public void createLoginSession(String email, String name, String role, String authToken) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_AUTH_TOKEN, authToken);
        editor.apply();
    }

    public void createLoginSession(String email, String name, String role, String authToken, String refreshToken) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_ROLE, role);
        editor.putString(KEY_AUTH_TOKEN, authToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    public void logoutUser() {
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "guest");
    }

    public String getAuthToken() {
        return prefs.getString(KEY_AUTH_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void setAuthToken(String token) {
        editor.putString(KEY_AUTH_TOKEN, token);
        editor.apply();
    }

    public void setRefreshToken(String refreshToken) {
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);
        editor.apply();
    }

    public boolean isReader() {
        return "reader".equals(getUserRole());
    }

    public boolean isAuthor() {
        return "author".equals(getUserRole());
    }

    public boolean isAdmin() {
        return "admin".equals(getUserRole());
    }

    public void updateUserName(String newName) {
        editor.putString(KEY_USER_NAME, newName);
        editor.apply();
    }

    private static final String KEY_AVATAR_URL = "avatarUrl";

    public String getAvatarUrl() {
        String avatarUrl = prefs.getString(KEY_AVATAR_URL, null);
        android.util.Log.d("UserSessionManager", "getAvatarUrl() returning: " + avatarUrl);
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        android.util.Log.d("UserSessionManager", "setAvatarUrl() called with: " + avatarUrl);
        editor.putString(KEY_AVATAR_URL, avatarUrl);
        editor.apply();
    }
}