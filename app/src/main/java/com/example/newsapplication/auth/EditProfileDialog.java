package com.example.newsapplication.auth;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatDialog;

import com.example.newsapplication.R;
import com.example.newsapplication.api.endpoints.MediaEndpoints;
import com.example.newsapplication.auth.UserSessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileDialog extends AppCompatDialog {

    public interface ProfileUpdateListener {
        void onProfileUpdated(String displayName, String avatarBase64);
        void onPasswordChanged();
    }

    private ProfileUpdateListener listener;
    private UserSessionManager sessionManager;
    private MediaEndpoints mediaEndpoints;
    private FragmentManager fragmentManager;
    private Activity activity;
    private com.example.newsapplication.repository.NewsRepository newsRepository;
    
    private ImageView avatarImageView;
    private TextInputEditText displayNameEditText;
    private TextInputEditText emailEditText;
    private TextView changeAvatarText;
    private TextView changePasswordText;
    private TextView cancelButton;
    private TextView saveButton;
    
    private String currentAvatarBase64 = "";
    private static final int PICK_IMAGE_REQUEST = 1;

    public EditProfileDialog(@NonNull Activity activity, FragmentManager fragmentManager, ProfileUpdateListener listener) {
        super(activity);
        this.activity = activity;
        this.listener = listener;
        this.fragmentManager = fragmentManager;
        this.sessionManager = new UserSessionManager(activity);
        this.newsRepository = new com.example.newsapplication.repository.NewsRepository(activity);
        this.mediaEndpoints = new MediaEndpoints(new com.example.newsapplication.api.ApiClient(activity));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_edit_profile, null);
        setContentView(view);

        initViews(view);
        setupClickListeners();
        loadCurrentUserData();
    }

    private void initViews(View view) {
        avatarImageView = view.findViewById(R.id.avatarImageView);
        displayNameEditText = view.findViewById(R.id.displayNameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        changeAvatarText = view.findViewById(R.id.changeAvatarText);
        changePasswordText = view.findViewById(R.id.changePasswordText);
        cancelButton = view.findViewById(R.id.cancelButton);
        saveButton = view.findViewById(R.id.saveButton);
    }

    private void setupClickListeners() {
        changeAvatarText.setOnClickListener(v -> openImagePicker());
        avatarImageView.setOnClickListener(v -> openImagePicker());
        
        changePasswordText.setOnClickListener(v -> showChangePasswordDialog());
        
        cancelButton.setOnClickListener(v -> dismiss());
        
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentUserData() {
        if (sessionManager.isLoggedIn()) {
            String userName = sessionManager.getUserName();
            String email = sessionManager.getUserEmail();
            
            displayNameEditText.setText(userName);
            emailEditText.setText(email);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        if (fragmentManager != null && activity != null) {
            // Launch image picker through the parent activity
            activity.startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    public void handleImageResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                avatarImageView.setImageBitmap(bitmap);
                
                // Convert to base64 for upload
                currentAvatarBase64 = bitmapToBase64(bitmap);
                
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveProfile() {
        String displayName = displayNameEditText.getText().toString().trim();
        
        if (displayName.isEmpty()) {
            Toast.makeText(getContext(), "Display name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        
        android.util.Log.d("EditProfileDialog", "=== Starting saveProfile() ===");
        android.util.Log.d("EditProfileDialog", "Display name: " + displayName);
        android.util.Log.d("EditProfileDialog", "Avatar base64 length: " + (currentAvatarBase64 != null ? currentAvatarBase64.length() : 0));
        
        // Update profile using API
        if (newsRepository != null) {
            android.util.Log.d("EditProfileDialog", "Calling newsRepository.updateProfile()");
            newsRepository.updateProfile(displayName, currentAvatarBase64, new com.example.newsapplication.repository.NewsRepository.RepositoryCallback<org.json.JSONObject>() {
                @Override
                public void onResult(com.example.newsapplication.api.ApiResponse<org.json.JSONObject> response) {
                    android.util.Log.d("EditProfileDialog", "Profile update API response - Success: " + response.isSuccess() + ", Error: " + response.getErrorMessage());
                    
                    if (response.isSuccess()) {
                        // Update local session on success
                        android.util.Log.d("EditProfileDialog", "Profile update successful, updating local session");
                        sessionManager.updateUserName(displayName);
                        
                        // Notify listener
                        if (listener != null) {
                            listener.onProfileUpdated(displayName, currentAvatarBase64);
                        }
                        
                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), "Failed to update profile: " + response.getErrorMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void uploadAvatar(String base64Image) {
        mediaEndpoints.uploadImage(base64Image, new com.example.newsapplication.api.ApiClient.ApiCallback<org.json.JSONObject>() {
            @Override
            public void onSuccess(com.example.newsapplication.api.ApiResponse<org.json.JSONObject> response) {
                // Handle successful upload if needed
                if (response.isSuccess()) {
                    // Could store the new avatar URL in session if API returns it
                }
            }

            @Override
            public void onError(com.example.newsapplication.api.ApiResponse<org.json.JSONObject> error) {
                // Handle upload error
                android.util.Log.e("EditProfile", "Avatar upload failed: " + error.getErrorMessage());
            }
        });
    }

    private void showChangePasswordDialog() {
        ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog(getContext(), sessionManager, new ChangePasswordDialog.PasswordChangeListener() {
            @Override
            public void onPasswordChanged() {
                Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onPasswordChanged();
                }
            }

            @Override
            public void onError(String message) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
        
        changePasswordDialog.show();
    }
}
