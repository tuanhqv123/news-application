package com.example.newsapplication.ui.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.ArticleDetailActivity;
import com.example.newsapplication.MainActivity;
import com.example.newsapplication.R;
import com.example.newsapplication.adapter.CategoryListAdapter;
import com.example.newsapplication.adapter.ChannelChipAdapter;
import com.example.newsapplication.adapter.NewsAdapter;
import com.example.newsapplication.auth.UserSessionManager;
import com.example.newsapplication.databinding.FragmentDashboardBinding;
import com.example.newsapplication.model.Article;
import com.example.newsapplication.model.Channel;
import com.example.newsapplication.repository.NewsRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardFragment extends Fragment {

    private static final String TAG = "DashboardFragment";
    private static final long SEARCH_DEBOUNCE_DELAY = 500;

    private FragmentDashboardBinding binding;
    private NewsAdapter contentAdapter;
    private NewsAdapter filteredArticlesAdapter;
    private CategoryListAdapter categoryListAdapter;
    private ChannelChipAdapter channelChipAdapter;
    
    private RecyclerView contentRecyclerView;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView channelsRecyclerView;
    private RecyclerView filteredArticlesRecyclerView;
    
    private LinearLayout filteredArticlesContainer;
    private ProgressBar articlesProgressBar;
    private TextView noArticlesText;
    private TextView filteredArticlesTitle;
    
    private EditText searchEditText;
    private ImageView clearSearchIcon;
    
    private List<Article> contentArticles;
    private List<Article> filteredArticles;
    private List<CategoryListAdapter.Category> categories;
    private List<Channel> channels;
    private Set<Integer> followedChannelIds;
    
    private NewsRepository newsRepository;
    private UserSessionManager sessionManager;
    private Handler searchHandler;
    private Runnable searchRunnable;
    
    private int selectedCategoryId = -1;
    private int selectedChannelId = -1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        newsRepository = new NewsRepository(getContext());
        sessionManager = new UserSessionManager(getContext());
        searchHandler = new Handler(Looper.getMainLooper());
        
        contentArticles = new ArrayList<>();
        filteredArticles = new ArrayList<>();
        categories = new ArrayList<>();
        channels = new ArrayList<>();
        followedChannelIds = new HashSet<>();

        initViews();
        setupAdapters();
        setupSearch();
        setupClickListeners();
        
        // Load followed channels first, then load all channels
        if (sessionManager.isLoggedIn()) {
            loadFollowedChannelsThenLoadAll();
        } else {
            loadChannels();
        }
        loadCategories();
        loadTrendingArticles();

        return root;
    }

    private void initViews() {
        contentRecyclerView = binding.contentRecyclerView;
        categoriesRecyclerView = binding.categoriesRecyclerView;
        channelsRecyclerView = binding.channelsRecyclerView;
        filteredArticlesRecyclerView = binding.filteredArticlesRecyclerView;
        
        filteredArticlesContainer = binding.filteredArticlesContainer;
        articlesProgressBar = binding.articlesProgressBar;
        noArticlesText = binding.noArticlesText;
        filteredArticlesTitle = binding.filteredArticlesTitle;
        
        searchEditText = binding.searchEditText;
        clearSearchIcon = binding.clearSearchIcon;
    }

    private void setupAdapters() {
        // Content/Trending articles adapter
        contentAdapter = new NewsAdapter(contentArticles, new NewsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article) {
                openArticleDetail(article);
            }

            @Override
            public void onBookmarkClick(Article article, int position) {
                handleBookmarkClick(article, position, contentAdapter);
            }
        });
        contentRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contentRecyclerView.setAdapter(contentAdapter);
        contentRecyclerView.setNestedScrollingEnabled(false);

        // Filtered articles adapter
        filteredArticlesAdapter = new NewsAdapter(filteredArticles, new NewsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article) {
                openArticleDetail(article);
            }

            @Override
            public void onBookmarkClick(Article article, int position) {
                handleBookmarkClick(article, position, filteredArticlesAdapter);
            }
        });
        filteredArticlesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        filteredArticlesRecyclerView.setAdapter(filteredArticlesAdapter);
        filteredArticlesRecyclerView.setNestedScrollingEnabled(false);

        // Categories adapter (vertical list)
        categoryListAdapter = new CategoryListAdapter(categories, (category, position) -> {
            selectedCategoryId = category.getId();
            selectedChannelId = -1;
            channelChipAdapter.clearSelection();
            loadArticlesByCategory(category.getId(), category.getName());
        });
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoriesRecyclerView.setAdapter(categoryListAdapter);
        categoriesRecyclerView.setNestedScrollingEnabled(false);

        // Channels adapter (horizontal chips)
        channelChipAdapter = new ChannelChipAdapter(channels, (channel, position) -> {
            channelChipAdapter.setSelectedPosition(position);
            selectedChannelId = channel.getId();
            selectedCategoryId = -1;
            loadArticlesByChannel(channel.getId(), channel.getName());
        });
        channelsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        channelsRecyclerView.setAdapter(channelChipAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearSearchIcon.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                
                if (s.length() > 0) {
                    searchRunnable = () -> performSearch(s.toString().trim());
                    searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_DELAY);
                } else {
                    hideFilteredArticles();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
        
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    performSearch(query);
                    hideKeyboard();
                }
                return true;
            }
            return false;
        });
        
        clearSearchIcon.setOnClickListener(v -> {
            searchEditText.setText("");
            hideFilteredArticles();
            hideKeyboard();
        });
    }

    private void setupClickListeners() {
        binding.seeMoreTrending.setOnClickListener(v -> {
            Toast.makeText(getContext(), "See more trending articles", Toast.LENGTH_SHORT).show();
        });

        binding.seeAllChannels.setOnClickListener(v -> {
            Toast.makeText(getContext(), "See all channels", Toast.LENGTH_SHORT).show();
        });

        binding.closeFilterButton.setOnClickListener(v -> {
            hideFilteredArticles();
            channelChipAdapter.clearSelection();
            selectedCategoryId = -1;
            selectedChannelId = -1;
        });
    }

    private void loadFollowedChannelsThenLoadAll() {
        newsRepository.getFollowedChannels(new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                if (response.isSuccess() && response.getData() != null) {
                    parseFollowedChannelIds(response.getData());
                }
                // Now load all channels
                loadChannels();
            }
        });
    }

    private void parseFollowedChannelIds(JSONObject data) {
        followedChannelIds.clear();
        try {
            JSONArray channelsArray = null;
            if (data.has("data")) {
                Object dataObj = data.get("data");
                if (dataObj instanceof JSONObject) {
                    JSONObject dataJson = (JSONObject) dataObj;
                    if (dataJson.has("channels")) {
                        channelsArray = dataJson.getJSONArray("channels");
                    }
                } else if (dataObj instanceof JSONArray) {
                    channelsArray = (JSONArray) dataObj;
                }
            } else if (data.has("channels")) {
                channelsArray = data.getJSONArray("channels");
            }
            
            if (channelsArray != null) {
                for (int i = 0; i < channelsArray.length(); i++) {
                    JSONObject channelJson = channelsArray.getJSONObject(i);
                    int id = channelJson.optInt("id", -1);
                    if (id != -1) {
                        followedChannelIds.add(id);
                    }
                }
            }
            android.util.Log.d(TAG, "Loaded " + followedChannelIds.size() + " followed channel IDs");
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error parsing followed channels", e);
        }
    }

    private void loadCategories() {
        newsRepository.getCategories(new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                if (response.isSuccess() && response.getData() != null) {
                    parseCategories(response.getData());
                } else {
                    loadDefaultCategories();
                }
            }
        });
    }

    private void parseCategories(JSONObject data) {
        categories.clear();
        try {
            JSONArray categoriesArray = null;
            if (data.has("categories")) {
                categoriesArray = data.getJSONArray("categories");
            } else if (data.has("data")) {
                Object dataObj = data.get("data");
                if (dataObj instanceof JSONArray) {
                    categoriesArray = (JSONArray) dataObj;
                } else if (dataObj instanceof JSONObject) {
                    JSONObject dataJson = (JSONObject) dataObj;
                    if (dataJson.has("categories")) {
                        categoriesArray = dataJson.getJSONArray("categories");
                    }
                }
            } else if (data.has("results")) {
                categoriesArray = data.getJSONArray("results");
            }
            
            if (categoriesArray != null) {
                for (int i = 0; i < categoriesArray.length(); i++) {
                    JSONObject catJson = categoriesArray.getJSONObject(i);
                    int id = catJson.optInt("id", i);
                    String name = catJson.optString("name", "Category " + i);
                    String slug = catJson.optString("slug", name.toLowerCase());
                    categories.add(new CategoryListAdapter.Category(id, name, slug));
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error parsing categories", e);
        }
        
        if (categories.isEmpty()) {
            loadDefaultCategories();
        } else {
            categoryListAdapter.setCategories(categories);
        }
    }

    private void loadDefaultCategories() {
        categories.clear();
        categories.add(new CategoryListAdapter.Category(1, "Politics"));
        categories.add(new CategoryListAdapter.Category(2, "Technology"));
        categories.add(new CategoryListAdapter.Category(3, "Sports"));
        categories.add(new CategoryListAdapter.Category(4, "Business"));
        categories.add(new CategoryListAdapter.Category(5, "Entertainment"));
        categories.add(new CategoryListAdapter.Category(6, "Health"));
        categoryListAdapter.setCategories(categories);
    }

    private void loadChannels() {
        newsRepository.getPublicChannels(new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                if (response.isSuccess() && response.getData() != null) {
                    parseChannels(response.getData());
                } else {
                    android.util.Log.e(TAG, "Failed to load channels: " + response.getErrorMessage());
                }
            }
        });
    }

    private void parseChannels(JSONObject data) {
        channels.clear();
        try {
            JSONArray channelsArray = null;
            if (data.has("channels")) {
                channelsArray = data.getJSONArray("channels");
            } else if (data.has("data")) {
                Object dataObj = data.get("data");
                if (dataObj instanceof JSONArray) {
                    channelsArray = (JSONArray) dataObj;
                } else if (dataObj instanceof JSONObject) {
                    JSONObject dataJson = (JSONObject) dataObj;
                    if (dataJson.has("channels")) {
                        channelsArray = dataJson.getJSONArray("channels");
                    }
                }
            } else if (data.has("results")) {
                channelsArray = data.getJSONArray("results");
            }
            
            if (channelsArray != null) {
                for (int i = 0; i < channelsArray.length(); i++) {
                    JSONObject channelJson = channelsArray.getJSONObject(i);
                    Channel channel = Channel.fromJson(channelJson);
                    // Mark as following if in followed set
                    if (followedChannelIds.contains(channel.getId())) {
                        channel.setFollowing(true);
                    }
                    channels.add(channel);
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error parsing channels", e);
        }
        
        channelChipAdapter.setChannels(channels);
    }

    private void loadTrendingArticles() {
        newsRepository.getArticles(new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                if (response.isSuccess() && response.getData() != null) {
                    parseArticles(response.getData(), contentArticles, contentAdapter);
                } else {
                    android.util.Log.e(TAG, "Failed to load trending articles: " + response.getErrorMessage());
                }
            }
        });
    }

    private void loadArticlesByCategory(int categoryId, String categoryName) {
        showFilteredArticlesLoading();
        filteredArticlesTitle.setText(categoryName + " Articles");
        
        newsRepository.getCategoryArticles(categoryId, 1, 20, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                hideFilteredArticlesLoading();
                if (response.isSuccess() && response.getData() != null) {
                    parseArticles(response.getData(), filteredArticles, filteredArticlesAdapter);
                    if (filteredArticles.isEmpty()) {
                        showNoArticles();
                    } else {
                        showFilteredArticles();
                    }
                } else {
                    showNoArticles();
                }
            }
        });
    }

    private void loadArticlesByChannel(int channelId, String channelName) {
        showFilteredArticlesLoading();
        filteredArticlesTitle.setText(channelName + " Articles");
        
        newsRepository.getChannelArticles(channelId, 1, 20, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                hideFilteredArticlesLoading();
                if (response.isSuccess() && response.getData() != null) {
                    parseArticles(response.getData(), filteredArticles, filteredArticlesAdapter);
                    if (filteredArticles.isEmpty()) {
                        showNoArticles();
                    } else {
                        showFilteredArticles();
                    }
                } else {
                    showNoArticles();
                }
            }
        });
    }

    private void performSearch(String query) {
        showFilteredArticlesLoading();
        filteredArticlesTitle.setText("Search: " + query);
        
        newsRepository.searchArticles(query, 1, 20, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                hideFilteredArticlesLoading();
                if (response.isSuccess() && response.getData() != null) {
                    parseArticles(response.getData(), filteredArticles, filteredArticlesAdapter);
                    if (filteredArticles.isEmpty()) {
                        showNoArticles();
                    } else {
                        showFilteredArticles();
                    }
                } else {
                    showNoArticles();
                }
            }
        });
    }

    private void parseArticles(JSONObject data, List<Article> targetList, NewsAdapter adapter) {
        targetList.clear();
        try {
            JSONArray articlesArray = null;
            if (data.has("articles")) {
                articlesArray = data.getJSONArray("articles");
            } else if (data.has("data")) {
                Object dataObj = data.get("data");
                if (dataObj instanceof JSONArray) {
                    articlesArray = (JSONArray) dataObj;
                } else if (dataObj instanceof JSONObject) {
                    JSONObject dataJson = (JSONObject) dataObj;
                    if (dataJson.has("articles")) {
                        articlesArray = dataJson.getJSONArray("articles");
                    } else if (dataJson.has("results")) {
                        articlesArray = dataJson.getJSONArray("results");
                    }
                }
            } else if (data.has("results")) {
                articlesArray = data.getJSONArray("results");
            }
            
            if (articlesArray != null) {
                for (int i = 0; i < articlesArray.length(); i++) {
                    JSONObject articleJson = articlesArray.getJSONObject(i);
                    Article article = parseArticleFromJson(articleJson);
                    if (article != null) {
                        targetList.add(article);
                    }
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error parsing articles", e);
        }
        
        adapter.notifyDataSetChanged();
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
                if (source.startsWith("http")) {
                    try {
                        java.net.URL url = new java.net.URL(source);
                        source = url.getHost();
                    } catch (Exception e) {
                        // Keep original
                    }
                }
            }
            
            String category = articleJson.optString("category", "General");
            String author = articleJson.optString("author", "Unknown");
            String imageUrl = articleJson.optString("hero_image_url", "");
            String createdAt = articleJson.optString("created_at", "");
            
            int imageResId = imageUrl.isEmpty() ? R.drawable.placeholder_image : R.drawable.ic_launcher_foreground;
            
            return new Article(id, title, summary, content, source, category, author, imageUrl, imageResId, createdAt, false);
        } catch (Exception e) {
            return null;
        }
    }

    private void handleBookmarkClick(Article article, int position, NewsAdapter adapter) {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showLoginDialog();
            }
            return;
        }
        
        boolean isBookmarked = article.isBookmarked();
        article.setBookmarked(!isBookmarked);
        adapter.notifyItemChanged(position);
        
        if (!isBookmarked) {
            newsRepository.bookmarkArticle(article.getId(), new NewsRepository.RepositoryCallback<JSONObject>() {
                @Override
                public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                    if (!response.isSuccess() && response.getStatusCode() != 400) {
                        article.setBookmarked(false);
                        adapter.notifyItemChanged(position);
                        Toast.makeText(getContext(), "Failed to bookmark", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            newsRepository.removeBookmark(article.getId(), new NewsRepository.RepositoryCallback<JSONObject>() {
                @Override
                public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                    if (!response.isSuccess()) {
                        article.setBookmarked(true);
                        adapter.notifyItemChanged(position);
                        Toast.makeText(getContext(), "Failed to remove bookmark", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void showFilteredArticlesLoading() {
        filteredArticlesContainer.setVisibility(View.VISIBLE);
        articlesProgressBar.setVisibility(View.VISIBLE);
        noArticlesText.setVisibility(View.GONE);
        filteredArticlesRecyclerView.setVisibility(View.GONE);
    }

    private void hideFilteredArticlesLoading() {
        articlesProgressBar.setVisibility(View.GONE);
    }

    private void showFilteredArticles() {
        filteredArticlesContainer.setVisibility(View.VISIBLE);
        filteredArticlesRecyclerView.setVisibility(View.VISIBLE);
        noArticlesText.setVisibility(View.GONE);
    }

    private void showNoArticles() {
        filteredArticlesContainer.setVisibility(View.VISIBLE);
        noArticlesText.setVisibility(View.VISIBLE);
        filteredArticlesRecyclerView.setVisibility(View.GONE);
    }

    private void hideFilteredArticles() {
        filteredArticlesContainer.setVisibility(View.GONE);
        filteredArticles.clear();
        filteredArticlesAdapter.notifyDataSetChanged();
    }

    private void openArticleDetail(Article article) {
        try {
            Intent intent = new Intent(getContext(), ArticleDetailActivity.class);
            intent.putExtra("article", article);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideKeyboard() {
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchHandler != null && searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        binding = null;
    }
}
