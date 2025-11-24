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
import com.example.newsapplication.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private UserSessionManager sessionManager;
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

        // Initialize session manager
        sessionManager = new UserSessionManager(getContext());

        initViews();
        setupClickListeners();

        // Always show profile UI (no login required)
        showLoggedInState();

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
        // Login button click
        loginButtonContainer.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.showLoginDialog();
            }
        });

        // Profile menu items
        binding.createPostItem.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Create Post clicked", Toast.LENGTH_SHORT).show();
        });

        binding.myArticlesItem.setOnClickListener(v -> {
            Toast.makeText(getContext(), "My Articles clicked", Toast.LENGTH_SHORT).show();
        });

        binding.settingsItem.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Settings clicked", Toast.LENGTH_SHORT).show();
        });

        binding.editProfileItem.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Edit Profile clicked", Toast.LENGTH_SHORT).show();
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
        // Show login UI
        loginButtonContainer.setVisibility(View.VISIBLE);
        profileContent.setVisibility(View.GONE);
        profileMenuItems.setVisibility(View.GONE);
    }

    private void showLoggedInState() {
        // Show profile UI
        loginButtonContainer.setVisibility(View.GONE);
        profileContent.setVisibility(View.VISIBLE);
        profileMenuItems.setVisibility(View.VISIBLE);

        // Set user data and button text based on login state
        if (sessionManager.isLoggedIn()) {
            userName.setText(sessionManager.getUserName());
            userEmail.setText(sessionManager.getUserEmail());
            binding.authButton.setText("Log Out");
            binding.authButton.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        } else {
            userName.setText("Guest User");
            userEmail.setText("guest@example.com");
            binding.authButton.setText("Login");
            binding.authButton.setTextColor(getResources().getColor(android.R.color.white, null));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}