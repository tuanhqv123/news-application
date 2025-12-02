package com.example.newsapplication.utils;

import org.junit.Test;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit tests for UrlBuilder
 */
public class UrlBuilderTest {

    @Test
    public void testBuildUrl_withNoParams() {
        // Arrange
        String baseUrl = "https://api.example.com/articles";
        Map<String, String> params = new HashMap<>();
        
        // Act
        String result = UrlBuilder.buildUrl(baseUrl, params);
        
        // Assert
        assertEquals("https://api.example.com/articles", result);
    }

    @Test
    public void testBuildUrl_withNullParams() {
        // Arrange
        String baseUrl = "https://api.example.com/articles";
        
        // Act
        String result = UrlBuilder.buildUrl(baseUrl, null);
        
        // Assert
        assertEquals("https://api.example.com/articles", result);
    }

    @Test
    public void testBuildUrl_withSingleParam() {
        // Arrange
        String baseUrl = "https://api.example.com/articles";
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        
        // Act
        String result = UrlBuilder.buildUrl(baseUrl, params);
        
        // Assert
        assertTrue(result.contains("page=1"));
        assertTrue(result.startsWith("https://api.example.com/articles"));
    }

    @Test
    public void testBuildUrl_withMultipleParams() {
        // Arrange
        String baseUrl = "https://api.example.com/articles";
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("limit", "10");
        params.put("category", "tech");
        
        // Act
        String result = UrlBuilder.buildUrl(baseUrl, params);
        
        // Assert
        assertTrue(result.contains("page=1"));
        assertTrue(result.contains("limit=10"));
        assertTrue(result.contains("category=tech"));
    }

    @Test
    public void testBuildUrl_withSpecialCharacters() {
        // Arrange
        String baseUrl = "https://api.example.com/search";
        Map<String, String> params = new HashMap<>();
        params.put("q", "hello world");
        
        // Act
        String result = UrlBuilder.buildUrl(baseUrl, params);
        
        // Assert
        assertTrue(result.contains("q=hello"));
        assertTrue(result.contains("world"));
        // URL encoding should handle the space
    }

    @Test
    public void testBuildUrl_withNullValue() {
        // Arrange
        String baseUrl = "https://api.example.com/articles";
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        params.put("category", null);
        
        // Act
        String result = UrlBuilder.buildUrl(baseUrl, params);
        
        // Assert
        assertTrue(result.contains("page=1"));
        assertFalse(result.contains("category"));
    }

    @Test
    public void testEncode_normalString() {
        // Arrange
        String input = "hello";
        
        // Act
        String result = UrlBuilder.encode(input);
        
        // Assert
        assertEquals("hello", result);
    }

    @Test
    public void testEncode_withSpaces() {
        // Arrange
        String input = "hello world";
        
        // Act
        String result = UrlBuilder.encode(input);
        
        // Assert
        assertEquals("hello+world", result);
    }

    @Test
    public void testEncode_withSpecialCharacters() {
        // Arrange
        String input = "hello&world=test";
        
        // Act
        String result = UrlBuilder.encode(input);
        
        // Assert
        assertTrue(result.contains("%"));
        assertFalse(result.contains("&"));
    }

    @Test
    public void testEncode_nullInput() {
        // Act
        String result = UrlBuilder.encode(null);
        
        // Assert
        assertEquals("", result);
    }

    @Test
    public void testBuildUrlWithOptionalParams_noParams() {
        // Arrange
        String baseUrl = "https://api.example.com/articles";
        
        // Act
        String result = UrlBuilder.buildUrlWithOptionalParams(baseUrl);
        
        // Assert
        assertEquals("https://api.example.com/articles", result);
    }

    @Test
    public void testBuildUrlWithOptionalParams_withParams() {
        // Arrange
        String baseUrl = "https://api.example.com/articles";
        
        // Act
        String result = UrlBuilder.buildUrlWithOptionalParams(baseUrl,
            "page", 1,
            "limit", 10
        );
        
        // Assert
        assertTrue(result.contains("page=1"));
        assertTrue(result.contains("limit=10"));
    }

    @Test
    public void testBuildUrlWithOptionalParams_withNullValues() {
        // Arrange
        String baseUrl = "https://api.example.com/articles";
        
        // Act
        String result = UrlBuilder.buildUrlWithOptionalParams(baseUrl,
            "page", 1,
            "category", null,
            "limit", 10
        );
        
        // Assert
        assertTrue(result.contains("page=1"));
        assertTrue(result.contains("limit=10"));
        assertFalse(result.contains("category"));
    }

    @Test
    public void testBuildUrlWithOptionalParams_mixedTypes() {
        // Arrange
        String baseUrl = "https://api.example.com/channels";
        
        // Act
        String result = UrlBuilder.buildUrlWithOptionalParams(baseUrl,
            "name", "Tech News",
            "is_active", true,
            "channel_id", 5
        );
        
        // Assert
        assertTrue(result.contains("name=Tech"));
        assertTrue(result.contains("is_active=true"));
        assertTrue(result.contains("channel_id=5"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildUrlWithOptionalParams_oddNumberOfParams() {
        // Arrange
        String baseUrl = "https://api.example.com/articles";
        
        // Act - should throw exception
        UrlBuilder.buildUrlWithOptionalParams(baseUrl,
            "page", 1,
            "limit" // Missing value
        );
    }

    @Test
    public void testBuildUrl_preservesBaseUrlStructure() {
        // Arrange
        String baseUrl = "https://api.example.com/v1/articles";
        Map<String, String> params = new HashMap<>();
        params.put("page", "1");
        
        // Act
        String result = UrlBuilder.buildUrl(baseUrl, params);
        
        // Assert
        assertTrue(result.startsWith("https://api.example.com/v1/articles"));
        assertTrue(result.contains("?"));
        assertTrue(result.contains("page=1"));
    }
}
