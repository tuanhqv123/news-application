package com.example.newsapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;
import com.example.newsapplication.model.Article;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private List<Article> articles;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Article article);
        void onBookmarkClick(Article article, int position);
    }

    public NewsAdapter(List<Article> articles, OnItemClickListener listener) {
        this.articles = articles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_feed, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        if (position < 0 || position >= articles.size()) {
            return;
        }

        Article article = articles.get(position);
        if (article == null) {
            return;
        }

        if (holder.titleTextView != null && article.getTitle() != null) {
            holder.titleTextView.setText(article.getTitle());
        }
        
        if (holder.sourceTextView != null) {
            String sourceText = article.getSource() != null ? article.getSource() : "";
            if (article.getDate() != null && !article.getDate().isEmpty()) {
                if (!sourceText.isEmpty()) {
                    sourceText += " • ";
                }
                sourceText += article.getDate();
            }
            holder.sourceTextView.setText(sourceText);
        }

        if (holder.categoryTextView != null && article.getCategory() != null) {
            holder.categoryTextView.setText(article.getCategory());
        }

        // Load image from URL using Picasso
        if (holder.imageView != null) {
            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                // Load image from URL with proper configuration
                com.squareup.picasso.Picasso.get()
                    .load(article.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.ic_launcher_foreground)
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
            } else {
                // Set placeholder if no URL is available
                holder.imageView.setImageResource(R.drawable.placeholder_image);
            }
        }

        // Handle bookmark click
        if (holder.itemView.findViewById(R.id.bookmarkIcon) != null) {
            ImageView bookmarkIcon = holder.itemView.findViewById(R.id.bookmarkIcon);
            
            // Update bookmark icon state
            if (article.isBookmarked()) {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark_filled);
            } else {
                bookmarkIcon.setImageResource(R.drawable.ic_bookmark_outline);
            }
            
            bookmarkIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookmarkClick(article, holder.getAdapterPosition());
                }
            });
        }

        // Handle item click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(article);
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void updateArticles(List<Article> newArticles) {
        this.articles = newArticles;
        notifyDataSetChanged();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTextView;
        TextView sourceTextView;
        TextView dateTextView;
        TextView categoryTextView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
        }
    }
}