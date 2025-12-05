package com.example.newsapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.newsapplication.adapter.MyArticlesAdapter;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.model.Article;
import com.example.newsapplication.repository.NewsRepository;
import com.example.newsapplication.utils.JsonParsingUtils;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class MyArticlesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyArticlesAdapter adapter;
    private ProgressBar progressBar;
    private TextView emptyStateText;
    private NewsRepository newsRepository;
    private List<Article> articles = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_articles);

        newsRepository = new NewsRepository(this);

        initViews();
        setupRecyclerView();
        loadMyArticles();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.myArticlesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateText = findViewById(R.id.emptyStateText);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new MyArticlesAdapter(articles, article -> {
            if (article != null) {
                Intent intent = new Intent(MyArticlesActivity.this, ArticleDetailActivity.class);
                intent.putExtra("article", article);
                startActivity(intent);
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadMyArticles() {
        progressBar.setVisibility(View.VISIBLE);
        emptyStateText.setVisibility(View.GONE);

        newsRepository.getMyArticles(1, 50, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(ApiResponse<JSONObject> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccess() && response.getData() != null) {
                    articles.clear();
                    articles.addAll(JsonParsingUtils.parseArticles(response.getData()));
                    adapter.notifyDataSetChanged();

                    if (articles.isEmpty()) {
                        emptyStateText.setVisibility(View.VISIBLE);
                        emptyStateText.setText("You haven't created any articles yet");
                    }
                } else {
                    Toast.makeText(MyArticlesActivity.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
