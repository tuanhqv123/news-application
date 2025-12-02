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
        private String description;

        public Category(int id, String name) {
            this.id = id;
            this.name = name;
            this.slug = name.toLowerCase().replace(" ", "-");
            this.description = "";
        }

        public Category(int id, String name, String slug) {
            this.id = id;
            this.name = name;
            this.slug = slug;
            this.description = "";
        }

        public Category(int id, String name, String slug, String description) {
            this.id = id;
            this.name = name;
            this.slug = slug;
            this.description = description;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public String getSlug() { return slug; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
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
        private final TextView descriptionTextView;
        private final ImageView arrowIcon;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.categoryNameTextView);
            descriptionTextView = itemView.findViewById(R.id.categoryDescriptionTextView);
            arrowIcon = itemView.findViewById(R.id.arrowIcon);
        }

        public void bind(Category category, OnCategoryClickListener listener, int position) {
            nameTextView.setText(category.getName());
            
            // Show description if available, otherwise show default text
            String description = category.getDescription();
            if (description != null && !description.isEmpty() && !description.equals("null")) {
                descriptionTextView.setText(description);
            } else {
                descriptionTextView.setText("Browse " + category.getName().toLowerCase() + " articles");
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category, position);
                }
            });
        }
    }
}
