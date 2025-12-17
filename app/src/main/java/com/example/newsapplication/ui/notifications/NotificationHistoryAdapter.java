package com.example.newsapplication.ui.notifications;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;
import com.example.newsapplication.database.NotificationHistoryHelper;

import java.util.ArrayList;
import java.util.List;

public class NotificationHistoryAdapter extends RecyclerView.Adapter<NotificationHistoryAdapter.ViewHolder> {

    private List<NotificationHistoryHelper.NotificationItem> notifications;
    private final OnNotificationClickListener clickListener;
    private final OnNotificationLongClickListener longClickListener;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationHistoryHelper.NotificationItem notification);
    }

    public interface OnNotificationLongClickListener {
        void onNotificationLongClick(NotificationHistoryHelper.NotificationItem notification);
    }

    public NotificationHistoryAdapter(OnNotificationClickListener clickListener,
                                    OnNotificationLongClickListener longClickListener) {
        this.notifications = new ArrayList<>();
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NotificationHistoryHelper.NotificationItem notification = notifications.get(position);

        holder.titleTextView.setText(notification.getTitle() != null ? notification.getTitle() : "No Title");
        holder.messageTextView.setText(notification.getMessage() != null ? notification.getMessage() : "");
        holder.timeTextView.setText(NotificationHistoryActivity.formatNotificationTime(notification.getCreatedAt()));

        // Set read/unread appearance - only change opacity if read
        if (notification.isRead()) {
            holder.itemView.setAlpha(0.5f); // Standard opacity for read notifications
        } else {
            holder.itemView.setAlpha(1.0f); // Unread notifications have full opacity
        }

        holder.itemView.setOnClickListener(v -> clickListener.onNotificationClick(notification));
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onNotificationLongClick(notification);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void setNotifications(List<NotificationHistoryHelper.NotificationItem> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public void updateNotification(NotificationHistoryHelper.NotificationItem notification) {
        int position = -1;
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getId() == notification.getId()) {
                position = i;
                break;
            }
        }
        if (position != -1) {
            notifications.set(position, notification);
            notifyItemChanged(position);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView messageTextView;
        TextView timeTextView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            messageTextView = itemView.findViewById(R.id.messageTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
        }
    }
}