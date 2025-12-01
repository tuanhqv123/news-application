package com.example.newsapplication.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying category chips in a horizontal RecyclerView.
 */
public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categories;
    private int selectedPosition = -1;
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

    public CategoryAdapter() {
        this.categories = new ArrayList<>();
    }

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories != null ? categories : new ArrayList<>();
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_chip, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        boolean isSelected = position == selectedPosition;
        holder.bind(category, isSelected, listener, position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        selectedPosition = -1;
        notifyDataSetChanged();
    }

    public void setListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        if (oldPosition >= 0) {
            notifyItemChanged(oldPosition);
        }
        if (position >= 0) {
            notifyItemChanged(position);
        }
    }

    public void clearSelection() {
        int oldPosition = selectedPosition;
        selectedPosition = -1;
        if (oldPosition >= 0) {
            notifyItemChanged(oldPosition);
        }
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView categoryTextView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
        }

        public void bind(Category category, boolean isSelected, OnCategoryClickListener listener, int position) {
            categoryTextView.setText(category.getName());
            
            if (isSelected) {
                categoryTextView.setBackgroundResource(R.drawable.tab_selected_background);
                categoryTextView.setTextColor(itemView.getContext().getResources().getColor(android.R.color.white));
            } else {
                categoryTextView.setBackgroundResource(R.drawable.category_chip_background);
                categoryTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.black));
            }
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category, position);
                }
            });
        }
    }
}
