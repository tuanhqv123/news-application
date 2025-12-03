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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
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

    private static final long SEARCH_DEBOUNCE_DELAY = 500;

    private FragmentDashboardBinding binding;
    private NewsAdapter filteredArticlesAdapter;
    private NewsAdapter searchResultsAdapter;
    private CategoryListAdapter categoryListAdapter;
    private ChannelChipAdapter channelChipAdapter;
    
    // Main content views
    private NestedScrollView mainContentScrollView;
    private LinearLayout mainContentContainer;
    private RecyclerView categoriesRecyclerView;
    private RecyclerView channelsRecyclerView;
    
    // Search results views
    private LinearLayout searchResultsContainer;
    private RecyclerView searchResultsRecyclerView;
    private ProgressBar searchProgressBar;
    private TextView noSearchResultsText;
    private TextView searchResultsTitle;
    private ImageView closeSearchButton;
    
    // Filtered articles views
    private LinearLayout filteredArticlesContainer;
    private RecyclerView filteredArticlesRecyclerView;
    private ProgressBar articlesProgressBar;
    private TextView noArticlesText;
    private TextView filteredArticlesTitle;
    private ImageView backFromArticlesButton;
    private Button followChannelButton;
    
    private EditText searchEditText;
    private ImageView clearSearchIcon;
    
    private List<Article> filteredArticles;
    private List<Article> searchResults;
    private List<CategoryListAdapter.Category> categories;
    private List<Channel> channels;
    private Set<Integer> followedChannelIds;
    
    private NewsRepository newsRepository;
    private UserSessionManager sessionManager;
    private Handler searchHandler;
    private Runnable searchRunnable;
    
    private int selectedCategoryId = -1;
    private int selectedChannelId = -1;
    private Channel selectedChannel = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        newsRepository = new NewsRepository(getContext());
        sessionManager = new UserSessionManager(getContext());
        searchHandler = new Handler(Looper.getMainLooper());
        
        filteredArticles = new ArrayList<>();
        searchResults = new ArrayList<>();
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
        
        // Check if navigated from Following tab with channel info
        if (getArguments() != null) {
            int channelId = getArguments().getInt("channelId", -1);
            String channelName = getArguments().getString("channelName", "");
            boolean isFollowing = getArguments().getBoolean("isFollowing", false);
            
            if (channelId > 0 && !channelName.isEmpty()) {
                // Create a channel object and show its articles
                Channel channel = new Channel();
                channel.setId(channelId);
                channel.setName(channelName);
                channel.setFollowing(isFollowing);
                selectedChannel = channel;
                selectedChannelId = channelId;
                
                // Show articles for this channel
                showArticlesByChannel(channel);
                
                // Clear arguments to prevent re-showing on rotation
                getArguments().clear();
            }
        }

        return root;
    }

    private void initViews() {
        // Main content
        mainContentScrollView = binding.mainContentScrollView;
        mainContentContainer = binding.mainContentContainer;
        categoriesRecyclerView = binding.categoriesRecyclerView;
        channelsRecyclerView = binding.channelsRecyclerView;
        
        // Search results
        searchResultsContainer = binding.searchResultsContainer;
        searchResultsRecyclerView = binding.searchResultsRecyclerView;
        searchProgressBar = binding.searchProgressBar;
        noSearchResultsText = binding.noSearchResultsText;
        searchResultsTitle = binding.searchResultsTitle;
        closeSearchButton = binding.closeSearchButton;
        
        // Filtered articles
        filteredArticlesContainer = binding.filteredArticlesContainer;
        filteredArticlesRecyclerView = binding.filteredArticlesRecyclerView;
        articlesProgressBar = binding.articlesProgressBar;
        noArticlesText = binding.noArticlesText;
        filteredArticlesTitle = binding.filteredArticlesTitle;
        backFromArticlesButton = binding.backFromArticlesButton;
        followChannelButton = binding.followChannelButton;
        
        searchEditText = binding.searchEditText;
        clearSearchIcon = binding.clearSearchIcon;
    }

    private void setupAdapters() {
        // Search results adapter
        searchResultsAdapter = new NewsAdapter(searchResults, new NewsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article) {
                openArticleDetail(article);
            }

            @Override
            public void onBookmarkClick(Article article, int position) {
                handleBookmarkClick(article, position, searchResultsAdapter);
            }
        });
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

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

        // Categories adapter (vertical list)
        categoryListAdapter = new CategoryListAdapter(categories, (category, position) -> {
            selectedCategoryId = category.getId();
            selectedChannelId = -1;
            selectedChannel = null;
            channelChipAdapter.clearSelection();
            showArticlesByCategory(category.getId(), category.getName());
        });
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoriesRecyclerView.setAdapter(categoryListAdapter);
        categoriesRecyclerView.setNestedScrollingEnabled(false);

        // Channels adapter (horizontal chips)
        channelChipAdapter = new ChannelChipAdapter(channels, (channel, position) -> {
            channelChipAdapter.setSelectedPosition(position);
            selectedChannelId = channel.getId();
            selectedCategoryId = -1;
            selectedChannel = channel;
            showArticlesByChannel(channel);
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
                    hideSearchResults();
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
            hideSearchResults();
            hideKeyboard();
        });
    }

    private void setupClickListeners() {
        // Close search results
        closeSearchButton.setOnClickListener(v -> {
            searchEditText.setText("");
            hideSearchResults();
            hideKeyboard();
        });

        // Back from articles
        backFromArticlesButton.setOnClickListener(v -> {
            hideFilteredArticles();
            channelChipAdapter.clearSelection();
            selectedCategoryId = -1;
            selectedChannelId = -1;
            selectedChannel = null;
        });

        // Follow channel button
        followChannelButton.setOnClickListener(v -> {
            if (selectedChannel != null) {
                handleFollowChannel();
            }
        });
    }

    private void handleFollowChannel() {
        if (!sessionManager.isLoggedIn()) {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).showLoginDialog();
            }
            return;
        }

        if (selectedChannel == null) return;

        boolean isFollowing = selectedChannel.isFollowing();
        
        if (isFollowing) {
            // Unfollow
            newsRepository.unfollowChannel(selectedChannel.getId(), new NewsRepository.RepositoryCallback<JSONObject>() {
                @Override
                public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                    if (response.isSuccess()) {
                        selectedChannel.setFollowing(false);
                        followedChannelIds.remove(selectedChannel.getId());
                        updateFollowButton(false);
                        Toast.makeText(getContext(), "Unfollowed " + selectedChannel.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            // Follow
            newsRepository.followChannel(selectedChannel.getId(), new NewsRepository.RepositoryCallback<JSONObject>() {
                @Override
                public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                    if (response.isSuccess()) {
                        selectedChannel.setFollowing(true);
                        followedChannelIds.add(selectedChannel.getId());
                        updateFollowButton(true);
                        Toast.makeText(getContext(), "Following " + selectedChannel.getName(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void updateFollowButton(boolean isFollowing) {
        if (isFollowing) {
            followChannelButton.setText("Following");
            followChannelButton.setBackgroundResource(R.drawable.button_following_background);
        } else {
            followChannelButton.setText("Follow");
            followChannelButton.setBackgroundResource(R.drawable.button_follow_background);
        }
    }

    private void loadFollowedChannelsThenLoadAll() {
        newsRepository.getFollowedChannels(new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                if (response.isSuccess() && response.getData() != null) {
                    parseFollowedChannelIds(response.getData());
                }
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
        } catch (Exception e) {
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
                    String description = catJson.optString("description", "");
                    categories.add(new CategoryListAdapter.Category(id, name, slug, description));
                }
            }
        } catch (Exception e) {
        }
        
        if (categories.isEmpty()) {
            loadDefaultCategories();
        } else {
            categoryListAdapter.setCategories(categories);
        }
    }

    private void loadDefaultCategories() {
        categories.clear();
        categories.add(new CategoryListAdapter.Category(1, "Politics", "politics", "Political news and updates"));
        categories.add(new CategoryListAdapter.Category(2, "Technology", "technology", "Tech news and innovations"));
        categories.add(new CategoryListAdapter.Category(3, "Sports", "sports", "Sports news and scores"));
        categories.add(new CategoryListAdapter.Category(4, "Business", "business", "Business and finance news"));
        categories.add(new CategoryListAdapter.Category(5, "Entertainment", "entertainment", "Entertainment and celebrity news"));
        categories.add(new CategoryListAdapter.Category(6, "Health", "health", "Health and wellness news"));
        categoryListAdapter.setCategories(categories);
    }

    private void loadChannels() {
        newsRepository.getPublicChannels(new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                if (response.isSuccess() && response.getData() != null) {
                    parseChannels(response.getData());
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
                    if (followedChannelIds.contains(channel.getId())) {
                        channel.setFollowing(true);
                    }
                    channels.add(channel);
                }
            }
        } catch (Exception e) {
        }
        
        channelChipAdapter.setChannels(channels);
    }

    private void showArticlesByCategory(int categoryId, String categoryName) {
        // Hide header and main content, show filtered articles
        binding.stickyHeader.setVisibility(View.GONE);
        mainContentScrollView.setVisibility(View.GONE);
        searchResultsContainer.setVisibility(View.GONE);
        filteredArticlesContainer.setVisibility(View.VISIBLE);
        
        // Hide follow button for categories
        followChannelButton.setVisibility(View.GONE);
        
        filteredArticlesTitle.setText(categoryName);
        showArticlesLoading();
        
        newsRepository.getCategoryArticles(categoryId, 1, 20, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                hideArticlesLoading();
                if (response.isSuccess() && response.getData() != null) {
                    parseArticles(response.getData(), filteredArticles, filteredArticlesAdapter);
                    if (filteredArticles.isEmpty()) {
                        showNoArticles();
                    } else {
                        showArticlesList();
                    }
                } else {
                    showNoArticles();
                }
            }
        });
    }

    private void showArticlesByChannel(Channel channel) {
        // Hide header and main content, show filtered articles
        binding.stickyHeader.setVisibility(View.GONE);
        mainContentScrollView.setVisibility(View.GONE);
        searchResultsContainer.setVisibility(View.GONE);
        filteredArticlesContainer.setVisibility(View.VISIBLE);
        
        // Show follow button for channels
        followChannelButton.setVisibility(View.VISIBLE);
        updateFollowButton(channel.isFollowing());
        
        filteredArticlesTitle.setText(channel.getName());
        showArticlesLoading();
        
        newsRepository.getChannelArticles(channel.getId(), 1, 20, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                hideArticlesLoading();
                if (response.isSuccess() && response.getData() != null) {
                    parseArticles(response.getData(), filteredArticles, filteredArticlesAdapter);
                    if (filteredArticles.isEmpty()) {
                        showNoArticles();
                    } else {
                        showArticlesList();
                    }
                } else {
                    showNoArticles();
                }
            }
        });
    }

    private void performSearch(String query) {
        // Show search results container, hide others
        mainContentScrollView.setVisibility(View.GONE);
        filteredArticlesContainer.setVisibility(View.GONE);
        searchResultsContainer.setVisibility(View.VISIBLE);
        
        searchResultsTitle.setText("Results for \"" + query + "\"");
        showSearchLoading();
        
        newsRepository.searchArticles(query, 1, 20, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                hideSearchLoading();
                if (response.isSuccess() && response.getData() != null) {
                    parseArticles(response.getData(), searchResults, searchResultsAdapter);
                    if (searchResults.isEmpty()) {
                        showNoSearchResults();
                    } else {
                        showSearchResultsList();
                    }
                } else {
                    showNoSearchResults();
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
            String author = articleJson.optString("author", "");
            String imageUrl = articleJson.optString("hero_image_url", "");
            // Use published_at first, fallback to created_at
            String dateStr = articleJson.optString("published_at", "");
            if (dateStr.isEmpty()) {
                dateStr = articleJson.optString("created_at", "");
            }
            
            int imageResId = imageUrl.isEmpty() ? R.drawable.placeholder_image : R.drawable.ic_launcher_foreground;
            
            return new Article(id, title, summary, content, source, category, author, imageUrl, imageResId, dateStr, false);
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
                        Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    // Search UI helpers
    private void showSearchLoading() {
        searchProgressBar.setVisibility(View.VISIBLE);
        noSearchResultsText.setVisibility(View.GONE);
        searchResultsRecyclerView.setVisibility(View.GONE);
    }

    private void hideSearchLoading() {
        searchProgressBar.setVisibility(View.GONE);
    }

    private void showSearchResultsList() {
        searchResultsRecyclerView.setVisibility(View.VISIBLE);
        noSearchResultsText.setVisibility(View.GONE);
    }

    private void showNoSearchResults() {
        noSearchResultsText.setVisibility(View.VISIBLE);
        searchResultsRecyclerView.setVisibility(View.GONE);
    }

    private void hideSearchResults() {
        searchResultsContainer.setVisibility(View.GONE);
        mainContentScrollView.setVisibility(View.VISIBLE);
        searchResults.clear();
        searchResultsAdapter.notifyDataSetChanged();
    }

    // Articles UI helpers
    private void showArticlesLoading() {
        articlesProgressBar.setVisibility(View.VISIBLE);
        noArticlesText.setVisibility(View.GONE);
        filteredArticlesRecyclerView.setVisibility(View.GONE);
    }

    private void hideArticlesLoading() {
        articlesProgressBar.setVisibility(View.GONE);
    }

    private void showArticlesList() {
        filteredArticlesRecyclerView.setVisibility(View.VISIBLE);
        noArticlesText.setVisibility(View.GONE);
    }

    private void showNoArticles() {
        noArticlesText.setVisibility(View.VISIBLE);
        filteredArticlesRecyclerView.setVisibility(View.GONE);
    }

    private void hideFilteredArticles() {
        // Show header and main content again
        binding.stickyHeader.setVisibility(View.VISIBLE);
        filteredArticlesContainer.setVisibility(View.GONE);
        mainContentScrollView.setVisibility(View.VISIBLE);
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
