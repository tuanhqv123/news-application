package com.example.newsapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import com.example.newsapplication.api.ApiResponse;
import com.example.newsapplication.api.endpoints.MediaEndpoints;
import com.example.newsapplication.model.Category;
import com.example.newsapplication.repository.NewsRepository;
import com.example.newsapplication.utils.JsonParsingUtils;
import com.google.android.material.textfield.TextInputEditText;
import jp.wasabeef.richeditor.RichEditor;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class CreateArticleActivity extends AppCompatActivity {

    private static final int PICK_HERO_IMAGE_REQUEST = 1;
    private static final int PICK_CONTENT_IMAGE_REQUEST = 2;

    private ViewFlipper stepFlipper;
    private int currentStep = 0;

    // Step indicators
    private TextView step1Indicator, step2Indicator, step3Indicator, step4Indicator;
    private View line1, line2, line3;
    private TextView stepTitle;

    // Step 1: Title & Category
    private TextInputEditText titleEditText;
    private Spinner categorySpinner;

    // Step 2: Hero Image
    private FrameLayout heroImageContainer;
    private ImageView heroImageView;
    private LinearLayout heroImagePlaceholder;
    private ProgressBar heroImageProgress;
    private TextView removeImageButton;
    private Uri selectedHeroImageUri;
    private String uploadedHeroImageUrl;

    // Step 3: Summary
    private TextInputEditText summaryEditText;

    // Step 4: Content with RichEditor
    private RichEditor richEditor;
    private LinearLayout contentImageProgress;

    // Navigation
    private AppCompatButton prevButton, nextButton, publishButton;

    // Data
    private NewsRepository newsRepository;
    private MediaEndpoints mediaEndpoints;
    private List<Category> categories = new ArrayList<>();
    private int selectedCategoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_article);

        newsRepository = new NewsRepository(this);
        mediaEndpoints = new MediaEndpoints(this);

        initViews();
        setupRichEditor();
        setupListeners();
        loadCategories();
        updateStepUI();
    }

    private void initViews() {
        stepFlipper = findViewById(R.id.stepFlipper);

        // Step indicators
        step1Indicator = findViewById(R.id.step1Indicator);
        step2Indicator = findViewById(R.id.step2Indicator);
        step3Indicator = findViewById(R.id.step3Indicator);
        step4Indicator = findViewById(R.id.step4Indicator);
        line1 = findViewById(R.id.line1);
        line2 = findViewById(R.id.line2);
        line3 = findViewById(R.id.line3);
        stepTitle = findViewById(R.id.stepTitle);

        // Step 1
        titleEditText = findViewById(R.id.titleEditText);
        categorySpinner = findViewById(R.id.categorySpinner);

        // Step 2
        heroImageContainer = findViewById(R.id.heroImageContainer);
        heroImageView = findViewById(R.id.heroImageView);
        heroImagePlaceholder = findViewById(R.id.heroImagePlaceholder);
        heroImageProgress = findViewById(R.id.heroImageProgress);
        removeImageButton = findViewById(R.id.removeImageButton);

        // Step 3
        summaryEditText = findViewById(R.id.summaryEditText);

        // Step 4
        richEditor = findViewById(R.id.richEditor);
        contentImageProgress = findViewById(R.id.contentImageProgress);

        // Navigation
        prevButton = findViewById(R.id.prevButton);
        nextButton = findViewById(R.id.nextButton);
        publishButton = findViewById(R.id.publishButton);

        // Back button
        findViewById(R.id.backButton).setOnClickListener(v -> onBackPressed());
    }

    private void setupRichEditor() {
        richEditor.setEditorHeight(200);
        richEditor.setEditorFontSize(16);
        richEditor.setEditorFontColor(getResources().getColor(R.color.black));
        richEditor.setPadding(16, 16, 16, 16);
        richEditor.setPlaceholder("Start writing your article...");

        // Toolbar actions
        findViewById(R.id.action_bold).setOnClickListener(v -> richEditor.setBold());
        findViewById(R.id.action_italic).setOnClickListener(v -> richEditor.setItalic());
        findViewById(R.id.action_underline).setOnClickListener(v -> richEditor.setUnderline());
        findViewById(R.id.action_heading1).setOnClickListener(v -> richEditor.setHeading(1));
        findViewById(R.id.action_heading2).setOnClickListener(v -> richEditor.setHeading(2));
        findViewById(R.id.action_bullet).setOnClickListener(v -> richEditor.setBullets());
        findViewById(R.id.action_number).setOnClickListener(v -> richEditor.setNumbers());
        findViewById(R.id.action_insert_link).setOnClickListener(v -> showInsertLinkDialog());
        findViewById(R.id.action_insert_image).setOnClickListener(v -> openContentImagePicker());
    }

    private void showInsertLinkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_insert_link, null);
        EditText urlEditText = dialogView.findViewById(R.id.urlEditText);
        EditText textEditText = dialogView.findViewById(R.id.textEditText);

        builder.setView(dialogView)
                .setTitle("Insert Link")
                .setPositiveButton("Insert", (dialog, which) -> {
                    String url = urlEditText.getText().toString().trim();
                    String text = textEditText.getText().toString().trim();
                    if (!url.isEmpty()) {
                        if (text.isEmpty()) text = url;
                        richEditor.insertLink(url, text);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openContentImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Image for Content"), PICK_CONTENT_IMAGE_REQUEST);
    }

    private void setupListeners() {
        prevButton.setOnClickListener(v -> goToPreviousStep());
        nextButton.setOnClickListener(v -> goToNextStep());
        publishButton.setOnClickListener(v -> publishArticle());

        heroImageContainer.setOnClickListener(v -> openHeroImagePicker());
        removeImageButton.setOnClickListener(v -> removeHeroImage());

        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0 && position <= categories.size()) {
                    selectedCategoryId = categories.get(position - 1).getId();
                } else {
                    selectedCategoryId = -1;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategoryId = -1;
            }
        });
    }

    private void loadCategories() {
        newsRepository.getCategories(new NewsRepository.RepositoryCallback<JSONObject>() {
            @Override
            public void onResult(ApiResponse<JSONObject> response) {
                if (response.isSuccess() && response.getData() != null) {
                    categories = JsonParsingUtils.parseCategories(response.getData());
                    setupCategorySpinner();
                }
            }
        });
    }

    private void setupCategorySpinner() {
        List<String> categoryNames = new ArrayList<>();
        categoryNames.add("Select a category");
        for (Category category : categories) {
            categoryNames.add(category.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void goToNextStep() {
        if (validateCurrentStep()) {
            if (currentStep < 3) {
                currentStep++;
                stepFlipper.setDisplayedChild(currentStep);
                updateStepUI();
            }
        }
    }

    private void goToPreviousStep() {
        if (currentStep > 0) {
            currentStep--;
            stepFlipper.setDisplayedChild(currentStep);
            updateStepUI();
        }
    }

    private boolean validateCurrentStep() {
        switch (currentStep) {
            case 0:
                String title = titleEditText.getText().toString().trim();
                if (title.isEmpty()) {
                    Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (selectedCategoryId == -1) {
                    Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;

            case 1:
                return true;

            case 2:
                String summary = summaryEditText.getText().toString().trim();
                if (summary.isEmpty()) {
                    Toast.makeText(this, "Please enter a summary", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;

            case 3:
                String content = richEditor.getHtml();
                if (content == null || content.trim().isEmpty()) {
                    Toast.makeText(this, "Please enter content", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return true;

            default:
                return true;
        }
    }

    private void updateStepUI() {
        updateStepIndicator(step1Indicator, currentStep >= 0);
        updateStepIndicator(step2Indicator, currentStep >= 1);
        updateStepIndicator(step3Indicator, currentStep >= 2);
        updateStepIndicator(step4Indicator, currentStep >= 3);

        line1.setBackgroundColor(getResources().getColor(currentStep >= 1 ? R.color.primary : R.color.gray));
        line2.setBackgroundColor(getResources().getColor(currentStep >= 2 ? R.color.primary : R.color.gray));
        line3.setBackgroundColor(getResources().getColor(currentStep >= 3 ? R.color.primary : R.color.gray));

        String[] titles = {"Step 1: Basic Info", "Step 2: Hero Image", "Step 3: Summary", "Step 4: Content"};
        stepTitle.setText(titles[currentStep]);

        prevButton.setVisibility(currentStep > 0 ? View.VISIBLE : View.INVISIBLE);
        nextButton.setVisibility(currentStep < 3 ? View.VISIBLE : View.GONE);
    }

    private void updateStepIndicator(TextView indicator, boolean active) {
        indicator.setBackgroundResource(active ? R.drawable.step_active_background : R.drawable.step_inactive_background);
        indicator.setTextColor(getResources().getColor(active ? android.R.color.white : R.color.gray));
    }

    private void openHeroImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Hero Image"), PICK_HERO_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            
            if (requestCode == PICK_HERO_IMAGE_REQUEST) {
                selectedHeroImageUri = imageUri;
                uploadHeroImage();
            } else if (requestCode == PICK_CONTENT_IMAGE_REQUEST) {
                uploadContentImage(imageUri);
            }
        }
    }

    private void uploadHeroImage() {
        heroImagePlaceholder.setVisibility(View.GONE);
        heroImageProgress.setVisibility(View.VISIBLE);
        removeImageButton.setVisibility(View.GONE);

        mediaEndpoints.uploadFile(selectedHeroImageUri, "article_hero.jpg", new MediaEndpoints.UploadCallback() {
            @Override
            public void onSuccess(String fileUrl) {
                uploadedHeroImageUrl = fileUrl;
                heroImageProgress.setVisibility(View.GONE);
                heroImageView.setVisibility(View.VISIBLE);
                removeImageButton.setVisibility(View.VISIBLE);

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedHeroImageUri);
                    heroImageView.setImageBitmap(bitmap);
                } catch (Exception e) {
                    heroImageView.setImageURI(selectedHeroImageUri);
                }
            }

            @Override
            public void onError(String errorMessage) {
                heroImageProgress.setVisibility(View.GONE);
                heroImagePlaceholder.setVisibility(View.VISIBLE);
                Toast.makeText(CreateArticleActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadContentImage(Uri imageUri) {
        contentImageProgress.setVisibility(View.VISIBLE);

        String fileName = "content_image_" + System.currentTimeMillis() + ".jpg";
        mediaEndpoints.uploadFile(imageUri, fileName, new MediaEndpoints.UploadCallback() {
            @Override
            public void onSuccess(String fileUrl) {
                contentImageProgress.setVisibility(View.GONE);
                richEditor.insertImage(fileUrl, "Article image", 300);
            }

            @Override
            public void onError(String errorMessage) {
                contentImageProgress.setVisibility(View.GONE);
                Toast.makeText(CreateArticleActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeHeroImage() {
        selectedHeroImageUri = null;
        uploadedHeroImageUrl = null;
        heroImageView.setVisibility(View.GONE);
        heroImageView.setImageBitmap(null);
        heroImagePlaceholder.setVisibility(View.VISIBLE);
        removeImageButton.setVisibility(View.GONE);
    }

    private void publishArticle() {
        for (int i = 0; i < 4; i++) {
            currentStep = i;
            if (!validateCurrentStep()) {
                stepFlipper.setDisplayedChild(currentStep);
                updateStepUI();
                return;
            }
        }

        String title = titleEditText.getText().toString().trim();
        String summary = summaryEditText.getText().toString().trim();
        String content = richEditor.getHtml();

        publishButton.setEnabled(false);
        publishButton.setText("Publishing...");

        newsRepository.createArticle(title, summary, content, selectedCategoryId,
                null, null, uploadedHeroImageUrl, null,
                new NewsRepository.RepositoryCallback<JSONObject>() {
                    @Override
                    public void onResult(ApiResponse<JSONObject> response) {
                        publishButton.setEnabled(true);
                        publishButton.setText("Publish");

                        if (response.isSuccess()) {
                            Toast.makeText(CreateArticleActivity.this, "Article created successfully!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(CreateArticleActivity.this, response.getErrorMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
