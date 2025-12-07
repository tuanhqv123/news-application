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
import com.example.newsapplication.adapter.AdminChannelsAdapter;
import com.example.newsapplication.api.ApiClient;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.api.endpoints.ChannelEndpoints;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ManageChannelsActivity extends AppCompatActivity {

    private RecyclerView channelsRecyclerView;
    private AdminChannelsAdapter adapter;
    private ProgressBar progressBar;
    private FloatingActionButton fabAddChannel;
    private ChannelEndpoints channelEndpoints;
    private List<JSONObject> channels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_channels);

        channelEndpoints = new ChannelEndpoints(new ApiClient(this));

        initViews();
        setupRecyclerView();
        setupFab();
        loadChannels();
    }

    private void initViews() {
        channelsRecyclerView = findViewById(R.id.channelsRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        fabAddChannel = findViewById(R.id.fabAddChannel);
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AdminChannelsAdapter(this, new ArrayList<>(), channel -> showChannelActionsDialog(channel));
        channelsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        channelsRecyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        fabAddChannel.setOnClickListener(v -> showCreateChannelDialog());
    }

    private void loadChannels() {
        progressBar.setVisibility(View.VISIBLE);

        channelEndpoints.getPublicChannels(new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    parseChannels(response.getData());
                });
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageChannelsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void parseChannels(JSONObject response) {
        channels.clear();
        try {
            JSONObject data = response.has("data") ? response.getJSONObject("data") : response;
            JSONArray channelsArray = data.getJSONArray("channels");

            for (int i = 0; i < channelsArray.length(); i++) {
                channels.add(channelsArray.getJSONObject(i));
            }
            adapter.updateChannels(channels);
        } catch (Exception e) {
            Toast.makeText(this, "Parse error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showChannelActionsDialog(JSONObject channel) {
        String name = channel.optString("name", "Channel");
        boolean isActive = channel.optBoolean("is_active", true);

        String[] actions = isActive
                ? new String[]{"View Details", "Edit", "Deactivate", "Delete"}
                : new String[]{"View Details", "Edit", "Activate", "Delete"};

        new AlertDialog.Builder(this)
                .setTitle(name)
                .setItems(actions, (dialog, which) -> {
                    int channelId = channel.optInt("id");
                    switch (which) {
                        case 0:
                            Toast.makeText(this, "Channel ID: " + channelId, Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            showEditChannelDialog(channel);
                            break;
                        case 2:
                            updateChannelStatus(channelId, !isActive);
                            break;
                        case 3:
                            confirmDeleteChannel(channelId, name);
                            break;
                    }
                })
                .show();
    }

    private void showCreateChannelDialog() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_channel);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText nameInput = dialog.findViewById(R.id.nameInput);
        EditText slugInput = dialog.findViewById(R.id.slugInput);
        EditText descriptionInput = dialog.findViewById(R.id.descriptionInput);
        EditText logoUrlInput = dialog.findViewById(R.id.logoUrlInput);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnCreate = dialog.findViewById(R.id.btnCreate);

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String slug = slugInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String logoUrl = logoUrlInput.getText().toString().trim();

            if (name.isEmpty() || slug.isEmpty()) {
                Toast.makeText(this, "Name and slug are required", Toast.LENGTH_SHORT).show();
                return;
            }

            createChannel(name, slug, description.isEmpty() ? null : description, logoUrl.isEmpty() ? null : logoUrl);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEditChannelDialog(JSONObject channel) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_create_channel);

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout((int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        EditText nameInput = dialog.findViewById(R.id.nameInput);
        EditText slugInput = dialog.findViewById(R.id.slugInput);
        EditText descriptionInput = dialog.findViewById(R.id.descriptionInput);
        EditText logoUrlInput = dialog.findViewById(R.id.logoUrlInput);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnCreate = dialog.findViewById(R.id.btnCreate);

        nameInput.setText(channel.optString("name"));
        slugInput.setText(channel.optString("slug"));
        slugInput.setEnabled(false);
        descriptionInput.setText(channel.optString("description"));
        logoUrlInput.setText(channel.optString("logo_url"));
        btnCreate.setText("Update");

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnCreate.setOnClickListener(v -> {
            String name = nameInput.getText().toString().trim();
            String description = descriptionInput.getText().toString().trim();
            String logoUrl = logoUrlInput.getText().toString().trim();

            if (name.isEmpty()) {
                Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show();
                return;
            }

            updateChannel(channel.optInt("id"), name, description.isEmpty() ? null : description, logoUrl.isEmpty() ? null : logoUrl);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void createChannel(String name, String slug, String description, String logoUrl) {
        progressBar.setVisibility(View.VISIBLE);

        channelEndpoints.createChannel(name, slug, description, null, logoUrl, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageChannelsActivity.this, "Channel created", Toast.LENGTH_SHORT).show();
                    loadChannels();
                });
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageChannelsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateChannel(int channelId, String name, String description, String logoUrl) {
        progressBar.setVisibility(View.VISIBLE);

        channelEndpoints.updateChannel(channelId, name, description, null, logoUrl, null, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageChannelsActivity.this, "Channel updated", Toast.LENGTH_SHORT).show();
                    loadChannels();
                });
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageChannelsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateChannelStatus(int channelId, boolean isActive) {
        progressBar.setVisibility(View.VISIBLE);

        channelEndpoints.updateChannel(channelId, null, null, null, null, isActive, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageChannelsActivity.this, isActive ? "Channel activated" : "Channel deactivated", Toast.LENGTH_SHORT).show();
                    loadChannels();
                });
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageChannelsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void confirmDeleteChannel(int channelId, String name) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Channel")
                .setMessage("Are you sure you want to delete \"" + name + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> deleteChannel(channelId))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteChannel(int channelId) {
        progressBar.setVisibility(View.VISIBLE);

        channelEndpoints.deleteChannel(channelId, new ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageChannelsActivity.this, "Channel deleted", Toast.LENGTH_SHORT).show();
                    loadChannels();
                });
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageChannelsActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
