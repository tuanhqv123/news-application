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
import com.example.newsapplication.auth.UserSessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;
import com.example.newsapplication.utils.CircleTransform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EditProfileDialog extends AppCompatDialog {

    public interface ProfileUpdateListener {
        void onProfileUpdated(String displayName, String avatarBase64);
    }

    private ProfileUpdateListener listener;
    private UserSessionManager sessionManager;
    private FragmentManager fragmentManager;
    private Activity activity;
    private com.example.newsapplication.repository.NewsRepository newsRepository;
    private com.example.newsapplication.api.endpoints.MediaEndpoints mediaEndpoints;
    
    private ImageView avatarImageView;
    private TextInputEditText displayNameEditText;
    private TextInputEditText emailEditText;
    private TextView changeAvatarText;
    private TextView cancelButton;
    private TextView saveButton;
    
    private String currentAvatarUrl = "";
    private Uri selectedImageUri = null;
    private static final int PICK_IMAGE_REQUEST = 1;

    public EditProfileDialog(@NonNull Activity activity, FragmentManager fragmentManager, ProfileUpdateListener listener) {
        super(activity);
        this.activity = activity;
        this.listener = listener;
        this.fragmentManager = fragmentManager;
        this.sessionManager = new UserSessionManager(activity);
        this.newsRepository = new com.example.newsapplication.repository.NewsRepository(activity);
        this.mediaEndpoints = new com.example.newsapplication.api.endpoints.MediaEndpoints(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.dialog_edit_profile, null);
        setContentView(view);

        if (activity instanceof com.example.newsapplication.MainActivity) {
            ((com.example.newsapplication.MainActivity) activity).setEditProfileDialog(this);
        }

        initViews(view);
        setupClickListeners();
        loadCurrentUserData();
    }
    
    @Override
    public void dismiss() {
        if (activity instanceof com.example.newsapplication.MainActivity) {
            ((com.example.newsapplication.MainActivity) activity).setEditProfileDialog(null);
        }
        super.dismiss();
    }

    private void initViews(View view) {
        avatarImageView = view.findViewById(R.id.avatarImageView);
        displayNameEditText = view.findViewById(R.id.displayNameEditText);
        emailEditText = view.findViewById(R.id.emailEditText);
        changeAvatarText = view.findViewById(R.id.changeAvatarText);
        cancelButton = view.findViewById(R.id.cancelButton);
        saveButton = view.findViewById(R.id.saveButton);
        
        View closeButton = view.findViewById(R.id.closeButton);
        if (closeButton != null) {
            closeButton.setOnClickListener(v -> dismiss());
        }
    }

    private void setupClickListeners() {
        changeAvatarText.setOnClickListener(v -> openImagePicker());
        avatarImageView.setOnClickListener(v -> openImagePicker());
        
        cancelButton.setOnClickListener(v -> dismiss());
        
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void loadCurrentUserData() {
        if (sessionManager.isLoggedIn()) {
            String userName = sessionManager.getUserName();
            String email = sessionManager.getUserEmail();
            String avatarUrl = sessionManager.getAvatarUrl();
            
            displayNameEditText.setText(userName);
            emailEditText.setText(email);
            
            if (avatarUrl != null && !avatarUrl.isEmpty()) {
                currentAvatarUrl = avatarUrl;
                Picasso.get()
                        .load(avatarUrl)
                        .transform(new CircleTransform())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(avatarImageView);
            }
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        
        if (activity != null) {
            try {
                activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "No image picker available", Toast.LENGTH_SHORT).show();
            }
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
            
            if (imageUri != null) {
                selectedImageUri = imageUri;
                try {
                    Bitmap bitmap;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        android.graphics.ImageDecoder.Source source = android.graphics.ImageDecoder.createSource(getContext().getContentResolver(), imageUri);
                        bitmap = android.graphics.ImageDecoder.decodeBitmap(source);
                    } else {
                        bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
                    }
                    
                    if (bitmap != null) {
                        Bitmap circularBitmap = getCircularBitmap(bitmap);
                        avatarImageView.setImageBitmap(circularBitmap);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int x = (bitmap.getWidth() - size) / 2;
        int y = (bitmap.getHeight() - size) / 2;
        
        Bitmap squaredBitmap = Bitmap.createBitmap(bitmap, x, y, size, size);
        
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(output);
        
        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);
        paint.setShader(new android.graphics.BitmapShader(squaredBitmap, 
                android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP));
        
        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, paint);
        
        return output;
    }

    private void saveProfile() {
        String displayName = displayNameEditText.getText().toString().trim();
        
        if (displayName.isEmpty()) {
            Toast.makeText(getContext(), "Display name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        
        saveButton.setEnabled(false);
        
        if (selectedImageUri != null) {
            Toast.makeText(getContext(), "Uploading image...", Toast.LENGTH_SHORT).show();
            
            mediaEndpoints.uploadFile(selectedImageUri, "avatar.jpg", new com.example.newsapplication.api.endpoints.MediaEndpoints.UploadCallback() {
                @Override
                public void onSuccess(String fileUrl) {
                    currentAvatarUrl = fileUrl;
                    updateProfileWithApi(displayName, currentAvatarUrl);
                }

                @Override
                public void onError(String errorMessage) {
                    saveButton.setEnabled(true);
                    Toast.makeText(getContext(), "Failed to upload image: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            updateProfileWithApi(displayName, currentAvatarUrl);
        }
    }
    
    private void updateProfileWithApi(String displayName, String avatarUrl) {
        if (newsRepository != null) {
            newsRepository.updateProfile(displayName, avatarUrl, new com.example.newsapplication.repository.NewsRepository.RepositoryCallback<org.json.JSONObject>() {
                @Override
                public void onResult(com.example.newsapplication.api.ApiResponse<org.json.JSONObject> response) {
                    saveButton.setEnabled(true);
                    
                    if (response.isSuccess()) {
                        sessionManager.updateUserName(displayName);
                        
                        if (listener != null) {
                            listener.onProfileUpdated(displayName, avatarUrl);
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
}
