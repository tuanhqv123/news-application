package com.example.newsapplication.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.newsapplication.R;
import com.example.newsapplication.utils.FontSizeManager;

public class FontSizeDialog extends Dialog {

    public interface FontSizeCallback {
        void onFontSizeApplied(int fontSize);
    }

    private FontSizeCallback callback;
    private FontSizeManager fontSizeManager;
    private RadioGroup fontSizeRadioGroup;
    private TextView previewTextView;
    private Button applyFontButton;
    private Button resetFontButton;

    // Font size options in sp
    private static final int[] FONT_SIZES = {12, 14, 16, 18};
    private static final int DEFAULT_FONT_SIZE = 14; // Medium

    public FontSizeDialog(@NonNull Context context, FontSizeCallback callback) {
        super(context, android.R.style.Theme_Material_Light_Dialog);
        this.callback = callback;
        this.fontSizeManager = new FontSizeManager(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_font_size);

        initViews();
        setupListeners();
        loadCurrentFontSize();
    }

    private void initViews() {
        fontSizeRadioGroup = findViewById(R.id.fontSizeRadioGroup);
        previewTextView = findViewById(R.id.previewTextView);
        applyFontButton = findViewById(R.id.applyFontButton);
        resetFontButton = findViewById(R.id.resetFontButton);

        // Set default selection to medium
        fontSizeRadioGroup.check(R.id.mediumFontRadioButton);
    }

    private void setupListeners() {
        // Radio group change listener to update preview
        fontSizeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int selectedSize = getSelectedFontSize();
            updatePreview(selectedSize);
        });

        // Apply button
        applyFontButton.setOnClickListener(v -> {
            int selectedSize = getSelectedFontSize();
            // Save the font size
            fontSizeManager.setFontSize(selectedSize);
            if (callback != null) {
                callback.onFontSizeApplied(selectedSize);
            }
            dismiss();
        });

        // Reset button
        resetFontButton.setOnClickListener(v -> {
            fontSizeManager.resetToDefault();
            loadCurrentFontSize();
            if (callback != null) {
                callback.onFontSizeApplied(DEFAULT_FONT_SIZE);
            }
            dismiss();
        });
    }

    private void loadCurrentFontSize() {
        float currentSize = fontSizeManager.getFontSize();
        updatePreview((int) currentSize);

        // Update radio button selection
        if (currentSize <= 12f) {
            fontSizeRadioGroup.check(R.id.smallFontRadioButton);
        } else if (currentSize <= 14f) {
            fontSizeRadioGroup.check(R.id.mediumFontRadioButton);
        } else if (currentSize <= 16f) {
            fontSizeRadioGroup.check(R.id.largeFontRadioButton);
        } else {
            fontSizeRadioGroup.check(R.id.extraLargeFontRadioButton);
        }
    }

    private int getSelectedFontSize() {
        int checkedId = fontSizeRadioGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.smallFontRadioButton) {
            return FONT_SIZES[0];
        } else if (checkedId == R.id.mediumFontRadioButton) {
            return FONT_SIZES[1];
        } else if (checkedId == R.id.largeFontRadioButton) {
            return FONT_SIZES[2];
        } else if (checkedId == R.id.extraLargeFontRadioButton) {
            return FONT_SIZES[3];
        }
        return DEFAULT_FONT_SIZE;
    }

    private void updatePreview(int fontSize) {
        previewTextView.setTextSize(fontSize);
    }
}