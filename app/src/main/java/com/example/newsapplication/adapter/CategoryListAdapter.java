package com.example.newsapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying categories in a vertical list.
 */
public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    public static class Category {
        private int id;
        private String name;
        private String slug;

        public Category(int id, String name) {
            this.id = id;
            this.name = name;
            this.slug = name.toLowerCase().replace(" ", "-");
        }

        public Category(int id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSlug() { return slug; }
    }

    public CategoryListAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories != null ? categories : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_list, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, listener, position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView nameTextView;
        private final TextView articleCountTextView;
        private final ImageView categoryIcon;
        private final ImageView arrowIcon;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.categoryNameTextView);
            articleCountTextView = itemView.findViewById(R.id.categoryArticleCount);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
        }

        public void bind(Category category, OnCategoryClickListener listener, int position) {
            nameTextView.setText(category.getName());
            articleCountTextView.setText("View articles");
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category, position);
                }
            });
        }
    }
}
