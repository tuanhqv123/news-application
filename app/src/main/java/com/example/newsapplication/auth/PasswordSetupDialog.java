package com.example.newsapplication.auth;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.newsapplication.R;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.api.endpoints.AuthEndpoints;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

public class PasswordSetupDialog extends Dialog {
    public interface PasswordSetupCallback {
        void onPasswordSetupSuccess();
        void onPasswordSetupError(String errorMessage);
    }

    private PasswordSetupCallback callback;
    private AuthEndpoints authEndpoints;
    private String tokenHash;
    private String userId;
    private String email;

    // UI components
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private Button setupButton;
    private ProgressBar progressBar;
    private TextView errorText;
    private TextView emailText;
    private ImageView closeButton;

    public PasswordSetupDialog(@NonNull Context context, String tokenHash, String userId, String email, PasswordSetupCallback callback) {
        super(context, android.R.style.Theme_Material_Light_Dialog);
        this.tokenHash = tokenHash;
        this.userId = userId;
        this.email = email;
        this.callback = callback;
        this.authEndpoints = new AuthEndpoints(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_password_setup);

        initViews();
        setupListeners();
        setEmailDisplay();
    }

    private void initViews() {
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);
        confirmPasswordInputLayout = findViewById(R.id.confirmPasswordInputLayout);
        setupButton = findViewById(R.id.setupButton);
        progressBar = findViewById(R.id.progressBar);
        errorText = findViewById(R.id.errorText);
        emailText = findViewById(R.id.emailText);
        closeButton = findViewById(R.id.closeButton);
    }

    private void setupListeners() {
        setupButton.setOnClickListener(v -> attemptPasswordSetup());
        closeButton.setOnClickListener(v -> dismiss());

        // Enable/disable button based on password fields
        View.OnFocusChangeListener focusChangeListener = (v, hasFocus) -> validatePasswords();
        passwordEditText.setOnFocusChangeListener(focusChangeListener);
        confirmPasswordEditText.setOnFocusChangeListener(focusChangeListener);

        // Add TextWatchers for real-time validation
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswords();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        passwordEditText.addTextChangedListener(textWatcher);
        confirmPasswordEditText.addTextChangedListener(textWatcher);
    }

    private void setEmailDisplay() {
        if (email != null && !email.isEmpty()) {
            emailText.setText(email);
            // Make sure both email label and text are visible
            emailText.setVisibility(View.VISIBLE);
            // The email label is a separate TextView above emailText in the layout
        } else {
            // Hide email section if not available
            emailText.setVisibility(View.GONE);
        }
    }

    private void validatePasswords() {
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        boolean isValid = !password.isEmpty() &&
                         !confirmPassword.isEmpty() &&
                         password.equals(confirmPassword) &&
                         password.length() >= 6;

        setupButton.setEnabled(isValid);

        // Clear errors
        passwordInputLayout.setError(null);
        confirmPasswordInputLayout.setError(null);
        errorText.setVisibility(View.GONE);
    }

    private void attemptPasswordSetup() {
        String password = passwordEditText.getText().toString().trim();
        String confirmPassword = confirmPasswordEditText.getText().toString().trim();

        android.util.Log.d("PasswordSetupDialog", "Attempting password setup");
        android.util.Log.d("PasswordSetupDialog", "Password: " + (password.isEmpty() ? "empty" : "filled (" + password.length() + " chars)"));
        android.util.Log.d("PasswordSetupDialog", "Confirm Password: " + (confirmPassword.isEmpty() ? "empty" : "filled"));

        // Validate passwords
        if (password.isEmpty()) {
            android.util.Log.d("PasswordSetupDialog", "Password is empty");
            passwordInputLayout.setError("Password is required");
            return;
        }

        if (password.length() < 6) {
            android.util.Log.d("PasswordSetupDialog", "Password too short: " + password.length());
            passwordInputLayout.setError("Password must be at least 6 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            android.util.Log.d("PasswordSetupDialog", "Passwords do not match");
            confirmPasswordInputLayout.setError("Passwords do not match");
            return;
        }

        android.util.Log.d("PasswordSetupDialog", "Password validation passed, calling API");
        android.util.Log.d("PasswordSetupDialog", "TokenHash: " + tokenHash);
        android.util.Log.d("PasswordSetupDialog", "UserId: " + userId);

        // Show loading state
        setLoading(true);

        // Call setup password API
        // Use simplified flow - no userId needed
        authEndpoints.setupPassword(password, tokenHash, new com.example.newsapplication.api.ApiClient.ApiCallback<JSONObject>() {
            @Override
            public void onSuccess(ApiResponse<JSONObject> response) {
                android.util.Log.d("PasswordSetupDialog", "Setup password API success");
                setLoading(false);
                Toast.makeText(getContext(), "Password set successfully!", Toast.LENGTH_SHORT).show();

                if (callback != null) {
                    callback.onPasswordSetupSuccess();
                }
                dismiss();
            }

            @Override
            public void onError(ApiResponse<JSONObject> error) {
                android.util.Log.d("PasswordSetupDialog", "Setup password API error: " + error.getErrorMessage());
                android.util.Log.d("PasswordSetupDialog", "Error status code: " + error.getStatusCode());
                setLoading(false);
                String errorMessage = error.getErrorMessage();

                if (errorMessage == null || errorMessage.isEmpty()) {
                    errorMessage = "Failed to setup password. Please try again.";
                }

                errorText.setText("Error: " + errorMessage);
                errorText.setVisibility(View.VISIBLE);

                if (callback != null) {
                    callback.onPasswordSetupError(errorMessage);
                }
            }
        });
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            setupButton.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            passwordEditText.setEnabled(false);
            confirmPasswordEditText.setEnabled(false);
        } else {
            setupButton.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            passwordEditText.setEnabled(true);
            confirmPasswordEditText.setEnabled(true);
        }
    }
}