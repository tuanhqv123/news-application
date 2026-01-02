package com.example.newsapplication.auth;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;

import com.example.newsapplication.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class AuthenticationDialog extends Dialog {
    private static final String TAG = "AuthDialog";

    private AuthCallback callback;
    private AuthService authService;
    private Activity activity;
    private boolean isLoginMode = true; // Default login

    // Views (sẽ init tùy mode)
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText nameEditText;
    private Button actionButton;
    private LinearLayout googleButton;
    private TextView switchModeText;
    private ImageView closeButton;

    public interface AuthCallback {
        void onAuthSuccess();
        void onAuthCancelled();
    }

    public AuthenticationDialog(@NonNull Context context, AuthCallback callback) {
        this(context, callback, true); // Default login mode
    }

    public AuthenticationDialog(@NonNull Context context, AuthCallback callback, boolean isLoginMode) {
        super(context);
        this.callback = callback;
        this.isLoginMode = isLoginMode;
        this.activity = getActivityFromContext(context);
    }

    private Activity getActivityFromContext(Context context) {
        while (context instanceof android.content.ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((android.content.ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (isLoginMode) {
            setContentView(R.layout.dialog_login);
        } else {
            setContentView(R.layout.dialog_signup);
        }

        authService = new AuthService(getContext());
        initViews();
        setupClickListeners();
    }

    private void initViews() {
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        closeButton = findViewById(R.id.closeButton);

        if (isLoginMode) {
            // Login views
            actionButton = findViewById(R.id.loginButton);
            googleButton = findViewById(R.id.googleLoginButton);
            switchModeText = findViewById(R.id.signupLinkText);
        } else {
            // Signup views
            nameEditText = findViewById(R.id.nameEditText);
            actionButton = findViewById(R.id.signupButton);
            googleButton = findViewById(R.id.googleSignupButton);
            switchModeText = findViewById(R.id.loginLinkText);
        }
    }

    private void setupClickListeners() {
        actionButton.setOnClickListener(v -> handleEmailPasswordAuth());
        googleButton.setOnClickListener(v -> handleGoogleSignIn());

        switchModeText.setOnClickListener(v -> {
            dismiss();
            // Mở dialog mới với mode đảo ngược
            AuthenticationDialog newDialog = new AuthenticationDialog(getContext(), callback, !isLoginMode);
            newDialog.show();
        });

        closeButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onAuthCancelled();
            }
            dismiss();
        });
    }

    private void handleEmailPasswordAuth() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        actionButton.setEnabled(false);

        if (isLoginMode) {
            // Login
            authService.login(email, password, new AuthService.AuthResultCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    if (callback != null) {
                        callback.onAuthSuccess();
                    }
                    dismiss();
                }

                @Override
                public void onError(String errorMessage) {
                    actionButton.setEnabled(true);
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Signup
            String name = nameEditText.getText().toString().trim();
            authService.register(email, password, name, new AuthService.AuthResultCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Toast.makeText(getContext(), "Registration successful! Please check your email.", Toast.LENGTH_LONG).show();

                    // Auto switch to login
                    dismiss();
                    AuthenticationDialog loginDialog = new AuthenticationDialog(getContext(), callback, true);
                    loginDialog.show();
                }

                @Override
                public void onError(String errorMessage) {
                    actionButton.setEnabled(true);
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handleGoogleSignIn() {
        if (activity == null) {
            Toast.makeText(getContext(), "Cannot perform Google Sign-In", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "Starting Google Sign-In");
        GoogleAuthHelper googleAuthHelper = new GoogleAuthHelper(activity);
        googleButton.setEnabled(false);

        googleAuthHelper.signIn(activity, new GoogleAuthHelper.GoogleSignInCallback() {
            @Override
            public void onSuccess(String idToken, String nonce) {
                Log.d(TAG, "ID Token received");

                authService.loginWithGoogle(idToken, nonce, new AuthService.AuthResultCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        Log.d(TAG, "Backend login successful");

                        if (activity != null) {
                            activity.runOnUiThread(() -> {
                                if (callback != null) {
                                    callback.onAuthSuccess();
                                }
                                dismiss();
                            });
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Backend error: " + errorMessage);

                        if (activity != null) {
                            activity.runOnUiThread(() -> {
                                googleButton.setEnabled(true);
                                Toast.makeText(getContext(), "Login failed: " + errorMessage, Toast.LENGTH_LONG).show();
                            });
                        }
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Google Sign-In error: " + error);

                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        googleButton.setEnabled(true);
                        Toast.makeText(getContext(), "Google Sign-In failed: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }


}
