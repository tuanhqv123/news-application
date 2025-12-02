package com.example.newsapplication;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;

import com.example.newsapplication.R;
import com.example.newsapplication.databinding.ActivityMainBinding;
import com.example.newsapplication.auth.AuthenticationDialog;
import com.example.newsapplication.auth.UserSessionManager;
import com.example.newsapplication.auth.AuthService;
import com.example.newsapplication.auth.EditProfileDialog;

import androidx.fragment.app.Fragment;
import android.content.Intent;

import com.example.newsapplication.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity implements AuthenticationDialog.AuthCallback {

    private ActivityMainBinding binding;
    private NavController navController;
    private String currentSource = "VnExpress";
    private UserSessionManager sessionManager;
    private AuthenticationDialog authDialog;
    private EditProfileDialog editProfileDialog;



    // Navigation state constants
    private static final int NAV_HOME = 1;
    private static final int NAV_EXPLORE = 2;
    private static final int NAV_SAVED = 3;
    private static final int NAV_PROFILE = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize session manager
        sessionManager = new UserSessionManager(this);
        
        // Initialize API client with auth token if available
        String authToken = sessionManager.getAuthToken();
        if (authToken != null) {
            new AuthService(this); // This will initialize ApiClient with token
        }

        // Set up navigation to include all fragments
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();

        try {
            navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

            // Set up custom navigation with new layout structure
            binding.homeNavItem.setOnClickListener(v -> {
                try {
                    navController.navigate(R.id.navigation_home);
                    updateNavigationState(NAV_HOME);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            binding.exploreNavItem.setOnClickListener(v -> {
                try {
                    navController.navigate(R.id.navigation_dashboard);
                    updateNavigationState(NAV_EXPLORE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            binding.savedNavItem.setOnClickListener(v -> {
                try {
                    if (sessionManager.isLoggedIn()) {
                        navController.navigate(R.id.navigation_saved);
                        updateNavigationState(NAV_SAVED);
                    } else {
                        showLoginDialog();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            binding.profileNavItem.setOnClickListener(v -> {
                try {
                    if (sessionManager.isLoggedIn()) {
                        navController.navigate(R.id.navigation_profile);
                        updateNavigationState(NAV_PROFILE);
                    } else {
                        showLoginDialog();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            // Set initial state
            updateNavigationState(NAV_HOME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateNavigationState(int selectedNav) {
        try {
            // Reset all navigation items to inactive state (50% opacity)
            setNavigationItemState(binding.homeIcon, binding.homeText, false);
            setNavigationItemState(binding.exploreIcon, binding.exploreText, false);
            setNavigationItemState(binding.savedIcon, binding.savedText, false);
            setNavigationItemState(binding.profileIcon, binding.profileText, false);

            // Set selected item to active state (100% opacity)
            if (selectedNav == NAV_HOME) {
                setNavigationItemState(binding.homeIcon, binding.homeText, true);
            } else if (selectedNav == NAV_EXPLORE) {
                setNavigationItemState(binding.exploreIcon, binding.exploreText, true);
            } else if (selectedNav == NAV_SAVED) {
                setNavigationItemState(binding.savedIcon, binding.savedText, true);
            } else if (selectedNav == NAV_PROFILE) {
                setNavigationItemState(binding.profileIcon, binding.profileText, true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setNavigationItemState(ImageView icon, TextView text, boolean isActive) {
        try {
            float alpha = isActive ? 1.0f : 0.5f;
            icon.setAlpha(alpha);
            text.setAlpha(alpha);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showLoginDialog() {
        if (authDialog == null) {
            authDialog = new AuthenticationDialog(this, this);
        }
        authDialog.show();
    }

    public void navigateToProfile() {
        navController.navigate(R.id.navigation_profile);
        updateNavigationState(NAV_PROFILE);
    }

    public void navigateToExploreWithChannel(int channelId, String channelName, boolean isFollowing) {
        // Navigate to explore and pass channel info
        android.os.Bundle args = new android.os.Bundle();
        args.putInt("channelId", channelId);
        args.putString("channelName", channelName);
        args.putBoolean("isFollowing", isFollowing);
        navController.navigate(R.id.navigation_dashboard, args);
        updateNavigationState(NAV_EXPLORE);
    }

    @Override
    public void onAuthSuccess() {
        String userName = sessionManager.getUserName();
        if (userName.isEmpty()) {
            userName = sessionManager.getUserEmail().split("@")[0];
        }
        
        Toast.makeText(this, "Welcome back, " + userName + "!", Toast.LENGTH_SHORT).show();
        
        navController.navigate(R.id.navigation_saved);
        updateNavigationState(NAV_SAVED);
    }

    @Override
    public void onAuthCancelled() {
    }



    private void switchNewsSource() {
        // Toggle between VnExpress and Tuổi Trẻ
        currentSource = "VnExpress".equals(currentSource) ? "Tuổi Trẻ" : "VnExpress";

        // Show toast message
        Toast.makeText(this, "Switched to " + currentSource, Toast.LENGTH_SHORT).show();

        // Get current fragment and update source
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        if (currentFragment instanceof HomeFragment) {
            ((HomeFragment) currentFragment).switchToSource(currentSource);
        }
    }

    public void setEditProfileDialog(EditProfileDialog dialog) {
        this.editProfileDialog = dialog;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        // Forward image picker result to EditProfileDialog
        if (editProfileDialog != null) {
            editProfileDialog.handleImageResult(requestCode, resultCode, data);
        }
    }
}