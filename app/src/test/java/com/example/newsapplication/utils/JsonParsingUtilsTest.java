package com.example.newsapplication.utils;

import com.example.newsapplication.model.Article;
import com.example.newsapplication.model.Channel;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Unit tests for JsonParsingUtils
 * Note: These are basic unit tests. For full Android context tests, use instrumented tests.
 */
public class JsonParsingUtilsTest {

    @Test
    public void testParseArticles_withResultsArray() throws Exception {
        // Arrange
        JSONObject response = new JSONObject();
        JSONArray articles = new JSONArray();
        
        JSONObject article1 = new JSONObject();
        article1.put("id", "1");
        article1.put("title", "Test Article 1");
        article1.put("summary", "Summary 1");
        article1.put("content", "Content 1");
        article1.put("category", "Tech");
        article1.put("author", "Author 1");
        article1.put("created_at", "2024-12-02");
        articles.put(article1);
        
        response.put("results", articles);
        
        // Act
        List<Article> result = JsonParsingUtils.parseArticles(response);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("Test Article 1", result.get(0).getTitle());
        assertEquals("Tech", result.get(0).getCategory());
    }

    @Test
    public void testParseArticles_withDataArticlesArray() throws Exception {
        // Arrange
        JSONObject response = new JSONObject();
        JSONObject data = new JSONObject();
        JSONArray articles = new JSONArray();
        
        JSONObject article1 = new JSONObject();
        article1.put("id", "2");
        article1.put("title", "Test Article 2");
        article1.put("summary", "Summary 2");
        article1.put("content", "Content 2");
        article1.put("channel_id", 5);
        article1.put("author", "Author 2");
        article1.put("created_at", "2024-12-02");
        articles.put(article1);
        
        data.put("articles", articles);
        response.put("data", data);
        
        // Act
        List<Article> result = JsonParsingUtils.parseArticles(response);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("2", result.get(0).getId());
        assertEquals("Test Article 2", result.get(0).getTitle());
        assertEquals("Channel 5", result.get(0).getCategory());
    }

    @Test
    public void testParseArticles_emptyResponse() throws Exception {
        // Arrange
        JSONObject response = new JSONObject();
        
        // Act
        List<Article> result = JsonParsingUtils.parseArticles(response);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testParseArticles_multipleArticles() throws Exception {
        // Arrange
        JSONObject response = new JSONObject();
        JSONArray articles = new JSONArray();
        
        for (int i = 1; i <= 5; i++) {
            JSONObject article = new JSONObject();
            article.put("id", String.valueOf(i));
            article.put("title", "Article " + i);
            article.put("summary", "Summary " + i);
            article.put("content", "Content " + i);
            article.put("category", "Category " + i);
            article.put("author", "Author " + i);
            article.put("created_at", "2024-12-02");
            articles.put(article);
        }
        
        response.put("results", articles);
        
        // Act
        List<Article> result = JsonParsingUtils.parseArticles(response);
        
        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("1", result.get(0).getId());
        assertEquals("5", result.get(4).getId());
    }

    @Test
    public void testParseArticle_withSourceUrl() throws Exception {
        // Arrange
        JSONObject articleJson = new JSONObject();
        articleJson.put("id", "100");
        articleJson.put("title", "Test");
        articleJson.put("summary", "Summary");
        articleJson.put("content", "Content");
        articleJson.put("source_url", "https://example.com/article");
        articleJson.put("author", "Author");
        articleJson.put("created_at", "2024-12-02");
        
        // Act
        Article result = JsonParsingUtils.parseArticle(articleJson);
        
        // Assert
        assertNotNull(result);
        assertEquals("example.com", result.getSource());
    }

    @Test
    public void testParseChannels_withResultsArray() throws Exception {
        // Arrange
        JSONObject response = new JSONObject();
        JSONArray channels = new JSONArray();
        
        JSONObject channel1 = new JSONObject();
        channel1.put("id", 1);
        channel1.put("name", "Tech News");
        channel1.put("slug", "tech-news");
        channel1.put("description", "Technology news");
        channels.put(channel1);
        
        response.put("results", channels);
        
        // Act
        List<Channel> result = JsonParsingUtils.parseChannels(response);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Tech News", result.get(0).getName());
    }

    @Test
    public void testParseChannels_withDataChannelsArray() throws Exception {
        // Arrange
        JSONObject response = new JSONObject();
        JSONObject data = new JSONObject();
        JSONArray channels = new JSONArray();
        
        JSONObject channel1 = new JSONObject();
        channel1.put("id", 2);
        channel1.put("name", "Sports");
        channel1.put("slug", "sports");
        channels.put(channel1);
        
        data.put("channels", channels);
        response.put("data", data);
        
        // Act
        List<Channel> result = JsonParsingUtils.parseChannels(response);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Sports", result.get(0).getName());
    }

    @Test
    public void testParseBookmarkedIds_withResults() throws Exception {
        // Arrange
        JSONObject response = new JSONObject();
        JSONArray bookmarks = new JSONArray();
        
        JSONObject bookmark1 = new JSONObject();
        bookmark1.put("article_id", "article-1");
        bookmarks.put(bookmark1);
        
        JSONObject bookmark2 = new JSONObject();
        bookmark2.put("article_id", "article-2");
        bookmarks.put(bookmark2);
        
        response.put("results", bookmarks);
        
        // Act
        Set<String> result = JsonParsingUtils.parseBookmarkedIds(response);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("article-1"));
        assertTrue(result.contains("article-2"));
    }

    @Test
    public void testParseBookmarkedIds_emptyResponse() throws Exception {
        // Arrange
        JSONObject response = new JSONObject();
        
        // Act
        Set<String> result = JsonParsingUtils.parseBookmarkedIds(response);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testParseArticle_withMissingFields() throws Exception {
        // Arrange - minimal article with only required fields
        JSONObject articleJson = new JSONObject();
        articleJson.put("id", "minimal");
        
        // Act
        Article result = JsonParsingUtils.parseArticle(articleJson);
        
        // Assert
        assertNotNull(result);
        assertEquals("minimal", result.getId());
        assertEquals("Unknown Title", result.getTitle());
        assertEquals("Unknown Author", result.getAuthor());
        assertEquals("General", result.getCategory());
    }

    @Test
    public void testParseArticle_nullInput() {
        // Act
        Article result = JsonParsingUtils.parseArticle(null);
        
        // Assert
        assertNull(result);
    }

    @Test
    public void testParseArticles_nullInput() {
        // Act
        List<Article> result = JsonParsingUtils.parseArticles(null);
        
        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }
}
