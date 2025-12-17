package com.example.newsapplication.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NotificationHistoryHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notification_history.db";
    private static final int DATABASE_VERSION = 1;

    // Table name
    private static final String TABLE_NOTIFICATIONS = "notifications";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_ARTICLE_ID = "article_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_CHANNEL_ID = "channel_id";
    private static final String COLUMN_SCREEN = "screen";
    private static final String COLUMN_DATA = "data"; // JSON string for extra data
    private static final String COLUMN_CREATED_AT = "created_at";
    private static final String COLUMN_IS_READ = "is_read";

    // Create table query
    private static final String CREATE_TABLE_NOTIFICATIONS = "CREATE TABLE " + TABLE_NOTIFICATIONS + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_ARTICLE_ID + " TEXT, "
            + COLUMN_TITLE + " TEXT, "
            + COLUMN_MESSAGE + " TEXT, "
            + COLUMN_TYPE + " TEXT, "
            + COLUMN_CHANNEL_ID + " TEXT, "
            + COLUMN_SCREEN + " TEXT, "
            + COLUMN_DATA + " TEXT, "
            + COLUMN_CREATED_AT + " TEXT, "
            + COLUMN_IS_READ + " INTEGER DEFAULT 0"
            + ")";

    private static NotificationHistoryHelper instance;

    public static synchronized NotificationHistoryHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationHistoryHelper(context.getApplicationContext());
        }
        return instance;
    }

    private NotificationHistoryHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOTIFICATIONS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        onCreate(db);
    }

    public long saveNotification(String articleId, String title, String message, String type,
                               String channelId, String screen, String data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_ARTICLE_ID, articleId);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_MESSAGE, message);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_CHANNEL_ID, channelId);
        values.put(COLUMN_SCREEN, screen);
        values.put(COLUMN_DATA, data);
        values.put(COLUMN_CREATED_AT, getCurrentTimestamp());
        values.put(COLUMN_IS_READ, 0);

        long id = db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();
        return id;
    }

    public List<NotificationItem> getAllNotifications() {
        List<NotificationItem> notifications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_NOTIFICATIONS +
                       " ORDER BY " + COLUMN_CREATED_AT + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                NotificationItem notification = new NotificationItem();
                notification.setId(cursor.getLong(0));
                notification.setArticleId(cursor.getString(1));
                notification.setTitle(cursor.getString(2));
                notification.setMessage(cursor.getString(3));
                notification.setType(cursor.getString(4));
                notification.setChannelId(cursor.getString(5));
                notification.setScreen(cursor.getString(6));
                notification.setData(cursor.getString(7));
                notification.setCreatedAt(cursor.getString(8));
                notification.setRead(cursor.getInt(9) == 1);
                notifications.add(notification);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return notifications;
    }

    public void markAsRead(long notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_READ, 1);

        db.update(TABLE_NOTIFICATIONS, values, COLUMN_ID + " = ?",
                new String[]{String.valueOf(notificationId)});
        db.close();
    }

    public void markAllAsRead() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_READ, 1);

        db.update(TABLE_NOTIFICATIONS, values, null, null);
        db.close();
    }

    public void deleteNotification(long notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTIFICATIONS, COLUMN_ID + " = ?",
                new String[]{String.valueOf(notificationId)});
        db.close();
    }

    public void clearAllNotifications() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTIFICATIONS, null, null);
        db.close();
    }

    public int getUnreadCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS +
                       " WHERE " + COLUMN_IS_READ + " = 0";

        Cursor cursor = db.rawQuery(query, null);
        int count = 0;

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }

        cursor.close();
        db.close();
        return count;
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // NotificationItem model class
    public static class NotificationItem {
        private long id;
        private String articleId;
        private String title;
        private String message;
        private String type;
        private String channelId;
        private String screen;
        private String data;
        private String createdAt;
        private boolean isRead;

        // Getters and Setters
        public long getId() { return id; }
        public void setId(long id) { this.id = id; }

        public String getArticleId() { return articleId; }
        public void setArticleId(String articleId) { this.articleId = articleId; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getChannelId() { return channelId; }
        public void setChannelId(String channelId) { this.channelId = channelId; }

        public String getScreen() { return screen; }
        public void setScreen(String screen) { this.screen = screen; }

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }

        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

        public boolean isRead() { return isRead; }
        public void setRead(boolean read) { isRead = read; }
    }
}