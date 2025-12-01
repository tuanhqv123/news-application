package com.example.newsapplication.auth;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDialog;

import com.example.newsapplication.R;
import com.example.newsapplication.api.endpoints.UserEndpoints;
import com.example.newsapplication.api.ApiClient;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class ChangePasswordDialog extends AppCompatDialog {

    public interface PasswordChangeListener {
        void onPasswordChanged();
        void onError(String message);
    }

    private PasswordChangeListener listener;
    private UserSessionManager sessionManager;
    private UserEndpoints userEndpoints;
    
    private TextInputEditText currentPasswordEditText;
    private TextInputEditText newPasswordEditText;
    private TextInputEditText confirmNewPasswordEditText;
    private TextView cancelButton;
    private TextView updateButton;

    public ChangePasswordDialog(@NonNull android.content.Context context, UserSessionManager sessionManager, PasswordChangeListener listener) {
        super(context);
        this.listener = listener;
        this.sessionManager = sessionManager;
        this.userEndpoints = new UserEndpoints(new ApiClient(context));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_change_password, null);
        setContentView(view);

        initViews(view);
        setupClickListeners();
    }

    private void initViews(View view) {
        currentPasswordEditText = view.findViewById(R.id.currentPasswordEditText);
        newPasswordEditText = view.findViewById(R.id.newPasswordEditText);
        confirmNewPasswordEditText = view.findViewById(R.id.confirmNewPasswordEditText);
        cancelButton = view.findViewById(R.id.cancelButton);
        updateButton = view.findViewById(R.id.updateButton);
    }

    private void setupClickListeners() {
        cancelButton.setOnClickListener(v -> dismiss());
        updateButton.setOnClickListener(v -> changePassword());
    }

    private void changePassword() {
        String currentPassword = currentPasswordEditText.getText().toString().trim();
        String newPassword = newPasswordEditText.getText().toString().trim();
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString().trim();

        // Validation
        if (currentPassword.isEmpty()) {
            showToast("Please enter current password");
            return;
        }

        if (newPassword.isEmpty()) {
            showToast("Please enter new password");
            return;
        }

        if (newPassword.length() < 6) {
            showToast("Password must be at least 6 characters");
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            showToast("New passwords do not match");
            return;
        }

        if (currentPassword.equals(newPassword)) {
            showToast("New password must be different from current password");
            return;
        }

        // Call API to change password
        changePasswordAPI(currentPassword, newPassword);
    }

    private void changePasswordAPI(String currentPassword, String newPassword) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("current_password", currentPassword);
            requestBody.put("new_password", newPassword);

            userEndpoints.changePassword(requestBody, new ApiClient.ApiCallback<JSONObject>() {
                @Override
                public void onSuccess(com.example.newsapplication.api.ApiResponse<JSONObject> response) {
                    dismiss();
                    if (listener != null) {
                        listener.onPasswordChanged();
                    }
                }

                @Override
                public void onError(com.example.newsapplication.api.ApiResponse<JSONObject> error) {
                    String errorMessage = error.getErrorMessage();
                    if (errorMessage == null || errorMessage.isEmpty()) {
                        errorMessage = "Failed to change password";
                    }
                    if (listener != null) {
                        listener.onError(errorMessage);
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error changing password");
        }
    }

    private void showToast(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
