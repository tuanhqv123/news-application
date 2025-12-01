package com.example.newsapplication.auth;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.newsapplication.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import org.json.JSONObject;

public class AuthenticationDialog extends Dialog {

    public interface AuthCallback {
        void onAuthSuccess();
        void onAuthCancelled();
    }

    private AuthCallback callback;
    private boolean isLoginMode = true;
    private View loginView;
    private View signupView;
    private AuthService authService;

    // Login views
    private TextInputEditText loginEmailEditText;
    private TextInputEditText loginPasswordEditText;
    private TextView forgotPasswordText;
    private TextView signupLinkText;
    private Button loginButton;
    private View googleLoginButton;

    // Signup views
    private TextInputEditText signupNameEditText;
    private TextInputEditText signupEmailEditText;
    private TextInputEditText signupPasswordEditText;
    private TextInputEditText signupConfirmPasswordEditText;
    private TextView loginLinkText;
    private Button signupButton;
    private View googleSignupButton;

    // Common views
    private ImageView closeButton;
    private TextView titleText;

    public AuthenticationDialog(@NonNull Context context, AuthCallback callback) {
        super(context, android.R.style.Theme_Material_Light_Dialog);
        this.callback = callback;
        this.authService = new AuthService(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_login);

        initLoginViews();
        setupLoginListeners();
    }

    private void initLoginViews() {
        titleText = findViewById(R.id.loginTitle);
        closeButton = findViewById(R.id.closeButton);
        loginEmailEditText = findViewById(R.id.emailEditText);
        loginPasswordEditText = findViewById(R.id.passwordEditText);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        signupLinkText = findViewById(R.id.signupLinkText);
        loginButton = findViewById(R.id.loginButton);
        googleLoginButton = findViewById(R.id.googleLoginButton);
    }

    private void initSignupViews() {
        titleText = findViewById(R.id.signupTitle);
        closeButton = findViewById(R.id.closeButton);
        signupNameEditText = findViewById(R.id.nameEditText);
        signupEmailEditText = findViewById(R.id.emailEditText);
        signupPasswordEditText = findViewById(R.id.passwordEditText);
        signupConfirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        loginLinkText = findViewById(R.id.loginLinkText);
        signupButton = findViewById(R.id.signupButton);
        googleSignupButton = findViewById(R.id.googleSignupButton);
    }

    private void setupLoginListeners() {
        closeButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onAuthCancelled();
            }
            dismiss();
        });

        loginButton.setOnClickListener(v -> attemptLogin());

        signupLinkText.setOnClickListener(v -> switchToSignup());

        forgotPasswordText.setOnClickListener(v -> {
            // TODO: Implement forgot password functionality
            Toast.makeText(getContext(), "Forgot password functionality coming soon!", Toast.LENGTH_SHORT).show();
        });

        // Google login button
        googleLoginButton.setOnClickListener(v -> {
            // TODO: Implement Google Sign-In
            Toast.makeText(getContext(), "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupSignupListeners() {
        closeButton.setOnClickListener(v -> {
            if (callback != null) {
                callback.onAuthCancelled();
            }
            dismiss();
        });

        signupButton.setOnClickListener(v -> attemptSignup());

        loginLinkText.setOnClickListener(v -> switchToLogin());

        // Google signup button
        googleSignupButton.setOnClickListener(v -> {
            // TODO: Implement Google Sign-In
            Toast.makeText(getContext(), "Google Sign-In coming soon!", Toast.LENGTH_SHORT).show();
        });
    }

    private void switchToSignup() {
        isLoginMode = false;
        setContentView(R.layout.dialog_signup);
        initSignupViews();
        setupSignupListeners();
    }

    private void switchToLogin() {
        isLoginMode = true;
        setContentView(R.layout.dialog_login);
        initLoginViews();
        setupLoginListeners();
    }

    private void attemptLogin() {
        String email = loginEmailEditText.getText().toString().trim();
        String password = loginPasswordEditText.getText().toString().trim();

        if (validateLoginInputs(email, password)) {
            loginButton.setEnabled(false);
            loginButton.setText("Logging in...");
            
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
                    loginButton.setEnabled(true);
                    loginButton.setText("Login");
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void attemptSignup() {
        String name = signupNameEditText.getText().toString().trim();
        String email = signupEmailEditText.getText().toString().trim();
        String password = signupPasswordEditText.getText().toString().trim();
        String confirmPassword = signupConfirmPasswordEditText.getText().toString().trim();

        if (validateSignupInputs(name, email, password, confirmPassword)) {
            signupButton.setEnabled(false);
            signupButton.setText("Signing up...");
            
            authService.register(email, password, name, new AuthService.AuthResultCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    Toast.makeText(getContext(), "Registration successful! Please login.", Toast.LENGTH_SHORT).show();
                    switchToLogin();
                    signupButton.setEnabled(true);
                    signupButton.setText("Sign Up");
                }

                @Override
                public void onError(String errorMessage) {
                    signupButton.setEnabled(true);
                    signupButton.setText("Sign Up");
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validateLoginInputs(String email, String password) {
        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean validateSignupInputs(String name, String email, String password, String confirmPassword) {
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your name", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (email.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(getContext(), "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.isEmpty()) {
            Toast.makeText(getContext(), "Please enter your password", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}