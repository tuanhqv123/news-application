package com.example.newsapplication;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
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
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.fragment.app.Fragment;

import com.example.newsapplication.R;
import com.example.newsapplication.auth.GoogleAuthHelper;
import com.example.newsapplication.databinding.ActivityMainBinding;
import com.example.newsapplication.auth.AuthenticationDialog;
import com.example.newsapplication.auth.UserSessionManager;
import com.example.newsapplication.auth.AuthService;
import com.example.newsapplication.auth.EditProfileDialog;
import com.example.newsapplication.auth.PasswordSetupDialog;
import com.example.newsapplication.audio.AudioPlayerService;
import com.example.newsapplication.ui.home.HomeFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements AuthenticationDialog.AuthCallback {

    private ActivityMainBinding binding;
    private NavController navController;
    private String currentSource = "VnExpress";
    private UserSessionManager sessionManager;
    private AuthenticationDialog authDialog;
    private EditProfileDialog editProfileDialog;
    private PasswordSetupDialog passwordSetupDialog;

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

        // ✅ CHECK GOOGLE PLAY SERVICES FIRST
        checkGooglePlayServices();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize session manager
        sessionManager = new UserSessionManager(this);

        // Initialize API client with auth token if available
        String authToken = sessionManager.getAuthToken();
        if (authToken != null) {
            new AuthService(this); // This will initialize ApiClient with token
        }

        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // Initialize notification system
        com.example.newsapplication.notifications.NotificationManager.getInstance(this).initialize();

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

        // Check if we need to show login dialog (e.g., after logout)
        if (getIntent() != null && getIntent().getBooleanExtra("showLogin", false)) {
            // Show login dialog after a short delay to ensure UI is ready
            new android.os.Handler().postDelayed(() -> {
                showLoginDialog();
            }, 300);
        }

        // Initialize audio mini-player
        initAudioMiniPlayer();

        // Handle deep links
        handleIntent(getIntent());

        // Check for notification data
        handleNotificationClick();
    }

    /**
     * ✅ CHECK GOOGLE PLAY SERVICES AVAILABILITY
     */
    private void checkGooglePlayServices() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e(TAG, "❌ Google Play Services not available. Error code: " + resultCode);

            if (availability.isUserResolvableError(resultCode)) {
                // Show dialog to install/update Google Play Services
                Log.d(TAG, "Showing Google Play Services error dialog");
                availability.getErrorDialog(this, resultCode, 9000).show();
            } else {
                Log.e(TAG, "This device doesn't support Google Play Services");
                Toast.makeText(this,
                        "This device doesn't support Google Sign-In",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            Log.d(TAG, "✅ Google Play Services is available and up to date");
        }
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

        boolean hasTitle = (title != null && !title.isEmpty()) || AudioPlayerService.sCurrentTitle != null;
        boolean shouldShow = playing || duration > 0 || hasTitle || AudioPlayerService.sIsPlaying;

        Log.d(TAG, "updateAudioMiniBar() - hasTitle: " + hasTitle + ", shouldShow: " + shouldShow);

        if (shouldShow) {
            Log.d(TAG, "updateAudioMiniBar() - Showing mini-player");
            audioMiniPlayerBar.setVisibility(View.VISIBLE);

            audioMiniPlayPause.setVisibility(View.VISIBLE);
            audioMiniClose.setVisibility(View.VISIBLE);
            audioMiniTitle.setVisibility(View.VISIBLE);
            audioMiniSeekBar.setVisibility(View.VISIBLE);
            audioMiniCurrentTime.setVisibility(View.VISIBLE);
            audioMiniDuration.setVisibility(View.VISIBLE);

            if (title != null && !title.isEmpty()) {
                audioMiniTitle.setText(title);
            } else if (AudioPlayerService.sCurrentTitle != null) {
                audioMiniTitle.setText(AudioPlayerService.sCurrentTitle);
            }

            audioMiniPlayPause.setImageResource(playing ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
            Log.d(TAG, "updateAudioMiniBar() - Set play/pause icon, playing: " + playing);

            if (duration > 0) {
                if (audioMiniSeekBar.getMax() != duration) {
                    audioMiniSeekBar.setMax(duration);
                }
                audioMiniDuration.setText(formatMillis(duration));
            }

            if (!isUserSeeking && position >= 0) {
                audioMiniSeekBar.setProgress(position);
                audioMiniCurrentTime.setText(formatMillis(position));
            }
        } else {
            Log.d(TAG, "updateAudioMiniBar() - NOT showing mini-player");
            if (!AudioPlayerService.sIsPlaying && AudioPlayerService.sCurrentTitle == null) {
                audioMiniPlayerBar.setVisibility(View.GONE);
            }
        }
    }

    private boolean mediaPlayerExists() {
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
            setNavigationItemState(binding.homeIcon, binding.homeText, false);
            setNavigationItemState(binding.exploreIcon, binding.exploreText, false);
            setNavigationItemState(binding.savedIcon, binding.savedText, false);
            setNavigationItemState(binding.profileIcon, binding.profileText, false);

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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        handleNotificationClick();
    }

    private void handleIntent(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return;
        }

        Uri uri = intent.getData();
        Log.d(TAG, "Deep link received: " + uri.toString());

        if ("newsapp".equals(uri.getScheme()) && "auth".equals(uri.getHost())) {
            String path = uri.getPath();
            if ("/invite".equals(path)) {
                String tokenHash = uri.getQueryParameter("token_hash");
                if (tokenHash != null && !tokenHash.isEmpty()) {
                    handleInviteLink(tokenHash);
                }
            }
        }
    }

    private void handleInviteLink(String tokenHash) {
        Log.d(TAG, "Handling invite link with token: " + tokenHash);

        runOnUiThread(() -> {
            Log.d(TAG, "Creating password setup dialog");
            if (passwordSetupDialog != null && passwordSetupDialog.isShowing()) {
                passwordSetupDialog.dismiss();
            }
            passwordSetupDialog = new PasswordSetupDialog(MainActivity.this, tokenHash, null, null, new PasswordSetupDialog.PasswordSetupCallback() {
                @Override
                public void onPasswordSetupSuccess() {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Password set successfully! Please login.", Toast.LENGTH_SHORT).show();
                        showLoginDialog();
                    });
                }

                @Override
                public void onPasswordSetupError(String errorMessage) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, "Error: " + errorMessage, Toast.LENGTH_LONG).show();
                    });
                }
            });
            Log.d(TAG, "Showing password setup dialog");
            passwordSetupDialog.show();
        });
    }

    private void handleNotificationClick() {
        String articleId = getIntent().getStringExtra("article_id");
        String notificationType = getIntent().getStringExtra("notification_type");

        Log.d(TAG, "handleNotificationClick: articleId=" + articleId + ", type=" + notificationType);
        Log.d(TAG, "Intent extras: " + getIntent().getExtras());

        if (articleId != null && !articleId.isEmpty()) {
            Log.d(TAG, "Found article_id, opening article: " + articleId);
            new Handler().postDelayed(() -> {
                openArticleFromNotification(articleId);
            }, 300);
        } else {
            Log.d(TAG, "No article_id found in intent");
        }
    }

    private void openArticleFromNotification(String articleId) {
        Log.d(TAG, "Opening article from notification: " + articleId);

        try {
            Intent intent = new Intent(this, ArticleDetailActivity.class);
            intent.putExtra("article_id", articleId);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            Log.d(TAG, "Starting ArticleDetailActivity with article_id: " + articleId);
            startActivity(intent);

            Toast.makeText(this, "Opening article...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error opening article from notification", e);
            Toast.makeText(this, "Error opening article", Toast.LENGTH_SHORT).show();
        }
    }

    public void navigateToProfile() {
        navController.navigate(R.id.navigation_profile);
        updateNavigationState(NAV_PROFILE);
    }

    public void navigateToExploreWithChannel(int channelId, String channelName, boolean isFollowing) {
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

        com.example.newsapplication.notifications.NotificationManager.getInstance(this).onUserLoggedIn();

        navController.navigate(R.id.navigation_saved);
        updateNavigationState(NAV_SAVED);
    }

    @Override
    public void onAuthCancelled() {
    }

    private void switchNewsSource() {
        currentSource = "VnExpress".equals(currentSource) ? "Tuổi Trẻ" : "VnExpress";

        Toast.makeText(this, "Switched to " + currentSource, Toast.LENGTH_SHORT).show();

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
        android.app.NotificationManager notificationManager = (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                android.service.notification.StatusBarNotification[] notifications = notificationManager.getActiveNotifications();
                for (android.service.notification.StatusBarNotification notification : notifications) {
                    if (notification.getId() == 1) {
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

    // ✅ THÊM ANNOTATION NÀY CHO onStart()
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");

        // Register broadcast receiver
        IntentFilter filter = new IntentFilter(AudioPlayerService.ACTION_AUDIO_STATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(
                    this,
                    audioStateReceiver,
                    filter,
                    ContextCompat.RECEIVER_NOT_EXPORTED
            );
        } else {
            registerReceiver(audioStateReceiver, filter);
        }


        boolean sIsPlaying = AudioPlayerService.sIsPlaying;
        String sCurrentTitle = AudioPlayerService.sCurrentTitle;
        boolean serviceRunning = isServiceRunning();
        boolean hasNotification = hasActiveNotification();

        Log.d(TAG, "onStart() - sIsPlaying: " + sIsPlaying + ", sCurrentTitle: " + sCurrentTitle +
                ", serviceRunning: " + serviceRunning + ", hasNotification: " + hasNotification);

        boolean hasAudioState = sIsPlaying ||
                (sCurrentTitle != null && !sCurrentTitle.isEmpty()) ||
                serviceRunning ||
                hasNotification;

        Log.d(TAG, "onStart() - hasAudioState: " + hasAudioState);

        if (hasAudioState) {
            Log.d(TAG, "onStart() - Showing mini-player");
            audioMiniPlayerBar.setVisibility(View.VISIBLE);

            if (audioMiniPlayPause != null) audioMiniPlayPause.setVisibility(View.VISIBLE);
            if (audioMiniClose != null) audioMiniClose.setVisibility(View.VISIBLE);
            if (audioMiniTitle != null) audioMiniTitle.setVisibility(View.VISIBLE);
            if (audioMiniSeekBar != null) audioMiniSeekBar.setVisibility(View.VISIBLE);
            if (audioMiniCurrentTime != null) audioMiniCurrentTime.setVisibility(View.VISIBLE);
            if (audioMiniDuration != null) audioMiniDuration.setVisibility(View.VISIBLE);

            if (sCurrentTitle != null && !sCurrentTitle.isEmpty()) {
                audioMiniTitle.setText(sCurrentTitle);
            } else {
                audioMiniTitle.setText("Audio đang phát");
            }
            audioMiniPlayPause.setImageResource(sIsPlaying ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
            Log.d(TAG, "onStart() - Set play/pause icon, sIsPlaying: " + sIsPlaying);

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

        boolean sIsPlaying = AudioPlayerService.sIsPlaying;
        String sCurrentTitle = AudioPlayerService.sCurrentTitle;
        boolean serviceRunning = isServiceRunning();
        boolean hasNotification = hasActiveNotification();

        Log.d(TAG, "onResume() - sIsPlaying: " + sIsPlaying + ", sCurrentTitle: " + sCurrentTitle +
                ", serviceRunning: " + serviceRunning + ", hasNotification: " + hasNotification);

        boolean hasAudio = sIsPlaying ||
                (sCurrentTitle != null && !sCurrentTitle.isEmpty()) ||
                serviceRunning ||
                hasNotification;

        Log.d(TAG, "onResume() - hasAudio: " + hasAudio);

        if (hasAudio) {
            Log.d(TAG, "onResume() - Showing mini-player");
            audioMiniPlayerBar.setVisibility(View.VISIBLE);

            if (audioMiniPlayPause != null) audioMiniPlayPause.setVisibility(View.VISIBLE);
            if (audioMiniClose != null) audioMiniClose.setVisibility(View.VISIBLE);
            if (audioMiniTitle != null) audioMiniTitle.setVisibility(View.VISIBLE);
            if (audioMiniSeekBar != null) audioMiniSeekBar.setVisibility(View.VISIBLE);
            if (audioMiniCurrentTime != null) audioMiniCurrentTime.setVisibility(View.VISIBLE);
            if (audioMiniDuration != null) audioMiniDuration.setVisibility(View.VISIBLE);

            if (sCurrentTitle != null && !sCurrentTitle.isEmpty()) {
                audioMiniTitle.setText(sCurrentTitle);
            } else {
                audioMiniTitle.setText("Audio đang phát");
            }
            audioMiniPlayPause.setImageResource(sIsPlaying ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
            Log.d(TAG, "onResume() - Set play/pause icon, sIsPlaying: " + sIsPlaying);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                Log.d(TAG, "onResume() - Requesting state from service");
                Intent requestIntent = new Intent(MainActivity.this, AudioPlayerService.class);
                requestIntent.setAction(AudioPlayerService.ACTION_SEEK);
                requestIntent.putExtra(AudioPlayerService.EXTRA_SEEK_POSITION, -1);
                startService(requestIntent);
            }, 50);
        } else {
            Log.d(TAG, "onResume() - NOT showing mini-player (no audio state)");
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(audioStateReceiver);
        } catch (Exception e) {
            // Already unregistered
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        // ✅ Handle Google Sign-In result
        GoogleAuthHelper.handleActivityResult(requestCode, resultCode, data);

        // Forward image picker result to EditProfileDialog
        if (editProfileDialog != null) {
            editProfileDialog.handleImageResult(requestCode, resultCode, data);
        }
    }

}
