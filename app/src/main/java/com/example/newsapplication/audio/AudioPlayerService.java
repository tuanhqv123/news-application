package com.example.newsapplication.audio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.media.app.NotificationCompat.MediaStyle;

import com.example.newsapplication.MainActivity;
import com.example.newsapplication.R;

import java.io.IOException;
import java.util.Locale;

public class AudioPlayerService extends Service {
    private static final String TAG = "AudioPlayerService";
    private static final String CHANNEL_ID = "audio_player_channel";
    private static final int NOTIFICATION_ID = 1;
    
    public static final String ACTION_PLAY = "com.example.newsapplication.ACTION_PLAY";
    public static final String ACTION_PAUSE = "com.example.newsapplication.ACTION_PAUSE";
    public static final String ACTION_STOP = "com.example.newsapplication.ACTION_STOP";
    public static final String ACTION_SEEK = "com.example.newsapplication.ACTION_SEEK";
    public static final String ACTION_SKIP_FORWARD = "com.example.newsapplication.ACTION_SKIP_FORWARD";
    public static final String ACTION_SKIP_BACKWARD = "com.example.newsapplication.ACTION_SKIP_BACKWARD";
    public static final String ACTION_AUDIO_STATE = "com.example.newsapplication.ACTION_AUDIO_STATE";
    
    private static final int SKIP_DURATION_MS = 10000; // 10 seconds
    
    public static final String EXTRA_AUDIO_URL = "audio_url";
    public static final String EXTRA_AUDIO_TITLE = "audio_title";
    public static final String EXTRA_DURATION_MS = "duration_ms";
    public static final String EXTRA_SEEK_POSITION = "seek_position";
    
    public static final String EXTRA_IS_PLAYING = "is_playing";
    public static final String EXTRA_POSITION_MS = "position_ms";
    public static final String EXTRA_DURATION_MS_BROADCAST = "duration_ms_broadcast";
    
    private MediaPlayer mediaPlayer;
    private String currentAudioUrl;
    private String currentTitle;
    private int currentDurationMs = 0; // Duration from API (tts_duration_seconds * 1000)
    private boolean isPlaying = false;
    
    // Static variables to hold global state
    public static boolean sIsPlaying = false;
    public static String sCurrentTitle = null;
    
    private Handler progressHandler;
    private Runnable progressRunnable;
    private int lastBroadcastPosition = -1;
    private boolean lastBroadcastPlaying = false;
    
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        progressHandler = new Handler(Looper.getMainLooper());
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case ACTION_PLAY:
                        handlePlay(intent);
                        break;
                    case ACTION_PAUSE:
                        handlePause(intent);
                        break;
                    case ACTION_STOP:
                        handleStop();
                        break;
                    case ACTION_SEEK:
                        handleSeek(intent);
                        break;
                    case ACTION_SKIP_FORWARD:
                        handleSkipForward();
                        break;
                    case ACTION_SKIP_BACKWARD:
                        handleSkipBackward();
                        break;
                }
            }
        }
        return START_NOT_STICKY;
    }
    
    private void handlePlay(Intent intent) {
        String audioUrl = intent.getStringExtra(EXTRA_AUDIO_URL);
        String title = intent.getStringExtra(EXTRA_AUDIO_TITLE);
        int durationMs = intent.getIntExtra(EXTRA_DURATION_MS, 0);
        
        // If mediaPlayer exists and is paused, just resume without needing URL
        if (mediaPlayer != null && currentAudioUrl != null && !isPlaying) {
            Log.d(TAG, "handlePlay() - Resuming existing mediaPlayer");
            try {
                mediaPlayer.start();
                isPlaying = true;
                sIsPlaying = true;
                startProgressUpdates();
                updateNotification();
                broadcastAudioState();
                return;
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error resuming MediaPlayer, will reload", e);
                // Fall through to reload
            }
        }
        
        // If same audio is already loaded and playing, do nothing
        if (mediaPlayer != null && currentAudioUrl != null && currentAudioUrl.equals(audioUrl) && isPlaying) {
            return;
        }
        
        if (audioUrl == null || audioUrl.isEmpty()) {
            // If no URL but mediaPlayer exists, try to resume
            if (mediaPlayer != null && !isPlaying) {
                Log.d(TAG, "handlePlay() - No URL provided, but mediaPlayer exists, trying to resume");
                try {
                    mediaPlayer.start();
                    isPlaying = true;
                    sIsPlaying = true;
                    startProgressUpdates();
                    updateNotification();
                    broadcastAudioState();
                    return;
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Error resuming MediaPlayer without URL", e);
                }
            }
            Log.e(TAG, "Audio URL is null or empty and cannot resume");
            return;
        }
        
        // Ensure title is not null
        if (title == null) {
            title = "Audio";
        }
        
        // Release existing player if different audio
        if (mediaPlayer != null) {
            releaseMediaPlayer();
        }
        
        currentAudioUrl = audioUrl;
        currentTitle = title != null ? title : "Audio";
        currentDurationMs = durationMs;
        
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
            
            mediaPlayer.setOnPreparedListener(mp -> {
                // Use API duration if provided, otherwise try MediaPlayer duration
                if (currentDurationMs > 0) {
                    // Use API duration
                } else {
                    int mpDuration = mp.getDuration();
                    if (mpDuration > 0) {
                        currentDurationMs = mpDuration;
                    }
                }
                
                mp.start();
                isPlaying = true;
                sIsPlaying = true;
                sCurrentTitle = currentTitle;
                
                Log.d(TAG, "handlePlay() - Audio started - sIsPlaying: " + sIsPlaying + ", sCurrentTitle: " + sCurrentTitle);
                
                startForeground(NOTIFICATION_ID, buildNotification());
                startProgressUpdates();
                broadcastAudioState();
            });
            
            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                sIsPlaying = false;
                stopProgressUpdates();
                broadcastAudioState();
                updateNotification();
            });
            
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "MediaPlayer error: what=" + what + ", extra=" + extra);
                isPlaying = false;
                sIsPlaying = false;
                stopProgressUpdates();
                broadcastAudioState();
                return true;
            });
            
        } catch (IOException e) {
            Log.e(TAG, "Error setting data source", e);
            isPlaying = false;
            sIsPlaying = false;
        }
    }
    
    private void handlePause(Intent intent) {
        Log.d(TAG, "handlePause() called - isPlaying: " + isPlaying + ", currentTitle: " + currentTitle);
        if (mediaPlayer != null) {
            if (isPlaying) {
                // Currently playing, pause it
                mediaPlayer.pause();
                isPlaying = false;
                sIsPlaying = false;
                // Keep sCurrentTitle so mini-player can still show when back
                if (sCurrentTitle == null && currentTitle != null) {
                    sCurrentTitle = currentTitle;
                }
                Log.d(TAG, "handlePause() - Paused - sIsPlaying: " + sIsPlaying + ", sCurrentTitle: " + sCurrentTitle);
                stopProgressUpdates();
                updateNotification();
                broadcastAudioState();
            } else {
                // Currently paused, resume it
                try {
                    mediaPlayer.start();
                    isPlaying = true;
                    sIsPlaying = true;
                    // Ensure sCurrentTitle is set
                    if (currentTitle != null) {
                        sCurrentTitle = currentTitle;
                    }
                    startProgressUpdates();
                    updateNotification();
                    broadcastAudioState();
                } catch (IllegalStateException e) {
                    Log.e(TAG, "Error resuming playback, trying to reload", e);
                    // If MediaPlayer is in invalid state, try to reload
                    String audioUrl = intent != null ? intent.getStringExtra(EXTRA_AUDIO_URL) : currentAudioUrl;
                    String title = intent != null ? intent.getStringExtra(EXTRA_AUDIO_TITLE) : currentTitle;
                    int duration = intent != null ? intent.getIntExtra(EXTRA_DURATION_MS, currentDurationMs) : currentDurationMs;
                    
                    if (audioUrl != null && !audioUrl.isEmpty()) {
                        Intent playIntent = new Intent(ACTION_PLAY);
                        playIntent.putExtra(EXTRA_AUDIO_URL, audioUrl);
                        playIntent.putExtra(EXTRA_AUDIO_TITLE, title != null ? title : "Audio");
                        playIntent.putExtra(EXTRA_DURATION_MS, duration);
                        handlePlay(playIntent);
                    }
                }
            }
        } else if (intent != null) {
            // MediaPlayer is null, try to reload from intent
            String audioUrl = intent.getStringExtra(EXTRA_AUDIO_URL);
            String title = intent.getStringExtra(EXTRA_AUDIO_TITLE);
            int duration = intent.getIntExtra(EXTRA_DURATION_MS, 0);
            
            if (audioUrl != null && !audioUrl.isEmpty()) {
                Intent playIntent = new Intent(ACTION_PLAY);
                playIntent.putExtra(EXTRA_AUDIO_URL, audioUrl);
                playIntent.putExtra(EXTRA_AUDIO_TITLE, title != null ? title : "Audio");
                playIntent.putExtra(EXTRA_DURATION_MS, duration);
                handlePlay(playIntent);
            }
        }
    }
    
    private void handlePause() {
        handlePause(null);
    }
    
    private void handleStop() {
        releaseMediaPlayer();
        // Clear all state when explicitly stopped
        currentAudioUrl = null;
        currentTitle = null;
        currentDurationMs = 0;
        sCurrentTitle = null;
        sIsPlaying = false;
        stopForeground(true);
        stopSelf();
    }
    
    private void handleSeek(Intent intent) {
        int position = intent.getIntExtra(EXTRA_SEEK_POSITION, 0);
        Log.d(TAG, "handleSeek() called with position: " + position);
        if (position == -1) {
            // Special value: just trigger broadcast without seeking
            Log.d(TAG, "handleSeek() - position is -1, triggering broadcast");
            Log.d(TAG, "handleSeek() - current state: isPlaying=" + isPlaying + ", currentTitle=" + currentTitle + 
                  ", sIsPlaying=" + sIsPlaying + ", sCurrentTitle=" + sCurrentTitle);
            // Reset last broadcast position to force broadcast
            lastBroadcastPosition = -1;
            lastBroadcastPlaying = !isPlaying; // Force state change
            // Always broadcast, even if paused or no mediaPlayer
            broadcastAudioState();
            return;
        }
        if (mediaPlayer != null && position >= 0) {
            try {
                mediaPlayer.seekTo(position);
                broadcastAudioState();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error seeking", e);
            }
        } else if (position == -1) {
            // If no mediaPlayer but position is -1, still broadcast state
            broadcastAudioState();
        }
    }
    
    private void handleSkipForward() {
        if (mediaPlayer != null) {
            try {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int newPosition = currentPosition + SKIP_DURATION_MS;
                int duration = currentDurationMs > 0 ? currentDurationMs : mediaPlayer.getDuration();
                if (duration > 0 && newPosition > duration) {
                    newPosition = duration;
                }
                mediaPlayer.seekTo(newPosition);
                broadcastAudioState();
                updateNotification();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error skipping forward", e);
            }
        }
    }
    
    private void handleSkipBackward() {
        if (mediaPlayer != null) {
            try {
                int currentPosition = mediaPlayer.getCurrentPosition();
                int newPosition = currentPosition - SKIP_DURATION_MS;
                if (newPosition < 0) {
                    newPosition = 0;
                }
                mediaPlayer.seekTo(newPosition);
                broadcastAudioState();
                updateNotification();
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error skipping backward", e);
            }
        }
    }
    
    private void startProgressUpdates() {
        stopProgressUpdates();
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && isPlaying) {
                    try {
                        int position = mediaPlayer.getCurrentPosition();
                        broadcastAudioState();
                        updateNotification();
                        progressHandler.postDelayed(this, 1000); // Update every second
                    } catch (IllegalStateException e) {
                        // MediaPlayer is in invalid state, stop updates
                        stopProgressUpdates();
                    } catch (Exception e) {
                        // Silently stop updates on any error
                        stopProgressUpdates();
                    }
                } else {
                    // MediaPlayer is null or not playing, stop updates
                    stopProgressUpdates();
                }
            }
        };
        if (progressHandler != null) {
            progressHandler.post(progressRunnable);
        }
    }
    
    private void stopProgressUpdates() {
        if (progressRunnable != null) {
            progressHandler.removeCallbacks(progressRunnable);
            progressRunnable = null;
        }
    }
    
    private void broadcastAudioState() {
        Log.d(TAG, "broadcastAudioState() called");
        // Broadcast if we have any audio state (playing, paused, or has title)
        // Don't early return if paused - we still need to broadcast state
        // Also check static variables to ensure we broadcast even if instance variables are cleared
        boolean hasAudioState = mediaPlayer != null || currentTitle != null || currentAudioUrl != null || 
                               sCurrentTitle != null || sIsPlaying;
        
        Log.d(TAG, "broadcastAudioState() - hasAudioState: " + hasAudioState + 
              ", mediaPlayer: " + (mediaPlayer != null) + 
              ", currentTitle: " + currentTitle + 
              ", currentAudioUrl: " + currentAudioUrl + 
              ", sCurrentTitle: " + sCurrentTitle + 
              ", sIsPlaying: " + sIsPlaying);
        
        if (!hasAudioState) {
            Log.d(TAG, "broadcastAudioState() - No audio state, returning early");
            return;
        }
        
        // If currentTitle is null but sCurrentTitle exists, use it
        if (currentTitle == null && sCurrentTitle != null) {
            currentTitle = sCurrentTitle;
            Log.d(TAG, "broadcastAudioState() - Restored currentTitle from sCurrentTitle: " + currentTitle);
        }
        
        try {
            Intent broadcast = new Intent(ACTION_AUDIO_STATE);
            // Set package to ensure broadcast is delivered correctly
            String packageName = getPackageName();
            if (packageName == null || packageName.isEmpty()) {
                // Service may be destroyed, stop updates
                stopProgressUpdates();
                return;
            }
            broadcast.setPackage(packageName);
            broadcast.putExtra(EXTRA_IS_PLAYING, isPlaying);
            
            int position = 0;
            int duration = currentDurationMs;
            
            if (mediaPlayer != null) {
                try {
                    position = mediaPlayer.getCurrentPosition();
                    // Only use MediaPlayer duration if API duration is not available
                    if (duration == 0) {
                        int mpDuration = mediaPlayer.getDuration();
                        if (mpDuration > 0) {
                            duration = mpDuration;
                            currentDurationMs = duration;
                        }
                    }
                } catch (IllegalStateException e) {
                    // MediaPlayer is in invalid state, but still broadcast with last known position
                    // Don't return - we still want to broadcast state
                } catch (Exception e) {
                    // Still broadcast even on error
                }
            }
            
            // Only broadcast if there's a significant change (every 500ms or state change)
            // But always broadcast if it's a state change or first broadcast
            int positionDiff = Math.abs(position - lastBroadcastPosition);
            boolean stateChanged = (isPlaying != lastBroadcastPlaying);
            
            // Always broadcast on state change or first time
            // For position updates, only broadcast if significant change (500ms) or first time
            if (!stateChanged && positionDiff < 500 && lastBroadcastPosition >= 0) {
                // Skip broadcast if no significant change (but allow through if first time)
                return;
            }
            
            lastBroadcastPosition = position;
            lastBroadcastPlaying = isPlaying;
            
            broadcast.putExtra(EXTRA_POSITION_MS, position);
            broadcast.putExtra(EXTRA_DURATION_MS_BROADCAST, duration);
            // Ensure title is not null - always use a non-null string
            String titleToSend = (currentTitle != null && !currentTitle.isEmpty()) ? currentTitle : "Audio";
            broadcast.putExtra(EXTRA_AUDIO_TITLE, titleToSend);
            
            // Update static variable to keep it in sync
            if (currentTitle != null && !currentTitle.isEmpty()) {
                sCurrentTitle = currentTitle;
            }
            
            Log.d(TAG, "broadcastAudioState() - Sending broadcast - playing: " + isPlaying + 
                  ", position: " + position + ", duration: " + duration + ", title: " + titleToSend);
            sendBroadcast(broadcast);
            Log.d(TAG, "broadcastAudioState() - Broadcast sent successfully");
        } catch (SecurityException | IllegalStateException e) {
            // App may have been destroyed or service is in invalid state, stop updates
            stopProgressUpdates();
        } catch (Exception e) {
            // Silently ignore other exceptions - these are usually harmless
            // (app destroyed, no receivers, etc.) and don't affect functionality
        }
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Audio Player",
                NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Audio playback notification");
            channel.setShowBadge(false);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Play/Pause action - use ACTION_PAUSE as toggle (it handles both play and pause)
        Intent toggleIntent = new Intent(ACTION_PAUSE);
        toggleIntent.setClass(this, AudioPlayerService.class);
        // Add audio info in case we need to resume
        if (currentAudioUrl != null) {
            toggleIntent.putExtra(EXTRA_AUDIO_URL, currentAudioUrl);
        }
        if (currentTitle != null) {
            toggleIntent.putExtra(EXTRA_AUDIO_TITLE, currentTitle);
        }
        toggleIntent.putExtra(EXTRA_DURATION_MS, currentDurationMs);
        PendingIntent togglePendingIntent = PendingIntent.getService(
            this, 1, toggleIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Stop action
        Intent stopIntent = new Intent(ACTION_STOP);
        stopIntent.setClass(this, AudioPlayerService.class);
        PendingIntent stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Skip backward action
        Intent skipBackwardIntent = new Intent(ACTION_SKIP_BACKWARD);
        skipBackwardIntent.setClass(this, AudioPlayerService.class);
        PendingIntent skipBackwardPendingIntent = PendingIntent.getService(
            this, 0, skipBackwardIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Skip forward action
        Intent skipForwardIntent = new Intent(ACTION_SKIP_FORWARD);
        skipForwardIntent.setClass(this, AudioPlayerService.class);
        PendingIntent skipForwardPendingIntent = PendingIntent.getService(
            this, 0, skipForwardIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        int position = 0;
        int duration = currentDurationMs;
        
        if (mediaPlayer != null) {
            try {
                position = mediaPlayer.getCurrentPosition();
                if (duration == 0) {
                    int mpDuration = mediaPlayer.getDuration();
                    if (mpDuration > 0) {
                        duration = mpDuration;
                        currentDurationMs = duration;
                    }
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error getting position/duration for notification", e);
            }
        }
        
        String timeText = formatTime(position) + " / " + formatTime(duration);
        String displayTitle = (currentTitle != null && !currentTitle.isEmpty()) ? currentTitle : "Audio";
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_play_circle)
            .setContentTitle(displayTitle)
            .setContentText(timeText)
            .setContentIntent(pendingIntent)
            .setOngoing(isPlaying)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(
                android.R.drawable.ic_media_rew,
                "Rewind 10s",
                skipBackwardPendingIntent
            )
            .addAction(
                isPlaying ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle,
                isPlaying ? "Pause" : "Play",
                togglePendingIntent
            )
            .addAction(
                android.R.drawable.ic_media_ff,
                "Forward 10s",
                skipForwardPendingIntent
            )
            .addAction(
                R.drawable.ic_close,
                "Stop",
                stopPendingIntent
            )
            .setStyle(new MediaStyle()
                .setShowActionsInCompactView(1, 2)); // Show play/pause and forward in compact view
        
        // Add progress bar if duration is available
        if (duration > 0) {
            builder.setProgress(duration, position, false);
        }
        
        return builder.build();
    }
    
    private void updateNotification() {
        if (isPlaying || mediaPlayer != null) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify(NOTIFICATION_ID, buildNotification());
            }
        }
    }
    
    private String formatTime(int millis) {
        if (millis <= 0) {
            return "00:00";
        }
        int totalSeconds = millis / 1000;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }
    
    private void releaseMediaPlayer() {
        stopProgressUpdates();
        if (mediaPlayer != null) {
            try {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                }
                mediaPlayer.release();
            } catch (Exception e) {
                Log.e(TAG, "Error releasing MediaPlayer", e);
            }
            mediaPlayer = null;
        }
        isPlaying = false;
        sIsPlaying = false;
        // Don't clear currentAudioUrl, currentTitle, and currentDurationMs here
        // They should be kept so we can resume playback
        // Only clear them in handleStop() when user explicitly stops
        lastBroadcastPosition = -1;
        lastBroadcastPlaying = false;
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called - Clearing static state");
        // Stop all updates before destroying
        stopProgressUpdates();
        releaseMediaPlayer();
        // Clear static state
        sIsPlaying = false;
        sCurrentTitle = null;
        Log.d(TAG, "onDestroy() - Static state cleared - sIsPlaying: " + sIsPlaying + ", sCurrentTitle: " + sCurrentTitle);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

