package com.example.newsapplication.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;
import com.example.newsapplication.adapter.NewsAdapter;
import com.example.newsapplication.data.MockDataProvider;
import com.example.newsapplication.databinding.FragmentDashboardBinding;
import com.example.newsapplication.model.Article;

import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private NewsAdapter contentAdapter;
    private RecyclerView contentRecyclerView;
    private MockDataProvider mockDataProvider;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initViews();
        setupContent();
        setupSearchBar();

        return root;
    }

    private void initViews() {
        contentRecyclerView = binding.contentRecyclerView;

        mockDataProvider = new MockDataProvider();
    }

    private void setupContent() {
        // Setup RecyclerView with content
        List<Article> contentArticles = mockDataProvider.getPopularNews();

        contentAdapter = new NewsAdapter(contentArticles, new NewsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Article article) {
                // Handle article click - navigate to detail
                Toast.makeText(getContext(), "Opening article: " + article.getTitle(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBookmarkClick(Article article, int position) {
                article.setBookmarked(!article.isBookmarked());
                contentAdapter.notifyItemChanged(position);
                Toast.makeText(getContext(),
                    article.isBookmarked() ? "Bookmarked" : "Removed from bookmarks",
                    Toast.LENGTH_SHORT).show();
            }
        });

        contentRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        contentRecyclerView.setAdapter(contentAdapter);

        // Handle "See More" click
        binding.seeMoreTrending.setOnClickListener(v -> {
            Toast.makeText(getContext(), "See more trending articles", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSearchBar() {
        // Handle search bar click
        binding.searchBar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Search clicked", Toast.LENGTH_SHORT).show();
        });

        binding.searchIcon.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Search icon clicked", Toast.LENGTH_SHORT).show();
        });
    }

  
  
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}