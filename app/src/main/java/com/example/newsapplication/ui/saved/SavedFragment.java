package com.example.newsapplication.ui.saved;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.MainActivity;
import com.example.newsapplication.R;
import com.example.newsapplication.adapter.NewsAdapter;
import com.example.newsapplication.auth.UserSessionManager;
import com.example.newsapplication.databinding.FragmentSavedBinding;
import com.example.newsapplication.model.Article;
import com.example.newsapplication.repository.NewsRepository;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {

    private FragmentSavedBinding binding;
    private NewsAdapter savedNewsAdapter;
    private RecyclerView savedNewsRecyclerView;
    private List<Article> savedArticlesList;

    private LinearLayout emptyStateText;
    private LinearLayout loginButtonContainer;
    private UserSessionManager sessionManager;
    private NewsRepository newsRepository;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSavedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize session manager and repository
        sessionManager = new UserSessionManager(getContext());
        newsRepository = new NewsRepository(getContext());

        initViews();
        setupClickListeners();

        // Check login status 
        if (sessionManager.isLoggedIn()) {
            // User is logged in, show saved content directly
            showLoggedInState();
        } else {
            showLoggedOutState();
        }

        return root;
    }

    private void initViews() {
        savedNewsRecyclerView = binding.savedNewsRecyclerView;
        emptyStateText = binding.emptyStateText;
        loginButtonContainer = binding.loginButtonContainer;
    }

    private void setupClickListeners() {
        // Login button click
        loginButtonContainer.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.showLoginDialog();
            }
        });
    }

    private void showLoggedOutState() {
        // Show empty state instead of login UI
        loginButtonContainer.setVisibility(View.GONE);
        savedNewsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);
    }

    private void showLoggedInState() {
        // Show saved articles UI
        loginButtonContainer.setVisibility(View.GONE);
        setupRecyclerView();
        loadSavedArticles();
    }

    private void setupRecyclerView() {
        savedArticlesList = new ArrayList<>();

        savedNewsAdapter = new NewsAdapter(savedArticlesList, new NewsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article) {
                // Handle article click - navigate to article detail
                try {
                    android.content.Intent intent = new android.content.Intent(getContext(), com.example.newsapplication.ArticleDetailActivity.class);
                    intent.putExtra("article", article);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onBookmarkClick(Article article, int position) {
                // Handle bookmark click (remove bookmark)
                newsRepository.removeBookmark(article.getId(), new NewsRepository.RepositoryCallback<JSONObject>() {
                    @Override
                    public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                        if (response.isSuccess()) {
                            savedArticlesList.remove(article);
                            savedNewsAdapter.notifyDataSetChanged();
                            
                            if (savedArticlesList.isEmpty()) {
                                savedNewsRecyclerView.setVisibility(View.GONE);
                                emptyStateText.setVisibility(View.VISIBLE);
                            }
                            
                            Toast.makeText(getContext(), "Article removed from saved", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Failed to remove bookmark", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        savedNewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        savedNewsRecyclerView.setAdapter(savedNewsAdapter);
    }

    private void loadSavedArticles() {
        if (newsRepository == null) return;
        
        newsRepository.getBookmarks(new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                if (response.isSuccess() && response.getData() != null) {
                    loadRealBookmarks(response.getData());
                } else {
                    android.util.Log.e("SavedFragment", "Failed to load bookmarks: " + response.getErrorMessage());
                    loadMockBookmarks();
                }
            }
        });
    }

    private void loadRealBookmarks(JSONObject data) {
        android.util.Log.d("SavedFragment", "Bookmarks response: " + data.toString());
        savedArticlesList.clear();
        
        try {
            JSONArray results = null;
            
            // API response format: {"success":true,"data":{"bookmarks":[{"user_id":"...","article_id":"...","articles":{...}}]}}
            if (data.has("data")) {
                Object dataObj = data.get("data");
                if (dataObj instanceof JSONObject) {
                    JSONObject dataJson = (JSONObject) dataObj;
                    if (dataJson.has("bookmarks")) {
                        results = dataJson.getJSONArray("bookmarks");
                        android.util.Log.d("SavedFragment", "Found bookmarks in data.bookmarks");
                    } else if (dataJson.has("results")) {
                        results = dataJson.getJSONArray("results");
                    }
                } else if (dataObj instanceof JSONArray) {
                    results = (JSONArray) dataObj;
                }
            } else if (data.has("bookmarks")) {
                results = data.getJSONArray("bookmarks");
            } else if (data.has("results")) {
                results = data.getJSONArray("results");
            }
            
            android.util.Log.d("SavedFragment", "Found " + (results != null ? results.length() : 0) + " bookmarks");
            
            if (results != null && results.length() > 0) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject bookmark = results.getJSONObject(i);
                    android.util.Log.d("SavedFragment", "Bookmark item " + i + ": " + bookmark.toString());
                    
                    Article article = null;
                    
                    // Check for "articles" key (API response uses "articles" not "article")
                    if (bookmark.has("articles") && !bookmark.isNull("articles")) {
                        JSONObject articleJson = bookmark.getJSONObject("articles");
                        article = parseArticleFromJson(articleJson);
                        android.util.Log.d("SavedFragment", "Parsed article from 'articles': " + (article != null ? article.getTitle() : "null"));
                    }
                    // Check if article data is embedded in the bookmark as "article"
                    else if (bookmark.has("article") && !bookmark.isNull("article")) {
                        JSONObject articleJson = bookmark.getJSONObject("article");
                        article = parseArticleFromJson(articleJson);
                    } 
                    // Check if this is the article directly (not wrapped)
                    else if (bookmark.has("title") && bookmark.has("id")) {
                        article = parseArticleFromJson(bookmark);
                    }
                    // Check for article_id and try to use other available fields
                    else if (bookmark.has("article_id")) {
                        String articleId = bookmark.optString("article_id");
                        // Create a minimal article with available data
                        article = new Article(
                            articleId,
                            bookmark.optString("title", "Saved Article"),
                            bookmark.optString("summary", ""),
                            bookmark.optString("content", ""),
                            bookmark.optString("source", "Unknown"),
                            bookmark.optString("category", "General"),
                            bookmark.optString("author", ""),
                            bookmark.optString("hero_image_url", bookmark.optString("image_url", "")),
                            R.drawable.placeholder_image,
                            bookmark.optString("created_at", ""),
                            true
                        );
                    }
                    
                    if (article != null) {
                        article.setBookmarked(true);
                        savedArticlesList.add(article);
                        android.util.Log.d("SavedFragment", "Added article: " + article.getTitle());
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e("SavedFragment", "Error parsing bookmarks", e);
            e.printStackTrace();
        }
        
        android.util.Log.d("SavedFragment", "Total articles loaded: " + savedArticlesList.size());
        
        if (savedArticlesList.isEmpty()) {
            savedNewsRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            savedNewsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }

        savedNewsAdapter.notifyDataSetChanged();
    }
    
    private Article parseArticleFromJson(JSONObject articleJson) {
        try {
            String id = articleJson.optString("id", "");
            String title = articleJson.optString("title", "Unknown Title");
            String summary = articleJson.optString("summary", "");
            String content = articleJson.optString("content", "");
            
            String source = articleJson.optString("source", "");
            if (source.isEmpty()) {
                source = articleJson.optString("source_url", "Unknown Source");
            }
            
            String category = articleJson.optString("category", "General");
            String author = articleJson.optString("author", "Unknown Author");
            String imageUrl = articleJson.optString("hero_image_url", "");
            String createdAt = articleJson.optString("created_at", "");
            
            int imageResId = imageUrl.isEmpty() ? R.drawable.placeholder_image : R.drawable.ic_launcher_foreground;
            
            return new Article(id, title, summary, content, source, category, author, imageUrl, imageResId, createdAt, true);
        } catch (Exception e) {
            return null;
        }
    }

    private void loadMockBookmarks() {
        // Clear existing bookmarks
        savedArticlesList.clear();

        // Show empty state since we don't have mock data anymore
        savedNewsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.VISIBLE);

        savedNewsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}