package com.example.newsapplication.admin;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapplication.R;
import com.example.newsapplication.adapter.AdminUsersAdapter;
import com.example.newsapplication.api.endpoints.UserEndpoints;
import com.example.newsapplication.auth.UserSessionManager;
import com.example.newsapplication.model.User;
import com.example.newsapplication.model.UserRole;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView usersRecyclerView;
    private AdminUsersAdapter adapter;
    private ProgressBar progressBar;
    private ChipGroup filterChipGroup;
    private FloatingActionButton fabInviteUser;
    private UserEndpoints userEndpoints;
    private UserSessionManager sessionManager;
    private String currentFilter = null; // null = all users

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        sessionManager = new UserSessionManager(this);
        userEndpoints = new UserEndpoints(this);

        initViews();
        setupRecyclerView();
        setupFilterChips();
        setupFab();
        loadUsers(currentFilter);
    }

    private void initViews() {
        usersRecyclerView = findViewById(R.id.usersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        filterChipGroup = findViewById(R.id.filterChipGroup);
        fabInviteUser = findViewById(R.id.fabInviteUser);

        // Back button
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adapter = new AdminUsersAdapter(this, new ArrayList<>(), new AdminUsersAdapter.UserActionListener() {
            @Override
            public void onChangeRole(User user) {
                showChangeRoleDialog(user);
            }

            @Override
            public void onBanUser(User user) {
                banUser(user);
            }

            @Override
            public void onUnbanUser(User user) {
                unbanUser(user);
            }
        });

        usersRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        usersRecyclerView.setAdapter(adapter);
    }

    private void setupFilterChips() {
        filterChipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                currentFilter = null;
                loadUsers(null);
                return;
            }

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll) {
                currentFilter = null;
            } else if (checkedId == R.id.chipAdmins) {
                currentFilter = "admin";
            } else if (checkedId == R.id.chipAuthors) {
                currentFilter = "author";
            } else if (checkedId == R.id.chipReaders) {
                currentFilter = "reader";
            }
            loadUsers(currentFilter);
        });
    }

    private void setupFab() {
        fabInviteUser.setOnClickListener(v -> showInviteUserDialog());
    }

    private void loadUsers(String role) {
        progressBar.setVisibility(View.VISIBLE);
        
        userEndpoints.getAllUserProfiles(role, new UserEndpoints.UserProfilesCallback() {
            @Override
            public void onSuccess(List<User> users) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    adapter.updateUsers(users);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showChangeRoleDialog(User user) {
        ChangeRoleDialog dialog = new ChangeRoleDialog(this, user, new ChangeRoleDialog.RoleChangeListener() {
            @Override
            public void onRoleChanged(User user, UserRole newRole) {
                changeUserRole(user, newRole);
            }
        });
        dialog.show();
    }

    private void changeUserRole(User user, UserRole newRole) {
        progressBar.setVisibility(View.VISIBLE);
        
        userEndpoints.setUserRole(user.getUserId(), newRole.getRoleId(), new UserEndpoints.RoleChangeCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "Role changed successfully", Toast.LENGTH_SHORT).show();
                    loadUsers(currentFilter); // Reload users
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void banUser(User user) {
        progressBar.setVisibility(View.VISIBLE);
        
        userEndpoints.banUser(user.getUserId(), new UserEndpoints.BanCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "User banned", Toast.LENGTH_SHORT).show();
                    loadUsers(currentFilter);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void unbanUser(User user) {
        progressBar.setVisibility(View.VISIBLE);
        
        userEndpoints.unbanUser(user.getUserId(), new UserEndpoints.BanCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "User unbanned", Toast.LENGTH_SHORT).show();
                    loadUsers(currentFilter);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ManageUsersActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void showInviteUserDialog() {
        InviteUserDialog dialog = new InviteUserDialog(this, new InviteUserDialog.InviteListener() {
            @Override
            public void onUserInvited() {
                loadUsers(currentFilter); // Reload users after invite
            }
        });
        dialog.show();
    }
}
