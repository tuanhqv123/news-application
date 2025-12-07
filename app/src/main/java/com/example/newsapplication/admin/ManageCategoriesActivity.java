package com.example.newsapplication.admin;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;
import com.example.newsapplication.adapter.AdminCategoriesAdapter;
import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.api.endpoints.CategoryEndpoints;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ManageCategoriesActivity extends AppCompatActivity {

    private RecyclerView categoriesRecyclerView;
    private AdminCategoriesAdapter adapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddCategory;
    private CategoryEndpoints categoryEndpoints;
    private List<JSONObject> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        categoryEndpoints = new CategoryEndpoints(new ApiClient(this));

        initViews();
        setupRecyclerView();
        setupFab();
        loadCategories();
    }

    private void initViews() {
        categoriesRecyclerView = findViewById(R.id.categoriesRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        fabAddCategory = findViewById(R.id.fabAddCategory);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AdminCategoriesAdapter(this, new ArrayList<>(), category -> showCategoryActionsDialog(category));
        categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoriesRecyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        fabAddCategory.setOnClickListener(v -> showCreateCategoryDialog());
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);

        categoryEndpoints.getCategories(new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    parseCategories(response.getData());
                });
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageCategoriesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void parseCategories(JSONObject response) {
        categories.clear();
        try {
            JSONArray categoriesArray;
            if (response.has("data")) {
                JSONObject data = response.getJSONObject("data");
                categoriesArray = data.has("categories") ? data.getJSONArray("categories") : new JSONArray();
            } else if (response.has("categories")) {
                categoriesArray = response.getJSONArray("categories");
            } else {
                categoriesArray = new JSONArray();
            }

            for (int i = 0; i < categoriesArray.length(); i++) {
                categories.add(categoriesArray.getJSONObject(i));
            }
            adapter.updateCategories(categories);
        } catch (Exception e) {
            Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showCategoryActionsDialog(JSONObject category) {
        String name = category.optString("name", "Category");
        int categoryId = category.optInt("id");

        new AlertDialog.Builder(this)
                .setTitle(name)
                .setItems(new String[]{"View Details"}, (dialog, which) -> {
                    Toast.makeText(this, "Category ID: " + categoryId + "\nSlug: " + category.optString("slug"), Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showCreateCategoryDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_category);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText nameInput = dialog.findViewById(R.id.nameInput);
        EditText slugInput = dialog.findViewById(R.id.slugInput);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnCreate = dialog.findViewById(R.id.btnCreate);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String slug = slugInput.getText().toString().trim();

            if (name.isEmpty() || slug.isEmpty()) {
                Toast.makeText(this, "Name and slug are required", Toast.LENGTH_SHORT).show();
                return;
            }

            createCategory(name, slug);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createCategory(String name, String slug) {
        progressBar.setVisibility(View.VISIBLE);

        categoryEndpoints.createCategory(name, slug, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageCategoriesActivity.this, "Category created", Toast.LENGTH_SHORT).show();
                    loadCategories();
                });
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageCategoriesActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
