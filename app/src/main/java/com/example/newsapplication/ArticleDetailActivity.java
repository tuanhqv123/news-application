package com.example.newsapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsapplication.model.Article;

public class ArticleDetailActivity extends AppCompatActivity {

    private ImageView backImageView;
    private ImageView articleImageView;
    private ImageView bookmarkImageView;
    private ImageView shareImageView;
    private TextView titleTextView;
    private TextView sourceTextView;
    private TextView dateTextView;
    private TextView categoryTextView;
    private TextView contentTextView;

    private Article currentArticle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        initViews();
        setupData();
        setupClickListeners();
    }

    private void initViews() {
        backImageView = findViewById(R.id.backImageView);
        articleImageView = findViewById(R.id.articleImageView);
        bookmarkImageView = findViewById(R.id.bookmarkImageView);
        shareImageView = findViewById(R.id.shareImageView);
        titleTextView = findViewById(R.id.titleTextView);
        sourceTextView = findViewById(R.id.sourceTextView);
        dateTextView = findViewById(R.id.dateTextView);
        categoryTextView = findViewById(R.id.categoryTextView);
        contentTextView = findViewById(R.id.contentTextView);
    }

    private void setupData() {
        // Get article from intent
        if (getIntent().hasExtra("article")) {
            currentArticle = (Article) getIntent().getSerializableExtra("article");

            if (currentArticle != null) {
                titleTextView.setText(currentArticle.getTitle());
                sourceTextView.setText(currentArticle.getSource());
                dateTextView.setText(currentArticle.getDate());
                categoryTextView.setText(currentArticle.getCategory());
                contentTextView.setText(currentArticle.getContent());

                // Load article image from local resources for frontend-only implementation
                try {
                    if (currentArticle.getImageResId() != 0) {
                        articleImageView.setImageResource(currentArticle.getImageResId());
                    } else {
                        // Set default placeholder if no image resource is available
                        articleImageView.setImageResource(R.drawable.ic_launcher_foreground);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Update bookmark icon
                updateBookmarkIcon();
            }
        }
    }

    private void setupClickListeners() {
        backImageView.setOnClickListener(v -> finish());

        bookmarkImageView.setOnClickListener(v -> {
            if (currentArticle != null) {
                currentArticle.setBookmarked(!currentArticle.isBookmarked());
                updateBookmarkIcon();
            }
        });

        shareImageView.setOnClickListener(v -> {
            // Simple share functionality
            if (currentArticle != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, currentArticle.getTitle() + " - " + currentArticle.getSource());
                startActivity(Intent.createChooser(shareIntent, "Share Article"));
            }
        });
    }

    private void updateBookmarkIcon() {
        if (currentArticle != null && currentArticle.isBookmarked()) {
            bookmarkImageView.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            bookmarkImageView.setImageResource(R.drawable.ic_bookmark_outline);
        }
    }
}