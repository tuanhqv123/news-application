package com.example.newsapplication.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.newsapplication.MainActivity;
import com.example.newsapplication.R;
import com.example.newsapplication.databinding.FragmentNotificationsBinding;
import com.example.newsapplication.auth.UserSessionManager;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;
    private UserSessionManager sessionManager;
    private LinearLayout loginButtonContainer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize session manager
        sessionManager = new UserSessionManager(getContext());

        initViews();
        setupClickListeners();

        return root;
    }

    private void initViews() {
        // Initialize login button
        loginButtonContainer = binding.loginButtonContainer;

        // Show/hide appropriate UI based on login state OR testing variable
        boolean isLoggedIn = sessionManager.isLoggedIn() || ((MainActivity) getActivity()) != null && ((MainActivity) getActivity()).isUserLoggedInForTesting();

        if (isLoggedIn) {
            // Show logged in UI
            loginButtonContainer.setVisibility(View.GONE);
            binding.userAvatar.setVisibility(View.VISIBLE);
            binding.userName.setVisibility(View.VISIBLE);
            binding.userEmail.setVisibility(View.VISIBLE);
            binding.statsSection.setVisibility(View.VISIBLE);
            binding.settingsMenu.setVisibility(View.VISIBLE);

            binding.userName.setText(sessionManager.getUserName());
            binding.userEmail.setText(sessionManager.getUserEmail());

            // Set user stats (mock data for now - replace with actual data)
            binding.articlesReadCount.setText("5");
            binding.bookmarksCount.setText("20");
            binding.followingCount.setText("12");
        } else {
            // Show logged out state
            loginButtonContainer.setVisibility(View.VISIBLE);
            binding.userAvatar.setVisibility(View.GONE);
            binding.userName.setVisibility(View.GONE);
            binding.userEmail.setVisibility(View.GONE);
            binding.statsSection.setVisibility(View.GONE);
            binding.settingsMenu.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        // Login button for logged out state
        loginButtonContainer.setOnClickListener(v -> {
            // Show login dialog
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.showLoginDialog();
            }
        });

        // Notification Center
        binding.notificationSettings.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Notifications Center clicked", Toast.LENGTH_SHORT).show();
        });

        // Change Password
        binding.accountSettings.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change Password clicked", Toast.LENGTH_SHORT).show();
        });

        // Language
        binding.privacyPolicy.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Language clicked", Toast.LENGTH_SHORT).show();
        });

        // FAQs
        if (binding.faqSettings != null) {
            binding.faqSettings.setOnClickListener(v -> {
                Toast.makeText(getContext(), "FAQs clicked", Toast.LENGTH_SHORT).show();
            });
        }

        // Logout Button
        binding.logoutButton.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();

                // Clear user session
                sessionManager.logoutUser();

                // Navigate back to home
                if (getActivity() instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) getActivity();
                    // Restart activity to clear navigation state
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
            } else {
                Toast.makeText(getContext(), "You are not logged in", Toast.LENGTH_SHORT).show();
            }
        });

        // User Avatar click
        binding.userAvatar.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Change profile picture", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}