package com.example.newsapplication;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
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
import java.util.Locale;

public class ArticleDetailActivity extends AppCompatActivity {

    private ImageView backImageView;
    private ImageView articleImageView;
    private ImageView bookmarkImageView;
    private ImageView shareImageView;
    private ImageView audioPlayImageView;
    private View audioControlBar;
    private ImageView audioControlPlayPause;
    private TextView audioStatusTextView;
    private TextView audioCurrentTimeTextView;
    private TextView audioDurationTextView;
    private SeekBar audioProgressSeekBar;
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
    
    // Audio player
    private MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private static final String AUDIO_BASE_URL = "https://byvkcpdtprodvhadpdix.supabase.co/storage/v1/object/public/audio_articles/";
    private Handler progressHandler;
    private Runnable progressRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail);

        // Initialize font size manager
        fontSizeManager = new FontSizeManager(this);
        
        // Initialize repository and session manager
        newsRepository = new NewsRepository(this);
        sessionManager = new UserSessionManager(this);
        progressHandler = new Handler(Looper.getMainLooper());

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
        audioPlayImageView = findViewById(R.id.audioPlayImageView);
        audioControlBar = findViewById(R.id.audioControlBar);
        audioControlPlayPause = findViewById(R.id.audioControlPlayPause);
        audioStatusTextView = findViewById(R.id.audioStatusTextView);
        audioCurrentTimeTextView = findViewById(R.id.audioCurrentTimeTextView);
        audioDurationTextView = findViewById(R.id.audioDurationTextView);
        audioProgressSeekBar = findViewById(R.id.audioProgressSeekBar);
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
                
                // Setup audio player if article has ID
                setupAudioPlayer();
            }
        }
    }
    
    private void setupAudioPlayer() {
        if (currentArticle == null || currentArticle.getId() == null || currentArticle.getId().isEmpty()) {
            audioPlayImageView.setVisibility(View.GONE);
            audioControlBar.setVisibility(View.GONE);
            return;
        }
        
        // Chỉ hiển thị nút audio trên top bar, thanh control sẽ hiện khi user bấm play
        audioPlayImageView.setVisibility(View.VISIBLE);
        audioControlBar.setVisibility(View.GONE);
        audioPlayImageView.setImageResource(R.drawable.ic_play_circle);
        audioControlPlayPause.setImageResource(R.drawable.ic_play_circle);
        audioStatusTextView.setText(currentArticle.getTitle() != null ? currentArticle.getTitle() : "Listen to article");
        audioCurrentTimeTextView.setText("00:00");
        audioDurationTextView.setText("00:00");
        audioProgressSeekBar.setProgress(0);
        isPlaying = false;
        updateAudioIcons();
        
        // Setup click listener
        audioPlayImageView.setOnClickListener(v -> toggleAudioPlayback());
        audioControlPlayPause.setOnClickListener(v -> toggleAudioPlayback());
        setupSeekBarListener();
    }
    
    private void setupSeekBarListener() {
        audioProgressSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioCurrentTimeTextView.setText(formatMillis(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (mediaPlayer != null) {
                    mediaPlayer.seekTo(seekBar.getProgress());
                }
            }
        });
    }
    
    private void updateAudioIcons() {
        int icon = isPlaying ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle;
        audioPlayImageView.setImageResource(icon);
        audioControlPlayPause.setImageResource(icon);
    }
    
    private void startProgressUpdates() {
        if (progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
        }
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int position = mediaPlayer.getCurrentPosition();
                    audioProgressSeekBar.setProgress(position);
                    audioCurrentTimeTextView.setText(formatMillis(position));
                    progressHandler.postDelayed(this, 500);
                }
            }
        };
        progressHandler.post(progressRunnable);
    }
    
    private void stopProgressUpdates() {
        if (progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
            progressRunnable = null;
        }
    }
    
    private String formatMillis(int millis) {
        if (millis <= 0) {
            return "00:00";
        }
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
    
    private String getAudioUrl() {
        if (currentArticle == null || currentArticle.getId() == null) {
            return null;
        }
        return AUDIO_BASE_URL + currentArticle.getId() + ".mp3";
    }
    
    private void toggleAudioPlayback() {
        if (isPlaying) {
            pauseAudio();
        } else {
            playAudio();
        }
    }
    
    private void playAudio() {
        String audioUrl = getAudioUrl();
        if (audioUrl == null) {
            Toast.makeText(this, "Audio not available", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(audioUrl);
                mediaPlayer.prepareAsync();
                
                mediaPlayer.setOnPreparedListener(mp -> {
                    // Lần đầu play: hiện thanh control
                    if (audioControlBar.getVisibility() != View.VISIBLE) {
                        audioControlBar.setVisibility(View.VISIBLE);
                    }
                    int duration = mp.getDuration();
                    audioProgressSeekBar.setMax(duration);
                    audioDurationTextView.setText(formatMillis(duration));
                    mp.start();
                    isPlaying = true;
                    updateAudioIcons();
                    startProgressUpdates();
                });
                
                mediaPlayer.setOnCompletionListener(mp -> {
                    isPlaying = false;
                    stopProgressUpdates();
                    audioProgressSeekBar.setProgress(0);
                    audioCurrentTimeTextView.setText("00:00");
                    updateAudioIcons();
                    releaseMediaPlayer();
                });
                
                mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                    Toast.makeText(ArticleDetailActivity.this, "Error playing audio", Toast.LENGTH_SHORT).show();
                    isPlaying = false;
                    stopProgressUpdates();
                    audioProgressSeekBar.setProgress(0);
                    audioCurrentTimeTextView.setText("00:00");
                    updateAudioIcons();
                    releaseMediaPlayer();
                    return true;
                });
            } else {
                mediaPlayer.start();
                isPlaying = true;
                updateAudioIcons();
                startProgressUpdates();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading audio", Toast.LENGTH_SHORT).show();
            releaseMediaPlayer();
        }
    }
    
    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            updateAudioIcons();
            stopProgressUpdates();
        }
    }
    
    private void releaseMediaPlayer() {
        if (mediaPlayer != null) {
            stopProgressUpdates();
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mediaPlayer = null;
            isPlaying = false;
            updateAudioIcons();
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
                                    currentArticle.setBookmarked(false);
                                    updateBookmarkIcon();
                                    Toast.makeText(ArticleDetailActivity.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
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
                                currentArticle.setBookmarked(true);
                                updateBookmarkIcon();
                                Toast.makeText(ArticleDetailActivity.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
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

        fontSizeIcon.setOnClickListener(v -> {
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
            showNoComments();
            return;
        }
        
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
                        showNoComments();
                    }
                } else {
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
            return;
        }
        
        commentEditText.setEnabled(false);
        sendCommentButton.setEnabled(false);
        
        newsRepository.addComment(articleId, commentText, new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                commentEditText.setEnabled(true);
                sendCommentButton.setEnabled(true);
                
                if (response.isSuccess()) {
                    commentEditText.setText("");
                    hideKeyboard();
                    Toast.makeText(ArticleDetailActivity.this, "Comment posted", Toast.LENGTH_SHORT).show();
                    
                    try {
                        JSONObject data = response.getData();
                        Comment newComment = Comment.fromJson(data);
                        
                        if (newComment.getContent() == null || newComment.getContent().isEmpty()) {
                            newComment = new Comment();
                            newComment.setContent(commentText);
                            newComment.setUserName(sessionManager.getUserName());
                            newComment.setCreatedAt(java.time.Instant.now().toString());
                        }
                        
                        commentsAdapter.addComment(newComment);
                        
                        int count = commentsAdapter.getItemCount();
                        commentCountTextView.setText("(" + count + ")");
                        
                        noCommentsTextView.setVisibility(View.GONE);
                        commentsRecyclerView.setVisibility(View.VISIBLE);
                        
                    } catch (Exception e) {
                        loadComments();
                    }
                } else {
                    Toast.makeText(ArticleDetailActivity.this, response.getErrorMessage(), Toast.LENGTH_SHORT).show();
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
        try {
            if (fontSizeDialog == null) {
                fontSizeDialog = new FontSizeDialog(this, new FontSizeDialog.FontSizeCallback() {
                    @Override
                    public void onFontSizeApplied(int fontSize) {
                        applyFontSize();
                    }
                });
            }
            fontSizeDialog.show();
        } catch (Exception e) {
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Pause audio when activity is paused
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            pauseAudio();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Release MediaPlayer when activity is destroyed
        releaseMediaPlayer();
    }
}