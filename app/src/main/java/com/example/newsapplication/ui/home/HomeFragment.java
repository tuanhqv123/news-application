package com.example.newsapplication.ui.home;

import android.content.Intent;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.ArticleDetailActivity;
import com.example.newsapplication.MainActivity;
import com.example.newsapplication.R;
import com.example.newsapplication.adapter.breaking.BreakingNewsAdapter;
import com.example.newsapplication.adapter.ChannelsAdapter;
import com.example.newsapplication.adapter.NewsAdapter;
import com.example.newsapplication.databinding.FragmentHomeBinding;
import com.example.newsapplication.model.Article;
import com.example.newsapplication.model.Channel;
import com.example.newsapplication.utils.JsonParsingUtils;

import com.example.newsapplication.auth.UserSessionManager;
import com.example.newsapplication.auth.AuthService;
import com.example.newsapplication.repository.NewsRepository;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;
    private NewsAdapter newsAdapter;
    private BreakingNewsAdapter breakingNewsAdapter;
    private ChannelsAdapter followingChannelsAdapter;
    private RecyclerView breakingNewsRecyclerView;
    private RecyclerView popularNewsRecyclerView;
    private RecyclerView followingChannelsRecyclerView;
    private List<Article> breakingNewsList;
    private List<Article> popularNewsList;
    private List<Channel> followingChannelsList;

    // Following section views
    private LinearLayout followingContainer;
    private TextView noFollowingText;

    private String currentSource = "VnExpress";
    private UserSessionManager sessionManager;
    private AuthService authService;
    private NewsRepository newsRepository;
    
    private String currentTab = "feeds";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        sessionManager = new UserSessionManager(getContext());
        authService = new AuthService(getContext());
        newsRepository = new NewsRepository(getContext());
        followingChannelsList = new ArrayList<>();
        
        setupRecyclerViews();
        setupFollowingSection();
        loadMockNews();
        
        // Update user avatar if logged in
        updateUserInfo();
        setupTabNavigation();
        setupHeaderClicks();

        // Handle see more click
        try {
            binding.seeMoreTextView.setOnClickListener(v -> {
                Toast.makeText(getContext(), "See more breaking news", Toast.LENGTH_SHORT).show();
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root;
    }

    private void setupFollowingSection() {
        followingContainer = binding.followingContainer;
        noFollowingText = binding.noFollowingText;
        followingChannelsRecyclerView = binding.followingChannelsRecyclerView;
        
        followingChannelsAdapter = new ChannelsAdapter(followingChannelsList, new ChannelsAdapter.OnChannelClickListener() {
            @Override
            public void onChannelClick(Channel channel) {
                // Navigate to channel articles
                Toast.makeText(getContext(), "Loading " + channel.getName() + " articles...", Toast.LENGTH_SHORT).show();
                loadArticlesFromChannel(channel.getId());
            }

            @Override
            public void onFollowClick(Channel channel, int position) {
                handleUnfollowClick(channel, position);
            }
        });
        followingChannelsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        followingChannelsRecyclerView.setAdapter(followingChannelsAdapter);
        followingChannelsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void handleUnfollowClick(Channel channel, int position) {
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            return;
        }
        
        newsRepository.unfollowChannel(channel.getId(), new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                if (response.isSuccess()) {
                    followingChannelsList.remove(position);
                    followingChannelsAdapter.notifyItemRemoved(position);
                    
                    if (followingChannelsList.isEmpty()) {
                        noFollowingText.setVisibility(View.VISIBLE);
                        followingChannelsRecyclerView.setVisibility(View.GONE);
                    }
                    
                    Toast.makeText(getContext(), "Unfollowed " + channel.getName(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to unfollow channel", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadArticlesFromChannel(int channelId) {
        newsRepository.getChannelArticles(channelId, 1, 20, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                if (response.isSuccess() && response.getData() != null) {
                    popularNewsList.clear();
                    List<Article> articles = JsonParsingUtils.parseArticles(response.getData());
                    popularNewsList.addAll(articles);
                    newsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getContext(), "Failed to load channel articles", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void handleBookmarkClick(Article article, int position, NewsAdapter adapter) {
        // Check if user is logged in
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.showLoginDialog();
            }
            return;
        }
        
        boolean isBookmarked = article.isBookmarked();
        article.setBookmarked(!isBookmarked);
        adapter.notifyItemChanged(position);
        
        if (newsRepository != null) {
            if (!isBookmarked) {
                newsRepository.bookmarkArticle(article.getId(), new NewsRepository.RepositoryCallback<JSONObject>() {
                    @Override
                    public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                        if (!response.isSuccess()) {
                            if (response.getStatusCode() == 400 || (response.getErrorMessage() != null && response.getErrorMessage().contains("duplicate"))) {
                                Toast.makeText(getContext(), "Article already bookmarked", Toast.LENGTH_SHORT).show();
                            } else {
                                article.setBookmarked(false);
                                adapter.notifyItemChanged(position);
                                Toast.makeText(getContext(), "Failed to bookmark article", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Article bookmarked", Toast.LENGTH_SHORT).show();
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
                        } else {
                            Toast.makeText(getContext(), "Bookmark removed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    private void setupHeaderClicks() {
        // User avatar click - navigate to profile if logged in, show login if not
        binding.userAvatar.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                // Navigate to profile fragment
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.navigateToProfile();
                }
            } else {
                // Show login dialog
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.showLoginDialog();
                }
            }
        });

        // Notification icon click
        binding.notificationIcon.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Notifications clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupTabNavigation() {
        // Feeds tab click
        binding.feedsTab.setOnClickListener(v -> {
            currentTab = "feeds";
            updateTabSelection(binding.feedsTab);
            loadFeedsContent();
        });

        // Popular tab click
        binding.popularTab.setOnClickListener(v -> {
            currentTab = "popular";
            updateTabSelection(binding.popularTab);
            loadPopularContent();
        });

        // Following tab click
        binding.followingTab.setOnClickListener(v -> {
            currentTab = "following";
            updateTabSelection(binding.followingTab);
            loadFollowingContent();
        });

        // Set default tab to Feeds
        updateTabSelection(binding.feedsTab);
    }

    private void updateUserInfo() {
        if (sessionManager.isLoggedIn()) {
            String userName = sessionManager.getUserName();
            String email = sessionManager.getUserEmail();
            
            // Show user name in header
            binding.userNameTextView.setText(userName.isEmpty() ? email.split("@")[0] : userName);
        } else {
            // Show generic user info
            binding.userNameTextView.setText("Guest User");
        }
    }

    private void updateTabSelection(android.view.View selectedTab) {
        // Reset all tabs to unselected state
        binding.feedsTab.setBackgroundResource(R.drawable.tab_background);
        binding.feedsTab.setTextColor(getResources().getColor(android.R.color.black));

        binding.popularTab.setBackgroundResource(R.drawable.tab_background);
        binding.popularTab.setTextColor(getResources().getColor(android.R.color.black));

        binding.followingTab.setBackgroundResource(R.drawable.tab_background);
        binding.followingTab.setTextColor(getResources().getColor(android.R.color.black));

        // Highlight selected tab with rounded background
        selectedTab.setBackgroundResource(R.drawable.tab_selected_background);
        if (selectedTab.getId() == R.id.feedsTab) {
            binding.feedsTab.setTextColor(android.graphics.Color.WHITE);
        } else if (selectedTab.getId() == R.id.popularTab) {
            binding.popularTab.setTextColor(android.graphics.Color.WHITE);
        } else if (selectedTab.getId() == R.id.followingTab) {
            binding.followingTab.setTextColor(android.graphics.Color.WHITE);
        }
    }

    private void loadFeedsContent() {
        // Hide following section, show news sections
        followingContainer.setVisibility(View.GONE);
        binding.breakingNewsTitle.setVisibility(View.VISIBLE);
        binding.seeMoreTextView.setVisibility(View.VISIBLE);
        binding.breakingNewsRecyclerView.setVisibility(View.VISIBLE);
        binding.popularNewsTitle.setVisibility(View.VISIBLE);
        binding.popularNewsRecyclerView.setVisibility(View.VISIBLE);
        
        loadArticlesFromAPI();
    }

    private void loadPopularContent() {
        // Hide following section, show news sections
        followingContainer.setVisibility(View.GONE);
        binding.breakingNewsTitle.setVisibility(View.VISIBLE);
        binding.seeMoreTextView.setVisibility(View.VISIBLE);
        binding.breakingNewsRecyclerView.setVisibility(View.VISIBLE);
        binding.popularNewsTitle.setVisibility(View.VISIBLE);
        binding.popularNewsRecyclerView.setVisibility(View.VISIBLE);
        
        loadArticlesFromAPI();
    }

    private void loadFollowingContent() {
        // Show following section, hide news sections
        binding.breakingNewsTitle.setVisibility(View.GONE);
        binding.seeMoreTextView.setVisibility(View.GONE);
        binding.breakingNewsRecyclerView.setVisibility(View.GONE);
        binding.popularNewsTitle.setVisibility(View.GONE);
        binding.popularNewsRecyclerView.setVisibility(View.GONE);
        
        followingContainer.setVisibility(View.VISIBLE);
        
        if (!sessionManager.isLoggedIn()) {
            noFollowingText.setVisibility(View.VISIBLE);
            noFollowingText.setText("Login to see channels you follow");
            followingChannelsRecyclerView.setVisibility(View.GONE);
            return;
        }
        
        // Load followed channels from API
        loadFollowedChannels();
    }

    private void loadFollowedChannels() {
        newsRepository.getFollowedChannels(new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                if (response.isSuccess() && response.getData() != null) {
                    parseFollowedChannels(response.getData());
                } else {
                    android.util.Log.e(TAG, "Failed to load followed channels: " + response.getErrorMessage());
                    noFollowingText.setVisibility(View.VISIBLE);
                    noFollowingText.setText("You haven't followed any channels yet.\nExplore channels to follow!");
                    followingChannelsRecyclerView.setVisibility(View.GONE);
                }
            }
        });
    }

    private void parseFollowedChannels(JSONObject data) {
        followingChannelsList.clear();
        
        List<Channel> channels = JsonParsingUtils.parseChannels(data);
        for (Channel channel : channels) {
            channel.setFollowing(true); // They are followed channels
            followingChannelsList.add(channel);
        }
        
        if (followingChannelsList.isEmpty()) {
            noFollowingText.setVisibility(View.VISIBLE);
            noFollowingText.setText("You haven't followed any channels yet.\nExplore channels to follow!");
            followingChannelsRecyclerView.setVisibility(View.GONE);
        } else {
            noFollowingText.setVisibility(View.GONE);
            followingChannelsRecyclerView.setVisibility(View.VISIBLE);
        }
        
        followingChannelsAdapter.notifyDataSetChanged();
    }

    private void setupRecyclerViews() {
        try {
            // Breaking News RecyclerView
            breakingNewsRecyclerView = binding.breakingNewsRecyclerView;
            breakingNewsList = new ArrayList<>();
            breakingNewsAdapter = new BreakingNewsAdapter(breakingNewsList, article -> {
                openArticleDetail(article);
            });
            breakingNewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
            breakingNewsRecyclerView.setAdapter(breakingNewsAdapter);

            // Popular News RecyclerView (Grid)
            popularNewsRecyclerView = binding.popularNewsRecyclerView;
            popularNewsList = new ArrayList<>();
            newsAdapter = new NewsAdapter(popularNewsList, new NewsAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(Article article) {
                    openArticleDetail(article);
                }

                @Override
                public void onBookmarkClick(Article article, int position) {
                    handleBookmarkClick(article, position, newsAdapter);
                }
            });
            popularNewsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1)); // Changed to 1 column for hero layout
            popularNewsRecyclerView.setAdapter(newsAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMockNews() {
        // Load articles from API instead of mock data
        loadArticlesFromAPI();
        
        // Load bookmarks if user is logged in
        if (sessionManager.isLoggedIn()) {
            loadBookmarksFromAPI();
        }
    }

    private void loadArticlesFromAPI() {
        android.util.Log.d(TAG, "=== Starting loadArticlesFromAPI() ===");
        
        if (newsRepository != null) {
            android.util.Log.d(TAG, "Calling newsRepository.getArticles()");
            newsRepository.getArticles(new NewsRepository.RepositoryCallback<JSONObject>() {
                @Override
                public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                    android.util.Log.d(TAG, "API Response received - Success: " + response.isSuccess());
                    
                    if (response.isSuccess() && response.getData() != null) {
                        // Clear existing data
                        breakingNewsList.clear();
                        popularNewsList.clear();
                        
                        // Parse articles using utility
                        List<Article> articles = JsonParsingUtils.parseArticles(response.getData());
                        
                        // Split into breaking and popular
                        for (int i = 0; i < articles.size(); i++) {
                            if (i < 5) {
                                breakingNewsList.add(articles.get(i));
                            } else if (i < 15) {
                                popularNewsList.add(articles.get(i));
                            }
                        }
                        
                        // Update UI
                        if (breakingNewsAdapter != null) {
                            breakingNewsAdapter.notifyDataSetChanged();
                        }
                        if (newsAdapter != null) {
                            newsAdapter.notifyDataSetChanged();
                        }
                    } else {
                        android.util.Log.e(TAG, "Failed to load articles: " + response.getErrorMessage());
                    }
                }
            });
        }
    }



    private void loadBookmarksFromAPI() {
        if (newsRepository != null) {
            newsRepository.getBookmarks(new NewsRepository.RepositoryCallback<JSONObject>() {
                @Override
                public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                    if (response.isSuccess() && response.getData() != null) {
                        android.util.Log.d(TAG, "Bookmarks loaded from API");
                        
                        // Parse bookmarked IDs using utility
                        java.util.Set<String> bookmarkedIds = JsonParsingUtils.parseBookmarkedIds(response.getData());
                        
                        // Update bookmark status for articles in current lists
                        for (Article article : breakingNewsList) {
                            if (bookmarkedIds.contains(article.getId())) {
                                article.setBookmarked(true);
                            }
                        }
                        
                        for (Article article : popularNewsList) {
                            if (bookmarkedIds.contains(article.getId())) {
                                article.setBookmarked(true);
                            }
                        }
                        
                        // Refresh adapters
                        if (breakingNewsAdapter != null) {
                            breakingNewsAdapter.notifyDataSetChanged();
                        }
                        if (newsAdapter != null) {
                            newsAdapter.notifyDataSetChanged();
                        }
                    } else {
                        android.util.Log.e(TAG, "Failed to load bookmarks: " + response.getErrorMessage());
                    }
                }
            });
        }
    }

    public void switchToSource(String source) {
        // For frontend-only implementation, source switching is handled by different mock data
        this.currentSource = source;
        loadArticlesFromAPI();
    }

    private void openArticleDetail(Article article) {
        try {
            if (getContext() != null) {
                Intent intent = new Intent(getContext(), ArticleDetailActivity.class);
                intent.putExtra("article", article);
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
