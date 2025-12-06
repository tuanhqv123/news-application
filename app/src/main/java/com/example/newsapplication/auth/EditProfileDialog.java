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
    private static final int PICK_IMAGE_REQUEST = 1001;

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

        try {
            if (activity != null) {
                // Use startActivityForResult from the activity
                activity.startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error opening image picker: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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
                Bitmap bitmap = null;
                java.io.InputStream inputStream = null;

                try {
                    // Try to get persistable URI permission (might fail for some URIs)
                    try {
                        getContext().getContentResolver().takePersistableUriPermission(imageUri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (Exception e) {
                        // Permission might already be granted or not needed, continue
                    }

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        android.graphics.ImageDecoder.Source source = android.graphics.ImageDecoder.createSource(getContext().getContentResolver(), imageUri);
                        android.graphics.ImageDecoder.OnHeaderDecodedListener listener = (decoder, info, source2) -> {
                            // Disable hardware bitmap to avoid software rendering issues
                            decoder.setAllocator(android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE);
                        };
                        bitmap = android.graphics.ImageDecoder.decodeBitmap(source, listener);
                    } else {
                        // For older versions, use BitmapFactory with options
                        android.graphics.BitmapFactory.Options options = new android.graphics.BitmapFactory.Options();
                        options.inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888;
                        options.inScaled = true;
                        options.inJustDecodeBounds = false;

                        // Get the input stream with proper error handling
                        inputStream = getContext().getContentResolver().openInputStream(imageUri);
                        if (inputStream != null) {
                            bitmap = android.graphics.BitmapFactory.decodeStream(inputStream, null, options);
                        }
                    }

                    if (bitmap != null) {
                        Bitmap circularBitmap = getCircularBitmap(bitmap);
                        avatarImageView.setImageBitmap(circularBitmap);
                    } else {
                        Toast.makeText(getContext(), "Could not load the selected image", Toast.LENGTH_SHORT).show();
                    }
                } catch (SecurityException e) {
                    Toast.makeText(getContext(), "Permission denied to access image", Toast.LENGTH_SHORT).show();
                } catch (OutOfMemoryError e) {
                    Toast.makeText(getContext(), "Image is too large, please choose a smaller image", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                } finally {
                    // Close input stream if it was opened
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                            // Ignore close errors
                        }
                    }
                }
            }
        }
    }
    
    private Bitmap getCircularBitmap(Bitmap bitmap) {
        // Scale down the bitmap if it's too large to prevent memory issues
        int maxSize = 1024; // Maximum size for processing
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        if (width > maxSize || height > maxSize) {
            float scale = Math.min((float)maxSize / width, (float)maxSize / height);
            width = (int)(width * scale);
            height = (int)(height * scale);
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
        }

        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        int x = (bitmap.getWidth() - size) / 2;
        int y = (bitmap.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(bitmap, x, y, size, size);

        // Create output bitmap with software config to avoid hardware issues
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(output);

        android.graphics.Paint paint = new android.graphics.Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        paint.setShader(new android.graphics.BitmapShader(squaredBitmap,
                android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP));

        float radius = size / 2f;
        canvas.drawCircle(radius, radius, radius, paint);

        // Recycle the squared bitmap to free memory
        if (squaredBitmap != bitmap) {
            squaredBitmap.recycle();
        }

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
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
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

                        // Save avatar URL to session manager if updated
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            sessionManager.setAvatarUrl(avatarUrl);
                        }

                        if (listener != null) {
                            listener.onProfileUpdated(displayName, avatarUrl);
                        }

                        Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        dismiss();
                    } else {
                        Toast.makeText(getContext(), response.getErrorMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
}
