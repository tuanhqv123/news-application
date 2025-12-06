package com.example.newsapplication;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
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
<<<<<<< HEAD
import com.example.newsapplication.notifications.NotificationManager;
import org.json.JSONObject;
=======
import com.example.newsapplication.audio.AudioPlayerService;
>>>>>>> c6a20c4f4a3003c7e995cc1ebc3107cf9681e42c

import androidx.fragment.app.Fragment;

import com.example.newsapplication.ui.home.HomeFragment;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AuthenticationDialog.AuthCallback {

    private ActivityMainBinding binding;
    private NavController navController;
    private String currentSource = "VnExpress";
    private UserSessionManager sessionManager;
    private AuthenticationDialog authDialog;
    private EditProfileDialog editProfileDialog;

    // Audio mini-player views
    private View audioMiniPlayerBar;
    private ImageView audioMiniPlayPause;
    private TextView audioMiniTitle;
    private TextView audioMiniCurrentTime;
    private SeekBar audioMiniSeekBar;
    private TextView audioMiniDuration;
    private ImageView audioMiniClose;
    private boolean isUserSeeking = false;

    private static final String TAG = "MainActivity";
    
    // BroadcastReceiver for audio state updates
    private BroadcastReceiver audioStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioPlayerService.ACTION_AUDIO_STATE.equals(intent.getAction())) {
                boolean playing = intent.getBooleanExtra(AudioPlayerService.EXTRA_IS_PLAYING, false);
                int position = intent.getIntExtra(AudioPlayerService.EXTRA_POSITION_MS, 0);
                int duration = intent.getIntExtra(AudioPlayerService.EXTRA_DURATION_MS_BROADCAST, 0);
                String title = intent.getStringExtra(AudioPlayerService.EXTRA_AUDIO_TITLE);
                if (title == null) {
                    title = "";
                }
                
                Log.d(TAG, "Broadcast received - playing: " + playing + ", position: " + position + 
                      ", duration: " + duration + ", title: " + title);
                
                updateAudioMiniBar(playing, position, duration, title);
            }
        }
    };

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

        // Initialize notification system
        NotificationManager.getInstance(this).initialize();

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
        
        // Initialize audio mini-player
        initAudioMiniPlayer();
    }
    
    private void initAudioMiniPlayer() {
        audioMiniPlayerBar = binding.audioMiniPlayerBar;
        audioMiniPlayPause = binding.audioMiniPlayPause;
        audioMiniTitle = binding.audioMiniTitle;
        audioMiniCurrentTime = binding.audioMiniCurrentTime;
        audioMiniSeekBar = binding.audioMiniSeekBar;
        audioMiniDuration = binding.audioMiniDuration;
        audioMiniClose = binding.audioMiniClose;
        
        // Initially hide mini-player
        audioMiniPlayerBar.setVisibility(View.GONE);
        
        // Setup click listeners
        audioMiniPlayPause.setOnClickListener(v -> toggleAudioPlayback());
        audioMiniClose.setOnClickListener(v -> stopAudio());
        
        // Setup seekbar listener
        audioMiniSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    audioMiniCurrentTime.setText(formatMillis(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isUserSeeking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isUserSeeking = false;
                // Send seek command to service
                Intent seekIntent = new Intent(MainActivity.this, AudioPlayerService.class);
                seekIntent.setAction(AudioPlayerService.ACTION_SEEK);
                seekIntent.putExtra(AudioPlayerService.EXTRA_SEEK_POSITION, seekBar.getProgress());
                startService(seekIntent);
            }
        });
    }
    
    private void toggleAudioPlayback() {
        // Always use ACTION_PAUSE because handlePause() can toggle between play and pause
        // It will resume if paused, or pause if playing
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_PAUSE);
        startService(intent);
    }
    
    private void stopAudio() {
        Intent intent = new Intent(this, AudioPlayerService.class);
        intent.setAction(AudioPlayerService.ACTION_STOP);
        startService(intent);
        audioMiniPlayerBar.setVisibility(View.GONE);
    }
    
    private void updateAudioMiniBar(boolean playing, int position, int duration, String title) {
        Log.d(TAG, "updateAudioMiniBar() - playing: " + playing + ", position: " + position + 
              ", duration: " + duration + ", title: " + title);
        
        // Always show mini-player if there's any audio state (playing, paused, or has title)
        boolean hasTitle = (title != null && !title.isEmpty()) || AudioPlayerService.sCurrentTitle != null;
        boolean shouldShow = playing || duration > 0 || hasTitle || AudioPlayerService.sIsPlaying;
        
        Log.d(TAG, "updateAudioMiniBar() - hasTitle: " + hasTitle + ", shouldShow: " + shouldShow);
        
        if (shouldShow) {
            Log.d(TAG, "updateAudioMiniBar() - Showing mini-player");
            // Show mini-player
            audioMiniPlayerBar.setVisibility(View.VISIBLE);
            
            // Ensure all child views are visible
            audioMiniPlayPause.setVisibility(View.VISIBLE);
            audioMiniClose.setVisibility(View.VISIBLE);
            audioMiniTitle.setVisibility(View.VISIBLE);
            audioMiniSeekBar.setVisibility(View.VISIBLE);
            audioMiniCurrentTime.setVisibility(View.VISIBLE);
            audioMiniDuration.setVisibility(View.VISIBLE);
            
            // Update title
            if (title != null && !title.isEmpty()) {
                audioMiniTitle.setText(title);
            } else if (AudioPlayerService.sCurrentTitle != null) {
                audioMiniTitle.setText(AudioPlayerService.sCurrentTitle);
            }
            
            // Update play/pause icon based on playing state
            audioMiniPlayPause.setImageResource(playing ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
            Log.d(TAG, "updateAudioMiniBar() - Set play/pause icon, playing: " + playing);
            
            // Update duration and seekbar
            if (duration > 0) {
                if (audioMiniSeekBar.getMax() != duration) {
                    audioMiniSeekBar.setMax(duration);
                }
                audioMiniDuration.setText(formatMillis(duration));
            }
            
            // Update position
            if (!isUserSeeking && position >= 0) {
                audioMiniSeekBar.setProgress(position);
                audioMiniCurrentTime.setText(formatMillis(position));
            }
        } else {
            Log.d(TAG, "updateAudioMiniBar() - NOT showing mini-player");
            // Only hide if truly stopped (no duration, no title, and not playing)
            if (!AudioPlayerService.sIsPlaying && AudioPlayerService.sCurrentTitle == null) {
                audioMiniPlayerBar.setVisibility(View.GONE);
            }
        }
    }
    
    private boolean mediaPlayerExists() {
        // Check if service indicates audio exists
        return AudioPlayerService.sIsPlaying || AudioPlayerService.sCurrentTitle != null;
    }
    
    private String formatMillis(int millis) {
        if (millis <= 0) {
            return "00:00";
        }
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
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

        // Update notification token with user ID after successful login
        NotificationManager.getInstance(this).onUserLoggedIn();

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

    private boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (AudioPlayerService.class.getName().equals(service.service.getClassName())) {
                    Log.d(TAG, "Service is running");
                    return true;
                }
            }
        }
        Log.d(TAG, "Service is NOT running");
        return false;
    }
    
    private boolean hasActiveNotification() {
        // Check if there's an active notification from AudioPlayerService
        // This is another way to detect if service is running
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                android.service.notification.StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
                for (android.service.notification.StatusBarNotification notification : notifications) {
                    if (notification.getId() == 1) { // AudioPlayerService uses NOTIFICATION_ID = 1
                        Log.d(TAG, "Found active notification from AudioPlayerService");
                        return true;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error checking notifications", e);
            }
        }
        return false;
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
        // Register broadcast receiver
        IntentFilter filter = new IntentFilter(AudioPlayerService.ACTION_AUDIO_STATE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(audioStateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(audioStateReceiver, filter);
        }
        
        // Check if audio is already playing or paused and show mini-player immediately
        // This ensures mini-player shows when back from ArticleDetailActivity
        // Check both static variables, service status, and notification
        boolean sIsPlaying = AudioPlayerService.sIsPlaying;
        String sCurrentTitle = AudioPlayerService.sCurrentTitle;
        boolean serviceRunning = isServiceRunning();
        boolean hasNotification = hasActiveNotification();
        
        Log.d(TAG, "onStart() - sIsPlaying: " + sIsPlaying + ", sCurrentTitle: " + sCurrentTitle + 
              ", serviceRunning: " + serviceRunning + ", hasNotification: " + hasNotification);
        
        // Show mini-player if ANY indicator shows audio exists
        boolean hasAudioState = sIsPlaying || 
                               (sCurrentTitle != null && !sCurrentTitle.isEmpty()) ||
                               serviceRunning ||
                               hasNotification;
        
        Log.d(TAG, "onStart() - hasAudioState: " + hasAudioState);
        
        if (hasAudioState) {
            Log.d(TAG, "onStart() - Showing mini-player");
            audioMiniPlayerBar.setVisibility(View.VISIBLE);
            
            // Ensure all child views are visible
            if (audioMiniPlayPause != null) audioMiniPlayPause.setVisibility(View.VISIBLE);
            if (audioMiniClose != null) audioMiniClose.setVisibility(View.VISIBLE);
            if (audioMiniTitle != null) audioMiniTitle.setVisibility(View.VISIBLE);
            if (audioMiniSeekBar != null) audioMiniSeekBar.setVisibility(View.VISIBLE);
            if (audioMiniCurrentTime != null) audioMiniCurrentTime.setVisibility(View.VISIBLE);
            if (audioMiniDuration != null) audioMiniDuration.setVisibility(View.VISIBLE);
            
            if (sCurrentTitle != null && !sCurrentTitle.isEmpty()) {
                audioMiniTitle.setText(sCurrentTitle);
            } else {
                // If no title but service is running, show placeholder
                audioMiniTitle.setText("Audio đang phát");
            }
            audioMiniPlayPause.setImageResource(sIsPlaying ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
            Log.d(TAG, "onStart() - Set play/pause icon, sIsPlaying: " + sIsPlaying);
            
            // Request current state from service immediately
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d(TAG, "onStart() - Requesting state from service");
                Intent requestIntent = new Intent(MainActivity.this, AudioPlayerService.class);
                requestIntent.setAction(AudioPlayerService.ACTION_SEEK);
                requestIntent.putExtra(AudioPlayerService.EXTRA_SEEK_POSITION, -1);
                startService(requestIntent);
            }, 50);
        } else {
            Log.d(TAG, "onStart() - NOT showing mini-player (no audio state)");
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
        // Always check when resuming (e.g., when back from another activity)
        // Show mini-player immediately if audio exists (playing or paused)
        // This is a backup check in case onStart() didn't catch it
        // Check both static variables, service status, and notification
        boolean sIsPlaying = AudioPlayerService.sIsPlaying;
        String sCurrentTitle = AudioPlayerService.sCurrentTitle;
        boolean serviceRunning = isServiceRunning();
        boolean hasNotification = hasActiveNotification();
        
        Log.d(TAG, "onResume() - sIsPlaying: " + sIsPlaying + ", sCurrentTitle: " + sCurrentTitle + 
              ", serviceRunning: " + serviceRunning + ", hasNotification: " + hasNotification);
        
        // Show mini-player if ANY indicator shows audio exists
        boolean hasAudio = sIsPlaying || 
                          (sCurrentTitle != null && !sCurrentTitle.isEmpty()) ||
                          serviceRunning ||
                          hasNotification;
        
        Log.d(TAG, "onResume() - hasAudio: " + hasAudio);
        
        if (hasAudio) {
            Log.d(TAG, "onResume() - Showing mini-player");
            // Show mini-player immediately
            audioMiniPlayerBar.setVisibility(View.VISIBLE);
            
            // Ensure all child views are visible
            if (audioMiniPlayPause != null) audioMiniPlayPause.setVisibility(View.VISIBLE);
            if (audioMiniClose != null) audioMiniClose.setVisibility(View.VISIBLE);
            if (audioMiniTitle != null) audioMiniTitle.setVisibility(View.VISIBLE);
            if (audioMiniSeekBar != null) audioMiniSeekBar.setVisibility(View.VISIBLE);
            if (audioMiniCurrentTime != null) audioMiniCurrentTime.setVisibility(View.VISIBLE);
            if (audioMiniDuration != null) audioMiniDuration.setVisibility(View.VISIBLE);
            
            if (sCurrentTitle != null && !sCurrentTitle.isEmpty()) {
                audioMiniTitle.setText(sCurrentTitle);
            } else {
                // If no title but service is running, show placeholder
                audioMiniTitle.setText("Audio đang phát");
            }
            audioMiniPlayPause.setImageResource(sIsPlaying ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
            Log.d(TAG, "onResume() - Set play/pause icon, sIsPlaying: " + sIsPlaying);
            
            // Request current state from service - trigger a broadcast immediately
            // Use a handler to delay slightly to ensure service is ready
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d(TAG, "onResume() - Requesting state from service");
                Intent requestIntent = new Intent(MainActivity.this, AudioPlayerService.class);
                requestIntent.setAction(AudioPlayerService.ACTION_SEEK);
                requestIntent.putExtra(AudioPlayerService.EXTRA_SEEK_POSITION, -1); // Special value to just trigger broadcast
                startService(requestIntent);
            }, 50); // Small delay to ensure service is ready
        } else {
            Log.d(TAG, "onResume() - NOT showing mini-player (no audio state)");
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        // Unregister broadcast receiver
        try {
            unregisterReceiver(audioStateReceiver);
        } catch (Exception e) {
            // Already unregistered
        }
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