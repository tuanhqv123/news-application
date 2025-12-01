package com.example.newsapplication.api;

public class ApiConfig {
    public static final String BASE_URL = "http://10.0.2.2:8000";
    public static final int TIMEOUT_MS = 30000;
    public static final String API_VERSION = "/api/v1";

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String BEARER_PREFIX = "Bearer ";

    public static final String PREFS_NAME = "NewsAppPrefs";
    public static final String TOKEN_KEY = "auth_token";

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_LIMIT = 10;

    private ApiConfig() {}
}