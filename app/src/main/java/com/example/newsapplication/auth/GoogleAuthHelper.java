package com.example.newsapplication.auth;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class GoogleAuthHelper {
    private static final String TAG = "GoogleAuthHelper";
    public static final int RC_SIGN_IN = 9001;

    private static final String WEB_CLIENT_ID = "655071533108-16ndgafbdo5s7fdjnsvrqj2r0j6g3319.apps.googleusercontent.com";

    private final Activity activity;
    private GoogleSignInClient googleSignInClient;
    private static GoogleSignInCallback staticCallback;  // ✅ THÊM static variable

    public interface GoogleSignInCallback {
        void onSuccess(String idToken, String nonce);
        void onError(String error);
    }

    public GoogleAuthHelper(Activity activity) {
        this.activity = activity;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(WEB_CLIENT_ID)
                .requestEmail()
                .requestProfile()
                .build();

        googleSignInClient = GoogleSignIn.getClient(activity, gso);
    }

    public void signIn(Activity activity, GoogleSignInCallback callback) {
        try {
            staticCallback = callback;  // ✅ Store callback

            Log.d(TAG, "=== Starting Google Sign-In (Legacy SDK) ===");
            Log.d(TAG, "Web Client ID: " + WEB_CLIENT_ID);

            // Sign out first to force account picker
            googleSignInClient.signOut().addOnCompleteListener(activity, task -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                activity.startActivityForResult(signInIntent, RC_SIGN_IN);
                Log.d(TAG, "✅ Sign-In intent launched");
            });

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to start sign-in", e);
            callback.onError("Failed to start: " + e.getMessage());
        }
    }

    /**
     * ✅ Call this from MainActivity.onActivityResult()
     */
    public static void handleActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_SIGN_IN) {
            Log.d(TAG, "📥 Handling Google Sign-In result, resultCode: " + resultCode);

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                String idToken = account.getIdToken();

                if (idToken != null) {
                    Log.d(TAG, "✅ ID Token received, length: " + idToken.length());

                    if (staticCallback != null) {
                        // ✅ Send null nonce for Legacy SDK
                        staticCallback.onSuccess(idToken, null);
                    }
                } else {
                    Log.e(TAG, "❌ ID Token is null");
                    if (staticCallback != null) {
                        staticCallback.onError("Failed to get ID token");
                    }
                }
            } catch (ApiException e) {
                Log.e(TAG, "❌ Sign-in failed: code=" + e.getStatusCode() + ", msg=" + e.getMessage());
                if (staticCallback != null) {
                    staticCallback.onError("Sign-in failed: " + e.getMessage());
                }
            }
        }
    }

    private static String generateNonce() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
