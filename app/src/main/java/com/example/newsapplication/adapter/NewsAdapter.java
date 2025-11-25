package com.example.newsapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
                .inflate(R.layout.item_news_card, parent, false);
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
        if (holder.sourceTextView != null && article.getSource() != null) {
            holder.sourceTextView.setText(article.getSource());
        }
        // Date is already included in sourceTextView, so we don't set dateTextView separately
        if (holder.categoryTextView != null && article.getCategory() != null) {
            holder.categoryTextView.setText(article.getCategory());
        }

        if (holder.imageView != null) {
            if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
                Glide.with(holder.imageView.getContext())
                        .load(article.getImageUrl())
                        .placeholder(article.getImageResId() != 0 ? article.getImageResId() : R.drawable.placeholder_image)
                        .error(R.drawable.error_image)
                        .into(holder.imageView);
            } else if (article.getImageResId() != 0) {
                holder.imageView.setImageResource(article.getImageResId());
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