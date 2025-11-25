package com.example.newsapplication.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.ArticleDetailActivity;
import com.example.newsapplication.R;
import com.example.newsapplication.adapter.BreakingNewsAdapter;
import com.example.newsapplication.adapter.NewsAdapter;
import com.example.newsapplication.data.MockDataProvider;
import com.example.newsapplication.data.remote.SupabaseNewsRemoteDataSource;
import com.example.newsapplication.databinding.FragmentHomeBinding;
import com.example.newsapplication.model.Article;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private NewsAdapter newsAdapter;
    private BreakingNewsAdapter breakingNewsAdapter;
    private RecyclerView breakingNewsRecyclerView;
    private RecyclerView popularNewsRecyclerView;
    private List<Article> breakingNewsList;
    private List<Article> popularNewsList;
    private MockDataProvider mockDataProvider;
    private String currentSource = "VnExpress";

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mockDataProvider = new MockDataProvider();
        setupRecyclerViews();
        loadBreakingNewsFromMock();
        loadPopularNewsFromMock();
        loadBreakingNews();
        loadPopularNews();
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

    private void setupHeaderClicks() {
        // User avatar click
        binding.userAvatar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "User profile clicked", Toast.LENGTH_SHORT).show();
        });

        // Notification icon click
        binding.notificationIcon.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Notifications clicked", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupTabNavigation() {
        // Feeds tab click
        binding.feedsTab.setOnClickListener(v -> {
            updateTabSelection(binding.feedsTab);
            loadFeedsContent();
        });

        // Popular tab click
        binding.popularTab.setOnClickListener(v -> {
            updateTabSelection(binding.popularTab);
            loadPopularContent();
        });

        // Following tab click
        binding.followingTab.setOnClickListener(v -> {
            updateTabSelection(binding.followingTab);
            loadFollowingContent();
        });

        // Set default tab to Popular
        updateTabSelection(binding.popularTab);
    }

    private void updateTabSelection(android.view.View selectedTab) {
        // Reset all tabs to unselected state
        binding.feedsTab.setBackgroundResource(R.drawable.tab_background);
        binding.feedsTab.setTextColor(getResources().getColor(R.color.black, null));

        binding.popularTab.setBackgroundResource(R.drawable.tab_background);
        binding.popularTab.setTextColor(getResources().getColor(R.color.black, null));

        binding.followingTab.setBackgroundResource(R.drawable.tab_background);
        binding.followingTab.setTextColor(getResources().getColor(R.color.black, null));

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
        Toast.makeText(getContext(), "Loading Feeds content", Toast.LENGTH_SHORT).show();
        loadBreakingNews();
    }

    private void loadPopularContent() {
        Toast.makeText(getContext(), "Loading Popular content", Toast.LENGTH_SHORT).show();
        loadPopularNews();
    }

    private void loadFollowingContent() {
        Toast.makeText(getContext(), "Loading Following content", Toast.LENGTH_SHORT).show();
        SupabaseNewsRemoteDataSource.fetchPopularNews(articles -> {
            List<Article> filtered = new ArrayList<>();
            for (Article article : articles) {
                if (article.getSource() != null && article.getSource().equalsIgnoreCase(currentSource)) {
                    filtered.add(article);
                }
            }
            if (filtered.isEmpty()) {
                filtered = new ArrayList<>(articles);
            }
            updatePopularNews(filtered);
        }, throwable -> {
            Log.e("HomeFragment", "Failed to load following content", throwable);
            loadPopularNewsFromMock();
            showRemoteErrorToast();
        });
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
                    article.setBookmarked(!article.isBookmarked());
                    newsAdapter.notifyItemChanged(position);
                }
            });
            popularNewsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
            popularNewsRecyclerView.setAdapter(newsAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBreakingNews() {
        SupabaseNewsRemoteDataSource.fetchBreakingNews(articles -> {
            if (articles != null && !articles.isEmpty()) {
                updateBreakingNews(articles);
            }
        }, throwable -> {
            Log.e("HomeFragment", "Failed to load breaking news from Supabase", throwable);
            loadBreakingNewsFromMock();
            showRemoteErrorToast();
        });
    }

    private void loadPopularNews() {
        SupabaseNewsRemoteDataSource.fetchPopularNews(articles -> {
            if (articles != null && !articles.isEmpty()) {
                updatePopularNews(articles);
            }
        }, throwable -> {
            Log.e("HomeFragment", "Failed to load popular news from Supabase", throwable);
            loadPopularNewsFromMock();
            showRemoteErrorToast();
        });
    }

    private void updateBreakingNews(List<Article> articles) {
        breakingNewsList.clear();
        breakingNewsList.addAll(articles.subList(0, Math.min(10, articles.size())));
        breakingNewsAdapter.notifyDataSetChanged();
    }

    private void updatePopularNews(List<Article> articles) {
        popularNewsList.clear();
        popularNewsList.addAll(articles.subList(0, Math.min(20, articles.size())));
        newsAdapter.notifyDataSetChanged();
    }

    private void loadBreakingNewsFromMock() {
        if (mockDataProvider != null) {
            List<Article> articles = mockDataProvider.getBreakingNews();
            if (articles != null && !articles.isEmpty()) {
                updateBreakingNews(articles);
            }
        }
    }

    private void loadPopularNewsFromMock() {
        if (mockDataProvider != null) {
            List<Article> articles = mockDataProvider.getPopularNews();
            if (articles != null && !articles.isEmpty()) {
                updatePopularNews(articles);
            }
        }
    }

    private void showRemoteErrorToast() {
        if (getContext() != null) {
            Toast.makeText(getContext(), "Không thể tải dữ liệu Supabase, dùng dữ liệu mẫu.", Toast.LENGTH_SHORT).show();
        }
    }

    public void switchToSource(String source) {
        // For frontend-only implementation, source switching is handled by different mock data
        this.currentSource = source;
        loadFollowingContent();
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
        // No RSS service to shutdown in frontend-only implementation
    }
}