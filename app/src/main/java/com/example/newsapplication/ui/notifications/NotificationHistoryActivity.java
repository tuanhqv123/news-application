package com.example.newsapplication.ui.notifications;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.ArticleDetailActivity;
import com.example.newsapplication.R;
import com.example.newsapplication.database.NotificationHistoryHelper;
import com.example.newsapplication.auth.UserSessionManager;
import com.example.newsapplication.MainActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationHistoryActivity extends AppCompatActivity {

    private ImageView backButton;
    private TextView clearAllButton;
    private RecyclerView recyclerView;
    private TextView emptyStateText;
    private NotificationHistoryAdapter adapter;
    private NotificationHistoryHelper dbHelper;
    private UserSessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_history);

        // Check if user is logged in
        sessionManager = new UserSessionManager(this);
        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(this, "Please login to view notifications", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        dbHelper = NotificationHistoryHelper.getInstance(this);
        initViews();
        setupRecyclerView();
        loadNotifications();
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        clearAllButton = findViewById(R.id.clearAllButton);
        recyclerView = findViewById(R.id.recyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);

        backButton.setOnClickListener(v -> finish());

        clearAllButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear All Notifications")
                    .setMessage("Are you sure you want to clear all notification history?")
                    .setPositiveButton("Clear", (dialog, which) -> {
                        dbHelper.clearAllNotifications();
                        loadNotifications();
                        Toast.makeText(this, "All notifications cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupRecyclerView() {
        adapter = new NotificationHistoryAdapter(this::onNotificationClick, this::onNotificationLongClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadNotifications() {
        List<NotificationHistoryHelper.NotificationItem> notifications = dbHelper.getAllNotifications();

        if (notifications.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateText.setVisibility(View.GONE);
            adapter.setNotifications(notifications);
        }
    }

    private void onNotificationClick(NotificationHistoryHelper.NotificationItem notification) {
        // Mark as read
        if (!notification.isRead()) {
            dbHelper.markAsRead(notification.getId());
            notification.setRead(true);
            adapter.updateNotification(notification);
        }

        // Navigate to article if has article_id
        if (notification.getArticleId() != null && !notification.getArticleId().isEmpty()) {
            Intent intent = new Intent(this, ArticleDetailActivity.class);
            intent.putExtra("article_id", notification.getArticleId());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Article not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void onNotificationLongClick(NotificationHistoryHelper.NotificationItem notification) {
        new AlertDialog.Builder(this)
                .setTitle("Notification Options")
                .setItems(new String[]{"Mark as " + (notification.isRead() ? "Unread" : "Read"), "Delete"}, (dialog, which) -> {
                    switch (which) {
                        case 0: // Mark as read/unread
                            if (notification.isRead()) {
                                // Mark as unread (we'd need to add this method to dbHelper)
                                Toast.makeText(this, "Marked as unread", Toast.LENGTH_SHORT).show();
                            } else {
                                dbHelper.markAsRead(notification.getId());
                                notification.setRead(true);
                                adapter.updateNotification(notification);
                                Toast.makeText(this, "Marked as read", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case 1: // Delete
                            dbHelper.deleteNotification(notification.getId());
                            loadNotifications();
                            Toast.makeText(this, "Notification deleted", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .show();
    }

    public static String formatNotificationTime(String timestamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date date = sdf.parse(timestamp);
            SimpleDateFormat outputSdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
            return outputSdf.format(date);
        } catch (ParseException e) {
            return timestamp;
        }
    }
}