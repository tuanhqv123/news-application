package com.example.newsapplication.ui.home;

import android.os.Bundle;
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

import com.example.newsapplication.R;
import com.example.newsapplication.databinding.FragmentHomeBinding;
import com.example.newsapplication.model.Article;

import com.example.newsapplication.adapter.breaking.BreakingNewsAdapter;
import com.example.newsapplication.adapter.NewsAdapter;

import java.util.ArrayList;
import java.util.List;

public class HomeFragmentSimple extends Fragment {

    private FragmentHomeBinding binding;
    private BreakingNewsAdapter breakingNewsAdapter;
    private NewsAdapter newsAdapter;
    private RecyclerView breakingNewsRecyclerView;
    private RecyclerView popularNewsRecyclerView;
    private List<Article> breakingNewsList;
    private List<Article> popularNewsList;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerViews();
        loadMockDataForTesting();

        return root;
    }

    private void setupRecyclerViews() {
        // Breaking News RecyclerView
        breakingNewsRecyclerView = binding.breakingNewsRecyclerView;
        breakingNewsList = new ArrayList<>();
        breakingNewsAdapter = new BreakingNewsAdapter(breakingNewsList, article -> {
            // Handle article click
        });
        breakingNewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        breakingNewsRecyclerView.setAdapter(breakingNewsAdapter);

        // Popular News RecyclerView (Grid)
        popularNewsRecyclerView = binding.popularNewsRecyclerView;
        popularNewsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(popularNewsList, new NewsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article) {
                // Handle article click
            }

            @Override
            public void onBookmarkClick(Article article, int position) {
                // Handle bookmark click
                article.setBookmarked(!article.isBookmarked());
                newsAdapter.notifyItemChanged(position);
            }
        });
        popularNewsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        popularNewsRecyclerView.setAdapter(newsAdapter);
    }

    private void loadMockDataForTesting() {
        // Add sample articles to test UI
        Article testArticle1 = new Article(
                "1",
                "Test Article 1",
                "This is a test article to verify the UI is working correctly",
                "This is the full content of test article 1 to verify the UI is working correctly.",
                "Test Author",
                "MockDataProvider",
                "General",
                "https://example.com/image1.jpg",
                R.drawable.placeholder_image,
                "2025-12-01 19:00:00",
                false
        );
        
        Article testArticle2 = new Article(
                "2",
                "Test Article 2",
                "This is another test article to verify bookmark functionality",
                "This is the full content of test article 2 to verify bookmark functionality.",
                "Test Author 2",
                "MockDataProvider", 
                "Technology",
                "https://example.com/image2.jpg",
                R.drawable.ic_launcher_foreground,
                "2025-12-01 19:05:00",
                false
        );

        breakingNewsList.add(testArticle1);
        breakingNewsList.add(testArticle2);
        
        popularNewsList.add(testArticle1);
        popularNewsList.add(testArticle2);
        
        // Update adapters
        breakingNewsAdapter.notifyDataSetChanged();
        newsAdapter.notifyDataSetChanged();
        
        Toast.makeText(getContext(), "Mock data loaded for testing", Toast.LENGTH_SHORT).show();
    }
}
