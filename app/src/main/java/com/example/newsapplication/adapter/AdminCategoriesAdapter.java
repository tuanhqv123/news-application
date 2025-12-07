package com.example.newsapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;

import org.json.JSONObject;

import java.util.List;

public class AdminCategoriesAdapter extends RecyclerView.Adapter<AdminCategoriesAdapter.CategoryViewHolder> {

    private Context context;
    private List<JSONObject> categories;
    private CategoryActionListener listener;

    public interface CategoryActionListener {
        void onCategoryClick(JSONObject category);
    }

    public AdminCategoriesAdapter(Context context, List<JSONObject> categories, CategoryActionListener listener) {
        this.context = context;
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        JSONObject category = categories.get(position);

        holder.categoryName.setText(category.optString("name", "Unknown"));
        holder.categorySlug.setText(category.optString("slug", ""));

        holder.categoryCard.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateCategories(List<JSONObject> newCategories) {
        this.categories = newCategories;
        notifyDataSetChanged();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        CardView categoryCard;
        TextView categoryName;
        TextView categorySlug;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryCard = itemView.findViewById(R.id.categoryCard);
            categoryName = itemView.findViewById(R.id.categoryName);
            categorySlug = itemView.findViewById(R.id.categorySlug);
        }
    }
}
