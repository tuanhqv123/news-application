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
import com.example.newsapplication.adapter.BreakingNewsAdapter;
import com.example.newsapplication.adapter.ChannelsAdapter;
import com.example.newsapplication.adapter.NewsAdapter;
import com.example.newsapplication.databinding.FragmentHomeBinding;
import com.example.newsapplication.model.Article;
import com.example.newsapplication.model.Channel;
import com.example.newsapplication.utils.JsonParsingUtils;

import com.example.newsapplication.auth.UserSessionManager;
import com.example.newsapplication.auth.AuthService;
import com.example.newsapplication.repository.NewsRepository;
import com.example.newsapplication.utils.CircleTransform;
import com.example.newsapplication.ui.notifications.NotificationHistoryActivity;
import com.example.newsapplication.database.NotificationHistoryHelper;
import com.squareup.picasso.Picasso;
import org.json.JSONObject;
import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

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
    private int currentPage = 1;
    private boolean isLoadingMore = false;
    private boolean hasMoreData = true;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        //PHẢI INFLATE BINDING TRƯỚC
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // SAU ĐÓ MỚI SETUP VIEWS
        sessionManager = new UserSessionManager(getContext());
        authService = new AuthService(getContext());
        newsRepository = new NewsRepository(getContext());
        followingChannelsList = new ArrayList<>();

        setupRecyclerViews();
        setupFollowingSection();

        // Setup load more button (SAU KHI binding đã được inflate)
        binding.loadMoreButton.setOnClickListener(v -> {
            loadMoreArticles();
        });

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
                // Navigate to channel articles activity
                navigateToChannelArticles(channel);
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
                    Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMoreArticles() {
        if (isLoadingMore || !hasMoreData) {
            return;
        }

        isLoadingMore = true;
        binding.loadMoreButton.setText("Loading...");
        binding.loadMoreButton.setEnabled(false);

        currentPage++;

        // Load theo tab hiện tại
        if (currentTab.equals("feeds")) {
            loadMoreFeedsContent();
        } else if (currentTab.equals("popular")) {
            loadMorePopularContent();
        }
    }

    private void loadMoreFeedsContent() {
        newsRepository.getArticles(currentPage, 20, 1, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                isLoadingMore = false;
                binding.loadMoreButton.setText("Load More");
                binding.loadMoreButton.setEnabled(true);

                if (response.isSuccess() && response.getData() != null) {
                    List<Article> newArticles = JsonParsingUtils.parseArticles(response.getData());

                    if (newArticles.isEmpty()) {
                        hasMoreData = false;
                        binding.loadMoreButton.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No more articles", Toast.LENGTH_SHORT).show();
                    } else {
                        int startPosition = popularNewsList.size();
                        popularNewsList.addAll(newArticles);
                        newsAdapter.notifyItemRangeInserted(startPosition, newArticles.size());
                    }
                } else {
                    currentPage--; // Rollback page if failed
                    Toast.makeText(getContext(), "Failed to load more", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadMorePopularContent() {
        newsRepository.getArticles(currentPage, 20, null, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                isLoadingMore = false;
                binding.loadMoreButton.setText("Load More");
                binding.loadMoreButton.setEnabled(true);

                if (response.isSuccess() && response.getData() != null) {
                    List<Article> newArticles = JsonParsingUtils.parseArticles(response.getData());

                    if (newArticles.isEmpty()) {
                        hasMoreData = false;
                        binding.loadMoreButton.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "No more articles", Toast.LENGTH_SHORT).show();
                    } else {
                        int startPosition = popularNewsList.size();
                        popularNewsList.addAll(newArticles);
                        newsAdapter.notifyItemRangeInserted(startPosition, newArticles.size());
                    }
                } else {
                    currentPage--; // Rollback page if failed
                    Toast.makeText(getContext(), "Failed to load more", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_SHORT).show();
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
            // Check if user is logged in
            UserSessionManager sessionManager = new UserSessionManager(requireContext());
            if (!sessionManager.isLoggedIn()) {
                Toast.makeText(getContext(), "Please login to view notifications", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(getContext(), NotificationHistoryActivity.class);
            startActivity(intent);
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
            String avatarUrl = sessionManager.getAvatarUrl();
            
            // Show user name in header
            binding.userNameTextView.setText(userName.isEmpty() ? email.split("@")[0] : userName);
            
            // Load avatar image with circular transform
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Picasso.get()
                        .load(avatarUrl)
                        .transform(new CircleTransform())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(binding.userAvatar);
            }
        } else {
            // Show generic user info
            binding.userNameTextView.setText("Guest User");
        }
    }

    private void updateTabSelection(android.view.View selectedTab) {
        // Reset all tabs to unselected state (orange background, white text)
        binding.feedsTab.setBackgroundResource(android.R.color.transparent);
        binding.feedsTab.setTextColor(android.graphics.Color.WHITE);

        binding.popularTab.setBackgroundResource(android.R.color.transparent);
        binding.popularTab.setTextColor(android.graphics.Color.WHITE);

        binding.followingTab.setBackgroundResource(android.R.color.transparent);
        binding.followingTab.setTextColor(android.graphics.Color.WHITE);

        // Highlight selected tab with white background and orange text
        selectedTab.setBackgroundResource(R.drawable.tab_selected_background);
        if (selectedTab.getId() == R.id.feedsTab) {
            binding.feedsTab.setTextColor(0xFFF39E3A); // Orange color
        } else if (selectedTab.getId() == R.id.popularTab) {
            binding.popularTab.setTextColor(0xFFF39E3A);
        } else if (selectedTab.getId() == R.id.followingTab) {
            binding.followingTab.setTextColor(0xFFF39E3A);
        }
    }

    private void loadFeedsContent() {
        // Reset pagination
        currentPage = 1;
        hasMoreData = true;

        // Show feeds content, hide following
        binding.feedsPopularContainer.setVisibility(View.VISIBLE);
        followingContainer.setVisibility(View.GONE);

        // Update title
        binding.popularNewsTitle.setText("Latest News");
        binding.popularNewsTitle.setVisibility(View.VISIBLE);

        // Show load more button
        binding.loadMoreButton.setVisibility(View.VISIBLE);

        // Load articles from category_id = 1
        loadArticlesFromCategory(1);
    }

    private void loadPopularContent() {
        // Reset pagination
        currentPage = 1;
        hasMoreData = true;

        // Show popular content, hide following
        binding.feedsPopularContainer.setVisibility(View.VISIBLE);
        followingContainer.setVisibility(View.GONE);

        // Update title
        binding.popularNewsTitle.setText("Popular News");
        binding.popularNewsTitle.setVisibility(View.VISIBLE);

        // Show load more button
        binding.loadMoreButton.setVisibility(View.VISIBLE);

        // Load all articles (no category filter)
        loadArticlesFromAPI();
    }

    private void loadFollowingContent() {
        // Hide load more button for following tab
        binding.loadMoreButton.setVisibility(View.GONE);

        // Show following section, hide feeds/popular
        binding.feedsPopularContainer.setVisibility(View.GONE);
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


    private void navigateToChannelArticles(Channel channel) {
        // Navigate to explore tab and show channel articles
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            mainActivity.navigateToExploreWithChannel(channel.getId(), channel.getName(), channel.isFollowing());
        }
    }

    private void loadFollowedChannels() {
        newsRepository.getFollowedChannels(new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                if (response.isSuccess() && response.getData() != null) {
                    parseFollowedChannels(response.getData());
                } else {
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
        // Load breaking news from category 9
        loadBreakingNews();

        // ✅ Load content theo tab hiện tại (default: feeds)
        if (currentTab.equals("feeds")) {
            loadFeedsContent();
        } else if (currentTab.equals("popular")) {
            loadPopularContent();
        } else if (currentTab.equals("following")) {
            loadFollowingContent();
        }

        // Load bookmarks if user is logged in
        if (sessionManager.isLoggedIn()) {
            loadBookmarksFromAPI();
        }
    }


    private void loadBreakingNews() {
        if (newsRepository != null) {
            newsRepository.getArticles(1, 5, 9, new NewsRepository.RepositoryCallback<JSONObject>() {
                @Override
                public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                    if (response.isSuccess() && response.getData() != null) {
                        breakingNewsList.clear();
                        List<Article> articles = JsonParsingUtils.parseArticles(response.getData());
                        breakingNewsList.addAll(articles);
                        
                        if (breakingNewsAdapter != null) {
                            breakingNewsAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

    private void loadArticlesFromCategory(int categoryId) {
        if (newsRepository != null) {
            newsRepository.getArticles(1, 20, categoryId, new NewsRepository.RepositoryCallback<JSONObject>() {
                @Override
                public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                    if (response.isSuccess() && response.getData() != null) {
                        popularNewsList.clear();
                        
                        List<Article> articles = JsonParsingUtils.parseArticles(response.getData());
                        popularNewsList.addAll(articles);
                        
                        if (newsAdapter != null) {
                            newsAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }
    
    private void loadArticlesFromAPI() {
        if (newsRepository != null) {
            newsRepository.getArticles(new NewsRepository.RepositoryCallback<JSONObject>() {
                @Override
                public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                    if (response.isSuccess() && response.getData() != null) {
                        popularNewsList.clear();
                        
                        List<Article> articles = JsonParsingUtils.parseArticles(response.getData());
                        popularNewsList.addAll(articles);
                        
                        if (newsAdapter != null) {
                            newsAdapter.notifyDataSetChanged();
                        }
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
                        java.util.Set<String> bookmarkedIds = JsonParsingUtils.parseBookmarkedIds(response.getData());
                        
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
                        
                        if (breakingNewsAdapter != null) {
                            breakingNewsAdapter.notifyDataSetChanged();
                        }
                        if (newsAdapter != null) {
                            newsAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

    public void switchToSource(String source) {
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
