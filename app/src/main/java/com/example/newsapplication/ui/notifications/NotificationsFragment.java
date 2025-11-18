package com.example.newsapplication.ui.notifications;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.newsapplication.MainActivity;
import com.example.newsapplication.R;
import com.example.newsapplication.databinding.FragmentNotificationsBinding;

public class NotificationsFragment extends Fragment {

    private FragmentNotificationsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NotificationsViewModel notificationsViewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        initViews();
        setupClickListeners();

        return root;
    }

    private void initViews() {
        // Set user stats (mock data)
        binding.articlesReadCount.setText("5");
        binding.bookmarksCount.setText("20");

        // Following count
        if (binding.followingCount != null) {
            binding.followingCount.setText("12");
        }
    }

    private void setupClickListeners() {
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
            Toast.makeText(getContext(), "Logging out...", Toast.LENGTH_SHORT).show();

            // Navigate back to home and reset user session
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                // Reset navigation to home
                mainActivity.finish();
                // Restart app to clear user session
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
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