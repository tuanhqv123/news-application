package com.example.newsapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;
import com.example.newsapplication.model.Article;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdminArticlesAdapter extends RecyclerView.Adapter<AdminArticlesAdapter.ArticleViewHolder> {

    private Context context;
    private List<Article> articles;
    private ArticleActionListener listener;

    public interface ArticleActionListener {
        void onArticleClick(Article article);
    }

    public AdminArticlesAdapter(Context context, List<Article> articles, ArticleActionListener listener) {
        this.context = context;
        this.articles = articles;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        Article article = articles.get(position);

        holder.articleTitle.setText(article.getTitle());
        holder.articleAuthor.setText(article.getAuthor() != null ? article.getAuthor() : "Unknown");
        holder.articleDate.setText(formatDate(article.getDate()));

        String channelName = article.getChannelName();
        if (channelName != null && !channelName.isEmpty()) {
            holder.channelName.setText(channelName);
            holder.channelName.setVisibility(View.VISIBLE);
        } else {
            holder.channelName.setText("No channel");
            holder.channelName.setVisibility(View.VISIBLE);
        }

        String status = article.getStatus();
        if (status == null) status = "draft";
        holder.statusBadge.setText(status.toUpperCase().replace("_", " "));

        switch (status.toLowerCase()) {
            case "published":
                holder.statusBadge.setBackgroundResource(R.drawable.badge_published);
                break;
            case "pending_review":
                holder.statusBadge.setBackgroundResource(R.drawable.badge_pending);
                break;
            case "rejected":
                holder.statusBadge.setBackgroundResource(R.drawable.badge_rejected);
                break;
            default:
                holder.statusBadge.setBackgroundResource(R.drawable.badge_draft);
                break;
        }

        if (article.getImageUrl() != null && !article.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(article.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.articleImage);
        } else {
            holder.articleImage.setImageResource(R.drawable.ic_launcher_foreground);
        }

        holder.articleCard.setOnClickListener(v -> listener.onArticleClick(article));
    }

    private String formatDate(String date) {
        if (date == null) return "";
        if (date.length() > 10) return date.substring(0, 10);
        return date;
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public void updateArticles(List<Article> newArticles) {
        this.articles = newArticles;
        notifyDataSetChanged();
    }

    static class ArticleViewHolder extends RecyclerView.ViewHolder {
        CardView articleCard;
        ImageView articleImage;
        TextView articleTitle;
        TextView articleAuthor;
        TextView statusBadge;
        TextView articleDate;
        TextView channelName;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            articleCard = itemView.findViewById(R.id.articleCard);
            articleImage = itemView.findViewById(R.id.articleImage);
            articleTitle = itemView.findViewById(R.id.articleTitle);
            articleAuthor = itemView.findViewById(R.id.articleAuthor);
            statusBadge = itemView.findViewById(R.id.statusBadge);
            articleDate = itemView.findViewById(R.id.articleDate);
            channelName = itemView.findViewById(R.id.channelName);
        }
    }
}
