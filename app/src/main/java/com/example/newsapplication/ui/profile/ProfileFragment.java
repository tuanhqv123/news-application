package com.example.newsapplication.ui.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.newsapplication.MainActivity;
import com.example.newsapplication.R;
import com.example.newsapplication.auth.UserSessionManager;
import com.example.newsapplication.auth.AuthService;
import com.example.newsapplication.auth.EditProfileDialog;
import com.example.newsapplication.databinding.FragmentProfileBinding;
import com.squareup.picasso.Picasso;
import com.example.newsapplication.utils.CircleTransform;
import org.json.JSONObject;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private UserSessionManager sessionManager;
    private EditProfileDialog editProfileDialog;
    private LinearLayout loginButtonContainer;
    private ImageView userAvatar;
    private TextView userName;
    private TextView userEmail;
    private LinearLayout profileContent;
    private LinearLayout profileMenuItems;
    private LinearLayout adminPanelSection;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize session manager and auth service
        sessionManager = new UserSessionManager(getContext());
        AuthService authService = new AuthService(getContext());

        initViews();
        setupClickListeners();

        // Check login status
        if (sessionManager.isLoggedIn()) {
            // User is logged in, show profile content directly
            showLoggedInState();
        } else {
            showLoggedOutState();
        }

        return root;
    }

    private void initViews() {
        loginButtonContainer = binding.loginButtonContainer;
        userAvatar = binding.userAvatar;
        userName = binding.userName;
        userEmail = binding.userEmail;
        profileContent = binding.profileContent;
        profileMenuItems = binding.profileMenuItems;
        adminPanelSection = binding.adminPanelSection;
    }

    private void setupClickListeners() {
        // Profile menu items with role-based behavior
        binding.createPostItem.setOnClickListener(v -> {
            if (sessionManager.isAuthor() || sessionManager.isAdmin()) {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.example.newsapplication.CreateArticleActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Only authors and admins can create posts", Toast.LENGTH_SHORT).show();
            }
        });

        binding.myArticlesItem.setOnClickListener(v -> {
            if (sessionManager.isAuthor() || sessionManager.isAdmin()) {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.example.newsapplication.MyArticlesActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Only authors and admins can view their articles", Toast.LENGTH_SHORT).show();
            }
        });

        binding.settingsItem.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Settings", Toast.LENGTH_SHORT).show();
            // TODO: Open settings screen
        });

        binding.editProfileItem.setOnClickListener(v -> {
            showEditProfileDialog();
        });

        // Admin menu items
        binding.manageUsersItem.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.example.newsapplication.admin.ManageUsersActivity.class);
            startActivity(intent);
        });

        binding.manageArticlesItem.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.example.newsapplication.admin.ManageArticlesActivity.class);
            startActivity(intent);
        });

        binding.manageChannelsItem.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.example.newsapplication.admin.ManageChannelsActivity.class);
            startActivity(intent);
        });

        binding.manageCategoriesItem.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(getActivity(), com.example.newsapplication.admin.ManageCategoriesActivity.class);
            startActivity(intent);
        });

        // User avatar click
        userAvatar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change profile picture", Toast.LENGTH_SHORT).show();
        });

        // Login/Logout button
        binding.authButton.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                // Logout logic - use AuthService to properly call API
                Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();

                AuthService authService = new AuthService(getContext());
                authService.logout(new AuthService.AuthResultCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        // Navigate back to home after successful logout
                        if (getActivity() instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity) getActivity();
                            // Restart activity to clear navigation state
                            android.content.Intent intent = new android.content.Intent(getActivity(), MainActivity.class);
                            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // Still navigate to home even if logout API fails
                        Toast.makeText(getContext(), "Logout completed", Toast.LENGTH_SHORT).show();
                        if (getActivity() instanceof MainActivity) {
                            MainActivity mainActivity = (MainActivity) getActivity();
                            android.content.Intent intent = new android.content.Intent(getActivity(), MainActivity.class);
                            intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            getActivity().finish();
                        }
                    }
                });
            } else {
                // Show login dialog
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    mainActivity.showLoginDialog();
                }
            }
        });
    }

    private void showLoggedOutState() {
        // Show empty profile state
        loginButtonContainer.setVisibility(View.GONE);
        profileContent.setVisibility(View.GONE);
        profileMenuItems.setVisibility(View.GONE);
        

    }

    private void showLoggedInState() {
        // Show profile UI
        loginButtonContainer.setVisibility(View.GONE);
        profileContent.setVisibility(View.VISIBLE);
        profileMenuItems.setVisibility(View.VISIBLE);

        // Set user data
        String displayName = sessionManager.getUserName();
        String email = sessionManager.getUserEmail();
        String role = sessionManager.getUserRole();
        String avatarUrl = sessionManager.getAvatarUrl();

        // Debug logging
        Log.d("ProfileFragment", "Loading profile data:");
        Log.d("ProfileFragment", "  Display Name: " + displayName);
        Log.d("ProfileFragment", "  Email: " + email);
        Log.d("ProfileFragment", "  Role: " + role);
        Log.d("ProfileFragment", "  Avatar URL: " + avatarUrl);
        
        userName.setText(displayName.isEmpty() ? email.split("@")[0] : displayName);
        userEmail.setText(email);
        binding.authButton.setText("Log Out");
        binding.authButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        
        // Load avatar image with circular transform
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            Log.d("ProfileFragment", "Loading avatar URL: " + avatarUrl);

            // First try to load from cache/network with better error handling
            Picasso.get()
                    .load(avatarUrl)
                    .transform(new CircleTransform())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(userAvatar, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("ProfileFragment", "Avatar loaded successfully");
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("ProfileFragment", "Failed to load avatar", e);
                            // Retry once with memory cleared
                            Picasso.get()
                                    .invalidate(avatarUrl);
                            Picasso.get()
                                    .load(avatarUrl)
                                    .transform(new CircleTransform())
                                    .placeholder(R.drawable.ic_launcher_foreground)
                                    .error(R.drawable.ic_launcher_foreground)
                                    .memoryPolicy(com.squareup.picasso.MemoryPolicy.NO_CACHE, com.squareup.picasso.MemoryPolicy.NO_STORE)
                                    .networkPolicy(com.squareup.picasso.NetworkPolicy.NO_CACHE)
                                    .into(userAvatar);
                        }
                    });
        } else {
            // Set default avatar
            userAvatar.setImageResource(R.drawable.ic_launcher_foreground);
        }
        
        // Show/hide menu items based on role
        updateMenuItemsByRole(role);
    }

    private void updateMenuItemsByRole(String role) {
        // Show/hide menu items based on role
        if (sessionManager.isAuthor() || sessionManager.isAdmin()) {
            binding.createPostItem.setVisibility(View.VISIBLE);
            binding.myArticlesItem.setVisibility(View.VISIBLE);
        } else {
            binding.createPostItem.setVisibility(View.GONE);
            binding.myArticlesItem.setVisibility(View.GONE);
        }
        
        // Show admin panel only for admins
        if (sessionManager.isAdmin()) {
            adminPanelSection.setVisibility(View.VISIBLE);
        } else {
            adminPanelSection.setVisibility(View.GONE);
        }
        
        binding.settingsItem.setVisibility(View.VISIBLE);
        binding.editProfileItem.setVisibility(View.VISIBLE);
    }

    private void showEditProfileDialog() {
        editProfileDialog = new EditProfileDialog(
            getActivity(),
            getChildFragmentManager(),
            new EditProfileDialog.ProfileUpdateListener() {
                @Override
                public void onProfileUpdated(String displayName, String avatarUrl) {
                    // Save avatar URL to session
                    if (avatarUrl != null && !avatarUrl.isEmpty()) {
                        sessionManager.setAvatarUrl(avatarUrl);
                    }
                    // Update UI with new data
                    showLoggedInState();
                }
            });
        
        editProfileDialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Handle image picker result from edit profile dialog
        if (editProfileDialog != null) {
            editProfileDialog.handleImageResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}