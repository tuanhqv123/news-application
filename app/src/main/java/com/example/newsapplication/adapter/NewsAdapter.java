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
import com.example.newsapplication.utils.DateUtils;
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
        
        // Set category
        if (holder.categoryTextView != null && article.getCategory() != null) {
            holder.categoryTextView.setText(article.getCategory());
        }
        
        // Set date only (no category, no dot separator)
        if (holder.categoryDateTextView != null) {
            String dateStr = article.getDate();
            String formattedDate = "";
            if (dateStr != null && !dateStr.isEmpty()) {
                formattedDate = DateUtils.formatToShortMonthDay(dateStr);
                if (formattedDate.isEmpty()) {
                    formattedDate = dateStr;
                }
            }
            
            holder.categoryDateTextView.setText(formattedDate);
        }
        
        // Hide source and date (using combined field instead)
        if (holder.sourceTextView != null) {
            holder.sourceTextView.setVisibility(View.GONE);
        }
        if (holder.dateTextView != null) {
            holder.dateTextView.setVisibility(View.GONE);
        }
        
        // Set summary if available
        if (holder.summaryTextView != null) {
            String summary = article.getSummary();
            if (summary != null && !summary.isEmpty()) {
                holder.summaryTextView.setText(summary);
                holder.summaryTextView.setVisibility(View.VISIBLE);
            } else {
                holder.summaryTextView.setVisibility(View.GONE);
            }
        }

        // Load image from URL using Picasso
        if (holder.imageView != null) {
            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                // Load image without placeholder to avoid blue/green indicator
                com.squareup.picasso.Picasso.get()
                    .load(article.getImageUrl())
                    .noPlaceholder()
                    .error(R.drawable.placeholder_image)
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
            } else {
                holder.imageView.setImageResource(R.drawable.placeholder_image);
            }
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
        TextView summaryTextView;
        TextView categoryDateTextView;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
            summaryTextView = itemView.findViewById(R.id.summaryTextView);
            categoryDateTextView = itemView.findViewById(R.id.categoryDateTextView);
        }
    }
}