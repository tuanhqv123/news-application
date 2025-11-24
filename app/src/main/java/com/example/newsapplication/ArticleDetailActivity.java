package com.example.newsapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.newsapplication.model.Article;
import com.example.newsapplication.ui.FontSizeDialog;
import com.example.newsapplication.utils.FontSizeManager;

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

    // Font size controls
    private TextView fontSizeIcon;
    private FontSizeDialog fontSizeDialog;

    private Article currentArticle;
    private FontSizeManager fontSizeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        // Initialize font size manager
        fontSizeManager = new FontSizeManager(this);

        initViews();
        setupData();
        setupClickListeners();
        applyFontSize();
    }

    private void initViews() {
        backImageView = findViewById(R.id.backImageView);
        articleImageView = findViewById(R.id.articleImageView);
        bookmarkImageView = findViewById(R.id.bookmarkImageView);
        shareImageView = findViewById(R.id.shareImageView);
        fontSizeIcon = findViewById(R.id.fontSizeIcon);
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

        // Font size icon click listener
        fontSizeIcon.setOnClickListener(v -> {
            android.util.Log.d("ArticleDetail", "Font size icon clicked");
            showFontSizeDialog();
        });
    }

    private void updateBookmarkIcon() {
        if (currentArticle != null && currentArticle.isBookmarked()) {
            bookmarkImageView.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            bookmarkImageView.setImageResource(R.drawable.ic_bookmark_outline);
        }
    }

    
    private void applyFontSize() {
        float fontSize = fontSizeManager.getFontSize();

        // Apply font size to content text (main article content)
        contentTextView.setTextSize(fontSize);

        // You can also adjust other text elements if needed
        // For now, we'll keep title and other metadata at their current sizes
        // titleTextView.setTextSize(fontSize + 2); // Slightly larger for title
        // sourceTextView.setTextSize(fontSize - 2); // Slightly smaller for source
    }

    private void showFontSizeDialog() {
        android.util.Log.d("ArticleDetail", "showFontSizeDialog called");
        try {
            if (fontSizeDialog == null) {
                android.util.Log.d("ArticleDetail", "Creating new FontSizeDialog");
                fontSizeDialog = new FontSizeDialog(this, new FontSizeDialog.FontSizeCallback() {
                    @Override
                    public void onFontSizeApplied(int fontSize) {
                        android.util.Log.d("ArticleDetail", "Font size applied: " + fontSize);
                        // Apply the new font size
                        applyFontSize();
                    }
                });
            }
            android.util.Log.d("ArticleDetail", "Showing FontSizeDialog");
            fontSizeDialog.show();
        } catch (Exception e) {
            android.util.Log.e("ArticleDetail", "Error showing font size dialog", e);
        }
    }
}