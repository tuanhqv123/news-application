package com.example.newsapplication.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;
import com.example.newsapplication.adapter.AdminArticlesAdapter;
import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.api.endpoints.ArticleEndpoints;
import com.example.newsapplication.model.Article;
import com.google.android.material.chip.ChipGroup;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ManageArticlesActivity extends AppCompatActivity {

    private RecyclerView articlesRecyclerView;
    private AdminArticlesAdapter adapter;
    private ProgressBar progressBar;
    private ChipGroup filterChipGroup;
    private ArticleEndpoints articleEndpoints;
    private String currentFilter = null;
    private List<Article> allArticles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_articles);

        articleEndpoints = new ArticleEndpoints(new ApiClient(this));

        initViews();
        setupRecyclerView();
        setupFilterChips();
        loadArticles();
    }

    private void initViews() {
        articlesRecyclerView = findViewById(R.id.articlesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AdminArticlesAdapter(this, new ArrayList<>(), article -> showArticleActionsDialog(article));
        articlesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        articlesRecyclerView.setAdapter(adapter);
    }

    private void setupFilterChips() {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentFilter = null;
                filterArticles();
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentFilter = null;
            } else if (checkedId == R.id.chipPending) {
                currentFilter = "pending_review";
            } else if (checkedId == R.id.chipPublished) {
                currentFilter = "published";
            } else if (checkedId == R.id.chipRejected) {
                currentFilter = "rejected";
            }
            filterArticles();
        });
    }

    private void loadArticles() {
        progressBar.setVisibility(View.VISIBLE);

        articleEndpoints.getAllArticlesAdmin(1, 100, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    parseArticles(response.getData());
                });
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageArticlesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void parseArticles(JSONObject response) {
        allArticles.clear();
        try {
            JSONObject data = response.has("data") ? response.getJSONObject("data") : response;
            JSONArray articlesArray = data.getJSONArray("articles");

            for (int i = 0; i < articlesArray.length(); i++) {
                JSONObject obj = articlesArray.getJSONObject(i);
                Article article = new Article();
                article.setId(obj.optString("id"));
                article.setTitle(obj.optString("title"));
                article.setDescription(obj.optString("summary"));
                article.setContent(obj.optString("content"));
                article.setImageUrl(obj.optString("hero_image_url"));
                article.setDate(obj.optString("created_at"));
                article.setStatus(obj.optString("status", "draft"));

                // Parse author info
                if (obj.has("author") && !obj.isNull("author")) {
                    JSONObject author = obj.getJSONObject("author");
                    article.setAuthor(author.optString("display_name", "Unknown"));
                } else {
                    article.setAuthor("Unknown");
                }

                // Parse channel info
                if (obj.has("channels") && !obj.isNull("channels")) {
                    JSONObject channel = obj.getJSONObject("channels");
                    article.setChannelName(channel.optString("name", ""));
                }

                allArticles.add(article);
            }
            filterArticles();
        } catch (Exception e) {
            Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void filterArticles() {
        if (currentFilter == null) {
            adapter.updateArticles(allArticles);
        } else {
            List<Article> filtered = new ArrayList<>();
            for (Article article : allArticles) {
                if (currentFilter.equals(article.getStatus())) {
                    filtered.add(article);
                }
            }
            adapter.updateArticles(filtered);
        }
    }

    private void showArticleActionsDialog(Article article) {
        String[] actions = {"View Detail", "Update Status"};

        new AlertDialog.Builder(this)
                .setTitle(article.getTitle())
                .setItems(actions, (dialog, which) -> {
                    if (which == 0) {
                        openArticleDetail(article);
                    } else if (which == 1) {
                        showChangeStatusDialog(article);
                    }
                })
                .show();
    }

    private void openArticleDetail(Article article) {
        android.content.Intent intent = new android.content.Intent(this, com.example.newsapplication.ArticleDetailActivity.class);
        intent.putExtra("article", article);
        startActivity(intent);
    }

    private void showChangeStatusDialog(Article article) {
        String currentStatus = article.getStatus();
        if (currentStatus == null) currentStatus = "draft";

        String[] statuses = {"draft", "pending_review", "published", "rejected"};
        String[] statusLabels = {"Draft", "Pending Review", "Published", "Rejected"};

        int currentIndex = 0;
        for (int i = 0; i < statuses.length; i++) {
            if (statuses[i].equals(currentStatus)) {
                currentIndex = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("Change Status")
                .setSingleChoiceItems(statusLabels, currentIndex, null)
                .setPositiveButton("Update", (dialog, which) -> {
                    int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    String newStatus = statuses[selectedPosition];
                    if (!newStatus.equals(article.getStatus())) {
                        updateArticleStatus(article, newStatus);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void approveArticle(Article article) {
        progressBar.setVisibility(View.VISIBLE);
        articleEndpoints.approveArticle(article.getId(), new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageArticlesActivity.this, "Article approved", Toast.LENGTH_SHORT).show();
                    loadArticles();
                });
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageArticlesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void rejectArticle(Article article) {
        progressBar.setVisibility(View.VISIBLE);
        articleEndpoints.rejectArticle(article.getId(), new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageArticlesActivity.this, "Article rejected", Toast.LENGTH_SHORT).show();
                    loadArticles();
                });
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageArticlesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateArticleStatus(Article article, String status) {
        progressBar.setVisibility(View.VISIBLE);
        articleEndpoints.updateArticleStatus(article.getId(), status, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageArticlesActivity.this, "Status updated", Toast.LENGTH_SHORT).show();
                    loadArticles();
                });
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageArticlesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
