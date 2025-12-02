package com.example.newsapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.adapter.CommentsAdapter;
import com.example.newsapplication.model.Article;
import com.example.newsapplication.model.Comment;
import com.example.newsapplication.ui.FontSizeDialog;
import com.example.newsapplication.utils.FontSizeManager;
import com.example.newsapplication.utils.DateUtils;
import com.example.newsapplication.repository.NewsRepository;
import com.example.newsapplication.auth.UserSessionManager;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ArticleDetailActivity extends AppCompatActivity {

    private static final String TAG = "ArticleDetailActivity";

    private ImageView backImageView;
    private ImageView articleImageView;
    private ImageView bookmarkImageView;
    private ImageView shareImageView;
    private TextView titleTextView;
    private TextView sourceTextView;
    private TextView dateTextView;
    private TextView categoryTextView;
    private TextView summaryTextView;
    private TextView contentTextView;
    private WebView contentWebView;

    // Font size controls
    private TextView fontSizeIcon;
    private FontSizeDialog fontSizeDialog;

    // Comments section
    private TextView commentsHeaderTextView;
    private TextView commentCountTextView;
    private ProgressBar commentsProgressBar;
    private TextView noCommentsTextView;
    private RecyclerView commentsRecyclerView;
    private EditText commentEditText;
    private ImageView sendCommentButton;
    private CommentsAdapter commentsAdapter;

    private Article currentArticle;
    private FontSizeManager fontSizeManager;
    private NewsRepository newsRepository;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        // Initialize font size manager
        fontSizeManager = new FontSizeManager(this);
        
        // Initialize repository and session manager
        newsRepository = new NewsRepository(this);
        sessionManager = new UserSessionManager(this);

        initViews();
        setupCommentsRecyclerView();
        setupData();
        setupClickListeners();
        applyFontSize();
        loadComments();
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
        summaryTextView = findViewById(R.id.summaryTextView);
        contentTextView = findViewById(R.id.contentTextView);
        contentWebView = findViewById(R.id.contentWebView);
        
        // Setup WebView
        setupWebView();
        
        // Comments views
        commentsHeaderTextView = findViewById(R.id.commentsHeaderTextView);
        commentCountTextView = findViewById(R.id.commentCountTextView);
        commentsProgressBar = findViewById(R.id.commentsProgressBar);
        noCommentsTextView = findViewById(R.id.noCommentsTextView);
        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentEditText = findViewById(R.id.commentEditText);
        sendCommentButton = findViewById(R.id.sendCommentButton);
    }
    
    private void setupWebView() {
        if (contentWebView != null) {
            WebSettings webSettings = contentWebView.getSettings();
            webSettings.setJavaScriptEnabled(false);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setUseWideViewPort(true);
            contentWebView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
        }
    }

    private void setupCommentsRecyclerView() {
        commentsAdapter = new CommentsAdapter();
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentsAdapter);
        commentsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupData() {
        // Get article from intent
        if (getIntent().hasExtra("article")) {
            currentArticle = (Article) getIntent().getSerializableExtra("article");

            if (currentArticle != null) {
                titleTextView.setText(currentArticle.getTitle());
                
                // Show channel name and formatted date
                categoryTextView.setVisibility(View.GONE);
                
                // Build source text: "Channel Name • Formatted Date"
                StringBuilder sourceBuilder = new StringBuilder();
                String channelName = currentArticle.getChannelName();
                if (channelName != null && !channelName.isEmpty()) {
                    sourceBuilder.append(channelName);
                }
                
                // Format and add published date using DateUtils
                String publishedAt = currentArticle.getPublishedAt();
                if (publishedAt == null || publishedAt.isEmpty()) {
                    publishedAt = currentArticle.getDate();
                }
                String formattedDate = DateUtils.formatToFullDate(publishedAt);
                if (!formattedDate.isEmpty()) {
                    if (sourceBuilder.length() > 0) {
                        sourceBuilder.append(" • ");
                    }
                    sourceBuilder.append(formattedDate);
                }
                
                sourceTextView.setText(sourceBuilder.toString());
                dateTextView.setVisibility(View.GONE);
                
                // Show summary
                String summary = currentArticle.getDescription();
                if (summary != null && !summary.isEmpty()) {
                    summaryTextView.setText(summary);
                    summaryTextView.setVisibility(View.VISIBLE);
                } else {
                    summaryTextView.setVisibility(View.GONE);
                }
                
                // Display content - check if it's HTML
                String content = currentArticle.getContent();
                if (content != null && !content.isEmpty()) {
                    if (isHtmlContent(content)) {
                        // Use WebView for HTML content
                        contentWebView.setVisibility(View.VISIBLE);
                        contentTextView.setVisibility(View.GONE);
                        loadHtmlContent(content);
                    } else {
                        // Use TextView for plain text
                        contentWebView.setVisibility(View.GONE);
                        contentTextView.setVisibility(View.VISIBLE);
                        contentTextView.setText(content);
                    }
                }

                // Load article image from URL using Picasso
                try {
                    if (currentArticle.getImageUrl() != null && !currentArticle.getImageUrl().isEmpty()) {
                        com.squareup.picasso.Picasso.get()
                            .load(currentArticle.getImageUrl())
                            .noPlaceholder()
                            .error(R.drawable.placeholder_image)
                            .fit()
                            .centerCrop()
                            .into(articleImageView);
                    } else {
                        articleImageView.setImageResource(R.drawable.placeholder_image);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    articleImageView.setImageResource(R.drawable.placeholder_image);
                }

                // Update bookmark icon
                updateBookmarkIcon();
            }
        }
    }
    
    private boolean isHtmlContent(String content) {
        return content != null && (content.contains("<") && content.contains(">"));
    }
    
    private void loadHtmlContent(String htmlContent) {
        int fontSize = (int) fontSizeManager.getFontSize();
        loadHtmlContentWithFontSize(htmlContent, fontSize);
    }
    
    private void loadHtmlContentWithFontSize(String htmlContent, int fontSize) {
        // Wrap HTML content with proper styling using dynamic font size
        String styledHtml = "<!DOCTYPE html><html><head>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<style>" +
                "body { font-family: sans-serif; font-size: " + fontSize + "px; line-height: 1.6; color: #333; margin: 0; padding: 0; }" +
                "img { max-width: 100%; height: auto; border-radius: 8px; margin: 12px 0; }" +
                "p { margin: 12px 0; }" +
                "a { color: #0866FF; text-decoration: none; }" +
                ".Normal { margin: 12px 0; }" +
                "video, iframe { max-width: 100%; }" +
                ".box_embed_video_parent { margin: 16px 0; }" +
                ".box_img_video img { width: 100%; border-radius: 8px; }" +
                "</style></head><body>" +
                htmlContent +
                "</body></html>";
        
        contentWebView.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null);
    }
    
    private void setupClickListeners() {
        backImageView.setOnClickListener(v -> finish());

        bookmarkImageView.setOnClickListener(v -> {
            if (currentArticle != null) {
                // Check if user is logged in
                if (sessionManager == null || !sessionManager.isLoggedIn()) {
                    Toast.makeText(this, "Please login to bookmark articles", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                boolean isBookmarked = currentArticle.isBookmarked();
                currentArticle.setBookmarked(!isBookmarked);
                
                // Call bookmark API
                if (!isBookmarked) {
                    // Add bookmark
                    newsRepository.bookmarkArticle(currentArticle.getId(), new NewsRepository.RepositoryCallback<JSONObject>() {
                        @Override
                        public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                            if (response.isSuccess()) {
                                updateBookmarkIcon();
                                Toast.makeText(ArticleDetailActivity.this, "Article bookmarked", Toast.LENGTH_SHORT).show();
                            } else {
                                // Check for duplicate bookmark error (400)
                                if (response.getStatusCode() == 400 || (response.getErrorMessage() != null && response.getErrorMessage().contains("duplicate"))) {
                                    // Treat as success since it's already bookmarked
                                    updateBookmarkIcon();
                                    Toast.makeText(ArticleDetailActivity.this, "Article already bookmarked", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Revert if API call fails
                                    currentArticle.setBookmarked(false);
                                    updateBookmarkIcon();
                                    android.util.Log.e(TAG, "Failed to bookmark article: " + response.getErrorMessage());
                                    Toast.makeText(ArticleDetailActivity.this, "Failed to bookmark article", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                } else {
                    // Remove bookmark
                    newsRepository.removeBookmark(currentArticle.getId(), new NewsRepository.RepositoryCallback<JSONObject>() {
                        @Override
                        public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                            if (response.isSuccess()) {
                                updateBookmarkIcon();
                                Toast.makeText(ArticleDetailActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                            } else {
                                // Revert if API call fails
                                currentArticle.setBookmarked(true);
                                updateBookmarkIcon();
                                android.util.Log.e(TAG, "Failed to remove bookmark: " + response.getErrorMessage());
                                Toast.makeText(ArticleDetailActivity.this, "Failed to remove bookmark", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
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
            android.util.Log.d(TAG, "Font size icon clicked");
            showFontSizeDialog();
        });

        // Comment input listeners
        sendCommentButton.setOnClickListener(v -> submitComment());
        
        commentEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                submitComment();
                return true;
            }
            return false;
        });
    }

    private void updateBookmarkIcon() {
        if (currentArticle != null && currentArticle.isBookmarked()) {
            bookmarkImageView.setImageResource(R.drawable.ic_bookmark_filled);
        } else {
            bookmarkImageView.setImageResource(R.drawable.ic_bookmark_outline);
        }
    }

    private void loadComments() {
        if (currentArticle == null) return;
        
        String articleId = currentArticle.getId();
        if (articleId == null || articleId.isEmpty()) {
            android.util.Log.w(TAG, "No article ID, skipping comments load");
            showNoComments();
            return;
        }
        
        android.util.Log.d(TAG, "Loading comments for article: " + articleId);
        showCommentsLoading();
        
        newsRepository.getComments(articleId, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                hideCommentsLoading();
                
                if (response.isSuccess()) {
                    try {
                        JSONObject data = response.getData();
                        List<Comment> comments = parseComments(data);
                        
                        if (comments.isEmpty()) {
                            showNoComments();
                        } else {
                            showComments(comments);
                        }
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error parsing comments", e);
                        showNoComments();
                    }
                } else {
                    android.util.Log.e(TAG, "Failed to load comments: " + response.getErrorMessage());
                    showNoComments();
                }
            }
        });
    }

    private List<Comment> parseComments(JSONObject data) {
        List<Comment> comments = new ArrayList<>();
        try {
            JSONArray commentsArray = null;
            
            // Check different possible structures
            if (data.has("comments")) {
                commentsArray = data.getJSONArray("comments");
            } else if (data.has("data")) {
                Object dataObj = data.get("data");
                if (dataObj instanceof JSONArray) {
                    commentsArray = (JSONArray) dataObj;
                } else if (dataObj instanceof JSONObject) {
                    JSONObject dataObject = (JSONObject) dataObj;
                    if (dataObject.has("comments")) {
                        commentsArray = dataObject.getJSONArray("comments");
                    }
                }
            } else if (data.has("items")) {
                commentsArray = data.getJSONArray("items");
            }
            
            if (commentsArray != null) {
                for (int i = 0; i < commentsArray.length(); i++) {
                    JSONObject commentJson = commentsArray.getJSONObject(i);
                    Comment comment = Comment.fromJson(commentJson);
                    comments.add(comment);
                }
            }
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error parsing comments array", e);
        }
        return comments;
    }

    private void showCommentsLoading() {
        commentsProgressBar.setVisibility(View.VISIBLE);
        noCommentsTextView.setVisibility(View.GONE);
        commentsRecyclerView.setVisibility(View.GONE);
    }

    private void hideCommentsLoading() {
        commentsProgressBar.setVisibility(View.GONE);
    }

    private void showNoComments() {
        noCommentsTextView.setVisibility(View.VISIBLE);
        commentsRecyclerView.setVisibility(View.GONE);
        commentCountTextView.setText("(0)");
    }

    private void showComments(List<Comment> comments) {
        noCommentsTextView.setVisibility(View.GONE);
        commentsRecyclerView.setVisibility(View.VISIBLE);
        commentsAdapter.setComments(comments);
        commentCountTextView.setText("(" + comments.size() + ")");
    }

    private void submitComment() {
        String commentText = commentEditText.getText().toString().trim();
        
        if (commentText.isEmpty()) {
            Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (sessionManager == null || !sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please login to comment", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentArticle == null) return;
        
        String articleId = currentArticle.getId();
        if (articleId == null || articleId.isEmpty()) {
            Toast.makeText(this, "Unable to post comment", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Disable input while posting
        commentEditText.setEnabled(false);
        sendCommentButton.setEnabled(false);
        
        android.util.Log.d(TAG, "Posting comment to article: " + articleId);
        
        newsRepository.addComment(articleId, commentText, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                // Re-enable input
                commentEditText.setEnabled(true);
                sendCommentButton.setEnabled(true);
                
                if (response.isSuccess()) {
                    // Clear input
                    commentEditText.setText("");
                    
                    // Hide keyboard
                    hideKeyboard();
                    
                    Toast.makeText(ArticleDetailActivity.this, "Comment posted", Toast.LENGTH_SHORT).show();
                    
                    // Create a new comment object for immediate display
                    try {
                        JSONObject data = response.getData();
                        Comment newComment = Comment.fromJson(data);
                        
                        // If no valid comment from response, create a temp one
                        if (newComment.getContent() == null || newComment.getContent().isEmpty()) {
                            newComment = new Comment();
                            newComment.setContent(commentText);
                            newComment.setUserName(sessionManager.getUserName());
                            newComment.setCreatedAt(java.time.Instant.now().toString());
                        }
                        
                        // Add to adapter
                        commentsAdapter.addComment(newComment);
                        
                        // Update count
                        int count = commentsAdapter.getItemCount();
                        commentCountTextView.setText("(" + count + ")");
                        
                        // Show comments list
                        noCommentsTextView.setVisibility(View.GONE);
                        commentsRecyclerView.setVisibility(View.VISIBLE);
                        
                    } catch (Exception e) {
                        android.util.Log.e(TAG, "Error creating comment from response", e);
                        // Reload all comments on error
                        loadComments();
                    }
                } else {
                    android.util.Log.e(TAG, "Failed to post comment: " + response.getErrorMessage());
                    Toast.makeText(ArticleDetailActivity.this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
    
    private void applyFontSize() {
        float fontSize = fontSizeManager.getFontSize();

        // Apply font size to plain text content
        contentTextView.setTextSize(fontSize);
        
        // Apply font size to WebView HTML content
        if (currentArticle != null && contentWebView.getVisibility() == View.VISIBLE) {
            String content = currentArticle.getContent();
            if (content != null && !content.isEmpty() && isHtmlContent(content)) {
                loadHtmlContentWithFontSize(content, (int) fontSize);
            }
        }
    }

    private void showFontSizeDialog() {
        android.util.Log.d(TAG, "showFontSizeDialog called");
        try {
            if (fontSizeDialog == null) {
                android.util.Log.d(TAG, "Creating new FontSizeDialog");
                fontSizeDialog = new FontSizeDialog(this, new FontSizeDialog.FontSizeCallback() {
                    @Override
                    public void onFontSizeApplied(int fontSize) {
                        android.util.Log.d(TAG, "Font size applied: " + fontSize);
                        // Apply the new font size
                        applyFontSize();
                    }
                });
            }
            android.util.Log.d(TAG, "Showing FontSizeDialog");
            fontSizeDialog.show();
        } catch (Exception e) {
            android.util.Log.e(TAG, "Error showing font size dialog", e);
        }
    }
}