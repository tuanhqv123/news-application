package com.example.newsapplication.ui.profile;

import android.os.Bundle;
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
    }

    private void setupClickListeners() {
        // Profile menu items with role-based behavior
        binding.createPostItem.setOnClickListener(v -> {
            if (sessionManager.isAuthor() || sessionManager.isAdmin()) {
                Toast.makeText(getContext(), "Create new article", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to article creation screen
            } else {
                Toast.makeText(getContext(), "Only authors and admins can create posts", Toast.LENGTH_SHORT).show();
            }
        });

        binding.myArticlesItem.setOnClickListener(v -> {
            if (sessionManager.isAuthor() || sessionManager.isAdmin()) {
                Toast.makeText(getContext(), "My Articles", Toast.LENGTH_SHORT).show();
                // TODO: Navigate to user's articles
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

        // User avatar click
        userAvatar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change profile picture", Toast.LENGTH_SHORT).show();
        });

        // Login/Logout button
        binding.authButton.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                // Logout logic
                Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();
                sessionManager.logoutUser();

                // Navigate back to home
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    // Restart activity to clear navigation state
                    android.content.Intent intent = new android.content.Intent(getActivity(), MainActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
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
        
        userName.setText(displayName.isEmpty() ? email.split("@")[0] : displayName);
        userEmail.setText(email);
        binding.authButton.setText("Log Out");
        binding.authButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        
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
        
        binding.settingsItem.setVisibility(View.VISIBLE);
        binding.editProfileItem.setVisibility(View.VISIBLE);
    }

    private void showEditProfileDialog() {
        editProfileDialog = new EditProfileDialog(
            getActivity(),
            getChildFragmentManager(),
            new EditProfileDialog.ProfileUpdateListener() {
                @Override
                public void onProfileUpdated(String displayName, String avatarBase64) {
                    // Update UI with new data - local update only, no API call
                    showLoggedInState();
                    Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onPasswordChanged() {
                    // Password change completed successfully
                    Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
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