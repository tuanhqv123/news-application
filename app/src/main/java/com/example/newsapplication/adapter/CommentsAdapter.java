package com.example.newsapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;
import com.example.newsapplication.model.Comment;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying comments in a RecyclerView.
 */
public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentViewHolder> {

    private List<Comment> comments;

    public CommentsAdapter() {
        this.comments = new ArrayList<>();
    }

    public CommentsAdapter(List<Comment> comments) {
        this.comments = comments != null ? comments : new ArrayList<>();
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments != null ? comments : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addComment(Comment comment) {
        this.comments.add(0, comment); // Add to beginning
        notifyItemInserted(0);
    }

    public void clearComments() {
        this.comments.clear();
        notifyDataSetChanged();
    }

    static class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ImageView avatarImageView;
        private final TextView userNameTextView;
        private final TextView timeTextView;
        private final TextView contentTextView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.avatarImageView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
        }

        public void bind(Comment comment) {
            // Set user name
            userNameTextView.setText(comment.getUserName());
            
            // Set time
            timeTextView.setText(comment.getTimeAgo());
            
            // Set content
            contentTextView.setText(comment.getContent());
            
            // Load avatar
            String avatarUrl = comment.getUserAvatar();
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                Picasso.get()
                        .load(avatarUrl)
                        .noPlaceholder()
                        .error(R.drawable.default_avatar)
                        .into(avatarImageView);
            } else {
                avatarImageView.setImageResource(R.drawable.default_avatar);
            }
        }
    }
}
