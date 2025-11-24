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
import com.example.newsapplication.data.MockDataProvider;

import java.util.ArrayList;
import java.util.List;

public class SavedFragment extends Fragment {

    private FragmentSavedBinding binding;
    private NewsAdapter savedNewsAdapter;
    private RecyclerView savedNewsRecyclerView;
    private List<Article> savedArticlesList;
    private MockDataProvider mockDataProvider;
    private LinearLayout emptyStateText;
    private LinearLayout loginButtonContainer;
    private UserSessionManager sessionManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSavedBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize session manager
        sessionManager = new UserSessionManager(getContext());

        initViews();
        setupClickListeners();

        // Always show saved articles (no login required)
        showLoggedInState();

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
        // Show login UI
        loginButtonContainer.setVisibility(View.VISIBLE);
        savedNewsRecyclerView.setVisibility(View.GONE);
        emptyStateText.setVisibility(View.GONE);
    }

    private void showLoggedInState() {
        // Show saved articles UI
        loginButtonContainer.setVisibility(View.GONE);
        setupRecyclerView();
        loadSavedArticles();
    }

    private void setupRecyclerView() {
        mockDataProvider = new MockDataProvider();
        savedArticlesList = new ArrayList<>();

        savedNewsAdapter = new NewsAdapter(savedArticlesList, new NewsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article) {
                // Handle article click - navigate to article detail
                Toast.makeText(getContext(), "Opening saved article: " + article.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBookmarkClick(Article article, int position) {
                // Handle bookmark click (toggle bookmark)
                article.setBookmarked(!article.isBookmarked());
                if (!article.isBookmarked()) {
                    savedArticlesList.remove(article);
                    savedNewsAdapter.notifyDataSetChanged();
                    Toast.makeText(getContext(), "Article removed from saved", Toast.LENGTH_SHORT).show();
                }
            }
        });

        savedNewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        savedNewsRecyclerView.setAdapter(savedNewsAdapter);
    }

    private void loadSavedArticles() {
        // Get bookmarked articles from mock data
        savedArticlesList.clear();

        // Add some mock saved articles
        List<Article> allArticles = MockDataProvider.getPopularNews();
        for (Article article : allArticles) {
            // For demo purposes, let's mark some articles as bookmarked
            if (article.getTitle().contains("Tech") || article.getTitle().contains("Politics")) {
                article.setBookmarked(true);
                savedArticlesList.add(article);
            }
        }

        // If no saved articles, show empty state
        if (savedArticlesList.isEmpty()) {
            savedNewsRecyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            savedNewsRecyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
        }

        savedNewsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}