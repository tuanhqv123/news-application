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

import androidx.fragment.app.Fragment;

import com.example.newsapplication.ui.home.HomeFragment;

public class MainActivity extends AppCompatActivity implements AuthenticationDialog.AuthCallback {

    private ActivityMainBinding binding;
    private NavController navController;
    private String currentSource = "VnExpress";
    private UserSessionManager sessionManager;
    private AuthenticationDialog authDialog;

    // Testing variable - set to true to bypass login for testing
    private boolean is_logged_in = true;

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
                    // Navigate to saved fragment (no login required)
                    navController.navigate(R.id.navigation_saved);
                    updateNavigationState(NAV_SAVED);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            binding.profileNavItem.setOnClickListener(v -> {
                try {
                    // Navigate to profile fragment (no login required)
                    navController.navigate(R.id.navigation_profile);
                    updateNavigationState(NAV_PROFILE);
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

    @Override
    public void onLoginSuccess(String email, String password) {
        // Create session with default name from email
        String name = email.substring(0, email.indexOf("@"));
        String role = "reader"; // Default role for login users

        sessionManager.createLoginSession(email, name, role);

        Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_SHORT).show();
        // Navigate to saved items after login
        navController.navigate(R.id.navigation_notifications);
        updateNavigationState(NAV_SAVED);
    }

    @Override
    public void onSignupSuccess(String name, String email, String password, String role) {
        sessionManager.createLoginSession(email, name, role);

        Toast.makeText(this, "Welcome, " + name + "! Role: " + role, Toast.LENGTH_SHORT).show();
        // Navigate to profile after signup
        navController.navigate(R.id.navigation_notifications);
        updateNavigationState(NAV_PROFILE);
    }

    @Override
    public void onAuthCancelled() {
        // User closed the dialog without authenticating
        Toast.makeText(this, "Authentication cancelled", Toast.LENGTH_SHORT).show();
    }

    public boolean isUserLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public String getCurrentUserRole() {
        return sessionManager.getUserRole();
    }

    public String getCurrentUserName() {
        return sessionManager.getUserName();
    }

    public String getCurrentUserEmail() {
        return sessionManager.getUserEmail();
    }

    public boolean isUserLoggedInForTesting() {
        return is_logged_in;
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
}